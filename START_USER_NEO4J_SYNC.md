# 🚀 Quick Start Guide - User & Task Neo4j Sync

## Current Status

✅ **Graph Service** - Running (port 8090)  
❌ **User Service** - NOT running  
❌ **Task Service** - Has configuration conflicts (needs fix)

---

## Step-by-Step Setup

### 1. Start User Service

```powershell
cd user-service
mvn clean compile
mvn spring-boot:run
```

Wait for the log message:
```
Started UserServiceApplication in X.XXX seconds
```

### 2. Test User Creation & Sync to Neo4j

#### Create a test user:

```powershell
curl -X POST http://localhost:8083/users `
  -H "Content-Type: application/json" `
  -d '{
    "username": "testuser123",
    "password": "password123",
    "roles": ["ROLE_USER"]
  }'
```

#### Check user-service logs for:

**✅ SUCCESS:**
```
✅ User testuser123 synced to graph successfully
```

**⚠️ FAILURE:**
```
⚠️ Failed to sync user to graph: <error message>
```

#### Verify in Neo4j Browser:

1. Open http://localhost:7474
2. Login: `neo4j` / `password`
3. Run query:

```cypher
MATCH (u:User) 
RETURN u.id, u.username 
ORDER BY u.id DESC 
LIMIT 10
```

---

## Troubleshooting

### Issue: User service won't start

**Check if port 8083 is in use:**
```powershell
netstat -ano | findstr :8083
```

If in use, kill the process or change the port in `application.properties`.

### Issue: "Failed to sync user to graph"

**Possible causes:**

1. **Graph service not running**
   ```powershell
   curl http://localhost:8090/api/graph/sync/health
   ```
   Should return: `Graph Sync API is healthy`

2. **Eureka not running** (if using service discovery)
   - Start: `cd infra/eureka-server && mvn spring-boot:run`

3. **Network issues**
   - Check firewall settings
   - Try direct URL: `graph.service.url=http://localhost:8090` in properties

### Issue: User created but NOT in Neo4j

**Check logs for these errors:**

| Log Message | Problem | Solution |
|-------------|---------|----------|
| `GraphClient bean not found` | Feign not configured | Check `@EnableFeignClients` |
| `Connection refused` | Graph service down | Start graph-service |
| `404 Not Found` | Wrong endpoint | Verify endpoint: `/api/graph/sync/user` |
| `503 Service Unavailable` | Fallback triggered | Graph service unreachable |

---

## Manual Test - Bypass User Service

If user-service has issues, test graph-service directly:

```powershell
curl -X POST http://localhost:8090/api/graph/sync/user `
  -H "Content-Type: application/json" `
  -d '{
    "id": "999",
    "username": "manual_test",
    "email": null,
    "department": null,
    "role": "ROLE_USER",
    "managerId": null
  }'
```

Then check Neo4j:
```cypher
MATCH (u:User {id: '999'}) RETURN u
```

If this works, the problem is in user-service's Feign client configuration.

---

## Configuration Check

### user-service/src/main/resources/application.properties

Required settings:

```properties
# Eureka (for service discovery)
eureka.client.enabled=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Feign
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
feign.hystrix.enabled=true

# Graph Service URL (fallback if Eureka fails)
graph.service.url=http://localhost:8090
```

### UserServiceApplication.java

Must have:

```java
@SpringBootApplication
@EnableCaching
@EnableFeignClients(basePackages = "com.example.user_service.client")
public class UserServiceApplication {
    // ...
}
```

---

## Expected Flow

```
User creates account via API
    ↓
UserService.addUser() called
    ↓
User saved to MySQL/H2 ✅
    ↓
GraphClient.syncUser() called (Feign)
    ↓
HTTP POST to graph-service:8090
    ↓
GraphSyncService.syncUser() processes
    ↓
UserNode saved to Neo4j ✅
```

---

## Quick Verification Checklist

Run these commands in order:

```powershell
# 1. Graph service health
curl http://localhost:8090/api/graph/sync/health

# 2. User service health  
curl http://localhost:8083/actuator/health

# 3. Neo4j browser access
Start-Process "http://localhost:7474"

# 4. Create test user
curl -X POST http://localhost:8083/users `
  -H "Content-Type: application/json" `
  -d '{"username":"test1","password":"pass","roles":["ROLE_USER"]}'

# 5. Check Neo4j (run in browser)
# MATCH (u:User) RETURN u LIMIT 10
```

---

## Common Errors & Fixes

### Error: "package lombok does not exist"

**Fixed!** I've rewritten the DTOs without Lombok. Just rebuild:
```powershell
mvn clean compile
```

### Error: "Conflicting bean definition: corsConfig"

**Fixed!** Deleted duplicate CorsConfig. Rebuild if needed.

### Error: "Conflicting bean definition: taskController"

**For task-service only.** Still needs fixing - not critical for user sync.

---

## Next Steps After Success

Once users are syncing to Neo4j:

1. **Query user relationships:**
   ```cypher
   MATCH (u:User)
   OPTIONAL MATCH (u)<-[:ASSIGNED_TO]-(t:Task)
   RETURN u.username, count(t) as task_count
   ```

2. **Test task sync** (after fixing task-service)

3. **Build more complex queries** for user activity analysis

---

**Created**: October 26, 2025  
**Status**: Ready to test  
**Priority**: High - User service must be running for sync to work!


# Service Communication Improvements

## Overview
This document describes the improvements made to inter-service communication in the PLM system after API Gateway integration.

## Architecture Pattern

### External Requests (Frontend → Backend)
```
Frontend (React)
  ↓
NGINX (Port 8111)
  ↓
API Gateway (Port 8080)
  - JWT Authentication
  - CORS Handling
  - Request Routing
  ↓
Backend Services (8081-8091)
```

### Internal Service-to-Service Communication
```
Backend Service A
  ↓
Eureka Service Discovery
  ↓
Backend Service B (Direct Connection)
```

**Key Point:** Internal service communication **bypasses** the API Gateway and uses **Eureka** for service discovery. This is the correct microservices pattern because:
- Lower latency (no extra hop)
- No authentication needed (trusted internal network)
- Better resilience
- Simpler configuration

## Changes Made

### 1. Task Service Database Configuration
**File:** `task-service/src/main/resources/application.yml`

**Change:** Reverted from H2 (dev) back to MySQL (production)
```yaml
spring:
  profiles:
    active: default  # Uses MySQL production database
```

**Database:**
- Host: `localhost:3306`
- Database: `plm_task_db`
- Username: `plm_user`
- Password: `plm_password`

### 2. Workflow Orchestrator Feign Configuration
**File:** `workflow-orchestrator/src/main/resources/application.yml`

**Added:**
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 10000  # 10 seconds
        readTimeout: 30000     # 30 seconds
        loggerLevel: full
      task-service:
        connectTimeout: 15000  # Longer for DB operations
        readTimeout: 45000
      user-service:
        connectTimeout: 10000
        readTimeout: 30000
      document-service:
        connectTimeout: 10000
        readTimeout: 30000
```

**Why:** 
- Previous default timeouts were too short (5 seconds)
- Task Service needs longer timeouts for database operations
- Prevents "Unexpected end of file" errors

### 3. Document Service Feign Configuration
**File:** `document-service/src/main/resources/application.properties`

**Added:**
```properties
# Default timeouts
feign.client.config.default.connectTimeout=10000
feign.client.config.default.readTimeout=30000
feign.client.config.default.loggerLevel=full

# Workflow Orchestrator - longer timeout
feign.client.config.workflow-orchestrator.connectTimeout=15000
feign.client.config.workflow-orchestrator.readTimeout=45000

# Other services
feign.client.config.graph-service.connectTimeout=10000
feign.client.config.graph-service.readTimeout=30000
feign.client.config.search-service.connectTimeout=10000
feign.client.config.search-service.readTimeout=30000
```

**Why:**
- Prevents timeout errors when calling Workflow Orchestrator
- Allows time for workflow process creation

### 4. Task Service Feign Configuration
**File:** `task-service/src/main/resources/application.yml`

**Updated:**
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 10000  # Increased from 5000
        readTimeout: 30000     # Increased from 5000
        loggerLevel: full
      user-service:
        connectTimeout: 10000
        readTimeout: 30000
      graph-service:
        connectTimeout: 10000
        readTimeout: 30000
      workflow-orchestrator:
        connectTimeout: 15000
        readTimeout: 45000
```

**Why:**
- Task Service calls User Service to resolve usernames
- Needs adequate timeout for user lookups

### 5. Enhanced Logging
**Added to both Workflow Orchestrator and Document Service:**

**Workflow Orchestrator:**
```yaml
logging:
  level:
    feign: DEBUG
    org.springframework.cloud.openfeign: DEBUG
    com.example.plm.workflow.client: DEBUG
```

**Document Service:**
```properties
logging.level.feign=DEBUG
logging.level.org.springframework.cloud.openfeign=DEBUG
logging.level.com.example.document_service.client=DEBUG
```

**Why:**
- Better debugging of service communication issues
- Can see actual HTTP requests/responses
- Helps diagnose timeout and connection problems

## Service Discovery (Eureka)

All services are registered with Eureka:
- Eureka Server: `http://localhost:8761/eureka/`
- Services register their hostname and port
- Services fetch registry to discover other services
- Feign clients use service names (e.g., `task-service`) which Eureka resolves

**Configuration Example:**
```yaml
eureka:
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Troubleshooting

### "Unexpected end of file from server" Error

**Causes:**
1. Target service is not running
2. Target service is not registered in Eureka
3. Timeout too short for operation
4. Target service crashes mid-request

**Solutions:**
1. Check service is running: `curl http://localhost:PORT/actuator/health`
2. Check Eureka registry: `http://localhost:8761/`
3. Increase Feign timeout (done in this update)
4. Check target service logs for exceptions

### Service Not Found

**Cause:** Service not registered in Eureka

**Check:**
1. Open Eureka dashboard: `http://localhost:8761/`
2. Verify service appears in the list
3. Check service logs for Eureka registration success

**Fix:**
- Ensure `eureka.client.enabled=true`
- Ensure `eureka.client.register-with-eureka=true`
- Wait 30-60 seconds for registration

### Connection Refused

**Cause:** Service is down or wrong port

**Fix:**
1. Verify service is running on correct port
2. Check `server.port` configuration
3. Restart service

## Testing Service Communication

### Test Script: `test-service-communication.ps1`
```powershell
# Check all services are registered in Eureka
$eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"}
$services = $eureka.applications.application.name
Write-Host "Registered Services: $($services -join ', ')"

# Check each service health
@("8081", "8082", "8086") | ForEach-Object {
    $port = $_
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health"
        Write-Host "Port $port : $($health.status)"
    } catch {
        Write-Host "Port $port : DOWN"
    }
}
```

## Expected Service Flow: Document Review

1. **Frontend submits document for review**
   ```
   POST http://localhost:8111/api/documents/{id}/submit-review
   ```

2. **NGINX routes to API Gateway**
   ```
   POST http://localhost:8080/api/documents/{id}/submit-review
   ```

3. **API Gateway validates JWT and routes to Document Service**
   ```
   POST http://localhost:8081/api/v1/documents/{id}/submit-review
   ```

4. **Document Service calls Workflow Orchestrator (via Eureka)**
   ```
   Document Service → Eureka (resolve workflow-orchestrator) → Workflow Orchestrator
   POST http://workflow-orchestrator/api/workflows/document-approval/start
   ```

5. **Workflow Orchestrator calls Task Service (via Eureka)**
   ```
   Workflow Orchestrator → Eureka (resolve task-service) → Task Service
   POST http://task-service/api/tasks
   ```

6. **Workflow Orchestrator calls User Service (via Eureka)**
   ```
   Workflow Orchestrator → Eureka (resolve user-service) → User Service  
   GET http://user-service/users/{id}
   ```

7. **Review task created in Task Service**
   - Reviewer sees task in frontend
   - Task linked to workflow job key
   - Completing task triggers workflow continuation

## Next Steps to Apply Changes

### 1. Stop Services
Stop these services (Ctrl+C in their windows):
- Task Service (Port 8082)
- Document Service (Port 8081)
- Workflow Orchestrator (Port 8086)

### 2. Compile Changes
```bash
# Workflow Orchestrator
cd workflow-orchestrator
mvn clean compile

# Document Service
cd ../document-service
mvn clean compile

# Task Service  
cd ../task-service
mvn clean compile
```

### 3. Restart Services
Restart in this order (wait for each to fully start):

1. **Task Service** (needs MySQL running)
   ```bash
   cd task-service
   mvn spring-boot:run
   ```
   Wait for: `Started TaskServiceApplication`

2. **Document Service**
   ```bash
   cd document-service
   mvn spring-boot:run
   ```
   Wait for: `Started DocumentServiceApplication`

3. **Workflow Orchestrator**
   ```bash
   cd workflow-orchestrator
   mvn spring-boot:run
   ```
   Wait for: `Started WorkflowOrchestratorApplication`

### 4. Verify
1. Check Eureka: `http://localhost:8761/`
   - Should see: TASK-SERVICE, DOCUMENT-SERVICE, WORKFLOW-ORCHESTRATOR

2. Test document submission:
   - Upload a document
   - Submit for review with reviewers
   - Check Workflow Orchestrator logs for task creation
   - Verify reviewer sees the task

## Summary

These changes improve the resilience and reliability of inter-service communication by:
- ✅ Adding appropriate timeouts for Feign clients
- ✅ Configuring service-specific timeouts based on operation complexity
- ✅ Enabling detailed logging for troubleshooting
- ✅ Documenting the correct architecture pattern
- ✅ Reverting Task Service to production database

The system now properly handles:
- Long-running database operations
- Workflow process creation
- User service lookups
- Network latency variations






# ✅ Verify User Sync to Neo4j

## User Created Successfully

**User ID**: 33  
**Username**: neo4j_test_082528  
**Created in**: MySQL/H2 database ✅

---

## Step 1: Check Neo4j Browser

1. **Open Neo4j Browser**: http://localhost:7474

2. **Login**:
   - Username: `neo4j`
   - Password: `password`

3. **Run this query**:
```cypher
MATCH (u:User {id: '33'}) 
RETURN u.id, u.username, u.role
```

### Expected Results:

**✅ If sync is working:**
```
u.id  | u.username         | u.role
------|-------------------|----------
"33"  | "neo4j_test_082528"| "ROLE_USER"
```

**❌ If sync is NOT working:**
- Query returns no results
- This means the sync failed or didn't happen

---

## Step 2: Check All Users in Neo4j

Run this to see all users:
```cypher
MATCH (u:User) 
RETURN u.id, u.username, u.role 
ORDER BY u.id DESC 
LIMIT 10
```

---

## Step 3: Check User-Service Logs

Look for the user-service console/log window and search for:

**✅ Success message:**
```
✅ User neo4j_test_082528 synced to graph successfully
```

**⚠️ Warning message:**
```
⚠️ Failed to sync user to graph: <error message>
```

**To find the log:**
- Look for process ID 22480 (the running user-service)
- Or check: `user-service/target/` for log files
- Or use `start-all-services.bat` output

---

## Troubleshooting

### If User is NOT in Neo4j:

1. **Check graph-service is running:**
```powershell
curl http://localhost:8090/api/graph/sync/health
```
Should return: `Graph Sync API is healthy`

2. **Test graph-service directly:**
```powershell
$testBody = @{
    id="999"
    username="direct_test"
    email=$null
    department=$null
    role="ROLE_USER"
    managerId=$null
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8090/api/graph/sync/user" -Method Post -Body $testBody -ContentType "application/json"
```

Then check Neo4j for user ID 999:
```cypher
MATCH (u:User {id: '999'}) RETURN u
```

3. **Check Eureka (if enabled):**
```powershell
curl http://localhost:8761
```

---

## Quick Test Script

Run this to create another user and verify:

```powershell
# Create user
$body = @{
    username="test_$(Get-Date -Format 'mmss')"
    password="pass123"
    roles=@("ROLE_USER")
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri "http://localhost:8083/users" -Method Post -Body $body -ContentType "application/json"
Write-Host "Created user ID: $($result.id)" -ForegroundColor Green

# Wait a moment
Start-Sleep -Seconds 2

# Check in Neo4j
Write-Host "`nNow run this in Neo4j Browser:" -ForegroundColor Cyan
Write-Host "MATCH (u:User {id: '$($result.id)'}) RETURN u" -ForegroundColor White
```

---

## Common Issues

| Issue | Solution |
|-------|----------|
| User in MySQL but not Neo4j | Graph-service not running or unreachable |
| "Graph service unavailable" | Check graph-service on port 8090 |
| No sync logs at all | GraphClient not being called - check code |
| Sync logs but Neo4j empty | Neo4j connection issue in graph-service |

---

## Next Steps

Once you confirm users are syncing:

1. ✅ **Update a user** - Test update sync
2. ✅ **Delete a user** - Test delete sync  
3. ✅ **Query relationships** - Test graph queries
4. 🔧 **Fix task-service** - Then test task sync

---

**What to do RIGHT NOW:**

1. Open http://localhost:7474 in your browser
2. Login: `neo4j` / `password`
3. Run: `MATCH (u:User {id: '33'}) RETURN u`
4. Tell me: **Did you see the user in Neo4j?** (Yes/No)


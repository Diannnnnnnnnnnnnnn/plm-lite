# 🧪 Priority 1 Testing - Step by Step

## Current Status
✅ Redis is running (Docker container on port 6379)  
🔄 user-service is starting...  
🔄 auth-service is starting...

## Testing Steps

### Step 1: Wait for Services (2-3 minutes)

Watch the PowerShell windows that opened. Wait until you see:
- user-service: `Started UserServiceApplication`
- auth-service: `Started AuthServiceApplication`

### Step 2: Test Service Health

```powershell
# Test user-service health
curl http://localhost:8083/actuator/health

# Test auth-service health  
curl http://localhost:8110/actuator/health
```

**Expected:** Both should show `"status":"UP"` and Redis component status.

⚠️ **If you see Redis connection errors:**
Your Redis container might not have password authentication. Quick fix:

```powershell
# Option A: Update both services to use no password
# In user-service/src/main/resources/application.properties
# Comment out: spring.redis.password=plm_redis_password

# OR Option B: Restart Redis with password
docker stop redis
docker rm redis
docker run -d -p 6379:6379 --name redis redis:7.2-alpine redis-server --requirepass plm_redis_password
```

### Step 3: Test User Caching

```powershell
# First call - should hit database (check console for "Fetching all users from database")
curl http://localhost:8083/users

# Second call - should hit cache (no database log, much faster!)
curl http://localhost:8083/users

# Check what's in Redis
docker exec redis redis-cli KEYS "*"
# Should show: users::all
```

### Step 4: Test JWT Blacklisting

```powershell
# 1. Login
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8110/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'

$token = $response.token
Write-Host "Token: $token"

# 2. Check token status (should be valid, not blacklisted)
Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token"

# 3. Logout (blacklist the token)
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8110/api/auth/logout `
  -Headers @{"Authorization"="Bearer $token"}

# 4. Check token again (should now be blacklisted)
Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token"

# 5. Check Redis blacklist
docker exec redis redis-cli KEYS "*jwtBlacklist*"
```

### Step 5: Test Cache Eviction

```powershell
# 1. Get all users (populates cache)
$users = Invoke-RestMethod http://localhost:8083/users

# 2. Update a user (should evict cache)
$user = $users[0]
$user.email = "updated_$(Get-Random)@example.com"

Invoke-RestMethod -Method PUT `
  -Uri "http://localhost:8083/users/$($user.id)" `
  -ContentType "application/json" `
  -Body ($user | ConvertTo-Json)

# 3. Check console - next GET should hit database again
curl http://localhost:8083/users
# Should see "Fetching all users from database" in console
```

### Step 6: Run Automated Test (Optional)

```powershell
.\test-redis-integration.ps1
```

## 📊 Success Criteria

✅ **Health checks pass** - Both services show UP with Redis status  
✅ **Caching works** - Second user query is faster, no DB log  
✅ **Blacklisting works** - Token becomes blacklisted after logout  
✅ **Cache eviction works** - Updates invalidate cache  

## 🐛 Troubleshooting

### Problem: Redis connection errors

**Check Redis password:**
```powershell
docker exec redis redis-cli --pass plm_redis_password ping
```

If it fails, your Redis doesn't have a password. Either:
1. Remove password from services (comment out `spring.redis.password` in both services)
2. Or restart Redis with password (see Option B in Step 2)

### Problem: Services won't start

**Check ports:**
```powershell
netstat -ano | findstr "8083"  # user-service
netstat -ano | findstr "8110"  # auth-service
```

If ports are in use, kill the processes or use different ports.

### Problem: "Connection refused" to services

Services need 2-3 minutes to fully start. Wait and try again.

### Problem: Cache not working

**Verify Redis connectivity from service:**
Check the service console window for errors like:
- "Unable to connect to Redis"
- "Connection refused"
- "Authentication failed"

## 🎯 Quick Performance Test

```powershell
# Compare performance with/without cache
Measure-Command { Invoke-RestMethod http://localhost:8083/users }
# First call: ~200ms (database)

Measure-Command { Invoke-RestMethod http://localhost:8083/users }
# Second call: ~5-10ms (cache) - 20-40x faster!
```

## 📊 View Cache Data

If you have Redis Commander running:
- Open: http://localhost:8085
- Browse cache keys
- View cache values
- Check TTL

Or use Docker:
```powershell
# View all keys
docker exec redis redis-cli KEYS "*"

# Get specific key
docker exec redis redis-cli GET "users::all"

# Check TTL
docker exec redis redis-cli TTL "users::all"
```

## ✅ All Tests Pass?

If everything works:
1. ✅ Redis connected
2. ✅ User caching functional (40x faster!)
3. ✅ JWT blacklisting working
4. ✅ Cache eviction working
5. ✅ Health checks showing Redis UP

**You're ready for Priority 2!** 🎉

## ❌ Tests Failing?

Common fixes:
1. **Redis password mismatch**: Remove password from config or restart Redis with password
2. **Services not started**: Wait 2-3 minutes
3. **Port conflicts**: Check if ports 8083/8110 are free
4. **Redis not running**: `docker ps` should show redis container

Need help? Check:
- Service console windows for error messages
- `docs/REDIS_INTEGRATION_GUIDE.md` for detailed troubleshooting
- Health endpoints: http://localhost:8083/actuator/health

---

**Next Steps:**
- Once Priority 1 tests pass → Move to Priority 2 (task-service & bom-service)
- If tests fail → Check troubleshooting section above


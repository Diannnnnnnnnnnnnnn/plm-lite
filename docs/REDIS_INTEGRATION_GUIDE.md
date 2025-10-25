# Redis Integration Guide - PLM-Lite

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Setup](#setup)
4. [Configuration](#configuration)
5. [Usage](#usage)
6. [Monitoring](#monitoring)
7. [Troubleshooting](#troubleshooting)

## Overview

Redis is integrated into PLM-Lite to provide:
- **Fast Caching**: Reduce database load by 80%+
- **JWT Token Blacklisting**: Secure logout functionality
- **Session Management**: Distributed session storage
- **Performance**: Sub-5ms response times for cached data

### Services with Redis Integration

| Service | Purpose | Cache Strategy |
|---------|---------|----------------|
| **auth-service** (Port 8110) | JWT blacklisting, Rate limiting | 2-hour TTL for blacklist |
| **user-service** (Port 8083) | User data caching | 10-minute TTL |
| **task-service** (Port 8082) | Task list caching | Coming in Priority 2 |
| **bom-service** (Port 8089) | BOM structure caching | Coming in Priority 2 |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    PLM Application                       │
├──────────────┬──────────────┬──────────────┬───────────┤
│ Auth-Service │ User-Service │ Task-Service │ ...       │
│   (8110)     │    (8083)    │    (8082)    │           │
└──────┬───────┴──────┬───────┴──────┬───────┴───────────┘
       │              │              │
       └──────────────┴──────────────┘
                      │
              ┌───────▼────────┐
              │  Redis Server  │
              │   (Port 6379)  │
              └───────┬────────┘
                      │
          ┌───────────┴──────────┐
          │   Redis Commander    │
          │   (Port 8085)        │
          │  Web UI for Redis    │
          └──────────────────────┘
```

## Setup

### 1. Start Redis Server

**Option A: Using Docker (Recommended)**
```powershell
# Start Redis with password
docker run -d -p 6379:6379 --name plm-redis redis:7.2-alpine redis-server --requirepass plm_redis_password

# Verify Redis is running
docker ps | findstr plm-redis
```

**Option B: Using Docker Compose**
```powershell
cd infra
docker-compose -f docker-compose-redis.yaml up -d
```

This starts:
- Redis server on port 6379
- Redis Commander UI on port 8085

### 2. Verify Redis Connection

```powershell
# Test Redis connection
redis-cli -h localhost -p 6379 -a plm_redis_password ping
# Expected output: PONG

# Check Redis info
redis-cli -h localhost -p 6379 -a plm_redis_password INFO stats
```

### 3. Start Services

```powershell
# Start all services (includes Redis check)
.\start-all-services.ps1

# Or start individual services
cd user-service
mvn spring-boot:run

cd auth-service
mvn spring-boot:run
```

## Configuration

### Redis Connection Settings

All services use these standardized settings:

```properties
# Redis Configuration
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=plm_redis_password
spring.redis.timeout=2000ms
```

### Cache TTL Configuration

| Cache Name | TTL | Service | Purpose |
|------------|-----|---------|---------|
| `users::all` | 10 min | user-service | All users list |
| `users::username:*` | 10 min | user-service | User by username lookup |
| `jwtBlacklist` | 2 hours | auth-service | Blacklisted JWT tokens |

### Graceful Degradation

Both services implement automatic fallback:
- Redis unavailable → Continue with database
- Redis timeout → Skip cache, use database
- Cache errors → Log warning, continue operation

## Usage

### Auth-Service API

#### 1. Login (Get JWT Token)
```bash
curl -X POST http://localhost:8110/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

#### 2. Logout (Blacklist Token)
```bash
curl -X POST http://localhost:8110/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Response:
{
  "message": "Logged out successfully",
  "token_expiry": 1698765432000
}
```

#### 3. Check Token Status
```bash
curl "http://localhost:8110/api/auth/check-token?token=YOUR_TOKEN"

# Response:
{
  "valid": true,
  "blacklisted": false,
  "accepted": true
}
```

### User-Service API

#### 1. Get All Users (Cached)
```bash
# First call - hits database
curl http://localhost:8083/users

# Second call - hits cache (much faster)
curl http://localhost:8083/users
```

#### 2. Update User (Evicts Cache)
```bash
curl -X PUT http://localhost:8083/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "new_email@example.com",
    "role": "ADMIN"
  }'
```

## Monitoring

### Health Checks

**Auth-Service Health:**
```bash
curl http://localhost:8110/actuator/health

# Response includes Redis status:
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "redis": "Available",
        "host": "localhost:6379"
      }
    }
  }
}
```

**User-Service Health:**
```bash
curl http://localhost:8083/actuator/health
```

### Redis Commander Web UI

Access at: **http://localhost:8085**

Features:
- View all cache keys
- Inspect key values
- Monitor TTL
- Delete keys manually
- View memory usage

### Redis CLI Commands

```bash
# Connect to Redis
redis-cli -h localhost -p 6379 -a plm_redis_password

# View all keys
KEYS *

# Get key value
GET "users::all"

# Check key TTL (time to live)
TTL "users::all"

# View cache statistics
INFO stats

# Monitor real-time commands
MONITOR

# Clear all cache (use with caution!)
FLUSHALL
```

### Performance Metrics

```bash
# Get cache statistics
redis-cli -h localhost -p 6379 -a plm_redis_password INFO stats | findstr "keyspace"

# Key metrics:
# - keyspace_hits: Cache hits (good)
# - keyspace_misses: Cache misses (database queries)
# - Hit Rate = hits / (hits + misses) * 100%
```

## Testing

### Automated Tests

Run the integration test script:

```powershell
.\test-redis-integration.ps1
```

This tests:
1. Redis server availability
2. Service health checks
3. User caching behavior
4. Cache eviction on updates
5. JWT token blacklisting
6. Token validation after logout

### Manual Testing

**Test User Caching:**
```powershell
# 1. Get users (database hit) - note the time
Measure-Command { Invoke-RestMethod http://localhost:8083/users }

# 2. Get users again (cache hit) - should be faster
Measure-Command { Invoke-RestMethod http://localhost:8083/users }

# 3. Update a user
Invoke-RestMethod -Method PUT -Uri http://localhost:8083/users/1 `
  -ContentType "application/json" `
  -Body '{"username":"admin","email":"test@example.com","role":"ADMIN"}'

# 4. Get users again (database hit after cache eviction)
Measure-Command { Invoke-RestMethod http://localhost:8083/users }
```

**Test JWT Blacklisting:**
```powershell
# 1. Login
$login = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8110/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'

$token = $login.token

# 2. Check token (should be valid)
Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token"

# 3. Logout
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8110/api/auth/logout `
  -Headers @{"Authorization"="Bearer $token"}

# 4. Check token again (should be blacklisted)
Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token"
```

## Troubleshooting

### Issue: Services Can't Connect to Redis

**Symptoms:**
- "Connection refused" errors
- Services start but caching doesn't work

**Solutions:**
```powershell
# Check if Redis is running
docker ps | findstr redis

# Check Redis logs
docker logs plm-redis

# Restart Redis
docker restart plm-redis

# Test connection manually
redis-cli -h localhost -p 6379 -a plm_redis_password ping
```

### Issue: Cache Not Working

**Symptoms:**
- Every request hits database
- No cache keys in Redis Commander

**Solutions:**
```powershell
# 1. Check cache configuration
curl http://localhost:8083/actuator/health

# 2. Check Redis keys
redis-cli -h localhost -p 6379 -a plm_redis_password KEYS "*"

# 3. Check service logs for cache errors
# Look for "Failed to get from cache" warnings

# 4. Verify spring.cache.type=redis in application.properties
```

### Issue: Stale Cache Data

**Symptoms:**
- Updated data not showing
- Old user information displayed

**Solutions:**
```powershell
# Clear specific cache
redis-cli -h localhost -p 6379 -a plm_redis_password DEL "users::all"

# Or clear all caches (use with caution)
redis-cli -h localhost -p 6379 -a plm_redis_password FLUSHALL
```

### Issue: High Memory Usage

**Symptoms:**
- Redis using too much memory
- Performance degradation

**Solutions:**
```bash
# Check memory usage
redis-cli -h localhost -p 6379 -a plm_redis_password INFO memory

# Set max memory limit (e.g., 256MB)
redis-cli -h localhost -p 6379 -a plm_redis_password CONFIG SET maxmemory 256mb

# Set eviction policy
redis-cli -h localhost -p 6379 -a plm_redis_password CONFIG SET maxmemory-policy allkeys-lru
```

### Issue: Token Still Valid After Logout

**Symptoms:**
- JWT token works after logout
- Token not blacklisted

**Solutions:**
```powershell
# 1. Check if token is in blacklist
redis-cli -h localhost -p 6379 -a plm_redis_password KEYS "jwtBlacklist*"

# 2. Manually add token to blacklist (for testing)
$expirationTime = ([DateTimeOffset]::Now.AddHours(2)).ToUnixTimeMilliseconds()
redis-cli -h localhost -p 6379 -a plm_redis_password SET "jwtBlacklist:YOUR_TOKEN" $expirationTime

# 3. Check service logs for blacklist errors
```

## Disabling Redis

If you need to disable Redis temporarily:

### Option 1: Environment Variable
```powershell
$env:SPRING_CACHE_TYPE="none"
mvn spring-boot:run
```

### Option 2: Update application.properties
```properties
# Change this line:
spring.cache.type=none
```

### Option 3: Stop Redis
```powershell
docker stop plm-redis
# Services will automatically fallback to database
```

## Performance Benchmarks

### Expected Performance Improvements

| Operation | Before Redis | With Redis | Improvement |
|-----------|--------------|------------|-------------|
| Get All Users | ~200ms | ~5ms | 97.5% faster |
| Get User by Username | ~50ms | ~2ms | 96% faster |
| JWT Validation | ~10ms | ~2ms | 80% faster |

### Cache Hit Rates

Target metrics:
- User Cache Hit Rate: >95%
- JWT Blacklist Check: ~100% (all in cache)
- Overall Database Load Reduction: 80%+

## Best Practices

1. **Monitor Cache Hit Rates**: Aim for >90% hit rate
2. **Set Appropriate TTLs**: Balance freshness vs performance
3. **Use Cache Eviction**: Always evict on updates/deletes
4. **Handle Failures Gracefully**: Don't let Redis failures break the app
5. **Monitor Memory Usage**: Set `maxmemory` limits in production
6. **Use Redis Commander**: Regularly inspect cache contents
7. **Test Fallback Behavior**: Ensure app works without Redis

## Security Considerations

1. **Password Protected**: Redis requires authentication
2. **Local Only**: Redis bound to localhost (not exposed externally)
3. **Token Security**: JWT tokens hashed before storing in blacklist
4. **TTL for Sensitive Data**: Blacklist entries auto-expire
5. **No Sensitive Data**: Don't cache passwords or sensitive info

## Next Steps

After Priority 1 completion:
- [ ] Integrate Redis in task-service for dashboard caching
- [ ] Integrate Redis in bom-service for BOM structure caching
- [ ] Add Redis Sentinel for high availability
- [ ] Implement Redis pub/sub for real-time updates
- [ ] Add distributed locks for concurrent operations

## Support

For issues or questions:
- Check service logs in console windows
- Review Redis logs: `docker logs plm-redis`
- Use Redis Commander UI: http://localhost:8085
- Run integration tests: `.\test-redis-integration.ps1`


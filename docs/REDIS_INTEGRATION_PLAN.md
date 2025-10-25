# Redis Integration Plan - Priority 1

## Overview
Integration of Redis caching for auth-service and user-service to improve performance and enable advanced features like JWT token blacklisting.

## Redis Server Details
- **Host**: localhost (Docker)
- **Port**: 6379
- **Password**: plm_redis_password
- **Version**: 7.2-alpine

## Services in Scope

### 1. Auth-Service (Port 8110)
**Purpose**: JWT token management and blacklisting
**Cache Strategy**:
- JWT Blacklist: 2-hour TTL (matches token expiration)
- Rate Limiting: Track login attempts per IP/user

**Implementation**:
- ✅ Redis dependencies added
- ✅ RedisConfig created
- ✅ TokenBlacklistService implemented
- 🔄 Logout endpoint with blacklisting
- 🔄 Integration with JWT validation
- 🔄 Health checks

### 2. User-Service (Port 8083)
**Purpose**: User data caching
**Cache Strategy**:
- User List Cache: 10-minute TTL
- Individual User Cache: 10-minute TTL
- findByUsername: 10-minute TTL (critical for auth)

**Implementation**:
- ✅ RedisConfig enabled
- ✅ @Cacheable on getAllUsers()
- ✅ @CacheEvict on addUser(), updateUser(), deleteUser()
- 🔄 Add caching for findByUsername
- 🔄 Health checks
- 🔄 Graceful degradation

## Cache Key Strategy

```
auth-service:
├── jwtBlacklist:<token_hash>     → expiration timestamp
├── loginAttempts:<username>      → attempt count
└── rateLimits:<ip_address>       → request count

user-service:
├── users::all                     → List<User>
├── users::<userId>               → User object
└── users:username:<username>     → User object
```

## TTL (Time To Live) Strategy

| Cache Type | TTL | Reason |
|------------|-----|--------|
| JWT Blacklist | 2 hours | Match token expiration |
| User List | 10 minutes | Balance freshness vs performance |
| User Details | 10 minutes | Users don't change frequently |
| Login Attempts | 15 minutes | Security - sliding window |

## Fallback Strategy
Both services will implement graceful degradation:
1. Redis unavailable → Log warning, continue with DB
2. Redis timeout → Skip cache, use DB
3. Serialization error → Log error, invalidate cache entry

## Performance Targets

### Before Redis:
- User list query: ~200ms (DB query)
- User lookup: ~50ms (DB query)
- JWT validation: ~10ms (computation only)

### After Redis:
- User list query: ~5ms (cache hit)
- User lookup: ~2ms (cache hit)
- JWT validation: ~2ms (cache check) + blacklist check

### Expected Improvements:
- 95% cache hit rate for user data
- 80% reduction in database queries
- Sub-5ms response time for cached data

## Monitoring & Metrics

### Redis Commander Dashboard
Access at: http://localhost:8085
- View all cache keys
- Monitor memory usage
- Inspect TTL values

### Application Metrics
```bash
# Auth-service health
curl http://localhost:8110/actuator/health

# User-service health  
curl http://localhost:8083/actuator/health

# Redis info
redis-cli -h localhost -p 6379 -a plm_redis_password INFO
```

## Testing Plan

### Unit Tests
- [ ] RedisConfig bean creation
- [ ] Cache annotations work correctly
- [ ] Token blacklist service methods

### Integration Tests
- [ ] User caching and eviction
- [ ] JWT blacklist functionality
- [ ] Graceful fallback on Redis failure

### E2E Tests
1. Login → verify user cached
2. Update user → verify cache evicted
3. Login again → verify new cache
4. Logout → verify token blacklisted
5. Try to use blacklisted token → verify rejected

## Rollback Plan
If Redis causes issues:
1. Set `spring.cache.type=none` in application.properties
2. Restart services
3. Services will continue without caching

## Success Criteria
- ✅ Both services connect to Redis successfully
- ✅ User data is cached and retrieved from cache
- ✅ Cache is invalidated on user updates
- ✅ JWT tokens can be blacklisted on logout
- ✅ Blacklisted tokens are rejected
- ✅ No errors when Redis is temporarily unavailable
- ✅ Performance improvement of >80% for cached queries

## Timeline
**Phase 1**: Configuration & Health (30 min) - ⏳ In Progress
**Phase 2**: Auth-Service Features (45 min)
**Phase 3**: User-Service Optimization (30 min)
**Phase 4**: Integration Testing (45 min)
**Phase 5**: Documentation (30 min)

**Total**: ~3 hours

## Next Steps (Priority 2)
After Priority 1 completion:
- task-service: Dashboard query caching
- bom-service: BOM structure caching
- document-service: Selective metadata caching


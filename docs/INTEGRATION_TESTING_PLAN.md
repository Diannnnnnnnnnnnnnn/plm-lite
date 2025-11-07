# Integration Testing Plan - NGINX + API Gateway + Auth Service

**Date:** November 6, 2025  
**Status:** Ready for Execution  
**Estimated Time:** 30 minutes  

---

## 🎯 Testing Objectives

1. Verify all services start correctly
2. Validate JWT authentication flow
3. Test API Gateway routing
4. Verify NGINX proxy configuration
5. Confirm frontend integration
6. Check security enforcement
7. Validate error handling

---

## 📋 Test Phases

### **Phase 1: Infrastructure Verification** (5 minutes)
Verify all infrastructure components are running.

**Tests:**
- [ ] Redis is accessible
- [ ] Neo4j is running
- [ ] MySQL is running (if used)
- [ ] Eureka Server is up
- [ ] NGINX container is running

**Expected Results:**
- All infrastructure health checks pass
- No connection errors in logs

---

### **Phase 2: Service Startup Verification** (10 minutes)
Verify all microservices start and register with Eureka.

**Tests:**
- [ ] Eureka Server accessible
- [ ] API Gateway starts on port 8080
- [ ] Auth Service starts on port 8110
- [ ] All services register with Eureka
- [ ] No startup errors in logs

**Expected Results:**
- All services show "UP" in Eureka dashboard
- No error logs during startup
- All health endpoints return 200 OK

---

### **Phase 3: NGINX Proxy Tests** (5 minutes)
Verify NGINX is routing traffic correctly.

**Tests:**
- [ ] NGINX health endpoint works
- [ ] NGINX proxies to frontend
- [ ] NGINX proxies to API Gateway
- [ ] NGINX proxies to Eureka dashboard
- [ ] CORS headers present

**Expected Results:**
- All proxy routes work
- No 502/503 errors
- CORS headers in responses

---

### **Phase 4: Authentication Flow Tests** (5 minutes)
Test the complete authentication flow.

**Tests:**
- [ ] Login with valid credentials returns JWT
- [ ] Login with invalid credentials returns 401
- [ ] JWT token is well-formed
- [ ] Token contains expected claims
- [ ] Logout clears token

**Expected Results:**
- Valid login returns 200 with JWT token
- Invalid login returns 401
- JWT can be decoded
- Token has expiration claim

---

### **Phase 5: API Gateway Security Tests** (5 minutes)
Verify JWT validation at API Gateway level.

**Tests:**
- [ ] Request without JWT returns 401
- [ ] Request with invalid JWT returns 401
- [ ] Request with valid JWT succeeds
- [ ] Public paths accessible without JWT
- [ ] User context headers added

**Expected Results:**
- Protected endpoints require JWT
- Public endpoints accessible
- User headers present in downstream requests

---

### **Phase 6: Frontend Integration Tests** (5 minutes)
Verify frontend works with new architecture.

**Tests:**
- [ ] Frontend loads on port 8111
- [ ] Login page displays
- [ ] Login with demo/demo succeeds
- [ ] JWT stored in localStorage
- [ ] API calls include Authorization header
- [ ] Dashboard loads after login

**Expected Results:**
- Frontend accessible
- Login flow works
- JWT automatically attached to requests
- All features work

---

### **Phase 7: End-to-End Flow Tests** (5 minutes)
Test complete user workflows.

**Tests:**
- [ ] User can login
- [ ] User can view documents
- [ ] User can create document
- [ ] User can view tasks
- [ ] User can logout
- [ ] Expired token triggers logout

**Expected Results:**
- All CRUD operations work
- Navigation works
- Auto-logout on token expiry

---

## 🛠️ Test Scripts

### **Script 1: Infrastructure Check**
```powershell
# test-infrastructure.ps1
Write-Host "Testing Infrastructure..." -ForegroundColor Cyan

# Test Redis
try {
    $redis = redis-cli -a plm_redis_password ping
    if ($redis -eq "PONG") {
        Write-Host "✓ Redis: OK" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Redis: FAILED" -ForegroundColor Red
}

# Test Docker containers
$nginx = docker ps | Select-String "plm-nginx"
if ($nginx) {
    Write-Host "✓ NGINX Container: Running" -ForegroundColor Green
} else {
    Write-Host "✗ NGINX Container: Not Running" -ForegroundColor Red
}
```

### **Script 2: Service Health Check**
```powershell
# test-services.ps1
$services = @{
    "Eureka" = "http://localhost:8761/actuator/health"
    "API Gateway" = "http://localhost:8080/actuator/health"
    "Auth Service" = "http://localhost:8110/actuator/health"
    "NGINX" = "http://localhost:8111/health"
}

foreach ($service in $services.GetEnumerator()) {
    try {
        $response = Invoke-WebRequest -Uri $service.Value -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Key): UP" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ $($service.Key): DOWN" -ForegroundColor Red
    }
}
```

### **Script 3: Authentication Test**
```powershell
# test-authentication.ps1
Write-Host "Testing Authentication..." -ForegroundColor Cyan

# Test login
$loginUrl = "http://localhost:8111/auth/login"
$body = @{
    username = "demo"
    password = "demo"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $body -ContentType "application/json"
    if ($response.token) {
        Write-Host "✓ Login: SUCCESS" -ForegroundColor Green
        Write-Host "  Token: $($response.token.Substring(0,20))..." -ForegroundColor Gray
        $global:token = $response.token
    }
} catch {
    Write-Host "✗ Login: FAILED" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
```

### **Script 4: API Gateway Security Test**
```powershell
# test-security.ps1
Write-Host "Testing API Gateway Security..." -ForegroundColor Cyan

# Test without token (should fail)
try {
    Invoke-RestMethod -Uri "http://localhost:8111/api/users" -UseBasicParsing
    Write-Host "✗ Security: FAILED (should require token)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Unauthorized access blocked: OK" -ForegroundColor Green
    }
}

# Test with token (should succeed)
if ($global:token) {
    try {
        $headers = @{
            "Authorization" = "Bearer $($global:token)"
        }
        $response = Invoke-RestMethod -Uri "http://localhost:8111/api/users" -Headers $headers
        Write-Host "✓ Authorized access: OK" -ForegroundColor Green
    } catch {
        Write-Host "✗ Authorized access: FAILED" -ForegroundColor Red
    }
}
```

---

## 📊 Test Results Template

```
===========================================
Integration Test Results
===========================================
Date: [DATE]
Tester: [NAME]

Phase 1: Infrastructure Verification
[ ] Redis - OK/FAILED
[ ] Neo4j - OK/FAILED
[ ] MySQL - OK/FAILED
[ ] Eureka - OK/FAILED
[ ] NGINX - OK/FAILED

Phase 2: Service Startup
[ ] Eureka Server - OK/FAILED
[ ] API Gateway - OK/FAILED
[ ] Auth Service - OK/FAILED
[ ] All services registered - OK/FAILED

Phase 3: NGINX Proxy
[ ] Health endpoint - OK/FAILED
[ ] Frontend proxy - OK/FAILED
[ ] API proxy - OK/FAILED
[ ] CORS headers - OK/FAILED

Phase 4: Authentication
[ ] Valid login - OK/FAILED
[ ] Invalid login - OK/FAILED
[ ] JWT format - OK/FAILED
[ ] Token claims - OK/FAILED

Phase 5: API Gateway Security
[ ] No token = 401 - OK/FAILED
[ ] Invalid token = 401 - OK/FAILED
[ ] Valid token = 200 - OK/FAILED
[ ] Public paths - OK/FAILED

Phase 6: Frontend Integration
[ ] Page loads - OK/FAILED
[ ] Login works - OK/FAILED
[ ] JWT stored - OK/FAILED
[ ] API calls authenticated - OK/FAILED

Phase 7: End-to-End
[ ] Full user workflow - OK/FAILED
[ ] CRUD operations - OK/FAILED
[ ] Navigation - OK/FAILED

===========================================
Overall Status: PASS/FAIL
Notes:
[Add any notes here]
===========================================
```

---

## 🔍 Manual Verification Steps

### **1. Visual Inspection**
- [ ] Open http://localhost:8111
- [ ] Login page displays correctly
- [ ] Login with demo/demo
- [ ] Dashboard loads
- [ ] Navigation works

### **2. Browser DevTools Check**
- [ ] Open Developer Tools (F12)
- [ ] Go to Network tab
- [ ] Login
- [ ] Verify `/auth/login` request succeeds
- [ ] Check response contains `token`
- [ ] Go to Application tab
- [ ] Check localStorage contains `jwt_token`

### **3. API Call Verification**
- [ ] Click on Documents or Users
- [ ] Check Network tab
- [ ] Verify Authorization header present
- [ ] Verify request goes to `/api/*`
- [ ] Verify response is 200 OK

### **4. Logout Verification**
- [ ] Click Logout
- [ ] Verify localStorage cleared
- [ ] Verify redirected to login page
- [ ] Try accessing app without login
- [ ] Should redirect to login

---

## 🐛 Common Issues & Solutions

### **Issue 1: NGINX not starting**
**Check:**
```bash
docker logs plm-nginx
docker ps -a | grep nginx
```
**Fix:**
```bash
cd infra/nginx
docker-compose down
docker-compose up -d
```

### **Issue 2: Services not in Eureka**
**Check:**
```
http://localhost:8761
```
**Fix:**
- Wait 30 seconds for registration
- Check service logs for errors
- Verify eureka.client.enabled=true

### **Issue 3: 401 on all requests**
**Check:**
- JWT secret matches in auth-service and api-gateway
- Token is being sent in Authorization header
**Fix:**
- Verify `jwt.secret` in both application.properties
- Check browser DevTools Network tab

### **Issue 4: CORS errors**
**Check:**
- Browser console for CORS errors
**Fix:**
- Verify allowedOrigins in api-gateway/application.properties
- Add your frontend URL to allowed origins

---

## 📈 Success Criteria

### **Minimum Requirements (Must Pass)**
- [ ] All services start without errors
- [ ] Login returns JWT token
- [ ] API calls with token succeed
- [ ] API calls without token return 401
- [ ] Frontend loads and works

### **Recommended (Should Pass)**
- [ ] All Eureka services registered
- [ ] NGINX health check passes
- [ ] Redis caching works
- [ ] Auto-logout on token expiry

### **Optional (Nice to Have)**
- [ ] All CRUD operations work
- [ ] Real-time features work
- [ ] File upload works
- [ ] Workflow triggers work

---

## 🎯 Next Steps After Testing

### **If All Tests Pass:**
1. Document any configuration changes
2. Create backup of working configuration
3. Train team on new architecture
4. Monitor production deployment

### **If Tests Fail:**
1. Document failing tests
2. Check logs for errors
3. Review configuration files
4. Consult troubleshooting guide
5. Retest after fixes

---

## 📝 Notes

- Run tests in order (infrastructure → services → frontend)
- Allow 30 seconds between service starts
- Check Eureka dashboard frequently
- Monitor console logs during tests
- Take screenshots of any errors

---

**Ready to Execute:** Yes  
**Estimated Duration:** 30 minutes  
**Prerequisites:** All services built (mvn clean install)  

---


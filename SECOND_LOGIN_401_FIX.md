# Second Login 401 Error Fix

## Problem
The first login works fine, but the second login attempt fails with a 401 Unauthorized error.

## Root Cause Analysis

The 401 error could be coming from multiple places in the request chain:
1. **API Gateway JWT Filter** - If it doesn't recognize `/auth/login` as a public path
2. **Auth Service** - If there's an issue calling the user service
3. **User Service** - If credentials are rejected (unlikely since first login works)

## Changes Made

### 1. API Gateway JWT Filter (`api-gateway/src/main/java/com/example/api_gateway/filter/JwtAuthenticationFilter.java`)
- ✅ Added `/auth` (without trailing slash) to public paths
- ✅ Improved `isPublicPath()` method with better path normalization
- ✅ Added debug logging for path matching
- ✅ **REBUILT** - The JAR has been rebuilt with these changes

### 2. Frontend Auth Service (`frontend/src/services/authService.js`)
- ✅ Enhanced token clearing (localStorage + sessionStorage)
- ✅ Added delay after clearing to ensure storage is cleared
- ✅ Added logging for debugging

### 3. Frontend API Client (`frontend/src/utils/apiClient.js`)
- ✅ Enhanced Authorization header removal for auth endpoints
- ✅ Added warning logs if token exists when it shouldn't
- ✅ Added verification logging

### 4. Auth Service (`auth-service/src/main/java/com/example/auth_service/service/AuthService.java`)
- ✅ Added comprehensive error logging
- ✅ Better exception handling for Feign errors
- ⚠️ **NEEDS REBUILD** - Rebuild auth-service to see detailed error logs

## Next Steps

### Step 1: Restart API Gateway
The API Gateway has been rebuilt. You need to restart it for the changes to take effect:

```powershell
# Stop the API Gateway (if running)
# Then restart it using your start script
.\start-all-services.ps1
```

Or if running manually:
```powershell
cd api-gateway
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Step 2: Rebuild Auth Service (Optional but Recommended)
To see detailed error logs from the auth service:

```powershell
cd auth-service
mvn clean package -DskipTests
# Then restart the auth service
```

### Step 3: Test the Login Flow
1. Clear browser storage:
   ```javascript
   localStorage.clear()
   sessionStorage.clear()
   location.reload()
   ```

2. First login - should work ✓

3. Logout

4. Second login - should now work ✓

### Step 4: Check Logs
If the issue persists, check the logs:

**API Gateway logs** should show:
```
🔵 [Gateway Filter] Checking path: '/auth/login' method: POST
🔍 [Gateway] Path check: '/auth/login' -> PUBLIC
🟢 [Gateway] Public path accessed: /auth/login
```

**Auth Service logs** should show:
```
[AuthService] Login attempt for username: <username>
[Auth Verify] Checking credentials for: <username>
[AuthService] User verified successfully: <username>
[AuthService] Token generated successfully for: <username>
```

## Debugging

If you still get a 401 error:

1. **Check API Gateway logs** - Is the path being recognized as PUBLIC?
2. **Check Auth Service logs** - Is the user service being called successfully?
3. **Check User Service logs** - Is the verify endpoint being hit?
4. **Check browser console** - Are there any CORS or network errors?

## Expected Behavior After Fix

✅ First login: Works
✅ Logout: Works  
✅ Second login: **Should now work** (this was the issue)
✅ Subsequent logins: Should all work

## Technical Details

The fix ensures that:
- `/auth/login` is always recognized as a public path by the API Gateway
- Tokens are properly cleared before login attempts
- Authorization headers are never sent with login requests
- Better error logging helps diagnose any remaining issues



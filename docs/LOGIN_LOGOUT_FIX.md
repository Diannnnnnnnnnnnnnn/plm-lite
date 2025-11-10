# Login/Logout Issue Fix

## Problem Summary
Users experienced a 401 Unauthorized error when trying to login a second time after logging out. The first login worked fine, but subsequent logins after logout would fail.

## Root Causes Identified

### 1. Missing Authorization Header Pass-through in NGINX
The NGINX configuration for `/auth/` location was missing the Authorization header pass-through directives that were present in the `/api/` location. This could cause issues with header propagation.

### 2. Request Interceptor Adding Token to Login Requests
The `apiClient.js` request interceptor was adding the Authorization header to **ALL** requests, including login and register requests. This meant:
- If a user had any token in localStorage (even an old/expired one)
- When they tried to login again
- The old token would be sent with the new login request
- This could cause confusion or rejection by the backend

### 3. Response Interceptor Interfering with Login Errors
The response interceptor was catching **ALL** 401 errors and clearing localStorage + redirecting. This included 401 errors from failed login attempts, which should be handled by the login component instead.

## Changes Made

### 1. Auth Service - Logout Endpoint Already Exists
**File:** `auth-service/src/main/java/com/example/auth_service/controller/LogoutController.java`

Good news! The auth service already has a sophisticated logout endpoint with token blacklisting:
- Accepts `Authorization: Bearer <token>` header
- Validates the token before blacklisting
- Adds token to Redis blacklist to prevent reuse
- Returns success response with token expiry time

No changes needed here - the endpoint was already properly implemented!

### 2. NGINX Configuration - Fixed Authorization Header
**File:** `infra/nginx/nginx.conf`

Added Authorization header pass-through to `/auth/` location:
```nginx
# Pass through Authorization header (but not required for login)
proxy_set_header Authorization $http_authorization;
proxy_pass_header Authorization;
```

### 3. Frontend API Client - Exclude Auth Endpoints from Token Injection
**File:** `frontend/src/utils/apiClient.js`

Modified the request interceptor to **NOT** add Authorization headers to login/register requests:
```javascript
// Don't add Authorization header to login/register endpoints
const isAuthEndpoint = config.url && (
  config.url.includes('/auth/login') || 
  config.url.includes('/auth/register')
);

if (!isAuthEndpoint) {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
}
```

**Important:** Logout is NOT excluded, so the token will be sent with logout requests for blacklisting.

### 4. Frontend API Client - Don't Interfere with Login Errors
**File:** `frontend/src/utils/apiClient.js`

Modified the response interceptor to **NOT** handle 401 errors for login/register:
```javascript
// Don't handle 401 errors for login/register endpoints - let the component handle it
const isAuthEndpoint = error.config && error.config.url && (
  error.config.url.includes('/auth/login') || 
  error.config.url.includes('/auth/register')
);

if (error.response && error.response.status === 401 && !isAuthEndpoint) {
  // Clear localStorage and redirect
}
```

### 5. Frontend Auth Service - Improved Logout
**File:** `frontend/src/services/authService.js`

Improved logout to properly call the backend logout endpoint before clearing localStorage:
```javascript
logout() {
  const token = localStorage.getItem('jwt_token');
  
  // Call backend logout endpoint with token (for blacklisting)
  // The interceptor will automatically add the Authorization header
  if (token) {
    try {
      apiClient.post('/auth/logout').catch(() => {});
    } catch (error) {}
  }
  
  // Remove tokens from localStorage
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user');
}
```

## Testing Instructions

### 1. Rebuild and Restart Services

#### Backend Services
```powershell
# Auth service - No rebuild needed (logout endpoint already exists)

# Restart NGINX (updated configuration)
cd infra\nginx
docker-compose down
docker-compose up -d
cd ..\..

# Services should still be running, but if not:
# .\start-all-services.ps1
```

#### Frontend
```powershell
cd frontend
npm start
```

### 2. Manual Testing Steps

1. Open the application at http://localhost:8111
2. Login with any valid credentials (e.g., demo/demo)
3. Verify you can see the dashboard with data
4. Click logout
5. **Try to login again with the same credentials**
6. Verify the second login succeeds without 401 errors
7. Repeat steps 4-6 multiple times to confirm consistency

### 3. Automated Testing

Run the provided test script:
```powershell
.\test-login-logout.ps1
```

This script tests:
- Initial login
- Token validation
- Accessing protected endpoints
- Logout
- **Second login after logout** (the problematic case)
- Second token validation

## Expected Results

✅ **Before Fix:**
- First login: SUCCESS ✓
- Logout: SUCCESS ✓ (backend logout endpoint exists)
- Second login: **FAIL with 401** ❌

✅ **After Fix:**
- First login: SUCCESS ✓
- Logout: SUCCESS ✓
- Second login: SUCCESS ✓
- Subsequent logins: SUCCESS ✓

## Technical Notes

### Why This Happened

The issue was a combination of:
1. **Token injection on all requests**: The frontend was adding Bearer tokens to login requests
2. **Header propagation**: NGINX wasn't consistently handling Authorization headers
3. **Error handling**: The response interceptor was too aggressive in handling 401s

### Best Practices Applied

1. **Exclude auth endpoints from automatic token injection** - Login/register should never have tokens
2. **Let components handle their own errors** - Don't intercept errors for auth endpoints globally
3. **Ensure backend endpoints exist** - Even for simple operations like logout
4. **Consistent NGINX configuration** - All API locations should have similar header handling

## Verification

After applying these fixes, you should see in the browser console:
```
[API] POST /auth/login          // First login - NO Bearer token sent
[API] GET /api/tasks            // Protected endpoint - Bearer token sent
[API] POST /auth/logout         // Logout - Bearer token sent
[API] POST /auth/login          // Second login - NO Bearer token sent ✓
```

The key difference is that login requests should **NOT** have Bearer tokens in the Authorization header.


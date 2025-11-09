# Clear Demo Token and Login Properly

## The Issue

You're currently logged in with a **fallback demo token** (`demo_token`), which is not a valid JWT from the backend. This token was created by the frontend's fallback login mechanism.

## Solution

### Step 1: Clear Browser Storage

Open the browser console (F12) and run:

```javascript
localStorage.clear()
sessionStorage.clear()
location.reload()
```

### Step 2: Login Again

After the page reloads:
1. Enter username: `demo`
2. Enter password: `demo`
3. Click "Sign In"

This time it should get a **real JWT token** from the backend!

### Expected Console Output

When you login, you should see:

```
[API Client] Base URL: http://localhost:8111
[API] Skipping token for auth endpoint: /auth/login
[API] POST /auth/login
[API] Headers: AxiosHeaders {Accept: 'application/json, text/plain, */*', Content-Type: 'application/json'}
```

**NO** Authorization header should be present for the login request!

After successful login, subsequent API requests should show:

```
[API] Adding token to request: eyJhbGciOiJIUzI1NiJ9...
[API] GET /api/tasks
[API] Headers: {..., Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9...'}
```

## What Changed

### ✅ Backend is now working:
- Auth Service accepts login requests
- API Gateway allows `/auth/*` paths through
- Returns proper JWT tokens

### ✅ Frontend is now working:
- No longer sends tokens to login endpoints
- Properly handles login errors
- Uses real backend authentication

## If Login Still Fails

If you still get 401 on login, try bypassing NGINX:

1. Open console and set:
```javascript
// Temporarily use API Gateway directly
window.location.href = 'http://localhost:8080/auth/login'
```

Or modify the frontend base URL:
```javascript
localStorage.setItem('REACT_APP_API_URL', 'http://localhost:8080')
location.reload()
```

Then try login again.










# Debug JWT Blacklisting Test

Write-Host "Debugging JWT Blacklisting..." -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if auth-service is responding
Write-Host "Step 1: Checking auth-service health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod http://localhost:8110/actuator/health -TimeoutSec 5
    Write-Host "  Status: $($health.status)" -ForegroundColor Green
    if ($health.components) {
        Write-Host "  Components: $($health.components | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Auth-service may not be started yet!" -ForegroundColor Red
    exit
}

Write-Host ""

# Step 2: Try to login
Write-Host "Step 2: Attempting login..." -ForegroundColor Yellow
try {
    $loginBody = '{"username":"vivi","password":"password"}'
    Write-Host "  Request body: $loginBody" -ForegroundColor Gray
    
    $login = Invoke-RestMethod -Method POST `
        -Uri http://localhost:8110/api/auth/login `
        -ContentType "application/json" `
        -Body $loginBody `
        -TimeoutSec 10
    
    Write-Host "  Login successful!" -ForegroundColor Green
    Write-Host "  Token: $($login.token.Substring(0,50))..." -ForegroundColor Gray
    $token = $login.token
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response: $responseBody" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "  1. User 'admin' doesn't exist - create it first" -ForegroundColor Gray
    Write-Host "  2. Password is incorrect" -ForegroundColor Gray
    Write-Host "  3. User-service is not running (auth-service needs it)" -ForegroundColor Gray
    exit
}

Write-Host ""

# Step 3: Check token before logout
Write-Host "Step 3: Checking token status before logout..." -ForegroundColor Yellow
try {
    $check1 = Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token" -TimeoutSec 5
    Write-Host "  Valid: $($check1.valid)" -ForegroundColor Gray
    Write-Host "  Blacklisted: $($check1.blacklisted)" -ForegroundColor Gray
    Write-Host "  Accepted: $($check1.accepted)" -ForegroundColor Gray
    
    if ($check1.blacklisted -eq $false) {
        Write-Host "  Token is NOT blacklisted (correct before logout)" -ForegroundColor Green
    } else {
        Write-Host "  WARNING: Token is already blacklisted!" -ForegroundColor Red
    }
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  The /check-token endpoint may not be working" -ForegroundColor Red
    exit
}

Write-Host ""

# Step 4: Logout (blacklist token)
Write-Host "Step 4: Logging out (blacklisting token)..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-RestMethod -Method POST `
        -Uri http://localhost:8110/api/auth/logout `
        -Headers @{"Authorization"="Bearer $token"} `
        -TimeoutSec 5
    
    Write-Host "  Logout successful!" -ForegroundColor Green
    Write-Host "  Response: $($logoutResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response: $responseBody" -ForegroundColor Red
    }
    exit
}

Write-Host ""

# Step 5: Check token after logout
Write-Host "Step 5: Checking token status after logout..." -ForegroundColor Yellow
try {
    $check2 = Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token" -TimeoutSec 5
    Write-Host "  Valid: $($check2.valid)" -ForegroundColor Gray
    Write-Host "  Blacklisted: $($check2.blacklisted)" -ForegroundColor Gray
    Write-Host "  Accepted: $($check2.accepted)" -ForegroundColor Gray
    
    if ($check2.blacklisted -eq $true) {
        Write-Host "  Token IS blacklisted (correct after logout)" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: Token should be blacklisted but isn't!" -ForegroundColor Red
    }
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

Write-Host ""

# Step 6: Check Redis for blacklist entry
Write-Host "Step 6: Checking Redis for blacklist entry..." -ForegroundColor Yellow
try {
    $keys = docker exec redis redis-cli KEYS "*jwtBlacklist*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
    if ($keys) {
        Write-Host "  Blacklist keys found in Redis:" -ForegroundColor Green
        foreach ($key in $keys) {
            Write-Host "    - $key" -ForegroundColor Gray
        }
    } else {
        Write-Host "  No blacklist keys found in Redis!" -ForegroundColor Red
        Write-Host "  This means the blacklist is not being stored properly" -ForegroundColor Red
    }
} catch {
    Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================"
Write-Host "Diagnostic complete!"
Write-Host "========================================"


# Redis Integration Test Script for PLM-Lite
# Tests Priority 1 services: auth-service and user-service

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Redis Integration Test - Priority 1" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$AUTH_SERVICE = "http://localhost:8110"
$USER_SERVICE = "http://localhost:8083"
$REDIS_HOST = "localhost"
$REDIS_PORT = 6379
$REDIS_PASSWORD = "plm_redis_password"

# Test counters
$passedTests = 0
$failedTests = 0
$totalTests = 0

function Test-Endpoint {
    param($name, $url, $method = "GET", $body = $null, $headers = @{})
    
    $global:totalTests++
    Write-Host "Test $global:totalTests : $name" -NoNewline
    
    try {
        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
            TimeoutSec = 5
        }
        
        if ($body) {
            $params['Body'] = ($body | ConvertTo-Json)
            $params['ContentType'] = 'application/json'
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        Write-Host " ✓ PASS" -ForegroundColor Green
        $global:passedTests++
        return $response
    } catch {
        Write-Host " ✗ FAIL" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        $global:failedTests++
        return $null
    }
}

function Test-Redis {
    Write-Host "Test 0: Redis Server Availability" -NoNewline
    try {
        $result = & redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping 2>&1
        if ($result -like "*PONG*") {
            Write-Host " ✓ PASS" -ForegroundColor Green
            return $true
        } else {
            Write-Host " ✗ FAIL (No PONG response)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host " ✗ FAIL (redis-cli not found or connection failed)" -ForegroundColor Red
        Write-Host "  Make sure Redis is running: docker run -d -p 6379:6379 redis:7.2-alpine redis-server --requirepass plm_redis_password" -ForegroundColor Yellow
        return $false
    }
}

Write-Host "Phase 1: Infrastructure Tests" -ForegroundColor Yellow
Write-Host "---------------------------------------------"

$redisAvailable = Test-Redis
if (-not $redisAvailable) {
    Write-Host ""
    Write-Host "WARNING: Redis is not available. Tests may fail." -ForegroundColor Yellow
    Write-Host "Press Enter to continue anyway, or Ctrl+C to exit..."
    Read-Host
}

Write-Host ""
Write-Host "Phase 2: Service Health Checks" -ForegroundColor Yellow
Write-Host "---------------------------------------------"

$authHealth = Test-Endpoint "Auth-Service Health" "$AUTH_SERVICE/actuator/health"
$userHealth = Test-Endpoint "User-Service Health" "$USER_SERVICE/actuator/health"

Write-Host ""
Write-Host "Phase 3: User-Service Caching Tests" -ForegroundColor Yellow
Write-Host "---------------------------------------------"

# Create a test user
$testUser = @{
    username = "redis_test_user_$(Get-Random -Maximum 9999)"
    password = "TestPassword123!"
    email = "redis_test@example.com"
    role = "USER"
}

Write-Host "Creating test user: $($testUser.username)..."
$createdUser = Test-Endpoint "Create Test User" "$USER_SERVICE/users" "POST" $testUser

if ($createdUser) {
    Write-Host "  Created user ID: $($createdUser.id)" -ForegroundColor Gray
    
    # First fetch - should hit database
    Write-Host ""
    Write-Host "First fetch (DB hit)..." -ForegroundColor Gray
    $firstFetch = Test-Endpoint "Fetch All Users (DB)" "$USER_SERVICE/users" "GET"
    Start-Sleep -Seconds 1
    
    # Second fetch - should hit cache
    Write-Host "Second fetch (Cache hit)..." -ForegroundColor Gray
    $secondFetch = Test-Endpoint "Fetch All Users (Cache)" "$USER_SERVICE/users" "GET"
    
    # Update user - should evict cache
    Write-Host ""
    Write-Host "Updating user (cache eviction)..." -ForegroundColor Gray
    $testUser.email = "updated_$($testUser.email)"
    $updatedUser = Test-Endpoint "Update Test User" "$USER_SERVICE/users/$($createdUser.id)" "PUT" $testUser
    
    # Fetch again - should hit database again
    Start-Sleep -Seconds 1
    $thirdFetch = Test-Endpoint "Fetch All Users (DB after update)" "$USER_SERVICE/users" "GET"
    
    # Cleanup
    Write-Host ""
    Write-Host "Cleaning up test user..." -ForegroundColor Gray
    Test-Endpoint "Delete Test User" "$USER_SERVICE/users/$($createdUser.id)" "DELETE"
}

Write-Host ""
Write-Host "Phase 4: Auth-Service JWT & Blacklist Tests" -ForegroundColor Yellow
Write-Host "---------------------------------------------"

# Login
Write-Host "Testing login flow..." -ForegroundColor Gray
$loginData = @{
    username = "admin"
    password = "admin123"
}

$loginResponse = Test-Endpoint "Login" "$AUTH_SERVICE/api/auth/login" "POST" $loginData

if ($loginResponse -and $loginResponse.token) {
    $token = $loginResponse.token
    Write-Host "  Got token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    
    # Validate token
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    
    $validateResponse = Test-Endpoint "Validate Token (Before Logout)" "$AUTH_SERVICE/api/auth/validate?token=$token" "GET"
    
    # Check token status
    $checkResponse = Test-Endpoint "Check Token Status" "$AUTH_SERVICE/api/auth/check-token?token=$token" "GET"
    if ($checkResponse) {
        Write-Host "  Token valid: $($checkResponse.valid), Blacklisted: $($checkResponse.blacklisted)" -ForegroundColor Gray
    }
    
    # Logout (blacklist token)
    Write-Host ""
    Write-Host "Testing logout (token blacklisting)..." -ForegroundColor Gray
    $logoutResponse = Test-Endpoint "Logout" "$AUTH_SERVICE/api/auth/logout" "POST" $null $headers
    
    # Try to use blacklisted token
    Start-Sleep -Seconds 1
    $checkAfterLogout = Test-Endpoint "Check Token After Logout" "$AUTH_SERVICE/api/auth/check-token?token=$token" "GET"
    if ($checkAfterLogout) {
        Write-Host "  Token valid: $($checkAfterLogout.valid), Blacklisted: $($checkAfterLogout.blacklisted)" -ForegroundColor Gray
        
        if ($checkAfterLogout.blacklisted -eq $true) {
            Write-Host "  ✓ Token successfully blacklisted!" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Token should be blacklisted but isn't!" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "Phase 5: Redis Cache Inspection" -ForegroundColor Yellow
Write-Host "---------------------------------------------"

if ($redisAvailable) {
    Write-Host "Checking Redis keys..." -ForegroundColor Gray
    try {
        $keys = & redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD KEYS "*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
        if ($keys) {
            Write-Host "Cache keys found:" -ForegroundColor Green
            foreach ($key in $keys) {
                Write-Host "  - $key" -ForegroundColor Gray
            }
        } else {
            Write-Host "  No cache keys found" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "Redis INFO:" -ForegroundColor Gray
        $info = & redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD INFO stats 2>&1 | Select-String "keyspace_hits|keyspace_misses|expired_keys"
        $info | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    } catch {
        Write-Host "  Could not inspect Redis" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "  Test Results" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Total Tests : $totalTests" -ForegroundColor White
Write-Host "Passed      : $passedTests" -ForegroundColor Green
Write-Host "Failed      : $failedTests" -ForegroundColor Red
Write-Host ""

if ($failedTests -eq 0) {
    Write-Host "✓ All tests passed! Redis integration is working correctly." -ForegroundColor Green
} else {
    Write-Host "✗ Some tests failed. Please check the output above." -ForegroundColor Red
}

Write-Host ""
Write-Host "To view Redis data in browser, visit:" -ForegroundColor Yellow
Write-Host "  Redis Commander: http://localhost:8085" -ForegroundColor Cyan
Write-Host ""


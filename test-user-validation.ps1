# Test User Validation Endpoint
# This script tests the /users/validate endpoint to diagnose login issues

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing User Service Validation" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8083"

# Test 1: Check if service is running
Write-Host ""
Write-Host "[1] Checking if user-service is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/users" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "User service is running!" -ForegroundColor Green
    $users = $response.Content | ConvertFrom-Json
    Write-Host "   Found $($users.Count) users in database" -ForegroundColor Gray
} catch {
    Write-Host "User service is NOT running or not accessible!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Please start the user-service first:" -ForegroundColor Yellow
    Write-Host "   cd user-service" -ForegroundColor White
    Write-Host "   mvn spring-boot:run" -ForegroundColor White
    exit 1
}

# Test 2: List all users
Write-Host ""
Write-Host "[2] Listing all users..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET -ErrorAction Stop
    if ($response.Count -eq 0) {
        Write-Host "No users found in database!" -ForegroundColor Red
        Write-Host "   Run reset-user-db.ps1 to reinitialize demo users" -ForegroundColor Yellow
    } else {
        Write-Host "Users found:" -ForegroundColor Green
        foreach ($user in $response) {
            Write-Host "   - $($user.username) (ID: $($user.id), Roles: $($user.roles))" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "Failed to list users: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Validate demo users
Write-Host ""
Write-Host "[3] Testing user validation..." -ForegroundColor Yellow
$testUsers = @(
    @{username="demo"; password="demo"},
    @{username="guodian"; password="password"},
    @{username="labubu"; password="password"},
    @{username="vivi"; password="password"}
)

foreach ($testUser in $testUsers) {
    $username = $testUser.username
    $password = $testUser.password
    try {
        $url = "$baseUrl/users/validate?username=$username&password=$password"
        $response = Invoke-RestMethod -Uri $url -Method POST -ErrorAction Stop
        Write-Host "SUCCESS: $username/$password - Valid! Roles: $($response.roles)" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 401) {
            Write-Host "FAILED: $username/$password - 401 Unauthorized" -ForegroundColor Red
        } else {
            Write-Host "FAILED: $username/$password - Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If validation is failing:" -ForegroundColor Yellow
Write-Host "1. Run reset-user-db.ps1 to reinitialize the database" -ForegroundColor White
Write-Host "2. Wait 30-60 seconds for service to fully start" -ForegroundColor White
Write-Host "3. Run this test script again" -ForegroundColor White

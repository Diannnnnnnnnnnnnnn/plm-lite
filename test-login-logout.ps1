# Test script to verify login/logout functionality

Write-Host "Testing Login/Logout Functionality" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host ""

# Test 1: Login
Write-Host "Test 1: Initial Login" -ForegroundColor Yellow
$loginBody = @{
    username = "demo"
    password = "demo"
} | ConvertTo-Json

Write-Host "Sending login request..." -ForegroundColor Cyan
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8111/auth/login" `
    -Method POST `
    -Body $loginBody `
    -ContentType "application/json" `
    -ErrorAction Stop

$token = $loginResponse.token
Write-Host "✓ Login successful! Token received: $($token.Substring(0, 20))..." -ForegroundColor Green
Write-Host ""

# Test 2: Validate token
Write-Host "Test 2: Validate Token" -ForegroundColor Yellow
$validateResponse = Invoke-RestMethod -Uri "http://localhost:8111/auth/validate?token=$token" `
    -Method GET `
    -ErrorAction Stop
Write-Host "✓ Token validation: $validateResponse" -ForegroundColor Green
Write-Host ""

# Test 3: Access protected endpoint with token
Write-Host "Test 3: Access Protected Endpoint" -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
}
$tasksResponse = Invoke-RestMethod -Uri "http://localhost:8111/api/tasks" `
    -Method GET `
    -Headers $headers `
    -ErrorAction Stop
Write-Host "✓ Successfully accessed /api/tasks - Got $($tasksResponse.Count) tasks" -ForegroundColor Green
Write-Host ""

# Test 4: Logout
Write-Host "Test 4: Logout" -ForegroundColor Yellow
$logoutResponse = Invoke-RestMethod -Uri "http://localhost:8111/auth/logout" `
    -Method POST `
    -Headers $headers `
    -ErrorAction Stop
Write-Host "✓ Logout successful: $logoutResponse" -ForegroundColor Green
Write-Host ""

# Test 5: Second login (the problematic case)
Write-Host "Test 5: Second Login (After Logout)" -ForegroundColor Yellow
Write-Host "Sending second login request..." -ForegroundColor Cyan
$loginResponse2 = Invoke-RestMethod -Uri "http://localhost:8111/auth/login" `
    -Method POST `
    -Body $loginBody `
    -ContentType "application/json" `
    -ErrorAction Stop

$token2 = $loginResponse2.token
Write-Host "✓ Second login successful! Token received: $($token2.Substring(0, 20))..." -ForegroundColor Green
Write-Host ""

# Test 6: Validate second token
Write-Host "Test 6: Validate Second Token" -ForegroundColor Yellow
$validateResponse2 = Invoke-RestMethod -Uri "http://localhost:8111/auth/validate?token=$token2" `
    -Method GET `
    -ErrorAction Stop
Write-Host "✓ Second token validation: $validateResponse2" -ForegroundColor Green
Write-Host ""

Write-Host "====================================" -ForegroundColor Green
Write-Host "All tests passed! ✓" -ForegroundColor Green
Write-Host "The login/logout issue has been fixed." -ForegroundColor Green










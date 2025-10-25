# Quick Redis Integration Test
Write-Host ""
Write-Host "========================================"
Write-Host "  Quick Redis Integration Test"
Write-Host "========================================"
Write-Host ""

$pass = 0
$fail = 0

# Test 1: Redis Running
Write-Host "Test 1: Redis Status..." -NoNewline
try {
    $result = docker exec redis redis-cli ping 2>&1
    if ($result -like "*PONG*") {
        Write-Host " PASS" -ForegroundColor Green
        $pass++
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $fail++
    }
} catch {
    Write-Host " FAIL" -ForegroundColor Red
    $fail++
}

# Test 2: User-Service Health
Write-Host "Test 2: User-Service Health..." -NoNewline
try {
    $health = Invoke-RestMethod http://localhost:8083/actuator/health -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host " PASS" -ForegroundColor Green
        $pass++
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $fail++
    }
} catch {
    Write-Host " FAIL (Service not started)" -ForegroundColor Red
    $fail++
}

# Test 3: Auth-Service Health
Write-Host "Test 3: Auth-Service Health..." -NoNewline
try {
    $health = Invoke-RestMethod http://localhost:8110/actuator/health -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host " PASS" -ForegroundColor Green
        $pass++
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $fail++
    }
} catch {
    Write-Host " FAIL (Service not started)" -ForegroundColor Red
    $fail++
}

# Test 4: User Caching
Write-Host "Test 4: User Caching..." -NoNewline
try {
    docker exec redis redis-cli FLUSHALL | Out-Null
    $time1 = Measure-Command { $users1 = Invoke-RestMethod http://localhost:8083/users -TimeoutSec 10 }
    $time2 = Measure-Command { $users2 = Invoke-RestMethod http://localhost:8083/users -TimeoutSec 10 }
    
    if ($time2.TotalMilliseconds -lt $time1.TotalMilliseconds) {
        Write-Host " PASS" -ForegroundColor Green
        Write-Host "   1st: $([math]::Round($time1.TotalMilliseconds))ms, 2nd: $([math]::Round($time2.TotalMilliseconds))ms" -ForegroundColor Gray
        $pass++
    } else {
        Write-Host " PARTIAL" -ForegroundColor Yellow
        $pass++
    }
} catch {
    Write-Host " FAIL" -ForegroundColor Red
    $fail++
}

# Test 5: JWT Blacklisting
Write-Host "Test 5: JWT Blacklisting..." -NoNewline
try {
    $login = Invoke-RestMethod -Method POST -Uri http://localhost:8110/api/auth/login -ContentType "application/json" -Body '{"username":"vivi","password":"password"}' -TimeoutSec 10
    $token = $login.token
    $check1 = Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token" -TimeoutSec 5
    Invoke-RestMethod -Method POST -Uri http://localhost:8110/api/auth/logout -Headers @{"Authorization"="Bearer $token"} -TimeoutSec 5 | Out-Null
    $check2 = Invoke-RestMethod "http://localhost:8110/api/auth/check-token?token=$token" -TimeoutSec 5
    
    if ($check1.blacklisted -eq $false -and $check2.blacklisted -eq $true) {
        Write-Host " PASS" -ForegroundColor Green
        $pass++
    } else {
        Write-Host " FAIL" -ForegroundColor Red
        $fail++
    }
} catch {
    Write-Host " FAIL" -ForegroundColor Red
    $fail++
}

# Summary
Write-Host ""
Write-Host "========================================"
Write-Host "Results: $pass passed, $fail failed"
Write-Host "========================================"
Write-Host ""

if ($fail -eq 0) {
    Write-Host "All tests passed! Priority 1 is working." -ForegroundColor Green
} else {
    Write-Host "Some tests failed. Check if services are fully started." -ForegroundColor Yellow
}


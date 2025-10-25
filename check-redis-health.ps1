# Redis Health Check Script

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Redis Health Check" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. Check if Redis container is running
Write-Host "1. Redis Container Status..." -NoNewline
$container = docker ps --filter "name=redis" --format "{{.Status}}"
if ($container -like "*Up*") {
    Write-Host " ✓ Running" -ForegroundColor Green
    Write-Host "   $container" -ForegroundColor Gray
} else {
    Write-Host " ✗ Not Running" -ForegroundColor Red
    exit
}

# 2. Test Redis connectivity
Write-Host "`n2. Redis Connectivity..." -NoNewline
$ping = docker exec redis redis-cli ping 2>&1
if ($ping -like "*PONG*") {
    Write-Host " ✓ Connected" -ForegroundColor Green
} else {
    Write-Host " ✗ Failed" -ForegroundColor Red
    exit
}

# 3. Check cache keys
Write-Host "`n3. Cache Keys in Redis..." -ForegroundColor Yellow
$keys = docker exec redis redis-cli KEYS "*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
if ($keys) {
    Write-Host "   Found $($keys.Count) key(s):" -ForegroundColor Green
    $keys | ForEach-Object { Write-Host "   - $_" -ForegroundColor Gray }
} else {
    Write-Host "   No keys found (cache is empty)" -ForegroundColor Yellow
}

# 4. Redis memory usage
Write-Host "`n4. Redis Memory Usage..." -ForegroundColor Yellow
$memory = docker exec redis redis-cli INFO memory | Select-String "used_memory_human"
Write-Host "   $memory" -ForegroundColor Gray

# 5. Cache statistics
Write-Host "`n5. Cache Statistics..." -ForegroundColor Yellow
$stats = docker exec redis redis-cli INFO stats | Select-String "keyspace_hits|keyspace_misses|expired_keys"
$stats | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }

# Calculate hit rate if we have data
$hitsLine = $stats | Select-String "keyspace_hits"
$missesLine = $stats | Select-String "keyspace_misses"
if ($hitsLine -and $missesLine) {
    $hits = [int]($hitsLine -replace '.*:(\d+).*','$1')
    $misses = [int]($missesLine -replace '.*:(\d+).*','$1')
    if (($hits + $misses) -gt 0) {
        $hitRate = [math]::Round(($hits / ($hits + $misses)) * 100, 2)
        Write-Host "   Cache Hit Rate: $hitRate%" -ForegroundColor $(if ($hitRate -gt 80) { "Green" } elseif ($hitRate -gt 50) { "Yellow" } else { "Red" })
    }
}

# 6. Check service connections
Write-Host "`n6. Service Health Checks..." -ForegroundColor Yellow

Write-Host "   User-Service (8083)..." -NoNewline
try {
    $userHealth = Invoke-RestMethod http://localhost:8083/actuator/health -TimeoutSec 3
    if ($userHealth.components.redis.status -eq "UP") {
        Write-Host " ✓ Redis Connected" -ForegroundColor Green
    } else {
        Write-Host " ⚠ Redis Not Connected" -ForegroundColor Yellow
    }
} catch {
    Write-Host " ✗ Service Not Running" -ForegroundColor Red
}

Write-Host "   Auth-Service (8110)..." -NoNewline
try {
    $authHealth = Invoke-RestMethod http://localhost:8110/actuator/health -TimeoutSec 3
    if ($authHealth.components.redis.status -eq "UP") {
        Write-Host " ✓ Redis Connected" -ForegroundColor Green
    } else {
        Write-Host " ⚠ Redis Not Connected" -ForegroundColor Yellow
    }
} catch {
    Write-Host " ✗ Service Not Running" -ForegroundColor Red
}

# 7. Test actual caching
Write-Host "`n7. Testing User Cache..." -ForegroundColor Yellow
try {
    Write-Host "   First query..." -NoNewline
    $time1 = Measure-Command { $users = Invoke-RestMethod http://localhost:8083/users -TimeoutSec 5 }
    Write-Host " $([math]::Round($time1.TotalMilliseconds))ms" -ForegroundColor Gray
    
    Write-Host "   Second query (cached)..." -NoNewline
    $time2 = Measure-Command { $users = Invoke-RestMethod http://localhost:8083/users -TimeoutSec 5 }
    Write-Host " $([math]::Round($time2.TotalMilliseconds))ms" -ForegroundColor Gray
    
    if ($time2.TotalMilliseconds -lt $time1.TotalMilliseconds) {
        $speedup = [math]::Round($time1.TotalMilliseconds / $time2.TotalMilliseconds, 1)
        Write-Host "   Cache is working! ($speedup times faster)" -ForegroundColor Green
    } else {
        Write-Host "   Cache may not be working optimally" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ Could not test caching" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Health Check Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nQuick Commands:" -ForegroundColor Yellow
Write-Host "  View cache data:    docker exec redis redis-cli KEYS '*'" -ForegroundColor Gray
Write-Host "  Monitor Redis:      docker exec redis redis-cli MONITOR" -ForegroundColor Gray
Write-Host "  Redis Commander:    http://localhost:8085" -ForegroundColor Gray
Write-Host "  Run full tests:     .\quick-test-redis.ps1" -ForegroundColor Gray
Write-Host ""


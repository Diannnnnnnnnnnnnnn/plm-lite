# ======================================
# PLM-Lite Infrastructure Startup Script
# Starts MinIO + Redis
# ======================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PLM-Lite Infrastructure Startup" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Change to project root directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "[1/3] Starting Infrastructure Services (MinIO + Redis)..." -ForegroundColor Green

# Start Docker Compose
docker-compose -f infra/docker-compose-infrastructure.yaml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ Failed to start infrastructure services!" -ForegroundColor Red
    exit 1
}

Write-Host "`n[2/3] Waiting for services to be healthy..." -ForegroundColor Yellow

# Wait for MinIO health check
$maxWait = 60
$waited = 0
$healthy = $false

while ($waited -lt $maxWait) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9000/minio/health/live" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ MinIO is healthy!" -ForegroundColor Green
            $healthy = $true
            break
        }
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
        $waited += 2
    }
}

if (-not $healthy) {
    Write-Host "`n⚠️  MinIO health check timed out, but it might still be starting..." -ForegroundColor Yellow
}

# Check Redis
Write-Host "`nChecking Redis..." -ForegroundColor Yellow
docker exec plm-redis redis-cli -a plm_redis_password PING 2>$null | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Redis is healthy!" -ForegroundColor Green
} else {
    Write-Host "⚠️  Redis might still be starting..." -ForegroundColor Yellow
}

Write-Host "`n[3/3] Checking bucket creation..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
docker logs plm-minio-init 2>&1 | Select-String "plm-documents" | ForEach-Object { Write-Host $_ -ForegroundColor Green }

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  ✅ Infrastructure Services Started" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "📊 Service URLs:" -ForegroundColor Cyan
Write-Host "   MinIO API:      http://localhost:9000" -ForegroundColor White
Write-Host "   MinIO Console:  http://localhost:9001 (minio/password)" -ForegroundColor White
Write-Host "   Redis:          localhost:6379" -ForegroundColor White
Write-Host "   Redis Commander: http://localhost:8085" -ForegroundColor White

Write-Host "`n📝 Next Steps:" -ForegroundColor Cyan
Write-Host "   1. Start document-service: cd document-service && mvn spring-boot:run" -ForegroundColor White
Write-Host "   2. Test MinIO: Access http://localhost:9001" -ForegroundColor White
Write-Host "   3. Stop services: docker-compose -f infra/docker-compose-infrastructure.yaml down" -ForegroundColor White

Write-Host "`n"


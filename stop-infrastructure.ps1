# ======================================
# PLM-Lite Infrastructure Stop Script
# Stops MinIO + Redis
# ======================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PLM-Lite Infrastructure Shutdown" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Change to project root directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "Stopping Infrastructure Services..." -ForegroundColor Yellow

# Stop Docker Compose
docker-compose -f infra/docker-compose-infrastructure.yaml down

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Infrastructure services stopped successfully!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Failed to stop infrastructure services!" -ForegroundColor Red
    exit 1
}

Write-Host "`n📝 Note: Data volumes are preserved. To remove data:" -ForegroundColor Cyan
Write-Host "   docker-compose -f infra/docker-compose-infrastructure.yaml down -v" -ForegroundColor White

Write-Host "`n"


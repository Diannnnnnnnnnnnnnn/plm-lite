# PLM-Lite Services Stop Script
Write-Host "========================================"
Write-Host "Stopping PLM-Lite Services"
Write-Host "========================================`n"

Write-Host "Stopping all Java processes (backend services)..." -ForegroundColor Yellow
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

Write-Host "Stopping all Node processes (frontend)..." -ForegroundColor Yellow
Stop-Process -Name node -Force -ErrorAction SilentlyContinue

Write-Host "`n========================================"
Write-Host "All services have been stopped!" -ForegroundColor Green
Write-Host "========================================`n"

Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

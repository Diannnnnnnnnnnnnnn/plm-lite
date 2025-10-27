# Restart Change Service Script
Write-Host "========================================"
Write-Host "Restarting Change Service"
Write-Host "========================================"

# Find and kill the change-service process
Write-Host "`nStopping change-service..." -ForegroundColor Yellow
$changeServiceProcess = Get-NetTCPConnection -LocalPort 8084 -ErrorAction SilentlyContinue | 
    Select-Object -ExpandProperty OwningProcess | 
    Get-Process -ErrorAction SilentlyContinue

if ($changeServiceProcess) {
    Write-Host "Found change-service process (PID: $($changeServiceProcess.Id))" -ForegroundColor Cyan
    Stop-Process -Id $changeServiceProcess.Id -Force
    Write-Host "✓ Change service stopped" -ForegroundColor Green
    Start-Sleep -Seconds 3
} else {
    Write-Host "No change-service process found on port 8084" -ForegroundColor Yellow
}

# Start change-service in a new window
Write-Host "`nStarting change-service on port 8084..." -ForegroundColor Green
$rootDir = Get-Location
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\change-service'; Write-Host 'Starting Change Service...' -ForegroundColor Cyan; mvn spring-boot:run" -WindowStyle Normal

Write-Host "`n========================================"
Write-Host "Change Service is restarting!" -ForegroundColor Green
Write-Host "========================================"
Write-Host "`nWait 30-60 seconds for the service to fully start."
Write-Host "Watch the new window for startup completion."
Write-Host "`nChange Service: http://localhost:8084" -ForegroundColor Cyan
Write-Host "`nPress any key to exit this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")


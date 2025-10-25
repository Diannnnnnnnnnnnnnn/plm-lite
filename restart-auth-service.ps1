# Restart Auth-Service Script

Write-Host "Restarting auth-service..." -ForegroundColor Cyan
Write-Host ""

# Find and kill auth-service process
Write-Host "Stopping auth-service process..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.Path -like "*auth-service*"} | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Start auth-service in new window
Write-Host "Starting auth-service..." -ForegroundColor Yellow
$scriptPath = Get-Location
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$scriptPath\auth-service'; mvn spring-boot:run"

Write-Host ""
Write-Host "Auth-service is starting in a new window..." -ForegroundColor Green
Write-Host "Wait 2-3 minutes for it to fully start, then run:" -ForegroundColor Yellow
Write-Host "  .\quick-test-redis.ps1" -ForegroundColor Cyan
Write-Host ""


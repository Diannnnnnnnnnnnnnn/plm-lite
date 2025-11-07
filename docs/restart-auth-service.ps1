# Restart Auth Service
Write-Host "Restarting Auth Service..." -ForegroundColor Cyan

# Find and stop Auth Service
Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -like "*Auth Service*"
} | Stop-Process -Force

Write-Host "Starting Auth Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\auth-service'; mvn spring-boot:run" -WindowStyle Normal

Write-Host "Waiting 20 seconds for Auth Service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host ""
Write-Host "Auth Service restarted!" -ForegroundColor Green
Write-Host "Check: http://localhost:8110/actuator/health" -ForegroundColor Cyan
Write-Host ""


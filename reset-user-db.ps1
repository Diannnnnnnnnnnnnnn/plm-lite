# Reset User Service Database
# This script stops the user service, deletes the database, and restarts it to reinitialize demo users

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Resetting User Service Database..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Stop user-service if it's running
Write-Host "`nStopping user-service..." -ForegroundColor Yellow
$userService = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -like "*user-service*" -or 
    (Get-WmiObject Win32_Process -Filter "ProcessId = $($_.Id)").CommandLine -like "*user-service*"
}

if ($userService) {
    Stop-Process -Id $userService.Id -Force
    Write-Host "User service stopped." -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "User service is not running." -ForegroundColor Gray
}

# Delete the database files
Write-Host "`nDeleting database files..." -ForegroundColor Yellow
$dbPath = "user-service\data"
if (Test-Path $dbPath) {
    Remove-Item "$dbPath\userdb.mv.db" -Force -ErrorAction SilentlyContinue
    Remove-Item "$dbPath\userdb.trace.db" -Force -ErrorAction SilentlyContinue
    Write-Host "Database files deleted." -ForegroundColor Green
} else {
    Write-Host "Database path not found." -ForegroundColor Gray
}

# Restart the user service
Write-Host "`nRestarting user-service..." -ForegroundColor Yellow
Write-Host "Starting user-service on port 8083..." -ForegroundColor Cyan
Write-Host "(The DataInitializer will create demo users: demo, guodian, labubu, vivi)" -ForegroundColor Gray

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\user-service'; Write-Host 'Starting User Service...' -ForegroundColor Green; mvn spring-boot:run"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Database reset complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nDemo Users:" -ForegroundColor Yellow
Write-Host "  - demo/demo (USER)" -ForegroundColor White
Write-Host "  - guodian/password (REVIEWER)" -ForegroundColor White
Write-Host "  - labubu/password (EDITOR)" -ForegroundColor White
Write-Host "  - vivi/password (APPROVER)" -ForegroundColor White
Write-Host "`nWait 30-60 seconds for the service to fully start before testing login." -ForegroundColor Gray


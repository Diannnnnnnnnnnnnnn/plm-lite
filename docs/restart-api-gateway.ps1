# Restart API Gateway
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Restarting API Gateway" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# Find and kill existing API Gateway process
Write-Host "Looking for API Gateway process..." -ForegroundColor Yellow
$javaProcesses = Get-WmiObject Win32_Process -Filter "name = 'java.exe'" | Where-Object {
    $_.CommandLine -like "*api-gateway*"
}

if ($javaProcesses) {
    foreach ($process in $javaProcesses) {
        Write-Host "Stopping API Gateway (PID: $($process.ProcessId))..." -ForegroundColor Red
        Stop-Process -Id $process.ProcessId -Force
    }
    Write-Host "API Gateway stopped." -ForegroundColor Green
    Start-Sleep -Seconds 3
} else {
    Write-Host "No existing API Gateway process found." -ForegroundColor Yellow
}

# Start API Gateway
Write-Host ""
Write-Host "Starting API Gateway on port 8080..." -ForegroundColor Green
$projectRoot = Get-Location
Start-Process cmd -ArgumentList "/k", "cd /d $projectRoot\api-gateway && echo ========================================== && echo   API Gateway - Port 8080 && echo   JWT Authentication ^& Routing && echo   UPDATED ROUTING CONFIGURATION && echo ========================================== && echo. && mvn spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "API Gateway is starting..." -ForegroundColor Green
Write-Host "Please wait 20-30 seconds for startup" -ForegroundColor Yellow
Write-Host ""
Write-Host "Fixes Applied:" -ForegroundColor Cyan
Write-Host "  ✅ Task Service: /api/tasks → /api/tasks" -ForegroundColor Green
Write-Host "  ✅ Document Service: /api/documents → /api/v1/documents" -ForegroundColor Green
Write-Host "  ✅ Change Service: /api/changes → /api/changes" -ForegroundColor Green
Write-Host "  ✅ BOM Service: /api/boms/parts → /api/v1/parts" -ForegroundColor Green
Write-Host ""
Write-Host "After Gateway starts, refresh your browser!" -ForegroundColor Yellow

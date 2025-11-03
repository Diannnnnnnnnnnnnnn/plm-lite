# ============================================
# Start All PLM Services with MySQL + Frontend
# ============================================
# This script starts all services in separate PowerShell windows
# All services will use MySQL for persistent data storage

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Starting PLM System - All Services" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Define services in startup order
$services = @(
    @{Name="Graph Service"; Path="infra\graph-service"; Port=8090; Delay=40},
    @{Name="Workflow Orchestrator"; Path="workflow-orchestrator"; Port=8086; Delay=40},
    @{Name="User Service"; Path="user-service"; Port=8083; Delay=45},
    @{Name="Task Service"; Path="task-service"; Port=8082; Delay=45},
    @{Name="Document Service"; Path="document-service"; Port=8081; Delay=45},
    @{Name="BOM Service"; Path="bom-service"; Port=8089; Delay=45},
    @{Name="Change Service"; Path="change-service"; Port=8084; Delay=30},
    @{Name="Search Service"; Path="infra\search-service"; Port=8091; Delay=30}
)

$projectRoot = $PWD.Path

# Start backend services
foreach ($service in $services) {
    Write-Host "[$($service.Name)]" -ForegroundColor Yellow
    Write-Host "  Port: $($service.Port)" -ForegroundColor White
    Write-Host "  Path: $($service.Path)" -ForegroundColor White
    
    # Create the PowerShell command to run in new window
    $command = @"
Set-Location '$projectRoot\$($service.Path)'
Write-Host '==========================================' -ForegroundColor Cyan
Write-Host '  $($service.Name)' -ForegroundColor Cyan
Write-Host '  Port: $($service.Port)' -ForegroundColor Cyan
Write-Host '  Database: MySQL' -ForegroundColor Green
Write-Host '==========================================' -ForegroundColor Cyan
Write-Host ''
mvn spring-boot:run
"@
    
    # Start service in new window
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $command
    
    Write-Host "  Status: Started in new window" -ForegroundColor Green
    Write-Host "  Waiting $($service.Delay) seconds for startup...`n" -ForegroundColor Gray
    
    Start-Sleep -Seconds $service.Delay
}

# Start Frontend
Write-Host "[Frontend - React]" -ForegroundColor Yellow
Write-Host "  Port: 3000" -ForegroundColor White
Write-Host "  Path: frontend" -ForegroundColor White

$frontendCommand = @"
Set-Location '$projectRoot\frontend'
Write-Host '==========================================' -ForegroundColor Cyan
Write-Host '  Frontend - React' -ForegroundColor Cyan
Write-Host '  Port: 3000' -ForegroundColor Cyan
Write-Host '  Development Server' -ForegroundColor Green
Write-Host '==========================================' -ForegroundColor Cyan
Write-Host ''
npm start
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCommand

Write-Host "  Status: Started in new window" -ForegroundColor Green
Write-Host "  Frontend starting...`n" -ForegroundColor Gray

Start-Sleep -Seconds 10

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  All Services Started Successfully!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Backend Services:" -ForegroundColor Cyan
Write-Host "  Graph Service:        http://localhost:8090" -ForegroundColor White
Write-Host "  Workflow Orchestrator: http://localhost:8086" -ForegroundColor White
Write-Host "  User Service:         http://localhost:8083" -ForegroundColor White
Write-Host "  Task Service:         http://localhost:8082" -ForegroundColor White
Write-Host "  Document Service:     http://localhost:8081" -ForegroundColor White
Write-Host "  BOM Service:          http://localhost:8089" -ForegroundColor White
Write-Host "  Change Service:       http://localhost:8084" -ForegroundColor White
Write-Host "  Search Service:       http://localhost:8091`n" -ForegroundColor White

Write-Host "Frontend:" -ForegroundColor Cyan
Write-Host "  React UI:             http://localhost:3000`n" -ForegroundColor White

Write-Host "Database Status:" -ForegroundColor Cyan
Write-Host "  Most services use MySQL" -ForegroundColor Green
Write-Host "  Graph service uses Neo4j" -ForegroundColor Green
Write-Host "  Workflow uses H2 (dev mode) or MySQL (prod mode)" -ForegroundColor Green
Write-Host "  Data will persist across restarts`n" -ForegroundColor Green

Write-Host "Total Windows Opened: 9" -ForegroundColor Yellow
Write-Host "  - 8 Backend services (including Search)" -ForegroundColor White
Write-Host "  - 1 Frontend`n" -ForegroundColor White

Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Wait for all services to finish starting (~5 minutes)" -ForegroundColor White
Write-Host "  2. Open browser: http://localhost:3000" -ForegroundColor White
Write-Host "  3. Re-create your data (users, documents, parts)" -ForegroundColor White
Write-Host "  4. Test creating changes with parts!`n" -ForegroundColor White

Write-Host "To Stop All Services:" -ForegroundColor Yellow
Write-Host "  Run: " -NoNewline -ForegroundColor White
Write-Host ".\stop-all-services.ps1`n" -ForegroundColor Gray

# PLM-Lite Services Startup Script
Write-Host "========================================"
Write-Host "Starting PLM-Lite Services"
Write-Host "========================================"

# Function to start a service in a new window
function Start-Service {
    param(
        [string]$Name,
        [string]$Path,
        [string]$Command,
        [int]$Port
    )
    Write-Host "Starting $Name on port $Port..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$Path'; $Command" -WindowStyle Normal
    Start-Sleep -Seconds 2
}

# Kill any existing processes
Write-Host "`nStopping any running services..." -ForegroundColor Yellow
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Stop-Process -Name node -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

Write-Host "`nStarting backend services...`n" -ForegroundColor Cyan

# Get the current directory
$rootDir = Get-Location

# Start Backend Services
Start-Service -Name "BOM Service" -Path "$rootDir\bom-service" -Command "mvn spring-boot:run" -Port 8089
Start-Service -Name "Change Service" -Path "$rootDir\change-service" -Command "mvn spring-boot:run" -Port 8084
Start-Service -Name "Document Service" -Path "$rootDir\document-service" -Command "mvn spring-boot:run" -Port 8081
Start-Service -Name "Task Service" -Path "$rootDir\task-service" -Command "mvn spring-boot:run" -Port 8082
Start-Service -Name "User Service" -Path "$rootDir\user-service" -Command "mvn spring-boot:run" -Port 8083
Start-Service -Name "Workflow Orchestrator" -Path "$rootDir\workflow-orchestrator" -Command "mvn spring-boot:run" -Port 8086

Write-Host "`nWaiting for backend services to initialize (60 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Start Frontend
Write-Host "`nStarting frontend..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$rootDir\frontend'; npm start" -WindowStyle Normal

Write-Host "`n========================================"
Write-Host "All services are starting!" -ForegroundColor Green
Write-Host "========================================`n"

Write-Host "Backend Services:" -ForegroundColor Cyan
Write-Host "  - Document Service:      http://localhost:8081"
Write-Host "  - Task Service:          http://localhost:8082"
Write-Host "  - User Service:          http://localhost:8083"
Write-Host "  - Change Service:        http://localhost:8084"
Write-Host "  - Workflow Orchestrator: http://localhost:8086"
Write-Host "  - BOM Service:           http://localhost:8089"

Write-Host "`nFrontend:" -ForegroundColor Cyan
Write-Host "  - React App:             http://localhost:3001"

Write-Host "`nPlease wait 1-2 minutes for all services to fully start." -ForegroundColor Yellow
Write-Host "`nPress any key to exit this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

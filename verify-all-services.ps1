# Verify All Services Are Running
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PLM Services Health Check" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$services = @(
    @{Name="Eureka Server"; Port=8761},
    @{Name="API Gateway"; Port=8080},
    @{Name="Document Service"; Port=8081},
    @{Name="Task Service"; Port=8082},
    @{Name="User Service"; Port=8083},
    @{Name="Workflow Orchestrator"; Port=8086},
    @{Name="Graph Service"; Port=8090}
)

$allUp = $true

foreach ($service in $services) {
    Write-Host "$($service.Name) (Port $($service.Port))..." -NoNewline
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:$($service.Port)/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
        if ($health.status -eq "UP") {
            Write-Host " UP" -ForegroundColor Green
        } else {
            Write-Host " $($health.status)" -ForegroundColor Yellow
            $allUp = $false
        }
    } catch {
        Write-Host " DOWN" -ForegroundColor Red
        $allUp = $false
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan

if ($allUp) {
    Write-Host "  All Services Running!" -ForegroundColor Green
    Write-Host "========================================`n" -ForegroundColor Cyan
    
    Write-Host "Check Eureka Registry:" -ForegroundColor Yellow
    Write-Host "  http://localhost:8761/`n" -ForegroundColor White
    
    Write-Host "Access Application:" -ForegroundColor Yellow
    Write-Host "  http://localhost:8111`n" -ForegroundColor White
    
    Write-Host "Now you can:" -ForegroundColor Cyan
    Write-Host "  1. Upload a document" -ForegroundColor White
    Write-Host "  2. Submit for review (select reviewers)" -ForegroundColor White
    Write-Host "  3. Reviewer will see the task" -ForegroundColor White
    Write-Host "  4. Complete the review" -ForegroundColor White
} else {
    Write-Host "  Some Services Are Down!" -ForegroundColor Red
    Write-Host "========================================`n" -ForegroundColor Cyan
    Write-Host "Please start all required services before testing." -ForegroundColor Yellow
}






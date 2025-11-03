# ============================================
# Check PLM Services Status (Backend + Frontend)
# ============================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PLM Services Status Check" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$services = @(
    @{Name="Graph Service"; Port=8090; Endpoint="/api/graph/health"},
    @{Name="Workflow Orchestrator"; Port=8086; Endpoint="/actuator/health"},
    @{Name="User Service"; Port=8083; Endpoint="/users"},
    @{Name="Task Service"; Port=8082; Endpoint="/tasks"},
    @{Name="Document Service"; Port=8081; Endpoint="/api/v1/documents"},
    @{Name="BOM Service"; Port=8089; Endpoint="/parts"},
    @{Name="Change Service"; Port=8084; Endpoint="/api/changes"},
    @{Name="Frontend"; Port=3000; Endpoint="/"}
)

$runningCount = 0
$totalServices = $services.Count

foreach ($service in $services) {
    Write-Host "[$($service.Name)]" -ForegroundColor Yellow
    Write-Host "  Port: $($service.Port)" -ForegroundColor White
    
    try {
        $url = "http://localhost:$($service.Port)$($service.Endpoint)"
        $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 3 -ErrorAction Stop
        Write-Host "  Status: " -NoNewline -ForegroundColor White
        Write-Host "RUNNING ✓" -ForegroundColor Green
        Write-Host "  Response: $($response.StatusCode)`n" -ForegroundColor Gray
        $runningCount++
    } catch {
        if ($_.Exception.Response) {
            Write-Host "  Status: " -NoNewline -ForegroundColor White
            Write-Host "RUNNING ✓" -ForegroundColor Green
            Write-Host "  Response: $($_.Exception.Response.StatusCode)`n" -ForegroundColor Gray
            $runningCount++
        } else {
            Write-Host "  Status: " -NoNewline -ForegroundColor White
            Write-Host "NOT RUNNING ✗" -ForegroundColor Red
            Write-Host "  Error: Service not responding`n" -ForegroundColor Red
        }
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Summary: $runningCount / $totalServices services running" -ForegroundColor White
Write-Host "========================================`n" -ForegroundColor Cyan

if ($runningCount -eq $totalServices) {
    Write-Host "✓ All services are running!" -ForegroundColor Green
    Write-Host "  Open: http://localhost:3000`n" -ForegroundColor Cyan
} elseif ($runningCount -gt 0) {
    Write-Host "⚠️  Some services are not running" -ForegroundColor Yellow
    Write-Host "  Check service windows for errors`n" -ForegroundColor Yellow
} else {
    Write-Host "✗ No services are running" -ForegroundColor Red
    Write-Host "  Run: start-all-services.bat`n" -ForegroundColor Yellow
}

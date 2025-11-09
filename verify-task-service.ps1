# Wait for Task Service to start and verify
Write-Host "Waiting for Task Service to start..." -ForegroundColor Yellow
Write-Host "(Start the Task Service in another window with: mvn spring-boot:run)" -ForegroundColor Cyan
Write-Host ""

$maxAttempts = 30
$attempt = 0

while ($attempt -lt $maxAttempts) {
    $attempt++
    Write-Host "Attempt $attempt/$maxAttempts..." -NoNewline
    
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -Method Get -TimeoutSec 2
        if ($health.status -eq "UP") {
            Write-Host " SUCCESS!" -ForegroundColor Green
            Write-Host ""
            Write-Host "========================================" -ForegroundColor Green
            Write-Host "  Task Service is RUNNING!" -ForegroundColor Green
            Write-Host "========================================" -ForegroundColor Green
            Write-Host ""
            Write-Host "Now you can:" -ForegroundColor Cyan
            Write-Host "  1. Submit a document for review" -ForegroundColor White
            Write-Host "  2. The workflow will create a task" -ForegroundColor White
            Write-Host "  3. The reviewer will see it in their task list" -ForegroundColor White
            exit 0
        }
    } catch {
        Write-Host " Not ready yet" -ForegroundColor Yellow
    }
    
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "Task Service did not start within 60 seconds." -ForegroundColor Red
Write-Host "Check the Task Service console for errors." -ForegroundColor Red






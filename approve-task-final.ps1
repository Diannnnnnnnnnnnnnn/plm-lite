# Approve Task 36

Write-Host "=== Approving Task 36 ===" -ForegroundColor Cyan

$approvalBody = @{
    status = "COMPLETED"
    approved = "APPROVED"
    decision = "APPROVED"
    comments = "Approved for testing"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/tasks/36/status" -Method Put -Body $approvalBody -ContentType "application/json"
    
    Write-Host "Task Approved!" -ForegroundColor Green
    Write-Host "Task ID: $($response.id)" -ForegroundColor Green
    Write-Host "Status: $($response.taskStatus)" -ForegroundColor Yellow  
    Write-Host "Decision: $($response.decision)" -ForegroundColor Yellow
    
} catch {
    Write-Host "Error approving task: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 3

Write-Host "`n=== Checking Change Status ===" -ForegroundColor Cyan

try {
    $change = Invoke-RestMethod -Uri "http://localhost:8084/api/changes/d64f0e66-7147-4a61-9c37-8468bccb7080" -Method Get
    Write-Host "Change Status: $($change.status)" -ForegroundColor Yellow
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Checking Document Status ===" -ForegroundColor Cyan

try {
    $document = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/65356e4c-4b0a-4775-b718-7cba4e844198" -Method Get
    Write-Host "Document Status: $($document.status)" -ForegroundColor Yellow
    Write-Host "Document Version: R$($document.revision).V$($document.version)" -ForegroundColor Yellow
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nCheck workflow-orchestrator console for detailed logs!" -ForegroundColor Magenta




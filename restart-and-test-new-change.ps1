# Test New Change Approval Flow

Write-Host "=== IMPORTANT: Restart workflow-orchestrator first! ===" -ForegroundColor Red
Write-Host "Press Ctrl+C to cancel, or any key to continue after restart..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host "`n=== Creating New Change for Testing ===" -ForegroundColor Cyan

# Use the document from your test
$documentId = "c77bb143-bce9-4ef0-bf17-7105e6ca3eba"

$createChangeBody = @{
    title = "Test Automatic Update"
    changeReason = "Testing automatic document version update on approval"
    changeDocument = $documentId
    stage = "PRODUCTION"
    changeClass = "MINOR"
    product = "Test Product"
    creator = "vivi"
} | ConvertTo-Json

Write-Host "Creating change..." -ForegroundColor Yellow

try {
    $change = Invoke-RestMethod -Uri "http://localhost:8084/api/changes" -Method Post -Body $createChangeBody -ContentType "application/json"
    
    $changeId = $change.id
    Write-Host "Change Created!" -ForegroundColor Green
    Write-Host "Change ID: $changeId" -ForegroundColor Green
    
    Write-Host "`n=== Submitting for Review ===" -ForegroundColor Cyan
    
    $submitBody = @{
        reviewerIds = @("4")
    } | ConvertTo-Json
    
    $result = Invoke-RestMethod -Uri "http://localhost:8084/api/changes/$changeId/submit" -Method Post -Body $submitBody -ContentType "application/json"
    
    Write-Host "Submitted for Review!" -ForegroundColor Green
    Write-Host "Check workflow-orchestrator logs for:" -ForegroundColor Yellow
    Write-Host "  Task service will complete this job when task is approved/rejected" -ForegroundColor Gray
    
    Write-Host "`n=== Now Approve the Task ===" -ForegroundColor Cyan
    Write-Host "Find the task ID in the logs, then approve it with:" -ForegroundColor Yellow
    Write-Host "  PUT http://localhost:8085/api/tasks/{taskId}/status" -ForegroundColor Gray
    Write-Host "  Body: {status: COMPLETED, approved: APPROVED, decision: APPROVED}" -ForegroundColor Gray
    
    Write-Host "`n=== Expected Result ===" -ForegroundColor Magenta
    Write-Host "After approval, you should see:" -ForegroundColor Yellow
    Write-Host "  Change Status: RELEASED" -ForegroundColor Gray
    Write-Host "  Document Status: IN_WORK" -ForegroundColor Gray
    Write-Host "  Document Version: Incremented (e.g., v1.0 -> v1.1)" -ForegroundColor Gray
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}




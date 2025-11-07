# Fix Current Change - Manual Document Update

Write-Host "=== Fixing Change a9d73b1e-4e53-4987-9a88-dd1977157c33 ===" -ForegroundColor Cyan

$changeId = "a9d73b1e-4e53-4987-9a88-dd1977157c33"
$documentId = "cbfa436f-aa27-4c6f-bf09-d6e158f24b58"

Write-Host "`nChange is already RELEASED" -ForegroundColor Green
Write-Host "Now updating document version..." -ForegroundColor Yellow

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$documentId/initiate-change-edit?changeId=$changeId&user=vivi" -Method Post
    
    Write-Host "`n=== SUCCESS ===" -ForegroundColor Green
    Write-Host "New Document Created!" -ForegroundColor Green
    Write-Host "  New Document ID: $($result.id)" -ForegroundColor Cyan
    Write-Host "  Status: $($result.status)" -ForegroundColor Yellow
    Write-Host "  Version: R$($result.revision).V$($result.version)" -ForegroundColor Yellow
    
    Write-Host "`n=== Old Document ===" -ForegroundColor Gray
    $oldDoc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$documentId" -Method Get
    Write-Host "  Status: $($oldDoc.status)" -ForegroundColor Gray
    Write-Host "  Version: R$($oldDoc.revision).V$($oldDoc.version)" -ForegroundColor Gray
    Write-Host "  Active: $($oldDoc.active)" -ForegroundColor Gray
    
} catch {
    Write-Host "`nError: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== IMPORTANT: Restart workflow-orchestrator! ===" -ForegroundColor Red
Write-Host "The service is still using OLD code." -ForegroundColor Red
Write-Host "Restart to load the new message-based BPMN." -ForegroundColor Yellow




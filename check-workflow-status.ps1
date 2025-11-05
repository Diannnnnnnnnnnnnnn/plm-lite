# Check Workflow and Manually Progress if Needed

Write-Host "=== Workflow Status Check ===" -ForegroundColor Cyan
Write-Host "Process Instance: 2251799814803062" -ForegroundColor Yellow
Write-Host "Change ID: d64f0e66-7147-4a61-9c37-8468bccb7080" -ForegroundColor Yellow

Write-Host "`n=== Current State ===" -ForegroundColor Cyan

# Check change status
try {
    $change = Invoke-RestMethod -Uri "http://localhost:8084/api/changes/d64f0e66-7147-4a61-9c37-8468bccb7080" -Method Get
    Write-Host "Change Status: $($change.status)" -ForegroundColor $(if ($change.status -eq "RELEASED") { "Green" } else { "Yellow" })
} catch {
    Write-Host "Change check error: $($_.Exception.Message)" -ForegroundColor Red
}

# Check document status  
try {
    $document = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/65356e4c-4b0a-4775-b718-7cba4e844198" -Method Get
    Write-Host "Document Status: $($document.status)" -ForegroundColor $(if ($document.status -eq "IN_WORK") { "Green" } else { "Yellow" })
    Write-Host "Document Version: R$($document.revision) V$($document.version)" -ForegroundColor Yellow
} catch {
    Write-Host "Document check error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== SOLUTION ===" -ForegroundColor Magenta
Write-Host "The workflow might have stopped due to max retries." -ForegroundColor Yellow
Write-Host "We need to manually update the change and document." -ForegroundColor Yellow

Write-Host "`n===Manual Update Change Status ===" -ForegroundColor Cyan

try {
    $statusBody = @{
        status = "RELEASED"
    } | ConvertTo-Json
    
    $result = Invoke-RestMethod -Uri "http://localhost:8084/api/changes/d64f0e66-7147-4a61-9c37-8468bccb7080/status" -Method Put -Body $statusBody -ContentType "application/json"
    Write-Host "Change status updated to RELEASED" -ForegroundColor Green
} catch {
    Write-Host "Error updating change: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Manual Document Version Update ===" -ForegroundColor Cyan

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/65356e4c-4b0a-4775-b718-7cba4e844198/initiate-change-edit?changeId=d64f0e66-7147-4a61-9c37-8468bccb7080&user=vivi" -Method Post
    Write-Host "Document version updated" -ForegroundColor Green
    Write-Host "New Document ID: $($result.id)" -ForegroundColor Green
    Write-Host "New Status: $($result.status)" -ForegroundColor Yellow
    Write-Host "New Version: R$($result.revision) V$($result.version)" -ForegroundColor Yellow
} catch {
    Write-Host "Error updating document: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Verification ===" -ForegroundColor Cyan

# Recheck change
try {
    $change = Invoke-RestMethod -Uri "http://localhost:8084/api/changes/d64f0e66-7147-4a61-9c37-8468bccb7080" -Method Get
    Write-Host "Final Change Status: $($change.status)" -ForegroundColor Green
} catch {}

# Check latest document
try {
    $document = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/65356e4c-4b0a-4775-b718-7cba4e844198" -Method Get
    Write-Host "Latest Document Status: $($document.status)" -ForegroundColor Green
    Write-Host "Latest Version: R$($document.revision) V$($document.version)" -ForegroundColor Green
} catch {}




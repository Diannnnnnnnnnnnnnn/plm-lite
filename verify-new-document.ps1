# Verify the New Document Version

Write-Host "=== Original Document (v1.0 RELEASED) ===" -ForegroundColor Cyan
try {
    $oldDoc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/65356e4c-4b0a-4775-b718-7cba4e844198" -Method Get
    Write-Host "ID: $($oldDoc.id)" -ForegroundColor Gray
    Write-Host "Status: $($oldDoc.status)" -ForegroundColor Yellow
    Write-Host "Version: R$($oldDoc.revision) V$($oldDoc.version)" -ForegroundColor Yellow
    Write-Host "Active: $($oldDoc.active)" -ForegroundColor $(if ($oldDoc.active) { "Green" } else { "Red" })
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== New Document (v1.1 IN_WORK) ===" -ForegroundColor Cyan
try {
    $newDoc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/a035f83c-670c-4c41-8bb6-fb7c509c01e1" -Method Get
    Write-Host "ID: $($newDoc.id)" -ForegroundColor Gray
    Write-Host "Status: $($newDoc.status)" -ForegroundColor Green
    Write-Host "Version: R$($newDoc.revision) V$($newDoc.version)" -ForegroundColor Green  
    Write-Host "Active: $($newDoc.active)" -ForegroundColor $(if ($newDoc.active) { "Green" } else { "Red" })
    Write-Host "`nThis is the editable version created by the approved change!" -ForegroundColor Magenta
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}




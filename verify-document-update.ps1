# Verify Document Update

$documentId = "cbfa436f-aa27-4c6f-bf09-d6e158f24b58"
$changeId = "a9d73b1e-4e53-4987-9a88-dd1977157c33"

Write-Host "=== Attempting Document Update ===" -ForegroundColor Cyan

try {
    Write-Host "Calling initiate-change-edit..." -ForegroundColor Yellow
    $newDoc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$documentId/initiate-change-edit?changeId=$changeId&user=vivi" -Method Post
    
    Write-Host "`nSUCCESS! New Version Created:" -ForegroundColor Green
    Write-Host "  New Document ID: $($newDoc.id)" -ForegroundColor Cyan
    Write-Host "  Status: $($newDoc.status)" -ForegroundColor Green
    Write-Host "  Revision: $($newDoc.revision)" -ForegroundColor Green
    Write-Host "  Version: $($newDoc.version)" -ForegroundColor Green
    Write-Host "  Full Version: R$($newDoc.revision).V$($newDoc.version)" -ForegroundColor Yellow
    
} catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    
    # Check if document is already in IN_WORK status
    Write-Host "`nChecking current document status..." -ForegroundColor Yellow
    try {
        $doc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$documentId" -Method Get
        Write-Host "Current Document:" -ForegroundColor Cyan
        Write-Host "  ID: $($doc.id)" -ForegroundColor Gray
        Write-Host "  Status: $($doc.status)" -ForegroundColor Yellow
        Write-Host "  Version: R$($doc.revision).V$($doc.version)" -ForegroundColor Yellow
        
        if ($doc.status -ne "RELEASED") {
            Write-Host "`nDocument is not RELEASED, can't initiate change edit" -ForegroundColor Red
        }
    } catch {
        Write-Host "Error checking document: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Check All Active Documents ===" -ForegroundColor Cyan
try {
    $docs = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents" -Method Get
    Write-Host "Total active documents: $($docs.Count)" -ForegroundColor Green
    
    # Find documents for this master
    $relevantDocs = $docs | Where-Object { $_.id -eq $documentId -or $_.master.id -eq ($docs | Where-Object { $_.id -eq $documentId }).master.id }
    
    Write-Host "`nDocuments for this master:" -ForegroundColor Cyan
    foreach ($d in $relevantDocs) {
        Write-Host "  ID: $($d.id)" -ForegroundColor Gray
        Write-Host "  Status: $($d.status)" -ForegroundColor $(if ($d.status -eq "IN_WORK") { "Green" } else { "Yellow" })
        Write-Host "  Version: R$($d.revision).V$($d.version)" -ForegroundColor Cyan
        Write-Host "  Active: $($d.active)" -ForegroundColor Gray
        Write-Host "  ---" -ForegroundColor Gray
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}




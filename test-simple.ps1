$docs = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents"
if ($docs.Count -gt 0) {
    $doc = $docs[0]
    Write-Host "Document ID: $($doc.id)"
    Write-Host "OriginalFilename: $($doc.originalFilename)"
    if ($doc.originalFilename) {
        Write-Host "WORKS!" -ForegroundColor Green
    } else {
        Write-Host "NOT WORKING - Restart service" -ForegroundColor Red
    }
}


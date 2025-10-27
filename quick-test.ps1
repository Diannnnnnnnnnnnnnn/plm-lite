# Quick Test for MinIO Metadata Issue
Write-Host "Testing MinIO Integration..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081/api/v1/documents"

# Create document
Write-Host "`nCreating document..." -ForegroundColor Yellow
$body = '{"title":"QuickTest","creator":"User","stage":"CONCEPTUAL_DESIGN","category":"Test"}'
$doc = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json"
Write-Host "Created: $($doc.id)" -ForegroundColor Green

# Upload file
Write-Host "`nUploading file..." -ForegroundColor Yellow
"Test" | Out-File test.txt -Encoding UTF8
$form = @{file = Get-Item test.txt; user = "User"}
$upload = Invoke-RestMethod -Uri "$baseUrl/$($doc.id)/upload" -Method Post -Form $form
Write-Host "Uploaded: $upload" -ForegroundColor Green

# Check document info
Write-Host "`nChecking metadata..." -ForegroundColor Yellow
$info = Invoke-RestMethod -Uri "$baseUrl/$($doc.id)"
Write-Host "FileKey: $($info.fileKey)"
Write-Host "OriginalFilename: $($info.originalFilename)"
Write-Host "ContentType: $($info.contentType)"
Write-Host "FileSize: $($info.fileSize)"
Write-Host "StorageLocation: $($info.storageLocation)"

if ($info.originalFilename) {
    Write-Host "`n✅ METADATA IS WORKING!" -ForegroundColor Green
} else {
    Write-Host "`n❌ NO METADATA - SERVICE NEEDS RESTART" -ForegroundColor Red
    Write-Host "Run: cd document-service && mvn spring-boot:run" -ForegroundColor Yellow
}

Remove-Item test.txt -ErrorAction SilentlyContinue


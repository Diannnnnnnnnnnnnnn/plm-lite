# Debug Delete Issue
Write-Host "`n=== MinIO Delete Debug ===" -ForegroundColor Cyan

# Step 1: Check MinIO is running
Write-Host "`n[1] Checking MinIO..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000/minio/health/live" -TimeoutSec 3
    Write-Host "✅ MinIO is UP" -ForegroundColor Green
} catch {
    Write-Host "❌ MinIO is DOWN!" -ForegroundColor Red
    exit 1
}

# Step 2: Get a document with a file
Write-Host "`n[2] Finding a document with a file..." -ForegroundColor Yellow
$docs = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents"
$docWithFile = $docs | Where-Object { $null -ne $_.fileKey -and $_.fileKey -ne "" } | Select-Object -First 1

if (-not $docWithFile) {
    Write-Host "❌ No documents with files found. Upload a document first." -ForegroundColor Red
    exit 1
}

Write-Host "Found document:" -ForegroundColor Cyan
Write-Host "  ID: $($docWithFile.id)"
Write-Host "  FileKey: $($docWithFile.fileKey)"
Write-Host "  OriginalFilename: $($docWithFile.originalFilename)"
Write-Host "  StorageLocation: $($docWithFile.storageLocation)"

# Step 3: Check if file exists in MinIO
Write-Host "`n[3] Checking if file exists..." -ForegroundColor Yellow
try {
    $exists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file/exists"
    if ($exists) {
        Write-Host "✅ File EXISTS in storage" -ForegroundColor Green
    } else {
        Write-Host "⚠️  File does NOT exist in storage!" -ForegroundColor Yellow
        Write-Host "   This means the file was never uploaded to MinIO" -ForegroundColor Gray
        Write-Host "   Check MinIO Console: http://localhost:9001" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Failed to check existence: $_" -ForegroundColor Red
}

# Step 4: Get file info
Write-Host "`n[4] Getting file info..." -ForegroundColor Yellow
try {
    $fileInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file/info"
    Write-Host "File Info:" -ForegroundColor Cyan
    $fileInfo | Format-List
} catch {
    Write-Host "❌ Failed to get file info: $_" -ForegroundColor Red
}

# Step 5: Try to delete
Write-Host "`n[5] Attempting to delete..." -ForegroundColor Yellow
try {
    $deleteResult = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file" -Method Delete
    Write-Host "✅ Delete succeeded: $deleteResult" -ForegroundColor Green
    
    # Step 6: Verify deletion
    Write-Host "`n[6] Verifying deletion..." -ForegroundColor Yellow
    Start-Sleep -Seconds 1
    $stillExists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($docWithFile.id)/file/exists"
    if ($stillExists) {
        Write-Host "❌ File STILL EXISTS after delete!" -ForegroundColor Red
    } else {
        Write-Host "✅ File successfully deleted!" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Delete FAILED!" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Gray
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
    
    # Show response body if available
    if ($_.ErrorDetails.Message) {
        Write-Host "   Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
    }
}

Write-Host "`n=== Debug Complete ===" -ForegroundColor Cyan
Write-Host "`n💡 Next Steps:" -ForegroundColor Yellow
Write-Host "   1. Check document-service logs for errors"
Write-Host "   2. Open MinIO Console: http://localhost:9001"
Write-Host "   3. Navigate to 'plm-documents' bucket"
Write-Host "   4. Check if file '$($docWithFile.fileKey)' is there"
Write-Host ""



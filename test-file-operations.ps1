# ======================================
# Test File Operations (Delete, Metadata, Exists)
# ======================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Testing File Operations" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8081/api/v1/documents"

# Step 1: Create a test file
Write-Host "[Step 1] Creating test file..." -ForegroundColor Yellow
$testContent = "This is a test document for MinIO integration testing."
$testFile = "test-document.txt"
$testContent | Out-File -FilePath $testFile -Encoding UTF8

# Step 2: Upload document with file
Write-Host "`n[Step 2] Uploading document with file..." -ForegroundColor Yellow

$form = @{
    file = Get-Item $testFile
    title = "Test Document"
    description = "Testing file operations"
    type = "SPECIFICATION"
    creator = "TestUser"
}

try {
    $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/upload" -Method Post -Form $form
    $documentId = $uploadResponse.id
    Write-Host "✅ Document uploaded successfully!" -ForegroundColor Green
    Write-Host "   Document ID: $documentId" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Upload failed: $_" -ForegroundColor Red
    exit 1
}

# Wait a moment for processing
Start-Sleep -Seconds 1

# Step 3: Get document info
Write-Host "`n[Step 3] Getting document info..." -ForegroundColor Yellow
try {
    $docInfo = Invoke-RestMethod -Uri "$baseUrl/$documentId" -Method Get
    Write-Host "✅ Document info retrieved:" -ForegroundColor Green
    Write-Host "   Title: $($docInfo.title)" -ForegroundColor White
    Write-Host "   FileKey: $($docInfo.fileKey)" -ForegroundColor White
} catch {
    Write-Host "❌ Failed to get document info: $_" -ForegroundColor Red
}

# Step 4: Check file metadata
Write-Host "`n[Step 4] Checking file metadata..." -ForegroundColor Yellow
try {
    $fileInfo = Invoke-RestMethod -Uri "$baseUrl/$documentId/file/info" -Method Get
    Write-Host "✅ File metadata retrieved:" -ForegroundColor Green
    Write-Host "   Original Filename: $($fileInfo.originalFilename)" -ForegroundColor White
    Write-Host "   Content Type: $($fileInfo.contentType)" -ForegroundColor White
    Write-Host "   File Size: $($fileInfo.fileSize) bytes" -ForegroundColor White
    Write-Host "   Storage Location: $($fileInfo.storageLocation)" -ForegroundColor White
    Write-Host "   Uploaded At: $($fileInfo.uploadedAt)" -ForegroundColor White
    Write-Host "   File Exists: $($fileInfo.exists)" -ForegroundColor White
    
    if ($fileInfo.exists -eq $false) {
        Write-Host "⚠️  WARNING: File metadata says file doesn't exist!" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Failed to get file metadata: $_" -ForegroundColor Red
    Write-Host "   Response: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
}

# Step 5: Check if file exists
Write-Host "`n[Step 5] Checking if file exists..." -ForegroundColor Yellow
try {
    $exists = Invoke-RestMethod -Uri "$baseUrl/$documentId/file/exists" -Method Get
    if ($exists) {
        Write-Host "✅ File exists in storage" -ForegroundColor Green
    } else {
        Write-Host "❌ File does not exist in storage" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Failed to check file existence: $_" -ForegroundColor Red
}

# Step 6: Download file to verify
Write-Host "`n[Step 6] Downloading file to verify..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "$baseUrl/$documentId/download" -OutFile "downloaded-test.txt"
    $downloadedContent = Get-Content "downloaded-test.txt" -Raw
    if ($downloadedContent -eq $testContent) {
        Write-Host "✅ File downloaded and content matches!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  File downloaded but content doesn't match" -ForegroundColor Yellow
    }
    Remove-Item "downloaded-test.txt"
} catch {
    Write-Host "❌ Failed to download file: $_" -ForegroundColor Red
}

# Step 7: Delete file
Write-Host "`n[Step 7] Deleting file..." -ForegroundColor Yellow
try {
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/$documentId/file" -Method Delete
    Write-Host "✅ File deleted: $deleteResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to delete file: $_" -ForegroundColor Red
    Write-Host "   Response: $($_.Exception.Response.StatusCode)" -ForegroundColor Gray
}

# Step 8: Verify file is deleted
Write-Host "`n[Step 8] Verifying file is deleted..." -ForegroundColor Yellow
try {
    $exists = Invoke-RestMethod -Uri "$baseUrl/$documentId/file/exists" -Method Get
    if ($exists -eq $false) {
        Write-Host "✅ File confirmed deleted (exists = false)" -ForegroundColor Green
    } else {
        Write-Host "❌ File still exists after deletion!" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠️  Could not verify deletion: $_" -ForegroundColor Yellow
}

# Step 9: Try to get metadata after deletion
Write-Host "`n[Step 9] Checking metadata after deletion..." -ForegroundColor Yellow
try {
    $fileInfo = Invoke-RestMethod -Uri "$baseUrl/$documentId/file/info" -Method Get
    Write-Host "⚠️  Metadata still returned (might be cached)" -ForegroundColor Yellow
    Write-Host "   Exists: $($fileInfo.exists)" -ForegroundColor White
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✅ Metadata cleared (404 Not Found)" -ForegroundColor Green
    } else {
        Write-Host "❌ Unexpected error: $_" -ForegroundColor Red
    }
}

# Step 10: Try to download deleted file
Write-Host "`n[Step 10] Trying to download deleted file..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "$baseUrl/$documentId/download" -OutFile "should-not-exist.txt"
    Write-Host "❌ Downloaded deleted file (this shouldn't happen!)" -ForegroundColor Red
    Remove-Item "should-not-exist.txt" -ErrorAction SilentlyContinue
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✅ Cannot download deleted file (404 Not Found)" -ForegroundColor Green
    } else {
        Write-Host "✅ Download failed as expected" -ForegroundColor Green
    }
}

# Cleanup
Write-Host "`n[Cleanup] Removing test file..." -ForegroundColor Yellow
Remove-Item $testFile -ErrorAction SilentlyContinue

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n📝 Summary:" -ForegroundColor Yellow
Write-Host "   Document ID: $documentId" -ForegroundColor White
Write-Host "   You can check MinIO Console: http://localhost:9001" -ForegroundColor White
Write-Host "   Verify file was deleted from 'plm-documents' bucket" -ForegroundColor White

Write-Host "`n"


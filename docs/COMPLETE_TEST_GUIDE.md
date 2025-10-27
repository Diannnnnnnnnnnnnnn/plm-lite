# Complete MinIO Integration Test Guide

## Current Situation
- Code is fixed and ready
- MinIO is running
- Service needs restart with new code
- Then upload NEW document to test

---

## Step-by-Step Instructions

### STEP 1: Restart Document Service

```powershell
# If service is running, stop it (Ctrl+C in its terminal)

# Then restart:
cd document-service
mvn clean spring-boot:run
```

**Wait for:** `Started DocumentServiceApplication in X.XXX seconds`

---

### STEP 2: Verify Service is Ready

```powershell
# In a NEW terminal, test health:
curl http://localhost:8081/api/v1/documents/health/minio
```

Should see: `"status":"UP"`

---

### STEP 3: Upload a Test Document via API

```powershell
# Create a test file
"Test content for MinIO" | Out-File -FilePath test-doc.txt -Encoding UTF8

# Create document
$createBody = @{
    title = "Test Document"
    creator = "TestUser"
    stage = "CONCEPTUAL_DESIGN"
    description = "Testing MinIO delete"
} | ConvertTo-Json

$doc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents" `
    -Method Post `
    -Body $createBody `
    -ContentType "application/json"

Write-Host "Document created: $($doc.id)"

# Upload file to document
$form = @{
    file = Get-Item test-doc.txt
    user = "TestUser"
}

$uploadResult = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($doc.id)/upload" `
    -Method Post `
    -Form $form

Write-Host "File uploaded: $uploadResult"

# Save document ID for later
$docId = $doc.id
Write-Host "`nDocument ID: $docId" -ForegroundColor Green
```

---

### STEP 4: Verify Metadata Was Saved

```powershell
# Get document info
$docInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId"

# Check metadata
Write-Host "`nDocument Metadata:"
Write-Host "  FileKey: $($docInfo.fileKey)"
Write-Host "  OriginalFilename: $($docInfo.originalFilename)"
Write-Host "  ContentType: $($docInfo.contentType)"
Write-Host "  FileSize: $($docInfo.fileSize)"
Write-Host "  StorageLocation: $($docInfo.storageLocation)"
Write-Host "  UploadedAt: $($docInfo.fileUploadedAt)"

# Verify these are NOT null/empty
if ($docInfo.originalFilename) {
    Write-Host "`n✅ Metadata IS saved!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Metadata NOT saved - service not restarted properly!" -ForegroundColor Red
    exit
}
```

---

### STEP 5: Check File Exists in MinIO

```powershell
# Check via API
$exists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"
Write-Host "`nFile exists in storage: $exists" -ForegroundColor $(if ($exists) {"Green"} else {"Red"})

# Get file info
$fileInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/info"
Write-Host "File info retrieved:"
Write-Host "  Exists: $($fileInfo.exists)"
Write-Host "  Size: $($fileInfo.fileSize) bytes"
Write-Host "  Location: $($fileInfo.storageLocation)"
```

---

### STEP 6: Verify in MinIO Console

1. Open: http://localhost:9001
2. Login: `minio` / `password`
3. Navigate to bucket: **Look for your bucket** (might be `documents` or `plm-documents` or `task-files`)
4. Find your file: Should see `{docId}_test-doc.txt`

**Important:** Note which bucket the file is actually in!

---

### STEP 7: Delete the File

```powershell
# Delete via API
Write-Host "`nDeleting file..." -ForegroundColor Yellow

try {
    $deleteResult = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file" -Method Delete
    Write-Host "✅ Delete successful: $deleteResult" -ForegroundColor Green
} catch {
    Write-Host "❌ Delete failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
    
    # Check service logs for details
    Write-Host "`nCheck document-service console for error messages"
    Write-Host "Look for: 'Failed to delete file from MinIO'"
}
```

---

### STEP 8: Verify Deletion

```powershell
# Check if file still exists
$stillExists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"

if ($stillExists) {
    Write-Host "❌ File STILL exists after delete!" -ForegroundColor Red
} else {
    Write-Host "✅ File successfully deleted!" -ForegroundColor Green
}

# Check metadata was cleared
$docAfterDelete = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId"
if ($docAfterDelete.fileKey) {
    Write-Host "⚠️  FileKey still set (but file deleted from storage)" -ForegroundColor Yellow
} else {
    Write-Host "✅ Metadata cleared" -ForegroundColor Green
}
```

---

### STEP 9: Verify in MinIO Console

1. Refresh MinIO Console page
2. Check the bucket
3. File should be GONE

---

## Troubleshooting

### If Delete Fails

#### Check 1: Service Logs
Look in the terminal where `mvn spring-boot:run` is running for:
```
ERROR - Failed to delete file from MinIO: {filename} - Error: ...
```

#### Check 2: Bucket Mismatch
The file might be in a different bucket than configured:
```powershell
# Check config
Get-Content document-service\src\main\resources\application.properties | Select-String "minio.bucket"

# Should be: minio.bucket=plm-documents
```

If your files are in a bucket called `documents` or `task-files`, you need to either:
- Change the config to match, OR
- Manually move files in MinIO Console

#### Check 3: Credentials Wrong
```powershell
# Check credentials in application.properties
Get-Content document-service\src\main\resources\application.properties | Select-String "minio"
```

Should be:
```
minio.url=http://localhost:9000
minio.access-key=minio
minio.secret-key=password
minio.bucket=plm-documents
```

#### Check 4: MinIO Connection
```powershell
# Test MinIO health
curl http://localhost:9000/minio/health/live

# Test from document-service
curl http://localhost:8081/api/v1/documents/health/minio
```

Both should return successful status.

---

## Quick All-in-One Test Script

Save this as `test-all.ps1`:

```powershell
$docId = $null

try {
    # Upload
    "Test" | Out-File test.txt -Encoding UTF8
    $createBody = '{"title":"Test","creator":"User","stage":"CONCEPTUAL_DESIGN","description":"Test"}'
    $doc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents" -Method Post -Body $createBody -ContentType "application/json"
    $docId = $doc.id
    
    $form = @{file = Get-Item test.txt; user = "User"}
    Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/upload" -Method Post -Form $form | Out-Null
    
    Write-Host "✅ Upload: OK" -ForegroundColor Green
    
    # Check metadata
    $info = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId"
    if ($info.originalFilename) {
        Write-Host "✅ Metadata: OK ($($info.originalFilename))" -ForegroundColor Green
    } else {
        Write-Host "❌ Metadata: MISSING" -ForegroundColor Red
        exit
    }
    
    # Check exists
    $exists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"
    Write-Host "✅ File exists: $exists" -ForegroundColor Green
    
    # Delete
    $result = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file" -Method Delete
    Write-Host "✅ Delete: $result" -ForegroundColor Green
    
    # Verify deleted
    $stillExists = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"
    if ($stillExists) {
        Write-Host "❌ Still exists after delete!" -ForegroundColor Red
    } else {
        Write-Host "✅ Verified deleted" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item test.txt -ErrorAction SilentlyContinue
}
```

---

## Expected Results After All Steps

✅ Document created  
✅ File uploaded to MinIO  
✅ Metadata saved (originalFilename, contentType, etc.)  
✅ File visible in MinIO Console  
✅ File exists check returns true  
✅ File info endpoint returns data  
✅ Delete succeeds  
✅ File removed from MinIO Console  
✅ File exists check returns false  

---

## Still Having Issues?

1. Check document-service console logs
2. Check MinIO is accessible: http://localhost:9001
3. Verify bucket exists and has correct name
4. Verify credentials match
5. Make sure service was restarted AFTER code changes
6. Upload a NEW document (old ones won't work)



# Troubleshooting File Operations

## Issue: Delete and Metadata Not Working

### Root Cause
The file metadata (originalFilename, contentType, fileSize, etc.) wasn't being saved to the database during upload, so the delete and metadata check operations had nothing to work with.

### What Was Fixed
1. ✅ Added `attachFileWithMetadata()` method to save file metadata during upload
2. ✅ Updated upload endpoint to use the new method
3. ✅ File metadata now properly saved: originalFilename, contentType, fileSize, storageLocation, uploadedAt

---

## Testing the Fix

### 1. Restart Document Service
```powershell
# Stop existing service (Ctrl+C if running)
cd document-service
mvn clean spring-boot:run
```

### 2. Run Test Script
```powershell
.\test-file-operations.ps1
```

This will:
- Upload a test document
- Check file metadata
- Verify file exists
- Download the file
- Delete the file
- Verify deletion

---

## Manual Testing

### Test 1: Upload Document
```powershell
$file = Get-Item "test.pdf"
$form = @{
    file = $file
    title = "Test Document"
    description = "Testing"
    type = "SPECIFICATION"
    creator = "TestUser"
}
$response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/upload" -Method Post -Form $form
$docId = $response.id
```

### Test 2: Check Metadata
```powershell
# Get file metadata
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/info"

# Expected response:
{
  "fileKey": "xxx_test.pdf",
  "originalFilename": "test.pdf",
  "contentType": "application/pdf",
  "fileSize": 12345,
  "storageLocation": "MINIO",
  "uploadedAt": "2025-10-25T...",
  "exists": true,
  "actualFileSize": 12345
}
```

### Test 3: Check File Exists
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"

# Expected: true
```

### Test 4: Delete File
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file" -Method Delete

# Expected: "File deleted successfully"
```

### Test 5: Verify Deletion
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$docId/file/exists"

# Expected: false
```

---

## Common Issues

### Issue: Metadata returns null values
**Cause:** Document was uploaded before the fix  
**Solution:** Upload a new document after restarting the service

### Issue: File exists but metadata not showing
**Cause:** Old uploads didn't save metadata  
**Solution:** 
1. Check MinIO Console to see if file is there
2. Upload a new document
3. Old documents won't have metadata (expected)

### Issue: Delete returns 500 error
**Cause:** File doesn't exist in MinIO  
**Solution:**
1. Check if file actually exists: `GET /documents/{id}/file/exists`
2. Check MinIO Console
3. If file doesn't exist, the metadata might be stale

### Issue: Metadata says exists=false but file is in MinIO
**Cause:** Bucket name mismatch or connection issue  
**Solution:**
1. Check application.properties: `minio.bucket=plm-documents`
2. Check MinIO Console - verify bucket name
3. Check MinIO health: `GET /api/v1/documents/health/minio`

---

## Debugging Steps

### 1. Check MinIO Health
```powershell
curl http://localhost:9000/minio/health/live
curl http://localhost:8081/api/v1/documents/health/minio
```

### 2. Check Document Service Logs
Look for:
- "File uploaded to MinIO - Bucket: plm-documents, File: ..."
- "File deleted from MinIO - Bucket: plm-documents, File: ..."
- "File exists in MinIO - Bucket: plm-documents, File: ..."

### 3. Check Database
```sql
-- Check if metadata is saved
SELECT id, fileKey, originalFilename, contentType, fileSize, storageLocation 
FROM document 
WHERE id = 'your-document-id';
```

### 4. Check MinIO Console
1. Open http://localhost:9001
2. Login: minio / password
3. Navigate to "plm-documents" bucket
4. Verify files are there

---

## Expected Behavior

### After Upload:
✅ File stored in MinIO  
✅ Metadata saved to database  
✅ GET /file/info returns all metadata  
✅ GET /file/exists returns true  

### After Delete:
✅ File removed from MinIO  
✅ Metadata cleared in database  
✅ GET /file/info returns 404  
✅ GET /file/exists returns false  
✅ GET /download returns 404 or empty  

---

## Quick Fix Commands

### Restart Everything
```powershell
# Stop infrastructure
docker-compose -f infra/docker-compose-infrastructure.yaml down

# Start infrastructure
.\start-infrastructure.ps1

# Restart document service
cd document-service
mvn clean spring-boot:run
```

### Clear and Restart
```powershell
# Stop and remove volumes (WARNING: Deletes all data)
docker-compose -f infra/docker-compose-infrastructure.yaml down -v

# Start fresh
.\start-infrastructure.ps1

# Restart service
cd document-service
mvn clean spring-boot:run
```

---

## Files Modified in This Fix

1. `DocumentController.java`
   - Updated upload() to call attachFileWithMetadata()

2. `DocumentService.java`
   - Added attachFileWithMetadata() interface method

3. `DocumentServiceImpl.java`
   - Implemented attachFileWithMetadata() with metadata saving

---

## Testing Checklist

After restarting:
- [ ] MinIO is running and healthy
- [ ] Document service starts without errors
- [ ] Upload new document with file
- [ ] Check metadata endpoint returns data
- [ ] File exists endpoint returns true
- [ ] Download file works
- [ ] Delete file works
- [ ] File exists returns false after delete
- [ ] Metadata endpoint returns 404 after delete

---

## Contact

If issues persist:
1. Check document service logs for errors
2. Check MinIO container logs: `docker logs plm-minio`
3. Verify bucket exists: MinIO Console → Buckets
4. Test with minimal file (text file) first


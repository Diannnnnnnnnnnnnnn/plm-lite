# Delete Document Fix - Files Now Deleted from MinIO!

## The Problem You Found! 🎯

When deleting a document via the web UI:
- ✅ Document deleted from database
- ✅ History records deleted
- ❌ **FILE STILL IN MINIO!** ← The bug you reported

## Root Cause

The `deleteDocument()` method in `DocumentServiceImpl` was only deleting:
1. History records from database
2. Document record from database

But **NOT deleting the actual file** from MinIO storage!

## The Fix

### 1. Injected FileStorageGateway
Added `FileStorageGateway` to the constructor so we can call delete on files.

### 2. Updated deleteDocument() Method
Now it:
1. **Checks if document has a file** (fileKey exists)
2. **Deletes file from MinIO** using `fileStorageGateway.delete()`
3. Deletes history records
4. Deletes document from database

## Code Changes

**Before:**
```java
public void deleteDocument(String documentId) {
    Document document = docRepo.findById(documentId)...
    
    // Delete history
    historyRepo.deleteAll(historyList);
    
    // Delete document
    docRepo.delete(document);
}
```

**After:**
```java
public void deleteDocument(String documentId) {
    Document document = docRepo.findById(documentId)...
    
    // DELETE FILE FROM STORAGE FIRST! ← NEW
    if (document.getFileKey() != null) {
        fileStorageGateway.delete(document.getFileKey());
    }
    
    // Delete history
    historyRepo.deleteAll(historyList);
    
    // Delete document
    docRepo.delete(document);
}
```

## How to Test

### 1. Restart Service
```powershell
# Stop document-service (Ctrl+C)
cd document-service
mvn clean spring-boot:run
```

### 2. Upload a Document with File
Via your web UI, upload a new document with a file.

### 3. Check MinIO Console
1. Open http://localhost:9001
2. Login: minio / password
3. Go to "plm-documents" bucket
4. You should see your uploaded file

### 4. Delete the Document
Delete it via the web UI.

### 5. Verify File is Gone from MinIO
1. Refresh MinIO Console
2. The file should now be **GONE** from the bucket!

## Testing with Script

```powershell
# Upload document via UI first, then:

# Get the document ID
$docs = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents"
$doc = $docs[0]
Write-Host "Document: $($doc.id), File: $($doc.fileKey)"

# Check MinIO Console - file should be there

# Delete the document
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/$($doc.id)" -Method Delete

# Check MinIO Console again - file should be GONE!
```

## What This Fixes

✅ **Delete document** → File deleted from MinIO  
✅ **Delete document** → File deleted from local storage (if fallback)  
✅ **No orphaned files** in MinIO anymore  
✅ **Storage cleanup** works properly  

## Clean Up Existing Orphaned Files

If you have files already in MinIO from old deleted documents:

### Option 1: Manual Cleanup (via MinIO Console)
1. Open http://localhost:9001
2. Go to "plm-documents" bucket
3. Manually delete files that don't match any existing document

### Option 2: Clean All and Start Fresh
```powershell
# WARNING: This deletes ALL files in the bucket!
docker exec plm-minio mc rm --recursive --force plmminio/plm-documents/
```

## Important Notes

- This fix only affects **NEW deletions** after restart
- Files from old deletions are still in MinIO (orphaned)
- You can clean them up manually via MinIO Console
- From now on, all document deletions will also delete files!

## Restart and Test!

```powershell
cd document-service
mvn clean spring-boot:run
```

Then:
1. Upload a document with file
2. Check MinIO (file should be there)
3. Delete the document
4. Check MinIO (file should be GONE!)

**This was the real issue you reported - now fixed!** 🎉



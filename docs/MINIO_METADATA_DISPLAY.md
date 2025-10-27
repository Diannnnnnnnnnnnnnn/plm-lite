# MinIO Console Metadata Display

## The Issue

When viewing files in MinIO Console (http://localhost:9001), you only see the filename, but not the document metadata (original filename, content type, etc.).

## Why?

MinIO has **two types** of metadata:

1. **System Metadata** (automatic)
   - File size
   - Last modified
   - ETag (checksum)
   - Content-Type

2. **User Metadata** (custom - what we need to add!)
   - original-filename
   - content-type
   - file-size
   - upload-time
   - Any custom fields

Previously, we were only storing metadata in our **database**, not in MinIO itself.

## The Fix

Updated `saveFile()` in `MinIOFileStorageService` to attach user metadata to MinIO objects:

```java
Map<String, String> userMetadata = new HashMap<>();
userMetadata.put("original-filename", file.getOriginalFilename());
userMetadata.put("content-type", file.getContentType());
userMetadata.put("file-size", String.valueOf(file.getSize()));
userMetadata.put("upload-time", LocalDateTime.now().toString());

minioClient.putObject(
    PutObjectArgs.builder()
        .bucket(bucketName)
        .object(filename)
        .stream(file.getInputStream(), file.getSize(), -1)
        .contentType(file.getContentType())
        .userMetadata(userMetadata)  // ← This is new!
        .build()
);
```

## How to Apply

```powershell
# Restart document service
cd document-service
mvn clean spring-boot:run
```

## How to View Metadata in MinIO Console

### After uploading a NEW file (after restart):

1. Open MinIO Console: http://localhost:9001
2. Login: minio / password
3. Go to "Buckets" → "plm-documents"
4. Click on any file
5. Look for "Metadata" section

You should now see:
```
User Metadata:
  original-filename: document.pdf
  content-type: application/pdf
  file-size: 12345
  upload-time: 2025-10-25T...
```

## Note About Old Files

Files uploaded **before this fix** won't have user metadata in MinIO. Only **new uploads** after restart will have it.

To see metadata on old files:
- The metadata is still in the database
- You can see it via API: `GET /api/v1/documents/{id}`
- But not in MinIO Console (MinIO doesn't have it)

## Testing

### 1. Upload New Document
Via UI after restart

### 2. Check API Response
```powershell
$doc = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents/{id}"
Write-Host "API Metadata:"
Write-Host "  OriginalFilename: $($doc.originalFilename)"
Write-Host "  ContentType: $($doc.contentType)"
Write-Host "  FileSize: $($doc.fileSize)"
```

### 3. Check MinIO Console
1. Open http://localhost:9001
2. Navigate to plm-documents bucket
3. Click on the file
4. View "Metadata" section
5. Should see user metadata!

## What This Enables

✅ **See file metadata in MinIO Console**  
✅ **Independent storage** (metadata in both DB and MinIO)  
✅ **Better debugging** (can verify metadata in MinIO)  
✅ **MinIO-native metadata** (can query via MinIO API)  
✅ **Audit trail** (upload time stored in MinIO)  

## Restart and Test!

```powershell
cd document-service
mvn clean spring-boot:run
```

Then upload a new document and check MinIO Console!



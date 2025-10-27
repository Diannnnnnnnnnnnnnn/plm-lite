# MinIO Integration Plan for PLM-Lite

**Document Version:** 1.1 (Simplified - Document Service Only)  
**Date:** October 25, 2025  
**Status:** Planning Phase  
**MinIO Version:** Running on Docker (Port 9000)  
**Scope:** Document Service Only

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Architecture Overview](#architecture-overview)
4. [Integration Phases](#integration-phases)
5. [Implementation Priority](#implementation-priority)
6. [Quick Start Guide](#quick-start-guide)
7. [Testing Strategy](#testing-strategy)
8. [Configuration Reference](#configuration-reference)
9. [Troubleshooting](#troubleshooting)
10. [Future Enhancements](#future-enhancements)

---

## Executive Summary

This document outlines the plan for integrating MinIO object storage with the **Document Service** in the PLM-Lite microservices architecture. MinIO will serve as the primary file storage backend for document management only.

### Scope
**✅ IN SCOPE:**
- Document Service file storage
- Document file upload/download/management
- Document file versioning
- Document attachments and specifications

**❌ OUT OF SCOPE (Future Enhancement):**
- Task Service attachments
- BOM Service files
- Change Service documents
- Other microservice file storage

### Goals
- ✅ Centralize document file storage in MinIO
- ✅ Eliminate local file system dependencies for documents
- ✅ Enable scalable document storage
- ✅ Implement document file versioning
- ✅ Ensure secure, authenticated document access
- ✅ Keep existing fallback mechanism (MinIO → Local Storage)

### Current MinIO Setup
- **Host:** localhost
- **Port:** 9000 (API), 9001 (Console)
- **Status:** Running in Docker
- **SDK Version:** 8.5.9 (document-service)
- **Primary Service:** document-service only

---

## Current State Analysis

### ✅ What's Already Implemented

#### 1. **Dependencies Installed**
- `document-service`: MinIO Java SDK 8.5.9 ✅
- OkHttp for HTTP client ✅

#### 2. **Docker Setup**
- MinIO container running via docker-compose ✅
- Location: `file-storage-service/docker-compose.yaml`
- Ports: 9000 (API), 9001 (Console UI) ✅

#### 3. **Service Implementations**
- `MinIOFileStorageService` in document-service ✅
- `LocalFileStorageService` as fallback ✅
- `FileStorageGatewayFeign` with fallback logic ✅
- Basic upload/download functionality ✅
- Automatic fallback to local storage on MinIO failure ✅

#### 4. **Document Service Integration**
```java
// Already implemented in document-service
- MinIOFileStorageService.saveFile()
- MinIOFileStorageService.getFile()
- FileStorageGatewayFeign.upload() with fallback
- FileStorageGatewayFeign.download() with fallback
```

#### 5. **API Endpoints in Document Service**
```
POST /documents/{id}/upload      - Upload document with file
GET  /documents/{id}/download    - Download document file
```

### ⚠️ Issues & Inconsistencies

#### 1. **Configuration Mismatch**
Different credentials between services:
- `document-service`: `minio/minio123`
- Docker Compose: `minio/password`
- **Need to standardize:** Use `minio/minio_plm_password`

#### 2. **Bucket Name**
- `document-service`: Uses bucket `documents`
- **Should be:** `plm-documents` for consistency

#### 3. **Infrastructure Issues**
- Docker Compose file isolated in `file-storage-service/` folder
- Not integrated with main infrastructure stack (should be with Redis)
- No health checks or startup dependencies
- No automatic bucket creation
- Document service may start before MinIO is ready

#### 4. **Missing Features in Document Service**
- No file deletion capability (old versions not cleaned up)
- No file listing/browsing (can't see all files for a document)
- Limited metadata tracking (file info not stored in DB)
- No true file versioning (when document updated)
- No pre-signed URL support (all downloads go through service)
- No audit trail (who uploaded/downloaded what)

#### 5. **Security Concerns**
- No explicit authentication on document file operations
- No fine-grained authorization (who can access which document files)
- No file type validation (could upload any file type)
- No virus scanning
- No rate limiting on file uploads

#### 6. **Documentation & Monitoring**
- No specific MinIO setup guide for document-service
- No MinIO health monitoring
- No alerting if MinIO goes down
- No storage usage tracking

---

## Architecture Overview

### System Architecture (Simplified - Document Service Only)

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Port 8080)                  │
│                  Authentication & Routing                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ JWT Validated Requests
                     │
                     ▼
                ┌──────────────────┐
                │  Document        │
                │  Service (8081)  │
                │                  │
                │  - Document CRUD │
                │  - File Upload   │
                │  - File Download │
                └────────┬─────────┘
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
    ┌──────────────────┐  ┌──────────────────┐
    │ MinIO Storage    │  │ Local Storage    │
    │ (Primary)        │  │ (Fallback)       │
    │                  │  │                  │
    │ Port: 9000       │  │ ./temp-uploads/  │
    │ Bucket:          │  │                  │
    │ plm-documents    │  │                  │
    └──────────────────┘  └──────────────────┘
```

### Bucket Organization Strategy (Single Bucket)

```
MinIO Server
│
└── plm-documents/              # ALL Document Files
    ├── DOC-123_specification.pdf
    ├── DOC-123_drawing.dwg
    ├── DOC-456_technical_manual.docx
    ├── DOC-789_cad_model.step
    │
    └── versions/               # Optional: Version History
        ├── DOC-123_v1_specification.pdf
        ├── DOC-123_v2_specification.pdf
        └── DOC-456_v1_technical_manual.docx

Future Buckets (Out of Current Scope):
├── plm-tasks/          # Task attachments (future)
├── plm-bom/            # BOM files (future)
└── plm-change/         # Change documents (future)
```

### Data Flow

#### Upload Flow (Simplified)
```
1. User uploads document + file via Frontend
2. Request → API Gateway (JWT validation)
3. API Gateway → Document Service
4. Document Service → FileStorageGateway
5. FileStorageGateway → MinIOFileStorageService
6. MinIOFileStorageService → MinIO (if available)
   OR → LocalFileStorageService (if MinIO down)
7. MinIO returns object key
8. Document Service saves document metadata + file reference to H2 DB
9. Response back to user with document ID
```

#### Download Flow (Simplified)
```
1. User requests document download
2. Request → API Gateway (JWT validation)
3. API Gateway → Document Service
4. Document Service checks document exists in DB
5. Document Service → FileStorageGateway
6. FileStorageGateway → MinIOFileStorageService
7. MinIOFileStorageService → MinIO (retrieve file)
   OR → LocalFileStorageService (if not in MinIO)
8. Stream file back to user via Document Service
```

#### Fallback Mechanism (Already Implemented)
```
MinIO Available?
├── YES → Store/Retrieve from MinIO
│         └── Success → Return file
│         └── Failure → Try Local Storage
│
└── NO  → Store/Retrieve from Local Storage
          └── Success → Return file
          └── Failure → Return error
```

---

## Integration Phases

### Phase 1: Centralize & Standardize Configuration ⭐ HIGH PRIORITY

**Duration:** 2-3 days  
**Status:** Not Started

#### Objectives
- Consolidate MinIO infrastructure
- Standardize configuration across all services
- Implement consistent credential management

#### Tasks

##### Task 1.1: Infrastructure Consolidation
- [ ] Move `file-storage-service/docker-compose.yaml` to `infra/`
- [ ] Create unified `infra/docker-compose-infrastructure.yaml`
- [ ] Include both Redis and MinIO in single compose file
- [ ] Add health checks for MinIO

**File:** `infra/docker-compose-infrastructure.yaml`
```yaml
version: '3.8'

services:
  # Redis Service (existing)
  redis:
    image: redis:7.2-alpine
    container_name: plm-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --requirepass plm_redis_password
    volumes:
      - redis-data:/data
    networks:
      - plm-network
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

  # MinIO Service (NEW)
  minio:
    image: minio/minio:latest
    container_name: plm-minio
    ports:
      - "9000:9000"   # API
      - "9001:9001"   # Console UI
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio_plm_password
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    networks:
      - plm-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
    restart: unless-stopped

  # MinIO Client for bucket initialization
  minio-init:
    image: minio/mc:latest
    container_name: plm-minio-init
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: >
      /bin/sh -c "
      /usr/bin/mc alias set plmminio http://minio:9000 minio minio_plm_password;
      /usr/bin/mc mb plmminio/plm-documents --ignore-existing;
      echo 'Bucket plm-documents created successfully';
      exit 0;
      "
    networks:
      - plm-network

  # Redis Commander (existing)
  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: plm-redis-commander
    environment:
      - REDIS_HOSTS=local:plm-redis:6379:0:plm_redis_password
    ports:
      - "8085:8081"
    networks:
      - plm-network
    depends_on:
      - redis
    restart: unless-stopped

volumes:
  redis-data:
    driver: local
  minio-data:
    driver: local

networks:
  plm-network:
    driver: bridge
```

##### Task 1.2: Standardize Service Configurations

**Update:** `file-storage-service/src/main/resources/application.properties`
```properties
# MinIO Configuration (STANDARDIZED)
minio.url=http://localhost:9000
minio.access-key=minio
minio.secret-key=minio_plm_password
minio.bucket=plm-tasks

# Connection Pool Settings
minio.connection.timeout=10000
minio.read.timeout=30000
minio.write.timeout=30000
```

**Update:** `document-service/src/main/resources/application.properties`
```properties
# MinIO Configuration (STANDARDIZED)
minio.url=http://localhost:9000
minio.access-key=minio
minio.secret-key=minio_plm_password
minio.bucket=plm-documents

# Connection Settings
minio.connection.timeout=10000
minio.read.timeout=30000
minio.write.timeout=30000
```

**Update:** `task-service/src/main/resources/application.properties`
```properties
# MinIO Configuration (STANDARDIZED via file-storage-service)
file-storage.url=localhost:9900
```

##### Task 1.3: Environment Variables (Optional)
Create `.env` file for sensitive credentials:
```properties
MINIO_ROOT_USER=minio
MINIO_ROOT_PASSWORD=minio_plm_password
MINIO_URL=http://localhost:9000
```

#### Deliverables
- ✅ Unified docker-compose-infrastructure.yaml
- ✅ Standardized application.properties across all services
- ✅ Auto-bucket creation on startup
- ✅ Health checks configured

---

### Phase 2: Enhanced Document Service MinIO Implementation ⭐ HIGH PRIORITY

**Duration:** 2-3 days  
**Status:** Not Started

#### Objectives
- Improve error handling and resilience in document-service
- Add file management features to document-service
- Implement proper logging and monitoring
- Add file metadata tracking

#### Tasks

##### Task 2.1: (Optional) Create Common MinIO Utilities

**Note:** Since we're only focusing on document-service, this can be done directly in document-service or optionally in plm-common for future reuse.

**New File (Optional):** `plm-common/src/main/java/com/example/plm/common/minio/MinioUtils.java`
```java
package com.example.plm.common.minio;

import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(MinioUtils.class);
    
    /**
     * Ensure bucket exists, create if not
     */
    public static void ensureBucketExists(MinioClient client, String bucketName) 
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        boolean exists = client.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        
        if (!exists) {
            client.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            logger.info("Created MinIO bucket: {}", bucketName);
        }
    }
    
    /**
     * Check if MinIO connection is healthy
     */
    public static boolean isHealthy(MinioClient client) {
        try {
            client.listBuckets();
            return true;
        } catch (Exception e) {
            logger.error("MinIO health check failed", e);
            return false;
        }
    }
    
    /**
     * Upload with retry logic
     */
    public static void uploadWithRetry(MinioClient client, String bucket, 
                                      String objectName, InputStream stream, 
                                      long size, String contentType, 
                                      int maxRetries) throws Exception {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                client.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build()
                );
                logger.info("Successfully uploaded {} to bucket {}", objectName, bucket);
                return;
            } catch (Exception e) {
                attempts++;
                lastException = e;
                logger.warn("Upload attempt {} failed for {}: {}", 
                           attempts, objectName, e.getMessage());
                
                if (attempts < maxRetries) {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                }
            }
        }
        
        throw new Exception("Failed to upload after " + maxRetries + " attempts", lastException);
    }
}
```

##### Task 2.2: Enhanced MinIOFileStorageService in Document Service

**Update:** `document-service/src/main/java/com/example/document_service/service/impl/MinIOFileStorageService.java`

Add methods:
```java
// Delete file from MinIO
public void deleteFile(String filename) throws Exception;

// Check if file exists in MinIO
public boolean fileExists(String filename) throws Exception;

// Get file size
public long getFileSize(String filename) throws Exception;

// List all files for a document (by prefix)
public List<String> listFilesForDocument(String documentId) throws Exception;

// Generate presigned URL for direct download (optional)
public String generatePresignedUrl(String filename, int expirySeconds) throws Exception;
```

##### Task 2.3: File Metadata Tracking in Document Entity

**Update:** `document-service/src/main/java/com/example/document_service/model/Document.java`

Add file-related fields:
```java
@Entity
@Table(name = "documents")
public class Document {
    // ... existing fields ...
    
    // File storage fields (ADD THESE)
    private String storedFilename;      // MinIO object key (e.g., "DOC-123_file.pdf")
    private String originalFilename;    // Original filename from user
    private String contentType;         // MIME type
    private Long fileSize;              // File size in bytes
    private String storageLocation;     // "MINIO" or "LOCAL"
    
    private LocalDateTime fileUploadedAt;
    private String fileUploadedBy;      // User who uploaded
    
    private String fileChecksum;        // MD5 or SHA-256 (optional)
    private Integer fileVersion;        // For versioning (optional)
    
    // Getters and setters
}
```

**OR Create Separate Entity (Better for multiple files per document):**

**New Entity:** `document-service/src/main/java/com/example/document_service/model/DocumentFile.java`
```java
@Entity
@Table(name = "document_files")
public class DocumentFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;          // Link to parent document
    
    private String storedFilename;      // MinIO object key
    private String originalFilename;    // Original filename from user
    private String contentType;         // MIME type
    private Long fileSize;              // File size in bytes
    private String storageLocation;     // "MINIO" or "LOCAL"
    
    private LocalDateTime uploadedAt;
    private String uploadedBy;          // User ID
    
    private String checksum;            // MD5 or SHA-256
    private Integer version;            // For versioning
    
    private Boolean isActive;           // Current version indicator
    private Boolean deleted;            // Soft delete
    private LocalDateTime deletedAt;
    
    // Getters and setters
}
```

##### Task 2.4: Improved Error Handling

Create custom exceptions:
```java
public class FileStorageException extends RuntimeException
public class FileNotFoundException extends FileStorageException
public class FileUploadException extends FileStorageException
public class FileDownloadException extends FileStorageException
public class BucketNotFoundException extends FileStorageException
```

#### Deliverables
- ✅ Enhanced MinIOFileStorageService with full CRUD operations
- ✅ File metadata tracking in Document entity or DocumentFile entity
- ✅ Retry logic and error handling
- ✅ Better logging (who uploaded what, when)
- ✅ Optional: Presigned URL support

---

### Phase 3: Document Service API Enhancements ⭐ MEDIUM PRIORITY

**Duration:** 2-3 days  
**Status:** Not Started

#### Objectives
- Enhance document-service REST APIs for file management
- Add file versioning support
- Add file deletion and cleanup
- Improve error responses

#### Tasks

##### Task 3.1: Enhanced Document Controller

**Update:** `document-service/src/main/java/com/example/document_service/controller/DocumentController.java`

**New/Enhanced Endpoints:**
```java
// Existing (enhance these)
POST /documents/upload          - Upload document + file (ENHANCE with metadata)
GET  /documents/{id}/download   - Download document file (ALREADY EXISTS)

// New endpoints to ADD
DELETE /documents/{id}/file     - Delete file from document
GET  /documents/{id}/file/info  - Get file metadata
HEAD /documents/{id}/file       - Check if file exists

// Optional: Multiple files per document
GET    /documents/{id}/files           - List all files for document
POST   /documents/{id}/files/upload    - Upload additional file
GET    /documents/{id}/files/{fileId}  - Download specific file
DELETE /documents/{id}/files/{fileId}  - Delete specific file

// Optional: Version control
GET  /documents/{id}/versions          - List all versions
POST /documents/{id}/versions/create   - Create new version
GET  /documents/{id}/versions/{ver}    - Get specific version info
```

##### Task 3.2: Document Service Implementation Updates

**Update:** `document-service/src/main/java/com/example/document_service/service/impl/DocumentServiceImpl.java`

Add methods:
```java
// Delete file when document is deleted
public void deleteDocument(Long id) {
    Document doc = findById(id);
    // Delete from MinIO
    fileStorageGateway.delete(doc.getStoredFilename());
    // Delete from DB
    documentRepository.delete(doc);
}

// Update document file (new version)
public Document updateDocumentFile(Long id, MultipartFile newFile) {
    // Upload new file
    // Update document metadata
    // Optionally keep old version
}

// Get file metadata
public FileInfo getFileInfo(Long documentId) {
    // Return file metadata from DB
}
```

##### Task 3.3: File Deletion Support

**Update:** `MinIOFileStorageService` to add delete method:
```java
public void deleteFile(String filename) {
    try {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .build()
        );
        logger.info("File deleted from MinIO: {}", filename);
    } catch (Exception e) {
        logger.error("Failed to delete file from MinIO: {}", filename, e);
        throw new FileStorageException("Failed to delete file", e);
    }
}
```

**Update:** `FileStorageGateway` interface:
```java
public interface FileStorageGateway {
    String upload(String documentId, MultipartFile file);
    byte[] download(String fileKey);
    void delete(String fileKey);  // ADD THIS
}
```

##### Task 3.4: Improved Error Responses

**Update:** `GlobalExceptionHandler` in document-service:
```java
@ExceptionHandler(FileStorageException.class)
public ResponseEntity<ApiError> handleFileStorageException(FileStorageException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("FILE_STORAGE_ERROR", ex.getMessage()));
}

@ExceptionHandler(FileNotFoundException.class)
public ResponseEntity<ApiError> handleFileNotFoundException(FileNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError("FILE_NOT_FOUND", ex.getMessage()));
}
```

#### Deliverables
- ✅ Enhanced document REST APIs
- ✅ File deletion support
- ✅ File metadata endpoints
- ✅ Better error handling
- ✅ Optional: File versioning
- ✅ Optional: Multiple files per document

---

### Phase 4: Infrastructure & DevOps ⭐ HIGH PRIORITY

**Duration:** 2-3 days  
**Status:** Not Started

#### Tasks

##### Task 4.1: Startup Scripts

**New File:** `start-storage-services.ps1`
```powershell
# Start MinIO and File Storage Service
Write-Host "Starting PLM Storage Infrastructure..." -ForegroundColor Green

# Start Docker containers
Write-Host "`nStarting MinIO..." -ForegroundColor Cyan
docker-compose -f infra/docker-compose-infrastructure.yaml up -d minio minio-init

# Wait for MinIO to be healthy
Write-Host "`nWaiting for MinIO to be ready..." -ForegroundColor Cyan
$maxWait = 30
$waited = 0
while ($waited -lt $maxWait) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9000/minio/health/live" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "MinIO is ready!" -ForegroundColor Green
            break
        }
    } catch {
        Start-Sleep -Seconds 2
        $waited += 2
    }
}

# Start file-storage-service
Write-Host "`nStarting File Storage Service..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd file-storage-service; mvn spring-boot:run"

Write-Host "`n✅ Storage services started successfully!" -ForegroundColor Green
Write-Host "MinIO Console: http://localhost:9001 (minio/minio_plm_password)" -ForegroundColor Yellow
Write-Host "File Storage API: http://localhost:9900" -ForegroundColor Yellow
```

**Update:** `start-all-services.ps1`
```powershell
# Add MinIO startup before other services
Write-Host "Starting Infrastructure (Redis, MinIO)..." -ForegroundColor Green
docker-compose -f infra/docker-compose-infrastructure.yaml up -d

# Wait for services...
Start-Sleep -Seconds 10

# Continue with existing service startup...
```

##### Task 4.2: Health Check Endpoints

**File:** `file-storage-service/src/main/java/com/example/file_storage_service/controller/HealthController.java`
```java
@RestController
@RequestMapping("/health")
public class HealthController {
    
    private final MinioClient minioClient;
    
    @GetMapping
    public ResponseEntity<HealthStatus> health() {
        try {
            // Test MinIO connection
            minioClient.listBuckets();
            
            return ResponseEntity.ok(new HealthStatus(
                "UP",
                "MinIO connection healthy",
                System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthStatus(
                    "DOWN",
                    "MinIO connection failed: " + e.getMessage(),
                    System.currentTimeMillis()
                ));
        }
    }
}
```

##### Task 4.3: Docker Service Dependencies

Update service startup to wait for MinIO:
```yaml
# In each service's configuration
depends_on:
  minio:
    condition: service_healthy
```

##### Task 4.4: Monitoring & Logging

Add structured logging:
```java
// Use SLF4J with Logback
logger.info("File uploaded - Bucket: {}, Key: {}, Size: {}, User: {}", 
           bucket, key, size, userId);

logger.error("File upload failed - Key: {}, Error: {}", key, e.getMessage(), e);
```

#### Deliverables
- ✅ Startup scripts for storage services
- ✅ Health check endpoints
- ✅ Service dependency management
- ✅ Structured logging
- ✅ Updated documentation

---

### Phase 5: API Gateway & Security ⭐ MEDIUM PRIORITY

**Duration:** 3-4 days  
**Status:** Not Started

#### Tasks

##### Task 5.1: API Gateway Routes

**Update:** `api-gateway/src/main/java/config/GatewayConfig.java`
```java
// Add file storage routes
.route("file-storage-service", r -> r
    .path("/api/files/**")
    .filters(f -> f
        .rewritePath("/api/files/(?<segment>.*)", "/files/${segment}")
        .filter(jwtAuthFilter)
    )
    .uri("lb://file-storage-service")
)
```

##### Task 5.2: File Access Control

**New:** `file-storage-service/src/main/java/security/FileAccessControl.java`
```java
@Component
public class FileAccessControl {
    
    /**
     * Check if user has permission to access file
     */
    public boolean canAccessFile(String userId, String fileKey) {
        // Check file metadata
        // Verify ownership or shared access
        // Check role permissions
        return hasPermission(userId, fileKey, Permission.READ);
    }
    
    /**
     * Check if user can delete file
     */
    public boolean canDeleteFile(String userId, String fileKey) {
        return hasPermission(userId, fileKey, Permission.DELETE);
    }
}
```

##### Task 5.3: JWT Token Validation

```java
@Component
public class JwtFileFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Extract JWT from Authorization header
        // Validate token
        // Extract user ID and roles
        // Add to request context
        // Continue chain
    }
}
```

##### Task 5.4: Rate Limiting

```java
// Add rate limiting for file uploads
@RateLimiter(name = "fileUpload", fallbackMethod = "uploadFallback")
public ResponseEntity<String> upload(MultipartFile file) {
    // Upload logic
}
```

##### Task 5.5: File Validation

```java
@Component
public class FileValidator {
    
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/jpeg",
        "image/png"
    );
    
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    
    public void validate(MultipartFile file) throws ValidationException {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File too large");
        }
        
        // Check content type
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ValidationException("File type not allowed");
        }
        
        // Check file extension
        // Scan for malware (optional)
    }
}
```

#### Deliverables
- ✅ API Gateway routing configured
- ✅ JWT authentication on file endpoints
- ✅ File access control
- ✅ Rate limiting
- ✅ File validation

---

### Phase 6: Frontend Integration ⭐ MEDIUM PRIORITY

**Duration:** 3-4 days  
**Status:** Not Started

#### Tasks

##### Task 6.1: File Upload Component

**New:** `frontend/src/components/FileUpload.js`
```javascript
import React, { useState } from 'react';
import axios from 'axios';

const FileUpload = ({ documentId, onUploadComplete }) => {
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [progress, setProgress] = useState(0);

    const handleUpload = async () => {
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        setUploading(true);

        try {
            const response = await axios.post(
                `/api/documents/${documentId}/files/upload`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    },
                    onUploadProgress: (progressEvent) => {
                        const percentCompleted = Math.round(
                            (progressEvent.loaded * 100) / progressEvent.total
                        );
                        setProgress(percentCompleted);
                    }
                }
            );

            onUploadComplete(response.data);
            setFile(null);
            setProgress(0);
        } catch (error) {
            console.error('Upload failed:', error);
            alert('Upload failed: ' + error.message);
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="file-upload">
            <input
                type="file"
                onChange={(e) => setFile(e.target.files[0])}
                disabled={uploading}
            />
            <button onClick={handleUpload} disabled={!file || uploading}>
                {uploading ? `Uploading... ${progress}%` : 'Upload'}
            </button>
            {uploading && (
                <div className="progress-bar">
                    <div
                        className="progress-fill"
                        style={{ width: `${progress}%` }}
                    />
                </div>
            )}
        </div>
    );
};

export default FileUpload;
```

##### Task 6.2: File List Component

```javascript
const FileList = ({ documentId }) => {
    const [files, setFiles] = useState([]);

    useEffect(() => {
        loadFiles();
    }, [documentId]);

    const loadFiles = async () => {
        const response = await axios.get(`/api/documents/${documentId}/files`);
        setFiles(response.data);
    };

    const handleDownload = async (fileId, filename) => {
        const response = await axios.get(
            `/api/documents/${documentId}/files/${fileId}`,
            { responseType: 'blob' }
        );
        
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        link.remove();
    };

    return (
        <div className="file-list">
            {files.map(file => (
                <div key={file.id} className="file-item">
                    <span>{file.filename}</span>
                    <span>{formatFileSize(file.size)}</span>
                    <button onClick={() => handleDownload(file.id, file.filename)}>
                        Download
                    </button>
                </div>
            ))}
        </div>
    );
};
```

##### Task 6.3: Drag & Drop Support

```javascript
const DragDropUpload = ({ onFilesDropped }) => {
    const [dragOver, setDragOver] = useState(false);

    const handleDrop = (e) => {
        e.preventDefault();
        setDragOver(false);
        
        const files = Array.from(e.dataTransfer.files);
        onFilesDropped(files);
    };

    return (
        <div
            className={`drop-zone ${dragOver ? 'drag-over' : ''}`}
            onDrop={handleDrop}
            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
            onDragLeave={() => setDragOver(false)}
        >
            Drop files here or click to upload
        </div>
    );
};
```

##### Task 6.4: File Preview

```javascript
const FilePreview = ({ fileUrl, fileType }) => {
    if (fileType.startsWith('image/')) {
        return <img src={fileUrl} alt="Preview" />;
    } else if (fileType === 'application/pdf') {
        return <embed src={fileUrl} type="application/pdf" />;
    } else {
        return <div>Preview not available for this file type</div>;
    }
};
```

#### Deliverables
- ✅ File upload component with progress
- ✅ File list/management component
- ✅ Drag & drop support
- ✅ File preview for images/PDFs
- ✅ Download functionality

---

### Phase 7: Advanced Features ⭐ LOW PRIORITY

**Duration:** 5-7 days  
**Status:** Future Enhancement

#### Features

##### 7.1 File Versioning

```java
public class FileVersionService {
    
    /**
     * Create new version of file
     */
    public FileMetadata createVersion(String originalKey, MultipartFile newFile) {
        // Get current version number
        // Upload new file with version suffix
        // Update metadata
        // Return new metadata
    }
    
    /**
     * List all versions of file
     */
    public List<FileMetadata> listVersions(String fileKey) {
        // Query metadata repository
        // Return sorted by version number
    }
    
    /**
     * Rollback to specific version
     */
    public void rollbackToVersion(String fileKey, int version) {
        // Copy versioned file to main location
        // Update metadata
    }
}
```

##### 7.2 Lifecycle Management

```java
public class FileLifecycleService {
    
    /**
     * Archive old files to cheaper storage tier
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void archiveOldFiles() {
        // Find files older than retention period
        // Move to archive bucket
        // Update metadata
    }
    
    /**
     * Delete expired files
     */
    @Scheduled(cron = "0 0 3 * * ?") // 3 AM daily
    public void cleanupExpiredFiles() {
        // Find files marked for deletion
        // Delete from MinIO
        // Delete metadata
    }
}
```

##### 7.3 Storage Analytics

```java
@RestController
@RequestMapping("/api/files/analytics")
public class FileAnalyticsController {
    
    @GetMapping("/usage")
    public StorageUsageReport getStorageUsage() {
        // Total storage used
        // Usage by bucket
        // Usage by user
        // Usage trends
    }
    
    @GetMapping("/popular")
    public List<FileStats> getPopularFiles() {
        // Most downloaded files
        // Most accessed files
        // Recent uploads
    }
}
```

##### 7.4 Distributed Storage (Future)

- Multiple MinIO instances
- Replication across data centers
- Automatic failover
- Load balancing

#### Deliverables
- ✅ File versioning system
- ✅ Lifecycle management
- ✅ Storage analytics dashboard
- ✅ Backup and archival policies

---

## Implementation Priority

### Timeline Overview (Document Service Only)

```
Week 1: Foundation & Infrastructure
├── Phase 1: Standardize Configuration (2-3 days)
└── Phase 4: Infrastructure Setup (2-3 days)

Week 2: Document Service Enhancement
├── Phase 2: Enhanced MinIO Implementation (2-3 days)
└── Phase 3: API Enhancements (2-3 days)

Week 3: Security & Frontend (Optional)
├── Phase 5: Security & Gateway (2-3 days)
└── Phase 6: Frontend Enhancements (2-3 days)

Total: 12-18 days for complete implementation
```

### Critical Path

```
1. Infrastructure Setup (Phase 1 + 4) ⭐ MUST DO FIRST
   └── Standardize config, integrate with infra stack
   └── Estimated: 3-4 days
   
2. Enhanced Document Service (Phase 2) ⭐ HIGH VALUE
   └── Better file management, metadata tracking
   └── Estimated: 2-3 days
   
3. API Enhancements (Phase 3) 🔵 MEDIUM VALUE
   └── File deletion, versioning, better endpoints
   └── Estimated: 2-3 days
   
4. Security (Phase 5) 🔵 MEDIUM VALUE
   └── JWT validation, file access control
   └── Estimated: 2-3 days
   
5. Frontend (Phase 6) ⚪ NICE TO HAVE
   └── Better UX for file uploads
   └── Estimated: 2-3 days
   
6. Advanced Features (Phase 7) ⚪ FUTURE
   └── Versioning, lifecycle, analytics
   └── Estimated: As needed
```

### Minimum Viable Product (MVP)

**To get MinIO working properly for documents:**
1. ✅ Phase 1: Standardized configuration (CRITICAL)
2. ✅ Phase 4: Infrastructure integration (CRITICAL)
3. ✅ Phase 2: Enhanced file management (HIGH)

**Estimated Time:** 6-8 days for MVP  
**Current Status:** MinIO already partially working, just needs refinement

### Recommended Phases (If Limited Time)

**Option A: Quick Fix (2-3 days)**
- Phase 1 only: Standardize config and infrastructure
- Result: MinIO runs reliably with proper setup

**Option B: Solid Foundation (1 week)**
- Phase 1 + Phase 4: Complete infrastructure
- Result: Production-ready MinIO setup

**Option C: Complete Solution (2 weeks)**
- Phase 1 + Phase 4 + Phase 2 + Phase 3
- Result: Fully featured document file management

**Option D: Production Ready (3 weeks)**
- All phases except Phase 7
- Result: Secure, user-friendly, production-ready system

---

## Quick Start Guide

### Prerequisites

- ✅ MinIO running on Docker (port 9000)
- ✅ Java 17+ installed
- ✅ Maven installed
- ✅ Docker and Docker Compose installed

### Step-by-Step Setup

#### Step 1: Start Infrastructure

```powershell
# Start MinIO and Redis
cd infra
docker-compose -f docker-compose-infrastructure.yaml up -d

# Verify MinIO is running
curl http://localhost:9000/minio/health/live

# Access MinIO Console
# Open browser: http://localhost:9001
# Login: minio / minio_plm_password
```

#### Step 2: Verify Bucket Created

```powershell
# Check docker logs for minio-init container
docker logs plm-minio-init

# Should see:
# Bucket plm-documents created successfully
```

#### Step 3: Start Document Service

```powershell
cd document-service
mvn clean install
mvn spring-boot:run
```

#### Step 4: Test Document Upload

```powershell
# Upload test document with file
curl -X POST http://localhost:8081/documents/upload `
  -F "file=@test.pdf" `
  -F "title=Test Document" `
  -F "description=Testing MinIO integration" `
  -F "type=SPECIFICATION"

# Should return: Document created with ID
```

#### Step 5: Verify in MinIO Console

```
1. Open http://localhost:9001
2. Login with minio/minio_plm_password
3. Navigate to "Buckets" → "plm-documents"
4. Verify your document file is there (e.g., "1_test.pdf")
```

#### Step 6: Test Download

```powershell
# Download document file (replace {id} with actual document ID)
curl http://localhost:8081/documents/{id}/download --output downloaded.pdf

# Verify content matches
```

#### Step 7: Check Fallback Mechanism

```powershell
# Stop MinIO
docker stop plm-minio

# Try uploading another document - should fall back to local storage
curl -X POST http://localhost:8081/documents/upload `
  -F "file=@test2.pdf" `
  -F "title=Test Document 2" `
  -F "description=Testing fallback" `
  -F "type=SPECIFICATION"

# Check ./temp-uploads/ directory for file

# Restart MinIO
docker start plm-minio
```

### Verification Checklist

- [ ] MinIO container running
- [ ] MinIO health check passing
- [ ] Bucket `plm-documents` automatically created
- [ ] Document Service started successfully
- [ ] Can upload document with file via API
- [ ] Can download document file via API
- [ ] File visible in MinIO console under `plm-documents` bucket
- [ ] Fallback to local storage works when MinIO down
- [ ] File metadata tracked in H2 database

---

## Configuration Reference

### MinIO Server Configuration

```properties
# Connection
minio.url=http://localhost:9000
minio.access-key=minio
minio.secret-key=minio_plm_password

# Buckets
minio.bucket.documents=plm-documents
minio.bucket.tasks=plm-tasks
minio.bucket.bom=plm-bom
minio.bucket.change=plm-change
minio.bucket.temp=plm-temp

# Connection Pool
minio.connection.timeout=10000
minio.read.timeout=30000
minio.write.timeout=30000
minio.max.connections=50

# Retry Configuration
minio.retry.maxAttempts=3
minio.retry.backoff=1000

# File Upload Limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Docker Compose Environment Variables

```yaml
environment:
  MINIO_ROOT_USER: minio
  MINIO_ROOT_PASSWORD: minio_plm_password
  MINIO_REGION_NAME: us-east-1
  MINIO_BROWSER: "on"
  MINIO_DOMAIN: localhost
```

### Security Configuration

```properties
# JWT Settings (in API Gateway)
jwt.secret=your-secret-key
jwt.expiration=86400000

# CORS Settings
cors.allowed-origins=http://localhost:3000
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
```

---

## Testing Strategy

### Unit Tests

```java
@SpringBootTest
public class FileStorageServiceTest {
    
    @Autowired
    private FileStorageService fileService;
    
    @Test
    public void testUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Test content".getBytes()
        );
        
        fileService.uploadFile(file, "test.txt");
        
        // Verify upload
        assertTrue(fileService.fileExists("test.txt"));
    }
    
    @Test
    public void testDownload() throws Exception {
        // Upload first
        // Then download and verify content
    }
    
    @Test
    public void testDelete() throws Exception {
        // Upload, delete, verify deleted
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FileControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testEndToEndUploadDownload() {
        // Upload via REST endpoint
        // Download via REST endpoint
        // Verify content matches
    }
}
```

### Performance Tests

```java
@Test
public void testConcurrentUploads() throws InterruptedException {
    int numThreads = 10;
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    
    for (int i = 0; i < numThreads; i++) {
        final int index = i;
        executor.submit(() -> {
            try {
                fileService.uploadFile(createTestFile(), "test_" + index + ".txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    
    // Verify all files uploaded successfully
}
```

### Test Checklist

**Functional Tests:**
- [ ] Upload single file
- [ ] Download file
- [ ] Delete file
- [ ] List files
- [ ] Get file metadata
- [ ] Handle duplicate filenames
- [ ] Handle special characters in filename
- [ ] Handle large files (>10MB)

**Error Handling:**
- [ ] Upload with MinIO down (fallback to local)
- [ ] Download non-existent file
- [ ] Upload with invalid credentials
- [ ] Exceed file size limit
- [ ] Invalid file type

**Security Tests:**
- [ ] Upload without authentication
- [ ] Access file without permission
- [ ] JWT token validation
- [ ] Rate limiting enforcement

**Performance Tests:**
- [ ] Concurrent uploads (10+ simultaneous)
- [ ] Large file upload (50MB)
- [ ] Download speed test
- [ ] Memory usage under load

---

## Troubleshooting

### Common Issues

#### Issue 1: MinIO Connection Refused

**Symptoms:**
```
Connection refused: localhost/127.0.0.1:9000
```

**Solutions:**
1. Check MinIO is running: `docker ps | grep minio`
2. Check port 9000 is accessible: `curl http://localhost:9000/minio/health/live`
3. Verify firewall not blocking port 9000
4. Check Docker network configuration

#### Issue 2: Bucket Not Found

**Symptoms:**
```
The specified bucket does not exist
```

**Solutions:**
1. Check minio-init container ran successfully: `docker logs plm-minio-init`
2. Manually create bucket via MinIO Console (http://localhost:9001)
3. Or use MinIO client:
   ```bash
   docker exec -it plm-minio mc mb /data/plm-documents
   ```

#### Issue 3: Access Denied

**Symptoms:**
```
Access Denied: The access key ID you provided does not exist
```

**Solutions:**
1. Verify credentials match in:
   - Docker Compose (MINIO_ROOT_USER/PASSWORD)
   - application.properties (minio.access-key/secret-key)
2. Restart MinIO container after changing credentials
3. Check for typos in credentials

#### Issue 4: File Upload Fails

**Symptoms:**
```
IOException: Broken pipe
or
MinioException: Unable to parse XML
```

**Solutions:**
1. Check file size under limit (50MB default)
2. Verify multipart configuration in application.properties
3. Check MinIO disk space: `docker exec plm-minio df -h /data`
4. Review MinIO container logs: `docker logs plm-minio`

#### Issue 5: Slow Upload/Download

**Solutions:**
1. Check network connectivity
2. Increase connection pool size in properties
3. Use presigned URLs for direct client-to-MinIO transfer
4. Check MinIO resource limits in Docker

#### Issue 6: Service Starts Before MinIO Ready

**Solutions:**
1. Add health check dependencies in docker-compose
2. Implement retry logic in application
3. Add startup delay:
   ```yaml
   depends_on:
     minio:
       condition: service_healthy
   ```

### Debug Commands

```powershell
# Check MinIO health
curl http://localhost:9000/minio/health/live

# List buckets via MinIO client
docker exec -it plm-minio mc ls /data/

# List files in bucket
docker exec -it plm-minio mc ls /data/plm-documents/

# Check MinIO logs
docker logs plm-minio --tail 100

# Check disk usage
docker exec plm-minio df -h /data

# Test MinIO credentials
docker exec plm-minio mc alias set testminio http://localhost:9000 minio minio_plm_password
```

### Logging Configuration

```properties
# Enable debug logging for MinIO
logging.level.io.minio=DEBUG
logging.level.com.example.file_storage_service=DEBUG

# Log file configuration
logging.file.name=logs/file-storage.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

## Future Enhancements

### Short Term (1-3 months)

1. **File Compression**
   - Automatic compression of large files
   - Transparent decompression on download

2. **Thumbnail Generation**
   - Auto-generate thumbnails for images
   - PDF first-page preview

3. **File Sharing**
   - Generate public/private share links
   - Time-limited access tokens
   - Share via email

4. **Batch Operations**
   - Bulk upload
   - Bulk download (as ZIP)
   - Bulk delete

### Medium Term (3-6 months)

1. **Advanced Search**
   - Full-text search in documents (PDF, DOC)
   - Search by metadata
   - Tag-based organization

2. **CDN Integration**
   - Use CDN for faster file delivery
   - Geographic distribution

3. **Backup & Disaster Recovery**
   - Automated backups to S3
   - Point-in-time recovery
   - Cross-region replication

4. **Audit & Compliance**
   - Complete audit trail
   - Compliance reports
   - Data retention policies

### Long Term (6+ months)

1. **AI/ML Integration**
   - Automatic document classification
   - Content analysis
   - Duplicate detection

2. **Multi-tenancy**
   - Separate storage per organization
   - Quota management per tenant

3. **Hybrid Cloud Storage**
   - Store frequently accessed files in MinIO
   - Archive cold data to cloud (S3, Azure)
   - Automatic tiering based on access patterns

4. **Advanced Security**
   - File encryption at rest
   - Encryption key management
   - Zero-knowledge encryption
   - DLP (Data Loss Prevention)

---

## Appendix

### A. API Reference (Document Service Only)

#### Document Service API

**Base URL:** `http://localhost:8081/documents`

**Current Endpoints:**
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/upload` | Upload document + file | ✅ Existing |
| GET | `/{id}/download` | Download document file | ✅ Existing |
| GET | `/{id}` | Get document metadata | ✅ Existing |
| PUT | `/{id}` | Update document | ✅ Existing |
| DELETE | `/{id}` | Delete document | ✅ Existing |

**Proposed New Endpoints (Phase 3):**
| Method | Endpoint | Description | Priority |
|--------|----------|-------------|----------|
| DELETE | `/{id}/file` | Delete file from document | High |
| GET | `/{id}/file/info` | Get file metadata | Medium |
| HEAD | `/{id}/file` | Check if file exists | Low |
| GET | `/{id}/files` | List all files (multi-file support) | Optional |
| POST | `/{id}/files/upload` | Upload additional file | Optional |
| GET | `/{id}/versions` | List document versions | Optional |

### B. Bucket Naming Convention

```
plm-documents          # Production documents (CURRENT SCOPE)
```

Future buckets (when other services integrate):
```
plm-documents-dev      # Development environment
plm-documents-test     # Testing environment
plm-tasks              # Task attachments (future)
plm-bom                # BOM files (future)
plm-change             # Change documents (future)
```

### C. File Naming Convention

```
{entityId}_{timestamp}_{originalFilename}
```

Examples:
- `DOC-123_20251025_specification.pdf`
- `TASK-456_20251025_screenshot.png`
- `BOM-789_20251025_cad_model.step`

### D. Useful MinIO Commands

```bash
# MinIO Client (mc) commands

# Configure alias
mc alias set plmminio http://localhost:9000 minio minio_plm_password

# List buckets
mc ls plmminio

# List files in bucket
mc ls plmminio/plm-documents

# Copy file to bucket
mc cp myfile.pdf plmminio/plm-documents/

# Copy file from bucket
mc cp plmminio/plm-documents/myfile.pdf ./

# Remove file
mc rm plmminio/plm-documents/myfile.pdf

# Get bucket stats
mc du plmminio/plm-documents

# Set bucket policy (public read)
mc anonymous set download plmminio/plm-documents

# Get bucket policy
mc anonymous get plmminio/plm-documents
```

### E. Performance Tuning

**MinIO Server:**
```yaml
# docker-compose.yaml
environment:
  MINIO_API_REQUESTS_MAX: 10000
  MINIO_API_REQUESTS_DEADLINE: 10s
```

**Java Client:**
```properties
# Connection pooling
minio.max.connections=100
minio.connection.timeout=10000
minio.socket.timeout=30000

# HTTP client tuning
minio.http.client.max.idle.connections=50
minio.http.client.keep.alive.duration=300
```

### F. Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for sensitive config
3. **Implement JWT authentication** on all file endpoints
4. **Validate file types** before upload
5. **Scan files** for malware (ClamAV integration)
6. **Use HTTPS** in production
7. **Implement rate limiting** to prevent abuse
8. **Enable audit logging** for compliance
9. **Regular security audits** of MinIO configuration
10. **Backup encryption keys** securely

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-10-25 | AI Assistant | Initial comprehensive plan (all services) |
| 1.1 | 2025-10-25 | AI Assistant | Simplified scope to document-service only |

---

## Related Documentation

- [REDIS_PRIORITY1_SUCCESS.md](./REDIS_PRIORITY1_SUCCESS.md) - Redis Integration Guide
- [REDIS_INTEGRATION_GUIDE.md](./docs/REDIS_INTEGRATION_GUIDE.md) - Detailed Redis Setup
- [START_TESTING_HERE.md](./START_TESTING_HERE.md) - Testing Procedures
- [README-STARTUP.md](./README-STARTUP.md) - System Startup Guide

---

**For questions or issues, please refer to the Troubleshooting section or check the project documentation.**

**Next Steps:** Review this plan and let's start with Phase 1 & 4 (Foundation & Infrastructure)!


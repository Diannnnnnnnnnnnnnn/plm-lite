# PLM-lite Document Data Schema Documentation

## Table of Contents
1. [Overview](#overview)
2. [Database Architecture](#database-architecture)
3. [Entity Models](#entity-models)
4. [Snapshot Versioning System](#snapshot-versioning-system)
5. [Version Increment Rules](#version-increment-rules)
6. [API Response Format](#api-response-format)
7. [Database Relationships](#database-relationships)
8. [Query Methods](#query-methods)
9. [File Storage](#file-storage)
10. [Examples](#examples)

---

## Overview

The PLM-lite document management system uses a **snapshot versioning architecture** where each edit or release creates a new immutable document record. This ensures complete version history with no data loss, allowing users to access any historical version of a document.

### Key Features
- ✅ **Complete Version History** - Every edit creates a new snapshot
- ✅ **Immutable Versions** - Historical versions never change
- ✅ **Fast Queries** - Active documents indexed separately
- ✅ **File Attachments** - Each version can have its own file
- ✅ **Audit Trail** - Full history of all changes tracked

---

## Database Architecture

The system uses **3 main tables**:

1. **`document_master`** - Document lineage (one per logical document)
2. **`document`** - Version snapshots (many per master)
3. **`document_history`** - Action audit trail (many per document)

---

## Entity Models

### 1. DocumentMaster Entity

**Table:** `document_master`

Represents the logical document that groups all versions together.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | String (UUID) | PRIMARY KEY | Master document identifier |
| `title` | String | - | Original document title |
| `creator` | String | - | User who created the first version |
| `category` | String | - | Document category/classification |
| `createTime` | LocalDateTime | Default: `now()` | When the master was first created |

**Purpose:** All versions/revisions of a document share the same master. This provides a stable identifier for the entire document lifecycle.

**Example:**
```json
{
  "id": "master-001",
  "title": "Product Specification",
  "creator": "vivi",
  "category": "Specifications",
  "createTime": "2025-10-15T10:00:00"
}
```

---

### 2. Document Entity (Main)

**Table:** `document`

Each row represents one immutable version/revision snapshot.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | String (UUID) | PRIMARY KEY | Unique identifier for this specific version |
| `master_id` | String | FOREIGN KEY, NOT NULL | Links to document_master.id |
| `title` | String | - | Document title (can change across versions) |
| `description` | String | - | Detailed description |
| `creator` | String | - | User who created **this version** |
| `createTime` | LocalDateTime | Default: `now()` | When **this version** was created |
| `version` | int | - | Minor version number (0, 1, 2, 3...) |
| `revision` | int | - | Major revision number (0, 1, 2, 3...) |
| `status` | Enum (String) | - | Lifecycle status (see Status enum) |
| `stage` | Enum (String) | - | Engineering stage (see Stage enum) |
| `fileKey` | String | Nullable | MinIO storage key for attached file |
| `bomId` | String | Nullable | Related Bill of Materials ID |
| `isActive` | boolean | NOT NULL, Default: `true` | TRUE = current version, FALSE = archived |

#### Computed Fields (Transient - Not Stored)

| Field | Type | Calculation | Example |
|-------|------|-------------|---------|
| `fullVersion` | String | `"v" + revision + "." + version` | "v0.1", "v1.0", "v2.3" |

**Key Concepts:**

- **`isActive` flag**: Only ONE version per master should have `isActive=true` at any time
- **Immutability**: Once created, versions are never modified (except `isActive` flag)
- **Versioning**: 
  - `version` increments on edits (v0.1 → v0.2 → v0.3)
  - `revision` increments on release (v0.3 → v1.0), `version` resets to 0

**Example:**
```json
{
  "id": "doc-abc-123",
  "master": { "id": "master-001" },
  "title": "Product Specification",
  "description": "Updated requirements for Q1",
  "creator": "vivi",
  "createTime": "2025-10-18T15:30:00",
  "version": 2,
  "revision": 0,
  "status": "IN_WORK",
  "stage": "DETAILED_DESIGN",
  "fileKey": "documents/2025/doc-abc-123_spec.pdf",
  "bomId": "bom-456",
  "isActive": true
}
```

---

### 3. DocumentHistory Entity

**Table:** `document_history`

Tracks all actions and changes on documents for audit purposes.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PRIMARY KEY (auto-increment) | Unique history record ID |
| `documentId` | String | - | Which document version this relates to |
| `action` | String | - | Action type (CREATED, UPDATED, RELEASED, etc.) |
| `oldValue` | String | - | Previous value before change |
| `newValue` | String | - | New value after change |
| `created_by` (user) | String | - | User who performed the action |
| `comment` | String | - | Optional comment/reason |
| `timestamp` | LocalDateTime | Default: `now()` | When the action occurred |

**Common Actions:**
- `CREATED` - New document/version created
- `UPDATED` - Document metadata updated
- `RELEASED` - Document released (status → RELEASED)
- `REJECTED` - Review rejected
- `FILE_ATTACHED` - File uploaded/attached
- `REVISED` - New revision created

**Example:**
```json
{
  "id": 42,
  "documentId": "doc-abc-123",
  "action": "UPDATED",
  "oldValue": "IN_WORK",
  "newValue": "IN_REVIEW",
  "user": "vivi",
  "comment": "Submitted for review",
  "timestamp": "2025-10-18T16:00:00"
}
```

---

## Enumerations

### Status Enum (Document Lifecycle)

```java
public enum Status {
    DRAFT,        // Initial draft state
    IN_WORK,      // Being actively worked on
    IN_REVIEW,    // Submitted for review/approval
    APPROVED,     // Approved but not yet released
    RELEASED,     // Officially released version
    OBSOLETE      // Deprecated/superseded
}
```

**Typical Flow:**
```
DRAFT → IN_WORK → IN_REVIEW → RELEASED
                      ↓
                  REJECTED (back to IN_WORK)
```

### Stage Enum (Engineering Phase)

```java
public enum Stage {
    CONCEPTUAL_DESIGN,      // Early concept phase
    PRELIMINARY_DESIGN,     // Initial design
    DETAILED_DESIGN,        // Full engineering design
    MANUFACTURING,          // Production phase
    IN_SERVICE,             // Deployed/in use
    RETIRED                 // End of life
}
```

**Typical Flow:**
```
CONCEPTUAL_DESIGN → PRELIMINARY_DESIGN → DETAILED_DESIGN → 
MANUFACTURING → IN_SERVICE → RETIRED
```

---

## Snapshot Versioning System

### How It Works

Every time a document is edited or released, a **new immutable record** is created with a new UUID. The previous version is marked as `isActive=false`.

### Version Lifecycle Example

#### Step 1: Document Created (v0.1)
```
document_master:
  id: MASTER-001, title: "Product Spec", creator: "vivi"

document:
  id: DOC-V1, masterId: MASTER-001, revision: 0, version: 1
  status: IN_WORK, isActive: TRUE ✅
  
Display: v0.1
```

#### Step 2: Document Edited (v0.1 → v0.2)
```
document_master:
  id: MASTER-001 (unchanged)

document (2 rows):
  id: DOC-V1, masterId: MASTER-001, revision: 0, version: 1
  status: IN_WORK, isActive: FALSE ❌ (archived)
  
  id: DOC-V2, masterId: MASTER-001, revision: 0, version: 2
  status: IN_WORK, isActive: TRUE ✅ (current)
  
Display: v0.2
```

#### Step 3: Document Edited Again (v0.2 → v0.3)
```
document_master:
  id: MASTER-001 (unchanged)

document (3 rows):
  id: DOC-V1, masterId: MASTER-001, revision: 0, version: 1, isActive: FALSE ❌
  id: DOC-V2, masterId: MASTER-001, revision: 0, version: 2, isActive: FALSE ❌
  id: DOC-V3, masterId: MASTER-001, revision: 0, version: 3, isActive: TRUE ✅
  
Display: v0.3
```

#### Step 4: Document Released (v0.3 → v1.0)
```
document_master:
  id: MASTER-001 (unchanged)

document (4 rows):
  id: DOC-V1, masterId: MASTER-001, revision: 0, version: 1, isActive: FALSE ❌
  id: DOC-V2, masterId: MASTER-001, revision: 0, version: 2, isActive: FALSE ❌
  id: DOC-V3, masterId: MASTER-001, revision: 0, version: 3, isActive: FALSE ❌
  id: DOC-V4, masterId: MASTER-001, revision: 1, version: 0, isActive: TRUE ✅
                                    status: RELEASED
  
Display: v1.0
```

#### Step 5: Edit Released Document (v1.0 → v1.1)
```
document (5 rows):
  id: DOC-V1, revision: 0, version: 1, status: IN_WORK, isActive: FALSE ❌
  id: DOC-V2, revision: 0, version: 2, status: IN_WORK, isActive: FALSE ❌
  id: DOC-V3, revision: 0, version: 3, status: IN_WORK, isActive: FALSE ❌
  id: DOC-V4, revision: 1, version: 0, status: RELEASED, isActive: FALSE ❌ (archived v1.0 - stays RELEASED)
  id: DOC-V5, revision: 1, version: 1, status: IN_WORK, isActive: TRUE ✅ (new v1.1 - must be reviewed again)
  
Display: v1.1
Note: v1.0 keeps RELEASED status even when archived
      v1.1 starts as IN_WORK and must go through review/release again
```

---

## Version Increment Rules

| User Action | Version Change | Revision Change | Creates New Record? | isActive Update | Status Change |
|-------------|---------------|-----------------|---------------------|-----------------|---------------|
| **Create Document** | Set to 1 | Set to 0 | ✅ Yes (first record) | New: TRUE | Set to IN_WORK |
| **Edit Document** | +1 increment | No change | ✅ Yes (snapshot) | Old: FALSE → New: TRUE | **Always IN_WORK** |
| **Release Document** | Reset to 0 | +1 increment | ✅ Yes (snapshot) | Old: FALSE → New: TRUE | Set to RELEASED |
| **Reject Review** | No change | No change | ❌ No (status updated) | No change | Back to IN_WORK |

### Important Status Rules

⚠️ **RELEASED status can ONLY be applied to the specific version that went through the review and release process.**

🔒 **CRITICAL: RELEASED documents CANNOT be edited directly!**

### Engineering Change Management (ECM) Workflow

**RELEASED documents are locked and require an approved Change Request before editing:**

1. **User wants to edit a RELEASED document** (e.g., v1.0)
   - ❌ Direct edit button is **disabled**
   - ✅ User must create a **Change Request**

2. **Create Change Request**
   - Navigate to "Changes" section
   - Create new change request
   - Select the RELEASED document

3. **Approve Change Request**
   - Change goes through approval workflow
   - Once approved, system calls `/initiate-change-edit` endpoint

4. **Document becomes editable**
   - Original v1.0: status=`RELEASED`, isActive=`FALSE` (archived)
   - New v1.1: status=`IN_WORK`, isActive=`TRUE` (editable)
   - User can now make changes

5. **After editing, document must be reviewed/released again**
   - v1.1 goes through review process
   - Once approved → becomes v2.0 with status=`RELEASED`

**Example Flow:**
```
v1.0: status=RELEASED, isActive=TRUE (locked, cannot edit)
  ↓ Create Change Request → Approve
v1.1: status=IN_WORK, isActive=TRUE (unlocked via approved change)
  ↓ Make edits → Review & Release
v2.0: status=RELEASED, isActive=TRUE (newly released, locked again)
```

**Why This Rule Exists:**
- 🛡️ **Quality Control**: Ensures all changes are authorized
- 📋 **Traceability**: Every change is tracked through change management
- ⚖️ **Compliance**: Meets regulatory requirements (FDA, ISO, etc.)
- 🔍 **Audit Trail**: Clear history of why documents were changed

### Special Cases

- **Attempting to Edit a RELEASED Document Directly**: 
  - ❌ Backend throws `ValidationException`
  - ❌ Frontend blocks with disabled button + tooltip
  - ✅ User must use Change Request workflow
  
- **Change-Based Editing**:
  - Requires approved Change Request
  - Uses `/initiate-change-edit` endpoint
  - Creates new editable version (v1.0 → v1.1)
  - Status changes from `RELEASED` to `IN_WORK`
  
- **File Upload During Edit**: The new file is attached to the **new version snapshot**

- **No Changes on Update**: If no fields changed, no new snapshot is created

---

## API Response Format

### DocumentResponse DTO

This is what the frontend receives from the API:

```json
{
  "id": "doc-abc-123-uuid",
  "masterId": "master-001-uuid",
  "title": "Product Specification",
  "description": "Detailed product requirements for Q1 2025",
  "status": "RELEASED",
  "stage": "DETAILED_DESIGN",
  "version": "v1.0",              // String format for display
  "revision": 1,                  // Integer for calculations
  "versionNumber": 0,             // Integer (actual version field)
  "fileKey": "documents/2025/10/doc-abc-123_specification.pdf",
  "creator": "vivi",
  "createTime": "2025-10-18T15:30:00",
  "master": {                     // Nested master info
    "id": "master-001-uuid",
    "documentNumber": "DOC-2025-001",
    "creator": "vivi",
    "createTime": "2025-10-15T10:00:00"
  }
}
```

### Field Mapping (Backend → Frontend)

| Backend Field | DTO Field | Frontend Display |
|--------------|-----------|------------------|
| `document.version` | `versionNumber` | Used in `v{revision}.{versionNumber}` |
| `document.revision` | `revision` | Used in `v{revision}.{versionNumber}` |
| `document.getFullVersion()` | `version` | "v1.0" (string) |
| `document.master.id` | `masterId` | Master reference |
| `document.master.*` | `master.*` | Full master object |

---

## Database Relationships

### Entity Relationship Diagram

```
┌─────────────────────┐
│  document_master    │ 
│  (Logical Document) │
│  - id (PK)         │
│  - title           │
│  - creator         │
│  - category        │
│  - createTime      │
└──────────┬──────────┘
           │ 1
           │
           │ Many
           ▼
┌─────────────────────┐
│     document        │ 
│  (Version Snapshot) │
│  - id (PK)         │
│  - master_id (FK)  │◄──┐
│  - version         │   │
│  - revision        │   │
│  - isActive        │   │
│  - title           │   │
│  - description     │   │
│  - creator         │   │
│  - createTime      │   │
│  - status          │   │
│  - stage           │   │
│  - fileKey         │   │
│  - bomId           │   │
└──────────┬──────────┘   │
           │ 1            │
           │              │
           │ Many         │
           ▼              │
┌─────────────────────┐   │
│  document_history   │   │
│   (Audit Trail)     │   │
│  - id (PK)         │   │
│  - documentId      │───┘
│  - action          │
│  - oldValue        │
│  - newValue        │
│  - user            │
│  - comment         │
│  - timestamp       │
└─────────────────────┘
```

### Relationship Details

- **One Master** → **Many Documents** (1:N)
  - All versions with the same `master_id` belong to the same logical document
  
- **One Document** → **Many History Records** (1:N)
  - Each document version has its own history entries

- **Active Constraint**: Only ONE document per master should have `isActive=true`

---

## REST API Endpoints

### Document Management Endpoints

| Method | Endpoint | Description | Status Constraint |
|--------|----------|-------------|-------------------|
| `GET` | `/documents` | Get all active documents | - |
| `GET` | `/documents/{id}` | Get specific document | - |
| `GET` | `/documents/{id}/versions` | Get all versions of a document | - |
| `POST` | `/documents` | Create new document | Creates as IN_WORK |
| `PUT` | `/documents/{id}` | Update document | ❌ **Blocks if RELEASED** |
| `POST` | `/documents/{id}/submit-review` | Submit for review | Requires IN_WORK |
| `POST` | `/documents/{id}/review-complete` | Complete review (approve/reject) | Requires IN_REVIEW |
| `POST` | `/documents/{id}/initiate-change-edit` | **Start change-based editing** | **Requires RELEASED** |
| `POST` | `/documents/{id}/upload` | Upload file to document | - |
| `GET` | `/documents/{id}/download` | Download document file | - |
| `DELETE` | `/documents/{id}` | Delete document | - |

### Change-Based Editing Endpoint (NEW)

**Purpose:** Unlock a RELEASED document for editing after Change Request approval.

**Endpoint:**
```
POST /api/v1/documents/{id}/initiate-change-edit?changeId={changeId}&user={username}
```

**Parameters:**
- `id` (path): Document ID of the RELEASED document
- `changeId` (query): The approved Change Request ID
- `user` (query): Username initiating the change

**Request Example:**
```http
POST /api/v1/documents/doc-abc-123/initiate-change-edit?changeId=CHG-2025-001&user=vivi
```

**Response:** Returns the new editable document version
```json
{
  "id": "doc-def-456",
  "masterId": "master-001",
  "title": "Product Specification",
  "status": "IN_WORK",
  "version": "v1.1",
  "revision": 1,
  "versionNumber": 1,
  "isActive": true
}
```

**Behavior:**
1. Validates document is RELEASED
2. Marks RELEASED version as inactive (isActive=false)
3. Creates new version with status=IN_WORK, isActive=true
4. Logs "CHANGE_INITIATED" action in history
5. Returns new editable document

**Error Cases:**
- `404 Not Found`: Document doesn't exist
- `400 ValidationException`: Document is not RELEASED
- `400 ValidationException`: Change ID is invalid

---

## Query Methods

### Repository Methods (DocumentRepository)

```java
// Get all active documents (for main list)
List<Document> findByIsActiveTrue();

// Get all versions of a document (for version history)
List<Document> findByMaster_IdOrderByRevisionDescVersionDesc(String masterId);

// Get the active version for a specific master
Document findByMaster_IdAndIsActiveTrue(String masterId);

// Find documents by BOM ID
List<Document> findByBomId(String bomId);

// Get specific document by ID
Optional<Document> findById(String id);
```

### Query Examples

#### 1. Get Active Documents (Main Document List)
```java
List<Document> activeDocuments = documentRepository.findByIsActiveTrue();
// Returns: Only documents with isActive=true
// Use Case: Display in main document table
```

#### 2. Get Version History
```java
String masterId = "master-001";
List<Document> versions = documentRepository
    .findByMaster_IdOrderByRevisionDescVersionDesc(masterId);
// Returns: [v2.0, v1.2, v1.1, v1.0, v0.3, v0.2, v0.1] (newest first)
// Use Case: Display in version history dialog
```

#### 3. Get Current Active Version
```java
String masterId = "master-001";
Document currentVersion = documentRepository
    .findByMaster_IdAndIsActiveTrue(masterId);
// Returns: The one document with isActive=true for this master
// Use Case: Get latest version for editing
```

---

## File Storage

### MinIO Integration

Documents can have attached files stored in MinIO object storage.

| Field | Description | Example |
|-------|-------------|---------|
| `fileKey` | Path to file in MinIO | `documents/2025/10/abc-123_filename.pdf` |
| Format | `documents/{year}/{month}/{uuid}_{originalFilename}` | - |

### File Operations

#### Upload File
```java
// 1. Create/Update document (returns new version snapshot)
Document newVersion = documentService.updateDocument(documentId, updateRequest);

// 2. Upload file to NEW version
String fileKey = fileStorageService.upload(file);

// 3. Attach file key to NEW version
documentService.attachFileKey(newVersion.getId(), fileKey, username);
```

#### Download File
```java
// Get document by version ID
Document document = documentService.getById(versionId);

// Download file using fileKey
byte[] fileData = fileStorageService.download(document.getFileKey());
```

### Key Points

- Each version can have its **own file**
- File keys are **copied** to new versions by default
- Uploading a new file during edit **replaces** the file for that version only
- Historical versions **keep their original files**

---

## Examples

### Example 1: Complete Document Lifecycle

```java
// 1. CREATE DOCUMENT (v0.1)
CreateDocumentRequest createReq = new CreateDocumentRequest();
createReq.setMasterId("MASTER-001");
createReq.setTitle("Product Specification");
createReq.setCreator("vivi");
createReq.setStage(Stage.CONCEPTUAL_DESIGN);
Document v01 = documentService.create(createReq);
// Result: id=DOC-V1, revision=0, version=1, isActive=true

// 2. EDIT DOCUMENT (v0.1 → v0.2)
UpdateDocumentRequest updateReq = new UpdateDocumentRequest();
updateReq.setTitle("Updated Product Specification");
updateReq.setDescription("Added new requirements");
updateReq.setUser("vivi");
Document v02 = documentService.updateDocument(v01.getId(), updateReq);
// Result: 
//   DOC-V1: isActive=false (archived)
//   DOC-V2: revision=0, version=2, isActive=true (new)

// 3. SUBMIT FOR REVIEW
SubmitForReviewRequest reviewReq = new SubmitForReviewRequest();
reviewReq.setReviewerIds(Arrays.asList("reviewer1", "reviewer2"));
Document v02InReview = documentService.submitForReview(v02.getId(), reviewReq);
// Result: Same document, status changed to IN_REVIEW

// 4. APPROVE AND RELEASE (v0.2 → v1.0)
Document v10 = documentService.completeReview(
    v02InReview.getId(), 
    true,  // approved
    "reviewer1", 
    "Approved for release"
);
// Result:
//   DOC-V1: revision=0, version=1, isActive=false
//   DOC-V2: revision=0, version=2, isActive=false
//   DOC-V3: revision=1, version=0, isActive=true, status=RELEASED (new)

// 5. GET VERSION HISTORY
List<Document> allVersions = documentService.getDocumentVersions(v10.getId());
// Returns: [v1.0 (DOC-V3), v0.2 (DOC-V2), v0.1 (DOC-V1)]

// 6. ACCESS HISTORICAL VERSION
Document historicalV01 = documentService.getById("DOC-V1");
// Returns: Original v0.1 with all its data intact

// 7. EDIT RELEASED DOCUMENT (v1.0 → v1.1)
UpdateDocumentRequest editReq = new UpdateDocumentRequest();
editReq.setDescription("Post-release updates");
editReq.setUser("vivi");
Document v11 = documentService.updateDocument(v10.getId(), editReq);
// Result:
//   DOC-V3: revision=1, version=0, status=RELEASED, isActive=false (archived v1.0 - stays RELEASED!)
//   DOC-V4: revision=1, version=1, status=IN_WORK, isActive=true (new v1.1 - must be reviewed again)
// IMPORTANT: v1.1 starts as IN_WORK, NOT RELEASED
//            Only the review/release process can set status to RELEASED
```

### Example 2: Frontend Version History Display

```javascript
// Fetch document details
const document = await documentService.getDocument(documentId);

// Fetch all versions for this document
const versions = await documentService.getDocumentVersions(documentId);

// Display version history (newest first)
versions.forEach((version, index) => {
  console.log(`v${version.revision}.${version.versionNumber}`);
  console.log(`  Status: ${version.status}`);
  console.log(`  Active: ${version.isActive}`);
  console.log(`  Created: ${version.createTime}`);
  console.log(`  By: ${version.creator}`);
  if (index === 0) {
    console.log(`  [Latest Version]`);
  }
  if (version.id === document.id) {
    console.log(`  [Current View]`);
  }
});
```

**Output:**
```
v1.0
  Status: RELEASED
  Active: true
  Created: 2025-10-18T16:00:00
  By: vivi
  [Latest Version]
  [Current View]
v0.2
  Status: IN_WORK
  Active: false
  Created: 2025-10-18T15:30:00
  By: vivi
v0.1
  Status: IN_WORK
  Active: false
  Created: 2025-10-18T15:00:00
  By: vivi
```

### Example 3: Query Performance

```java
// BAD: Getting all documents and filtering (slow for large datasets)
List<Document> allDocs = documentRepository.findAll();
List<Document> activeDocs = allDocs.stream()
    .filter(Document::isActive)
    .collect(Collectors.toList());

// GOOD: Using indexed query (fast)
List<Document> activeDocs = documentRepository.findByIsActiveTrue();
// Uses index on isActive column
```

---

## Best Practices

### 1. Understand Status Rules for RELEASED Documents

⚠️ **Critical Rule**: RELEASED status is sacred and only applies to versions that completed the review/release process.

```java
// CORRECT: When editing, always set status to IN_WORK
newVersion.setStatus(Status.IN_WORK);  // Even if previous version was RELEASED

// WRONG: Copying status from previous version
newVersion.setStatus(currentVersion.getStatus());  // Don't do this!
```

**Why?**
- Each version must earn its RELEASED status through the formal review process
- Historical RELEASED versions remain as proof of what was officially released
- New edits need re-validation before they can be released

### 2. Always Use the Active Version for Editing
```java
// Get the active version for a master
Document activeDoc = documentRepository
    .findByMaster_IdAndIsActiveTrue(masterId);

// Never edit inactive versions
if (!activeDoc.isActive()) {
    throw new IllegalStateException("Cannot edit inactive version");
}
```

### 3. Maintain isActive Integrity
```java
// When creating a new version snapshot
// Step 1: Mark old version as inactive
oldVersion.setActive(false);
documentRepository.save(oldVersion);

// Step 2: Create new version as active
newVersion.setActive(true);
documentRepository.save(newVersion);
```

### 4. Handle Version Display Correctly
```javascript
// WRONG: Version 0 is falsy in JavaScript
const display = `v${doc.revision}.${doc.versionNumber || doc.version || 0}`;
// When versionNumber=0, falls back to doc.version which might be "v1.0"
// Result: "v1.v1.0" ❌

// CORRECT: Check for undefined
const display = `v${doc.revision}.${doc.versionNumber !== undefined ? doc.versionNumber : 0}`;
// Correctly handles versionNumber=0
// Result: "v1.0" ✅
```

### 5. Query Optimization
```java
// Create database indexes for common queries
@Table(name = "document", indexes = {
    @Index(name = "idx_master_active", columnList = "master_id, isActive"),
    @Index(name = "idx_active", columnList = "isActive")
})
```

---

## Storage Considerations

### Database Size Growth

Each version creates a new row:
- **Average Row Size**: ~1-2 KB (without large text fields)
- **10 documents with 5 versions each**: ~50-100 KB
- **1000 documents with 10 versions each**: ~10-20 MB

### Cleanup Strategies (Future Enhancement)

```java
// Option 1: Archive old versions to separate table
// Option 2: Soft delete inactive versions older than X days
// Option 3: Compress inactive version data
```

---

## Troubleshooting

### Common Issues

#### Issue 1: "v1.v1.0" Instead of "v1.0"
**Cause**: JavaScript treats `0` as falsy in `||` operator
**Fix**: Use `!== undefined` check instead of `||`

#### Issue 2: Multiple Active Versions
**Cause**: Transaction didn't complete properly when creating new version
**Fix**: Ensure both save operations are in same transaction

#### Issue 3: Missing Version History
**Cause**: Query using wrong method or master_id not matching
**Fix**: Use `findByMaster_IdOrderByRevisionDescVersionDesc(masterId)`

---

## Future Enhancements

### Potential Improvements

1. **Version Diff**: Compare changes between versions
2. **Branching**: Create parallel version branches
3. **Merging**: Merge changes from different branches
4. **Compression**: Compress inactive versions to save space
5. **Archiving**: Move old versions to cold storage
6. **Permissions**: Version-specific access control

---

## Related Documentation

- [DEVELOPMENT_DOCUMENTATION.md](./DEVELOPMENT_DOCUMENTATION.md) - Full system documentation
- API Documentation - REST endpoint details
- Database Migration Guide - Schema evolution procedures

---

**Last Updated**: October 18, 2025  
**Version**: 1.0  
**Author**: PLM-lite Development Team


# Part-Based BOM Migration Plan

## 🎯 Goal
Migrate from BOM Header/Items system to Part-based hierarchical system while preserving existing functionality.

## 📋 Current State

### BOM Header System
- ✅ Document-centric (linked to documentId)
- ✅ Status workflow (DRAFT → IN_REVIEW → RELEASED)
- ✅ Soft delete support
- ✅ Update tracking
- ✅ Flat item list with quantities
- ❌ No Neo4j sync
- ❌ No real hierarchical relationships

### Part System  
- ✅ Hierarchical via PartUsage (parent-child with quantities)
- ✅ Document links via DocumentPartLink
- ✅ Neo4j sync implemented
- ✅ Creator tracking
- ❌ No Status field (only Stage)
- ❌ No Description field
- ❌ No soft delete
- ❌ No update tracking

## 🔧 Phase 1: Enhance Part Model

### Step 1.1: Add Fields to Part Entity

Add to `Part.java`:
```java
@Column
private String description;  // Detailed description

@Column
@Enumerated(EnumType.STRING)
private Status status;  // DRAFT, IN_REVIEW, RELEASED, etc.

@Column(nullable = false)
private boolean deleted = false;  // Soft delete

@Column
private LocalDateTime deleteTime;

@Column(nullable = false)
private LocalDateTime updateTime;

@PreUpdate
protected void onUpdate() {
    updateTime = LocalDateTime.now();
}
```

### Step 1.2: Update PartServiceImpl

Add sync for status changes, soft delete, etc.

### Step 1.3: Update GraphSyncService

Sync new fields to Neo4j:
- description
- status
- deleted flag

## 🔄 Phase 2: Create BOM Adapter Layer

### Step 2.1: BomToPart Adapter Service

Create `BomToPartAdapter.java`:
- Maps BOM Header → Part (root part)
- Maps BOM Items → PartUsage relationships
- Preserves documentId via DocumentPartLink

```java
/**
 * Adapts BOM Header/Items operations to Part-based operations
 * Provides backward compatibility during migration
 */
@Service
public class BomToPartAdapter {
    
    // Convert BOM Header to root Part
    public Part bomHeaderToPart(BomHeader header);
    
    // Convert BOM Items to PartUsage relationships
    public List<PartUsage> bomItemsToPartUsages(List<BomItem> items, Part parent);
    
    // Create Part-based BOM from BOM Header
    public Part createPartBasedBom(CreateBomRequest request);
    
    // Query Part system as if it's BOM Header
    public BomResponse getPartAsBomResponse(String partId);
}
```

### Step 2.2: Update BOM Controller (Backward Compatible)

Option A: **Keep BOM Controller, delegate to Part operations**
```java
@PostMapping
public ResponseEntity<BomResponse> create(@RequestBody CreateBomRequest request) {
    // Internally creates Part + PartUsage relationships
    Part rootPart = bomToPartAdapter.createPartBasedBom(request);
    return ResponseEntity.ok(bomToPartAdapter.getPartAsBomResponse(rootPart.getId()));
}
```

Option B: **Deprecate BOM Controller, redirect to Part Controller**
```java
@Deprecated
@PostMapping
public ResponseEntity<BomResponse> create(@RequestBody CreateBomRequest request) {
    // Redirect or return migration notice
}
```

## 🗃️ Phase 3: Data Migration

### Step 3.1: Migration Script

Create SQL script to migrate existing BOM Headers/Items to Parts:

```sql
-- Migrate BOM Headers to Parts
INSERT INTO part (id, title, description, stage, status, level, creator, create_time, update_time, deleted, delete_time)
SELECT 
    id,
    description AS title,
    description,
    stage,
    status,
    'ASSEMBLY' AS level,
    creator,
    create_time,
    update_time,
    deleted,
    delete_time
FROM bom_headers
WHERE deleted = false;

-- Migrate BOM Items to PartUsage
-- (Requires Part entities for each unique partNumber first)

-- Link Parts to Documents
INSERT INTO document_part_link (link_id, part_id, document_id)
SELECT 
    UUID(),
    id,
    document_id
FROM bom_headers
WHERE document_id IS NOT NULL;
```

### Step 3.2: Neo4j Sync for Migrated Data

Run sync for all migrated Parts to populate Neo4j.

## 📱 Phase 4: Frontend Update

### Step 4.1: Update Web UI

- Point "BOM Management" to Part-based APIs
- Use `/parts` endpoints instead of `/boms`
- Add hierarchy visualization (already in Neo4j)

### Step 4.2: Update API Calls

**Before (BOM Header):**
```javascript
POST /boms
GET /boms/{id}
GET /boms/document/{documentId}
```

**After (Part-based):**
```javascript
POST /parts
GET /parts/{id}
GET /parts/document/{documentId}  // Via DocumentPartLink
POST /parts/usage  // Add child parts
GET /parts/{id}/bom-hierarchy  // Get full BOM tree
```

## ⏱️ Implementation Timeline

### Week 1: Enhance Part Model
- ✅ Add status, description, soft delete fields
- ✅ Update Part service
- ✅ Update Neo4j sync
- ✅ Test Part CRUD with new fields

### Week 2: Create Adapter Layer
- ✅ Build BomToPartAdapter
- ✅ Update BOM Controller to delegate
- ✅ Maintain backward compatibility
- ✅ Test existing BOM APIs

### Week 3: Data Migration
- ✅ Create migration scripts
- ✅ Test on dev environment
- ✅ Migrate production data
- ✅ Sync to Neo4j

### Week 4: Frontend Update
- ✅ Update Web UI to use Part APIs
- ✅ Add hierarchy visualization
- ✅ Test end-to-end
- ✅ Deprecate BOM Header system

## ✅ Benefits After Migration

1. **Single Source of Truth** - Part entity for all BOM data
2. **Real-Time Neo4j Sync** - Automatic graph updates
3. **True Hierarchy** - Via PartUsage relationships
4. **Advanced Queries** - BOM explosion, where-used, impact analysis
5. **Better Performance** - Graph queries in Neo4j
6. **Cleaner Architecture** - One system instead of two

## 🚨 Risks & Mitigation

| Risk | Mitigation |
|------|------------|
| Data loss during migration | Full backup before migration, rollback plan |
| Breaking existing integrations | Maintain BOM API backward compatibility via adapter |
| Frontend not updated | Gradual rollout, feature flags |
| Neo4j sync failures | Fallback to MySQL, retry mechanism |

## 🔍 Decision Point: Which Option?

### Option 1: Full Migration (Recommended)
- Enhance Part model
- Deprecate BOM Header completely
- Update frontend to use Part APIs
- **Effort**: Medium
- **Benefit**: Clean architecture, single system

### Option 2: Hybrid (Quick Win)
- Keep both systems
- BOM Controller delegates to Part operations
- Frontend can use either API
- **Effort**: Low
- **Benefit**: Backward compatible, gradual migration

### Option 3: Parallel Systems
- Keep BOM Header as-is
- Use Part for new features
- No migration
- **Effort**: Low
- **Benefit**: No breaking changes, but technical debt

## 🎯 Recommendation

**Go with Option 1 (Full Migration)** because:
1. You already have Part hierarchy working
2. Neo4j sync is already implemented for Parts
3. BOM Header adds complexity with no real benefit
4. Better long-term maintainability

---

## 🚀 Next Steps

1. **Decide**: Which option? (Recommended: Option 1)
2. **Enhance Part Model**: Add status, description, soft delete
3. **Update Neo4j Sync**: Handle new fields
4. **Test**: Create parts with new fields
5. **Migrate Data**: Run migration script
6. **Update Frontend**: Point to Part APIs


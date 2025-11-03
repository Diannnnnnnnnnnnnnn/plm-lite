# Change Management BOM vs Part Fix

## Problem
In the Change Management system, when users selected a **Part** from a BOM, the system was trying to find it as a **BOM header** instead of a Part, causing "BOM not found" errors.

## Root Cause
The system has two entities:
- `ChangeBom` - for linking changes to BOM headers
- `ChangePart` - for linking changes to specific Parts

However, the frontend was sending everything as `bomIds`, even when a Part was selected. The backend only processed `bomIds` and didn't handle Parts separately.

## Solution

### 1. Backend Changes

#### a) Updated `CreateChangeRequest` DTO
**File:** `change-service/src/main/java/com/example/plm/change/dto/CreateChangeRequest.java`

Added support for `partIds`:
```java
private List<String> bomIds = new ArrayList<>();
private List<String> partIds = new ArrayList<>();  // NEW

public List<String> getPartIds() { return partIds; }
public void setPartIds(List<String> partIds) { this.partIds = partIds; }
```

#### b) Updated `ChangeServiceDev`
**File:** `change-service/src/main/java/com/example/plm/change/service/ChangeServiceDev.java`

- Added `ChangePart` import
- Updated `createChange()` method to handle `partIds` and create `ChangePart` relationships
- Added Neo4j graph sync for change-part links
- Updated `mapToResponse()` to include Part IDs in the response

```java
// Create Part relationships if provided
if (request.getPartIds() != null && !request.getPartIds().isEmpty()) {
    for (String partId : request.getPartIds()) {
        ChangePart changePart = new ChangePart(
            UUID.randomUUID().toString(),
            change,
            partId
        );
        changePartRepository.save(changePart);
        
        // Sync to Neo4j graph
        if (graphServiceClient != null) {
            try {
                graphServiceClient.syncChangePart(changeId, partId);
                log.info("✅ Change-Part link {} -> {} synced to graph", changeId, partId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to sync change-part link to graph: {}", e.getMessage());
            }
        }
    }
}
```

#### c) Updated `ChangeResponse` DTO
**File:** `change-service/src/main/java/com/example/plm/change/dto/ChangeResponse.java`

Added `partIds` field:
```java
private List<String> partIds = new ArrayList<>();

public List<String> getPartIds() { return partIds; }
public void setPartIds(List<String> partIds) { this.partIds = partIds; }
```

### 2. Frontend Changes

#### Updated `ChangeManager.js`
**File:** `frontend/src/components/Changes/ChangeManager.js`

**a) Fixed `handleSelectBOMItem` function:**
- Changed from composite ID (`${bomId}-${item.id}`) to just the part ID (`item.id`)
- Added `bomId` reference for display purposes

```javascript
const handleSelectBOMItem = (item, bomId, bomDescription) => {
  setNewChange({
    ...newChange,
    product: item.id, // Use the actual part ID from the BOM item
    selectedProductInfo: {
      id: item.id,
      description: `${item.description} (from ${bomDescription})`,
      partNumber: item.partNumber,
      type: 'Part',
      bomId: bomId // Keep reference to parent BOM for display purposes
    }
  });
  setBomSelectionOpen(false);
  setBomSearchTerm('');
};
```

**b) Updated `handleCreateChange` function:**
- Added logic to distinguish between BOM and Part selections
- Send `bomIds` when a BOM header is selected
- Send `partIds` when a Part is selected

```javascript
// Determine if the selected product is a BOM or Part
const isBOM = newChange.selectedProductInfo?.type === 'BOM';
const isPart = newChange.selectedProductInfo?.type === 'Part';

const changeData = {
  title: newChange.title,
  changeClass: newChange.changeClass,
  product: newChange.product,
  stage: newChange.stage,
  creator: currentUser,
  changeReason: newChange.changeReason,
  changeDocument: newChange.changeDocument,
  documentIds: newChange.changeDocument ? [newChange.changeDocument] : [],
  bomIds: isBOM && newChange.product ? [newChange.product] : [],
  partIds: isPart && newChange.product ? [newChange.product] : []
};
```

## Benefits

1. **Correct Entity Mapping**: Changes now properly link to either BOM headers OR Parts, not treating everything as BOMs
2. **Neo4j Graph Sync**: Change-Part relationships are synced to the Neo4j graph database for visualization
3. **Database Integrity**: Uses the correct repository (`ChangeBomRepository` vs `ChangePartRepository`) based on the entity type
4. **Future-Proof**: Supports the system's migration from BOM-centric to Part-centric architecture

## Testing

To test the fix:

1. **Create a change linked to a BOM header:**
   - Select a BOM (not a part within it)
   - The change should save with the BOM ID in `bomIds`

2. **Create a change linked to a Part:**
   - Expand a BOM and select a specific part
   - The change should save with the Part ID in `partIds`

3. **Verify in Neo4j:**
   - Check that change-part relationships appear in the graph
   - Query: `MATCH (c:Change)-[:AFFECTS]->(p:Part) RETURN c, p`

## Related Files Modified

### Backend
- `change-service/src/main/java/com/example/plm/change/dto/CreateChangeRequest.java`
- `change-service/src/main/java/com/example/plm/change/dto/ChangeResponse.java`
- `change-service/src/main/java/com/example/plm/change/service/ChangeServiceDev.java`

### Frontend
- `frontend/src/components/Changes/ChangeManager.js`

## Database Schema

The system now properly uses both tables:

```sql
-- For BOM header links
CREATE TABLE change_bom (
    id VARCHAR(255) PRIMARY KEY,
    change_id VARCHAR(255) NOT NULL,
    bom_id VARCHAR(255) NOT NULL
);

-- For Part links
CREATE TABLE ChangePart (
    id VARCHAR(255) PRIMARY KEY,
    changetask_id VARCHAR(255) NOT NULL,
    part_id VARCHAR(255) NOT NULL
);
```

## Deployment Notes

⚠️ **Important**: After deploying these changes, restart the change-service to pick up the new logic.

```powershell
# Windows
.\restart-change-service.ps1

# Or restart all services
.\start-all-services.ps1
```


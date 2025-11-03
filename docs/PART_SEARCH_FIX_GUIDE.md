# Part Search Fix - Complete Guide

## Problem Summary
Parts in the BOM service were not appearing in the global search because:
1. **Deleted parts were removed from Elasticsearch** - When parts were soft-deleted, they were removed from the index
2. **Old parts weren't indexed** - Parts created before Elasticsearch integration weren't in the index
3. **No re-indexing capability** - There was no way to bulk re-index existing parts

## Solution Implemented

### 1. **Re-indexing Endpoint** ✅
Added a new endpoint to bulk re-index all parts from the database to Elasticsearch:
- **Endpoint**: `POST http://localhost:8089/api/v1/parts/elasticsearch/reindex`
- **Method**: Added `reindexAllParts()` in `PartService` and `PartServiceImpl`
- **Location**: `bom-service/src/main/java/com/example/bom_service/controller/PartController.java`

### 2. **Updated Deletion Behavior** ✅
Modified the deletion logic to **keep deleted parts in Elasticsearch** with a `deleted` flag:
- **Location**: `bom-service/src/main/java/com/example/bom_service/service/impl/PartServiceImpl.java`
- **Change**: Instead of deleting from ES, we re-index with `deleted: true`

### 3. **Updated Search Filter** ✅
Modified the search service to **filter out deleted parts** from search results:
- **Location**: `infra/search-service/src/main/java/com/example/plm/search/service/UnifiedSearchService.java`
- **Change**: Added a boolean query filter to exclude parts where `deleted: true`

### 4. **Re-indexing Scripts** ✅
Created scripts for easy re-indexing:
- **Windows Batch**: `reindex-parts.bat`
- **PowerShell**: `reindex-parts.ps1`

---

## How to Fix Your Parts Search

### Step 1: Ensure Services are Running
Make sure these services are running:
1. **Elasticsearch** on port 9200
2. **BOM Service** on port 8089
3. **Search Service** (if testing global search)

### Step 2: Re-index All Parts
Run the re-indexing script:

**Option A - Using PowerShell Script:**
```powershell
.\reindex-parts.ps1
```

**Option B - Using Batch File:**
```cmd
reindex-parts.bat
```

**Option C - Using cURL directly:**
```bash
curl -X POST http://localhost:8089/api/v1/parts/elasticsearch/reindex
```

**Option D - Using PowerShell directly:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8089/api/v1/parts/elasticsearch/reindex" -Method POST
```

### Step 3: Verify the Re-indexing
You should see a response like:
```
Successfully re-indexed 26 parts to Elasticsearch
```

### Step 4: Test the Search
**Test via Search Service:**
```bash
curl "http://localhost:8093/api/v1/search/parts?q=ES Test Part"
```

**Or test all entities:**
```bash
curl "http://localhost:8093/api/v1/search?q=ES Test Part"
```

---

## Expected Results

After re-indexing:
- ✅ All 26 parts (including the ones with `deleted: false`) should be in Elasticsearch
- ✅ Global search will return **only non-deleted parts** (18 parts in your case)
- ✅ Parts with `deleted: true` will be in the index but filtered out from search results
- ✅ Future part creations will auto-index
- ✅ Future part deletions will keep the part in the index with `deleted: true`

---

## Troubleshooting

### If re-indexing fails:
1. **Check BOM service logs** for errors
2. **Verify Elasticsearch is running**: `curl http://localhost:9200`
3. **Check network connectivity** between services

### If search still doesn't return parts:
1. **Verify parts are in Elasticsearch**:
   ```bash
   curl "http://localhost:9200/parts/_search?pretty"
   ```

2. **Check Search Service logs** for errors

3. **Verify the deleted field**:
   ```bash
   curl "http://localhost:9200/parts/_search?q=deleted:false&pretty"
   ```

### If you get "Cannot find symbol: reindexAllParts":
This is an IDE linter error. The code will compile correctly. Try:
1. **Rebuild the project**: `mvn clean install` in the `bom-service` directory
2. **Restart the BOM service**

---

## Technical Details

### Modified Files:
1. `bom-service/src/main/java/com/example/bom_service/controller/PartController.java`
   - Added `@PostMapping("/elasticsearch/reindex")` endpoint

2. `bom-service/src/main/java/com/example/bom_service/service/PartService.java`
   - Added `int reindexAllParts()` method signature

3. `bom-service/src/main/java/com/example/bom_service/service/impl/PartServiceImpl.java`
   - Implemented `reindexAllParts()` method
   - Modified `deletePart()` to re-index instead of delete from ES

4. `infra/search-service/src/main/java/com/example/plm/search/service/UnifiedSearchService.java`
   - Added boolean query filter in `searchParts()` to exclude deleted parts

### New Files:
1. `reindex-parts.bat` - Windows batch script for re-indexing
2. `reindex-parts.ps1` - PowerShell script for re-indexing
3. `PART_SEARCH_FIX_GUIDE.md` - This guide

---

## Quick Start Commands

```powershell
# 1. Start Elasticsearch (if not running)
.\start-elasticsearch.bat

# 2. Start BOM Service (if not running)
cd bom-service
mvn spring-boot:run

# 3. Re-index all parts
.\reindex-parts.ps1

# 4. Test search
curl "http://localhost:8093/api/v1/search/parts?q=ES Test"
```

---

## Future Maintenance

- **After database restore**: Run `reindex-parts.ps1`
- **After bulk data import**: Run `reindex-parts.ps1`
- **Regular operations**: Parts will auto-index on create/update/delete

---

## Summary of Changes

| What | Before | After |
|------|--------|-------|
| Part creation | ✅ Auto-indexed | ✅ Auto-indexed (unchanged) |
| Part update | ✅ Auto-indexed | ✅ Auto-indexed (unchanged) |
| Part deletion | ❌ Removed from ES | ✅ Re-indexed with `deleted: true` |
| Search | ❌ Missing old/deleted parts | ✅ Shows only active parts |
| Bulk re-indexing | ❌ Not available | ✅ Available via endpoint |

---

Need help? Check the service logs or contact the development team.





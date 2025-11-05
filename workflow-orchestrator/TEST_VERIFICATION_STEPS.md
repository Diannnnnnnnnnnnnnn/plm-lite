# Test Verification Steps - Change Workflow Fix

## ✅ Fix Applied Successfully

**Date:** 2025-11-05  
**Issue:** First change approval did not update document version, second one did  
**Root Cause:** Duplicate worker registrations causing random behavior  
**Solution:** Removed old duplicate workers from ChangeWorkflowWorkers.java  

## Service Status

- ✅ **Build:** SUCCESS
- ✅ **Package:** SUCCESS  
- ✅ **Deployment:** Restarted (New PID: 57276)
- ✅ **Health Check:** UP on port 8086

## What Was Fixed

### Removed Duplicate Workers
From `ChangeWorkflowWorkers.java`:
- ❌ Old `update-change-status` worker registration (manual)
- ❌ Old `notify-change-completion` worker registration (manual)
- ❌ Old handler methods without document update logic

### Now Using Correct Workers
From `ChangeWorkerHandler.java` (with @JobWorker annotations):
- ✅ `update-change-status` - Updates both change AND document
- ✅ `notify-change-completion` - Sends proper notifications

## Test Procedure

### 1. Create a Test Change

**Endpoint:** `POST http://localhost:8086/api/workflows/change/start`

**Request Body:**
```json
{
  "changeId": "test-change-001",
  "changeTitle": "Test Change After Fix",
  "creator": "vivi",
  "reviewerId": "3",
  "documentId": "66df8bb4-1cbd-4673-9c61-e8e87bb88954"
}
```

**Expected Response:**
```json
{
  "processInstanceKey": "XXXXX",
  "status": "STARTED",
  "message": "Change approval workflow started successfully"
}
```

### 2. Verify Task Creation

Check logs for:
```
📋 Creating change approval task
   Change ID: test-change-001
   Title: Test Change After Fix
   Reviewer: 3
   ✓ Created change review task ID: XX
   ✓ Task linked to CHANGE: test-change-001
```

### 3. Approve the Task

The task service should have created a task. Approve it through the frontend or API.

### 4. Verify Document Update (CRITICAL!)

**This is what was broken before!**

Check logs for:
```
🔧 Worker: update-change-status
   Process Instance: XXXXX
   Change ID: test-change-001
   New Status: RELEASED
   Document ID: 66df8bb4-1cbd-4673-9c61-e8e87bb88954
   ✓ Updated change test-change-001 to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document 66df8bb4-1cbd-4673-9c61-e8e87bb88954 updated: status → IN_WORK, version incremented
```

**Must See:** "Document updated: status → IN_WORK, version incremented"

### 5. Verify Database Changes

**Change Service:**
```sql
SELECT id, title, status FROM changes WHERE id = 'test-change-001';
```
Expected: `status = 'RELEASED'`

**Document Service:**
```sql
SELECT id, master_id, version, status FROM documents WHERE id = '66df8bb4-1cbd-4673-9c61-e8e87bb88954';
```
Expected: 
- `version` incremented (e.g., v0.1 → v0.2)
- `status = 'IN_WORK'`

## Success Criteria

✅ All the following must be TRUE:

1. **Change status updated** to RELEASED
2. **Document version incremented** (e.g., v0.1 → v0.2)
3. **Document status updated** to IN_WORK
4. **Logs show document update** message
5. **No more "Connection refused" errors** (those were from duplicate workers competing)
6. **Consistent behavior** - every approved change updates the document

## What Should NOT Happen

❌ **Before the fix** (random behavior):
- Sometimes: Only change updated, document NOT updated
- Sometimes: Both change AND document updated

✅ **After the fix** (consistent behavior):
- Always: Both change AND document updated

## Monitoring Worker Registration

On service startup, check logs for:

```
🔧 Registering Change Approval Workers...
   ✓ Registered: create-change-approval-task
   ✓ Registered: wait-for-change-review
   NOTE: update-change-status and notify-change-completion workers are now
         registered via @JobWorker annotations in ChangeWorkerHandler.java
✅ Change workflow workers registered successfully!
```

**Important:** Should NOT see duplicate registrations of `update-change-status`

## Rollback Procedure (If Needed)

If the fix causes issues:

1. Stop workflow-orchestrator: `Stop-Process -Id 57276`
2. Revert the file:
   ```bash
   git checkout HEAD -- workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkflowWorkers.java
   ```
3. Rebuild: `mvn clean package -pl workflow-orchestrator -DskipTests`
4. Restart service

## Related Documentation

- `CHANGE_WORKFLOW_DUPLICATE_WORKER_FIX.md` - Detailed problem analysis
- `ChangeWorkerHandler.java` - Current implementation (correct one)
- `ChangeWorkflowWorkers.java` - Updated file (duplicate workers removed)

## Notes

- The fix ensures ALL approved changes will update the document version
- No BPMN changes were needed
- No database migration needed
- The issue was purely code-level duplication


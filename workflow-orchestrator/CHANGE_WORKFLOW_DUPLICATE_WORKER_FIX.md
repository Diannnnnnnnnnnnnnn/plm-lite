# Change Workflow - Duplicate Worker Registration Fix

## Problem Identified

The first change approval workflow did NOT update the document status/version, but the second one DID.

## Root Cause

There were **TWO competing worker implementations** for the same job types:

### 1. Old Workers (ChangeWorkflowWorkers.java)
- Manually registered in `@PostConstruct` method
- Located at lines 62-80 (now removed)
- **DID NOT update document** when change was approved
- Used manual HttpClient calls

```java
// OLD WORKER - No document update
private void handleUpdateChangeStatus(JobClient client, ActivatedJob job) {
    // ... only updated change status
    // ❌ No document version update
}
```

### 2. New Workers (ChangeWorkerHandler.java)
- Auto-registered via `@JobWorker` annotation
- **DOES update document** when change is approved
- Uses Feign clients

```java
// NEW WORKER - With document update
@JobWorker(type = "update-change-status")
public Map<String, Object> updateChangeStatus(final ActivatedJob job) {
    // Update change status
    changeServiceClient.updateStatus(changeId, statusUpdate);
    
    // ✅ Update document when approved
    if ("RELEASED".equals(newStatus) && documentId != null) {
        documentServiceClient.initiateChangeBasedEdit(documentId, changeId, creator);
    }
}
```

## The Issue

When Zeebe had multiple workers registered for the same job type, it **randomly distributed jobs** between them:

- **First change (516c0966)**: Got OLD worker → No document update
- **Second change (46b9e454)**: Got NEW worker → Document updated ✅

## Evidence from Logs

**First Change (using OLD worker):**
```
🔄 Updating change status via REST API  ← OLD worker indicator
   Change ID: 516c0966-cc1c-4ce6-81bc-73e519f4b9ea
   New Status: RELEASED
   ✓ Change status updated successfully to: RELEASED
   ← No document update logs
```

**Second Change (using NEW worker):**
```
🔧 Worker: update-change-status  ← NEW worker indicator
   Change ID: 46b9e454-d214-4351-91e6-1ecbb82bac84
   New Status: RELEASED
   Document ID: 66df8bb4-1cbd-4673-9c61-e8e87bb88954
   ✓ Updated change 46b9e454... to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document 66df8bb4... updated: status → IN_WORK, version incremented ✅
```

## Solution Applied

### Removed Duplicate Worker Registrations

**File:** `ChangeWorkflowWorkers.java`

**Removed:**
1. Manual registration of `update-change-status` worker (lines 62-70)
2. Manual registration of `notify-change-completion` worker (lines 72-80)
3. Old handler methods: `handleUpdateChangeStatus()` and `handleNotifyChangeCompletion()`

**Kept:**
- `create-change-approval-task` worker (still needed)
- `wait-for-change-review` worker (still needed)

### Current State

Now only the **NEW workers in ChangeWorkerHandler.java** handle:
- ✅ `update-change-status` - Updates change status AND document version
- ✅ `notify-change-completion` - Sends completion notifications

## Testing Instructions

1. **Restart workflow-orchestrator service**
2. **Create a new change approval workflow**
3. **Approve the change**
4. **Verify:**
   - Change status updated to RELEASED ✅
   - Document version incremented ✅
   - Document status updated to IN_WORK ✅
   - Logs show: "Document {id} updated: status → IN_WORK, version incremented" ✅

## Expected Behavior (After Fix)

Every approved change should now:
1. Update change status to RELEASED
2. Increment document version (e.g., v0.1 → v0.2)
3. Set document status to IN_WORK
4. Send completion notification

## Files Modified

1. `workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkflowWorkers.java`
   - Removed duplicate worker registrations
   - Removed old handler methods
   - Added explanatory comments

## Related Files (No Changes Needed)

- `ChangeWorkerHandler.java` - Contains the correct implementation
- `change-approval.bpmn` - BPMN workflow definition (no changes needed)


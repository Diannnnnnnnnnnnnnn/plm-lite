# Change Document Version Update - Complete Implementation

## Summary

When a change is approved, the system now automatically:
1. ✅ Updates change status to RELEASED
2. ✅ Updates document status to IN_WORK
3. ✅ Increments document version (e.g., v1.0 → v1.1)
4. ✅ Creates a new editable document version

## Files Modified

### 1. Change Service
**Files:**
- `change-service/src/main/java/com/example/change_service/client/WorkflowOrchestratorClient.java`
- `change-service/src/main/java/com/example/change_service/service/ChangeService.java`
- `change-service/src/main/java/com/example/change_service/service/ChangeServiceDev.java`

**Changes:**
- Added `documentId` field to `StartChangeApprovalRequest` DTO
- Pass document ID when starting change approval workflow

### 2. Workflow Orchestrator
**Files:**
- `workflow-orchestrator/src/main/java/com/example/plm/workflow/controller/WorkflowController.java`
- `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`
- `workflow-orchestrator/src/main/java/com/example/plm/workflow/client/DocumentServiceClient.java`
- `workflow-orchestrator/src/main/java/com/example/plm/workflow/client/TaskServiceClient.java`
- `workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkerHandler.java`

**Changes:**

#### WorkflowController & WorkflowService
- Accept `documentId` parameter in change approval workflow
- Include document ID in workflow variables

#### DocumentServiceClient
- Added `initiateChangeBasedEdit()` method to call document service

#### TaskServiceClient
- Added `updateTaskWorkflowJobKey()` method for automatic workflow continuation

#### ChangeWorkerHandler
**Critical Enhancement - `update-change-status` worker:**
```java
// 1. Update change status (existing)
changeServiceClient.updateStatus(changeId, statusUpdate);

// 2. NEW: When approved, update document
if ("RELEASED".equals(newStatus) && documentId != null) {
    documentServiceClient.initiateChangeBasedEdit(documentId, changeId, creator);
    // Document: RELEASED → IN_WORK, v1.0 → v1.1
}
```

**Fixed Workflow Continuation - `wait-for-change-review` worker:**
```java
// Update task with workflow job key for automatic continuation
taskServiceClient.updateTaskWorkflowJobKey(taskId, job.getKey());
```

This ensures that when the task is approved, the workflow automatically continues instead of relying on polling.

### 3. Task Service
**File:**
- `task-service/src/main/java/com/example/task_service/TaskController.java`

**Changes:**
- Added `PUT /api/tasks/{id}/workflow-job-key` endpoint
- Allows workflow to set job key for automatic continuation when task is completed

## How It Works

### Complete Flow:

1. **User creates a change** for a RELEASED document (v1.0)
   - Change status: IN_WORK
   - Document: Still RELEASED v1.0

2. **User submits change for review**
   - Change status: IN_REVIEW
   - Workflow starts with: `{changeId, changeTitle, creator, reviewerId, documentId}`
   - Task created in task-service

3. **Workflow waits for approval**
   - `wait-for-change-review` worker activates
   - Sets workflow job key on the task
   - Polls task status

4. **Reviewer approves the task**
   - Task status: PENDING → COMPLETED
   - Task decision: APPROVED
   - Task service automatically completes workflow job (because workflowJobKey is set)

5. **Workflow continues automatically**
   - Gateway routes to approval path
   - `update-change-status` worker executes:
     - Updates change status: IN_REVIEW → RELEASED
     - Calls document service: `initiateChangeBasedEdit()`
       - Old document (v1.0): Marked inactive (archived)
       - New document (v1.1): Created with IN_WORK status, marked active

6. **Result:**
   - Change: RELEASED ✅
   - Old Document: RELEASED v1.0 (inactive/archived) ✅
   - New Document: IN_WORK v1.1 (active/editable) ✅

## Key Improvements

### Before This Fix:
- ❌ Change approval only updated change status
- ❌ Document remained RELEASED, version unchanged
- ❌ Manual intervention required to edit document

### After This Fix:
- ✅ Change approval updates both change AND document
- ✅ Document automatically becomes editable (IN_WORK)
- ✅ Version automatically increments
- ✅ Fully automated workflow

### Workflow Continuation Fix:
- **Before:** Relied on polling, often exceeded retry limits
- **After:** Uses automatic job completion via `workflowJobKey`
  - When task is completed, task-service calls workflow-orchestrator
  - Workflow immediately continues without polling
  - More reliable and efficient

## Testing

### Test Scenario:

1. Create a RELEASED document:
   ```bash
   POST /api/v1/documents
   # Then submit for review and approve to get RELEASED v1.0
   ```

2. Create a change for that document:
   ```bash
   POST /api/changes
   {
     "title": "Update Document",
     "changeDocument": "{documentId}",
     "changeReason": "Testing",
     ...
   }
   ```

3. Submit change for review:
   ```bash
   POST /api/changes/{changeId}/submit
   {
     "reviewerIds": ["1"]
   }
   ```

4. Approve the task:
   ```bash
   PUT /api/tasks/{taskId}/status
   {
     "status": "COMPLETED",
     "approved": "APPROVED",
     "decision": "APPROVED"
   }
   ```

5. **Verify Results:**
   - Change status: `RELEASED` ✅
   - New document created with:
     - Status: `IN_WORK` ✅
     - Version: Incremented (e.g., v1.1) ✅

### Expected Logs:

**Workflow Orchestrator:**
```
🔧 Worker: wait-for-change-review
   ✓ Updated task with workflow job key: {jobKey}
   ✓ Change APPROVED by reviewer
🔧 Worker: update-change-status
   Change ID: {changeId}
   New Status: RELEASED
   Document ID: {documentId}
   ✓ Updated change to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document {documentId} updated: status → IN_WORK, version incremented
```

**Document Service:**
```
INFO: Change-based editing initiated for document {documentId} (Change #{changeId}). 
      New editable version {newVersionId} created.
```

## Manual Testing (PowerShell Scripts)

Created helper scripts in project root:
- `check-and-approve-task.ps1` - Check and approve tasks
- `check-workflow-status.ps1` - Manual fallback if workflow fails
- `verify-new-document.ps1` - Verify document version increment

## Architecture

```
Change Approval Flow:
┌─────────────────┐
│  User Approves  │
│      Task       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│   Task Service          │
│  - Sets decision        │
│  - Completes workflow   │
│    job (auto)           │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Workflow Continues     │
│  - update-change-status │
│    worker triggered     │
└────────┬────────────────┘
         │
         ├──────────────────────┐
         │                      │
         ▼                      ▼
┌────────────────┐    ┌──────────────────────┐
│ Change Service │    │  Document Service    │
│ Status→RELEASED│    │  Status→IN_WORK      │
└────────────────┘    │  Version→v1.1        │
                      │  (new doc created)   │
                      └──────────────────────┘
```

## Known Issues & Solutions

### Issue: Workflow doesn't continue after approval
**Cause:** Task doesn't have workflow job key set
**Solution:** The `wait-for-change-review` worker now sets it automatically

### Issue: Polling exceeds retry limit
**Cause:** Zeebe gives up after ~3 failed polling attempts
**Solution:** Use automatic job completion instead of polling (implemented)

### Issue: 500 error when accessing task
**Cause:** Task service may have issues with specific task IDs
**Solution:** Use the `/api/tasks` endpoint to list all tasks and find the correct one

## Rollback Instructions

If issues occur:

1. Revert changes in `ChangeWorkerHandler.java`:
   - Remove document update logic from `update-change-status` worker
   - Remove workflow job key update from `wait-for-change-review` worker

2. Revert workflow variable changes:
   - Remove `documentId` parameter from workflow chain
   - Restart affected services

3. Use manual workflow for document updates:
   - Approve change manually
   - Call document service endpoint manually:
     ```
     POST /api/v1/documents/{id}/initiate-change-edit?changeId={changeId}&user={user}
     ```

## Success Criteria

✅ Change status updates to RELEASED when approved
✅ Document status changes to IN_WORK automatically  
✅ Document version increments automatically
✅ New editable document version is created
✅ Old document version is preserved as inactive
✅ Workflow completes without manual intervention
✅ No polling retry limit errors

## Next Steps

1. **Test thoroughly** with various scenarios
2. **Monitor logs** for any errors during change approval
3. **Verify** document history is being logged correctly
4. Consider adding **notifications** when document becomes editable
5. Add **UI indicators** showing the new editable version




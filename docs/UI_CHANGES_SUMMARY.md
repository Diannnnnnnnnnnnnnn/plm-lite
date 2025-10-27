# UI Changes Summary - Two-Stage Review Fix

## ✅ All UI Updates Complete!

### Files Modified

1. **`frontend/src/services/taskService.js`**
   - Updated `updateTaskStatus()` method to accept `approved` and `comments` parameters
   - Now sends the `approved` flag to the backend for workflow integration

2. **`frontend/src/components/Tasks/ReviewTasks.js`**
   - Updated `handleSubmitReview()` to call both:
     - `addTaskSignoff()` - for tracking in Neo4j
     - `updateTaskStatus()` - for workflow completion with `approved` flag
   - Approval: passes `approved: true`
   - Rejection: passes `approved: false`

3. **`frontend/src/components/Tasks/TaskManager.js`**
   - Updated `handleApproveReview()` for both change and document approvals
   - Updated `handleDeclineReview()` for both change and document rejections
   - All now pass the `approved` parameter to `updateTaskStatus()`

4. **`plm-common/src/main/java/com/example/plm/common/model/Status.java`**
   - Added new status: `IN_TECHNICAL_REVIEW`
   - This status is used between initial review and technical review

---

## What Changed in the Code

### Before (Not Working):
```javascript
// ReviewTasks.js - OLD
await taskService.addTaskSignoff(
  selectedTask.id,
  userId,
  reviewAction,
  reviewComment
);
// ❌ Workflow doesn't proceed - missing status update!
```

### After (Working):
```javascript
// ReviewTasks.js - NEW
await taskService.addTaskSignoff(
  selectedTask.id,
  userId,
  reviewAction,
  reviewComment
);

// ✅ Update task status with approved flag - triggers workflow!
const isApproved = reviewAction === 'APPROVED';
await taskService.updateTaskStatus(
  selectedTask.id,
  'COMPLETED',
  isApproved,  // ← KEY: Passes approval decision
  reviewComment
);
```

---

## How It Works Now

### When User Clicks "Approve":

1. **Frontend** (`ReviewTasks.js`):
   ```javascript
   handleSubmitReview() {
     // Step 1: Record the signoff
     await taskService.addTaskSignoff(taskId, userId, 'APPROVED', comment);
     
     // Step 2: Update status and trigger workflow
     await taskService.updateTaskStatus(taskId, 'COMPLETED', true, comment);
   }
   ```

2. **Task Service** receives:
   ```http
   PUT /tasks/{id}/status
   {
     "status": "COMPLETED",
     "approved": "true",  ← Workflow uses this!
     "comments": "Looks good"
   }
   ```

3. **Task Service** automatically:
   - Updates task status to COMPLETED
   - Calls workflow-orchestrator to complete the job
   - Passes `approved: true` to the workflow

4. **Workflow** proceeds:
   - Initial review complete → Status changes to `IN_TECHNICAL_REVIEW`
   - Technical review complete → Checks `approved` flag
   - If `approved=true` → Status changes to `RELEASED`
   - If `approved=false` → Status changes to `IN_WORK`

---

## What You Need to Do

### 1. Restart Workflow-Orchestrator ⚡ CRITICAL
The updated BPMN workflow must be deployed:

```bash
cd workflow-orchestrator
# Stop current instance
# Then start:
mvn spring-boot:run
```

Wait for this message in the logs:
```
✅ Deployed process: document-approval-with-review
```

### 2. Restart Document-Service (Recommended)
So it picks up the new `IN_TECHNICAL_REVIEW` status from plm-common:

```bash
cd document-service
# Stop current instance
# Then start:
mvn spring-boot:run
```

### 3. Test the Workflow! 🧪

Once services are restarted:

1. **Start a new two-stage review**:
   - Go to Documents → Submit a document for review
   - Select Initial Reviewer (e.g., User 1)
   - Select Technical Reviewer (e.g., User 2)
   - Submit

2. **Initial Reviewer Approves**:
   - Login as User 1
   - Go to Review Tasks
   - Click "Approve" on the document
   - ✅ Document status should change to `IN_TECHNICAL_REVIEW`
   - ✅ User 2 should now see the task

3. **Technical Reviewer Approves**:
   - Login as User 2
   - Go to Review Tasks
   - Click "Approve" on the document
   - ✅ Document status should change to `RELEASED`
   - ✅ Workflow completes

4. **Test Rejection**:
   - Start another workflow
   - Have either reviewer click "Reject"
   - ✅ Document status should change to `IN_WORK`
   - ✅ Workflow completes

---

## Status Flow

```
Document Created → IN_WORK

Submit for Review → IN_REVIEW

Initial Reviewer Approves → IN_TECHNICAL_REVIEW ← NEW!

Technical Reviewer Approves → RELEASED ✅
Technical Reviewer Rejects → IN_WORK ❌
```

---

## Files You Don't Need to Change

- ✅ Frontend is fully updated
- ✅ Status enum is updated
- ✅ BPMN workflow is updated
- ✅ Task service already has the logic

You only need to **restart the services**!

---

## Troubleshooting

### If workflow still stuck after technical review:

**Check Frontend Network Tab:**
Look for the `PUT /tasks/{id}/status` request. It should include:
```json
{
  "status": "COMPLETED",
  "approved": "true"  ← Make sure this is present!
}
```

**Check Task-Service Logs:**
Should see:
```
🔄 Auto-completing workflow job: 2251799814137987
✅ Workflow job completed successfully!
```

**Check Workflow-Orchestrator Logs:**
Should see:
```
✅ Job completed: 2251799814137987
🔄 Updating document status: {docId} -> IN_TECHNICAL_REVIEW
```

### If document status not changing after initial review:

**Make sure workflow-orchestrator was restarted** to deploy the updated BPMN!

---

## Summary of Changes

| File | Change | Purpose |
|------|--------|---------|
| `taskService.js` | Added `approved` and `comments` parameters | Send approval decision to backend |
| `ReviewTasks.js` | Call `updateTaskStatus` with `approved` flag | Trigger workflow completion |
| `TaskManager.js` | Updated all review completions | Consistent workflow integration |
| `Status.java` | Added `IN_TECHNICAL_REVIEW` | Support intermediate status |
| `plm-common` | Compiled and installed | Make status available to all services |

---

## Next Steps

1. ⏳ **You're restarting workflow-orchestrator** (in progress)
2. ✅ **UI updates are complete**
3. ✅ **Status enum is updated**
4. 🧪 **Test the workflow** (next step after restart)

Good luck! 🚀




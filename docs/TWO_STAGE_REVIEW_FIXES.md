# Two-Stage Review Workflow Fixes

## Issues Fixed

### Issue 1: Document Status Not Changing After First Review ✅
**Problem:** After the initial reviewer completes their task, the document status remains "INREVIEW" instead of changing to show it's now in technical review.

**Solution:** Added an intermediate status update task in the BPMN workflow that runs after the initial review completes.

**Workflow Flow (Updated):**
```
Start → Create Tasks → Wait for Initial Review 
      → Update Status: IN_TECHNICAL_REVIEW ← NEW!
      → Wait for Technical Review 
      → Decision Gateway
      → Update Status: RELEASED or IN_WORK
      → End
```

**Status Changes:**
- When workflow starts: `INREVIEW` (existing document status)
- After initial review completes: `IN_TECHNICAL_REVIEW` (new intermediate status)
- After technical review completes: `RELEASED` (approved) or `IN_WORK` (rejected)

---

### Issue 2: Workflow Not Proceeding After Second Review ✅
**Problem:** After the technical reviewer completes their task, the workflow gets stuck and doesn't proceed to update the final status.

**Root Cause:** The workflow's decision gateway needs an `approved` variable to decide between RELEASED and IN_WORK status, but the UI wasn't passing this variable when completing tasks.

**Solution:** The UI must pass the `approved` parameter when updating task status.

---

## How to Complete Review Tasks Properly

### API Endpoint
When a reviewer approves or rejects a document, the UI must call:

```
PUT http://localhost:8082/tasks/{taskId}/status
Content-Type: application/json
```

### Request Body Format

**For APPROVAL:**
```json
{
  "status": "COMPLETED",
  "approved": "true",
  "comments": "Looks good! Approved."
}
```

**For REJECTION:**
```json
{
  "status": "COMPLETED",
  "approved": "false",
  "comments": "Needs revision. Please fix XYZ."
}
```

### What Happens Automatically

When task status is updated to "COMPLETED":

1. ✅ Task-service updates the task in the database
2. ✅ Task-service automatically calls workflow-orchestrator to complete the job
3. ✅ Workflow-orchestrator receives the `approved` variable
4. ✅ Workflow proceeds to the decision gateway
5. ✅ If `approved=true` → Updates document to RELEASED
6. ✅ If `approved=false` → Updates document to IN_WORK
7. ✅ Workflow completes

---

## Implementation in Your UI

### Option 1: Update Existing Review Dialog (Recommended)

If you have a review dialog where users click "Approve" or "Reject", update it to include the `approved` parameter:

**JavaScript Example:**
```javascript
async function completeReviewTask(taskId, isApproved, comments) {
  const response = await fetch(`http://localhost:8082/tasks/${taskId}/status`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      status: 'COMPLETED',
      approved: isApproved ? 'true' : 'false',  // ← KEY CHANGE!
      comments: comments
    })
  });
  
  if (response.ok) {
    console.log('✅ Task completed and workflow proceeding');
  }
}

// When user clicks "Approve" button:
document.getElementById('approveBtn').addEventListener('click', () => {
  completeReviewTask(taskId, true, 'Approved');
});

// When user clicks "Reject" button:
document.getElementById('rejectBtn').addEventListener('click', () => {
  completeReviewTask(taskId, false, 'Needs revision');
});
```

**React Example:**
```javascript
const handleApprove = async () => {
  await axios.put(`http://localhost:8082/tasks/${taskId}/status`, {
    status: 'COMPLETED',
    approved: 'true',  // ← KEY CHANGE!
    comments: approvalComments
  });
  // Refresh task list
};

const handleReject = async () => {
  await axios.put(`http://localhost:8082/tasks/${taskId}/status`, {
    status: 'COMPLETED',
    approved: 'false',  // ← KEY CHANGE!
    comments: rejectionComments
  });
  // Refresh task list
};
```

---

## Testing the Fixes

### Test Scenario: Full Two-Stage Review

1. **Start Workflow:**
   ```bash
   POST http://localhost:8086/api/workflows/documents/start
   {
     "documentId": "test-doc-123",
     "masterId": "DOC-001",
     "version": "v1.0",
     "creator": "alice",
     "initialReviewer": "1",
     "technicalReviewer": "2"
   }
   ```

2. **Check Initial Status:**
   - Document status: `INREVIEW`
   - Initial reviewer (user 1) sees task in their list

3. **Initial Reviewer Approves:**
   ```bash
   PUT http://localhost:8082/tasks/{initialTaskId}/status
   {
     "status": "COMPLETED",
     "approved": "true",
     "comments": "Initial review passed"
   }
   ```

4. **Check Intermediate Status:**
   - ✅ Document status should change to: `IN_TECHNICAL_REVIEW`
   - ✅ Technical reviewer (user 2) sees task in their list
   - ✅ Initial review task disappears from user 1's list

5. **Technical Reviewer Approves:**
   ```bash
   PUT http://localhost:8082/tasks/{technicalTaskId}/status
   {
     "status": "COMPLETED",
     "approved": "true",
     "comments": "Technical review passed"
   }
   ```

6. **Check Final Status:**
   - ✅ Document status should change to: `RELEASED`
   - ✅ Workflow completes
   - ✅ Both tasks are marked COMPLETED

### Test Scenario: Rejection at Technical Review

Same as above, but in step 5:

```bash
PUT http://localhost:8082/tasks/{technicalTaskId}/status
{
  "status": "COMPLETED",
  "approved": "false",  ← REJECTED
  "comments": "Technical issues found"
}
```

**Expected Result:**
- ✅ Document status changes to: `IN_WORK`
- ✅ Workflow completes
- ✅ Document can be revised and resubmitted

---

## Changes Made

### Files Modified

1. **workflow-orchestrator/src/main/resources/bpmn/document-approval-with-review.bpmn**
   - Added `ServiceTask_UpdateToTechnicalReview` task
   - Updates status to `IN_TECHNICAL_REVIEW` after initial review
   - Added sequence flow: `Flow_InitialReviewToStatusUpdate`

2. **task-service/src/main/java/com/example/task_service/Task.java**
   - Increased description column from 255 to 1000 characters
   - Supports longer workflow-generated descriptions

### Workflow Need to be Redeployed

After updating the BPMN file, you need to restart workflow-orchestrator:

```bash
cd workflow-orchestrator
mvn spring-boot:run
```

The updated workflow will be automatically deployed on startup.

---

## Important Notes

1. **The `approved` parameter is critical** - Without it, the workflow will default to approved=true
2. **Document Service Status**: Make sure your document-service has the `IN_TECHNICAL_REVIEW` status defined
3. **Existing Workflows**: This fix applies to new workflow instances. Stuck workflows may need manual completion
4. **Task Service Auto-Sync**: The automatic workflow completion only works if the task has a `workflowJobKey` set (which it should have automatically)

---

## Troubleshooting

### Workflow Still Stuck After Technical Review?

**Check if the `approved` variable is being passed:**

1. Look at task-service logs when you mark task as COMPLETED
2. You should see:
   ```
   🔄 Auto-completing workflow job: 2251799814137987
   ✅ Workflow job completed successfully!
   ```

3. If not, check:
   - Is the UI sending the `approved` parameter?
   - Does the task have a `workflowJobKey`?
   - Is workflow-orchestrator running?

### Document Status Not Changing?

1. Check workflow-orchestrator logs for:
   ```
   🔄 Updating document status: {docId} -> IN_TECHNICAL_REVIEW
   ✓ Document status updated successfully
   ```

2. If missing, check:
   - Is the updated BPMN deployed? (Restart workflow-orchestrator)
   - Is document-service running?
   - Does document-service have the status defined?

---

## Next Steps

1. ✅ Restart workflow-orchestrator to deploy the updated BPMN
2. ✅ Update your UI to pass the `approved` parameter
3. ✅ Test the full two-stage review flow
4. ✅ Add `IN_TECHNICAL_REVIEW` status to your document-service if not already present


# Document Approval Workflow - Review Completion Guide

## ✅ Status: FULLY OPERATIONAL

### Services Running
- **task-service**: Port 8082 ✅
- **workflow-orchestrator**: Port 8086 ✅

### BPMN Deployed
- ✅ **document-approval.bpmn** - Deployed successfully
- ✅ **change-approval.bpmn** - Deployed successfully

### Workers Registered
- ✅ `create-approval-task` - Creates review tasks
- ✅ `wait-for-review` - Waits for reviewer decision (NEW!)
- ✅ `update-status` - Updates document status
- ✅ `notify-completion` - Sends notifications

---

## 🔄 New Workflow Flow

```
[Document Submitted]
        ↓
[Create Approval Tasks] ← Creates task in task-service
        ↓
[Wait For Review] ← Worker waits for completion (jobKey logged)
        ↓
   [Reviewer uses UI to approve/reject]
        ↓
   [UI calls API to complete the job]
        ↓
    ◇ Approved? ◇
   ┌─────┴─────┐
  YES          NO
   │            │
   ▼            ▼
[APPROVED]  [REJECTED]
   │            │
   ▼            ▼
[Notify]    [Notify]
   │            │
   └─────┬──────┘
         ▼
       [End]
```

---

## 🎯 How to Complete a Review

### Step 1: Start Workflow
When a workflow starts, you'll see output like:
```
📋 Creating approval tasks for document: b0cd44a7-...
   ✓ Created task ID 134 for vivi

⏳ Waiting for review completion for document: b0cd44a7-...
   Job Key: 6755399441091234
   Process Instance: 2251799813964774
   ℹ️  Review this document and call POST /api/workflows/jobs/{jobKey}/complete
```

**Key Information to Note:**
- **Job Key**: `6755399441091234` (example)
- **Task ID**: `134` (for the reviewer to see in UI)
- **Process Instance**: `2251799813964774`

---

### Step 2: Reviewer Reviews Document
Reviewer sees the task (ID 134) in their task list and uses your existing review dialog to:
- Read the document
- Make a decision (Approve or Reject)

---

### Step 3: Complete the Workflow Job

When the reviewer submits their decision, your frontend should call:

**API Endpoint:**
```
POST http://localhost:8086/api/workflows/jobs/{jobKey}/complete
```

**Request Body:**
```json
{
  "approved": true,     // or false for rejection
  "comments": "Looks good! Approved."  // optional
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8086/api/workflows/jobs/6755399441091234/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Approved"}'
```

**Example with JavaScript/Fetch:**
```javascript
const jobKey = 6755399441091234; // Get this from task metadata

fetch(`http://localhost:8086/api/workflows/jobs/${jobKey}/complete`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    approved: true,  // or false
    comments: 'Looks good!'
  })
})
.then(response => response.json())
.then(data => console.log('Workflow completed:', data));
```

---

### Step 4: Workflow Continues Automatically

After the API call:
1. **Wait For Review** job completes with the `approved` variable
2. **Decision Gateway** evaluates `approved` 
3. If `approved = true` → Updates status to **APPROVED**
4. If `approved = false` → Updates status to **REJECTED**
5. **Notification** sent to document creator
6. **Workflow ends**

---

## 📊 Process Instance: 2251799813964774

For the stuck process instance you mentioned, you need to:

1. **Find the Job Key**
   Check the workflow-orchestrator logs for:
   ```
   ⏳ Waiting for review completion for document: ...
      Job Key: XXXXX
   ```

2. **Complete the Job**
   ```bash
   curl -X POST http://localhost:8086/api/workflows/jobs/{JOB_KEY}/complete \
     -H "Content-Type: application/json" \
     -d '{"approved": true}'
   ```

---

## 🔧 Integration with Your UI

### Option 1: Store Job Key with Task

When creating the task, you could store the job key as task metadata:

```java
// In handleWaitForReview worker
task.setMetadata(Map.of("jobKey", jobKey));
```

Then your UI can retrieve it when displaying the review dialog.

### Option 2: Query Active Jobs

You could create an endpoint to query active jobs for a specific process instance:

```
GET /api/workflows/instances/{processInstanceKey}/active-jobs
```

This would return the job key that needs to be completed.

---

## 🎯 Complete Workflow Example

### 1. Start Approval Workflow
```bash
POST http://localhost:8086/api/workflows/documents/start-approval
{
  "documentId": "abc-123",
  "masterId": "DOC-001",
  "version": "v1.0",
  "creator": "john",
  "reviewerIds": [2]
}
```

**Response:**
```json
{
  "processInstanceKey": "2251799813964774",
  "status": "STARTED"
}
```

### 2. System Creates Task
- Task ID 134 created for reviewer (user ID 2)
- Reviewer sees task in their task list
- Job key logged: `6755399441091234`

### 3. Reviewer Approves
UI calls:
```bash
POST http://localhost:8086/api/workflows/jobs/6755399441091234/complete
{
  "approved": true,
  "comments": "Approved"
}
```

### 4. Workflow Completes
- Document status → APPROVED
- Creator notified
- Workflow ends

---

## 🐛 Troubleshooting

### Process Stuck at "Wait For Review"
**Problem**: Review was done in UI but workflow didn't continue

**Solution**: Call the complete endpoint with the job key:
```bash
POST /api/workflows/jobs/{jobKey}/complete
{
  "approved": true/false
}
```

### Can't Find Job Key
**Solution**: Check workflow-orchestrator logs:
```bash
Get-Content workflow-orchestrator/console-out.log | Select-String "Job Key"
```

### Job Timed Out
**Problem**: No action taken within 24 hours

**Solution**: Job will auto-timeout. Start a new workflow.

---

## 📝 Next Steps

1. ✅ **Update your review dialog** to call the complete endpoint when reviewer submits
2. ✅ **Store job key** with the task metadata for easy retrieval
3. ✅ **Test the flow** end-to-end
4. ✅ **Monitor logs** to see the workflow progress

---

**Status**: ✅ Ready for testing!  
**Date**: October 20, 2025, 20:17  
**Version**: Final





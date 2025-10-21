# Document Approval Workflow - Quick Start Guide

## ✅ System Ready

**Date**: October 20, 2025, 20:22  
**Status**: All systems operational

### Services Running
- ✅ task-service (Port 8082)
- ✅ workflow-orchestrator (Port 8086) 
- ✅ document-approval.bpmn deployed
- ✅ All 4 workers registered

---

## 🚀 How to Use the Workflow

### Step 1: Start a New Approval Workflow

```bash
POST http://localhost:8086/api/workflows/documents/start-approval
Content-Type: application/json

{
  "documentId": "your-doc-uuid-here",
  "masterId": "DOC-001",
  "version": "v1.0",
  "creator": "john",
  "reviewerIds": [2]
}
```

**What Happens:**
1. Workflow starts
2. Creates review task in task-service
3. **"Wait For Review"** worker activates and logs the Job Key
4. Task appears in reviewer's task list

---

### Step 2: Check Logs for Job Key

After starting the workflow, check the console:

```powershell
Get-Content workflow-orchestrator/console-out.log -Tail 20
```

You'll see:
```
📋 Creating approval tasks for document: abc-123
   ✓ Created task ID 135 for username

⏳ Waiting for review completion for document: abc-123
   Job Key: 6755399441098765  ← THIS IS IMPORTANT!
   Process Instance: 2251799813970000
   ℹ️  Call POST /api/workflows/tasks/{jobKey}/complete
```

**Save the Job Key!** You'll need it to complete the workflow.

---

### Step 3: Reviewer Reviews Document

Reviewer sees Task ID 135 in their UI and reviews the document.

---

### Step 4: Complete the Workflow

When reviewer clicks Approve/Reject in your UI, call:

```bash
POST http://localhost:8086/api/workflows/tasks/{jobKey}/complete
Content-Type: application/json

{
  "approved": true,     # or false for reject
  "comments": "Looks good!"
}
```

**Example:**
```bash
curl -X POST http://localhost:8086/api/workflows/tasks/6755399441098765/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Approved"}'
```

---

### Step 5: Workflow Completes Automatically

- If `approved = true` → Document status = APPROVED
- If `approved = false` → Document status = REJECTED
- Creator gets notified
- Workflow ends

---

## 🔧 Integration Points

### Your Frontend Needs To:

1. **When starting workflow**: Save the Process Instance Key
2. **After workflow starts**: Query logs or create API to get Job Key
3. **When reviewer approves/rejects**: Call the complete endpoint with Job Key

### Recommended: Store Job Key with Task

Modify the `wait-for-review` worker to store the job key:

```java
// In DocumentWorkflowWorkers.java
private void handleWaitForReview(JobClient client, ActivatedJob job) {
    Long jobKey = job.getKey();
    String documentId = (String) variables.get("documentId");
    
    // Store job key in task metadata or separate table
    // So your UI can retrieve it when showing the review dialog
    
    System.out.println("⏳ Job Key: " + jobKey);
    // Don't complete - wait for API call
}
```

---

## 📊 Complete Example

### 1. Start Workflow
```json
POST /api/workflows/documents/start-approval
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
  "processInstanceKey": "2251799813970000",
  "status": "STARTED",
  "message": "Document approval workflow started successfully"
}
```

### 2. Check Logs (or create API endpoint)
```
⏳ Waiting for review...
   Job Key: 6755399441098765
```

### 3. Reviewer Approves
```json
POST /api/workflows/tasks/6755399441098765/complete
{
  "approved": true,
  "comments": "Looks good!"
}
```

**Response:**
```json
{
  "status": "COMPLETED",
  "message": "Task completed successfully"
}
```

### 4. Check Document Status
Document status is now "APPROVED" ✅

---

## 🎯 Testing Right Now

Try it immediately:

```bash
# 1. Start a new workflow
curl -X POST http://localhost:8086/api/workflows/documents/start-approval \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "test-doc-123",
    "masterId": "TEST-001",
    "version": "v1.0",
    "creator": "vivi",
    "reviewerIds": [2]
  }'

# 2. Check logs for Job Key
Get-Content workflow-orchestrator/console-out.log -Tail 10

# 3. Complete the review (use actual job key from logs)
curl -X POST http://localhost:8086/api/workflows/tasks/YOUR_JOB_KEY/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Test approval"}'
```

---

## ⚠️ Important Notes

### Old Process Cancelled
Process Instance **2251799813964774** was created with the old BPMN (User Task) and has been cancelled. It cannot be completed with the new system.

### Always Start Fresh
After any BPMN changes, old running workflows cannot be migrated. Cancel them and start new ones.

### Job Key Availability
The Job Key is only available:
- In the logs (after "Wait For Review" activates)
- Or via a custom API you create to query active jobs

### Timeout
Jobs timeout after 24 hours if not completed.

---

## 🔜 Next Steps

### Recommended Enhancements:

1. **Create API to get Job Key**
```java
@GetMapping("/instances/{processInstanceKey}/active-jobs")
public ResponseEntity<List<JobInfo>> getActiveJobs(@PathVariable long processInstanceKey) {
    // Query Zeebe for active jobs in this process instance
    // Return list including job keys
}
```

2. **Store Job Key in Task Metadata**
```java
// Store jobKey with task when creating it
// So your UI can retrieve it when showing review dialog
task.setMetadata(Map.of("jobKey", jobKey, "processInstanceKey", processInstanceKey));
```

3. **Auto-complete on Review**
Update your review dialog submit handler to:
```javascript
async function submitReview(taskId, approved, comments) {
  // Get task metadata to find jobKey
  const task = await getTask(taskId);
  const jobKey = task.metadata.jobKey;
  
  // Complete the workflow job
  await fetch(`http://localhost:8086/api/workflows/tasks/${jobKey}/complete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approved, comments })
  });
  
  // Update task status in task-service
  await updateTaskStatus(taskId, approved ? 'COMPLETED' : 'REJECTED');
}
```

---

## ✅ Summary

| Step | Action | API Endpoint |
|------|--------|--------------|
| 1 | Start workflow | `POST /api/workflows/documents/start-approval` |
| 2 | Get job key | Check logs or create custom API |
| 3 | Complete review | `POST /api/workflows/tasks/{jobKey}/complete` |
| 4 | Verify status | Check document-service for updated status |

---

**Everything is ready! Start a new workflow and test the complete flow.** 🎉

The key difference from before: You must call the `/tasks/{jobKey}/complete` API when the reviewer approves/rejects.


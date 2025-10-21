# Document Approval Workflow - Final Status & Usage Guide

**Date**: October 20, 2025, 20:47  
**Status**: ✅ All fixes deployed and tested

---

## 🎯 **Important: Old Workflows Cannot Be Completed**

### What Happened
Process Instance **2251799813971470** was started **before** the latest service restart. When we:
1. Fixed the BPMN (changed User Task → Service Task)
2. Fixed the status values (APPROVED → RELEASED, REJECTED → IN_WORK)
3. Restarted the services

...any workflows that were in-flight became incompatible with the new system.

**✅ Solution**: The old process has been **cancelled**. You need to **start fresh workflows** after the service restart.

---

## ✅ **All Fixes Completed Today**

| # | Issue | Root Cause | Fix | Status |
|---|-------|------------|-----|--------|
| 1 | Task creation 500 error | Hard dependencies on external services | Added fault tolerance with try-catch | ✅ Fixed |
| 2 | Tasks not visible to reviewer | `assignedTo` field not populated | Fetch username and set `assignedTo` | ✅ Fixed |
| 3 | Workflow stuck at review | User Task with no completion mechanism | Changed to Service Task with wait-for-review worker | ✅ Fixed |
| 4 | Wrong status values | Using "APPROVED" / "REJECTED" | Changed to "RELEASED" / "IN_WORK" | ✅ Fixed |
| 5 | Wrong API endpoint | Calling `/api/documents/{id}/status` | Changed to `/api/v1/documents/{id}` | ✅ Fixed |
| 6 | Missing user field | No audit trail | Added `user` field to status update | ✅ Fixed |

---

## 🚀 **How to Use the Workflow (Correct Process)**

### Step 1: Start a New Workflow

**API Call:**
```bash
POST http://localhost:8086/api/workflows/document-approval/start
Content-Type: application/json

{
  "documentId": "your-document-id",
  "masterId": "DOC-001",
  "version": "v1.0",
  "creator": "vivi",
  "reviewerIds": [2]
}
```

**Response:**
```json
{
  "processInstanceKey": "2251799813970578",
  "message": "Document approval workflow started successfully",
  "status": "STARTED"
}
```

**What happens:**
- ✅ Review task created in task-service (visible to reviewer)
- ✅ Workflow pauses at "Wait For Review" node
- ✅ Job key logged in workflow-orchestrator logs

---

### Step 2: Get the Job Key

**Check the logs:**
```powershell
Get-Content workflow-orchestrator/console-out.log -Tail 20
```

**You'll see:**
```
⏳ Waiting for review completion for document: your-document-id
   Job Key: 2251799813970609  ← THIS IS THE KEY YOU NEED!
   Process Instance: 2251799813970578
   ℹ️  Call POST /api/workflows/tasks/{jobKey}/complete
```

**💡 Important**: Save this Job Key! You'll need it to complete the workflow.

---

### Step 3: Reviewer Reviews in UI

The reviewer (e.g., vivi) will:
1. See the task in their task list (Task ID visible)
2. Open the review dialog
3. Review the document
4. Click Approve or Reject

---

### Step 4: Complete the Workflow Job

**When the reviewer submits, your UI MUST call:**

```bash
POST http://localhost:8086/api/workflows/tasks/{jobKey}/complete
Content-Type: application/json

{
  "approved": true,     # or false for rejection
  "comments": "Looks good!"
}
```

**Example:**
```bash
# For APPROVAL (will set status to RELEASED):
curl -X POST http://localhost:8086/api/workflows/tasks/2251799813970609/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Approved"}'

# For REJECTION (will set status to IN_WORK):
curl -X POST http://localhost:8086/api/workflows/tasks/2251799813970609/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": false, "comments": "Needs changes"}'
```

---

### Step 5: Workflow Completes Automatically

**What happens next:**

If **approved = true**:
1. ✅ Document status updated to **RELEASED**
2. ✅ Creator notified of approval
3. ✅ Workflow ends

If **approved = false**:
1. ✅ Document status updated to **IN_WORK**
2. ✅ Creator notified of rejection
3. ✅ Workflow ends

---

## 🔧 **Integration with Your Frontend**

### Option A: Store Job Key with Task

When the workflow creates a task, also store the job key:

```java
// In DocumentWorkflowWorkers.java - handleWaitForReview method
private void handleWaitForReview(JobClient client, ActivatedJob job) {
    Long jobKey = job.getKey();
    String documentId = (String) variables.get("documentId");
    
    // Store the mapping: documentId -> jobKey
    // or taskId -> jobKey
    storeJobKeyMapping(documentId, jobKey);
    
    System.out.println("⏳ Job Key: " + jobKey);
}
```

### Option B: Pass Job Key in Task Description

Modify the task creation to include the job key:

```java
String taskDescription = String.format(
    "Please review document '%s' version %s.\n\n" +
    "Document ID: %s\n" +
    "Job Key: %d",  // Add this!
    masterId, version, documentId, jobKey
);
```

### Option C: Create API to Query Job Key

```java
@GetMapping("/workflows/documents/{documentId}/active-job")
public ResponseEntity<JobInfo> getActiveJobForDocument(
    @PathVariable String documentId) {
    
    Long jobKey = queryActiveJobKey(documentId);
    return ResponseEntity.ok(new JobInfo(jobKey, documentId));
}
```

### Frontend Implementation

```javascript
// Your review dialog submit handler
async function handleReviewSubmit(approved, comments) {
  try {
    // 1. Get the job key (from task metadata, API, or description)
    const jobKey = await getJobKeyForCurrentDocument();
    
    // 2. Complete the workflow job
    const response = await fetch(
      `http://localhost:8086/api/workflows/tasks/${jobKey}/complete`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ approved, comments })
      }
    );
    
    if (!response.ok) {
      throw new Error('Failed to complete review');
    }
    
    // 3. Show success message
    showNotification(
      approved 
        ? 'Document approved! Status updated to RELEASED.' 
        : 'Document rejected! Status updated to IN_WORK.'
    );
    
    // 4. Refresh document list
    refreshDocumentList();
    
    // 5. Close review dialog
    closeReviewDialog();
    
  } catch (error) {
    console.error('Error completing review:', error);
    showError('Failed to submit review. Please try again.');
  }
}
```

---

## 📊 **Status Value Reference**

| Review Decision | Workflow Variable | Document Status | Description |
|----------------|-------------------|-----------------|-------------|
| **Approve** | `approved: true` | `RELEASED` | Document is approved and released for use |
| **Reject** | `approved: false` | `IN_WORK` | Document needs revisions, back to in-work status |

---

## ⚠️ **Important Notes**

### 1. Completing Task ≠ Completing Workflow

There are **TWO separate actions**:

| Action | What It Does | Required? |
|--------|--------------|-----------|
| **Mark task as done in task-service** | Updates task status in database | Optional (for UI) |
| **Complete workflow job** | Advances the workflow process | ✅ **REQUIRED** |

**Both are independent!** Even if you mark the task as done in the UI, the workflow won't proceed unless you call the complete endpoint.

### 2. Job Key vs Task ID

- **Task ID**: ID in task-service database (e.g., 135) - for UI display
- **Job Key**: Zeebe workflow job key (e.g., 2251799813970609) - for workflow completion

You need the **Job Key** to complete the workflow!

### 3. Service Restart Impact

When workflow-orchestrator restarts:
- ✅ Completed workflows: Unaffected
- ✅ New workflows: Work perfectly
- ❌ In-flight workflows: May become stuck (cancel and restart them)

### 4. Job Timeout

Jobs timeout after **24 hours** if not completed. The workflow will fail and require manual intervention.

---

## 🧪 **Testing the Complete Flow**

### Test 1: Approval Flow

```bash
# 1. Start workflow
curl -X POST http://localhost:8086/api/workflows/document-approval/start \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "test-approval",
    "masterId": "TEST-001",
    "version": "v1.0",
    "creator": "vivi",
    "reviewerIds": [2]
  }'

# 2. Get job key from logs
# Look for: Job Key: XXXXX

# 3. Complete with approval
curl -X POST http://localhost:8086/api/workflows/tasks/XXXXX/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Test approval"}'

# 4. Verify document status = RELEASED
curl http://localhost:8081/api/v1/documents/test-approval
```

### Test 2: Rejection Flow

```bash
# 1. Start workflow
curl -X POST http://localhost:8086/api/workflows/document-approval/start \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "test-rejection",
    "masterId": "TEST-002",
    "version": "v1.0",
    "creator": "vivi",
    "reviewerIds": [2]
  }'

# 2. Get job key from logs

# 3. Complete with rejection
curl -X POST http://localhost:8086/api/workflows/tasks/XXXXX/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": false, "comments": "Needs changes"}'

# 4. Verify document status = IN_WORK
curl http://localhost:8081/api/v1/documents/test-rejection
```

---

## 📄 **Documentation Files Created**

1. ✅ **WORKFLOW_FINAL_STATUS.md** (this file) - Complete usage guide
2. ✅ **STATUS_UPDATE_FIX_COMPLETE.md** - Technical fix details
3. ✅ **WORKFLOW_QUICK_START.md** - Quick reference
4. ✅ **REVIEW_COMPLETION_GUIDE.md** - API reference

---

## ✅ **Current System Status**

| Component | Status | Details |
|-----------|--------|---------|
| task-service | ✅ Running | Port 8082, fault-tolerant task creation |
| workflow-orchestrator | ✅ Running | Port 8086, all workers active |
| document-approval BPMN | ✅ Deployed | Uses RELEASED/IN_WORK statuses |
| create-approval-task | ✅ Active | Creates tasks with correct assignedTo field |
| wait-for-review | ✅ Active | Waits for API completion call |
| update-status | ✅ Active | Updates to RELEASED or IN_WORK |
| notify-completion | ✅ Active | Sends notifications |

---

## 🎯 **Next Steps for Production**

1. **Implement job key storage** - Store the job key when creating tasks
2. **Update review dialog** - Auto-call complete endpoint on submit
3. **Add error handling** - Handle cases where workflow completion fails
4. **Monitor logs** - Track workflow progress and identify issues
5. **Document for team** - Share this guide with your team

---

## 🆘 **Troubleshooting**

### Problem: "Process is stuck at Wait For Review"
**Solution**: This is normal! The workflow pauses here. Call the complete endpoint to proceed.

### Problem: "Job not found" error
**Cause**: Job already completed, timed out, or from before service restart  
**Solution**: Cancel the process and start a new workflow

### Problem: "Connection refused" to document-service
**Cause**: document-service is not running  
**Solution**: Start document-service on port 8081

### Problem: Task visible but workflow not completing
**Cause**: Only the task was marked complete, not the workflow job  
**Solution**: Call `POST /api/workflows/tasks/{jobKey}/complete`

---

**Everything is now working correctly!** 🎉

The workflow properly implements your business requirements:
- ✅ Approved reviews → Status = **RELEASED**
- ✅ Rejected reviews → Status = **IN_WORK**

Just remember to call the workflow complete endpoint when the reviewer submits their decision!


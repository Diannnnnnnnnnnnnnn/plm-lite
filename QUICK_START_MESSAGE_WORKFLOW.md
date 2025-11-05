# Quick Start - Message-Based Change Approval

## ✅ Implementation Complete!

The change approval workflow now uses **proper message-based BPMN** instead of polling.

---

## What You Need to Do

### Step 1: Restart Services (CRITICAL!)

**You MUST restart these services to load the new code and BPMN:**

```powershell
# Stop and restart:
1. workflow-orchestrator (port 8086) ← NEW BPMN FILE!
2. task-service (port 8085) ← NEW MESSAGE LOGIC!
3. change-service (port 8084) ← Already updated
```

**Why?** The BPMN file changed from polling to message-based events.

---

### Step 2: Test with a NEW Change Request

**IMPORTANT:** Old/failed workflows won't work. You need a NEW change request.

#### Create Test Document (if needed):
```bash
POST http://localhost:8081/api/v1/documents
{
  "title": "Test Document",
  "description": "For testing",
  "stage": "PRODUCTION",
  "creator": "vivi"
}

# Submit for review and approve to get RELEASED status
```

#### Create Change:
```bash
POST http://localhost:8084/api/changes
{
  "title": "Test Message Workflow",
  "changeReason": "Testing message-based approval",
  "changeDocument": "{your-released-documentId}",
  "stage": "PRODUCTION",
  "changeClass": "MINOR",
  "product": "Test",
  "creator": "vivi"
}
```

#### Submit for Review:
```bash
POST http://localhost:8084/api/changes/{changeId}/submit
{
  "reviewerIds": ["4"]
}
```

---

### Step 3: Approve the Task

Find the task ID in the workflow-orchestrator logs, then:

```bash
PUT http://localhost:8085/api/tasks/{taskId}/status
{
  "status": "COMPLETED",
  "decision": "APPROVED",
  "comments": "Looks good"
}
```

---

### Step 4: Verify Success

#### Check Change:
```bash
GET http://localhost:8084/api/changes/{changeId}
```
**Expected:** `status: "RELEASED"` ✅

#### Check Document:
```bash
GET http://localhost:8081/api/v1/documents/{documentId}
```
**Expected (for newest version):**
- `status: "IN_WORK"` ✅
- `version: "v1.1"` (or incremented) ✅

---

## Expected Logs

### After Submit for Review:

**workflow-orchestrator:**
```
✓ Created change review task ID: XX
✓ Task linked to CHANGE: {changeId}
```

### After Approving Task:

**task-service:**
```
📨 Publishing message: change-review-completed
📨 Correlation Key (changeId): {changeId}
✅ Workflow message published successfully!
```

**workflow-orchestrator:**
```
📨 Publishing message to workflow
   ✓ Message published successfully
   
🔧 Worker: update-change-status
   ✓ Updated change to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document updated: status → IN_WORK, version incremented
```

---

## What Changed?

### Before (Broken):
- Workflow **polled** task status
- Failed after 3 retries ❌
- Workflow got stuck ❌

### After (Fixed):
- Workflow **waits for message** ⏳
- Task service **publishes message** when approved 📨
- Workflow **continues automatically** ✅
- **No polling, no failures!** ✅

---

## Files Modified

- ✅ `change-approval.bpmn` - Message event instead of service task
- ✅ `WorkflowService.java` - Added publishMessage()
- ✅ `WorkflowController.java` - Added /messages/publish endpoint  
- ✅ `ChangeWorkerHandler.java` - Removed polling worker
- ✅ `TaskController.java` - Publishes message on completion
- ✅ `WorkflowOrchestratorClient.java` - Added publishMessage()

---

## Need Help?

**Workflow not continuing?**
1. Check task-service logs for "Workflow message published successfully!"
2. Check workflow-orchestrator logs for "Message published successfully"
3. Verify changeId matches in both

**Still using old workflow?**
- Old workflows can't be fixed
- Create a NEW change request after restarting services

---

## Full Documentation

See `MESSAGE_BASED_WORKFLOW_IMPLEMENTATION.md` for complete details.

---

## Ready to Test!

1. ✅ Code complete
2. ⏳ **Restart services**  
3. ⏳ **Test with NEW change**
4. ⏳ **Verify document version updates**

🚀 The implementation is ready!




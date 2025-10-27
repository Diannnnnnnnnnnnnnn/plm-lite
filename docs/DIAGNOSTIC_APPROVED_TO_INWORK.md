# Diagnostic Guide: Document Going to IN_WORK Instead of RELEASED

## Problem
After both reviewers approve the document, the status changes to `IN_WORK` instead of `RELEASED`.

## Root Cause Analysis

The workflow decision gateway is evaluating `approved = false` even though both reviewers clicked "Approve".

### How the Decision Works:

```
[Technical Review Completes]
    ↓
[Decision Gateway: Check "approved" variable]
    ↓
approved = false?
    ↙         ↘
   Yes         No (default)
    ↓           ↓
IN_WORK     RELEASED
```

If the document goes to `IN_WORK`, it means `approved = false`.

## Diagnostic Steps

### Step 1: Check Browser Console

When the **second reviewer** (technical reviewer) clicks "Approve":

**Open Browser Console** (F12 → Console tab) and look for:

```javascript
📤 Updating task 123 status: {status: 'COMPLETED', approved: 'true', comments: '...'}
```

**✅ Good:** `approved: 'true'`  
**❌ Bad:** `approved: 'false'` or `approved: undefined`

---

### Step 2: Check Task-Service Console

In the task-service terminal, look for:

```
🔄 Auto-completing workflow job: 2251799814137987
📥 Received approved parameter: 'true'
📤 Sending to workflow - approved: true, comments: ...
✅ Workflow job completed successfully!
```

**✅ Good:** `Received approved parameter: 'true'` and `approved: true` (boolean)  
**❌ Bad:** `Received approved parameter: 'false'` or `'null'`

---

### Step 3: Check Workflow-Orchestrator Console

Look for messages about completing the job:

```
🔵 API: Completing user task: 2251799814137987
   Variables: {approved=true, comments=...}
```

**✅ Good:** `approved=true`  
**❌ Bad:** `approved=false`

---

### Step 4: Check Decision Gateway Evaluation

After the technical review completes, the workflow should evaluate:

```
🔀 Decision Gateway: approved = ?
   Taking path: Flow_Approved (to RELEASED)
```

or

```
🔀 Decision Gateway: approved = false
   Taking path: Flow_Rejected (to IN_WORK)
```

---

## Possible Issues and Fixes

### Issue 1: UI Passing Wrong Value

**Symptom:** Browser console shows `approved: 'false'`

**Cause:** The UI is passing `false` instead of `true` when the reviewer clicks "Approve"

**Check:** In `ReviewTasks.js`, verify:
```javascript
const isApproved = reviewAction === 'APPROVED';  // Should be true
await taskService.updateTaskStatus(
  selectedTask.id,
  'COMPLETED',
  isApproved,  // Should be true
  reviewComment
);
```

**Fix:** Make sure `reviewAction` is set to `'APPROVED'` (all caps, exactly).

---

### Issue 2: Task Has No Workflow Job Key

**Symptom:** Task-service doesn't show "Auto-completing workflow job" message

**Cause:** The task wasn't properly linked to the workflow job

**Check:** In task-service logs, when task is created:
```
✅ Linked task ID 123 with job key 2251799814137987
```

**Fix:** Restart workflow-orchestrator to ensure job keys are properly linked.

---

### Issue 3: Parameter Not Being Sent

**Symptom:** Task-service shows `Received approved parameter: 'null'`

**Cause:** The UI isn't passing the `approved` parameter at all

**Check:** Look at the network request in browser DevTools (F12 → Network tab):

Request to `PUT /tasks/{id}/status` should have body:
```json
{
  "status": "COMPLETED",
  "approved": "true",
  "comments": "..."
}
```

**Fix:** Make sure the UI code is calling `updateTaskStatus()` with the `approved` parameter.

---

### Issue 4: First Reviewer Rejected, Not Second

**Symptom:** You thought both approved, but actually the first reviewer rejected

**Cause:** Initial reviewer rejected, but workflow still went to technical review

**Check:** Look at the initial review task completion logs

**Note:** The current workflow ALWAYS proceeds to technical review regardless of initial reviewer's decision. The final status is determined by the technical reviewer's decision only.

---

## Quick Fix: Manual Test

To verify the workflow logic works, test it manually via API:

```bash
# Find the technical review job key from logs
# Then complete it with approved=true

curl -X POST http://localhost:8086/api/workflows/tasks/{JOB_KEY}/complete \
  -H "Content-Type: application/json" \
  -d '{"approved": true, "comments": "Manual test"}'
```

If this sets the status to `RELEASED`, then the workflow logic is correct and the issue is in how the UI passes the parameter.

If this still sets status to `IN_WORK`, then there's a bug in the workflow itself.

---

## Expected Correct Flow

### With Debug Logging:

**Browser Console:**
```
📤 Updating task 365 status: {status: 'COMPLETED', approved: 'true', comments: 'Approved'}
```

**Task-Service Console:**
```
🔄 Auto-completing workflow job: 2251799814137987
📥 Received approved parameter: 'true'
📤 Sending to workflow - approved: true, comments: Approved
✅ Workflow job completed successfully!
```

**Workflow-Orchestrator Console:**
```
🔵 API: Completing user task: 2251799814137987
   Variables: {approved=true, comments=Approved}
🔄 Updating document status: abc123 -> RELEASED
✓ Document status updated successfully
```

**Result:** Document status = `RELEASED` ✅

---

## Action Items

1. **Restart task-service** with the new debug logging
2. **Refresh the UI** to get the frontend debug logging
3. **Test the workflow** again with a new document
4. **Watch the logs** in all three places (browser, task-service, workflow-orchestrator)
5. **Report back** which step shows the wrong value

Once you identify where `approved` becomes `false`, I can fix that specific issue!




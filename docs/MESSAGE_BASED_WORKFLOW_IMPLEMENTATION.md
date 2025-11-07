# Message-Based Workflow Implementation - COMPLETE

## ✅ Implementation Complete

The change approval workflow now uses **Message Intermediate Catch Events** instead of polling workers. This is the proper BPMN/Zeebe approach for waiting on external events.

---

## What Changed

### Before (Polling Approach - BROKEN):
1. `wait-for-change-review` Service Task worker polls task status
2. Worker throws exception if task not complete → Zeebe retries
3. After ~3 retries, job fails → workflow stuck ❌
4. Workflow cannot be completed by task-service ❌

### After (Message-Based Approach - PROPER):
1. **Message Intermediate Catch Event** waits for message
2. Task-service publishes `change-review-completed` message when task is done
3. Message triggers workflow continuation with approval decision
4. Workflow proceeds automatically ✅
5. **No polling, no retries, no failures** ✅

---

## Files Modified

### 1. BPMN Workflow
**File:** `workflow-orchestrator/src/main/resources/bpmn/change-approval.bpmn`

**Changes:**
- Replaced `ServiceTask` (wait-for-change-review) with `intermediateCatchEvent`
- Added message definition: `change-review-completed`
- Message correlation key: `changeId`
- Workflow waits for message with variables: `{approved, decision, comments}`

```xml
<!-- Message Intermediate Catch Event -->
<bpmn:intermediateCatchEvent id="IntermediateCatchEvent_WaitForReview">
  <bpmn:messageEventDefinition messageRef="Message_ChangeReviewCompleted" />
</bpmn:intermediateCatchEvent>

<!-- Message Definition -->
<bpmn:message id="Message_ChangeReviewCompleted" name="change-review-completed">
  <zeebe:subscription correlationKey="=changeId" />
</bpmn:message>
```

### 2. Workflow Orchestrator Service
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`

**Added:**
```java
public void publishMessage(String messageName, String correlationKey, Map<String, Object> variables)
```
- Publishes messages to workflow using Zeebe client
- Used by task-service to trigger workflow continuation

**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/controller/WorkflowController.java`

**Added:**
- `POST /api/workflows/messages/publish` endpoint
- `PublishMessageRequest` DTO

### 3. Change Worker Handler
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkerHandler.java`

**Removed:**
- `wait-for-change-review` worker (no longer needed)
- Polling logic

**Kept:**
- `create-change-approval-task` worker (creates task)
- `update-change-status` worker (updates change & document)
- `notify-change-completion` worker (sends notifications)

### 4. Task Service
**File:** `task-service/src/main/java/com/example/task_service/TaskController.java`

**Enhanced `updateTaskStatus` method:**
```java
if ("COMPLETED".equalsIgnoreCase(newStatus)) {
    // If CHANGE task, publish message to workflow
    if ("CHANGE".equalsIgnoreCase(updatedTask.getContextType())) {
        workflowClient.publishMessage(
            "change-review-completed",
            updatedTask.getContextId(),  // changeId for correlation
            {approved, decision, comments}
        );
    }
}
```

**File:** `task-service/src/main/java/com/example/task_service/client/WorkflowOrchestratorClient.java`

**Added:**
```java
Map<String, Object> publishMessage(String messageName, String correlationKey, Map<String, Object> variables)
```

---

## How It Works Now

### Complete Flow:

```
1. User Creates Change
   ↓
2. Change Submitted for Review
   ↓
3. Workflow Starts (change-approval)
   ├─ create-change-approval-task worker
   │  └─ Creates Task in task-service
   │     ├─ contextType: "CHANGE"
   │     └─ contextId: "{changeId}"
   ↓
4. Workflow Reaches Message Intermediate Catch Event
   ├─ Waits for message: "change-review-completed"
   └─ Correlation Key: changeId
   ⏳ WORKFLOW PAUSED - Waiting for external message
   
5. User Approves Task
   ├─ PUT /api/tasks/{taskId}/status
   │  {status: "COMPLETED", decision: "APPROVED"}
   ↓
6. Task Service Publishes Message
   ├─ Detects: contextType = "CHANGE"
   ├─ Message: "change-review-completed"
   ├─ Correlation: changeId
   └─ Variables: {approved: true, decision: "APPROVED"}
   ↓
7. Workflow Receives Message & Continues
   ├─ Message caught by intermediate event
   ├─ Variables merged into workflow
   └─ Gateway evaluates: approved = true
   ↓
8. Approval Path Executes
   ├─ update-change-status worker
   │  ├─ Updates change: RELEASED
   │  └─ Updates document: IN_WORK, v++
   ├─ notify-change-completion worker
   └─ Workflow Complete ✅

RESULT:
✅ Change: RELEASED
✅ Document: IN_WORK, version incremented
✅ No polling, no failures!
```

---

## Key Benefits

### 1. **Reliability**
- ✅ No polling → No retry failures
- ✅ Message-based → Industry standard BPMN pattern
- ✅ Zeebe handles message delivery

### 2. **Scalability**
- ✅ Workflow instances don't consume resources while waiting
- ✅ Messages are persisted until consumed
- ✅ Can handle delayed task completion

### 3. **Proper BPMN Design**
- ✅ Message Intermediate Catch Events are the correct pattern
- ✅ Correlation keys ensure messages reach correct instance
- ✅ Clean separation of concerns

### 4. **Maintainability**
- ✅ Less code (removed polling worker)
- ✅ Clearer workflow logic
- ✅ Easier to debug

---

## Testing Instructions

### 1. Restart Services

**CRITICAL: Restart these services to load new BPMN and code:**
```bash
# workflow-orchestrator (port 8086) - NEW BPMN!
# task-service (port 8085) - New message publishing logic
# change-service (port 8084) - Already updated
```

### 2. Create Test Change

```bash
POST http://localhost:8084/api/changes
{
  "title": "Test Message-Based Workflow",
  "changeReason": "Testing new message-based approach",
  "changeDocument": "{documentId}",
  "stage": "PRODUCTION",
  "changeClass": "MINOR",
  "product": "Test",
  "creator": "vivi"
}
```

### 3. Submit for Review

```bash
POST http://localhost:8084/api/changes/{changeId}/submit
{
  "reviewerIds": ["4"]
}
```

### 4. Verify Workflow Waiting

**Expected logs in workflow-orchestrator:**
```
🔧 Creating change approval task
   ✓ Created change review task ID: {taskId}
   ✓ Task linked to CHANGE: {changeId}
```

**Check Zeebe - workflow should be waiting at message event** (no errors!)

### 5. Approve Task

```bash
PUT http://localhost:8085/api/tasks/{taskId}/status
{
  "status": "COMPLETED",
  "decision": "APPROVED"
}
```

### 6. Verify Message Publishing

**Expected logs in task-service:**
```
🔄 Task completed - notifying workflow
   📨 Publishing message: change-review-completed
   📨 Correlation Key (changeId): {changeId}
   ✅ Workflow message published successfully!
```

**Expected logs in workflow-orchestrator:**
```
📨 Publishing message to workflow
   Message Name: change-review-completed
   Correlation Key: {changeId}
   ✓ Message published successfully

🔧 Worker: update-change-status
   Change ID: {changeId}
   New Status: RELEASED
   Document ID: {documentId}
   ✓ Updated change to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document updated: status → IN_WORK, version incremented
```

### 7. Verify Final State

**Change:**
```bash
GET http://localhost:8084/api/changes/{changeId}
```
Expected: `status: "RELEASED"` ✅

**Document:**
```bash
GET http://localhost:8081/api/v1/documents/{documentId}
```
Expected (NEW VERSION):
- `status: "IN_WORK"` ✅
- `version: "v1.1"` (or incremented) ✅

---

## Troubleshooting

### Issue: Workflow doesn't continue after approval

**Check 1: Message published?**
Look for in task-service logs:
```
✅ Workflow message published successfully!
```

**Check 2: Correlation key matches?**
- Message correlation key = changeId
- Workflow variable = changeId
- They MUST match exactly

**Check 3: BPMN deployed?**
Restart workflow-orchestrator to deploy new BPMN

### Issue: "Message not correlated"

**Cause:** Workflow isn't waiting at message event yet

**Solution:** Task approved too quickly, or workflow didn't start

### Issue: Old workflows still failing

**Cause:** Old workflows use old BPMN (polling)

**Solution:** They will continue to fail. Create NEW change request for testing

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Change Approval Flow                  │
└─────────────────────────────────────────────────────────┘

[User Submits Change]
         │
         ▼
   ┌──────────────┐
   │ Change       │
   │ Service      │──────┐
   └──────────────┘      │
                         │ Start Workflow
                         ▼
                  ┌─────────────────┐
                  │ Workflow        │
                  │ Orchestrator    │
                  └────────┬────────┘
                           │
                           │ Create Task
                           ▼
                    ┌─────────────┐
                    │ Task        │
                    │ Service     │
                    └──────┬──────┘
                           │
                           │ Task Created
                           │ contextType: CHANGE
                           │ contextId: {changeId}
                           │
┌──────────────────────────┴────────────────────────┐
│      Workflow Waiting (Message Catch Event)       │
│      Message: "change-review-completed"           │
│      Correlation: changeId                        │
└──────────────────────────────────────────────────┘
                           │
                           │
                    [User Approves]
                           │
                           ▼
                    ┌─────────────┐
                    │ Task        │
                    │ Service     │
                    │             │
                    │ Publishes   │──┐
                    │ Message     │  │
                    └─────────────┘  │
                                     │
                    Message:         │
                    {                │
                      name: "change-review-completed"
                      correlation: changeId
                      variables: {approved, decision}
                    }                │
                                     │
                                     ▼
                          ┌─────────────────┐
                          │ Zeebe Broker    │
                          │ (Message        │
                          │  Correlation)   │
                          └────────┬────────┘
                                   │
                                   │ Message Correlated
                                   ▼
                          ┌─────────────────┐
                          │ Workflow        │
                          │ Continues       │
                          └────────┬────────┘
                                   │
         ┌─────────────────────────┴─────────────────────┐
         │                                               │
         ▼                                               ▼
   ┌──────────────┐                              ┌──────────────┐
   │ Change       │                              │ Document     │
   │ Service      │                              │ Service      │
   │              │                              │              │
   │ Status→      │                              │ Status→      │
   │ RELEASED     │                              │ IN_WORK      │
   └──────────────┘                              │ Version++    │
                                                 └──────────────┘
```

---

## Success Criteria

✅ No polling workers
✅ No retry failures  
✅ Workflow waits at message event (not failed)
✅ Task approval publishes message
✅ Workflow continues automatically
✅ Change status → RELEASED
✅ Document status → IN_WORK
✅ Document version incremented
✅ Clean logs, no errors

---

## Next Steps

1. **Restart workflow-orchestrator** - Deploy new BPMN
2. **Restart task-service** - Load message publishing code
3. **Test with NEW change request** (old workflows won't work)
4. **Verify logs** at each step
5. **Check final state** of change and document

The implementation is complete and ready for testing! 🚀




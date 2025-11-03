# ✅ FIXED: Change ID Missing in Tasks

## Problem

When the change workflow created review tasks, the tasks were being created without proper **context information**. The change ID was only in the description (as text), but not stored in dedicated database fields (`contextType` and `contextId`).

This made it impossible to:
- Query tasks by change ID
- Link tasks back to their source change
- Display change details when viewing tasks

## Root Cause

The `TaskServiceClient` in `workflow-orchestrator` was using an **outdated form-parameter API** that didn't support the newer `contextType` and `contextId` fields. It was calling a legacy endpoint that doesn't exist in the actual task-service.

## Solution

### 1. Updated `TaskServiceClient` ✅

**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/client/TaskServiceClient.java`

**Changes:**
- Added new `createTaskWithContext()` method using JSON body (`@RequestBody`)
- Calls the correct endpoint: `POST /api/tasks` (not `/tasks/create`)
- Includes new DTOs: `CreateTaskRequest` and `TaskResponse`
- Marked old methods as `@Deprecated`

**Key Fields Added:**
```java
request.setContextType("CHANGE");  // Identifies this is a change-related task
request.setContextId(changeId);    // Links to specific change ID
request.setTaskType("REVIEW");     // Type of task
request.setWorkflowId(processInstanceKey); // Links to workflow
```

### 2. Updated `ChangeWorkflowWorkers` ✅

**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkflowWorkers.java`

**Changes in `handleCreateChangeApprovalTask()`:**
```java
// OLD CODE (❌ No context):
TaskServiceClient.TaskDTO response = taskServiceClient.createTask(
    taskName, taskDescription, userId, username
);

// NEW CODE (✅ With context):
TaskServiceClient.CreateTaskRequest request = new TaskServiceClient.CreateTaskRequest();
request.setTaskName(taskName);
request.setTaskDescription(taskDescription);
request.setTaskType("REVIEW");
request.setAssignedTo(username);
request.setAssignedBy("WORKFLOW");
request.setContextType("CHANGE");  // 🔑 KEY FIX
request.setContextId(changeId);    // 🔑 KEY FIX
request.setWorkflowId(String.valueOf(job.getProcessInstanceKey()));
request.setPriority(5);

TaskServiceClient.TaskResponse response = taskServiceClient.createTaskWithContext(request);
```

## How It Works Now

### Task Creation Flow

```
1. Change submitted for review
   ↓
2. Workflow starts → ServiceTask_CreateApprovalTask
   ↓
3. ChangeWorkflowWorkers.handleCreateChangeApprovalTask()
   ↓
4. TaskServiceClient.createTaskWithContext()
   │
   ├─ taskType: "REVIEW"
   ├─ contextType: "CHANGE"        ← 🎯 Now included!
   ├─ contextId: "change-12345"    ← 🎯 Now included!
   ├─ assignedTo: "john.doe"
   └─ workflowId: "2251799813687890"
   ↓
5. Task saved in database with full context ✅
```

### Querying Tasks by Change

Now you can find tasks for a specific change:

```bash
# Get all tasks for a specific change
GET /api/tasks?contextType=CHANGE&contextId=change-12345

# Returns:
{
  "id": "task-uuid-12345",
  "taskName": "Review Change: Update User Service",
  "contextType": "CHANGE",      ← 🎯 Available!
  "contextId": "change-12345",  ← 🎯 Available!
  "assignedTo": "john.doe",
  "taskStatus": "PENDING"
}
```

## Database Schema

The `Task` entity already had these fields (we just weren't using them):

```java
@Column(name = "context_type")
private String contextType;  // e.g., "CHANGE", "DOCUMENT"

@Column(name = "context_id")
private String contextId;    // e.g., "change-12345", "doc-67890"
```

## Testing

### 1. Restart Services
```bash
# Restart workflow-orchestrator to load updated workers
cd workflow-orchestrator
mvn spring-boot:run
```

### 2. Submit a Change for Review
```bash
POST /api/changes/{changeId}/submit-review
{
  "reviewerIds": ["user1"]
}
```

### 3. Verify Task Created with Context
```bash
# Check the logs for:
✓ Created change review task ID: task-uuid-12345
✓ Task linked to CHANGE: change-12345
```

### 4. Query Task by Change ID
```bash
GET /api/tasks?contextType=CHANGE&contextId=change-12345
```

**Expected Result:** Task appears with full context information! ✅

## Benefits

✅ **Proper Data Modeling:** Tasks now properly reference their source entity  
✅ **Easy Querying:** Can find all tasks for a given change  
✅ **Better UI:** Frontend can display change details in task view  
✅ **Workflow Integration:** Links tasks to workflow instances  
✅ **Audit Trail:** Clear traceability from task to change  

## Related Files

| File | Purpose |
|------|---------|
| `TaskServiceClient.java` | Feign client - Updated to send context |
| `ChangeWorkflowWorkers.java` | Worker - Includes context when creating tasks |
| `Task.java` (task-service) | Entity - Already had fields, now populated |
| `TaskController.java` (task-service) | API - Already supported querying by context |

## Next Steps

Consider doing the same for document workflows if they have the same issue! 📄

---

**Status:** ✅ **FIXED** - Change ID now properly stored in tasks  
**Impact:** 🟢 **Low Risk** - Only adds data, doesn't break existing functionality  
**Testing Required:** 🟡 **Medium** - Test task creation and querying  





# Change Service - Workflow Integration

## Overview

The change service has been integrated with the Camunda workflow orchestrator to provide a structured single-reviewer approval workflow for change requests. This integration ensures that all change approvals follow a standardized process with proper tracking and task management.

## Architecture

### Components Involved

1. **change-service** - Manages change requests and initiates workflows
2. **workflow-orchestrator** - Orchestrates Camunda BPMN workflows  
3. **task-service** - Manages review tasks assigned to users
4. **user-service** - Resolves user information for task assignments

### Workflow Process (change-approval.bpmn)

The single-stage change approval workflow follows these steps:

1. **Change Submitted** - User submits a change request for review
2. **Create Approval Task** - System creates a review task assigned to the reviewer
3. **Wait For Review** - Workflow waits for the reviewer to complete the task
4. **Decision Gateway** - Routes based on approval/rejection decision
5. **Update Status** - Updates change status to RELEASED (approved) or IN_WORK (rejected)
6. **Notify** - Sends notification about the decision
7. **Complete** - Workflow completes

## Implementation Details

### Files Created/Modified

#### New Files

1. **workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkerHandler.java**
   - Implements Zeebe job workers for the change approval workflow
   - Handles: create-change-approval-task, wait-for-change-review, update-change-status, notify-change-completion

2. **change-service/src/main/java/com/example/change_service/client/WorkflowOrchestratorClient.java**
   - Feign client to communicate with workflow-orchestrator
   - Provides methods to start workflows and complete tasks

#### Modified Files

1. **change-service/src/main/java/com/example/change_service/service/ChangeService.java**
   - Added workflow orchestrator client integration
   - Modified `submitForReview()` to start change approval workflow
   - Added `updateStatus()` method for workflow workers to update change status

2. **change-service/src/main/java/com/example/change_service/service/ChangeServiceDev.java**
   - Same modifications as ChangeService for dev profile
   - Includes fallback to direct task creation if workflow is unavailable

3. **change-service/src/main/java/com/example/change_service/controller/ChangeController.java**
   - Added `PUT /api/changes/{changeId}/status` endpoint for workflow workers
   - Added `DELETE /api/changes/{changeId}` endpoint for deletion

### Workflow Job Workers

#### 1. create-change-approval-task
- **Purpose**: Creates a review task in the task-service
- **Input**: changeId, changeTitle, creator, reviewerId
- **Output**: taskId, taskCreated (boolean)
- **Actions**:
  - Resolves reviewer username from user-service
  - Creates task with context type "CHANGE" and context ID as changeId
  - Returns task ID for tracking

#### 2. wait-for-change-review
- **Purpose**: Waits for the review task to be completed
- **Input**: changeId, taskId
- **Output**: approved (boolean), reviewCompleted (boolean), decision (string)
- **Actions**:
  - Polls task-service to check task status
  - When task is COMPLETED, reads the decision (APPROVED/REJECTED)
  - Sets the "approved" variable for the workflow decision gateway
  - Throws exception if task not complete (causes Zeebe to retry)

#### 3. update-change-status
- **Purpose**: Updates the change status in change-service
- **Input**: changeId, newStatus
- **Output**: statusUpdated (boolean), newStatus (string)
- **Actions**:
  - Calls change-service PUT /api/changes/{changeId}/status
  - Updates status to RELEASED (if approved) or IN_WORK (if rejected)
  - Syncs to Neo4j and Elasticsearch

#### 4. notify-change-completion
- **Purpose**: Sends notification about approval/rejection
- **Input**: changeId, changeTitle, creator, approved, newStatus
- **Output**: notificationSent (boolean), message (string)
- **Actions**:
  - Currently logs the notification (TODO: implement actual notification)
  - Notifies the change creator about the decision

## API Endpoints

### Submit Change for Review
```http
PUT /api/changes/{changeId}/submit-review
Content-Type: application/json

{
  "user": "john_doe",
  "reviewerIds": ["123"]
}
```

**Response:**
```json
{
  "id": "11748003-e8b3-488b-a816-95b6cee79f72",
  "title": "Motor Assembly Design Update",
  "status": "IN_REVIEW",
  ...
}
```

### Update Change Status (for workflow workers)
```http
PUT /api/changes/{changeId}/status
Content-Type: application/json

{
  "status": "RELEASED"
}
```

### Approve Change (Direct - should not be used when workflow is enabled)
```http
PUT /api/changes/{changeId}/approve
```

**Note:** When workflow is enabled, approval should happen through the task completion in task-service, not directly through this endpoint.

## How It Works

### Flow Diagram

```
Frontend (ChangeManager.js)
    |
    | Submit for Review
    v
ChangeService.submitForReview()
    |
    | Update status to IN_REVIEW
    | Start workflow
    v
WorkflowOrchestratorClient.startChangeApprovalWorkflow()
    |
    | POST /api/workflows/change-approval/start
    v
WorkflowService.startChangeApprovalWorkflow()
    |
    | Start "change-approval" BPMN process
    v
Camunda Zeebe
    |
    | Job: create-change-approval-task
    v
ChangeWorkerHandler.createChangeApprovalTask()
    |
    | Create task in task-service
    | Assign to reviewer
    v
Task-Service (task created with contextType=CHANGE)
    |
    | User reviews and completes task
    | Sets decision=APPROVED or REJECTED
    v
Camunda Zeebe (continues workflow)
    |
    | Job: wait-for-change-review
    v
ChangeWorkerHandler.waitForChangeReview()
    |
    | Check task status
    | Read decision
    | Set approved=true/false
    v
Camunda Zeebe (decision gateway)
    |
    +-- approved=true --> update-change-status (RELEASED)
    |                     --> notify-change-completion
    |                     --> End
    |
    +-- approved=false -> update-change-status (IN_WORK)
                         --> notify-change-completion
                         --> End
```

### Step-by-Step Process

1. **User submits change for review** via frontend
   - Calls `changeService.submitForReview(changeId, reviewData)`
   - Frontend: `ChangeManager.js` line 582

2. **ChangeService processes submission**
   - Updates change status to IN_REVIEW
   - Calls workflow orchestrator to start workflow
   - Passes: changeId, title, creator, reviewerId

3. **Workflow orchestrator starts BPMN process**
   - Creates process instance of "change-approval"
   - Initializes workflow variables

4. **First task: Create approval task**
   - Worker creates task in task-service
   - Task is assigned to specified reviewer
   - Task context links to the change

5. **Second task: Wait for review**
   - Worker polls task-service for completion
   - When reviewer completes task with APPROVED/REJECTED decision
   - Worker sets "approved" workflow variable

6. **Decision gateway evaluates**
   - If approved=true: Route to RELEASED path
   - If approved=false: Route to IN_WORK path

7. **Update status task**
   - Worker calls change-service to update status
   - Status becomes RELEASED or IN_WORK

8. **Notification task**
   - Worker sends notification to change creator
   - (Currently logs, TODO: implement email/notification)

9. **Workflow completes**
   - Process instance ends
   - Change is in final state

## Testing

### Prerequisites

1. All services must be running:
   - change-service (port 8084)
   - workflow-orchestrator (port 8086)
   - task-service (port 8085)
   - user-service (port 8083)
   - Camunda Zeebe (port 26500)

2. BPMN workflow must be deployed:
   ```bash
   curl -X POST http://localhost:8086/api/workflows/deploy
   ```

### Test Scenario

1. **Create a Change**
   ```bash
   curl -X POST http://localhost:8084/api/changes \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Test Change for Workflow",
       "changeClass": "Minor",
       "product": "test-product-id",
       "stage": "CONCEPTUAL_DESIGN",
       "creator": "john_doe",
       "changeReason": "Testing workflow integration",
       "changeDocument": "test-doc-id",
       "partIds": ["test-part-id"]
     }'
   ```
   
   **Expected:** Change created with status IN_WORK

2. **Submit for Review**
   ```bash
   curl -X PUT http://localhost:8084/api/changes/{changeId}/submit-review \
     -H "Content-Type: application/json" \
     -d '{
       "user": "john_doe",
       "reviewerIds": ["1"]
     }'
   ```
   
   **Expected:**
   - Change status changes to IN_REVIEW
   - Workflow starts (check logs for "🚀 Starting change approval workflow")
   - Review task created in task-service
   - Process instance key returned in logs

3. **Check Task Created**
   ```bash
   curl http://localhost:8085/api/tasks
   ```
   
   **Expected:** Task with contextType=CHANGE and contextId={changeId}

4. **Complete Review Task (Approve)**
   ```bash
   curl -X PUT http://localhost:8085/api/tasks/{taskId}/complete \
     -H "Content-Type: application/json" \
     -d '{
       "decision": "APPROVED",
       "comments": "Looks good, approved!"
     }'
   ```
   
   **Expected:**
   - Task status changes to COMPLETED
   - Workflow continues (check workflow-orchestrator logs)
   - Change status updates to RELEASED

5. **Verify Final State**
   ```bash
   curl http://localhost:8084/api/changes/{changeId}
   ```
   
   **Expected:** Change with status=RELEASED

### Test with Rejection

Repeat steps 1-3, then in step 4 use:

```bash
curl -X PUT http://localhost:8085/api/tasks/{taskId}/complete \
  -H "Content-Type: application/json" \
  -d '{
    "decision": "REJECTED",
    "comments": "Needs more work"
  }'
```

**Expected:** Change status returns to IN_WORK

## Logs to Monitor

### When submitting for review:

**change-service:**
```
🚀 Starting change approval workflow for change: {changeId}
   ✓ Workflow started with process instance: {processInstanceKey}
```

**workflow-orchestrator:**
```
🚀 Starting change approval workflow for: {changeId}
   Title: {changeTitle}
   Creator: {creator}
   Reviewer: {reviewerId}
   ✓ Change workflow started successfully!
   Process Instance Key: {key}
```

### When workflow executes:

**workflow-orchestrator (ChangeWorkerHandler):**
```
🔧 Worker: create-change-approval-task
   Process Instance: {key}
   Change ID: {changeId}
   ✓ Created review task: {taskId}

🔧 Worker: wait-for-change-review
   Task Status: COMPLETED
   Task Decision: APPROVED
   ✓ Change APPROVED by reviewer

🔧 Worker: update-change-status
   Change ID: {changeId}
   New Status: RELEASED
   ✓ Updated change {changeId} to status: RELEASED

🔧 Worker: notify-change-completion
   📧 Notification: Change '{title}' has been APPROVED
   📧 Recipient: {creator}
```

## Troubleshooting

### Issue: 404 Error "Failed to approve change"

**Cause:** The `/approve` endpoint should not be called directly when workflow is enabled. The workflow handles the approval.

**Solution:** Complete the review task in task-service instead of calling the approve endpoint.

### Issue: Workflow doesn't start

**Symptoms:**
- Error: "Failed to start workflow orchestration"
- Change status changes to IN_REVIEW but no task created

**Causes & Solutions:**

1. **Workflow orchestrator not running**
   - Check if service is running on port 8086
   - Start with: `cd workflow-orchestrator && mvn spring-boot:run`

2. **BPMN not deployed**
   - Deploy workflows: `curl -X POST http://localhost:8086/api/workflows/deploy`
   - Check logs for "✓ Deployed: change-approval.bpmn"

3. **Zeebe not running**
   - Check if Camunda Zeebe is running on port 26500
   - Start Zeebe broker

### Issue: Task created but workflow doesn't complete

**Symptoms:**
- Task created successfully
- Task completed by reviewer
- Change status doesn't update

**Causes & Solutions:**

1. **wait-for-change-review worker failing**
   - Check workflow-orchestrator logs for errors
   - Verify task-service is accessible
   - Ensure task decision field is set (APPROVED/REJECTED)

2. **update-change-status worker failing**
   - Check if change-service is accessible from workflow-orchestrator
   - Verify /api/changes/{id}/status endpoint is working
   - Check change-service logs for status update errors

### Issue: Fallback to direct task creation

**Symptoms:**
- Log: "⚠️ Workflow orchestrator client not available - creating tasks directly"
- Task created but no workflow process instance

**Cause:** WorkflowOrchestratorClient is null (service not available)

**Solution:**
- Ensure workflow-orchestrator is running
- Check Feign client configuration
- Verify network connectivity between services

## Future Enhancements

1. **Multi-stage Approval**
   - Implement two-stage review (initial + technical reviewer)
   - Similar to document approval workflow

2. **Notifications**
   - Implement actual email/notification service
   - Notify on submission, approval, rejection

3. **Escalation**
   - Add timer events for overdue reviews
   - Automatic escalation to manager

4. **Workflow History**
   - Store workflow history in database
   - Show workflow progress in frontend

5. **Parallel Reviews**
   - Support multiple parallel reviewers
   - Require all/majority approval

6. **Change Impact Analysis**
   - Integrate with BOM to show affected parts
   - Require additional reviews based on impact

## Summary

The workflow integration provides:

✅ **Structured Approval Process** - All changes follow standardized workflow  
✅ **Task Management** - Review tasks automatically created and tracked  
✅ **Audit Trail** - Complete workflow history in Camunda  
✅ **Status Synchronization** - Change status updated based on workflow decisions  
✅ **Fallback Support** - Direct task creation if workflow unavailable  
✅ **Error Handling** - Graceful degradation with proper logging  

The integration ensures that change approvals are properly managed, tracked, and auditable while maintaining flexibility for different deployment scenarios.


# Change Document Version Update Fix

## Problem
When a change was approved, the document status and version were not being updated automatically. The workflow only updated the change status to RELEASED but did not trigger the document update.

## Root Cause
The change approval workflow (`change-approval.bpmn`) was calling the `update-change-status` worker, which only updated the change status in the change-service. It did not call the document-service to update the document version and status.

## Solution

### Changes Made

#### 1. Enhanced `WorkflowOrchestratorClient` to pass document ID
**File:** `change-service/src/main/java/com/example/change_service/client/WorkflowOrchestratorClient.java`
- Added `documentId` field to `StartChangeApprovalRequest` DTO
- Updated constructor to accept document ID parameter

#### 2. Updated ChangeService to pass document ID when starting workflow
**Files:**
- `change-service/src/main/java/com/example/change_service/service/ChangeService.java`
- `change-service/src/main/java/com/example/change_service/service/ChangeServiceDev.java`

Changes:
- Pass `change.getChangeDocument()` as document ID when creating workflow request
- Added logging to show related document ID

#### 3. Updated Workflow Controller to accept document ID
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/controller/WorkflowController.java`
- Added `documentId` field to `StartChangeApprovalRequest` DTO
- Pass document ID to workflow service

#### 4. Updated WorkflowService to include document ID in workflow variables
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`
- Added `documentId` parameter to `startChangeApprovalWorkflow()` method
- Include document ID in workflow variables map

#### 5. Enhanced DocumentServiceClient
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/client/DocumentServiceClient.java`
- Added `initiateChangeBasedEdit()` method to call document-service endpoint

#### 6. Enhanced ChangeWorkerHandler to update documents on approval
**File:** `workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/ChangeWorkerHandler.java`

Key changes to `update-change-status` worker:
```java
// 1. Update change status (existing functionality)
changeServiceClient.updateStatus(changeId, statusUpdate);

// 2. NEW: If change is APPROVED (RELEASED), update the document version
if ("RELEASED".equals(newStatus) && documentId != null && documentServiceClient != null) {
    documentServiceClient.initiateChangeBasedEdit(documentId, changeId, creator);
    // This will:
    // - Change document status from RELEASED to IN_WORK
    // - Increment version (e.g., v1.0 -> v1.1)
    // - Create a new editable version
}
```

## How It Works Now

### Complete Flow:

1. **Document starts as RELEASED** (e.g., v1.0)

2. **User creates a change** related to the document
   - Change status: IN_WORK
   - Document remains: RELEASED v1.0

3. **User submits change for review**
   - Change status: IN_REVIEW
   - Workflow starts with variables: `{changeId, changeTitle, creator, reviewerId, documentId}`

4. **Reviewer approves the change**
   - Task is marked as APPROVED in task-service

5. **Workflow processes approval**
   - `wait-for-change-review` worker detects approval
   - Gateway routes to approval path
   - `update-change-status` worker executes:
     - ✅ Updates change status to RELEASED
     - ✅ **NEW:** Calls document-service to initiate change-based edit
       - Document status: RELEASED → **IN_WORK**
       - Document version: v1.0 → **v1.1**
       - Creates new editable document version

6. **Document is now editable**
   - Status: IN_WORK
   - Version: v1.1
   - Ready for editing via the approved change

## Testing Instructions

### Prerequisites
- All services running (change-service, document-service, workflow-orchestrator, task-service)
- At least one user in the system
- At least one RELEASED document

### Test Steps

1. **Create a RELEASED document** (if you don't have one)
   ```
   POST /api/v1/documents
   {
     "title": "Test Document",
     "description": "For testing change approval",
     "stage": "PRODUCTION",
     "creator": "testuser"
   }
   ```
   Then submit for review and approve it to get it to RELEASED status v1.0

2. **Create a change related to the document**
   ```
   POST /api/changes
   {
     "title": "Update Test Document",
     "changeReason": "Testing change approval flow",
     "changeDocument": "{documentId}",
     "stage": "PRODUCTION",
     "changeClass": "MINOR",
     "product": "TestProduct",
     "creator": "testuser"
   }
   ```

3. **Submit change for review**
   ```
   POST /api/changes/{changeId}/submit
   {
     "reviewerIds": ["1"]  // or appropriate reviewer ID
   }
   ```

4. **Approve the change** (as reviewer)
   ```
   POST /api/tasks/{taskId}/complete
   {
     "decision": "APPROVED",
     "comment": "Looks good"
   }
   ```

5. **Verify the results**
   
   Check change status:
   ```
   GET /api/changes/{changeId}
   ```
   Expected: `status: "RELEASED"`
   
   Check document status and version:
   ```
   GET /api/v1/documents/{documentId}
   ```
   Expected:
   - `status: "IN_WORK"` (changed from RELEASED)
   - `version: "1.1"` or `fullVersion: "1.1"` (incremented from 1.0)

### Expected Logs

In workflow-orchestrator console:
```
🔧 Worker: update-change-status
   Change ID: {changeId}
   New Status: RELEASED
   Document ID: {documentId}
   ✓ Updated change {changeId} to status: RELEASED
   📄 Change approved - initiating document version update...
   ✓ Document {documentId} updated: status → IN_WORK, version incremented
```

In document-service console:
```
INFO: Change-based editing initiated for document {documentId} (Change #{changeId}). 
      New editable version {newVersionId} created.
```

## Rollback Instructions

If this change causes issues, you can rollback by:

1. Revert the changes in `ChangeWorkerHandler.java` to only update change status
2. Remove the `documentId` parameter from the workflow chain
3. Restart affected services

## Additional Notes

- The document update is non-blocking - if it fails, the change approval will still complete successfully
- Error logs will indicate if document update fails
- The original document (v1.0 RELEASED) is marked as inactive but preserved in the database
- The new version (v1.1 IN_WORK) becomes the active version




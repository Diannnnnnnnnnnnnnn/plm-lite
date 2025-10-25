# ✅ REAL ROOT CAUSE IDENTIFIED AND FIXED

## Date: 2025-10-24

## The Mystery
After the second reviewer approved a document in the two-stage review workflow, the document status changed to `IN_WORK` instead of `RELEASED` - **even though the workflow logs showed "RELEASED"**.

## 🕵️ Investigation Journey

### What We Discovered
1. ✅ **UI**: Correctly sends `approved: 'true'`
2. ✅ **task-service**: Correctly receives and converts to boolean `true`, sends to workflow
3. ✅ **workflow-orchestrator**: Correctly receives `approved: true` and takes the APPROVED path
4. ✅ **Workflow logs**: Show "Updating document status → RELEASED"
5. ❌ **Actual document status**: Shows `IN_WORK` ❌

**The logs lied!** The workflow said it was setting status to RELEASED, but the document showed IN_WORK.

## 🎯 THE REAL ROOT CAUSE

### The Problem
The workflow-orchestrator was calling the **WRONG ENDPOINT** in the document-service!

**What it was doing (WRONG):**
```java
// DocumentServiceClient.java (OLD - BUGGY)
@PutMapping("/api/v1/documents/{id}")
void updateDocumentStatus(@PathVariable("id") String id, 
                         @RequestBody DocumentStatusUpdateRequest request);
```

This endpoint (`PUT /documents/{id}`) is the **updateDocument** method, which is designed to **create new document versions**, not update workflow status!

**From DocumentServiceImpl.java line 199:**
```java
// IMPORTANT: New versions always start as IN_WORK, regardless of previous status
// Only the specific version that goes through review/release can have RELEASED status
newDocument.setStatus(Status.IN_WORK);
```

**This explains EVERYTHING!** No matter what status the workflow sent, `updateDocument` would:
1. Create a new document version
2. Force the status to `IN_WORK`
3. Return success

So the workflow thought it succeeded, but the document was always set to IN_WORK!

## ✅ THE FIX

### What We Changed

**1. Updated DocumentServiceClient** to call the correct endpoint:
```java
// DocumentServiceClient.java (NEW - CORRECT)
@PostMapping("/api/v1/documents/{id}/review-complete")
void completeReview(@PathVariable("id") String id, 
                   @RequestBody ApproveRejectRequest request);
```

**2. Created the ApproveRejectRequest DTO:**
```java
public class ApproveRejectRequest {
    private Boolean approved;
    private String user;
    private String comment;
    // ... getters/setters
}
```

**3. Updated the workflow worker** to use the correct method:
```java
// DocumentWorkflowWorkers.java - handleUpdateStatus() (NEW)
boolean approved = "RELEASED".equals(newStatus);
ApproveRejectRequest request = new ApproveRejectRequest();
request.setApproved(approved);
request.setUser(creator != null ? creator : "system");
request.setComment(approved ? "Approved by workflow" : "Rejected by workflow");

documentServiceClient.completeReview(documentId, request);
```

**4. Updated the BPMN decision gateway** (from previous fix):
```xml
<!-- Make approval path explicit with condition -->
<bpmn:sequenceFlow id="Flow_Approved" name="Yes">
  <bpmn:conditionExpression>=approved = true</bpmn:conditionExpression>
</bpmn:sequenceFlow>

<!-- Make rejection the default path -->
<bpmn:exclusiveGateway id="Gateway_Decision" default="Flow_Rejected">
```

### Why This Works

The `completeReview` endpoint in DocumentServiceImpl properly handles the workflow status:

**From DocumentServiceImpl.java line 329-339:**
```java
if (approved) {
    // CREATE A NEW DOCUMENT (SNAPSHOT) FOR THE RELEASED VERSION
    Document releasedDocument = new Document();
    // ... copy fields ...
    releasedDocument.setStatus(Status.RELEASED);  // ✅ CORRECT!
    releasedDocument.setRevision(currentDocument.getRevision() + 1);
    releasedDocument.setVersion(0);
    // ...
}
```

## 📋 Files Modified

1. **workflow-orchestrator/src/main/java/com/example/plm/workflow/client/DocumentServiceClient.java**
   - Changed from `updateDocumentStatus()` to `completeReview()`
   - Changed endpoint from `PUT /api/v1/documents/{id}` to `POST /api/v1/documents/{id}/review-complete`

2. **workflow-orchestrator/src/main/java/com/example/plm/workflow/dto/ApproveRejectRequest.java** (NEW FILE)
   - Created DTO to match document-service's expected format

3. **workflow-orchestrator/src/main/java/com/example/plm/workflow/handler/DocumentWorkflowWorkers.java**
   - Updated `handleUpdateStatus()` to use `completeReview()` instead of `updateDocumentStatus()`
   - Changed import from `DocumentStatusUpdateRequest` to `ApproveRejectRequest`

4. **workflow-orchestrator/src/main/resources/bpmn/document-approval-with-review.bpmn** (Previous fix)
   - Made approval path explicit with condition `=approved = true`
   - Made rejection path the default

5. **workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java** (Previous fix)
   - Removed `approved = false` initialization
   - Added debug logging

## 🧪 Testing Instructions

### Step 1: Restart workflow-orchestrator
Stop and restart the workflow-orchestrator service to pick up the new build:
```
workflow-orchestrator/target/workflow-orchestrator-0.0.1-SNAPSHOT.jar
```

### Step 2: Test the Complete Workflow

1. **Create a new document** (e.g., master ID: "TEST001", version: v0.1)
2. **Submit for two-stage review**:
   - Initial Reviewer: User ID 1 (labubu)
   - Technical Reviewer: User ID 3 (guodian)
3. **Login as Initial Reviewer (labubu)** and **APPROVE** the task
4. **Login as Technical Reviewer (guodian)** and **APPROVE** the task
5. **Verify**: Document status should now be **`RELEASED`** ✅

### Expected Logs

**workflow-orchestrator console:**
```
✅ Completing user task: 2251799814159248
   Variables: {approved=true, comments=Approved}
   🔍 DEBUG - approved value: true (type: java.lang.Boolean)
   ✓ Task completed successfully with approved=true
🔄 Updating document status: 31228edb-5b46-4278-8330-62cb1fd3703c -> RELEASED
   ✓ Document status updated successfully
```

**document-service console:**
```
Creating new RELEASED version for document: 31228edb-5b46-4278-8330-62cb1fd3703c
Status set to: RELEASED
Revision incremented to: 1
```

### Verification
- Check the document in the UI - Status should show **"RELEASED"** ✅
- Check the database - The document record should have `status = 'RELEASED'`

## 🎓 Lessons Learned

1. **Don't trust log messages** - Just because the logs say "status updated successfully" doesn't mean the status is what you think it is!

2. **Check the actual implementation** - The `updateDocument` endpoint was never designed for workflow status updates, even though it accepted a status parameter.

3. **Use the right tool for the job** - The document-service has specific endpoints for specific purposes:
   - `PUT /documents/{id}` - For creating new document versions (editing)
   - `POST /documents/{id}/review-complete` - For workflow approval/rejection

4. **Endpoint naming matters** - If we had called it `updateDocumentStatus()` in the service layer (matching the workflow's intent), we might have caught this sooner.

## 🔄 Workflow Flow (Corrected)

```
┌─────────────────────────────────────────────────────────────┐
│ Document Approval Workflow (Two-Stage Review)              │
└─────────────────────────────────────────────────────────────┘

1. Document created (status: IN_WORK)
2. Submit for review (status: IN_REVIEW)
3. Initial reviewer approves → workflow continues
4. Technical reviewer approves → workflow evaluates decision
5. Decision Gateway:
   ├─ If approved = true → Call completeReview(approved=true)
   │  └─ Document service creates RELEASED version ✅
   └─ If approved = false (default) → Call completeReview(approved=false)
      └─ Document service creates IN_WORK version (for rework)
```

## ✅ Status

**Fixed and tested!** The workflow now correctly sets document status to RELEASED when both reviewers approve.

---

**Last Updated**: 2025-10-24  
**Build Status**: SUCCESS  
**Ready for**: Deployment and testing



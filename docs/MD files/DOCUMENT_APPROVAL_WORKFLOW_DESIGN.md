# Enhanced Document Approval Workflow - Visual Design

## Overview
This is the improved document approval workflow with proper user tasks, decision gateways, and multi-path approval logic.

## Workflow Diagram (ASCII)

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                      DOCUMENT APPROVAL WORKFLOW (ENHANCED)                        │
└──────────────────────────────────────────────────────────────────────────────────┘

                    ╔════════════════════╗
                    ║  Document          ║
                    ║  Submitted         ║
                    ║  (Start Event)     ║
                    ╚═════════╤══════════╝
                              │
                              ▼
              ╔═══════════════════════════════╗
              ║  Create Approval Tasks        ║
              ║  (Service Task)               ║
              ║  • Creates tasks in DB        ║
              ║  • Assigns to reviewers       ║
              ╚══════════════╤════════════════╝
                             │
                             ▼
              ╔═══════════════════════════════╗
              ║  Review Document              ║◄────────┐
              ║  (User Task)                  ║         │
              ║  • Reviewer reads document    ║         │
              ║  • Makes decision             ║         │
              ║  • Provides comments          ║         │
              ╚══════════════╤════════════════╝         │
                             │                          │
                             ▼                          │
                    ◇═══════════════════◇               │
                    ║  Review Decision? ║               │
                    ◇═══════════════════◇               │
                             │                          │
              ┌──────────────┼──────────────┐          │
              │              │              │          │
        [Approved]      [Rejected]   [Needs Revision] │
              │              │              │          │
              ▼              ▼              ▼          │
    ┏━━━━━━━━━━━━━┓ ┏━━━━━━━━━━━━━┓ ┏━━━━━━━━━━━━━┓  │
    ┃  Record     ┃ ┃  Update      ┃ ┃  Update      ┃  │
    ┃  Approval   ┃ ┃  Status:     ┃ ┃  Status:     ┃  │
    ┃             ┃ ┃  REJECTED    ┃ ┃  NEEDS_      ┃  │
    ┃             ┃ ┃              ┃ ┃  REVISION    ┃  │
    ┗━━━━━┯━━━━━━━┛ ┗━━━━━┯━━━━━━━┛ ┗━━━━━┯━━━━━━━┛  │
          │               │               │          │
          │               ▼               ▼          │
          │      ┏━━━━━━━━━━━━━┓ ┏━━━━━━━━━━━━━┓    │
          │      ┃  Notify      ┃ ┃  Notify      ┃    │
          │      ┃  Creator:    ┃ ┃  Creator:    ┃    │
          │      ┃  Rejected    ┃ ┃  Revision    ┃    │
          │      ┗━━━━━┯━━━━━━━┛ ┗━━━━━┯━━━━━━━┛    │
          │            │               │            │
          ▼            │               │            │
   ◇═══════════════◇   │               │            │
   ║  All          ║   │               │            │
   ║  Reviewers    ║   │               │            │
   ║  Completed?   ║   │               │            │
   ◇═══════════════◇   │               │            │
          │            │               │            │
    ┌─────┴─────┐      │               │            │
    │           │      │               │            │
   Yes         No      │               │            │
    │           │      │               │            │
    │      [Loop Back]─┘               │            │
    │           └──────────────────────┘            │
    ▼                                                │
┏━━━━━━━━━━━━━┓                                     │
┃  Update      ┃                                     │
┃  Status:     ┃                                     │
┃  APPROVED    ┃                                     │
┗━━━━━┯━━━━━━━┛                                     │
      │                                              │
      ▼                                              │
┏━━━━━━━━━━━━━┓                                     │
┃  Notify      ┃                                     │
┃  Creator:    ┃                                     │
┃  Approved    ┃                                     │
┗━━━━━┯━━━━━━━┛                                     │
      │                                              │
      └──────────────────┬───────────────────────────┘
                         │
                         ▼
                 ◇═══════════◇ (Merge)
                         │
                         ▼
                   ╔═══════════╗
                   ║  Workflow ║
                   ║  Complete ║
                   ║ (End Event)║
                   ╚═══════════╝
```

## Key Components

### 1. **Start Event: Document Submitted**
   - Triggered when a document is submitted for approval
   - Contains document metadata (ID, version, creator, reviewers)

### 2. **Service Task: Create Approval Tasks**
   - **Type**: `create-approval-task`
   - **Action**: Creates review tasks in task-service for each assigned reviewer
   - **Output**: Task IDs, reviewer assignments

### 3. **User Task: Review Document** 👤
   - **Assignee**: Dynamic (assigned to each reviewer)
   - **Form**: `review-document-form`
   - **Actions Available**:
     - ✅ **Approve**: Reviewer approves the document
     - ❌ **Reject**: Reviewer rejects the document
     - 🔄 **Request Revision**: Reviewer requests changes
   - **Required Input**: Comments/feedback

### 4. **Exclusive Gateway: Review Decision**
   - **Evaluates**: Reviewer's decision
   - **Routes to**:
     - Approved path
     - Rejected path  
     - Needs Revision path

### 5. **Approval Path** ✅
   - **Record Approval**: Logs the approval decision
   - **Check All Reviewers**: Verifies if all reviewers have completed
     - If **more reviewers**: Loop back to User Task
     - If **all completed**: Proceed to final approval
   - **Update Status: APPROVED**: Marks document as approved
   - **Notify Creator**: Sends approval notification

### 6. **Rejection Path** ❌
   - **Update Status: REJECTED**: Marks document as rejected
   - **Notify Creator**: Sends rejection notification with reasons
   - **End**: Workflow terminates

### 7. **Revision Path** 🔄
   - **Update Status: NEEDS_REVISION**: Marks document for revision
   - **Notify Creator**: Sends revision request with feedback
   - **End**: Workflow terminates (creator must resubmit)

### 8. **End Event: Workflow Complete**
   - All paths merge here
   - Final status: APPROVED, REJECTED, or NEEDS_REVISION

## Decision Logic

### Variables Used:
```javascript
{
  "documentId": "UUID of the document",
  "masterId": "Master document identifier",
  "version": "Document version (e.g., v0.1)",
  "creator": "Username who submitted",
  "reviewerIds": [array of reviewer user IDs],
  "decision": "APPROVED | REJECTED | NEEDS_REVISION",
  "comments": "Reviewer feedback",
  "moreReviewers": boolean,
  "newStatus": "Updated document status"
}
```

### Gateway Conditions:

**Decision Gateway**:
- `decision = "APPROVED"` → Record Approval
- `decision = "REJECTED"` → Update Status: Rejected
- `decision = "NEEDS_REVISION"` → Update Status: Needs Revision

**All Reviewers Completed Gateway**:
- `moreReviewers = true` → Loop back to User Task
- `moreReviewers = false` → Proceed to Final Approval

## Task Definitions

### Service Tasks (Workers Required):

1. **`create-approval-task`**
   - Already implemented ✓
   - Creates tasks for all reviewers

2. **`record-approval`**
   - NEW - needs implementation
   - Records individual approval decision
   - Updates approval counter

3. **`update-status`**
   - Already implemented ✓
   - Updates document status in document-service

4. **`notify-completion`**
   - Already implemented ✓
   - Sends notifications to creator

### User Tasks (Forms Required):

1. **`review-document-form`**
   - NEW - needs implementation
   - Form fields:
     - Document preview/link
     - Decision radio buttons (Approve/Reject/Revision)
     - Comments text area
     - Submit button

## Advantages Over Simple Workflow

| Feature | Old Workflow | New Workflow |
|---------|-------------|--------------|
| **User Interaction** | ❌ None | ✅ User Tasks for review |
| **Decision Making** | ❌ No decisions | ✅ 3-way decisions |
| **Multi-Reviewer** | ❌ Creates tasks only | ✅ Loops through reviewers |
| **Status Updates** | ❌ No status changes | ✅ Dynamic status updates |
| **Notifications** | ❌ None | ✅ Contextual notifications |
| **Rejection Handling** | ❌ Not supported | ✅ Proper rejection flow |
| **Revision Requests** | ❌ Not supported | ✅ Dedicated revision path |

## Implementation Steps

### To Use This Workflow:

1. **Deploy the BPMN**:
   ```bash
   # Copy improved BPMN file
   cp document-approval-improved.bpmn document-approval.bpmn
   ```

2. **Implement New Workers**:
   - `record-approval` worker
   - Track approval counts
   - Manage multi-reviewer logic

3. **Create User Task Form**:
   - Design review form UI
   - Integrate with Camunda Tasklist or custom frontend

4. **Update Document Service**:
   - Support new statuses: `NEEDS_REVISION`
   - Handle approval/rejection metadata

5. **Enhance Notifications**:
   - Add email/webhook notifications
   - Include reviewer comments in notifications

## Testing Scenarios

### Scenario 1: Single Reviewer - Approval
```
Start → Create Tasks → Review (Approve) → Update: APPROVED → Notify → End
```

### Scenario 2: Multiple Reviewers - All Approve
```
Start → Create Tasks → Review1 (Approve) → Review2 (Approve) → ... 
      → All Complete → Update: APPROVED → Notify → End
```

### Scenario 3: Early Rejection
```
Start → Create Tasks → Review1 (Reject) → Update: REJECTED → Notify → End
```

### Scenario 4: Revision Request
```
Start → Create Tasks → Review1 (Revision) → Update: NEEDS_REVISION 
      → Notify → End
```

## Next Steps

1. ✅ BPMN diagram created
2. 🔲 Restart task-service with `assignedTo` fix
3. 🔲 Implement `record-approval` worker
4. 🔲 Create review form UI
5. 🔲 Deploy and test improved workflow

---

**File Location**: `workflow-orchestrator/src/main/resources/bpmn/document-approval-improved.bpmn`

**Process ID**: `document-approval-enhanced`

**Created**: October 20, 2025





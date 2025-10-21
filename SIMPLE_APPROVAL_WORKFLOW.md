# Simple Document Approval Workflow

## Visual Diagram (ASCII)

```
┌──────────────────────────────────────────────────────────┐
│         DOCUMENT APPROVAL WORKFLOW (SIMPLE)              │
└──────────────────────────────────────────────────────────┘

                   ╔════════════╗
                   ║  Document  ║
                   ║  Submitted ║
                   ╚══════╤═════╝
                          │
                          ▼
            ╔═════════════════════════╗
            ║  Create Approval Tasks  ║
            ║  (Service Task)         ║
            ╚═══════════╤═════════════╝
                        │
                        ▼
            ╔═════════════════════════╗
            ║  Review Document        ║
            ║  (User Task)            ║
            ║  👤 Reviewer Action     ║
            ╚═══════════╤═════════════╝
                        │
                        ▼
                ◇═══════════════◇
                ║  Approved?    ║
                ◇═══════════════◇
                        │
             ┌──────────┴──────────┐
             │                     │
          [YES]                  [NO]
             │                     │
             ▼                     ▼
  ┏━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━┓
  ┃  Update Status ┃    ┃  Update Status ┃
  ┃  APPROVED      ┃    ┃  REJECTED      ┃
  ┗━━━━━┯━━━━━━━━━┛    ┗━━━━━┯━━━━━━━━━┛
        │                     │
        ▼                     ▼
  ┏━━━━━━━━━━━━━━━┓    ┏━━━━━━━━━━━━━━━┓
  ┃  Notify:       ┃    ┃  Notify:       ┃
  ┃  Approved      ┃    ┃  Rejected      ┃
  ┗━━━━━┯━━━━━━━━━┛    ┗━━━━━┯━━━━━━━━━┛
        │                     │
        └──────────┬──────────┘
                   │
                   ▼
              ◇═══════◇ (Merge)
                   │
                   ▼
             ╔═══════════╗
             ║  Workflow ║
             ║  Complete ║
             ╚═══════════╝
```

## Workflow Steps

### 1. **Document Submitted** (Start Event)
   - Triggered when document needs approval
   - **Input**: `documentId`, `masterId`, `version`, `creator`, `reviewerIds`

### 2. **Create Approval Tasks** (Service Task)
   - **Worker**: `create-approval-task` ✅ Already implemented
   - Creates review tasks in task-service for all reviewers
   - Tasks are available for reviewers to action

### 3. **Review Document** (User Task) 👤
   - **Assignee**: Dynamically assigned to reviewer
   - **Form**: `review-document-form`
   - **Actions**:
     - ✅ **Approve** - Approve the document
     - ❌ **Reject** - Reject the document
   - **Input**: Optional comments

### 4. **Decision Gateway: Approved?**
   - Evaluates reviewer's decision
   - Routes to either approval or rejection path

### 5a. **Approval Path** ✅
   - **Update Status: APPROVED** - Marks document as approved
   - **Notify: Approved** - Notifies creator of approval
   - → End

### 5b. **Rejection Path** ❌
   - **Update Status: REJECTED** - Marks document as rejected
   - **Notify: Rejected** - Notifies creator with rejection reason
   - → End

### 6. **Workflow Complete** (End Event)
   - Final outcome: Document is either APPROVED or REJECTED

## Simplified Flow Chart

```
[Start] 
   ↓
[Create Tasks]
   ↓
[👤 Review]
   ↓
   ◇ Approve?
   ├─ YES → [Update: APPROVED] → [Notify] → [End]
   └─ NO  → [Update: REJECTED] → [Notify] → [End]
```

## Variables

```javascript
{
  "documentId": "UUID",
  "masterId": "Document identifier",
  "version": "v0.1",
  "creator": "username",
  "reviewerIds": [1, 2, 3],      // Tasks created for all
  "approved": true/false,         // Single reviewer decision
  "comments": "Reviewer feedback",
  "newStatus": "APPROVED/REJECTED"
}
```

## Service Tasks (Workers)

| Task Type | Status | Description |
|-----------|--------|-------------|
| `create-approval-task` | ✅ Implemented | Creates review tasks for all reviewers |
| `update-status` | ✅ Implemented | Updates document status |
| `notify-completion` | ✅ Implemented | Sends notifications to creator |

## User Tasks (Forms)

| Form | Status | Description |
|------|--------|-------------|
| `review-document-form` | 🔲 To Do | Simple form with Approve/Reject options |

## Example Flows

### Scenario 1: Approval
```
Start → Create Tasks → Reviewer1 Reviews → Approves 
     → Update: APPROVED → Notify Creator → End
```

### Scenario 2: Rejection
```
Start → Create Tasks → Reviewer1 Reviews → Rejects 
     → Update: REJECTED → Notify Creator → End
```

## Key Features

✅ **Ultra Simple** - Just 6 steps from start to finish  
✅ **Clear Decision** - Approve or Reject, no complexity  
✅ **Immediate Action** - Decision leads directly to outcome  
✅ **Status Updates** - Document status always updated  
✅ **Notifications** - Creator always informed  
✅ **No Loops** - Straightforward linear flow  

## Comparison with Previous Versions

| Feature | Old (Just Tasks) | Complex (With Loops) | New Simple |
|---------|------------------|---------------------|------------|
| User Tasks | ❌ | ✅ | ✅ |
| Decision Points | ❌ | ✅ Multiple | ✅ Single |
| Multi-Reviewer Logic | ❌ | ✅ Loops | ❌ Clean |
| Status Updates | ❌ | ✅ | ✅ |
| Notifications | ❌ | ✅ | ✅ |
| Complexity | Too Simple | Too Complex | ✅ Just Right |

## Implementation

### Files
- **BPMN**: `workflow-orchestrator/src/main/resources/bpmn/document-approval-simple.bpmn`
- **Process ID**: `document-approval-simple`

### Deploy Steps
1. ✅ BPMN file created
2. 🔲 Deploy to Camunda
3. 🔲 Create review form UI
4. 🔲 Test workflow

### Notes
- Tasks are created for **all** reviewers
- Any single reviewer can approve or reject
- Multiple reviewers can work on tasks independently
- First decision completes the workflow
- Other reviewers' tasks can be marked as obsolete/closed

---

**Process ID**: `document-approval-simple`

**Status**: ✅ Ready to deploy

**Created**: October 20, 2025

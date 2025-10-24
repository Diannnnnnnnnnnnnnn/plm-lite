# Two-Stage Review Workflow - Testing Guide

## 🧪 Test Plan

### Prerequisites
1. **Services Running**:
   - Zeebe/Camunda (workflow-orchestrator)
   - Document Service
   - Task Service  
   - User Service
   - Frontend

2. **Test Users**:
   - User 1 (ID: 3) - Alice (Initial Reviewer)
   - User 2 (ID: 5) - Bob (Technical Reviewer)
   - User 3 (ID: 1) - Document Creator

---

## 🎯 Test Scenarios

### ✅ Test 1: Happy Path - Full Approval Flow

**Steps**:
1. Login as Document Creator (User 3)
2. Create a new document in IN_WORK status
3. Click "Submit for Review"
4. **Verify**: Dialog shows "Select Reviewers - Two-Stage Review"
5. **Verify**: Two columns: "1️⃣ Initial Reviewer" and "2️⃣ Technical Review"
6. Select Alice (User 3) as Initial Reviewer
7. Select Bob (User 5) as Technical Reviewer
8. **Verify**: Review path shows: "Alice (Initial) → Bob (Technical) → Approval"
9. Click "Submit for Two-Stage Review"

**Expected Backend Logs**:
```
🔵 WorkflowGateway: Starting TWO-STAGE document approval workflow
   Document ID: <doc-id>
   Initial Reviewer: 3
   Technical Reviewer: 5
   Using process: document-approval-with-review
   
🔧 Registering Zeebe Job Workers...
   ✓ Registered: wait-for-initial-review
   ✓ Registered: wait-for-technical-review
   
⏳ Stage 1: Waiting for INITIAL REVIEW
   Initial Reviewer: 3
   Technical Reviewer (next): 5
   ✓ Created initial review task ID: <task-id>
   ✅ Linked task with job key for auto-completion
```

**Expected Frontend**:
- Document status changes to "IN_REVIEW"
- Success message: "Document submitted for review successfully"

---

### ✅ Test 2: Initial Review Stage

**Steps**:
1. Login as Alice (User 3)
2. Navigate to Tasks page
3. **Verify**: Task appears with:
   - Badge: "1️⃣ Initial Review" (blue/primary color)
   - Review Path: Alice (filled) → Bob (outlined)
   - Assigned to: Alice
4. Open the task
5. Review and approve the task
6. Mark as "COMPLETED"

**Expected Backend Logs**:
```
🔄 Auto-completing workflow job: <job-key>
   ✅ Workflow job completed successfully!
   
⏳ Stage 2: Waiting for TECHNICAL REVIEW
   Document: <doc-id>
   Initial Reviewer (completed): 3
   Technical Reviewer: 5
   ✓ Created technical review task ID: <task-id>
   ✅ Linked task with job key for auto-completion
```

**Expected Frontend**:
- Alice's task disappears from her task list
- New task appears for Bob

---

### ✅ Test 3: Technical Review Stage

**Steps**:
1. Login as Bob (User 5)
2. Navigate to Tasks page
3. **Verify**: Task appears with:
   - Badge: "2️⃣ Technical Review" (purple/secondary color)
   - Review Path: Alice (outlined) → Bob (filled)
   - Assigned to: Bob
4. Open the task
5. Review and approve the task
6. Mark as "COMPLETED"

**Expected Backend Logs**:
```
🔄 Auto-completing workflow job: <job-key>
   ✅ Workflow job completed successfully!
   
🔄 Updating document status: <doc-id> -> RELEASED
   ✓ Document status updated successfully
   
📧 Sending completion notification for document: <doc-id>
   Final status: RELEASED
```

**Expected Frontend**:
- Bob's task disappears
- Document status changes to "RELEASED"
- Document appears in "Released" documents list

---

### ✅ Test 4: Rejection Flow - Initial Review Rejects

**Steps**:
1-6. Same as Test 1
7. Login as Alice
8. Open initial review task
9. **Reject** the document (mark as COMPLETED with approved=false)

**Expected**:
- Workflow proceeds to technical review is **skipped**
- Document status changes to "IN_WORK"
- Document creator is notified of rejection

---

### ✅ Test 5: Rejection Flow - Technical Review Rejects

**Steps**:
1-6. Same as Test 1
7-9. Alice approves (Test 2)
10. Login as Bob
11. Open technical review task
12. **Reject** the document

**Expected**:
- Document status changes to "IN_WORK"
- Document creator is notified of rejection
- Alice's approval is recorded but document still rejected

---

### ✅ Test 6: Validation - Cannot Select Same Reviewer Twice

**Steps**:
1. Start document submission
2. Select Alice as Initial Reviewer
3. Try to select Alice as Technical Reviewer

**Expected**:
- Alice is disabled/unavailable in Technical Reviewer column
- Cannot submit until two different reviewers are selected

---

### ✅ Test 7: Backward Compatibility - Legacy Single Reviewer

**Steps**:
1. Use legacy API format:
```json
POST /api/workflows/document-approval/start
{
  "documentId": "doc-123",
  "reviewerIds": ["3"]
}
```

**Expected**:
- Uses `document-approval` process (not `document-approval-with-review`)
- Single-stage review workflow
- Works as before

---

## 📊 Test Checklist

### Backend Tests
- [ ] BPMN file `document-approval-with-review.bpmn` deploys successfully
- [ ] Two new workers register: `wait-for-initial-review` and `wait-for-technical-review`
- [ ] Task entity saves `initialReviewer`, `technicalReviewer`, `reviewStage` fields
- [ ] Initial review task creates with correct reviewer
- [ ] Auto-progression from initial to technical review
- [ ] Technical review task creates with correct reviewer
- [ ] Workflow completes successfully on approval
- [ ] Workflow handles rejection correctly
- [ ] Legacy API still works

### Frontend Tests
- [ ] Reviewer dialog shows two-column layout
- [ ] Can select different reviewers for each stage
- [ ] Cannot select same person for both stages
- [ ] Review path preview displays correctly
- [ ] Task list shows review stage badges
- [ ] Task cards display both reviewers
- [ ] Current stage reviewer is highlighted (filled chip)
- [ ] Next stage reviewer is outlined
- [ ] Task transitions work smoothly

### Integration Tests
- [ ] End-to-end approval flow completes
- [ ] End-to-end rejection flow completes
- [ ] Task notifications work
- [ ] Document status updates correctly
- [ ] Workflow history logs correctly
- [ ] No errors in console logs
- [ ] Database persists all fields correctly

---

## 🐛 Common Issues & Solutions

### Issue 1: BPMN Not Deploying
**Solution**: Check `workflow-orchestrator` logs for deployment errors. Ensure the BPMN file is valid XML.

### Issue 2: Workers Not Registering
**Solution**: Verify `DocumentWorkflowWorkers.registerWorkers()` is called on startup. Check for Zeebe connection issues.

### Issue 3: Tasks Not Creating
**Solution**: Check TaskServiceClient Feign configuration. Ensure task-service is running on port 8082.

### Issue 4: Review Stage Not Showing in UI
**Solution**: Verify task entity has the new fields. Check frontend is reading `reviewStage`, `initialReviewer`, `technicalReviewer` from task object.

### Issue 5: Auto-progression Not Working
**Solution**: Check that `workflowJobKey` is being stored in tasks. Verify TaskController status update endpoint completes workflow jobs.

---

## 📝 Manual Test Script

```bash
# Terminal 1: Start Workflow Orchestrator
cd workflow-orchestrator
mvn clean spring-boot:run

# Terminal 2: Start Document Service
cd document-service
mvn clean spring-boot:run

# Terminal 3: Start Task Service
cd task-service
mvn clean spring-boot:run

# Terminal 4: Start User Service (if separate)
cd user-service
mvn clean spring-boot:run

# Terminal 5: Start Frontend
cd frontend
npm start
```

**Test API Endpoints**:

```bash
# 1. Create test document
curl -X POST http://localhost:8081/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "masterId": "TEST-DOC-001",
    "title": "Test Document for Two-Stage Review",
    "description": "Testing two-stage review workflow",
    "creator": "testuser",
    "stage": "PROTOTYPE"
  }'

# 2. Submit for two-stage review
curl -X POST http://localhost:8081/api/documents/{doc-id}/submit-review \
  -H "Content-Type: application/json" \
  -d '{
    "user": "testuser",
    "initialReviewer": "3",
    "technicalReviewer": "5",
    "twoStageReview": true
  }'

# 3. Check created tasks
curl http://localhost:8082/tasks

# 4. Check workflow instance
# (Use Camunda Operate UI or Zeebe CLI)
```

---

## ✅ Success Criteria

All tests pass when:
1. ✅ Two-stage review workflow completes successfully
2. ✅ Tasks are created sequentially for each reviewer
3. ✅ Auto-progression works between stages
4. ✅ UI shows review stage indicators correctly
5. ✅ Both approval and rejection paths work
6. ✅ Legacy single-reviewer mode still functions
7. ✅ No errors in backend or frontend logs
8. ✅ Database correctly stores all review metadata

---

**Test Status**: 🟡 Ready for Testing
**Last Updated**: 2024-01-18
**Tester**: [Your Name]


# Quick Fix Summary: Two-Stage Review Issues

## What Was Wrong?

### 1. ❌ Document status stayed "INREVIEW" after first reviewer finished
**Why:** No intermediate status update in the workflow

### 2. ❌ Workflow stuck after second reviewer finished  
**Why:** UI wasn't passing the `approved` parameter when completing tasks

---

## What Was Fixed?

### ✅ Added Intermediate Status Update
The workflow now updates the document status to `IN_TECHNICAL_REVIEW` after the initial review completes.

### ✅ Documented Required API Changes
Your UI needs to pass `approved: "true"` or `approved: "false"` when marking tasks as COMPLETED.

---

## What You Need to Do

### Step 1: Restart Workflow-Orchestrator ⚡ REQUIRED
```bash
# Stop the current workflow-orchestrator
# Then restart:
cd workflow-orchestrator
mvn spring-boot:run
```

This deploys the updated BPMN with the intermediate status update.

### Step 2: Update Your UI Code ⚡ REQUIRED

When a reviewer clicks "Approve" or "Reject", your UI must include the `approved` parameter:

**BEFORE (Wrong):**
```javascript
PUT /tasks/{id}/status
{
  "status": "COMPLETED",
  "comments": "Looks good"
}
```

**AFTER (Correct):**
```javascript
PUT /tasks/{id}/status
{
  "status": "COMPLETED",
  "approved": "true",  // ← ADD THIS!
  "comments": "Looks good"
}
```

For rejection:
```javascript
{
  "status": "COMPLETED",
  "approved": "false",  // ← THIS TRIGGERS REJECTION
  "comments": "Needs changes"
}
```

### Step 3: Add IN_TECHNICAL_REVIEW Status (If Needed)

Make sure your document-service recognizes `IN_TECHNICAL_REVIEW` as a valid status.

---

## Test It

1. **Start a new workflow** with initial reviewer = 1, technical reviewer = 2
2. **Initial reviewer approves** → Document should change to `IN_TECHNICAL_REVIEW`
3. **Technical reviewer approves** → Document should change to `RELEASED`
4. **Done!** ✅

---

## Full Documentation

See `TWO_STAGE_REVIEW_FIXES.md` for:
- Detailed explanations
- Code examples
- Troubleshooting guide
- Testing scenarios



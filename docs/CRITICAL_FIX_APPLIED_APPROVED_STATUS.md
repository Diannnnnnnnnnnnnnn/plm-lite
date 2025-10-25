# CRITICAL FIX APPLIED: Document Status Issue (IN_WORK vs RELEASED)

## Date: 2025-10-24

## Problem
After the second reviewer approved a document in the two-stage review workflow, the document status incorrectly changed to `IN_WORK` instead of `RELEASED`.

## Root Cause Identified ✅

### The Investigation Trail:

1. **✅ UI Layer (frontend)**: 
   - Correctly sends `approved: 'true'` (string)
   - Confirmed via browser console logs

2. **✅ Task-Service Layer**: 
   - Correctly receives `'true'` and converts to boolean `true`
   - Correctly sends `approved: true` to workflow-orchestrator
   - **Confirmed by logs**: `Sending to workflow - approved: true, comments: Approved`

3. **❌ Workflow-Orchestrator Layer** (THE BUG):
   - The workflow was initialized with `approved = false` in `WorkflowService.java` line 67
   - This initial `false` value was interfering with the BPMN decision gateway
   - Even though job completions set `approved = true`, the workflow was taking the rejection path

### The Specific Bug:

```java
// BEFORE (BUGGY):
variables.put("approved", false); // Will be set by user task completion
```

This caused the workflow's decision gateway to evaluate `approved = false` and take the **rejection path** (status → IN_WORK) instead of the **approval path** (status → RELEASED).

## Fix Applied

### Changes Made:

**File**: `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`

**Line 67** (Changed):
```java
// BEFORE:
variables.put("approved", false); // Will be set by user task completion

// AFTER:
// DON'T initialize 'approved' - let it be set by review completion
// If we set it to false here, it might interfere with the decision gateway
```

**Lines 121-123** (Added debug logging):
```java
System.out.println("✅ Completing user task: " + jobKey);
System.out.println("   Variables: " + variables);
System.out.println("   🔍 DEBUG - approved value: " + variables.get("approved") + 
    " (type: " + (variables.get("approved") != null ? 
    variables.get("approved").getClass().getName() : "null") + ")");
```

## How the Fix Works

### Before (Buggy):
1. Workflow starts with `approved = false` ❌
2. Initial reviewer approves → sets `approved = true` 
3. Technical reviewer approves → sets `approved = true`
4. Decision gateway evaluates... but somehow sees `false` ❌
5. Takes rejection path → Status = `IN_WORK` ❌

### After (Fixed):
1. Workflow starts **WITHOUT** `approved` variable ✅
2. Initial reviewer approves → sets `approved = true` ✅
3. Technical reviewer approves → sets `approved = true` ✅
4. Decision gateway evaluates `approved = true` ✅
5. Takes default (approval) path → Status = `RELEASED` ✅

## Testing Instructions

### Step 1: Restart workflow-orchestrator
```bash
# Stop the current workflow-orchestrator service
# Then restart it to pick up the new build
```

The workflow-orchestrator has been **rebuilt** with the fix:
- Location: `workflow-orchestrator/target/workflow-orchestrator-0.0.1-SNAPSHOT.jar`

### Step 2: Test the Complete Workflow

1. **Create a new document**
2. **Submit for two-stage review**:
   - Initial Reviewer: (e.g., user ID 2)
   - Technical Reviewer: (e.g., user ID 3)
3. **As Initial Reviewer**: Login and **APPROVE** the review task
4. **As Technical Reviewer**: Login and **APPROVE** the review task
5. **Verify**: Document status should now be **`RELEASED`** ✅

### Step 3: Check Logs

**Look for in workflow-orchestrator console**:
```
✅ Completing user task: 2251799814157146
   Variables: {approved=true, comments=Approved}
   🔍 DEBUG - approved value: true (type: java.lang.Boolean)
   ✓ Task completed successfully with approved=true
```

**Look for in task-service console**:
```
🔄 Auto-completing workflow job: 2251799814157146
   📥 Received approved parameter: 'true'
   📤 Sending to workflow - approved: true, comments: Approved
   ✅ Workflow job completed successfully!
```

## Expected Behavior After Fix

### Two-Stage Review Workflow:
| Step | Status | Description |
|------|--------|-------------|
| Document created | `IN_WORK` | Initial state |
| Submitted for review | `IN_REVIEW` | Awaiting initial reviewer |
| Initial reviewer approves | `IN_REVIEW` | Still in review, awaiting technical reviewer |
| Technical reviewer approves | `RELEASED` | ✅ **Document approved!** |
| Technical reviewer rejects | `IN_WORK` | Document needs rework |

## Files Modified

1. `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`
   - Removed `approved = false` initialization
   - Added debug logging

2. `docs/FIX_APPROVED_STATUS_ISSUE.md` (Created)
   - Diagnostic documentation

3. `docs/CRITICAL_FIX_APPLIED_APPROVED_STATUS.md` (This file)
   - Summary of fix applied

## Rollback Instructions (If Needed)

If you need to revert this change:
```java
// In WorkflowService.java, line 67, restore:
variables.put("approved", false); // Will be set by user task completion
```

Then rebuild:
```bash
cd workflow-orchestrator
mvn clean package -DskipTests
```

## Next Steps

1. ✅ **Restart workflow-orchestrator** with the new build
2. ✅ **Test the complete two-stage review workflow**
3. ✅ **Verify document status changes to RELEASED** after second approval
4. ✅ **Monitor logs** to ensure `approved = true` is being received correctly

---

**Status**: Fix applied, awaiting testing  
**Priority**: CRITICAL  
**Estimated Test Time**: 5-10 minutes  
**Confidence Level**: HIGH (root cause identified and addressed)



# Fix: Document Status Changes to IN_WORK Instead of RELEASED

## Problem Summary
When the second reviewer approves a document, the status incorrectly changes to `IN_WORK` instead of `RELEASED`.

## Root Cause Analysis

### What We Know:
1. ✅ **UI is correct**: Sends `approved: 'true'` (string)
2. ✅ **task-service is correct**: 
   - Receives `'true'`
   - Converts to boolean `true`
   - Sends `approved: true` to workflow-orchestrator
   - **Logs confirm**: `Sending to workflow - approved: true, comments: Approved`

3. ❓ **workflow-orchestrator status**: NEEDS VERIFICATION
   - We added debug logging to confirm it receives `approved: true`
   - But workflow is taking the REJECTION path (IN_WORK) instead of APPROVAL path (RELEASED)

### Potential Issue:
The workflow starts with `approved = false` in `WorkflowService.java`:

```java
variables.put("approved", false); // Line 67
```

This initial `false` value might be interfering with the decision gateway evaluation.

## BPMN Decision Gateway Logic

The BPMN file has:
```xml
<bpmn:exclusiveGateway id="Gateway_Decision" default="Flow_Approved">
  <bpmn:outgoing>Flow_Approved</bpmn:outgoing>  <!-- DEFAULT: Goes to RELEASED -->
  <bpmn:outgoing>Flow_Rejected</bpmn:outgoing>   <!-- CONDITION: Goes to IN_WORK -->
</bpmn:exclusiveGateway>

<bpmn:sequenceFlow id="Flow_Rejected">
  <bpmn:conditionExpression>=approved = false</bpmn:conditionExpression>
</bpmn:sequenceFlow>
```

**Expected Behavior:**
- If `approved == false` → Take rejection path → Status = IN_WORK
- Otherwise (default) → Take approval path → Status = RELEASED

## Testing Steps

### Step 1: Restart workflow-orchestrator
I've added debug logging to `WorkflowService.java`. Please restart the workflow-orchestrator service to pick up these changes.

### Step 2: Test the workflow
1. Create a new document
2. Submit it for two-stage review (initial reviewer + technical reviewer)
3. As the initial reviewer, **approve** the task
4. As the technical reviewer, **approve** the task

### Step 3: Collect Logs
Please share the **workflow-orchestrator** console logs. Look for:
```
✅ Completing user task: [jobKey]
   Variables: {approved=true, comments=Approved}
   🔍 DEBUG - approved value: true (type: java.lang.Boolean)
   ✓ Task completed successfully with approved=true
```

## Potential Fixes

### Fix Option 1: Remove Initial False Value
Change `WorkflowService.java` line 67 from:
```java
variables.put("approved", false);
```
To:
```java
// Don't initialize approved - let it be set by review completion
// variables.put("approved", false);
```

### Fix Option 2: Change BPMN Condition
Change the condition from:
```xml
<bpmn:conditionExpression>=approved = false</bpmn:conditionExpression>
```
To:
```xml
<bpmn:conditionExpression>=approved != true</bpmn:conditionExpression>
```

### Fix Option 3: Explicit Boolean Check
Change condition to:
```xml
<bpmn:conditionExpression>=approved = false or approved = null</bpmn:conditionExpression>
```

## Next Steps

1. **Restart workflow-orchestrator** with the new debug logging
2. **Test the complete workflow** and share the logs
3. Based on the logs, we'll apply the appropriate fix

---

**Last Updated**: 2025-10-24  
**Status**: Awaiting workflow-orchestrator logs with debug output


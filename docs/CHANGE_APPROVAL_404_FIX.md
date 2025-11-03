# Change Approval 404 Error Fix

## Error
```
PUT http://localhost:8084/api/changes/991e9024-4c01-4662-9be4-0914bda152d2/approve 404 (Not Found)
Error approving change 991e9024-4c01-4662-9be4-0914bda152d2
```

## Root Cause
The change with ID `991e9024-4c01-4662-9be4-0914bda152d2` **does not exist** in the change-service database.

### Why This Happens:
1. A task was created with:
   - `contextType` = "CHANGE"
   - `contextId` = "991e9024-4c01-4662-9be4-0914bda152d2"

2. But the actual change was **never created** in the change-service database

3. When the user tries to approve the change, the backend code throws an exception:
```java
Change change = changeRepository.findById(changeId)
    .orElseThrow(() -> new RuntimeException("Change not found"));
```

## Solution

### 1. ✅ Better Error Handling (Frontend)
Added specific error messages for different failure scenarios:

```javascript
try {
  await changeService.approveChange(changeId);
  // ... success handling
} catch (error) {
  if (error.response && error.response.status === 404) {
    alert(`Change not found (ID: ${changeId}). 
          The change may have been deleted or 
          the task contains an invalid change reference.`);
  } else if (error.response && error.response.status === 400) {
    alert('Change cannot be approved. 
          It may not be in review status.');
  } else {
    alert(`Failed to approve change: ${error.message || 'Unknown error'}`);
  }
  return; // Don't mark task as completed
}
```

### 2. ⚠️ Prevention: Validate Change Exists Before Creating Task

The change-service should validate that changes exist before creating tasks:

**Current Code (ChangeServiceDev.java:246):**
```java
taskServiceClient.createTaskWithContext(request);
```

**Should Be:**
```java
// Verify change exists before creating task
Change change = changeRepository.findById(changeId)
    .orElseThrow(() -> new RuntimeException("Cannot create task for non-existent change"));

taskServiceClient.createTaskWithContext(request);
```

## How to Reproduce

### The Problem:
1. A task references a change that doesn't exist
2. User tries to approve the change through the task
3. Gets 404 error

### To Test the Fix:
1. **Create a valid change:**
```bash
POST http://localhost:8084/api/changes
{
  "title": "Test Change",
  "changeReason": "Testing",
  "creator": "testuser"
}
```

2. **Submit for review:**
```bash
PUT http://localhost:8084/api/changes/{changeId}/submit-review
{
  "reviewers": ["reviewer1"]
}
```

3. **Approve through task:**
- Login as reviewer
- Open task in TaskManager
- Click "Approve"
- ✅ Should work correctly

## Port Configuration

| Service | Port | Endpoint |
|---------|------|----------|
| user-service | 8083 | http://localhost:8083 |
| change-service | 8084 | http://localhost:8084/api |
| task-service | 8082 | http://localhost:8082/api |
| document-service | 8081 | http://localhost:8081/api/v1 |

## Backend Error Handling

**ChangeController.java:**
```java
@PutMapping("/{changeId}/approve")
public ResponseEntity<ChangeResponse> approveChange(@PathVariable String changeId) {
    try {
        ChangeResponse response = changeServiceDev != null ?
            changeServiceDev.approveChange(changeId) :
            changeService.approveChange(changeId);
        return ResponseEntity.ok(response);
    } catch (IllegalStateException e) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);  // 400
    } catch (RuntimeException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);    // 404
    } catch (Exception e) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 500
    }
}
```

**Error Types:**
- **404 (NOT_FOUND)**: Change doesn't exist in database
- **400 (BAD_REQUEST)**: Change not in correct status (e.g., not IN_REVIEW)
- **500 (INTERNAL_SERVER_ERROR)**: Other errors

## Recommended Improvements

### 1. Add Change Existence Validation
When creating tasks in `ChangeServiceDev.submitForReview()`:

```java
// Before creating tasks, verify change exists
if (!changeRepository.existsById(changeId)) {
    throw new RuntimeException("Change not found: " + changeId);
}

// Then create tasks
for (String reviewerId : reviewers) {
    // ... create task with contextId = changeId
}
```

### 2. Add Cascade Delete
If a change is deleted, delete associated tasks:

```java
@PreRemove
public void preRemove() {
    // Delete associated tasks when change is deleted
    taskServiceClient.deleteTasksByContextId("CHANGE", this.id);
}
```

### 3. Add Change Status Validation
Before allowing approval:

```java
if (change.getStatus() != Status.IN_REVIEW) {
    throw new IllegalStateException(
        "Change must be in IN_REVIEW status to approve. " +
        "Current status: " + change.getStatus()
    );
}
```

### 4. Better Frontend Error Messages
- Show the changeId in error message ✅ (Done)
- Provide link to change details page
- Suggest actions (contact admin, refresh, etc.)

## Files Modified

1. **Frontend:**
   - `frontend/src/components/Tasks/TaskManager.js`
     - Added nested try-catch for change approval
     - Added specific error messages for 404 and 400 errors
     - Better logging

2. **Backend (needs improvement):**
   - `change-service/.../ChangeServiceDev.java`
     - Should add validation before creating tasks
     - Should handle missing changes gracefully

## Testing Checklist

- [ ] Create change successfully
- [ ] Submit change for review
- [ ] Task created with correct contextId
- [ ] Approve change through task UI
- [ ] ✅ Success message shown
- [ ] Task marked as completed
- [ ] Change status updated to RELEASED
- [ ] Test with non-existent change ID
- [ ] ✅ User-friendly error message shown
- [ ] Task NOT marked as completed on error

## Summary

**Issue:** Task references non-existent change → 404 error on approval

**Immediate Fix:** ✅ Better error messages in frontend

**Long-term Fix:** ⚠️ Validate change exists before creating tasks

**User Experience:** Users now see helpful error message instead of generic failure


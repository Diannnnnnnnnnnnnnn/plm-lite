# Frontend Compatibility Fix

## Issue
Frontend JavaScript was failing to extract change ID from tasks with error:
```
TaskManager.js:603 Could not extract change ID from task description
```

## Root Cause
**Field name mismatch** between backend API and frontend code:

### Backend (TaskResponse DTO)
```java
private String taskDescription;  // New API field name
```

### Frontend (TaskManager.js:595)
```javascript
if (task.description) {  // Old API field name
```

The new TaskResponse DTO used `taskDescription` and `taskName`, but the frontend expected the old field names `description` and `name`.

## Solution
Added **backward-compatible field aliases** to TaskResponse DTO using Jackson annotations:

```java
// Primary field names (new API)
@JsonProperty("taskName")
private String taskName;

@JsonProperty("taskDescription")  
private String taskDescription;

// Backward compatibility getters (old API)
@JsonProperty("name")
public String getName() { return taskName; }

@JsonProperty("description")
public String getDescription() { return taskDescription; }
```

## Result
The JSON response now includes **both** field names:

```json
{
  "id": "123",
  "taskName": "Review Change: ECR-001",
  "name": "Review Change: ECR-001",              // ✅ Frontend can use this
  "taskDescription": "Please review change...",
  "description": "Please review change...",      // ✅ Frontend can use this
  "taskType": "REVIEW",
  "taskStatus": "PENDING",
  "contextType": "CHANGE",
  "contextId": "change-uuid-123"
}
```

## Frontend Extraction Logic
The frontend extracts change ID using this pattern (line 596):
```javascript
const match = task.description.match(/review change\s+([a-f0-9-]+)/i);
```

This matches the change service's task description format (ChangeServiceDev.java:238):
```java
request.setTaskDescription("Please review change " + changeId + " - " + change.getChangeReason());
```

✅ **Pattern matches correctly** when changeId is a UUID (e.g., `abc123-def456-...`)

## Files Modified
1. `task-service/src/main/java/com/example/task_service/dto/TaskResponse.java`
   - Added `@JsonProperty` annotations
   - Added `getName()` and `getDescription()` alias getters
   - Maintains backward compatibility with old frontend code

## Testing
To verify the fix works:

1. **Create a change review task:**
```bash
POST http://localhost:8083/api/changes/submitForReview
{
  "changeId": "test-change-123"
}
```

2. **Check task response includes both field names:**
```bash
GET http://localhost:8082/api/tasks?assignedTo=username
```

Expected JSON should include both `description` and `taskDescription` fields.

3. **Frontend should now extract change ID successfully:**
- Open TaskManager in browser
- Click on change review task
- Click "Approve" button
- Should NOT show "Could not extract change ID" error

## Additional Notes

### Other GET Endpoints
The legacy GET endpoints still return the `Task` entity directly, which already has `name` and `description` fields:

```java
@GetMapping
public List<Task> getAllTasks()  // Returns Task entity

@GetMapping("/{id}")
public Optional<Task> getTaskById()  // Returns Task entity
```

These continue to work with the frontend because the Task entity uses `name` and `description` field names.

### New POST Endpoint
Only the new POST endpoint returns `TaskResponse`:

```java
@PostMapping
public ResponseEntity<?> createTask(@Valid @RequestBody CreateTaskRequest request)
// Returns TaskResponse with both old and new field names
```

## Backward Compatibility Matrix

| API Response | Old Frontend (task.name) | Old Frontend (task.description) | New Client (task.taskName) | New Client (task.taskDescription) |
|--------------|--------------------------|--------------------------------|---------------------------|----------------------------------|
| GET /api/tasks (Task entity) | ✅ Works | ✅ Works | ❌ Not available | ❌ Not available |
| POST /api/tasks (TaskResponse) | ✅ Works (alias) | ✅ Works (alias) | ✅ Works | ✅ Works |

This ensures **full backward compatibility** while supporting the new API field names!


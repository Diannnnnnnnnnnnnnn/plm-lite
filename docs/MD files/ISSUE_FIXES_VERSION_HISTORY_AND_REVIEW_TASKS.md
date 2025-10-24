# Issue Fixes: Version History & Review Tasks

## 🐛 Issues Reported

### Issue 1: Version History Showing Other Documents
**Problem**: When creating a new document, the version history shows other unrelated documents.

### Issue 2: Review Tasks Not Visible to Reviewers
**Problem**: After submitting a document for review, the reviewer doesn't see the review task.

---

## 🔍 Root Causes

### Issue 1: Shared Master ID
**Cause**: Multiple documents are sharing the same `masterId`, causing them to appear in each other's version history.

**How it happens:**
```
User creates Document A with documentNumber: "DOC-001"
→ masterId = "DOC-001"

User creates Document B with documentNumber: "DOC-001"  ← Same number!
→ masterId = "DOC-001"

Now when viewing version history for either document:
→ Backend query: findByMaster_IdOrderByRevisionDescVersionDesc("DOC-001")
→ Returns: [Document A versions + Document B versions] ❌
```

**Expected behavior**: Each document should have a unique `masterId`, so version history only shows versions of THAT specific document.

---

### Issue 2: Missing `assignedTo` Field in Review Tasks
**Cause**: When creating review tasks, the workflow service and change service were not setting the `assignedTo` field (username), only setting `userId` (number).

**How it happens:**
```java
// OLD CODE (BROKEN):
CreateTaskRequest taskRequest = new CreateTaskRequest();
taskRequest.setName("Review Document...");
taskRequest.setUserId(123L);
// ❌ Missing: taskRequest.setAssignedTo("vivi");

// Result in database:
Task {
  id: 1,
  name: "Review Document...",
  userId: 123,
  assignedTo: null  ← NULL!
}

// Frontend filter:
GET /tasks?assignedTo=vivi
→ Returns: [] (empty, because assignedTo is null)
```

**Expected behavior**: Tasks should have both `userId` AND `assignedTo` (username) so filtering works correctly.

---

## ✅ Fixes Implemented

### Fix 1: Version History Issue

**Solution**: Ensure each document gets a unique `masterId`.

**Current Backend Logic** (already correct):
```java
// document-service/DocumentServiceImpl.java
public Document create(CreateDocumentRequest req) {
    DocumentMaster master = masterRepo.findById(req.getMasterId()).orElseGet(() -> {
        // If masterId doesn't exist, create new DocumentMaster
        DocumentMaster m = new DocumentMaster();
        m.setId(req.getMasterId());  // ← This must be unique!
        m.setTitle(req.getTitle());
        return masterRepo.save(m);
    });
    // ... rest of document creation
}
```

**Frontend Recommendation**: Generate unique document numbers automatically:

```javascript
// OPTION 1: Auto-generate unique masterId
const handleCreateDocument = async () => {
  const uniqueMasterId = `DOC-${Date.now()}-${Math.random().toString(36).substring(7)}`;
  
  const documentData = {
    title: newDocument.title,
    documentNumber: newDocument.documentNumber, // User-friendly display number
    masterId: uniqueMasterId, // ← Unique identifier for versioning
    // ...
  };
  
  await documentService.uploadDocument(documentData, selectedFile, getCurrentUsername());
};

// OPTION 2: Use backend auto-generation
// Let the backend generate masterId automatically if not provided
```

**User Action Required**: 
- ⚠️ **Don't reuse document numbers** for different documents
- Use unique identifiers for each new document
- Consider implementing auto-generation of document numbers

---

### Fix 2: Review Tasks Not Visible

**Solution**: Set `assignedTo` field when creating review tasks.

#### **A. Workflow Orchestrator Service**

**File**: `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java`

**Changes**:

1. **Added user service client** to fetch usernames:
```java
@FeignClient(name = "user-service", url = "http://localhost:8083")
interface UserServiceClient {
    @GetMapping("/users/by-username/{username}")
    UserResponse getUserByUsername(@PathVariable("username") String username);
    
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);  // ← NEW
}
```

2. **Updated `CreateTaskRequest` class** to include `assignedTo`:
```java
class CreateTaskRequest {
    private String name;
    private String description;
    private Long userId;
    private String assignedTo; // ← NEW: Username for task filtering

    // Getters and setters...
}
```

3. **Modified `createReviewTasks` method** to fetch and set username:
```java
public void createReviewTasks(String documentId, String masterId, String version, 
                               String creator, List<String> reviewers) {
    for (String reviewerId : reviewers) {
        try {
            Long userId = Long.parseLong(reviewerId);

            // ✅ NEW: Fetch username from user service
            String username = null;
            try {
                UserResponse user = userServiceClient.getUserById(userId);
                username = user.getUsername();
                System.out.println("Resolved user ID " + userId + " to username: " + username);
            } catch (Exception e) {
                System.err.println("Failed to fetch username: " + e.getMessage());
            }

            CreateTaskRequest taskRequest = new CreateTaskRequest();
            taskRequest.setName("Review Document: " + masterId + " " + version + " [" + documentId + "]");
            taskRequest.setDescription("Please review document...");
            taskRequest.setUserId(userId);
            taskRequest.setAssignedTo(username); // ✅ NEW: Set username

            TaskResponse response = taskServiceClient.createTask(taskRequest);
            System.out.println("Created task for user " + userId + " (username: " + username + ")");
        } catch (Exception e) {
            System.err.println("Failed to create task: " + e.getMessage());
        }
    }
}
```

#### **B. Change Service**

**File**: `change-service/src/main/java/com/example/plm/change/client/TaskServiceClient.java`

**Changes**:
```java
class TaskDTO {
    private String name;
    private String description;
    private Long userId;
    private String assignedTo; // ← NEW

    public TaskDTO(String name, String description, Long userId, String assignedTo) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.assignedTo = assignedTo; // ← NEW
    }

    // Getters and setters...
}
```

**File**: `change-service/src/main/java/com/example/plm/change/service/ChangeServiceDev.java`

**Changes**:

1. **Added user service client**:
```java
@Autowired(required = false)
private UserServiceClient userServiceClient;

@FeignClient(name = "user-service-dev", url = "http://localhost:8083")
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
```

2. **Updated task creation in `submitForReview`**:
```java
for (String reviewerId : reviewerIds) {
    try {
        Long userId = Long.parseLong(reviewerId);
        
        // ✅ Fetch username from user service
        String username = null;
        if (userServiceClient != null) {
            try {
                UserResponse user = userServiceClient.getUserById(userId);
                username = user.getUsername();
            } catch (Exception e) {
                System.err.println("Failed to fetch username: " + e.getMessage());
            }
        }
        
        TaskServiceClient.TaskDTO task = new TaskServiceClient.TaskDTO(
            "Review Change: " + change.getTitle(),
            "Please review change " + changeId,
            userId,
            username // ✅ Pass username
        );
        taskServiceClient.createTask(task);
    } catch (Exception e) {
        System.err.println("Failed to create task: " + e.getMessage());
    }
}
```

---

## 🔄 Data Flow After Fix

### Review Task Creation Flow:

```
1. User submits document for review with reviewerIds: [1, 2]
   ↓
2. Document Service → Workflow Service
   POST /workflow/reviews/start
   Body: ["1", "2"]
   ↓
3. Workflow Service processes each reviewer:
   For reviewerId "1":
     a. Parse userId: 1
     b. Call User Service: GET /users/1
     c. Response: { "id": 1, "username": "vivi" }
     d. Create task with:
        - userId: 1
        - assignedTo: "vivi" ✅
   ↓
4. Task Service saves task:
   {
     "id": 123,
     "name": "Review Document: DOC-001 v0.1",
     "userId": 1,
     "assignedTo": "vivi" ✅
   }
   ↓
5. Frontend fetches tasks:
   User "vivi" logs in
   → GET /tasks?assignedTo=vivi
   → Returns: [task 123] ✅
```

---

## 📊 Testing

### Test Fix 1: Version History

**Steps**:
1. Create a new document with a unique document number (e.g., "TEST-DOC-001")
2. Open the document details
3. Check the "Version History" section
4. **Expected**: Should only show versions of THIS document
5. **Verify**: No other documents appear in the history

**If still broken**:
- Check if `masterId` is unique for each document
- Look in document service logs for duplicate masterId warnings

---

### Test Fix 2: Review Tasks

**Steps**:
1. Login as user "vivi" (or any user)
2. Create and submit a document for review
3. Select reviewer: user "guodian" (ID: 2)
4. Submit for review
5. Logout
6. Login as "guodian"
7. Navigate to Task Management
8. **Expected**: Should see the review task
9. **Verify**: Task details show "Review Document: ..."

**Debug if broken**:
```bash
# Check workflow-orchestrator logs:
tail -f workflow-orchestrator/logs/application.log

# Look for:
"Resolved user ID 2 to username: guodian" ✅
"Created task for reviewer ID 2 (username: guodian)" ✅

# Check task-service database:
SELECT id, name, user_id, assigned_to FROM task;

# Should show:
# id | name                    | user_id | assigned_to
# ---+-------------------------+---------+------------
#  1 | Review Document: ...    |    2    | guodian    ✅
```

---

## 🗃️ Database Cleanup

### Clear Old Tasks Without `assignedTo`

```sql
-- Connect to task service database (H2)
-- Location: task-service/data/taskdb

-- View existing tasks:
SELECT id, name, user_id, assigned_to FROM task;

-- Option 1: Delete all old tasks (recommended for testing):
DELETE FROM task;

-- Option 2: Update existing tasks (if you want to keep them):
-- This requires knowing the username for each userId
UPDATE task SET assigned_to = 'vivi' WHERE user_id = 1;
UPDATE task SET assigned_to = 'guodian' WHERE user_id = 2;
```

Or simply delete the database file and restart:
```powershell
# Stop services
Stop-Process -Name java -Force -ErrorAction SilentlyContinue

# Delete task database
Remove-Item -Path "task-service\data\taskdb*.db" -Force

# Restart services
.\start-all-services.bat
```

---

## 📝 Files Modified

| File | Purpose | Status |
|------|---------|--------|
| `workflow-orchestrator/src/main/java/com/example/plm/workflow/service/WorkflowService.java` | Add `assignedTo` to document review tasks | ✅ Compiled |
| `change-service/src/main/java/com/example/plm/change/client/TaskServiceClient.java` | Add `assignedTo` field to TaskDTO | ✅ Compiled |
| `change-service/src/main/java/com/example/plm/change/service/ChangeServiceDev.java` | Add user service client and set `assignedTo` in change review tasks | ✅ Compiled |

---

## 🚀 Deployment Steps

1. **Stop all services**:
```powershell
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
```

2. **Clean task database** (optional but recommended):
```powershell
Remove-Item -Path "task-service\data\taskdb*.db" -Force
```

3. **Restart all services**:
```powershell
.\start-all-services.bat
```

4. **Verify services are running**:
- Document Service: http://localhost:8081
- Task Service: http://localhost:8082
- User Service: http://localhost:8083
- Workflow Orchestrator: http://localhost:8086
- Change Service: http://localhost:8087

5. **Test the fixes** (see Testing section above)

---

## ⚠️ Important Notes

### Version History Issue:
- The backend logic is correct - it queries by `masterId`
- **User must ensure each document has a unique `masterId`**
- Consider implementing auto-generation of unique document identifiers
- Do NOT reuse document numbers for different documents

### Review Tasks Issue:
- Fixed in backend services (workflow-orchestrator and change-service)
- **Old tasks in the database won't have `assignedTo` field populated**
- Recommend clearing task database or updating existing tasks manually
- New review tasks will automatically have `assignedTo` set correctly

---

## 🎯 Summary

| Issue | Root Cause | Fix | Status |
|-------|------------|-----|--------|
| Version history showing other documents | Shared `masterId` between different documents | Ensure unique `masterId` for each document | ⚠️ User Action Required |
| Review tasks not visible to reviewers | Missing `assignedTo` field in task creation | Set `assignedTo` (username) when creating tasks | ✅ Fixed |

---

*Last Updated: October 19, 2025*
*Services Compiled: workflow-orchestrator, change-service*
*Database Reset Recommended: task-service*


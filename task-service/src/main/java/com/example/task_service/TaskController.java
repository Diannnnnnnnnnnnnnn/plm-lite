package com.example.task_service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.task_service.client.WorkflowOrchestratorClient;
import com.example.task_service.dto.CreateTaskRequest;
import com.example.task_service.dto.TaskResponse;
import com.example.task_service.model.SignoffAction;
import com.example.task_service.model.TaskSignoff;
import com.example.task_service.model.neo4j.TaskNode;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/tasks")  // Unified controller for all task operations
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Zeebe client removed - task-service no longer starts processes directly

    @Autowired
    private WorkflowOrchestratorClient workflowClient;

    @GetMapping
    public List<Task> getAllTasks(@RequestParam(required = false) String assignedTo) {
        // If assignedTo parameter is provided, filter by that user
        // This ensures users only see tasks assigned to them
        if (assignedTo != null && !assignedTo.isEmpty()) {
            return taskService.getTasksByAssignedTo(assignedTo);
        }
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Optional<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // NEW: Modern JSON-based task creation endpoint (accepts CreateTaskRequest)
    // This endpoint is used by workflow-orchestrator and change-service
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            TaskResponse response = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // If CreateTaskRequest fails, try legacy Task entity
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid request format: " + e.getMessage()));
        }
    }
    
    // LEGACY: Support for raw Task entity (kept for backward compatibility)
    @PostMapping("/legacy")
    public Task addTaskLegacy(@RequestBody Task task) {
        return taskService.addTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        Optional<Task> optionalTask = taskService.getTaskById(id);
        if (optionalTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = optionalTask.get();
        String newStatus = statusUpdate.get("status");
        String approved = statusUpdate.get("approved"); // "true" or "false"
        String comments = statusUpdate.get("comments");
        
        if (newStatus != null) {
            task.setTaskStatus(newStatus);
            Task updatedTask = taskService.updateTask(id, task);
            
            // ✅ AUTOMATIC WORKFLOW SYNC: If task is completed and has a workflow job key, complete the workflow!
            if ("COMPLETED".equalsIgnoreCase(newStatus) && updatedTask.getWorkflowJobKey() != null) {
                System.out.println("🔄 Auto-completing workflow job: " + updatedTask.getWorkflowJobKey());
                System.out.println("   📥 Received approved parameter: '" + approved + "'");
                try {
                    Map<String, Object> workflowVariables = new HashMap<>();
                    // Default to approved if not explicitly rejected
                    boolean approvedValue = !"false".equalsIgnoreCase(approved) && !"rejected".equalsIgnoreCase(approved);
                    workflowVariables.put("approved", approvedValue);
                    workflowVariables.put("comments", comments != null ? comments : "Task completed");
                    
                    System.out.println("   📤 Sending to workflow - approved: " + approvedValue + ", comments: " + (comments != null ? comments : "Task completed"));
                    
                    workflowClient.completeWorkflowJob(updatedTask.getWorkflowJobKey(), workflowVariables);
                    System.out.println("   ✅ Workflow job completed successfully!");
                } catch (Exception e) {
                    System.err.println("   ⚠️ Failed to complete workflow job: " + e.getMessage());
                    System.err.println("   Task status updated, but workflow may need manual completion.");
                    // Don't fail the task update if workflow completion fails
                }
            }
            
            return ResponseEntity.ok(updatedTask);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Long userId,
            @RequestParam(value = "assignedTo", required = false) String assignedTo,
            @RequestParam(value = "jobKey", required = false) Long jobKey,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            // NEW: Two-stage review parameters
            @RequestParam(value = "initialReviewer", required = false) String initialReviewer,
            @RequestParam(value = "technicalReviewer", required = false) String technicalReviewer,
            @RequestParam(value = "reviewStage", required = false) String reviewStage
    ) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setUserId(userId);
        if (assignedTo != null && !assignedTo.isBlank()) {
            task.setAssignedTo(assignedTo);
        }
        task.setWorkflowJobKey(jobKey); // Store the workflow job key for automatic sync

        // NEW: Set two-stage review fields
        if (initialReviewer != null && !initialReviewer.isBlank()) {
            task.setInitialReviewer(initialReviewer);
        }
        if (technicalReviewer != null && !technicalReviewer.isBlank()) {
            task.setTechnicalReviewer(technicalReviewer);
        }
        if (reviewStage != null && !reviewStage.isBlank()) {
            task.setReviewStage(reviewStage);
        }

        List<MultipartFile> fileList = (files != null) ? List.of(files) : null;

        try {
            System.out.println("📝 Creating task with review info:");
            System.out.println("   Name: " + name);
            System.out.println("   AssignedTo: " + assignedTo);
            System.out.println("   InitialReviewer: " + initialReviewer);
            System.out.println("   TechnicalReviewer: " + technicalReviewer);
            System.out.println("   ReviewStage: " + reviewStage);
            
            Task createdTask = taskService.addTask(task, fileList);
            System.out.println("   ✅ Task created successfully: ID " + createdTask.getId());
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            System.err.println("❌ ERROR creating task: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // This is for the workflow
    @Value("${camunda.process.key:task-process}")
    private String processKey;

    // Step 2: Start Process after Task is created
    @PostMapping("/start-process")
    public ResponseEntity<String> startTaskProcess(@RequestParam Long taskId) {
        Optional<Task> optionalTask = taskService.getTaskById(taskId);

        if (optionalTask.isEmpty()) {
            return ResponseEntity.badRequest().body("Task with ID " + taskId + " not found");
        }

        Task task = optionalTask.get();

        // Legacy Zeebe start removed; respond with guidance
        return ResponseEntity.status(410)
                .body("Deprecated: start workflows via workflow-orchestrator API.");
    }

    @PostMapping("/migrate-columns")
    public ResponseEntity<String> migrateTaskColumns() {
        try {
            int count = taskService.migrateTaskColumns();
            return ResponseEntity.ok("Successfully migrated " + count + " tasks from old columns to new columns");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==================== SIGNOFF ENDPOINTS ====================
    
    @PostMapping("/{id}/signoff")
    public ResponseEntity<?> addSignoff(
            @PathVariable Long id,
            @RequestBody Map<String, String> signoffRequest) {
        try {
            String userId = signoffRequest.get("userId");
            String actionStr = signoffRequest.get("action");
            String comments = signoffRequest.get("comments");
            
            if (userId == null || actionStr == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "userId and action are required"));
            }
            
            SignoffAction action = SignoffAction.valueOf(actionStr.toUpperCase());
            TaskSignoff signoff = taskService.addTaskSignoff(id, userId, action, comments);
            
            if (signoff == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Signoff feature not available"));
            }
            
            return ResponseEntity.ok(signoff);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid action. Must be: APPROVED, REJECTED, REVIEWED, or ACKNOWLEDGED"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/signoffs")
    public ResponseEntity<List<TaskSignoff>> getTaskSignoffs(@PathVariable Long id) {
        List<TaskSignoff> signoffs = taskService.getSignoffsForTask(id);
        return ResponseEntity.ok(signoffs);
    }
    
    @GetMapping("/signoffs/user/{userId}")
    public ResponseEntity<List<TaskSignoff>> getUserSignoffs(@PathVariable String userId) {
        List<TaskSignoff> signoffs = taskService.getSignoffsByUser(userId);
        return ResponseEntity.ok(signoffs);
    }

    // ==================== NEO4J RELATIONSHIP ENDPOINTS ====================
    
    @GetMapping("/{id}/relationships")
    public ResponseEntity<List<TaskNode>> getTaskRelationships(@PathVariable Long id) {
        List<TaskNode> relationships = taskService.getTaskRelationships(String.valueOf(id));
        return ResponseEntity.ok(relationships);
    }
    
    @PostMapping("/{id}/sync-neo4j")
    public ResponseEntity<String> syncTaskToNeo4j(@PathVariable Long id) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        taskService.syncTaskToNeo4j(taskOpt.get());
        return ResponseEntity.ok("Task synced to Neo4j successfully");
    }

}

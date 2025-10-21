package com.example.task_service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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



@RestController
@RequestMapping("/tasks")
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

    @PostMapping
    public Task addTask(@RequestBody Task task) {
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
                try {
                    Map<String, Object> workflowVariables = new HashMap<>();
                    // Default to approved if not explicitly rejected
                    workflowVariables.put("approved", !"false".equalsIgnoreCase(approved) && !"rejected".equalsIgnoreCase(approved));
                    workflowVariables.put("comments", comments != null ? comments : "Task completed");
                    
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
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setUserId(userId);
        if (assignedTo != null && !assignedTo.isBlank()) {
            task.setAssignedTo(assignedTo);
        }
        task.setWorkflowJobKey(jobKey); // Store the workflow job key for automatic sync

        List<MultipartFile> fileList = (files != null) ? List.of(files) : null;

        Task createdTask = taskService.addTask(task, fileList);
        return ResponseEntity.ok(createdTask);
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


}

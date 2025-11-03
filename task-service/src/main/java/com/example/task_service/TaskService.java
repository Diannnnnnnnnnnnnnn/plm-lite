package com.example.task_service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.task_service.client.FileStorageClient;
import com.example.task_service.client.GraphClient;
import com.example.task_service.dto.CreateTaskRequest;
import com.example.task_service.dto.TaskResponse;
import com.example.task_service.elasticsearch.TaskDocument;
import com.example.task_service.elasticsearch.TaskSearchRepository;
import com.example.task_service.messaging.TaskMessageProducer;
import com.example.task_service.model.FileMetadata;
import com.example.task_service.model.SignoffAction;
import com.example.task_service.model.TaskSignoff;
import com.example.task_service.model.TaskStatus;
import com.example.task_service.model.TaskType;
import com.example.task_service.model.neo4j.TaskNode;
import com.example.task_service.repository.FileMetadataRepository;
import com.example.task_service.repository.TaskSignoffRepository;
import com.example.task_service.repository.neo4j.TaskNodeRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserClient userClient; // Inject Feign Client

    @Autowired(required = false)
    private TaskSearchRepository taskSearchRepository;

    @Autowired(required = false)
    private GraphClient graphClient;

    @Autowired(required = false)
    private FileStorageClient fileStorageClient;

    @Autowired(required = false)
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private TaskMessageProducer taskMessageProducer;

    @Autowired(required = false)
    private TaskSignoffRepository taskSignoffRepository;

    @Autowired(required = false)
    private TaskNodeRepository taskNodeRepository;

    public Task addTask(Task task, List<MultipartFile> files) {
        // Prefer assignedTo if provided (from orchestrator); otherwise, try to resolve via user-service
        if (task.getAssignedTo() == null || task.getAssignedTo().isBlank()) {
            try {
                User user = userClient.getUserById(task.getUserId());
                if (user == null) {
                    System.err.println("⚠ Warning: User not found with ID: " + task.getUserId() + ", but continuing task creation");
                } else {
                    // Set assignedTo field with username for query compatibility
                    task.setAssignedTo(user.getUsername());
                    System.out.println("✓ Assigned task to user: " + user.getUsername() + " (ID: " + task.getUserId() + ")");
                }
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to validate user ID " + task.getUserId() + ": " + e.getMessage());
                System.err.println("   Continuing with task creation anyway...");
            }
        }

        // First save to DB to generate ID
        Task savedTask = taskRepository.save(task);

        // After saving task and before returning (fault-tolerant)
        try {
            taskMessageProducer.sendTaskCreatedMessage(String.valueOf(savedTask.getId()));
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to send RabbitMQ message for task " + savedTask.getId() + ": " + e.getMessage());
        }
        
        // Upload and associate files if provided
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String fileUrl = fileStorageClient.uploadFile(file);
                    FileMetadata metadata = new FileMetadata();
                    metadata.setFilename(file.getOriginalFilename());
                    metadata.setFileUrl(fileUrl);
                    metadata.setTask(savedTask);
                    fileMetadataRepository.save(metadata);
                } catch (Exception e) {
                    System.err.println("⚠ Warning: Failed to upload file " + file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
        }
        // Sync with Elasticsearch (if available)
        if (taskSearchRepository != null) {
            try {
                taskSearchRepository.save(new TaskDocument(
                    savedTask.getId(), savedTask.getName(), savedTask.getDescription(), savedTask.getUserId()
                ));
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to index task in Elasticsearch: " + e.getMessage());
            }
        }

        // Send to graph service (fault-tolerant)
        try {
            graphClient.createTask(String.valueOf(savedTask.getId()), savedTask.getName());
            graphClient.assignTask(String.valueOf(savedTask.getUserId()), String.valueOf(savedTask.getId()));
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to sync task with graph service: " + e.getMessage());
        }

        return savedTask;
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    public List<Task> getAllTasks() {         
       return taskRepository.findAll();
    }     

    public Task updateTask(Long id, Task task) {
        Optional<Task> existingOpt = taskRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null; // Handle non-existent ID scenario
        }

        Task existing = existingOpt.get();

        if (task.getName() != null) existing.setName(task.getName());
        if (task.getDescription() != null) existing.setDescription(task.getDescription());
        if (task.getUserId() != null) existing.setUserId(task.getUserId());
        if (task.getAssignedTo() != null && !task.getAssignedTo().isBlank()) existing.setAssignedTo(task.getAssignedTo());
        if (task.getTaskStatus() != null) existing.setTaskStatus(task.getTaskStatus());
        if (task.getDueDate() != null) existing.setDueDate(task.getDueDate());
        if (task.getWorkflowJobKey() != null) existing.setWorkflowJobKey(task.getWorkflowJobKey());

        // createdAt is immutable once set

        Task updatedTask = taskRepository.save(existing);
        
        // Re-index to Elasticsearch (if available)
        if (taskSearchRepository != null) {
            try {
                taskSearchRepository.save(new TaskDocument(
                    updatedTask.getId(), updatedTask.getName(), updatedTask.getDescription(), updatedTask.getUserId()
                ));
                System.out.println("✅ Task " + updatedTask.getId() + " re-indexed to Elasticsearch");
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to re-index task in Elasticsearch: " + e.getMessage());
            }
        }
        
        return updatedTask;
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
        
        // Delete from Elasticsearch (if available)
        if (taskSearchRepository != null) {
            try {
                taskSearchRepository.deleteById(String.valueOf(id));
                System.out.println("✅ Task " + id + " deleted from Elasticsearch");
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to delete task from Elasticsearch: " + e.getMessage());
            }
        }
    }

    public Task addTask(Task task) {
        return addTask(task, List.of()); // delegate to the main method with an empty file list
    }

    public List<Task> getTasksByAssignedTo(String assignedTo) {
        return taskRepository.findByAssignedTo(assignedTo);
    }

    // NEW: Create task from CreateTaskRequest (new API format)
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = new Task();
        
        // Map fields from request to task entity
        task.setName(request.getTaskName());
        task.setDescription(request.getTaskDescription());
        task.setAssignedTo(request.getAssignedTo());
        task.setAssignedBy(request.getAssignedBy());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setWorkflowId(request.getWorkflowId());
        task.setContextType(request.getContextType());
        task.setContextId(request.getContextId());
        
        // Convert taskType from String to enum
        if (request.getTaskType() != null && !request.getTaskType().isBlank()) {
            try {
                task.setTaskType(TaskType.valueOf(request.getTaskType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                task.setTaskType(TaskType.GENERAL); // Default to GENERAL if invalid
            }
        } else {
            task.setTaskType(TaskType.GENERAL);
        }
        
        // Set default status
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // Save the task
        Task savedTask = addTask(task);
        
        // Sync to Neo4j (optional - fault-tolerant)
        syncTaskToNeo4j(savedTask);
        
        // Convert to response
        return mapToTaskResponse(savedTask);
    }
    
    // Helper method to convert Task entity to TaskResponse DTO
    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
            String.valueOf(task.getId()),
            task.getName(),
            task.getDescription(),
            task.getTaskType(),
            task.getStatus(),
            task.getAssignedTo(),
            task.getAssignedBy(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getPriority(),
            task.getParentTaskId() != null ? String.valueOf(task.getParentTaskId()) : null,
            task.getWorkflowId(),
            task.getContextType(),
            task.getContextId()
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public int migrateTaskColumns() {
        System.out.println("🔄 Starting migration of task columns...");
        
        // Get all tasks
        List<Task> allTasks = taskRepository.findAll();
        System.out.println("   Found " + allTasks.size() + " tasks");
        
        int migratedCount = 0;
        for (Task task : allTasks) {
            // The columns are now mapped correctly, JPA will read/write to task_name and task_description
            // Just re-save each task to ensure data is in the correct columns
            try {
                taskRepository.save(task);
                migratedCount++;
                System.out.println("   ✓ Migrated task ID: " + task.getId() + " - " + task.getName());
            } catch (Exception e) {
                System.err.println("   ✗ Failed to migrate task ID: " + task.getId() + " - " + e.getMessage());
            }
        }
        
        System.out.println("✅ Migration complete: " + migratedCount + " tasks migrated");
        return migratedCount;
    }

    // ==================== SIGNOFF SUPPORT ====================
    
    @org.springframework.transaction.annotation.Transactional
    public TaskSignoff addTaskSignoff(Long taskId, String userId, SignoffAction action, String comments) {
        if (taskSignoffRepository == null) {
            System.err.println("⚠ TaskSignoffRepository not available");
            return null;
        }
        
        // Verify task exists
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }
        
        Task task = taskOpt.get();
        
        // Create signoff
        TaskSignoff signoff = new TaskSignoff(
            taskId,
            userId,
            action,
            comments,
            LocalDateTime.now(),
            true
        );
        
        TaskSignoff savedSignoff = taskSignoffRepository.save(signoff);
        System.out.println("✓ Created signoff for task " + taskId + " by user " + userId + " with action " + action);
        
        // Auto-update task status based on signoff action
        if (action == SignoffAction.APPROVED) {
            long approvalCount = taskSignoffRepository.countByTaskIdAndActionAndIsRequired(taskId, SignoffAction.APPROVED, true);
            int requiredApprovals = getRequiredApprovalsForTask(taskId);
            
            if (approvalCount >= requiredApprovals) {
                task.setStatus(TaskStatus.COMPLETED);
                taskRepository.save(task);
                System.out.println("✓ Task " + taskId + " auto-completed (all approvals received)");
            }
        } else if (action == SignoffAction.REJECTED) {
            task.setStatus(TaskStatus.CANCELLED);
            taskRepository.save(task);
            System.out.println("✓ Task " + taskId + " cancelled due to rejection");
        }
        
        return savedSignoff;
    }
    
    public List<TaskSignoff> getSignoffsForTask(Long taskId) {
        if (taskSignoffRepository == null) {
            return List.of();
        }
        return taskSignoffRepository.findByTaskId(taskId);
    }
    
    public List<TaskSignoff> getSignoffsByUser(String userId) {
        if (taskSignoffRepository == null) {
            return List.of();
        }
        return taskSignoffRepository.findByUserId(userId);
    }
    
    private int getRequiredApprovalsForTask(Long taskId) {
        // Default to 1 approval required
        // This can be made configurable per task type later
        return 1;
    }

    // ==================== NEO4J INTEGRATION ====================
    
    public void syncTaskToNeo4j(Task task) {
        if (taskNodeRepository == null) {
            System.out.println("⚠ Neo4j not available, skipping graph sync for task " + task.getId());
            return;
        }
        
        try {
            // Check if task node already exists
            Optional<TaskNode> existingNode = taskNodeRepository.findByTaskId(String.valueOf(task.getId()));
            
            TaskNode taskNode;
            if (existingNode.isPresent()) {
                taskNode = existingNode.get();
            } else {
                taskNode = new TaskNode();
                taskNode.setTaskId(String.valueOf(task.getId()));
            }
            
            // Update task node properties
            taskNode.setTaskName(task.getName());
            taskNode.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : "GENERAL");
            taskNode.setTaskStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
            taskNode.setCreatedAt(task.getCreatedAt());
            
            taskNodeRepository.save(taskNode);
            System.out.println("✓ Synced task " + task.getId() + " to Neo4j");
        } catch (Exception e) {
            System.err.println("⚠ Failed to sync task to Neo4j: " + e.getMessage());
        }
    }
    
    public List<TaskNode> getTaskRelationships(String taskId) {
        if (taskNodeRepository == null) {
            return List.of();
        }
        return taskNodeRepository.findDependentTasks(taskId);
    }
 
}


package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Task Service
 */
@FeignClient(name = "task-service", url = "http://localhost:8082")
public interface TaskServiceClient {
    
    // Legacy method - deprecated (uses form params, doesn't match task-service API)
    @PostMapping("/api/tasks/create")
    @Deprecated
    TaskDTO createTask(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "assignedTo", required = false) String assignedTo
    );
    
    // NEW: Proper JSON-based task creation matching task-service API
    @PostMapping("/api/tasks")
    TaskResponse createTaskWithContext(@RequestBody CreateTaskRequest request);
    
    // Legacy method for two-stage review
    @PostMapping("/api/tasks/create")
    @Deprecated
    TaskDTO createTaskWithReviewInfo(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "assignedTo", required = false) String assignedTo,
        @RequestParam(value = "initialReviewer", required = false) String initialReviewer,
        @RequestParam(value = "technicalReviewer", required = false) String technicalReviewer,
        @RequestParam(value = "reviewStage", required = false) String reviewStage
    );
    
    @PutMapping("/api/tasks/{id}")
    TaskDTO updateTaskWithJobKey(
        @PathVariable("id") Long id,
        @RequestBody TaskDTO task
    );
    
    /**
     * Request DTO for creating tasks with full context
     */
    class CreateTaskRequest {
        private String taskName;
        private String taskDescription;
        private String taskType;  // Use String for simplicity
        private String assignedTo;
        private String assignedBy;
        private String contextType;  // e.g., "CHANGE", "DOCUMENT"
        private String contextId;    // e.g., change ID or document ID
        private String workflowId;
        private Integer priority;

        public CreateTaskRequest() {}

        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getTaskDescription() { return taskDescription; }
        public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
        
        public String getAssignedBy() { return assignedBy; }
        public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
        
        public String getContextType() { return contextType; }
        public void setContextType(String contextType) { this.contextType = contextType; }
        
        public String getContextId() { return contextId; }
        public void setContextId(String contextId) { this.contextId = contextId; }
        
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    /**
     * Response DTO from task service
     */
    class TaskResponse {
        private String id;
        private String taskName;
        private String taskDescription;
        private String taskType;
        private String taskStatus;
        private String assignedTo;
        private String assignedBy;
        private String contextType;
        private String contextId;
        private String workflowId;

        public TaskResponse() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getTaskDescription() { return taskDescription; }
        public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public String getTaskStatus() { return taskStatus; }
        public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
        
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
        
        public String getAssignedBy() { return assignedBy; }
        public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
        
        public String getContextType() { return contextType; }
        public void setContextType(String contextType) { this.contextType = contextType; }
        
        public String getContextId() { return contextId; }
        public void setContextId(String contextId) { this.contextId = contextId; }
        
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    }

    /**
     * Simple DTO for task response (LEGACY)
     */
    @Deprecated
    class TaskDTO {
        private Long id;
        private String name;
        private String description;
        private Long userId;
        private Long workflowJobKey;
        private String initialReviewer;
        private String technicalReviewer;
        private String reviewStage;
        
        public TaskDTO() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getWorkflowJobKey() { return workflowJobKey; }
        public void setWorkflowJobKey(Long workflowJobKey) { this.workflowJobKey = workflowJobKey; }
        
        public String getInitialReviewer() { return initialReviewer; }
        public void setInitialReviewer(String initialReviewer) { this.initialReviewer = initialReviewer; }
        public String getTechnicalReviewer() { return technicalReviewer; }
        public void setTechnicalReviewer(String technicalReviewer) { this.technicalReviewer = technicalReviewer; }
        public String getReviewStage() { return reviewStage; }
        public void setReviewStage(String reviewStage) { this.reviewStage = reviewStage; }
    }
}


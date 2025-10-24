package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Task Service
 * Note: task-service uses form parameters, not JSON body
 */
@FeignClient(name = "task-service", url = "http://localhost:8082")
public interface TaskServiceClient {
    
    @PostMapping("/tasks/create")
    TaskDTO createTask(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "assignedTo", required = false) String assignedTo
    );
    
    // NEW: Create task with two-stage review information
    @PostMapping("/tasks/create")
    TaskDTO createTaskWithReviewInfo(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "assignedTo", required = false) String assignedTo,
        @RequestParam(value = "initialReviewer", required = false) String initialReviewer,
        @RequestParam(value = "technicalReviewer", required = false) String technicalReviewer,
        @RequestParam(value = "reviewStage", required = false) String reviewStage
    );
    
    @PutMapping("/tasks/{id}")
    TaskDTO updateTaskWithJobKey(
        @PathVariable("id") Long id,
        @RequestBody TaskDTO task
    );
    
    /**
     * Simple DTO for task response
     */
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


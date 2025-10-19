package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
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
        @RequestParam("userId") Long userId
    );
    
    /**
     * Simple DTO for task response
     */
    class TaskDTO {
        private Long id;
        private String name;
        private String description;
        private Long userId;
        
        public TaskDTO() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}


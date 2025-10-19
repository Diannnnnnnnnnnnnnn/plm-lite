package com.example.plm.change.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "task-service", url = "http://localhost:8082")
public interface TaskServiceClient {

    @PostMapping("/tasks")
    TaskDTO createTask(@RequestBody TaskDTO task);

    class TaskDTO {
        private String name;
        private String description;
        private Long userId;
        private String assignedTo; // Username for task filtering

        public TaskDTO() {}

        public TaskDTO(String name, String description, Long userId) {
            this.name = name;
            this.description = description;
            this.userId = userId;
        }

        public TaskDTO(String name, String description, Long userId, String assignedTo) {
            this.name = name;
            this.description = description;
            this.userId = userId;
            this.assignedTo = assignedTo;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    }
}

package com.example.change_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "task-service")
public interface TaskServiceClient {

    @PostMapping("/tasks")
    TaskDTO createTask(@RequestBody TaskDTO task);

    @PostMapping("/tasks/with-context")
    TaskDTO createTaskWithContext(@RequestBody CreateTaskRequest request);

    @DeleteMapping("/tasks/by-context")
    void deleteTasksByContextId(@RequestParam("contextType") String contextType, 
                                 @RequestParam("contextId") String contextId);

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

    class CreateTaskRequest {
        private String taskName;
        private String taskDescription;
        private String taskType;
        private String assignedTo;
        private String assignedBy;
        private String contextType;
        private String contextId;
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

        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }
}

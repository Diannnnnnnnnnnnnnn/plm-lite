package com.example.task_service.dto;

import java.time.LocalDateTime;

import com.example.task_service.model.TaskStatus;
import com.example.task_service.model.TaskType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskResponse {

    private String id;  // Will be converted from Long to String for API consistency
    
    // Support both old and new field names for backward compatibility
    @JsonProperty("taskName")
    private String taskName;
    
    @JsonProperty("taskDescription")
    private String taskDescription;
    
    private TaskType taskType;
    private TaskStatus taskStatus;
    private String assignedTo;
    private String assignedBy;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer priority;
    private String parentTaskId;
    private String workflowId;
    private String contextType;
    private String contextId;

    public TaskResponse() {}

    public TaskResponse(String id, String taskName, String taskDescription, TaskType taskType,
                       TaskStatus taskStatus, String assignedTo, String assignedBy,
                       LocalDateTime dueDate, LocalDateTime createdAt, LocalDateTime updatedAt,
                       Integer priority, String parentTaskId, String workflowId,
                       String contextType, String contextId) {
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.priority = priority;
        this.parentTaskId = parentTaskId;
        this.workflowId = workflowId;
        this.contextType = contextType;
        this.contextId = contextId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    
    // Backward compatibility: old frontend expects 'name' field
    @JsonProperty("name")
    public String getName() { return taskName; }
    public void setName(String name) { this.taskName = name; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    
    // Backward compatibility: old frontend expects 'description' field
    @JsonProperty("description")
    public String getDescription() { return taskDescription; }
    public void setDescription(String description) { this.taskDescription = description; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) { this.taskStatus = taskStatus; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(String parentTaskId) { this.parentTaskId = parentTaskId; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
}


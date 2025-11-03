package com.example.task_service.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank(message = "Task name is required")
    private String taskName;

    private String taskDescription;

    private String taskType;  // String for flexible input, will be converted to enum

    @NotBlank(message = "Assigned to is required")
    private String assignedTo;

    @NotBlank(message = "Assigned by is required")
    private String assignedBy;

    private LocalDateTime dueDate;

    private Integer priority;

    private String parentTaskId;

    private String workflowId;

    private String contextType;

    private String contextId;

    public CreateTaskRequest() {}

    public CreateTaskRequest(String taskName, String taskDescription, String taskType,
                            String assignedTo, String assignedBy, LocalDateTime dueDate,
                            Integer priority, String parentTaskId, String workflowId,
                            String contextType, String contextId) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskType = taskType;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
        this.priority = priority;
        this.parentTaskId = parentTaskId;
        this.workflowId = workflowId;
        this.contextType = contextType;
        this.contextId = contextId;
    }

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

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

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


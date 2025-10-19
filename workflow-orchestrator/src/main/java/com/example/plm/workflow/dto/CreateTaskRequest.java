package com.example.plm.workflow.dto;

/**
 * DTO for creating tasks in Task Service
 * Matches task-service CreateTaskRequest DTO
 */
public class CreateTaskRequest {
    private String taskName;
    private String taskDescription;
    private String taskType;  // TaskType enum value as string: REVIEW, APPROVAL, etc.
    private String assignedTo;
    private String assignedBy;
    private Integer priority;
    private String workflowId;
    private String contextType;
    private String contextId;

    // Getters and setters
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
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    
    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }
    
    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
}



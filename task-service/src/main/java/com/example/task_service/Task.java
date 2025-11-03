package com.example.task_service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.task_service.model.FileMetadata;
import com.example.task_service.model.TaskType;
import com.example.task_service.model.TaskStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "Task")
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use task_name and task_description columns for consistency
    @Column(name = "task_name")
    private String name;
    
    @Column(name = "task_description", length = 1000)
    private String description;
    
    // Legacy fields
    private Long userId;
    private String assignedTo;
    
    // New enum-based status (stores as String in DB)
    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus status;
    
    // Legacy string-based status for backward compatibility
    @Column(name = "task_status_legacy", insertable = false, updatable = false)
    private String taskStatus;
    
    // NEW: Task type
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;
    
    // NEW: Assigned by (who created/assigned the task)
    @Column(name = "assigned_by")
    private String assignedBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    
    // NEW: Updated timestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // NEW: Priority
    @Column(name = "priority")
    private Integer priority;
    
    private Long workflowJobKey;
    
    // Two-stage review fields (legacy)
    private String initialReviewer;
    private String technicalReviewer;
    private String reviewStage;
    
    // NEW: Parent task support for sub-tasks
    @Column(name = "parent_task_id")
    private Long parentTaskId;
    
    // NEW: Workflow ID (String for UUID support)
    @Column(name = "workflow_id")
    private String workflowId;
    
    // NEW: Context support (DOCUMENT, CHANGE, PART, etc.)
    @Column(name = "context_type")
    private String contextType;
    
    @Column(name = "context_id")
    private String contextId;

    // File attachments
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<FileMetadata> files = new ArrayList<>();

    // Constructors
    public Task() {
        this.status = TaskStatus.TODO;
        this.taskType = TaskType.GENERAL;
        this.createdAt = LocalDateTime.now();
    }

    public Task(String name, String description, Long userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.status = TaskStatus.TODO;
        this.taskType = TaskType.GENERAL;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Legacy getter for backward compatibility
    public String getTaskStatus() {
        return status != null ? status.name() : null;
    }

    // Legacy setter for backward compatibility
    public void setTaskStatus(String taskStatus) {
        if (taskStatus != null) {
            try {
                this.status = TaskStatus.valueOf(taskStatus);
            } catch (IllegalArgumentException e) {
                // Default to TODO if invalid status
                this.status = TaskStatus.TODO;
            }
        }
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<FileMetadata> getFiles() {
        return files;
    }

    public void setFiles(List<FileMetadata> files) {
        this.files = files;
    }

    public Long getWorkflowJobKey() {
        return workflowJobKey;
    }

    public void setWorkflowJobKey(Long workflowJobKey) {
        this.workflowJobKey = workflowJobKey;
    }

    public String getInitialReviewer() {
        return initialReviewer;
    }

    public void setInitialReviewer(String initialReviewer) {
        this.initialReviewer = initialReviewer;
    }

    public String getTechnicalReviewer() {
        return technicalReviewer;
    }

    public void setTechnicalReviewer(String technicalReviewer) {
        this.technicalReviewer = technicalReviewer;
    }

    public String getReviewStage() {
        return reviewStage;
    }

    public void setReviewStage(String reviewStage) {
        this.reviewStage = reviewStage;
    }

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

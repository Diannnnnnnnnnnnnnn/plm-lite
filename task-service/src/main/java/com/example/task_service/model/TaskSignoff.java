package com.example.task_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TaskSignoff")
public class TaskSignoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private SignoffAction action;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "signoff_date", nullable = false)
    private LocalDateTime signoffDate;

    @Column(name = "is_required")
    private Boolean isRequired;

    public TaskSignoff() {}

    public TaskSignoff(Long taskId, String userId, SignoffAction action, 
                      String comments, LocalDateTime signoffDate, Boolean isRequired) {
        this.taskId = taskId;
        this.userId = userId;
        this.action = action;
        this.comments = comments;
        this.signoffDate = signoffDate;
        this.isRequired = isRequired;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public SignoffAction getAction() { return action; }
    public void setAction(SignoffAction action) { this.action = action; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getSignoffDate() { return signoffDate; }
    public void setSignoffDate(LocalDateTime signoffDate) { this.signoffDate = signoffDate; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
}


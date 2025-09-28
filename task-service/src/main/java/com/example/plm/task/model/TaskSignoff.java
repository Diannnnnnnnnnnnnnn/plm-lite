package com.example.plm.task.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_signoffs")
public class TaskSignoff {

    @Id
    private String id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "signoff_user", nullable = false)
    private String signoffUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "signoff_action", nullable = false)
    private SignoffAction signoffAction;

    @Column(name = "comments")
    private String comments;

    @Column(name = "signoff_timestamp", nullable = false)
    private LocalDateTime signoffTimestamp;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    public TaskSignoff() {}

    public TaskSignoff(String id, String taskId, String signoffUser,
                      SignoffAction signoffAction, String comments,
                      LocalDateTime signoffTimestamp, Boolean isRequired) {
        this.id = id;
        this.taskId = taskId;
        this.signoffUser = signoffUser;
        this.signoffAction = signoffAction;
        this.comments = comments;
        this.signoffTimestamp = signoffTimestamp;
        this.isRequired = isRequired;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getSignoffUser() { return signoffUser; }
    public void setSignoffUser(String signoffUser) { this.signoffUser = signoffUser; }

    public SignoffAction getSignoffAction() { return signoffAction; }
    public void setSignoffAction(SignoffAction signoffAction) { this.signoffAction = signoffAction; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getSignoffTimestamp() { return signoffTimestamp; }
    public void setSignoffTimestamp(LocalDateTime signoffTimestamp) { this.signoffTimestamp = signoffTimestamp; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
}
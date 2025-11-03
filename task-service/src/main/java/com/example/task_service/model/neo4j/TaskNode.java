package com.example.task_service.model.neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Task")
public class TaskNode {

    @Id
    @GeneratedValue
    private Long id;

    private String taskId;  // Reference to MySQL task ID
    private String taskName;
    private String taskType;
    private String taskStatus;
    private LocalDateTime createdAt;

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<UserNode> assignedUsers = new HashSet<>();

    @Relationship(type = "PART_OF", direction = Relationship.Direction.OUTGOING)
    private WorkflowNode workflow;

    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<TaskNode> dependencies = new HashSet<>();

    public TaskNode() {}

    public TaskNode(String taskId, String taskName, String taskType, String taskStatus) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<UserNode> getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(Set<UserNode> assignedUsers) { this.assignedUsers = assignedUsers; }

    public WorkflowNode getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowNode workflow) { this.workflow = workflow; }

    public Set<TaskNode> getDependencies() { return dependencies; }
    public void setDependencies(Set<TaskNode> dependencies) { this.dependencies = dependencies; }
}


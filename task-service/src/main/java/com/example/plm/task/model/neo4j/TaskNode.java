package com.example.plm.task.model.neo4j;

import com.example.plm.task.model.TaskStatus;
import com.example.plm.task.model.TaskType;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Task")
public class TaskNode {

    @Id
    private String id;

    @Property("taskName")
    private String taskName;

    @Property("taskDescription")
    private String taskDescription;

    @Property("taskType")
    private String taskType;

    @Property("taskStatus")
    private String taskStatus;

    @Property("assignedTo")
    private String assignedTo;

    @Property("assignedBy")
    private String assignedBy;

    @Property("dueDate")
    private LocalDateTime dueDate;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Property("priority")
    private Integer priority;

    @Property("workflowId")
    private String workflowId;

    @Property("contextType")
    private String contextType;

    @Property("contextId")
    private String contextId;

    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    private Set<TaskNode> dependencies = new HashSet<>();

    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.OUTGOING)
    private Set<TaskNode> subtasks = new HashSet<>();

    @Relationship(type = "ASSIGNED_TO")
    private Set<UserNode> assignees = new HashSet<>();

    @Relationship(type = "CREATED_BY")
    private UserNode creator;

    public TaskNode() {}

    public TaskNode(String id, String taskName, String taskDescription, String taskType,
                    String taskStatus, String assignedTo, String assignedBy,
                    LocalDateTime dueDate, LocalDateTime createdAt, Integer priority,
                    String workflowId, String contextType, String contextId) {
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.priority = priority;
        this.workflowId = workflowId;
        this.contextType = contextType;
        this.contextId = contextId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

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

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }

    public Set<TaskNode> getDependencies() { return dependencies; }
    public void setDependencies(Set<TaskNode> dependencies) { this.dependencies = dependencies; }

    public Set<TaskNode> getSubtasks() { return subtasks; }
    public void setSubtasks(Set<TaskNode> subtasks) { this.subtasks = subtasks; }

    public Set<UserNode> getAssignees() { return assignees; }
    public void setAssignees(Set<UserNode> assignees) { this.assignees = assignees; }

    public UserNode getCreator() { return creator; }
    public void setCreator(UserNode creator) { this.creator = creator; }
}
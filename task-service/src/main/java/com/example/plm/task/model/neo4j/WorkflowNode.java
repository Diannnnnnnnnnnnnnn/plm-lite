package com.example.plm.task.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Workflow")
public class WorkflowNode {

    @Id
    private String id;

    @Property("workflowName")
    private String workflowName;

    @Property("workflowType")
    private String workflowType;

    @Property("status")
    private String status;

    @Property("contextType")
    private String contextType;

    @Property("contextId")
    private String contextId;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Property("processInstanceId")
    private String processInstanceId;

    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    private Set<TaskNode> tasks = new HashSet<>();

    @Relationship(type = "INITIATED_BY")
    private UserNode initiator;

    public WorkflowNode() {}

    public WorkflowNode(String id, String workflowName, String workflowType,
                        String status, String contextType, String contextId,
                        LocalDateTime createdAt, String processInstanceId) {
        this.id = id;
        this.workflowName = workflowName;
        this.workflowType = workflowType;
        this.status = status;
        this.contextType = contextType;
        this.contextId = contextId;
        this.createdAt = createdAt;
        this.processInstanceId = processInstanceId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }

    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }

    public Set<TaskNode> getTasks() { return tasks; }
    public void setTasks(Set<TaskNode> tasks) { this.tasks = tasks; }

    public UserNode getInitiator() { return initiator; }
    public void setInitiator(UserNode initiator) { this.initiator = initiator; }
}
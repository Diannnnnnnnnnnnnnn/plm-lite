package com.example.task_service.model.neo4j;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("Workflow")
public class WorkflowNode {

    @Id
    @GeneratedValue
    private Long id;

    private String workflowId;  // Reference to workflow instance ID
    private String workflowType;
    private String status;
    private LocalDateTime startedAt;

    public WorkflowNode() {}

    public WorkflowNode(String workflowId, String workflowType, String status) {
        this.workflowId = workflowId;
        this.workflowType = workflowType;
        this.status = status;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }

    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}


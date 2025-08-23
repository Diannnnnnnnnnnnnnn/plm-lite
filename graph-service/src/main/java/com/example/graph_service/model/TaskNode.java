package com.example.graph_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Task")
public class TaskNode {

    @Id
    private String id;

    private String title;

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.INCOMING)
    private UserNode assignee;

    // No-arg constructor
    public TaskNode() {}

    // All-arg constructor
    public TaskNode(String id, String title, UserNode assignee) {
        this.id = id;
        this.title = title;
        this.assignee = assignee;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserNode getAssignee() {
        return assignee;
    }

    public void setAssignee(UserNode assignee) {
        this.assignee = assignee;
    }
}

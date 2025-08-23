package com.example.graph_service.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("User")
public class UserNode {

    @Id
    private String id;

    private String name;

    @Relationship(type = "ASSIGNED_TO")
    private List<TaskNode> tasks = new ArrayList<>();

    // ✅ No-arg constructor
    public UserNode() {}

    // ✅ All-arg constructor
    public UserNode(String id, String name, List<TaskNode> tasks) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
    }

    // ✅ Getters and setters (you can generate these in your IDE if needed)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<TaskNode> getTasks() { return tasks; }
    public void setTasks(List<TaskNode> tasks) { this.tasks = tasks; }
}

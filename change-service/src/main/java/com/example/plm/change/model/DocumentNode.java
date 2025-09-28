package com.example.plm.change.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Document")
public class DocumentNode {

    @Id
    private String id;

    private String title;
    private String status;
    private String type;

    public DocumentNode() {}

    public DocumentNode(String id, String title, String status, String type) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
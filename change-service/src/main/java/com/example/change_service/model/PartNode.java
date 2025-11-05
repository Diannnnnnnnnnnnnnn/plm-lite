package com.example.change_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Part")
public class PartNode {

    @Id
    private String id;

    private String name;
    private String partNumber;
    private String type;

    public PartNode() {}

    public PartNode(String id, String name, String partNumber, String type) {
        this.id = id;
        this.name = name;
        this.partNumber = partNumber;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}



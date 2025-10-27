package com.example.graph_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j Node representing a Part in the PLM system.
 * Synced from bom-service MySQL database.
 */
@Node("Part")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartNode {
    
    @Id
    private String id;
    
    private String title;
    private String description;
    private String stage;
    private String status;
    private String level;
    private String creator;
    private LocalDateTime createTime;
    
    /**
     * Hierarchical relationships - children of this part
     * Using relationship properties to store quantity
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private List<PartUsageRelationship> children = new ArrayList<>();
    
    /**
     * Hierarchical relationships - parents of this part
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.INCOMING)
    private List<PartUsageRelationship> parents = new ArrayList<>();
    
    /**
     * Documents linked to this part
     */
    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.OUTGOING)
    private List<DocumentNode> linkedDocuments = new ArrayList<>();
    
    /**
     * User who created this part
     */
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode createdBy;
    
    /**
     * Changes that affect this part
     */
    @Relationship(type = "AFFECTED_BY", direction = Relationship.Direction.INCOMING)
    private List<ChangeNode> affectingChanges = new ArrayList<>();
    
    // Convenience constructors
    public PartNode(String id, String title, String stage, String level, String creator) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.level = level;
        this.creator = creator;
        this.createTime = LocalDateTime.now();
    }
}


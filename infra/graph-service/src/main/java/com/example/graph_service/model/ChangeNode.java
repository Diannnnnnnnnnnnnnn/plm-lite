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
 * Neo4j Node representing a Change Request/Task in the PLM system.
 * Synced from change-service MySQL database.
 */
@Node("Change")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNode {
    
    @Id
    private String id;
    
    private String title;
    private String description;
    private String status;
    private String priority;
    private String changeType;
    private LocalDateTime createTime;
    
    /**
     * Parts affected by this change
     */
    @Relationship(type = "AFFECTS", direction = Relationship.Direction.OUTGOING)
    private List<PartNode> affectedParts = new ArrayList<>();
    
    /**
     * Documents related to this change
     */
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.INCOMING)
    private List<DocumentNode> relatedDocuments = new ArrayList<>();
    
    /**
     * User who initiated this change
     */
    @Relationship(type = "INITIATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode initiator;
    
    /**
     * Users who reviewed or are reviewing this change
     */
    @Relationship(type = "REVIEWED_BY", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> reviewers = new ArrayList<>();
    
    /**
     * Tasks associated with this change
     */
    @Relationship(type = "HAS_TASK", direction = Relationship.Direction.OUTGOING)
    private List<TaskNode> tasks = new ArrayList<>();
    
    // Convenience constructors
    public ChangeNode(String id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createTime = LocalDateTime.now();
    }
}


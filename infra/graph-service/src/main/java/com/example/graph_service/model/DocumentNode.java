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
 * Neo4j Node representing a Document in the PLM system.
 * Synced from document-service MySQL database.
 */
@Node("Document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNode {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private String version;
    private String status;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createTime;
    
    /**
     * Parts linked to this document
     */
    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.INCOMING)
    private List<PartNode> linkedParts = new ArrayList<>();
    
    /**
     * User who created this document
     */
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode creator;
    
    /**
     * User who uploaded this document version
     */
    @Relationship(type = "UPLOADED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode uploader;
    
    /**
     * Changes related to this document
     */
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private List<ChangeNode> relatedChanges = new ArrayList<>();
    
    // Convenience constructors
    public DocumentNode(String id, String name, String description, String version, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.status = status;
        this.createTime = LocalDateTime.now();
    }
}


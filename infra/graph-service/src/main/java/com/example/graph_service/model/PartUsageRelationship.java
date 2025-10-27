package com.example.graph_service.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Relationship properties for HAS_CHILD relationships between parts.
 * Stores quantity and other metadata about the parent-child relationship.
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartUsageRelationship {
    
    @Id
    @GeneratedValue
    private Long id;
    
    /**
     * Quantity of the child part used in the parent
     */
    private Integer quantity;
    
    /**
     * When this relationship was created
     */
    private LocalDateTime createdAt;
    
    /**
     * The child part in this relationship
     */
    @TargetNode
    private PartNode childPart;
    
    // Convenience constructor
    public PartUsageRelationship(Integer quantity, PartNode childPart) {
        this.quantity = quantity;
        this.childPart = childPart;
        this.createdAt = LocalDateTime.now();
    }
}


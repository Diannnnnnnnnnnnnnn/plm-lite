package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event DTO for synchronizing data from microservices to Neo4j graph database.
 * This event is published by services (BOM, Document, Change) when entities are created/updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphSyncEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Type of entity being synced
     */
    private EntityType entityType;
    
    /**
     * Operation being performed
     */
    private Operation operation;
    
    /**
     * Unique identifier of the entity
     */
    private String entityId;
    
    /**
     * Entity data as key-value pairs
     */
    private Map<String, Object> data;
    
    /**
     * Timestamp when the event was created
     */
    private LocalDateTime timestamp;
    
    /**
     * Source service that generated the event
     */
    private String sourceService;
    
    /**
     * Entity types that can be synced to Neo4j
     */
    public enum EntityType {
        PART,           // From BOM service
        DOCUMENT,       // From Document service
        CHANGE,         // From Change service
        USER,           // From User service
        TASK,           // From Task service
        PART_USAGE,     // Part parent-child relationship
        PART_DOCUMENT_LINK,  // Part-Document link
        CHANGE_PART      // Change affects Part
    }
    
    /**
     * Operations that can be performed
     */
    public enum Operation {
        CREATE,         // Create new node/relationship
        UPDATE,         // Update existing node properties
        DELETE,         // Delete node/relationship
        LINK,           // Create relationship
        UNLINK          // Delete relationship
    }
    
    // Convenience constructors
    public GraphSyncEvent(EntityType entityType, Operation operation, String entityId, Map<String, Object> data) {
        this.entityType = entityType;
        this.operation = operation;
        this.entityId = entityId;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}


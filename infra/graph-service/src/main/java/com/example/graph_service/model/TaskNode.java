package com.example.graph_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Neo4j Node representing a Task in the PLM system.
 * Synced from task-service MySQL database.
 */
@Node("Task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskNode {

    @Id
    private String id;

    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createTime;

    /**
     * User assigned to this task
     */
    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.INCOMING)
    private UserNode assignee;

    /**
     * User who created this task
     */
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode creator;

    /**
     * Change request related to this task
     */
    @Relationship(type = "RELATED_TO_CHANGE", direction = Relationship.Direction.OUTGOING)
    private ChangeNode relatedChange;

    /**
     * Part related to this task
     */
    @Relationship(type = "RELATED_TO_PART", direction = Relationship.Direction.OUTGOING)
    private PartNode relatedPart;

    // Convenience constructors for backward compatibility
    public TaskNode(String id, String title, UserNode assignee) {
        this.id = id;
        this.title = title;
        this.assignee = assignee;
        this.createTime = LocalDateTime.now();
    }
    
    public TaskNode(String id, String title) {
        this.id = id;
        this.title = title;
        this.createTime = LocalDateTime.now();
    }
}

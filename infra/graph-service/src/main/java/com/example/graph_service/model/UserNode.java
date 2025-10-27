package com.example.graph_service.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j Node representing a User in the PLM system.
 * Synced from user-service MySQL database.
 */
@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNode {

    @Id
    private String id;

    private String username;
    private String email;
    private String department;
    private String role;

    /**
     * Parts created by this user
     */
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private List<PartNode> createdParts = new ArrayList<>();

    /**
     * Documents created by this user
     */
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private List<DocumentNode> createdDocuments = new ArrayList<>();

    /**
     * Changes initiated by this user
     */
    @Relationship(type = "INITIATED_BY", direction = Relationship.Direction.INCOMING)
    private List<ChangeNode> initiatedChanges = new ArrayList<>();

    /**
     * Tasks assigned to this user
     */
    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    private List<TaskNode> assignedTasks = new ArrayList<>();

    /**
     * Manager of this user (organizational hierarchy)
     */
    @Relationship(type = "REPORTS_TO", direction = Relationship.Direction.OUTGOING)
    private UserNode manager;

    /**
     * Direct reports of this user
     */
    @Relationship(type = "REPORTS_TO", direction = Relationship.Direction.INCOMING)
    private List<UserNode> directReports = new ArrayList<>();

    // Convenience constructors for backward compatibility
    public UserNode(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    // Legacy compatibility - name maps to username
    public String getName() { 
        return username; 
    }
    
    public void setName(String name) { 
        this.username = name; 
    }
}

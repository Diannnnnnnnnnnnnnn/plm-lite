package com.example.graph_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for syncing a User from user-service to graph-service (Neo4j).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncRequest {
    
    private String id;
    private String username;
    private String email;
    private String department;
    private String role;
    
    // Optional: manager ID for reporting relationships
    private String managerId;
}


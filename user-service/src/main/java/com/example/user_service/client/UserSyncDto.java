package com.example.user_service.client;

/**
 * DTO for syncing user data to graph-service (Neo4j).
 */
public class UserSyncDto {
    
    private String id;
    private String username;
    private String email;
    private String department;
    private String role;
    
    // Optional: manager ID for reporting relationships
    private String managerId;

    public UserSyncDto() {
    }

    public UserSyncDto(String id, String username, String email, String department, String role, String managerId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.department = department;
        this.role = role;
        this.managerId = managerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}


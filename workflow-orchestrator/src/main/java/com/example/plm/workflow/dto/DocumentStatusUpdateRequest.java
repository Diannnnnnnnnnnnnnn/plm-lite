package com.example.plm.workflow.dto;

/**
 * DTO for updating document status
 */
public class DocumentStatusUpdateRequest {
    private String status;
    private String user;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}



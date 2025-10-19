package com.example.document_service.dto.workflow;

import java.util.List;

/**
 * DTO for starting document approval workflow in Camunda
 */
public class StartDocumentApprovalRequest {
    private String documentId;
    private String masterId;
    private String version;
    private String creator;
    private List<String> reviewerIds;

    public StartDocumentApprovalRequest() {}

    public StartDocumentApprovalRequest(String documentId, String masterId, String version, 
                                       String creator, List<String> reviewerIds) {
        this.documentId = documentId;
        this.masterId = masterId;
        this.version = version;
        this.creator = creator;
        this.reviewerIds = reviewerIds;
    }

    // Getters and setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public List<String> getReviewerIds() { return reviewerIds; }
    public void setReviewerIds(List<String> reviewerIds) { this.reviewerIds = reviewerIds; }
}


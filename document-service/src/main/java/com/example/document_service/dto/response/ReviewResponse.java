package com.example.document_service.dto.response;

public class ReviewResponse {
    private Long documentId;
    private String status;
    private String reviewerId;
    private String decision;
    private String comments;

    public ReviewResponse() {
    }

    public ReviewResponse(Long documentId, String status, String reviewerId, String decision, String comments) {
        this.documentId = documentId;
        this.status = status;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.comments = comments;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}

package com.example.document_service.dto.request;

public class ApproveRejectRequest {
    private Boolean approved;
    private String user;
    private String comment;

    public ApproveRejectRequest() {}

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

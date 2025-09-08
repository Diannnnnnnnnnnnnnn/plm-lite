package com.example.document_service.dto.request;

import java.util.List;

public class SubmitForReviewRequest {
    private String user;
    private List<String> reviewerIds;

    public SubmitForReviewRequest() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getReviewerIds() {
        return reviewerIds;
    }

    public void setReviewerIds(List<String> reviewerIds) {
        this.reviewerIds = reviewerIds;
    }

    @Override
    public String toString() {
        return "SubmitForReviewRequest{" +
                "user='" + user + '\'' +
                ", reviewerIds=" + reviewerIds +
                '}';
    }
}

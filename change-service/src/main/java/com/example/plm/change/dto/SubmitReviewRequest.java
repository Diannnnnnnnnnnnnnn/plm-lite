package com.example.plm.change.dto;

import java.util.List;

public class SubmitReviewRequest {
    private String user;
    private List<String> reviewerIds;

    public SubmitReviewRequest() {}

    public SubmitReviewRequest(String user, List<String> reviewerIds) {
        this.user = user;
        this.reviewerIds = reviewerIds;
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
}

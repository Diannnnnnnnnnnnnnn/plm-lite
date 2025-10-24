package com.example.document_service.dto.request;

import java.util.List;

public class SubmitForReviewRequest {
    private String user;
    private List<String> reviewerIds; // Legacy support
    
    // NEW: Two-stage review support
    private String initialReviewer;
    private String technicalReviewer;
    private Boolean twoStageReview;

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

    public String getInitialReviewer() {
        return initialReviewer;
    }

    public void setInitialReviewer(String initialReviewer) {
        this.initialReviewer = initialReviewer;
    }

    public String getTechnicalReviewer() {
        return technicalReviewer;
    }

    public void setTechnicalReviewer(String technicalReviewer) {
        this.technicalReviewer = technicalReviewer;
    }

    public Boolean getTwoStageReview() {
        return twoStageReview;
    }

    public void setTwoStageReview(Boolean twoStageReview) {
        this.twoStageReview = twoStageReview;
    }

    @Override
    public String toString() {
        return "SubmitForReviewRequest{" +
                "user='" + user + '\'' +
                ", reviewerIds=" + reviewerIds +
                ", initialReviewer='" + initialReviewer + '\'' +
                ", technicalReviewer='" + technicalReviewer + '\'' +
                ", twoStageReview=" + twoStageReview +
                '}';
    }
}

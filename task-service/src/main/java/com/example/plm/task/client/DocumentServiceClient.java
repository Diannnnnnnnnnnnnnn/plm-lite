package com.example.plm.task.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "document-service", url = "${document-service.url:http://localhost:8083}")
public interface DocumentServiceClient {

    @PostMapping("/api/v1/documents/{id}/review-complete")
    void completeReview(@PathVariable("id") String documentId,
                       @RequestBody ReviewCompleteRequest request);
}

class ReviewCompleteRequest {
    private Boolean approved;
    private String user;
    private String comment;

    public ReviewCompleteRequest() {
    }

    public ReviewCompleteRequest(Boolean approved, String user, String comment) {
        this.approved = approved;
        this.user = user;
        this.comment = comment;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

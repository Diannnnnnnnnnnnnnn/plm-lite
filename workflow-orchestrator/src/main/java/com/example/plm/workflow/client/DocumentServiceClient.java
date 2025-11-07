package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.plm.workflow.dto.ApproveRejectRequest;

/**
 * Feign Client for Document Service
 */
@FeignClient(name = "document-service")
public interface DocumentServiceClient {
    
    @PostMapping("/api/v1/documents/{id}/review-complete")
    void completeReview(@PathVariable("id") String id, @RequestBody ApproveRejectRequest request);
    
    @PostMapping("/api/v1/documents/{id}/initiate-change-edit")
    void initiateChangeBasedEdit(
        @PathVariable("id") String documentId,
        @RequestParam("changeId") String changeId,
        @RequestParam("user") String user
    );
}


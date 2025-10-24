package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.plm.workflow.dto.ApproveRejectRequest;

/**
 * Feign Client for Document Service
 */
@FeignClient(name = "document-service", url = "http://localhost:8081")
public interface DocumentServiceClient {
    
    @PostMapping("/api/v1/documents/{id}/review-complete")
    void completeReview(@PathVariable("id") String id, @RequestBody ApproveRejectRequest request);
}


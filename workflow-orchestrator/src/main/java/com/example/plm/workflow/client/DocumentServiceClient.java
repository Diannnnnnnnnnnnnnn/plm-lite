package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.plm.workflow.dto.DocumentStatusUpdateRequest;

/**
 * Feign Client for Document Service
 */
@FeignClient(name = "document-service", url = "http://localhost:8081")
public interface DocumentServiceClient {
    
    @PutMapping("/api/v1/documents/{id}")
    void updateDocumentStatus(@PathVariable("id") String id, @RequestBody DocumentStatusUpdateRequest request);
}


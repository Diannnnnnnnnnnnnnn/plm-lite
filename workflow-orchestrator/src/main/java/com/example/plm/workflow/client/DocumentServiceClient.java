package com.example.plm.workflow.client;

import com.example.plm.workflow.dto.DocumentStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for Document Service
 */
@FeignClient(name = "document-service", url = "http://localhost:8081")
public interface DocumentServiceClient {
    
    @PutMapping("/api/documents/{id}/status")
    void updateDocumentStatus(@PathVariable("id") String id, @RequestBody DocumentStatusUpdateRequest request);
}


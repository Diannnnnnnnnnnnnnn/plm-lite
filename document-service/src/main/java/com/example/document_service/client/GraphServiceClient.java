package com.example.document_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for calling Graph Service to sync Document data to Neo4j
 */
@FeignClient(name = "graph-service", fallback = GraphServiceClientFallback.class)
public interface GraphServiceClient {

    @PostMapping("/api/graph/sync/document")
    ResponseEntity<String> syncDocument(@RequestBody DocumentSyncDto document);

    @PostMapping("/api/graph/sync/part-document-link")
    ResponseEntity<String> syncPartDocumentLink(@RequestBody PartDocumentLinkDto link);

    @DeleteMapping("/api/graph/sync/document/{documentId}")
    ResponseEntity<String> deleteDocument(@PathVariable("documentId") String documentId);
}


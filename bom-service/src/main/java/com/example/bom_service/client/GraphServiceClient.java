package com.example.bom_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for calling Graph Service to sync BOM data to Neo4j
 */
@FeignClient(name = "graph-service", fallback = GraphServiceClientFallback.class)
public interface GraphServiceClient {

    @PostMapping("/api/graph/sync/part")
    ResponseEntity<String> syncPart(@RequestBody PartSyncDto part);

    @PostMapping("/api/graph/sync/part-usage")
    ResponseEntity<String> syncPartUsage(@RequestBody PartUsageDto partUsage);

    @PostMapping("/api/graph/sync/part-document-link")
    ResponseEntity<String> syncPartDocumentLink(@RequestBody PartDocumentLinkDto link);

    @DeleteMapping("/api/graph/sync/part/{partId}")
    ResponseEntity<String> deletePart(@PathVariable("partId") String partId);
}


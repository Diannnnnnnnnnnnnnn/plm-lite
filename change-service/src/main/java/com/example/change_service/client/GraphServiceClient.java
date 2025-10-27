package com.example.change_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for calling Graph Service to sync Change data to Neo4j
 */
@FeignClient(name = "graph-service", url = "http://localhost:8090", fallback = GraphServiceClientFallback.class)
public interface GraphServiceClient {

    @PostMapping("/api/graph/sync/change")
    ResponseEntity<String> syncChange(@RequestBody ChangeSyncDto change);

    @PostMapping("/api/graph/sync/change-part")
    ResponseEntity<String> syncChangePart(
        @RequestParam("changeId") String changeId,
        @RequestParam("partId") String partId
    );

    @DeleteMapping("/api/graph/sync/change/{changeId}")
    ResponseEntity<String> deleteChange(@PathVariable("changeId") String changeId);
}


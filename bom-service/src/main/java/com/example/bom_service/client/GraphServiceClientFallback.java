package com.example.bom_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback for Graph Service Client - handles gracefully when Graph Service is down
 * Main BOM operations will continue working even if graph sync fails
 */
@Component
@Slf4j
public class GraphServiceClientFallback implements GraphServiceClient {

    @Override
    public ResponseEntity<String> syncPart(PartSyncDto part) {
        log.warn("Graph Service unavailable - could not sync part: {}", part.getId());
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> syncPartUsage(PartUsageDto partUsage) {
        log.warn("Graph Service unavailable - could not sync part usage");
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> syncPartDocumentLink(PartDocumentLinkDto link) {
        log.warn("Graph Service unavailable - could not sync part-document link");
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> deletePart(String partId) {
        log.warn("Graph Service unavailable - could not delete part: {}", partId);
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }
}


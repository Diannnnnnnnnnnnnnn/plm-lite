package com.example.change_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback for Graph Service Client - handles gracefully when Graph Service is down
 */
@Component
@Slf4j
public class GraphServiceClientFallback implements GraphServiceClient {

    @Override
    public ResponseEntity<String> syncChange(ChangeSyncDto change) {
        log.warn("Graph Service unavailable - could not sync change: {}", change.getId());
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> syncChangePart(String changeId, String partId) {
        log.warn("Graph Service unavailable - could not sync change-part link");
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> deleteChange(String changeId) {
        log.warn("Graph Service unavailable - could not delete change: {}", changeId);
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }
}


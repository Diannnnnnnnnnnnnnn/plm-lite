package com.example.document_service.client;

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
    public ResponseEntity<String> syncDocument(DocumentSyncDto document) {
        log.warn("Graph Service unavailable - could not sync document: {}", document.getId());
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> syncPartDocumentLink(PartDocumentLinkDto link) {
        log.warn("Graph Service unavailable - could not sync part-document link");
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }

    @Override
    public ResponseEntity<String> deleteDocument(String documentId) {
        log.warn("Graph Service unavailable - could not delete document: {}", documentId);
        return ResponseEntity.ok("Graph sync skipped (service unavailable)");
    }
}


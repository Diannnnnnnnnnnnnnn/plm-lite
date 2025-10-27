package com.example.user_service.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for GraphClient.
 * Provides graceful degradation when graph-service is unavailable.
 */
@Component
public class GraphClientFallback implements GraphClient {

    @Override
    public ResponseEntity<String> syncUser(UserSyncDto user) {
        System.err.println("⚠️ Graph service unavailable - user " + user.getId() + " NOT synced to graph (graceful fallback)");
        return ResponseEntity.status(503).body("Graph service unavailable");
    }

    @Override
    public ResponseEntity<String> deleteUser(String userId) {
        System.err.println("⚠️ Graph service unavailable - user " + userId + " NOT deleted from graph (graceful fallback)");
        return ResponseEntity.status(503).body("Graph service unavailable");
    }
}


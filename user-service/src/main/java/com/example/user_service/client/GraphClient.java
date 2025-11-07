package com.example.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for calling graph-service to sync users to Neo4j.
 * Falls back gracefully if graph-service is unavailable.
 */
@FeignClient(
    name = "graph-service",
    fallback = GraphClientFallback.class
)
public interface GraphClient {

    /**
     * Sync user creation to Neo4j graph database
     */
    @PostMapping("/api/graph/sync/user")
    ResponseEntity<String> syncUser(@RequestBody UserSyncDto user);

    /**
     * Delete user from Neo4j graph database
     */
    @DeleteMapping("/api/graph/sync/user/{userId}")
    ResponseEntity<String> deleteUser(@PathVariable("userId") String userId);
}

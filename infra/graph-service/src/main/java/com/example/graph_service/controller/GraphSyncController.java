package com.example.graph_service.controller;

import com.example.graph_service.dto.PartSyncRequest;
import com.example.graph_service.dto.DocumentSyncRequest;
import com.example.graph_service.dto.ChangeSyncRequest;
import com.example.graph_service.dto.PartDocumentLinkRequest;
import com.example.graph_service.dto.PartUsageRequest;
import com.example.graph_service.dto.TaskSyncRequest;
import com.example.graph_service.dto.UserSyncRequest;
import com.example.graph_service.service.GraphSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for synchronizing data from other microservices to Neo4j.
 * Called directly by BOM, Document, and Change services via Feign clients.
 */
@RestController
@RequestMapping("/api/graph/sync")
@RequiredArgsConstructor
@Slf4j
public class GraphSyncController {

    private final GraphSyncService graphSyncService;

    // ==========================================
    // PART SYNC ENDPOINTS (from BOM Service)
    // ==========================================

    /**
     * Sync a part creation to Neo4j
     * Called when a new part is created in BOM service
     */
    @PostMapping("/part")
    public ResponseEntity<String> syncPart(@RequestBody PartSyncRequest request) {
        log.info("Syncing part to graph: {}", request.getId());
        try {
            graphSyncService.syncPart(request);
            return ResponseEntity.ok("Part synced successfully");
        } catch (Exception e) {
            log.error("Error syncing part: {}", request.getId(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Sync part usage (BOM hierarchy) to Neo4j
     * Called when a parent-child relationship is created
     */
    @PostMapping("/part-usage")
    public ResponseEntity<String> syncPartUsage(@RequestBody PartUsageRequest request) {
        log.info("Syncing part usage: {} -> {}", request.getParentPartId(), request.getChildPartId());
        try {
            graphSyncService.syncPartUsage(request);
            return ResponseEntity.ok("Part usage synced successfully");
        } catch (Exception e) {
            log.error("Error syncing part usage", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete part from Neo4j
     */
    @DeleteMapping("/part/{partId}")
    public ResponseEntity<String> deletePart(@PathVariable String partId) {
        log.info("Deleting part from graph: {}", partId);
        try {
            graphSyncService.deletePart(partId);
            return ResponseEntity.ok("Part deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting part: {}", partId, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // DOCUMENT SYNC ENDPOINTS (from Document Service)
    // ==========================================

    /**
     * Sync a document to Neo4j
     * Called when a new document is uploaded
     */
    @PostMapping("/document")
    public ResponseEntity<String> syncDocument(@RequestBody DocumentSyncRequest request) {
        log.info("Syncing document to graph: {}", request.getId());
        try {
            graphSyncService.syncDocument(request);
            return ResponseEntity.ok("Document synced successfully");
        } catch (Exception e) {
            log.error("Error syncing document: {}", request.getId(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Sync part-document link to Neo4j
     * Called when a part is linked to a document
     */
    @PostMapping("/part-document-link")
    public ResponseEntity<String> syncPartDocumentLink(@RequestBody PartDocumentLinkRequest request) {
        log.info("Syncing part-document link: {} -> {}", request.getPartId(), request.getDocumentId());
        try {
            graphSyncService.syncPartDocumentLink(request);
            return ResponseEntity.ok("Part-document link synced successfully");
        } catch (Exception e) {
            log.error("Error syncing part-document link", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete document from Neo4j
     */
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<String> deleteDocument(@PathVariable String documentId) {
        log.info("Deleting document from graph: {}", documentId);
        try {
            graphSyncService.deleteDocument(documentId);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting document: {}", documentId, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // CHANGE SYNC ENDPOINTS (from Change Service)
    // ==========================================

    /**
     * Sync a change request to Neo4j
     * Called when a new change is created
     */
    @PostMapping("/change")
    public ResponseEntity<String> syncChange(@RequestBody ChangeSyncRequest request) {
        log.info("Syncing change to graph: {}", request.getId());
        try {
            graphSyncService.syncChange(request);
            return ResponseEntity.ok("Change synced successfully");
        } catch (Exception e) {
            log.error("Error syncing change: {}", request.getId(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Sync change-part relationship (change affects part)
     * Called when a change is linked to a part
     */
    @PostMapping("/change-part")
    public ResponseEntity<String> syncChangePart(
            @RequestParam String changeId,
            @RequestParam String partId) {
        log.info("Syncing change-part link: {} affects {}", changeId, partId);
        try {
            graphSyncService.syncChangePart(changeId, partId);
            return ResponseEntity.ok("Change-part link synced successfully");
        } catch (Exception e) {
            log.error("Error syncing change-part link", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete change from Neo4j
     */
    @DeleteMapping("/change/{changeId}")
    public ResponseEntity<String> deleteChange(@PathVariable String changeId) {
        log.info("Deleting change from graph: {}", changeId);
        try {
            graphSyncService.deleteChange(changeId);
            return ResponseEntity.ok("Change deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting change: {}", changeId, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // TASK SYNC ENDPOINTS (from Task Service)
    // ==========================================

    /**
     * Sync a task to Neo4j
     * Called when a new task is created in task service
     */
    @PostMapping("/task")
    public ResponseEntity<String> syncTask(@RequestBody TaskSyncRequest request) {
        log.info("Syncing task to graph: {}", request.getId());
        try {
            graphSyncService.syncTask(request);
            return ResponseEntity.ok("Task synced successfully");
        } catch (Exception e) {
            log.error("Error syncing task: {}", request.getId(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete task from Neo4j
     */
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable String taskId) {
        log.info("Deleting task from graph: {}", taskId);
        try {
            graphSyncService.deleteTask(taskId);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting task: {}", taskId, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // USER SYNC ENDPOINTS (from User Service)
    // ==========================================

    /**
     * Sync a user to Neo4j
     * Called when a new user is created in user service
     */
    @PostMapping("/user")
    public ResponseEntity<String> syncUser(@RequestBody UserSyncRequest request) {
        log.info("Syncing user to graph: {}", request.getId());
        try {
            graphSyncService.syncUser(request);
            return ResponseEntity.ok("User synced successfully");
        } catch (Exception e) {
            log.error("Error syncing user: {}", request.getId(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete user from Neo4j
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        log.info("Deleting user from graph: {}", userId);
        try {
            graphSyncService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // HEALTH CHECK
    // ==========================================

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Graph Sync API is healthy");
    }
}


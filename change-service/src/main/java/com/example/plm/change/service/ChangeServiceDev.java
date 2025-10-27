package com.example.plm.change.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.plm.change.client.TaskServiceClient;
import com.example.plm.change.dto.ChangeResponse;
import com.example.plm.change.dto.CreateChangeRequest;
import com.example.plm.change.model.Change;
import com.example.plm.change.model.ChangeBom;
import com.example.plm.change.model.ChangePart;
import com.example.plm.change.model.ChangeSearchDocument;
import com.example.plm.change.repository.mysql.ChangeBomRepository;
import com.example.plm.change.repository.mysql.ChangeDocumentRepository;
import com.example.plm.change.repository.mysql.ChangePartRepository;
import com.example.plm.change.repository.mysql.ChangeRepository;
import com.example.plm.common.model.Status;

// Graph Service imports
import com.example.change_service.client.GraphServiceClient;
import com.example.change_service.client.ChangeSyncDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Profile("dev")
@Slf4j
public class ChangeServiceDev {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ChangeDocumentRepository changeDocumentRepository;

    @Autowired
    private ChangePartRepository changePartRepository;

    @Autowired
    private ChangeBomRepository changeBomRepository;

    @Autowired(required = false)
    private TaskServiceClient taskServiceClient;

    @Autowired(required = false)
    private DocumentServiceClient documentServiceClient;

    @Autowired(required = false)
    private UserServiceClient userServiceClient;

    @Autowired(required = false)
    private GraphServiceClient graphServiceClient;

    @FeignClient(name = "user-service-dev", url = "http://localhost:8083")
    public interface UserServiceClient {
        @GetMapping("/users/{id}")
        UserResponse getUserById(@PathVariable("id") Long id);
    }

    public static class UserResponse {
        private Long id;
        private String username;

        public UserResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @FeignClient(name = "document-service-dev", url = "http://localhost:8081")
    public interface DocumentServiceClient {
        @PostMapping("/api/v1/documents/{id}/initiate-change-edit")
        DocumentResponse initiateChangeBasedEdit(
            @PathVariable("id") String documentId,
            @RequestParam("changeId") String changeId,
            @RequestParam("user") String user
        );
    }

    public static class DocumentResponse {
        private String id;
        private String masterId;
        private String title;
        private String status;
        private String version;
        private boolean isActive;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getMasterId() { return masterId; }
        public void setMasterId(String masterId) { this.masterId = masterId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    @Transactional
    public ChangeResponse createChange(CreateChangeRequest request) {
        // In dev mode, skip document validation
        String changeId = UUID.randomUUID().toString();

        Change change = new Change(
            changeId,
            request.getTitle(),
            request.getStage(),
            request.getChangeClass(),
            request.getProduct(),
            Status.IN_WORK,
            request.getCreator(),
            LocalDateTime.now(),
            request.getChangeReason(),
            request.getChangeDocument()
        );

        change = changeRepository.save(change);

        // Sync to Neo4j
        syncChangeToGraph(change);

        // Create BOM relationships if provided
        if (request.getBomIds() != null && !request.getBomIds().isEmpty()) {
            for (String bomId : request.getBomIds()) {
                ChangeBom changeBom = new ChangeBom(change, bomId);
                changeBomRepository.save(changeBom);
            }
        }

        // Create Part relationships if provided
        if (request.getPartIds() != null && !request.getPartIds().isEmpty()) {
            for (String partId : request.getPartIds()) {
                ChangePart changePart = new ChangePart(
                    UUID.randomUUID().toString(),
                    change,
                    partId
                );
                changePartRepository.save(changePart);
                
                // Sync to Neo4j graph
                if (graphServiceClient != null) {
                    try {
                        graphServiceClient.syncChangePart(changeId, partId);
                        log.info("✅ Change-Part link {} -> {} synced to graph", changeId, partId);
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to sync change-part link to graph: {}", e.getMessage());
                    }
                }
            }
        }

        return mapToResponse(change);
    }
    
    private void syncChangeToGraph(Change change) {
        if (graphServiceClient == null) {
            log.warn("GraphServiceClient not available, skipping sync");
            return;
        }
        
        try {
            ChangeSyncDto dto = new ChangeSyncDto(
                change.getId(),
                change.getTitle(),
                change.getChangeReason() != null ? change.getChangeReason() : "", // description
                change.getStatus() != null ? change.getStatus().name() : "IN_WORK", // status
                change.getStage() != null ? change.getStage().name() : "DESIGN", // priority (using stage as priority)
                change.getChangeClass() != null ? change.getChangeClass() : "NORMAL", // changeType
                change.getCreator(), // initiator
                change.getCreateTime()
            );
            graphServiceClient.syncChange(dto);
            log.info("✅ Change {} synced to graph successfully", change.getId());
        } catch (Exception e) {
            log.warn("⚠️ Failed to sync change {} to graph: {}", change.getId(), e.getMessage());
        }
    }

    @Transactional
    public ChangeResponse submitForReview(String changeId, List<String> reviewerIds) {
        Change change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_WORK) {
            throw new IllegalStateException("Only changes in work can be submitted for review");
        }

        change.setStatus(Status.IN_REVIEW);
        change = changeRepository.save(change);

        // Create review tasks for each reviewer
        if (taskServiceClient != null && reviewerIds != null && !reviewerIds.isEmpty()) {
            for (String reviewerId : reviewerIds) {
                try {
                    Long userId = Long.parseLong(reviewerId);
                    
                    // Fetch username from user service for task filtering
                    String username = null;
                    if (userServiceClient != null) {
                        try {
                            UserResponse user = userServiceClient.getUserById(userId);
                            username = user.getUsername();
                            System.out.println("Resolved user ID " + userId + " to username: " + username);
                        } catch (Exception e) {
                            System.err.println("Failed to fetch username for user ID " + userId + ": " + e.getMessage());
                        }
                    }
                    
                    TaskServiceClient.TaskDTO task = new TaskServiceClient.TaskDTO(
                        "Review Change: " + change.getTitle(),
                        "Please review change " + changeId + " - " + change.getChangeReason(),
                        userId,
                        username // Pass username for filtering
                    );
                    taskServiceClient.createTask(task);
                    System.out.println("Created review task for user ID " + userId + " (username: " + username + ")");
                } catch (Exception e) {
                    // Log error but don't fail the whole operation
                    System.err.println("Failed to create task for reviewer " + reviewerId + ": " + e.getMessage());
                }
            }
        }

        return mapToResponse(change);
    }

    @Transactional
    public ChangeResponse approveChange(String changeId) {
        Change change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_REVIEW) {
            throw new IllegalStateException("Only changes in review can be approved");
        }

        change.setStatus(Status.RELEASED);
        change = changeRepository.save(change);

        // IMPORTANT: When change is approved, unlock the affected documents
        // This allows users to edit RELEASED documents through the approved change
        if (documentServiceClient != null && change.getChangeDocument() != null) {
            try {
                String documentId = change.getChangeDocument();
                String user = change.getCreator(); // Use change creator as the user
                
                // Call document service to initiate change-based editing
                DocumentResponse unlockedDoc = 
                    documentServiceClient.initiateChangeBasedEdit(documentId, changeId, user);
                
                System.out.println(String.format(
                    "INFO: Document unlocked for editing via approved change. " +
                    "Change: %s, Document: %s → %s (v%s, status=%s)",
                    changeId, documentId, unlockedDoc.getId(), 
                    unlockedDoc.getVersion(), unlockedDoc.getStatus()
                ));
            } catch (Exception e) {
                // Log error but don't fail the approval
                System.err.println(String.format(
                    "WARN: Failed to unlock document for change %s: %s. " +
                    "User may need to manually unlock the document.",
                    changeId, e.getMessage()
                ));
                e.printStackTrace();
            }
        }

        return mapToResponse(change);
    }

    public Optional<ChangeResponse> getChangeById(String id) {
        return changeRepository.findById(id).map(this::mapToResponse);
    }

    public List<ChangeResponse> getAllChanges() {
        return changeRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> getChangesByStatus(Status status) {
        return changeRepository.findByStatus(status).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> getChangesByCreator(String creator) {
        return changeRepository.findByCreator(creator).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> searchChanges(String keyword) {
        return changeRepository.findByKeyword(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeSearchDocument> searchChangesElastic(String query) {
        // In dev mode, return empty list since we don't have Elasticsearch
        return List.of();
    }

    private ChangeResponse mapToResponse(Change change) {
        ChangeResponse response = new ChangeResponse(
            change.getId(),
            change.getTitle(),
            change.getStage(),
            change.getChangeClass(),
            change.getProduct(),
            change.getStatus(),
            change.getCreator(),
            change.getCreateTime(),
            change.getChangeReason(),
            change.getChangeDocument()
        );

        // Load BOM IDs
        List<String> bomIds = changeBomRepository.findByChangeId(change.getId())
            .stream()
            .map(ChangeBom::getBomId)
            .collect(Collectors.toList());
        response.setBomIds(bomIds);

        // Load Part IDs
        List<String> partIds = changePartRepository.findByChangeId(change.getId())
            .stream()
            .map(ChangePart::getPartId)
            .collect(Collectors.toList());
        response.setPartIds(partIds);

        return response;
    }
}
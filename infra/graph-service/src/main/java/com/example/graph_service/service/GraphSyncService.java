package com.example.graph_service.service;

import com.example.graph_service.dto.*;
import com.example.graph_service.model.*;
import com.example.graph_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for synchronizing data from microservices to Neo4j graph database.
 * Handles creation of nodes and relationships based on events from other services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphSyncService {

    private final PartNodeRepository partRepo;
    private final DocumentNodeRepository documentRepo;
    private final ChangeNodeRepository changeRepo;
    private final UserNodeRepository userRepo;
    private final TaskNodeRepository taskRepo;

    // ==========================================
    // PART SYNC
    // ==========================================

    /**
     * Sync a part from BOM service to Neo4j
     */
    @Transactional
    public void syncPart(PartSyncRequest request) {
        log.info("Syncing part: {} - {}", request.getId(), request.getTitle());
        
        // Check if part already exists
        PartNode part = partRepo.findById(request.getId())
                .orElse(new PartNode());
        
        // Update fields
        part.setId(request.getId());
        part.setTitle(request.getTitle());
        part.setDescription(request.getDescription());
        part.setStage(request.getStage());
        part.setStatus(request.getStatus());
        part.setLevel(request.getLevel());
        part.setCreator(request.getCreator());
        part.setCreateTime(request.getCreateTime());
        
        // Save to Neo4j
        partRepo.save(part);
        log.info("Part synced successfully: {}", request.getId());
        
        // Link to creator user if exists
        linkPartToCreator(part, request.getCreator());
    }

    /**
     * Sync part usage (BOM hierarchy) from BOM service
     */
    @Transactional
    public void syncPartUsage(PartUsageRequest request) {
        log.info("Syncing part usage: {} -> {} (qty: {})", 
                 request.getParentPartId(), request.getChildPartId(), request.getQuantity());
        
        PartNode parent = partRepo.findById(request.getParentPartId())
                .orElseThrow(() -> new RuntimeException("Parent part not found: " + request.getParentPartId()));
        
        PartNode child = partRepo.findById(request.getChildPartId())
                .orElseThrow(() -> new RuntimeException("Child part not found: " + request.getChildPartId()));
        
        // Create relationship with quantity
        PartUsageRelationship usage = new PartUsageRelationship(
                request.getQuantity(),
                child
        );
        
        // Add to parent's children list
        if (parent.getChildren() == null) {
            parent.setChildren(new java.util.ArrayList<>());
        }
        parent.getChildren().add(usage);
        
        // Save parent (will persist the relationship)
        partRepo.save(parent);
        log.info("Part usage synced successfully");
    }

    /**
     * Delete part from Neo4j
     */
    @Transactional
    public void deletePart(String partId) {
        log.info("Deleting part from graph: {}", partId);
        partRepo.deleteById(partId);
    }

    // ==========================================
    // DOCUMENT SYNC
    // ==========================================

    /**
     * Sync a document from Document service to Neo4j
     */
    @Transactional
    public void syncDocument(DocumentSyncRequest request) {
        log.info("Syncing document: {} - {}", request.getId(), request.getName());
        
        // Check if document already exists
        DocumentNode doc = documentRepo.findById(request.getId())
                .orElse(new DocumentNode());
        
        // Update fields
        doc.setId(request.getId());
        doc.setName(request.getName());
        doc.setDescription(request.getDescription());
        doc.setVersion(request.getVersion());
        doc.setStatus(request.getStatus());
        doc.setFileType(request.getFileType());
        doc.setFileSize(request.getFileSize());
        doc.setCreateTime(request.getCreateTime());
        
        // Save to Neo4j
        documentRepo.save(doc);
        log.info("Document synced successfully: {}", request.getId());
        
        // Link to creator if provided
        if (request.getCreator() != null) {
            linkDocumentToCreator(doc, request.getCreator());
        }
    }

    /**
     * Sync part-document link
     */
    @Transactional
    public void syncPartDocumentLink(PartDocumentLinkRequest request) {
        log.info("Syncing part-document link: {} -> {}", request.getPartId(), request.getDocumentId());
        
        PartNode part = partRepo.findById(request.getPartId())
                .orElseThrow(() -> new RuntimeException("Part not found: " + request.getPartId()));
        
        DocumentNode doc = documentRepo.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found: " + request.getDocumentId()));
        
        // Add document to part's linked documents
        if (part.getLinkedDocuments() == null) {
            part.setLinkedDocuments(new java.util.ArrayList<>());
        }
        part.getLinkedDocuments().add(doc);
        
        // Save part (will persist the relationship)
        partRepo.save(part);
        log.info("Part-document link synced successfully");
    }

    /**
     * Delete document from Neo4j
     */
    @Transactional
    public void deleteDocument(String documentId) {
        log.info("Deleting document from graph: {}", documentId);
        documentRepo.deleteById(documentId);
    }

    // ==========================================
    // CHANGE SYNC
    // ==========================================

    /**
     * Sync a change request from Change service to Neo4j
     */
    @Transactional
    public void syncChange(ChangeSyncRequest request) {
        log.info("Syncing change: {} - {}", request.getId(), request.getTitle());
        
        // Check if change already exists
        ChangeNode change = changeRepo.findById(request.getId())
                .orElse(new ChangeNode());
        
        // Update fields
        change.setId(request.getId());
        change.setTitle(request.getTitle());
        change.setDescription(request.getDescription());
        change.setStatus(request.getStatus());
        change.setPriority(request.getPriority());
        change.setChangeType(request.getChangeType());
        change.setCreateTime(request.getCreateTime());
        
        // Save to Neo4j
        changeRepo.save(change);
        log.info("Change synced successfully: {}", request.getId());
        
        // Link to initiator if provided
        if (request.getInitiator() != null) {
            linkChangeToInitiator(change, request.getInitiator());
        }
    }

    /**
     * Sync change-part relationship (change affects part)
     */
    @Transactional
    public void syncChangePart(String changeId, String partId) {
        log.info("Syncing change-part link: {} affects {}", changeId, partId);
        
        ChangeNode change = changeRepo.findById(changeId)
                .orElseThrow(() -> new RuntimeException("Change not found: " + changeId));
        
        PartNode part = partRepo.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        
        // Add part to change's affected parts
        if (change.getAffectedParts() == null) {
            change.setAffectedParts(new java.util.ArrayList<>());
        }
        change.getAffectedParts().add(part);
        
        // Save change (will persist the relationship)
        changeRepo.save(change);
        log.info("Change-part link synced successfully");
    }

    /**
     * Delete change from Neo4j
     */
    @Transactional
    public void deleteChange(String changeId) {
        log.info("Deleting change from graph: {}", changeId);
        changeRepo.deleteById(changeId);
    }

    // ==========================================
    // TASK SYNC
    // ==========================================

    /**
     * Sync a task from task-service to Neo4j
     */
    @Transactional
    public void syncTask(TaskSyncRequest request) {
        log.info("Syncing task: {} - {}", request.getId(), request.getTitle());
        
        // Check if task already exists
        TaskNode task = taskRepo.findById(request.getId())
                .orElse(new TaskNode());
        
        // Update fields
        task.setId(request.getId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setCreateTime(request.getCreateTime());
        
        // Save to Neo4j
        taskRepo.save(task);
        log.info("Task synced successfully: {}", request.getId());
        
        // Link to assignee and creator if provided
        if (request.getAssigneeId() != null) {
            linkTaskToAssignee(task, request.getAssigneeId());
        }
        if (request.getCreatorId() != null) {
            linkTaskToCreator(task, request.getCreatorId());
        }
        
        // Link to related entities if provided
        if (request.getRelatedChangeId() != null) {
            linkTaskToChange(task, request.getRelatedChangeId());
        }
        if (request.getRelatedPartId() != null) {
            linkTaskToPart(task, request.getRelatedPartId());
        }
    }

    /**
     * Delete task from Neo4j
     */
    @Transactional
    public void deleteTask(String taskId) {
        log.info("Deleting task from graph: {}", taskId);
        taskRepo.deleteById(taskId);
    }

    // ==========================================
    // USER SYNC
    // ==========================================

    /**
     * Sync a user from user-service to Neo4j
     */
    @Transactional
    public void syncUser(UserSyncRequest request) {
        log.info("Syncing user: {} - {}", request.getId(), request.getUsername());
        
        // Check if user already exists
        UserNode user = userRepo.findById(request.getId())
                .orElse(new UserNode());
        
        // Update fields
        user.setId(request.getId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setRole(request.getRole());
        
        // Save to Neo4j
        userRepo.save(user);
        log.info("User synced successfully: {}", request.getId());
        
        // Link to manager if provided
        if (request.getManagerId() != null) {
            linkUserToManager(user, request.getManagerId());
        }
    }

    /**
     * Delete user from Neo4j
     */
    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user from graph: {}", userId);
        userRepo.deleteById(userId);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void linkPartToCreator(PartNode part, String creatorUsername) {
        try {
            // Find user by username (you may need to adjust this based on your user ID strategy)
            userRepo.findAll().stream()
                    .filter(u -> creatorUsername.equals(u.getUsername()))
                    .findFirst()
                    .ifPresent(user -> {
                        part.setCreatedBy(user);
                        partRepo.save(part);
                        log.info("Linked part {} to creator {}", part.getId(), user.getUsername());
                    });
        } catch (Exception e) {
            log.warn("Could not link part to creator: {}", e.getMessage());
        }
    }

    private void linkDocumentToCreator(DocumentNode doc, String creatorUsername) {
        try {
            userRepo.findAll().stream()
                    .filter(u -> creatorUsername.equals(u.getUsername()))
                    .findFirst()
                    .ifPresent(user -> {
                        doc.setCreator(user);
                        documentRepo.save(doc);
                        log.info("Linked document {} to creator {}", doc.getId(), user.getUsername());
                    });
        } catch (Exception e) {
            log.warn("Could not link document to creator: {}", e.getMessage());
        }
    }

    private void linkChangeToInitiator(ChangeNode change, String initiatorUsername) {
        try {
            userRepo.findAll().stream()
                    .filter(u -> initiatorUsername.equals(u.getUsername()))
                    .findFirst()
                    .ifPresent(user -> {
                        change.setInitiator(user);
                        changeRepo.save(change);
                        log.info("Linked change {} to initiator {}", change.getId(), user.getUsername());
                    });
        } catch (Exception e) {
            log.warn("Could not link change to initiator: {}", e.getMessage());
        }
    }

    private void linkTaskToAssignee(TaskNode task, String assigneeId) {
        try {
            userRepo.findById(assigneeId).ifPresent(user -> {
                task.setAssignee(user);
                taskRepo.save(task);
                log.info("Linked task {} to assignee {}", task.getId(), user.getUsername());
            });
        } catch (Exception e) {
            log.warn("Could not link task to assignee: {}", e.getMessage());
        }
    }

    private void linkTaskToCreator(TaskNode task, String creatorId) {
        try {
            userRepo.findById(creatorId).ifPresent(user -> {
                task.setCreator(user);
                taskRepo.save(task);
                log.info("Linked task {} to creator {}", task.getId(), user.getUsername());
            });
        } catch (Exception e) {
            log.warn("Could not link task to creator: {}", e.getMessage());
        }
    }

    private void linkTaskToChange(TaskNode task, String changeId) {
        try {
            changeRepo.findById(changeId).ifPresent(change -> {
                task.setRelatedChange(change);
                taskRepo.save(task);
                log.info("Linked task {} to change {}", task.getId(), change.getId());
            });
        } catch (Exception e) {
            log.warn("Could not link task to change: {}", e.getMessage());
        }
    }

    private void linkTaskToPart(TaskNode task, String partId) {
        try {
            partRepo.findById(partId).ifPresent(part -> {
                task.setRelatedPart(part);
                taskRepo.save(task);
                log.info("Linked task {} to part {}", task.getId(), part.getId());
            });
        } catch (Exception e) {
            log.warn("Could not link task to part: {}", e.getMessage());
        }
    }

    private void linkUserToManager(UserNode user, String managerId) {
        try {
            userRepo.findById(managerId).ifPresent(manager -> {
                user.setManager(manager);
                userRepo.save(user);
                log.info("Linked user {} to manager {}", user.getId(), manager.getUsername());
            });
        } catch (Exception e) {
            log.warn("Could not link user to manager: {}", e.getMessage());
        }
    }
}


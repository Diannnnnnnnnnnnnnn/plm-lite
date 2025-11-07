package com.example.change_service.service;

import com.example.change_service.dto.CreateChangeRequest;
import com.example.change_service.dto.ChangeResponse;
import com.example.change_service.model.Changes;
import com.example.change_service.model.ChangeDocument;
import com.example.change_service.model.ChangePart;
import com.example.change_service.model.ChangeNode;
import com.example.change_service.model.ChangeSearchDocument;
import com.example.change_service.repository.elasticsearch.ChangeSearchRepository;
import com.example.change_service.repository.mysql.ChangeRepository;
import com.example.change_service.repository.mysql.ChangeDocumentRepository;
import com.example.change_service.repository.mysql.ChangePartRepository;
import com.example.change_service.repository.neo4j.ChangeNodeRepository;
import com.example.change_service.client.WorkflowOrchestratorClient;
import com.example.plm.common.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!dev")
public class ChangeService {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ChangeDocumentRepository changeDocumentRepository;

    @Autowired
    private ChangePartRepository changePartRepository;

    @Autowired(required = false)
    private ChangeNodeRepository changeNodeRepository;

    @Autowired(required = false)
    private ChangeSearchRepository changeSearchRepository;

    @Autowired(required = false)
    private DocumentServiceClient documentServiceClient;

    @Autowired(required = false)
    private WorkflowOrchestratorClient workflowOrchestratorClient;

    @FeignClient(name = "document-service")
    public interface DocumentServiceClient {
        @GetMapping("/api/v1/documents/{id}")
        DocumentInfo getDocument(@PathVariable("id") String id);
        
        @PostMapping("/api/v1/documents/{id}/initiate-change-edit")
        void initiateChangeBasedEdit(
            @PathVariable("id") String documentId,
            @RequestParam("changeId") String changeId,
            @RequestParam("user") String user
        );
    }

    public static class DocumentInfo {
        private String id;
        private String title;
        private Status status;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    @Transactional
    public ChangeResponse createChange(CreateChangeRequest request) {
        if (documentServiceClient != null) {
            DocumentInfo document = documentServiceClient.getDocument(request.getChangeDocument());
            if (document.getStatus() != Status.RELEASED) {
                throw new IllegalStateException("Change can only be applied to released documents");
            }
        }

        String changeId = UUID.randomUUID().toString();

        Changes change = new Changes(
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

        // Sync to Neo4j after transaction commits (to avoid transaction conflicts)
        final String finalChangeId = changeId;
        final String finalDocumentId = request.getChangeDocument();
        if (changeNodeRepository != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        ChangeNode changeNode = new ChangeNode(
                            finalChangeId,
                            request.getTitle(),
                            request.getStage().toString(),
                            request.getChangeClass(),
                            request.getProduct(),
                            Status.IN_WORK.toString(),
                            request.getCreator(),
                            LocalDateTime.now(),
                            request.getChangeReason()
                        );
                        changeNodeRepository.save(changeNode);
                        changeNodeRepository.createDocumentRelationship(finalChangeId, finalDocumentId);
                        System.out.println("✅ Change synced to Neo4j: " + finalChangeId);
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to sync change to Neo4j: " + e.getMessage());
                    }
                }
            });
        }

        if (changeSearchRepository != null) {
            ChangeSearchDocument searchDoc = new ChangeSearchDocument(
                changeId,
                request.getTitle(),
                request.getStage().toString(),
                request.getChangeClass(),
                request.getProduct(),
                Status.IN_WORK.toString(),
                request.getCreator(),
                LocalDateTime.now(),
                request.getChangeReason(),
                List.of(request.getChangeDocument()),
                List.of()
            );
            changeSearchRepository.save(searchDoc);
        }

        return mapToResponse(change);
    }

    @Transactional
    public ChangeResponse submitForReview(String changeId, List<String> reviewerIds) {
        Changes change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_WORK && change.getStatus() != Status.DRAFT) {
            throw new IllegalStateException("Only changes in work or draft can be submitted for review");
        }

        // Validate that we have at least one reviewer
        if (reviewerIds == null || reviewerIds.isEmpty()) {
            throw new IllegalArgumentException("At least one reviewer must be specified");
        }

        change.setStatus(Status.IN_REVIEW);
        change = changeRepository.save(change);

        // Update Neo4j after transaction commits
        final String finalChangeId = changeId;
        if (changeNodeRepository != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        ChangeNode changeNode = changeNodeRepository.findById(finalChangeId).orElse(null);
                        if (changeNode != null) {
                            changeNode.setStatus(Status.IN_REVIEW.toString());
                            changeNodeRepository.save(changeNode);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to update change status in Neo4j: " + e.getMessage());
                    }
                }
            });
        }

        if (changeSearchRepository != null) {
            try {
                Optional<ChangeSearchDocument> searchDoc = changeSearchRepository.findById(changeId);
                if (searchDoc.isPresent()) {
                    ChangeSearchDocument doc = searchDoc.get();
                    doc.setStatus(Status.IN_REVIEW.toString());
                    changeSearchRepository.save(doc);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to update change in Elasticsearch (non-critical): " + e.getMessage());
            }
        }

        // Start workflow orchestration for the change approval
        if (workflowOrchestratorClient != null) {
            try {
                // Use the first reviewer for single-reviewer workflow
                String reviewerId = reviewerIds.get(0);
                
                WorkflowOrchestratorClient.StartChangeApprovalRequest workflowRequest = 
                    new WorkflowOrchestratorClient.StartChangeApprovalRequest(
                        changeId,
                        change.getTitle(),
                        change.getCreator(),
                        reviewerId,
                        change.getChangeDocument()  // Pass document ID
                    );

                System.out.println("🚀 Starting change approval workflow for change: " + changeId);
                System.out.println("   Related document: " + change.getChangeDocument());
                Map<String, String> workflowResponse = workflowOrchestratorClient.startChangeApprovalWorkflow(workflowRequest);
                System.out.println("   ✓ Workflow started with process instance: " + workflowResponse.get("processInstanceKey"));
                
            } catch (Exception e) {
                System.err.println("⚠️ Failed to start workflow orchestration (non-critical): " + e.getMessage());
                e.printStackTrace();
                // Don't fail the entire operation if workflow fails - the status is already updated
            }
        } else {
            System.out.println("⚠️ Workflow orchestrator client not available - change submitted without workflow");
        }

        return mapToResponse(change);
    }

    @Transactional
    public ChangeResponse approveChange(String changeId) {
        Changes change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_REVIEW) {
            throw new IllegalStateException("Only changes in review can be approved");
        }

        change.setStatus(Status.RELEASED);
        change = changeRepository.save(change);

        // IMPORTANT: Unlock the document for editing via approved change
        if (documentServiceClient != null) {
            try {
                documentServiceClient.initiateChangeBasedEdit(
                    change.getChangeDocument(),
                    changeId,
                    change.getCreator()
                );
                System.out.println("Document unlocked for editing via approved change: " + changeId);
            } catch (Exception e) {
                System.err.println("Failed to unlock document for change " + changeId + ": " + e.getMessage());
                // Don't fail the approval if document unlock fails
            }
        }

        // Update Neo4j after transaction commits
        final String finalChangeId = changeId;
        if (changeNodeRepository != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        ChangeNode changeNode = changeNodeRepository.findById(finalChangeId).orElse(null);
                        if (changeNode != null) {
                            changeNode.setStatus(Status.RELEASED.toString());
                            changeNodeRepository.save(changeNode);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to update change status in Neo4j: " + e.getMessage());
                    }
                }
            });
        }

        if (changeSearchRepository != null) {
            Optional<ChangeSearchDocument> searchDoc = changeSearchRepository.findById(changeId);
            if (searchDoc.isPresent()) {
                ChangeSearchDocument doc = searchDoc.get();
                doc.setStatus(Status.RELEASED.toString());
                changeSearchRepository.save(doc);
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
        if (changeSearchRepository != null) {
            return changeSearchRepository.findByTitleContaining(query);
        }
        return List.of();
    }

    @Transactional
    public void updateStatus(String changeId, Status newStatus) {
        Changes change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        System.out.println("Updating change " + changeId + " status from " + change.getStatus() + " to " + newStatus);
        
        change.setStatus(newStatus);
        changeRepository.save(change);

        // Update Neo4j after transaction commits
        final String finalChangeId = changeId;
        if (changeNodeRepository != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        ChangeNode changeNode = changeNodeRepository.findById(finalChangeId).orElse(null);
                        if (changeNode != null) {
                            changeNode.setStatus(newStatus.toString());
                            changeNodeRepository.save(changeNode);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to update change status in Neo4j: " + e.getMessage());
                    }
                }
            });
        }

        if (changeSearchRepository != null) {
            try {
                Optional<ChangeSearchDocument> searchDoc = changeSearchRepository.findById(changeId);
                if (searchDoc.isPresent()) {
                    ChangeSearchDocument doc = searchDoc.get();
                    doc.setStatus(newStatus.toString());
                    changeSearchRepository.save(doc);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to update change in Elasticsearch: " + e.getMessage());
            }
        }
        
        System.out.println("✅ Change status updated successfully");
    }

    @Transactional
    public void deleteChange(String changeId) {
        Changes change = changeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("Change not found with id: " + changeId));
        
        // Delete related records first
        changeDocumentRepository.findByChangeId(changeId).forEach(cd -> changeDocumentRepository.delete(cd));
        changePartRepository.findByChangeId(changeId).forEach(cp -> changePartRepository.delete(cp));
        
        // Delete from Elasticsearch
        if (changeSearchRepository != null) {
            try {
                changeSearchRepository.deleteById(changeId);
            } catch (Exception e) {
                // Log but don't fail if ES delete fails
                System.err.println("Failed to delete change from Elasticsearch: " + e.getMessage());
            }
        }
        
        // Delete from Neo4j
        if (changeNodeRepository != null) {
            try {
                changeNodeRepository.deleteById(changeId);
            } catch (Exception e) {
                // Log but don't fail if Neo4j delete fails
                System.err.println("Failed to delete change from Neo4j: " + e.getMessage());
            }
        }
        
        // Delete the change itself
        changeRepository.delete(change);
    }

    @Transactional(readOnly = true)
    public int reindexAllChanges() {
        if (changeSearchRepository == null) {
            System.err.println("⚠️ Elasticsearch repository not available, skipping reindex");
            return 0;
        }

        System.out.println("🔄 Starting reindex of all changes...");
        
        // Clear existing data in Elasticsearch
        changeSearchRepository.deleteAll();
        System.out.println("   Cleared existing Elasticsearch data");
        
        // Get all changes from MySQL
        List<Changes> allChanges = changeRepository.findAll();
        System.out.println("   Found " + allChanges.size() + " changes in MySQL");
        
        int count = 0;
        for (Changes change : allChanges) {
            try {
                ChangeSearchDocument doc = new ChangeSearchDocument();
                doc.setId(change.getId());
                doc.setTitle(change.getTitle());
                doc.setStage(change.getStage() != null ? change.getStage().toString() : null);
                doc.setChangeClass(change.getChangeClass());
                doc.setProduct(change.getProduct());
                doc.setStatus(change.getStatus() != null ? change.getStatus().toString() : null);
                doc.setCreator(change.getCreator());
                doc.setCreateTime(change.getCreateTime());
                doc.setChangeReason(change.getChangeReason());
                
                changeSearchRepository.save(doc);
                count++;
            } catch (Exception e) {
                System.err.println("   ⚠️ Failed to index change " + change.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("✅ Reindexed " + count + " changes successfully");
        return count;
    }

    private ChangeResponse mapToResponse(Changes change) {
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

        // Load Part IDs from the database
        List<String> partIds = changePartRepository.findByChangeId(change.getId())
            .stream()
            .map(changePart -> changePart.getPartId())
            .collect(Collectors.toList());
        response.setPartIds(partIds);

        return response;
    }
}

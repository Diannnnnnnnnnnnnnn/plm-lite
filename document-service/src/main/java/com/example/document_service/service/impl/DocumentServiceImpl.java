package com.example.document_service.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.document_service.dto.request.CreateDocumentRequest;
import com.example.document_service.dto.request.SubmitForReviewRequest;
import com.example.document_service.dto.request.UpdateDocumentRequest;
import com.example.document_service.exception.NotFoundException;
import com.example.document_service.exception.ValidationException;
import com.example.document_service.model.Document;
import com.example.document_service.model.DocumentHistory;
import com.example.document_service.model.DocumentMaster;
import com.example.document_service.repository.DocumentHistoryRepository;
import com.example.document_service.repository.DocumentMasterRepository;
import com.example.document_service.repository.DocumentRepository;
import com.example.document_service.service.DocumentService;
import com.example.document_service.service.gateway.Neo4jGateway;
import com.example.document_service.service.gateway.SearchGateway;
import com.example.document_service.service.gateway.WorkflowGateway;
import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentMasterRepository masterRepo;
    private final DocumentRepository docRepo;
    private final DocumentHistoryRepository historyRepo;
    private final SearchGateway searchGateway;
    private final WorkflowGateway workflowGateway;
    private final Neo4jGateway neo4jGateway;

    public DocumentServiceImpl(DocumentMasterRepository masterRepo,
                               DocumentRepository docRepo,
                               DocumentHistoryRepository historyRepo,
                               SearchGateway searchGateway,
                               WorkflowGateway workflowGateway,
                               Neo4jGateway neo4jGateway) {
        this.masterRepo = masterRepo;
        this.docRepo = docRepo;
        this.historyRepo = historyRepo;
        this.searchGateway = searchGateway;
        this.workflowGateway = workflowGateway;
        this.neo4jGateway = neo4jGateway;
    }

    private void logHistory(Document doc, String action, String oldVal, String newVal, String user, String comment) {
        DocumentHistory h = new DocumentHistory();
        h.setDocumentId(doc.getId());
        h.setAction(action);
        h.setOldValue(oldVal);
        h.setNewValue(newVal);
        h.setUser(user);
        h.setComment(comment);
        historyRepo.save(h);
    }

    private void sync(Document d) {
        // TODO: Re-enable when search-service and graph-service are available
        // Temporarily disabled for testing core document service functionality
        /*
        try {
            searchGateway.index(d);
            neo4jGateway.upsert(d);
        } catch (Exception e) {
            throw new DocumentServiceException("Failed to synchronize document with external services", e);
        }
        */
        System.out.println("INFO: External service sync disabled - document saved to database only");
    }

    @Override
    public List<Document> getAllDocuments() {
        // Only return active documents (current versions)
        return docRepo.findByIsActiveTrue();
    }

    @Override
    public Document getById(String documentId) {
        return docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    private void validateCreateRequest(CreateDocumentRequest req) {
        if (req.getMasterId() == null || req.getMasterId().trim().isEmpty()) {
            throw new ValidationException("Master ID is required");
        }
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (req.getCreator() == null || req.getCreator().trim().isEmpty()) {
            throw new ValidationException("Creator is required");
        }
        if (req.getStage() == null) {
            throw new ValidationException("Stage is required");
        }
    }

    @Transactional
    @Override
    public Document create(CreateDocumentRequest req) {
        validateCreateRequest(req);
        
        // Check if a document with this masterID already exists
        if (masterRepo.existsById(req.getMasterId())) {
            throw new ValidationException(
                "Master ID '" + req.getMasterId() + "' is already in use. " +
                "Please use a different Master ID or create a new version of the existing document."
            );
        }
        
        DocumentMaster master = new DocumentMaster();
        master.setId(req.getMasterId());
        master.setTitle(req.getTitle());
        master.setCreator(req.getCreator());
        master.setCategory(req.getCategory());
        master = masterRepo.save(master);

        Document d = new Document();
        d.setId(UUID.randomUUID().toString());
        d.setMaster(master);
        d.setTitle(req.getTitle());
        d.setDescription(req.getDescription());
        d.setCreator(req.getCreator());
        d.setStage(req.getStage());
        d.setStatus(Status.IN_WORK);
        d.setRevision(0);
        d.setVersion(1);
        d.setBomId(req.getBomId());  // Set related BOM ID

        d = docRepo.save(d);
        logHistory(d, "CREATED", null, d.getStatus().name(), req.getCreator(), "Initial creation");
        sync(d);
        return d;
    }

    @Transactional
    @Override
    public Document updateDocument(String documentId, UpdateDocumentRequest req) {
        Document currentDocument = getById(documentId);
        
        // CRITICAL BUSINESS RULE: RELEASED documents cannot be edited directly
        // They can only be edited through an approved change request
        if (currentDocument.getStatus() == Status.RELEASED) {
            throw new ValidationException(
                "Cannot edit RELEASED document directly. " +
                "Please create and approve a Change Request first, " +
                "then use the change-based editing workflow."
            );
        }

        boolean hasChanges = false;
        StringBuilder changeLog = new StringBuilder();

        // Check if there are actual changes
        if (req.getTitle() != null && !req.getTitle().trim().isEmpty() &&
            !req.getTitle().equals(currentDocument.getTitle())) {
            changeLog.append("Title changed from '").append(currentDocument.getTitle())
                     .append("' to '").append(req.getTitle()).append("'; ");
            hasChanges = true;
        }

        if (req.getStage() != null && !req.getStage().equals(currentDocument.getStage())) {
            changeLog.append("Stage changed from '").append(currentDocument.getStage())
                     .append("' to '").append(req.getStage()).append("'; ");
            hasChanges = true;
        }

        if (req.getStatus() != null && !req.getStatus().equals(currentDocument.getStatus())) {
            changeLog.append("Status changed from '").append(currentDocument.getStatus())
                     .append("' to '").append(req.getStatus()).append("'; ");
            hasChanges = true;
        }

        if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
            String currentDesc = currentDocument.getDescription() != null ? currentDocument.getDescription() : "";
            if (!req.getDescription().equals(currentDesc)) {
                changeLog.append("Description updated; ");
                hasChanges = true;
            }
        }

        if (hasChanges) {
            // CREATE A NEW DOCUMENT (SNAPSHOT) INSTEAD OF UPDATING
            Document newDocument = new Document();
            newDocument.setId(UUID.randomUUID().toString());
            newDocument.setMaster(currentDocument.getMaster());
            newDocument.setTitle(req.getTitle() != null ? req.getTitle() : currentDocument.getTitle());
            newDocument.setDescription(req.getDescription() != null ? req.getDescription() : currentDocument.getDescription());
            newDocument.setCreator(currentDocument.getCreator());
            newDocument.setCreateTime(LocalDateTime.now());
            newDocument.setStage(req.getStage() != null ? req.getStage() : currentDocument.getStage());
            // IMPORTANT: New versions always start as IN_WORK, regardless of previous status
            // Only the specific version that goes through review/release can have RELEASED status
            newDocument.setStatus(Status.IN_WORK);
            newDocument.setRevision(currentDocument.getRevision());
            newDocument.setVersion(currentDocument.getVersion() + 1);  // Increment version
            newDocument.setFileKey(currentDocument.getFileKey());  // Copy file key from current version
            newDocument.setBomId(currentDocument.getBomId());
            newDocument.setActive(true);  // New document is the active version
            
            // Mark the current document as inactive (archived version)
            currentDocument.setActive(false);
            docRepo.save(currentDocument);
            
            // Save the new document
            newDocument = docRepo.save(newDocument);
            
            changeLog.append("New version created: v").append(newDocument.getRevision())
                     .append(".").append(newDocument.getVersion());
            
            String user = req.getUser() != null ? req.getUser() : "System";
            String comment = changeLog.toString().trim();
            logHistory(newDocument, "UPDATED", null, newDocument.getStatus().name(), user, comment);
            sync(newDocument);
            
            return newDocument;
        }

        return currentDocument;
    }

    private void validateSubmitForReviewRequest(SubmitForReviewRequest req) {
        if (req.getUser() == null || req.getUser().trim().isEmpty()) {
            throw new ValidationException("User is required");
        }
        // Accept either legacy single-stage reviewers OR new two-stage reviewers
        boolean hasLegacyReviewers = req.getReviewerIds() != null && !req.getReviewerIds().isEmpty();
        boolean hasTwoStageReviewers = Boolean.TRUE.equals(req.getTwoStageReview())
                && req.getInitialReviewer() != null && !req.getInitialReviewer().trim().isEmpty()
                && req.getTechnicalReviewer() != null && !req.getTechnicalReviewer().trim().isEmpty();

        if (!hasLegacyReviewers && !hasTwoStageReviewers) {
            throw new ValidationException("At least one reviewer is required (either reviewerIds or two-stage reviewers)");
        }
    }

    private void validateStatus(Document doc, Status expectedStatus, String operation) {
        if (expectedStatus != doc.getStatus()) {
            throw new ValidationException("Cannot " + operation + " document. Expected status: " + expectedStatus + ", but was: " + doc.getStatus());
        }
    }

    @Transactional
    @Override
    public Document submitForReview(String documentId, SubmitForReviewRequest req) {
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new ValidationException("Document ID is required");
        }
        validateSubmitForReviewRequest(req);
        Document d = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        validateStatus(d, Status.IN_WORK, "submit for review");
        String oldStatus = d.getStatus().name();
        d.setStatus(Status.IN_REVIEW);
        d = docRepo.save(d);

        String newStatus = d.getStatus().name();
        
        // Determine log message based on review type
        String reviewInfo;
        if (Boolean.TRUE.equals(req.getTwoStageReview())) {
            reviewInfo = "Two-Stage Review: Initial=" + req.getInitialReviewer() + ", Technical=" + req.getTechnicalReviewer();
        } else {
            reviewInfo = "Reviewers=" + String.join(",", req.getReviewerIds());
        }
        logHistory(d, "SUBMIT_REVIEW", oldStatus, newStatus, req.getUser(), reviewInfo);

        sync(d);

        try {
            // NEW: Check if two-stage review or legacy review
            if (Boolean.TRUE.equals(req.getTwoStageReview()) && 
                req.getInitialReviewer() != null && 
                req.getTechnicalReviewer() != null) {
                // Two-stage review workflow
                workflowGateway.startTwoStageReviewProcess(
                        d.getId(),
                        d.getMaster() != null ? d.getMaster().getId() : null,
                        d.getFullVersion(),
                        d.getCreator(),
                        req.getInitialReviewer(),
                        req.getTechnicalReviewer()
                );
                System.out.println("INFO: Successfully started TWO-STAGE review workflow for document: " + d.getId());
            } else {
                // Legacy single-stage review workflow
                workflowGateway.startReviewProcess(
                        d.getId(),
                        d.getMaster() != null ? d.getMaster().getId() : null,
                        d.getFullVersion(),
                        d.getCreator(),
                        req.getReviewerIds()
                );
                System.out.println("INFO: Successfully started review workflow for document: " + d.getId());
            }
        } catch (Exception e) {
            System.out.println("WARN: Failed to start review workflow, but document status updated: " + e.getMessage());
            // Don't throw exception - allow the submit to continue even if workflow fails
        }

        return d;
    }

    private void validateCompleteReviewRequest(String documentId, String approver) {
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new ValidationException("Document ID is required");
        }
        if (approver == null || approver.trim().isEmpty()) {
            throw new ValidationException("Approver is required");
        }
    }

    @Transactional
    @Override
    public Document completeReview(String documentId, boolean approved, String approver, String comment) {
        validateCompleteReviewRequest(documentId, approver);
        Document currentDocument = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        validateStatus(currentDocument, Status.IN_REVIEW, "complete review");
        String oldStatus = currentDocument.getStatus().name();

        Document resultDocument;
        
        if (approved) {
            // CREATE A NEW DOCUMENT (SNAPSHOT) FOR THE RELEASED VERSION
            Document releasedDocument = new Document();
            releasedDocument.setId(UUID.randomUUID().toString());
            releasedDocument.setMaster(currentDocument.getMaster());
            releasedDocument.setTitle(currentDocument.getTitle());
            releasedDocument.setDescription(currentDocument.getDescription());
            releasedDocument.setCreator(currentDocument.getCreator());
            releasedDocument.setCreateTime(LocalDateTime.now());
            releasedDocument.setStage(currentDocument.getStage());
            releasedDocument.setStatus(Status.RELEASED);
            releasedDocument.setRevision(currentDocument.getRevision() + 1);  // Increment revision
            releasedDocument.setVersion(0);  // Reset version to 0
            releasedDocument.setFileKey(currentDocument.getFileKey());
            releasedDocument.setBomId(currentDocument.getBomId());
            releasedDocument.setActive(true);  // New released document is active
            
            // Mark the current document as inactive
            currentDocument.setActive(false);
            docRepo.save(currentDocument);
            
            // Save the new released document
            releasedDocument = docRepo.save(releasedDocument);
            logHistory(releasedDocument, "RELEASED", oldStatus, releasedDocument.getStatus().name(), approver, comment);
            sync(releasedDocument);
            
            resultDocument = releasedDocument;
        } else {
            // If rejected, just update status (no need for snapshot)
            currentDocument.setStatus(Status.IN_WORK);
            currentDocument = docRepo.save(currentDocument);
            logHistory(currentDocument, "REJECTED", oldStatus, currentDocument.getStatus().name(), approver, comment);
            sync(currentDocument);
            
            resultDocument = currentDocument;
        }

        try {
            workflowGateway.notifyApprovalResult(resultDocument.getId(), approved, approver, comment);
            System.out.println("INFO: Successfully notified workflow of approval result for document: " + resultDocument.getId());
        } catch (Exception e) {
            System.out.println("WARN: Failed to notify workflow of approval result: " + e.getMessage());
            // Don't throw exception - allow the approval to continue even if workflow notification fails
        }
        return resultDocument;
    }

    @Transactional
    @Override
    public Document revise(String documentId, String user) {
        Document current = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        // Mark the current document as inactive
        current.setActive(false);
        docRepo.save(current);

        // Create new revision
        Document d = new Document();
        d.setId(UUID.randomUUID().toString());
        d.setMaster(current.getMaster());
        d.setTitle(current.getTitle());
        d.setDescription(current.getDescription());
        d.setCreator(user);
        d.setStage(current.getStage());
        d.setStatus(Status.IN_WORK);
        d.setRevision(current.getRevision());
        d.setVersion(current.getVersion() + 1);
        d.setFileKey(current.getFileKey());
        d.setBomId(current.getBomId());
        d.setActive(true);  // New revision is active

        d = docRepo.save(d);
        logHistory(d, "REVISED", current.getFullVersion(), d.getFullVersion(), user, "Minor version increment");
        sync(d);
        return d;
    }

    @Transactional
    @Override
    public Document initiateChangeBasedEdit(String documentId, String changeId, String user) {
        Document releasedDocument = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        // Validate that the document is RELEASED
        if (releasedDocument.getStatus() != Status.RELEASED) {
            throw new ValidationException(
                "Document must be in RELEASED status to initiate change-based editing. " +
                "Current status: " + releasedDocument.getStatus()
            );
        }

        // Mark the released document as inactive
        releasedDocument.setActive(false);
        docRepo.save(releasedDocument);

        // Create new editable version
        Document newVersion = new Document();
        newVersion.setId(UUID.randomUUID().toString());
        newVersion.setMaster(releasedDocument.getMaster());
        newVersion.setTitle(releasedDocument.getTitle());
        newVersion.setDescription(releasedDocument.getDescription());
        newVersion.setCreator(releasedDocument.getCreator()); // Keep original creator
        newVersion.setCreateTime(LocalDateTime.now());
        newVersion.setStage(releasedDocument.getStage());
        newVersion.setStatus(Status.IN_WORK); // New version starts as IN_WORK
        newVersion.setRevision(releasedDocument.getRevision());
        newVersion.setVersion(releasedDocument.getVersion() + 1); // Increment version
        newVersion.setFileKey(releasedDocument.getFileKey()); // Copy file key
        newVersion.setBomId(releasedDocument.getBomId());
        newVersion.setActive(true); // New version is active

        newVersion = docRepo.save(newVersion);

        // Log the change-based edit initiation
        String comment = String.format(
            "Change-based editing initiated via Change Request #%s. " +
            "Document transitioned from RELEASED %s to IN_WORK %s",
            changeId,
            releasedDocument.getFullVersion(),
            newVersion.getFullVersion()
        );
        logHistory(newVersion, "CHANGE_INITIATED", 
            releasedDocument.getStatus().name(), 
            newVersion.getStatus().name(), 
            user, 
            comment);
        
        sync(newVersion);
        
        System.out.println(String.format(
            "INFO: Change-based editing initiated for document %s (Change #%s). " +
            "New editable version %s created.",
            documentId, changeId, newVersion.getId()
        ));
        
        return newVersion;
    }

    @Transactional
    @Override
    public Document updateStage(String documentId, Stage stage, String user, String comment) {
        Document d = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        String oldStage = d.getStage().name();
        d.setStage(stage);
        d = docRepo.save(d);

        logHistory(d, "STAGE_UPDATED", oldStage, stage.name(), user, comment);
        sync(d);
        return d;
    }

    @Override
    public List<DocumentHistory> history(String documentId) {
        return historyRepo.findByDocumentIdOrderByTimestampDesc(documentId);
    }

    @Override
    public List<Document> getDocumentVersions(String documentId) {
        Document document = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        
        // Get all documents with the same master ID, sorted by revision and version (descending - newest first)
        if (document.getMaster() != null) {
            return docRepo.findByMaster_IdOrderByRevisionDescVersionDesc(document.getMaster().getId());
        }
        
        // If no master, return just this document
        return List.of(document);
    }

    @Transactional
    @Override
    public void attachFileKey(String documentId, String fileKey, String user) {
        Document d = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        String oldKey = d.getFileKey();
        d.setFileKey(fileKey);
        docRepo.save(d);
        logHistory(d, "FILE_ATTACHED", oldKey, fileKey, user, "File stored via file-storage service");
    }

    @Override
    @Transactional
    public void deleteDocument(String documentId) {
        Document document = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        // Delete all history records for this document
        List<DocumentHistory> historyList = historyRepo.findByDocumentIdOrderByTimestampDesc(documentId);
        historyRepo.deleteAll(historyList);

        // Delete the document
        docRepo.delete(document);
    }

    @Override
    public List<Document> getDocumentsByBomId(String bomId) {
        return docRepo.findByBomId(bomId);
    }
}

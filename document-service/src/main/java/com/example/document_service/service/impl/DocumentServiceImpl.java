package com.example.document_service.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.document_service.dto.request.CreateDocumentRequest;
import com.example.document_service.dto.request.SubmitForReviewRequest;
import com.example.document_service.dto.request.UpdateDocumentRequest;
import com.example.plm.common.model.Status;
import com.example.plm.common.model.Stage;
import com.example.document_service.exception.DocumentServiceException;
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
        return docRepo.findAll();
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
        DocumentMaster master = masterRepo.findById(req.getMasterId()).orElseGet(() -> {
            DocumentMaster m = new DocumentMaster();
            m.setId(req.getMasterId());
            m.setTitle(req.getTitle());
            m.setCreator(req.getCreator());
            m.setCategory(req.getCategory());
            return masterRepo.save(m);
        });

        Document d = new Document();
        d.setId(UUID.randomUUID().toString());
        d.setMaster(master);
        d.setTitle(req.getTitle());
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
        Document document = getById(documentId);

        boolean hasChanges = false;
        StringBuilder changeLog = new StringBuilder();

        // Update title if provided
        if (req.getTitle() != null && !req.getTitle().trim().isEmpty() &&
            !req.getTitle().equals(document.getTitle())) {
            changeLog.append("Title changed from '").append(document.getTitle())
                     .append("' to '").append(req.getTitle()).append("'; ");
            document.setTitle(req.getTitle());
            hasChanges = true;
        }

        // Update stage if provided
        if (req.getStage() != null && !req.getStage().equals(document.getStage())) {
            changeLog.append("Stage changed from '").append(document.getStage())
                     .append("' to '").append(req.getStage()).append("'; ");
            document.setStage(req.getStage());
            hasChanges = true;
        }

        // Update status if provided
        if (req.getStatus() != null && !req.getStatus().equals(document.getStatus())) {
            changeLog.append("Status changed from '").append(document.getStatus())
                     .append("' to '").append(req.getStatus()).append("'; ");
            document.setStatus(req.getStatus());
            hasChanges = true;
        }

        if (hasChanges) {
            document = docRepo.save(document);
            String user = req.getUser() != null ? req.getUser() : "System";
            String comment = req.getDescription() != null ? req.getDescription() : changeLog.toString().trim();
            logHistory(document, "UPDATED", null, document.getStatus().name(), user, comment);
            sync(document);
        }

        return document;
    }

    private void validateSubmitForReviewRequest(SubmitForReviewRequest req) {
        if (req.getUser() == null || req.getUser().trim().isEmpty()) {
            throw new ValidationException("User is required");
        }
        if (req.getReviewerIds() == null || req.getReviewerIds().isEmpty()) {
            throw new ValidationException("At least one reviewer is required");
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
        logHistory(d, "SUBMIT_REVIEW", oldStatus, newStatus, req.getUser(),
                "Reviewers=" + String.join(",", req.getReviewerIds()));

        sync(d);

        try {
            workflowGateway.startReviewProcess(
                    d.getId(),
                    d.getMaster() != null ? d.getMaster().getId() : null,
                    d.getFullVersion(),
                    d.getCreator(),
                    req.getReviewerIds()
            );
            System.out.println("INFO: Successfully started review workflow for document: " + d.getId());
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
        Document d = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        validateStatus(d, Status.IN_REVIEW, "complete review");
        String oldStatus = d.getStatus().name();

        if (approved) {
            d.setStatus(Status.RELEASED);
            d.setRevision(d.getRevision() + 1);
            d.setVersion(0);
            logHistory(d, "RELEASED", oldStatus, d.getStatus().name(), approver, comment);
        } else {
            d.setStatus(Status.IN_WORK);
            logHistory(d, "REJECTED", oldStatus, d.getStatus().name(), approver, comment);
        }

        d = docRepo.save(d);
        sync(d);
        try {
            workflowGateway.notifyApprovalResult(d.getId(), approved, approver, comment);
            System.out.println("INFO: Successfully notified workflow of approval result for document: " + d.getId());
        } catch (Exception e) {
            System.out.println("WARN: Failed to notify workflow of approval result: " + e.getMessage());
            // Don't throw exception - allow the approval to continue even if workflow notification fails
        }
        return d;
    }

    @Transactional
    @Override
    public Document revise(String documentId, String user) {
        Document current = docRepo.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        Document d = new Document();
        d.setId(UUID.randomUUID().toString());
        d.setMaster(current.getMaster());
        d.setTitle(current.getTitle());
        d.setCreator(user);
        d.setStage(current.getStage());
        d.setStatus(Status.IN_WORK);
        d.setRevision(current.getRevision());
        d.setVersion(current.getVersion() + 1);
        d.setFileKey(current.getFileKey());

        d = docRepo.save(d);
        logHistory(d, "REVISED", current.getFullVersion(), d.getFullVersion(), user, "Minor version increment");
        sync(d);
        return d;
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

package com.example.plm.change.service;

import com.example.plm.change.dto.CreateChangeRequest;
import com.example.plm.change.dto.ChangeResponse;
import com.example.plm.change.model.*;
import com.example.plm.change.repository.elasticsearch.ChangeSearchRepository;
import com.example.plm.change.repository.mysql.ChangeRepository;
import com.example.plm.change.repository.mysql.ChangeDocumentRepository;
import com.example.plm.change.repository.mysql.ChangePartRepository;
import com.example.plm.change.repository.neo4j.ChangeNodeRepository;
import com.example.plm.common.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;
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

    @Autowired
    private ChangeNodeRepository changeNodeRepository;

    @Autowired
    private ChangeSearchRepository changeSearchRepository;

    @Autowired
    private DocumentServiceClient documentServiceClient;

    @FeignClient(name = "document-service")
    public interface DocumentServiceClient {
        @GetMapping("/api/documents/{id}")
        DocumentInfo getDocument(@PathVariable String id);

        @PutMapping("/api/documents/{id}/status")
        void updateDocumentStatus(@PathVariable String id, @RequestBody DocumentStatusUpdateRequest request);
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

    public static class DocumentStatusUpdateRequest {
        private Status status;

        public DocumentStatusUpdateRequest() {}
        public DocumentStatusUpdateRequest(Status status) { this.status = status; }

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    @Transactional
    public ChangeResponse createChange(CreateChangeRequest request) {
        DocumentInfo document = documentServiceClient.getDocument(request.getChangeDocument());
        if (document.getStatus() != Status.RELEASED) {
            throw new IllegalStateException("Change can only be applied to released documents");
        }

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

        ChangeNode changeNode = new ChangeNode(
            changeId,
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
        changeNodeRepository.createDocumentRelationship(changeId, request.getChangeDocument());

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

        return mapToResponse(change);
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

        ChangeNode changeNode = changeNodeRepository.findById(changeId).orElse(null);
        if (changeNode != null) {
            changeNode.setStatus(Status.IN_REVIEW.toString());
            changeNodeRepository.save(changeNode);
        }

        Optional<ChangeSearchDocument> searchDoc = changeSearchRepository.findById(changeId);
        if (searchDoc.isPresent()) {
            ChangeSearchDocument doc = searchDoc.get();
            doc.setStatus(Status.IN_REVIEW.toString());
            changeSearchRepository.save(doc);
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

        documentServiceClient.updateDocumentStatus(
            change.getChangeDocument(),
            new DocumentStatusUpdateRequest(Status.IN_WORK)
        );

        ChangeNode changeNode = changeNodeRepository.findById(changeId).orElse(null);
        if (changeNode != null) {
            changeNode.setStatus(Status.RELEASED.toString());
            changeNodeRepository.save(changeNode);
        }

        Optional<ChangeSearchDocument> searchDoc = changeSearchRepository.findById(changeId);
        if (searchDoc.isPresent()) {
            ChangeSearchDocument doc = searchDoc.get();
            doc.setStatus(Status.RELEASED.toString());
            changeSearchRepository.save(doc);
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
        return changeSearchRepository.findByTitleContaining(query);
    }

    private ChangeResponse mapToResponse(Change change) {
        return new ChangeResponse(
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
    }
}
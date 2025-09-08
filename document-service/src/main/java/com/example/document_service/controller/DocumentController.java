package com.example.document_service.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.plm.common.model.Stage;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.document_service.dto.request.ApproveRejectRequest;
import com.example.document_service.dto.request.CreateDocumentRequest;
import com.example.document_service.dto.request.SearchRequest;
import com.example.document_service.dto.request.SubmitForReviewRequest;
import com.example.document_service.dto.request.UpdateStageRequest;
import com.example.document_service.dto.response.DocumentHistoryResponse;
import com.example.document_service.dto.response.DocumentResponse;
import com.example.document_service.mapper.DocumentHistoryMapper;
import com.example.document_service.mapper.DocumentMapper;
import com.example.document_service.model.Document;
import com.example.document_service.model.DocumentHistory;

import com.example.document_service.service.DocumentService;
import com.example.document_service.service.gateway.FileStorageGateway;
import com.example.document_service.service.gateway.SearchGateway;
import com.example.document_service.client.SearchServiceClient;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageGateway fileStorageGateway;
    private final SearchGateway searchGateway;

    public DocumentController(DocumentService documentService,
                              FileStorageGateway fileStorageGateway,
                              SearchGateway searchGateway) {
        if (documentService == null) {
            throw new IllegalArgumentException("DocumentService cannot be null");
        }
        this.documentService = documentService;
        this.fileStorageGateway = fileStorageGateway;
        this.searchGateway = searchGateway;
    }

    @PostMapping
    public DocumentResponse create(@RequestBody CreateDocumentRequest req) {
        Document d = documentService.create(req);
        return DocumentMapper.toResponse(d);
    }

    @PostMapping("/{id}/submit-review")
    public DocumentResponse submitForReview(@PathVariable String id,
                                            @RequestBody SubmitForReviewRequest req) {
        Document d = documentService.submitForReview(id, req);
        return DocumentMapper.toResponse(d);
    }

    @PostMapping("/{id}/review-complete")
    public DocumentResponse complete(@PathVariable String id,
                                     @RequestBody ApproveRejectRequest req) {
        Document d = documentService.completeReview(
                id,
                Boolean.TRUE.equals(req.getApproved()),
                req.getUser(),
                req.getComment()
        );
        return DocumentMapper.toResponse(d);
    }

    @PostMapping("/{id}/revise")
    public DocumentResponse revise(@PathVariable String id,
                                   @RequestParam String user) {
        Document d = documentService.revise(id, user);
        return DocumentMapper.toResponse(d);
    }

    @PostMapping("/{id}/stage")
    public DocumentResponse updateStage(@PathVariable String id,
                                        @RequestBody UpdateStageRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        Stage stage = req.getStage();
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }
        
        Document d = documentService.updateStage(
            id,
            stage,
            req.getUser(),
            req.getComment()
        );
        return DocumentMapper.toResponse(d);
    }

    @GetMapping("/{id}/history")
    public List<DocumentHistoryResponse> history(@PathVariable String id) {
        List<DocumentHistory> list = documentService.history(id);
        return list.stream()
                   .map(DocumentHistoryMapper::toResponse)
                   .collect(Collectors.toList());
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@PathVariable String id,
                         @RequestPart MultipartFile file,
                         @RequestParam String user) {
        String objectKey = fileStorageGateway.upload(id, file);
        documentService.attachFileKey(id, objectKey, user);
        return objectKey;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] data = fileStorageGateway.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @PostMapping("/search")
    public Object search(@RequestBody SearchRequest req) {
        List<SearchServiceClient.DocumentEsDto> searchResult = searchGateway.search(
                req.getQ(),
                req.getStage(),
                req.getStatus(),
                req.getCategory()
        );
        
        return searchResult;
    }
}

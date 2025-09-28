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
import org.springframework.web.bind.annotation.PutMapping;
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
import com.example.document_service.dto.request.UpdateDocumentRequest;
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
@RequestMapping("/api/v1/documents")
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

    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return documents.stream()
                        .map(DocumentMapper::toResponse)
                        .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DocumentResponse getById(@PathVariable String id) {
        Document d = documentService.getById(id);
        return DocumentMapper.toResponse(d);
    }

    @PostMapping
    public DocumentResponse create(@RequestBody CreateDocumentRequest req) {
        Document d = documentService.create(req);
        return DocumentMapper.toResponse(d);
    }

    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable String id,
                                   @RequestBody UpdateDocumentRequest req) {
        Document d = documentService.updateDocument(id, req);
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
        Document document = documentService.getById(id);
        if (document.getFileKey() == null || document.getFileKey().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = fileStorageGateway.download(document.getFileKey());

        // Extract original filename from fileKey (format: documentId_originalFilename)
        String filename;
        if (document.getFileKey() != null && document.getFileKey().contains("_")) {
            // Extract the part after the first underscore (original filename with extension)
            filename = document.getFileKey().substring(document.getFileKey().indexOf("_") + 1);
        } else {
            // Fallback to document title or ID if fileKey format is unexpected
            filename = document.getTitle() != null ? document.getTitle() : id;
        }

        // Handle Unicode characters in filename by URL encoding
        String encodedFilename;
        try {
            encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8")
                    .replaceAll("\\+", "%20"); // Replace + with %20 for better compatibility
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to ASCII-safe filename
            encodedFilename = "document_" + id + ".bin";
        }

        // Determine the correct content type based on file extension
        MediaType contentType = getContentTypeByFilename(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(contentType)
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

    private MediaType getContentTypeByFilename(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        String lowercaseFilename = filename.toLowerCase();

        // Microsoft Office documents
        if (lowercaseFilename.endsWith(".docx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (lowercaseFilename.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        } else if (lowercaseFilename.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (lowercaseFilename.endsWith(".xls")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        } else if (lowercaseFilename.endsWith(".pptx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        } else if (lowercaseFilename.endsWith(".ppt")) {
            return MediaType.parseMediaType("application/vnd.ms-powerpoint");
        }

        // PDF
        else if (lowercaseFilename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }

        // Images
        else if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowercaseFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowercaseFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }

        // Text files
        else if (lowercaseFilename.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN;
        } else if (lowercaseFilename.endsWith(".csv")) {
            return MediaType.parseMediaType("text/csv");
        }

        // Archives
        else if (lowercaseFilename.endsWith(".zip")) {
            return MediaType.parseMediaType("application/zip");
        } else if (lowercaseFilename.endsWith(".rar")) {
            return MediaType.parseMediaType("application/vnd.rar");
        }

        // Default to octet-stream for unknown types
        else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}

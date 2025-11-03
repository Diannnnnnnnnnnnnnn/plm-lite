package com.example.document_service.controller;

import com.example.document_service.elasticsearch.DocumentSearchDocument;
import com.example.document_service.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Elasticsearch document search operations
 */
@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DocumentSearchController {
    
    private final DocumentSearchService searchService;
    
    /**
     * Search documents using Elasticsearch
     * GET /api/v1/documents/search/elastic?q=motor
     */
    @GetMapping("/search/elastic")
    public List<DocumentSearchDocument> searchDocuments(@RequestParam String q) {
        return searchService.search(q);
    }
    
    /**
     * Search documents by creator
     * GET /api/v1/documents/search/elastic/by-creator?creator=John
     */
    @GetMapping("/search/elastic/by-creator")
    public List<DocumentSearchDocument> searchByCreator(@RequestParam String creator) {
        return searchService.searchByCreator(creator);
    }
    
    /**
     * Search documents by status
     * GET /api/v1/documents/search/elastic/by-status?status=APPROVED
     */
    @GetMapping("/search/elastic/by-status")
    public List<DocumentSearchDocument> searchByStatus(@RequestParam String status) {
        return searchService.searchByStatus(status);
    }
}




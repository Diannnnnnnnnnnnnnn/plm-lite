package com.example.document_service.service;

import com.example.document_service.elasticsearch.DocumentSearchDocument;
import com.example.document_service.elasticsearch.DocumentSearchRepository;
import com.example.document_service.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing document indexing and searching in Elasticsearch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchService {
    
    private final DocumentSearchRepository searchRepository;
    
    /**
     * Index a document to Elasticsearch
     * Called automatically when document is created or updated
     */
    public void indexDocument(Document document) {
        try {
            DocumentSearchDocument searchDoc = DocumentSearchDocument.fromDocument(document);
            searchRepository.save(searchDoc);
            log.info("✅ Document {} indexed to Elasticsearch", document.getId());
        } catch (Exception e) {
            log.error("❌ Failed to index document {}: {}", document.getId(), e.getMessage());
        }
    }
    
    /**
     * Delete document from Elasticsearch
     * Called automatically when document is deleted
     */
    public void deleteDocument(String documentId) {
        try {
            searchRepository.deleteById(documentId);
            log.info("✅ Document {} removed from Elasticsearch", documentId);
        } catch (Exception e) {
            log.error("❌ Failed to delete document {} from ES: {}", documentId, e.getMessage());
        }
    }
    
    /**
     * Search documents by query string
     */
    public List<DocumentSearchDocument> search(String query) {
        try {
            return searchRepository.findByTitleContaining(query);
        } catch (Exception e) {
            log.error("❌ Search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Search documents by creator
     */
    public List<DocumentSearchDocument> searchByCreator(String creator) {
        return searchRepository.findByCreator(creator);
    }
    
    /**
     * Search documents by status
     */
    public List<DocumentSearchDocument> searchByStatus(String status) {
        return searchRepository.findByStatus(status);
    }
}




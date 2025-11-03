package com.example.document_service.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for document search
 * Provides CRUD operations and custom query methods for the documents index
 */
@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchDocument, String> {
    
    /**
     * Find documents by title containing the search term
     */
    List<DocumentSearchDocument> findByTitleContaining(String title);
    
    /**
     * Find documents by creator
     */
    List<DocumentSearchDocument> findByCreator(String creator);
    
    /**
     * Find documents by status
     */
    List<DocumentSearchDocument> findByStatus(String status);
    
    /**
     * Find documents by category
     */
    List<DocumentSearchDocument> findByCategory(String category);
    
    /**
     * Find documents by stage and status
     */
    List<DocumentSearchDocument> findByStageAndStatus(String stage, String status);
    
    /**
     * Find active documents
     */
    List<DocumentSearchDocument> findByIsActiveTrue();
}




package com.example.task_service.elasticsearch;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

// Elasticsearch Repository for Task Document
@Repository
public interface TaskSearchRepository extends ElasticsearchRepository<TaskDocument, String> {

    // Define custom query methods (if any) here, e.g., search by title or description
    List<TaskDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}

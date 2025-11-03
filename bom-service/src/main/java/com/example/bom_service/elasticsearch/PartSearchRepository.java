package com.example.bom_service.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartSearchRepository extends ElasticsearchRepository<PartSearchDocument, String> {
    List<PartSearchDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
    List<PartSearchDocument> findByCreator(String creator);
}




package com.example.bom_service.service;

import com.example.bom_service.elasticsearch.PartSearchDocument;
import com.example.bom_service.elasticsearch.PartSearchRepository;
import com.example.bom_service.model.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartSearchService {

    private final PartSearchRepository partSearchRepository;

    public void indexPart(Part part) {
        try {
            PartSearchDocument searchDocument = PartSearchDocument.fromPart(part);
            partSearchRepository.save(searchDocument);
            log.info("✅ Part {} indexed to Elasticsearch", part.getId());
        } catch (Exception e) {
            log.error("❌ Failed to index Part {} to Elasticsearch: {}", part.getId(), e.getMessage());
            // Don't throw - ES indexing shouldn't break the main flow
        }
    }

    public void deletePart(String partId) {
        try {
            partSearchRepository.deleteById(partId);
            log.info("✅ Part {} deleted from Elasticsearch", partId);
        } catch (Exception e) {
            log.error("❌ Failed to delete Part {} from Elasticsearch: {}", partId, e.getMessage());
        }
    }
}




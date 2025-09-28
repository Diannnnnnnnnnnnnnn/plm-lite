package com.example.plm.change.repository.elasticsearch;

import com.example.plm.change.model.ChangeSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChangeSearchRepository extends ElasticsearchRepository<ChangeSearchDocument, String> {

    List<ChangeSearchDocument> findByTitleContaining(String title);

    List<ChangeSearchDocument> findByChangeReasonContaining(String reason);

    List<ChangeSearchDocument> findByStatus(String status);

    List<ChangeSearchDocument> findByCreator(String creator);

    List<ChangeSearchDocument> findByProduct(String product);

    List<ChangeSearchDocument> findByStage(String stage);

    List<ChangeSearchDocument> findByCreateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<ChangeSearchDocument> findByAffectedDocumentIdsContaining(String documentId);

    List<ChangeSearchDocument> findByAffectedPartIdsContaining(String partId);
}
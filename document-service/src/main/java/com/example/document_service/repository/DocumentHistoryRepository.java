package com.example.document_service.repository;

import com.example.document_service.model.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {
    List<DocumentHistory> findByDocumentIdOrderByTimestampDesc(String documentId);
}

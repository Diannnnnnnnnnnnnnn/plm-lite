package com.example.document_service.repository;

import com.example.document_service.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByPartId(String partId);
    List<Document> findByMaster_IdOrderByRevisionDescVersionDesc(String masterId);
    List<Document> findByIsActiveTrue();
    Document findByMaster_IdAndIsActiveTrue(String masterId);
}

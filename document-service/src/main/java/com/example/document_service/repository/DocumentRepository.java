package com.example.document_service.repository;

import com.example.document_service.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByBomId(String bomId);
}

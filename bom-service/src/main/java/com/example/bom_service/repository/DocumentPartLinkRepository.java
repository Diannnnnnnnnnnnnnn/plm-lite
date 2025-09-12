package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bom_service.model.DocumentPartLink;

import java.util.List;

public interface DocumentPartLinkRepository extends JpaRepository<DocumentPartLink, String> {
    
    List<DocumentPartLink> findByPartId(String partId);
    
    List<DocumentPartLink> findByDocumentId(String documentId);
    
    void deleteByPartIdAndDocumentId(String partId, String documentId);
}

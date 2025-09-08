package com.example.document_service.repository;

import com.example.document_service.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface DocumentRepository extends JpaRepository<Document, String> {}

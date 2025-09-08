package com.example.document_service.repository;

import com.example.document_service.model.DocumentMaster;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface DocumentMasterRepository extends JpaRepository<DocumentMaster, String> {}

package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bom_service.model.BomHeader;

import java.util.List;

public interface BomHeaderRepository extends JpaRepository<BomHeader, String> {
    List<BomHeader> findByDocumentId(String documentId);
}

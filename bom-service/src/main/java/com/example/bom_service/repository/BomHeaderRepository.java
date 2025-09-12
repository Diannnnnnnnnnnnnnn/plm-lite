package com.example.bom_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.bom_service.model.BomHeader;

public interface BomHeaderRepository extends JpaRepository<BomHeader, String> {
    
    @Query("SELECT b FROM BomHeader b WHERE b.documentId = ?1 AND b.deleted = false")
    List<BomHeader> findByDocumentId(String documentId);
    
    @Query("SELECT b FROM BomHeader b WHERE b.deleted = false")
    List<BomHeader> findAllActive();
    
    @Query("SELECT b FROM BomHeader b WHERE b.id = ?1 AND b.deleted = false")
    Optional<BomHeader> findByIdActive(String id);
    
    List<BomHeader> findByDeletedTrueAndDeleteTimeBefore(LocalDateTime time);
}

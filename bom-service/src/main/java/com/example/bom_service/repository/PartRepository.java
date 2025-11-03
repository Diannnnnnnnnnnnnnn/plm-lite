package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.bom_service.model.Part;
import com.example.plm.common.model.Stage;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, String> {
    
    @Query("SELECT p FROM Part p WHERE p.creator = ?1 AND p.deleted = false")
    List<Part> findByCreator(String creator);
    
    @Query("SELECT p FROM Part p WHERE p.stage = ?1 AND p.deleted = false")
    List<Part> findByStage(Stage stage);
    
    @Query("SELECT p FROM Part p WHERE p.title LIKE %?1% AND p.deleted = false")
    List<Part> findByTitleContaining(String title);
    
    @Query("SELECT p FROM Part p WHERE p.level = ?1 AND p.deleted = false")
    List<Part> findByLevel(String level);
    
    @Query("SELECT DISTINCT cu.child FROM PartUsage cu WHERE cu.parent.id = ?1 AND cu.child.deleted = false")
    List<Part> findChildrenOf(String parentId);
    
    @Query("SELECT DISTINCT pu.parent FROM PartUsage pu WHERE pu.child.id = ?1 AND pu.parent.deleted = false")
    List<Part> findParentsOf(String childId);
}

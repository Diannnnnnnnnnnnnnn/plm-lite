package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.bom_service.model.Part;
import com.example.plm.common.model.Stage;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, String> {
    
    List<Part> findByCreator(String creator);
    
    List<Part> findByStage(Stage stage);
    
    @Query("SELECT p FROM Part p WHERE p.title LIKE %?1%")
    List<Part> findByTitleContaining(String title);
    
    @Query("SELECT p FROM Part p WHERE p.level = ?1")
    List<Part> findByLevel(String level);
    
    @Query("SELECT DISTINCT p FROM Part p JOIN p.childUsages cu WHERE cu.parent.id = ?1")
    List<Part> findChildrenOf(String parentId);
    
    @Query("SELECT DISTINCT p FROM Part p JOIN p.parentUsages pu WHERE pu.child.id = ?1")
    List<Part> findParentsOf(String childId);
}

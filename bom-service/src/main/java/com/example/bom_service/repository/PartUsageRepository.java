package com.example.bom_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.bom_service.model.PartUsage;

import java.util.List;

public interface PartUsageRepository extends JpaRepository<PartUsage, String> {
    
    @Query("SELECT pu FROM PartUsage pu WHERE pu.parent.id = ?1 AND pu.parent.deleted = false AND pu.child.deleted = false")
    List<PartUsage> findByParentId(String parentId);
    
    @Query("SELECT pu FROM PartUsage pu WHERE pu.child.id = ?1 AND pu.parent.deleted = false AND pu.child.deleted = false")
    List<PartUsage> findByChildId(String childId);
    
    @Query("SELECT pu FROM PartUsage pu WHERE pu.parent.id = ?1 AND pu.child.id = ?2")
    PartUsage findByParentAndChild(String parentId, String childId);
    
    @Query("SELECT pu FROM PartUsage pu WHERE pu.parent.id = ?1 AND pu.child.deleted = false ORDER BY pu.child.title")
    List<PartUsage> findByParentIdOrderByChildTitle(String parentId);
}

package com.example.plm.change.repository.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.plm.change.model.ChangeBom;

@Repository
public interface ChangeBomRepository extends JpaRepository<ChangeBom, String> {
    // Use custom query to find by change ID through the relationship
    @Query("SELECT cb FROM ChangeBom cb WHERE cb.change.id = :changeId")
    List<ChangeBom> findByChangeId(@Param("changeId") String changeId);
    
    List<ChangeBom> findByBomId(String bomId);
}

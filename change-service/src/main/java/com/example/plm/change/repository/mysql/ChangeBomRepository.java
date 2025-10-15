package com.example.plm.change.repository.mysql;

import com.example.plm.change.model.ChangeBom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeBomRepository extends JpaRepository<ChangeBom, String> {
    List<ChangeBom> findByChangeId(String changeId);
    List<ChangeBom> findByBomId(String bomId);
}

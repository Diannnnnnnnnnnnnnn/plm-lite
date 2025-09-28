package com.example.plm.change.repository.mysql;

import com.example.plm.change.model.ChangePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangePartRepository extends JpaRepository<ChangePart, String> {

    List<ChangePart> findByChangeId(String changeId);

    List<ChangePart> findByPartId(String partId);
}
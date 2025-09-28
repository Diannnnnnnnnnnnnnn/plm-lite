package com.example.plm.change.repository.mysql;

import com.example.plm.change.model.ChangeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeDocumentRepository extends JpaRepository<ChangeDocument, String> {

    List<ChangeDocument> findByChangeId(String changeId);

    List<ChangeDocument> findByDocumentId(String documentId);
}
package com.example.change_service.repository.mysql;

import com.example.change_service.model.ChangeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeDocumentRepository extends JpaRepository<ChangeDocument, String> {

    List<ChangeDocument> findByChangeId(String changeId);

    List<ChangeDocument> findByDocumentId(String documentId);
}


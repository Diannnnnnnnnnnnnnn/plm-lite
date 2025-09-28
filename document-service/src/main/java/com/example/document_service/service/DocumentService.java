package com.example.document_service.service;

import java.util.List;

import com.example.document_service.dto.request.CreateDocumentRequest;
import com.example.document_service.dto.request.SubmitForReviewRequest;
import com.example.document_service.dto.request.UpdateDocumentRequest;
import com.example.document_service.model.Document;
import com.example.document_service.model.DocumentHistory;
import com.example.plm.common.model.Stage;

public interface DocumentService {
    List<Document> getAllDocuments();
    Document getById(String documentId);
    Document create(CreateDocumentRequest req);
    Document updateDocument(String documentId, UpdateDocumentRequest req);
    Document submitForReview(String documentId, SubmitForReviewRequest req);
    Document completeReview(String documentId, boolean approved, String approver, String comment);
    Document revise(String documentId, String user);
    Document updateStage(String documentId, Stage stage, String user, String comment);
    List<DocumentHistory> history(String documentId);
    void attachFileKey(String documentId, String fileKey, String user);
}

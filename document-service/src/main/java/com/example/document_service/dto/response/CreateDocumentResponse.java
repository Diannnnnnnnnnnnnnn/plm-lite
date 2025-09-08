package com.example.document_service.dto.response;

public class CreateDocumentResponse {
    private Long documentId;
    private String message;

    public CreateDocumentResponse() {
    }

    public CreateDocumentResponse(Long documentId, String message) {
        this.documentId = documentId;
        this.message = message;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

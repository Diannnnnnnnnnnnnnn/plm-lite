package com.example.bom_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LinkPartToDocumentRequest {
    
    @NotBlank(message = "Part ID is required")
    private String partId;
    
    @NotBlank(message = "Document ID is required")
    private String documentId;

    // Getters and Setters
    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}

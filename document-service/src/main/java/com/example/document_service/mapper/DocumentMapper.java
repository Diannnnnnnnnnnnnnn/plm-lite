package com.example.document_service.mapper;

import com.example.document_service.dto.response.DocumentResponse;
import com.example.document_service.model.Document;

public class DocumentMapper {

    private DocumentMapper() {}

    public static DocumentResponse toResponse(Document d) {
        if (d == null) return null;
        
        return new DocumentResponse(
                d.getId(),
                d.getMaster() != null ? d.getMaster().getId() : null,
                d.getTitle(),
                d.getStatus(),
                d.getStage(),
                d.getFullVersion()
        );
    }
}

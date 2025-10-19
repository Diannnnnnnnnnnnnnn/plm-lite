package com.example.document_service.mapper;

import com.example.document_service.dto.response.DocumentResponse;
import com.example.document_service.model.Document;

public class DocumentMapper {

    private DocumentMapper() {}

    public static DocumentResponse toResponse(Document d) {
        if (d == null) return null;

        DocumentResponse response = new DocumentResponse(
                d.getId(),
                d.getMaster() != null ? d.getMaster().getId() : null,
                d.getTitle(),
                d.getStatus(),
                d.getStage(),
                d.getFullVersion(),
                d.getFileKey()
        );
        
        // Set creator and createTime from Document
        response.setCreator(d.getCreator());
        response.setCreateTime(d.getCreateTime());
        
        // Set description
        response.setDescription(d.getDescription());
        
        // Set revision and version numbers
        response.setRevision(d.getRevision());
        response.setVersionNumber(d.getVersion());
        
        // Set master information
        if (d.getMaster() != null) {
            DocumentResponse.MasterInfo masterInfo = new DocumentResponse.MasterInfo(
                    d.getMaster().getId(),
                    d.getMaster().getTitle(), // Using title as documentNumber
                    d.getMaster().getCreator(),
                    d.getMaster().getCreateTime()
            );
            response.setMaster(masterInfo);
        }
        
        return response;
    }
}

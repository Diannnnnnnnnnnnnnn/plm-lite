package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for syncing Part-Document link from BOM/Document Service to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartDocumentLinkRequest {
    private String partId;
    private String documentId;
}


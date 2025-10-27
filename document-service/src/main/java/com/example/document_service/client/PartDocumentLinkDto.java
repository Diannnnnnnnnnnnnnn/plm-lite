package com.example.document_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for syncing Part-Document link to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartDocumentLinkDto {
    private String partId;
    private String documentId;
}


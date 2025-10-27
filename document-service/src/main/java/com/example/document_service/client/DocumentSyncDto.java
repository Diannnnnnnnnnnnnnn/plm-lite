package com.example.document_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Document to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSyncDto {
    private String id;
    private String name;
    private String description;
    private String version;
    private String status;
    private String fileType;
    private Long fileSize;
    private String creator;
    private LocalDateTime createTime;
}


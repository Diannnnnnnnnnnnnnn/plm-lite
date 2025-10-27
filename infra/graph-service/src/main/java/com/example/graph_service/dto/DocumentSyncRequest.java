package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Document data from Document Service to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSyncRequest {
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


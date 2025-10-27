package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Part data from BOM Service to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartSyncRequest {
    private String id;
    private String title;
    private String description;
    private String stage;
    private String status;
    private String level;
    private String creator;
    private LocalDateTime createTime;
}


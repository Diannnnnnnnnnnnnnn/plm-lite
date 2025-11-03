package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Document search result DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResult {
    private String id;
    private String title;
    private String description;
    private String documentNumber;
    private String masterId;
    private String status;
    private String stage;
    private String category;
    private String contentType;
    private String creator;
    private Long fileSize;
    private String version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isActive;
    private Float score;  // Search relevance score
    private String type = "DOCUMENT";  // Entity type for frontend
}




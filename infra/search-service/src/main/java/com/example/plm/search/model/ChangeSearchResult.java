package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Change Request search result DTO (placeholder for Phase 3)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSearchResult {
    private String id;
    private String title;
    private String description;
    private String status;
    private String stage;
    private String changeClass;
    private String changeReason;
    private String creator;
    private LocalDateTime createTime;
    private Float score;
    private String type = "CHANGE";
}


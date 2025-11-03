package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task search result DTO (placeholder for Phase 3)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchResult {
    private String id;
    private String taskName;
    private String description;
    private String status;
    private String assignee;
    private LocalDateTime createTime;
    private Float score;
    private String type = "TASK";
}


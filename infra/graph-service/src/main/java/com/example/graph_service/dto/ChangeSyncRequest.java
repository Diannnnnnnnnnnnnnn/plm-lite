package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Change data from Change Service to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSyncRequest {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String changeType;
    private String initiator;
    private LocalDateTime createTime;
}


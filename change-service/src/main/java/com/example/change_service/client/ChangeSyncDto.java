package com.example.change_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Change to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSyncDto {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String changeType;
    private String initiator;
    private LocalDateTime createTime;
}


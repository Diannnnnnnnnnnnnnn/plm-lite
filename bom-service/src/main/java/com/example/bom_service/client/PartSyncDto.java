package com.example.bom_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing Part to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartSyncDto {
    private String id;
    private String title;
    private String description;
    private String stage;
    private String status;
    private String level;
    private String creator;
    private LocalDateTime createTime;
}


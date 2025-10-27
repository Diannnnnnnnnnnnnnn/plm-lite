package com.example.graph_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for syncing a Task from task-service to graph-service (Neo4j).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSyncRequest {
    
    private String id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createTime;
    
    // Assignee and creator IDs for relationships
    private String assigneeId;
    private String creatorId;
    
    // Related entities
    private String relatedChangeId;
    private String relatedPartId;
}


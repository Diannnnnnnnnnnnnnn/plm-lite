package com.example.task_service.model;

public enum TaskStatus {
    PENDING,      // Task is created but not started
    TODO,         // Legacy status - same as PENDING
    IN_PROGRESS,  // Task is being worked on
    COMPLETED,    // Task is finished
    CANCELLED,    // Task was cancelled
    OVERDUE       // Task passed its due date
}


package com.example.task_service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Custom query methods can be added here if needed
    List<Task> findByAssignedTo(String assignedTo);
}

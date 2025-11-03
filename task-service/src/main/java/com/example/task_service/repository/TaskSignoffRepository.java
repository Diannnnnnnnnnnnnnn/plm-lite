package com.example.task_service.repository;

import com.example.task_service.model.SignoffAction;
import com.example.task_service.model.TaskSignoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSignoffRepository extends JpaRepository<TaskSignoff, Long> {
    
    List<TaskSignoff> findByTaskId(Long taskId);
    
    List<TaskSignoff> findByUserId(String userId);
    
    long countByTaskIdAndActionAndIsRequired(Long taskId, SignoffAction action, Boolean isRequired);
    
    List<TaskSignoff> findByTaskIdAndAction(Long taskId, SignoffAction action);
}


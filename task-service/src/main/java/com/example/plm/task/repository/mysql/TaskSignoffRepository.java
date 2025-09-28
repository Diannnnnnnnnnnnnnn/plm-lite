package com.example.plm.task.repository.mysql;

import com.example.plm.task.model.TaskSignoff;
import com.example.plm.task.model.SignoffAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSignoffRepository extends JpaRepository<TaskSignoff, String> {

    List<TaskSignoff> findByTaskId(String taskId);

    List<TaskSignoff> findBySignoffUser(String signoffUser);

    List<TaskSignoff> findByTaskIdAndSignoffAction(String taskId, SignoffAction signoffAction);

    List<TaskSignoff> findByTaskIdAndIsRequired(String taskId, Boolean isRequired);

    long countByTaskIdAndSignoffActionAndIsRequired(String taskId, SignoffAction signoffAction, Boolean isRequired);
}
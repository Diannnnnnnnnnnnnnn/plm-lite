package com.example.plm.task.repository.mysql;

import com.example.plm.task.model.Task;
import com.example.plm.task.model.TaskStatus;
import com.example.plm.task.model.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByAssignedTo(String assignedTo);

    List<Task> findByAssignedBy(String assignedBy);

    List<Task> findByTaskStatus(TaskStatus taskStatus);

    List<Task> findByTaskType(TaskType taskType);

    List<Task> findByWorkflowId(String workflowId);

    List<Task> findByContextTypeAndContextId(String contextType, String contextId);

    List<Task> findByParentTaskId(String parentTaskId);

    List<Task> findByAssignedToAndTaskStatus(String assignedTo, TaskStatus taskStatus);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.taskStatus IN :activeStatuses")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate,
                               @Param("activeStatuses") List<TaskStatus> activeStatuses);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :assignedTo AND t.taskStatus IN :statuses ORDER BY t.priority DESC, t.dueDate ASC")
    List<Task> findTasksByAssigneeAndStatuses(@Param("assignedTo") String assignedTo,
                                            @Param("statuses") List<TaskStatus> statuses);

    @Query("SELECT t FROM Task t WHERE t.contextType = :contextType AND t.contextId = :contextId AND t.taskStatus = :status")
    List<Task> findByContextAndStatus(@Param("contextType") String contextType,
                                    @Param("contextId") String contextId,
                                    @Param("status") TaskStatus status);
}
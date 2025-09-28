package com.example.plm.task.service;

import com.example.plm.task.dto.CreateTaskRequest;
import com.example.plm.task.dto.TaskResponse;
import com.example.plm.task.model.*;
import com.example.plm.task.model.neo4j.TaskNode;
import com.example.plm.task.repository.mysql.TaskRepository;
import com.example.plm.task.repository.mysql.TaskSignoffRepository;
import com.example.plm.task.repository.neo4j.TaskNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!dev")
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSignoffRepository taskSignoffRepository;

    @Autowired
    private TaskNodeRepository taskNodeRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        String taskId = UUID.randomUUID().toString();

        Task task = new Task(
            taskId,
            request.getTaskName(),
            request.getTaskDescription(),
            request.getTaskType(),
            TaskStatus.PENDING,
            request.getAssignedTo(),
            request.getAssignedBy(),
            request.getDueDate(),
            LocalDateTime.now(),
            request.getPriority(),
            request.getParentTaskId(),
            request.getWorkflowId(),
            request.getContextType(),
            request.getContextId()
        );

        task = taskRepository.save(task);

        TaskNode taskNode = new TaskNode(
            taskId,
            request.getTaskName(),
            request.getTaskDescription(),
            request.getTaskType().toString(),
            TaskStatus.PENDING.toString(),
            request.getAssignedTo(),
            request.getAssignedBy(),
            request.getDueDate(),
            LocalDateTime.now(),
            request.getPriority(),
            request.getWorkflowId(),
            request.getContextType(),
            request.getContextId()
        );

        taskNodeRepository.save(taskNode);

        if (request.getWorkflowId() != null) {
            taskNodeRepository.linkToWorkflow(request.getWorkflowId(), taskId);
        }

        if (request.getParentTaskId() != null) {
            taskNodeRepository.createParentChildRelationship(request.getParentTaskId(), taskId);
        }

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTaskStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        Optional<TaskNode> taskNode = taskNodeRepository.findById(taskId);
        if (taskNode.isPresent()) {
            TaskNode node = taskNode.get();
            node.setTaskStatus(newStatus.toString());
            node.setUpdatedAt(LocalDateTime.now());
            taskNodeRepository.save(node);
        }

        return mapToResponse(task);
    }

    @Transactional
    public void addTaskSignoff(String taskId, String userId, SignoffAction action, String comments) {
        String signoffId = UUID.randomUUID().toString();

        TaskSignoff signoff = new TaskSignoff(
            signoffId,
            taskId,
            userId,
            action,
            comments,
            LocalDateTime.now(),
            true
        );

        taskSignoffRepository.save(signoff);

        if (action == SignoffAction.APPROVED) {
            long requiredApprovals = taskSignoffRepository
                .countByTaskIdAndSignoffActionAndIsRequired(taskId, SignoffAction.APPROVED, true);

            if (requiredApprovals >= getRequiredApprovalsForTask(taskId)) {
                updateTaskStatus(taskId, TaskStatus.COMPLETED);
            }
        } else if (action == SignoffAction.REJECTED) {
            updateTaskStatus(taskId, TaskStatus.CANCELLED);
        }
    }

    public Optional<TaskResponse> getTaskById(String id) {
        return taskRepository.findById(id).map(this::mapToResponse);
    }

    public List<TaskResponse> getTasksByAssignee(String assigneeId) {
        return taskRepository.findByAssignedTo(assigneeId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByTaskStatus(status).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByWorkflow(String workflowId) {
        return taskRepository.findByWorkflowId(workflowId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByContext(String contextType, String contextId) {
        return taskRepository.findByContextTypeAndContextId(contextType, contextId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getOverdueTasks() {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        return taskRepository.findOverdueTasks(LocalDateTime.now(), activeStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getMyActiveTasks(String userId) {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        return taskRepository.findTasksByAssigneeAndStatuses(userId, activeStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private int getRequiredApprovalsForTask(String taskId) {
        return 1;
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTaskName(),
            task.getTaskDescription(),
            task.getTaskType(),
            task.getTaskStatus(),
            task.getAssignedTo(),
            task.getAssignedBy(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getPriority(),
            task.getParentTaskId(),
            task.getWorkflowId(),
            task.getContextType(),
            task.getContextId()
        );
    }
}

@Service
@Profile("dev")
class TaskServiceDev {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSignoffRepository taskSignoffRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        String taskId = UUID.randomUUID().toString();

        Task task = new Task(
            taskId,
            request.getTaskName(),
            request.getTaskDescription(),
            request.getTaskType(),
            TaskStatus.PENDING,
            request.getAssignedTo(),
            request.getAssignedBy(),
            request.getDueDate(),
            LocalDateTime.now(),
            request.getPriority(),
            request.getParentTaskId(),
            request.getWorkflowId(),
            request.getContextType(),
            request.getContextId()
        );

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(String taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTaskStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        return mapToResponse(task);
    }

    @Transactional
    public void addTaskSignoff(String taskId, String userId, SignoffAction action, String comments) {
        String signoffId = UUID.randomUUID().toString();

        TaskSignoff signoff = new TaskSignoff(
            signoffId,
            taskId,
            userId,
            action,
            comments,
            LocalDateTime.now(),
            true
        );

        taskSignoffRepository.save(signoff);

        if (action == SignoffAction.APPROVED) {
            updateTaskStatus(taskId, TaskStatus.COMPLETED);
        } else if (action == SignoffAction.REJECTED) {
            updateTaskStatus(taskId, TaskStatus.CANCELLED);
        }
    }

    public Optional<TaskResponse> getTaskById(String id) {
        return taskRepository.findById(id).map(this::mapToResponse);
    }

    public List<TaskResponse> getTasksByAssignee(String assigneeId) {
        return taskRepository.findByAssignedTo(assigneeId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByTaskStatus(status).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByWorkflow(String workflowId) {
        return taskRepository.findByWorkflowId(workflowId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByContext(String contextType, String contextId) {
        return taskRepository.findByContextTypeAndContextId(contextType, contextId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getOverdueTasks() {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        return taskRepository.findOverdueTasks(LocalDateTime.now(), activeStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> getMyActiveTasks(String userId) {
        List<TaskStatus> activeStatuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        return taskRepository.findTasksByAssigneeAndStatuses(userId, activeStatuses).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTaskName(),
            task.getTaskDescription(),
            task.getTaskType(),
            task.getTaskStatus(),
            task.getAssignedTo(),
            task.getAssignedBy(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getPriority(),
            task.getParentTaskId(),
            task.getWorkflowId(),
            task.getContextType(),
            task.getContextId()
        );
    }
}
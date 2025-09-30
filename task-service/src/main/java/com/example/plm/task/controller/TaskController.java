package com.example.plm.task.controller;

import com.example.plm.task.dto.CreateTaskRequest;
import com.example.plm.task.dto.TaskResponse;
import com.example.plm.task.model.SignoffAction;
import com.example.plm.task.model.TaskStatus;
import com.example.plm.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String id) {
        Optional<TaskResponse> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            TaskStatus newStatus = TaskStatus.valueOf(statusUpdate.get("status"));
            TaskResponse response = taskService.updateTaskStatus(id, newStatus);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/signoff")
    public ResponseEntity<Void> addSignoff(
            @PathVariable String id,
            @RequestBody Map<String, String> signoffRequest) {
        try {
            String userId = signoffRequest.get("userId");
            SignoffAction action = SignoffAction.valueOf(signoffRequest.get("action"));
            String comments = signoffRequest.get("comments");

            taskService.addTaskSignoff(id, userId, action, comments);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String contextType,
            @RequestParam(required = false) String contextId) {

        List<TaskResponse> tasks;

        if (assignee != null) {
            tasks = taskService.getTasksByAssignee(assignee);
        } else if (status != null) {
            TaskStatus taskStatus = TaskStatus.valueOf(status);
            tasks = taskService.getTasksByStatus(taskStatus);
        } else if (workflowId != null) {
            tasks = taskService.getTasksByWorkflow(workflowId);
        } else if (contextType != null && contextId != null) {
            tasks = taskService.getTasksByContext(contextType, contextId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<TaskResponse> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks/{userId}")
    public ResponseEntity<List<TaskResponse>> getMyActiveTasks(@PathVariable String userId) {
        List<TaskResponse> tasks = taskService.getMyActiveTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/by-document/{documentId}")
    public ResponseEntity<List<TaskResponse>> getTasksByDocument(@PathVariable String documentId) {
        List<TaskResponse> tasks = taskService.getTasksByContext("DOCUMENT", documentId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/review-tasks/{userId}")
    public ResponseEntity<List<TaskResponse>> getReviewTasks(@PathVariable String userId) {
        // Get all active tasks for this user and filter for REVIEW type
        List<TaskResponse> allTasks = taskService.getMyActiveTasks(userId);
        List<TaskResponse> reviewTasks = allTasks.stream()
                .filter(task -> task.getTaskType() == com.example.plm.task.model.TaskType.REVIEW)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(reviewTasks);
    }
}
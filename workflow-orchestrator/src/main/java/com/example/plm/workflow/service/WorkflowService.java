package com.example.plm.workflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public class WorkflowService {

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    public String startDocumentApprovalWorkflow(String documentId, String initiatorId) {
        System.out.println("WorkflowServiceDev: Starting document approval workflow for " + documentId);
        return "dev-workflow-" + documentId;
    }

    public String startChangeApprovalWorkflow(String changeId, String initiatorId) {
        System.out.println("WorkflowServiceDev: Starting change approval workflow for " + changeId);
        return "dev-workflow-" + changeId;
    }

    public void completeTask(String jobKey, Map<String, Object> variables) {
        System.out.println("WorkflowServiceDev: Completing task " + jobKey + " with variables: " + variables);
    }

    public void cancelProcessInstance(String processInstanceKey) {
        System.out.println("WorkflowServiceDev: Cancelling process instance " + processInstanceKey);
    }

    public void createReviewTasks(String documentId, String masterId, String version, String creator, List<String> reviewers) {
        System.out.println("WorkflowServiceDev: Creating review tasks for document " + documentId + " with reviewers: " + reviewers);

        // Create a review task for each reviewer
        for (String reviewerId : reviewers) {
            try {
                // reviewerId is already a user ID, use it directly
                Long userId = Long.parseLong(reviewerId);

                CreateTaskRequest taskRequest = new CreateTaskRequest();
                taskRequest.setName("Review Document: " + masterId + " " + version + " [" + documentId + "]");
                taskRequest.setDescription("Please review document '" + masterId + "' version " + version + " submitted by " + creator + ". Document ID: " + documentId);
                taskRequest.setUserId(userId);

                TaskResponse response = taskServiceClient.createTask(taskRequest);
                System.out.println("Created task for reviewer ID " + userId + ": " + response.getId());
            } catch (Exception e) {
                System.err.println("Failed to create task for reviewer " + reviewerId + ": " + e.getMessage());
                e.printStackTrace();
                // Continue with other reviewers even if one fails
            }
        }
    }
}

@FeignClient(name = "task-service", url = "http://localhost:8082")
interface TaskServiceClient {
    @PostMapping("/tasks")
    TaskResponse createTask(@RequestBody CreateTaskRequest request);
}

@FeignClient(name = "user-service", url = "http://localhost:8083")
interface UserServiceClient {
    @GetMapping("/users/by-username/{username}")
    UserResponse getUserByUsername(@PathVariable("username") String username);
}

class CreateTaskRequest {
    private String name;
    private String description;
    private Long userId;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private Long userId;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

class UserResponse {
    private Long id;
    private String username;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

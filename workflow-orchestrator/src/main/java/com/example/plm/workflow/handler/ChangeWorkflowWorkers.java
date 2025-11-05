package com.example.plm.workflow.handler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.plm.workflow.client.TaskServiceClient;
import com.example.plm.workflow.client.UserServiceClient;
import com.example.plm.workflow.dto.UserResponse;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import jakarta.annotation.PostConstruct;

/**
 * Zeebe Job Workers for Change Approval Workflow
 * Handles all service tasks defined in the change-approval.bpmn process
 */
@Component
public class ChangeWorkflowWorkers {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * Register all change workflow job workers after bean initialization
     */
    @PostConstruct
    public void registerWorkers() {
        System.out.println("\n🔧 Registering Change Approval Workers...");
        
        // Register create-change-approval-task worker
        zeebeClient.newWorker()
                .jobType("create-change-approval-task")
                .handler(this::handleCreateChangeApprovalTask)
                .name("create-change-approval-task-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: create-change-approval-task");

        // Register wait-for-change-review worker
        zeebeClient.newWorker()
                .jobType("wait-for-change-review")
                .handler(this::handleWaitForChangeReview)
                .name("wait-for-change-review-worker")
                .maxJobsActive(100)
                .timeout(Duration.ofHours(24))
                .open();
        System.out.println("   ✓ Registered: wait-for-change-review");

        // NOTE: update-change-status and notify-change-completion workers are now
        // registered via @JobWorker annotations in ChangeWorkerHandler.java
        // Those workers have better implementation with document version updates

        System.out.println("✅ Change workflow workers registered successfully!\n");
    }

    /**
     * Worker for "Create Change Approval Task" service task
     * Creates a review task for the change reviewer
     */
    private void handleCreateChangeApprovalTask(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String changeTitle = (String) variables.get("changeTitle");
        String creator = (String) variables.get("creator");
        String reviewerId = (String) variables.get("reviewerId");
        
        System.out.println("📋 Creating change approval task");
        System.out.println("   Change ID: " + changeId);
        System.out.println("   Title: " + changeTitle);
        System.out.println("   Reviewer: " + reviewerId);
        
        try {
            // Parse reviewer ID
            Long userId = null;
            String username = null;
            try {
                userId = Long.parseLong(reviewerId);
                // Fetch username from user service
                try {
                    UserResponse user = userServiceClient.getUserById(userId);
                    username = user.getUsername();
                    System.out.println("   ✓ Resolved user ID " + userId + " to username: " + username);
                } catch (Exception e) {
                    System.err.println("   ⚠ Failed to fetch username for user ID " + userId);
                    username = "User" + userId; // Fallback username
                }
            } catch (NumberFormatException e) {
                // If reviewer is a username, look up the userId
                try {
                    UserResponse user = userServiceClient.getUserByUsername(reviewerId);
                    userId = user.getId();
                    username = reviewerId;
                    System.out.println("   ✓ Resolved username '" + reviewerId + "' to user ID: " + userId);
                } catch (Exception ex) {
                    System.err.println("   ⚠ Failed to fetch user ID for username: " + reviewerId);
                    userId = 0L; // Fallback
                    username = reviewerId;
                }
            }

            // Create approval task with context information
            String taskName = "Review Change: " + changeTitle;
            String taskDescription = 
                "Please review change '" + changeTitle + "' submitted by " + creator + ".\n\n" +
                "Change ID: " + changeId + "\n" +
                "Workflow Instance: " + job.getProcessInstanceKey();

            // Build request with context
            TaskServiceClient.CreateTaskRequest request = new TaskServiceClient.CreateTaskRequest();
            request.setTaskName(taskName);
            request.setTaskDescription(taskDescription);
            request.setTaskType("REVIEW");  // TaskType.REVIEW
            request.setAssignedTo(username);
            request.setAssignedBy("WORKFLOW");
            request.setContextType("CHANGE");  // 🔑 KEY FIX: Store context type
            request.setContextId(changeId);    // 🔑 KEY FIX: Store change ID
            request.setWorkflowId(String.valueOf(job.getProcessInstanceKey()));
            request.setPriority(5);

            TaskServiceClient.TaskResponse response = taskServiceClient.createTaskWithContext(request);
            
            String taskId = response.getId();
            System.out.println("   ✓ Created change review task ID: " + taskId);
            System.out.println("   ✓ Task linked to CHANGE: " + changeId);

            // Complete the job
            Map<String, Object> result = new HashMap<>();
            result.put("tasksCreated", true);
            result.put("taskId", taskId);
            result.put("reviewerId", reviewerId);
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("❌ Error creating change approval task: " + e.getMessage());
            e.printStackTrace();
            
            // Fail the job
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Failed to create change approval task: " + e.getMessage())
                    .send();
        }
    }

    /**
     * Worker for "Wait For Change Review" service task
     * Polls the task until it's completed
     */
    private void handleWaitForChangeReview(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        Object taskIdObj = variables.get("taskId");
        
        System.out.println("⏳ Checking change review status");
        System.out.println("   Change ID: " + changeId);
        System.out.println("   Task ID: " + taskIdObj);
        
        try {
            if (taskIdObj == null) {
                throw new RuntimeException("No taskId found in workflow variables");
            }
            
            // Get task status
            Long taskId = Long.parseLong(taskIdObj.toString());
            TaskServiceClient.TaskDTO task = taskServiceClient.getTask(taskId);
            
            System.out.println("   Task Status: " + task.getStatus());
            System.out.println("   Task Decision: " + task.getDecision());
            
            // Check if task is completed
            if ("COMPLETED".equals(task.getStatus())) {
                // Task is completed - check the decision
                boolean isApproved = "APPROVED".equals(task.getDecision());
                
                System.out.println("   ✓ Review completed: " + (isApproved ? "APPROVED" : "REJECTED"));
                
                // Complete the workflow job with the approval result
                Map<String, Object> result = new HashMap<>();
                result.put("approved", isApproved);
                result.put("reviewCompleted", true);
                result.put("decision", task.getDecision());
                
                client.newCompleteCommand(job.getKey())
                        .variables(result)
                        .send()
                        .join();
                        
            } else {
                // Task not completed yet - fail the job to trigger retry
                System.out.println("   ⏳ Task not yet completed, will retry...");
                client.newFailCommand(job.getKey())
                        .retries(job.getRetries())
                        .errorMessage("Task not yet completed")
                        .send();
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Error checking task status: " + e.getMessage());
            // Fail the job to trigger retry
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries())
                    .errorMessage("Error checking task: " + e.getMessage())
                    .send();
        }
    }

    // NOTE: handleUpdateChangeStatus and handleNotifyChangeCompletion methods have been
    // removed because they are now implemented in ChangeWorkerHandler.java with @JobWorker
    // annotations. The new implementation in ChangeWorkerHandler includes:
    // 1. Document version updates when changes are approved
    // 2. Better error handling and logging
    // 3. Integration with DocumentServiceClient for change-based edits
}


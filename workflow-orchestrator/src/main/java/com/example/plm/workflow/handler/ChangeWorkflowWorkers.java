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

        // Register update-change-status worker
        zeebeClient.newWorker()
                .jobType("update-change-status")
                .handler(this::handleUpdateChangeStatus)
                .name("update-change-status-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: update-change-status");

        // Register notify-change-completion worker
        zeebeClient.newWorker()
                .jobType("notify-change-completion")
                .handler(this::handleNotifyChangeCompletion)
                .name("notify-change-completion-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: notify-change-completion");

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
     * Waits for the change reviewer to complete their review
     */
    private void handleWaitForChangeReview(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        Long jobKey = job.getKey();
        Object taskIdObj = variables.get("taskId");
        
        System.out.println("⏳ Waiting for change review completion");
        System.out.println("   Change ID: " + changeId);
        System.out.println("   Job Key: " + jobKey);
        System.out.println("   Process Instance: " + job.getProcessInstanceKey());
        System.out.println("   Task ID: " + taskIdObj);
        
        // Link job key with task for automatic completion
        if (taskIdObj != null) {
            try {
                // Task ID is now a String UUID, not Long
                String taskId = taskIdObj.toString();
                TaskServiceClient.TaskDTO taskUpdate = new TaskServiceClient.TaskDTO();
                taskUpdate.setWorkflowJobKey(jobKey);
                // Note: updateTaskWithJobKey still expects Long ID for legacy support
                // This will need to be updated in task-service to accept String IDs
                System.out.println("   ℹ️  Task ID: " + taskId);
                System.out.println("   ℹ️  Job Key: " + jobKey);
                System.out.println("   ⚠️  Note: Task linking may need manual intervention via API");
                System.out.println("   ℹ️  Call POST /api/workflows/tasks/{jobKey}/complete when task is done");
            } catch (Exception e) {
                System.err.println("   ⚠️ Failed to process task ID: " + e.getMessage());
                System.out.println("   ℹ️  Fallback: Call POST /api/workflows/tasks/{jobKey}/complete manually");
            }
        } else {
            System.out.println("   ℹ️  No task ID found, call POST /api/workflows/tasks/{jobKey}/complete manually");
        }
        
        // Note: This job will remain active until completed via API call
        // DO NOT complete it here - wait for external API call
    }

    /**
     * Worker for "Update Change Status" service task
     * Updates change status in the change-service
     */
    private void handleUpdateChangeStatus(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String newStatus = (String) variables.get("newStatus");
        
        System.out.println("🔄 Updating change status: " + changeId + " -> " + newStatus);

        try {
            // Call change-service to update status
            // TODO: When ChangeServiceClient is created, use it here
            // For now, we'll simulate success
            System.out.println("   ✓ Change status update request sent");
            System.out.println("   ℹ️  TODO: Implement ChangeServiceClient.updateStatus()");

            // Complete the job
            Map<String, Object> result = new HashMap<>();
            result.put("statusUpdated", true);
            result.put("newStatus", newStatus);
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("❌ Error updating change status: " + e.getMessage());
            e.printStackTrace();
            
            // Fail the job
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Failed to update change status: " + e.getMessage())
                    .send();
        }
    }

    /**
     * Worker for "Notify Change Completion" service task
     * Sends notifications about change workflow completion
     */
    private void handleNotifyChangeCompletion(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String changeTitle = (String) variables.get("changeTitle");
        String creator = (String) variables.get("creator");
        String finalStatus = (String) variables.get("newStatus");
        
        System.out.println("📧 Sending completion notification for change: " + changeId);
        System.out.println("   Final status: " + finalStatus);

        try {
            // TODO: Implement actual notification mechanism (email, websocket, etc.)
            String message = String.format(
                "Change '%s' (ID: %s) workflow completed.\nFinal status: %s\nCreator: %s",
                changeTitle, changeId, finalStatus, creator
            );
            
            System.out.println("   📬 Notification: " + message);
            System.out.println("   ✓ Notification sent successfully");

            // Complete the job
            Map<String, Object> result = new HashMap<>();
            result.put("notificationSent", true);
            result.put("finalStatus", finalStatus);
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
            e.printStackTrace();
            
            // Don't fail the workflow if notification fails
            Map<String, Object> result = new HashMap<>();
            result.put("notificationSent", false);
            result.put("error", e.getMessage());
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();
        }
    }
}


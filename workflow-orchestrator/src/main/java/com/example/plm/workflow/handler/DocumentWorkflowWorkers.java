package com.example.plm.workflow.handler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.plm.workflow.client.DocumentServiceClient;
import com.example.plm.workflow.client.TaskServiceClient;
import com.example.plm.workflow.client.UserServiceClient;
import com.example.plm.workflow.dto.ApproveRejectRequest;
import com.example.plm.workflow.dto.UserResponse;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import jakarta.annotation.PostConstruct;

/**
 * Zeebe Job Workers for Document Approval Workflow
 * Handles all service tasks defined in the document-approval.bpmn process
 */
@Component
public class DocumentWorkflowWorkers {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private DocumentServiceClient documentServiceClient;

    /**
     * Register all job workers after bean initialization
     */
    @PostConstruct
    public void registerWorkers() {
        System.out.println("\n🔧 Registering Zeebe Job Workers...");
        
        // Register create-approval-task worker
        zeebeClient.newWorker()
                .jobType("create-approval-task")
                .handler(this::handleCreateApprovalTask)
                .name("create-approval-task-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: create-approval-task");

        // Register update-status worker
        zeebeClient.newWorker()
                .jobType("update-status")
                .handler(this::handleUpdateStatus)
                .name("update-status-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: update-status");

        // Register notify-completion worker
        zeebeClient.newWorker()
                .jobType("notify-completion")
                .handler(this::handleNotifyCompletion)
                .name("notify-completion-worker")
                .maxJobsActive(10)
                .timeout(Duration.ofSeconds(30))
                .open();
        System.out.println("   ✓ Registered: notify-completion");

        // Register wait-for-review worker (legacy - for old workflows)
        zeebeClient.newWorker()
                .jobType("wait-for-review")
                .handler(this::handleWaitForReview)
                .name("wait-for-review-worker")
                .maxJobsActive(100)
                .timeout(Duration.ofHours(24)) // 24 hour timeout for review
                .open();
        System.out.println("   ✓ Registered: wait-for-review");

        // NEW: Register wait-for-initial-review worker
        zeebeClient.newWorker()
                .jobType("wait-for-initial-review")
                .handler(this::handleWaitForInitialReview)
                .name("wait-for-initial-review-worker")
                .maxJobsActive(100)
                .timeout(Duration.ofHours(24))
                .open();
        System.out.println("   ✓ Registered: wait-for-initial-review");

        // NEW: Register wait-for-technical-review worker
        zeebeClient.newWorker()
                .jobType("wait-for-technical-review")
                .handler(this::handleWaitForTechnicalReview)
                .name("wait-for-technical-review-worker")
                .maxJobsActive(100)
                .timeout(Duration.ofHours(24))
                .open();
        System.out.println("   ✓ Registered: wait-for-technical-review");

        System.out.println("✅ All job workers registered successfully!\n");
    }

    /**
     * Worker for "Create Approval Task" service task
     * Creates review tasks in the task-service for assigned reviewers
     * NOW SUPPORTS TWO-STAGE REVIEW: Stores both initialReviewer and technicalReviewer
     */
    private void handleCreateApprovalTask(JobClient client, ActivatedJob job) {
        // Extract variables from job
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String masterId = (String) variables.get("masterId");
        String version = (String) variables.get("version");
        String creator = (String) variables.get("creator");
        
        // NEW: Support both old (reviewerIds list) and new (initialReviewer + technicalReviewer) formats
        String initialReviewer = (String) variables.get("initialReviewer");
        String technicalReviewer = (String) variables.get("technicalReviewer");
        List<String> reviewerIds = (List<String>) variables.get("reviewerIds");
        
        System.out.println("📋 Creating approval tasks for document: " + documentId);
        if (initialReviewer != null && technicalReviewer != null) {
            System.out.println("   Two-Stage Review Mode:");
            System.out.println("   Initial Reviewer: " + initialReviewer);
            System.out.println("   Technical Reviewer: " + technicalReviewer);
        } else {
            System.out.println("   Legacy Mode - Reviewers: " + reviewerIds);
        }

        Long lastCreatedTaskId = null; // Track last created task ID for job key linking
        
        try {
            // NEW: For two-stage review, skip creating tasks here
            // Individual review workers (wait-for-initial-review, wait-for-technical-review) will create them
            if (initialReviewer != null && technicalReviewer != null) {
                System.out.println("   ℹ️  Two-stage mode: Tasks will be created by individual review workers");
                System.out.println("   ℹ️  Skipping task creation in create-approval-task worker");
            }
            // Legacy mode: Create a task for each reviewer
            else if (reviewerIds != null && !reviewerIds.isEmpty()) {
                for (String reviewerId : reviewerIds) {
                    try {
                        Long userId = Long.parseLong(reviewerId);
                        
                        // Fetch username from user service
                        String username = null;
                        try {
                            UserResponse user = userServiceClient.getUserById(userId);
                            username = user.getUsername();
                            System.out.println("   ✓ Resolved user ID " + userId + " to username: " + username);
                        } catch (Exception e) {
                            System.err.println("   ⚠ Failed to fetch username for user ID " + userId + ": " + e.getMessage());
                        }

                        // Create task using form parameters (task-service uses old API)
                        String taskName = "Review Document: " + masterId + " " + version;
                        String taskDescription = 
                            "Please review document '" + masterId + "' version " + version + 
                            " submitted by " + creator + ".\n\n" +
                            "Document ID: " + documentId + "\n" +
                            "Workflow Instance: " + job.getProcessInstanceKey();

                        TaskServiceClient.TaskDTO response = taskServiceClient.createTask(
                            taskName,
                            taskDescription,
                            userId,
                            username // assignedTo
                        );
                        lastCreatedTaskId = response.getId(); // Store for job key linking
                        System.out.println("   ✓ Created task ID " + response.getId() + " for " + username);
                        
                    } catch (Exception e) {
                        System.err.println("   ✗ Failed to create task for reviewer " + reviewerId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.err.println("   ⚠️  No reviewers specified (neither reviewerIds nor two-stage reviewers)");
            }

            // Complete the job and pass task ID to next step
            Map<String, Object> result = new HashMap<>();
            result.put("tasksCreated", true);
            result.put("taskCount", reviewerIds != null ? reviewerIds.size() : 2);
            result.put("taskId", lastCreatedTaskId); // Pass to wait-for-review step
            
            // NEW: Store reviewers in workflow variables for two-stage review
            if (initialReviewer != null && technicalReviewer != null) {
                result.put("initialReviewer", initialReviewer);
                result.put("technicalReviewer", technicalReviewer);
            }
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("❌ Error creating approval tasks: " + e.getMessage());
            e.printStackTrace();
            
            // Fail the job
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Failed to create approval tasks: " + e.getMessage())
                    .send();
        }
    }

    /**
     * Worker for "Update Status" service task
     * Updates document status in the document-service
     */
    private void handleUpdateStatus(JobClient client, ActivatedJob job) {
        // Extract variables from job
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String newStatus = (String) variables.get("newStatus");
        String creator = (String) variables.get("creator");
        
        System.out.println("🔄 Updating document status: " + documentId + " -> " + newStatus);

        try {
            // Use completeReview endpoint which properly handles RELEASED/IN_WORK status
            boolean approved = "RELEASED".equals(newStatus);
            ApproveRejectRequest request = new ApproveRejectRequest();
            request.setApproved(approved);
            request.setUser(creator != null ? creator : "system");
            request.setComment(approved ? "Approved by workflow" : "Rejected by workflow");
            
            documentServiceClient.completeReview(documentId, request);
            
            System.out.println("   ✓ Document status updated successfully");

            // Complete the job
            Map<String, Object> result = new HashMap<>();
            result.put("statusUpdated", true);
            result.put("newStatus", newStatus);
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("❌ Error updating document status: " + e.getMessage());
            e.printStackTrace();
            
            // Fail the job
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Failed to update document status: " + e.getMessage())
                    .send();
        }
    }

    /**
     * Worker for "Wait For Review" service task
     * This worker waits to be completed by an external API call when review is done
     */
    private void handleWaitForReview(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        Long jobKey = job.getKey();
        Object taskIdObj = variables.get("taskId");
        
        System.out.println("⏳ Waiting for review completion for document: " + documentId);
        System.out.println("   Job Key: " + jobKey);
        System.out.println("   Process Instance: " + job.getProcessInstanceKey());
        
        // ✅ AUTOMATIC SYNC: Link this job key with the task
        if (taskIdObj != null) {
            try {
                Long taskId = Long.valueOf(taskIdObj.toString());
                // Create task DTO with the job key
                TaskServiceClient.TaskDTO taskUpdate = new TaskServiceClient.TaskDTO();
                taskUpdate.setWorkflowJobKey(jobKey);
                // Update task with workflow job key
                taskServiceClient.updateTaskWithJobKey(taskId, taskUpdate);
                System.out.println("   ✅ Linked task ID " + taskId + " with job key " + jobKey);
                System.out.println("   ℹ️  Task will auto-complete workflow when marked as COMPLETED in UI!");
            } catch (Exception e) {
                System.err.println("   ⚠️ Failed to link job key with task: " + e.getMessage());
                System.out.println("   ℹ️  Fallback: Call POST /api/workflows/tasks/{jobKey}/complete manually");
            }
        } else {
            System.out.println("   ℹ️  No task ID found, call POST /api/workflows/tasks/{jobKey}/complete manually");
        }
        
        // Note: This job will remain active until completed via API call
        // or until it times out (24 hours)
        // DO NOT complete it here - wait for external API call
    }

    /**
     * Worker for "Notify Completion" service task
     * Sends notifications about workflow completion
     */
    private void handleNotifyCompletion(JobClient client, ActivatedJob job) {
        // Extract variables from job
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String masterId = (String) variables.get("masterId");
        String creator = (String) variables.get("creator");
        String finalStatus = (String) variables.get("newStatus");
        
        System.out.println("📧 Sending completion notification for document: " + documentId);
        System.out.println("   Final status: " + finalStatus);

        try {
            // TODO: Implement actual notification mechanism (email, websocket, etc.)
            // For now, just log the notification
            String message = String.format(
                "Document '%s' (ID: %s) workflow completed.\nFinal status: %s\nCreator: %s",
                masterId, documentId, finalStatus, creator
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
            
            // Don't fail the workflow if notification fails - just complete with error flag
            Map<String, Object> result = new HashMap<>();
            result.put("notificationSent", false);
            result.put("error", e.getMessage());
            
            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();
        }
    }

    /**
     * NEW: Worker for "Wait For Initial Review" service task
     * Creates task for initial reviewer and waits for completion
     */
    private void handleWaitForInitialReview(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String masterId = (String) variables.get("masterId");
        String version = (String) variables.get("version");
        String creator = (String) variables.get("creator");
        String initialReviewer = (String) variables.get("initialReviewer");
        String technicalReviewer = (String) variables.get("technicalReviewer");
        Long jobKey = job.getKey();
        
        System.out.println("⏳ Stage 1: Waiting for INITIAL REVIEW");
        System.out.println("   Document: " + documentId);
        System.out.println("   Initial Reviewer: " + initialReviewer);
        System.out.println("   Technical Reviewer (next): " + technicalReviewer);
        System.out.println("   Job Key: " + jobKey);
        
        try {
            // Create task for initial reviewer
            String taskName = "Initial Review: " + masterId + " " + version;
            String taskDescription = 
                "Please perform the initial review of document '" + masterId + "' version " + version + 
                " submitted by " + creator + ".\n\n" +
                "This is the FIRST stage of a two-stage review process.\n" +
                "After you complete this review, it will go to " + technicalReviewer + " for technical review.\n\n" +
                "Document ID: " + documentId + "\n" +
                "Workflow Instance: " + job.getProcessInstanceKey();

            // Parse reviewer ID (assuming format is userId or username)
            Long userId = null;
            try {
                userId = Long.parseLong(initialReviewer);
            } catch (NumberFormatException e) {
                // If not a number, try to look up by username
                System.out.println("   ℹ️  Initial reviewer is username, not ID: " + initialReviewer);
            }

            TaskServiceClient.TaskDTO response;
            if (userId != null) {
                // Fetch username from user service
                String username = null;
                try {
                    UserResponse user = userServiceClient.getUserById(userId);
                    username = user.getUsername();
                    System.out.println("   ✓ Resolved user ID " + userId + " to username: " + username);
                } catch (Exception e) {
                    System.err.println("   ⚠ Failed to fetch username for user ID " + userId);
                    username = "User" + userId; // Fallback username
                }
                
                // Use the enhanced createTask method with review info
                response = taskServiceClient.createTaskWithReviewInfo(
                    taskName,
                    taskDescription,
                    userId,
                    username,
                    username, // initialReviewer
                    technicalReviewer,
                    "INITIAL_REVIEW"
                );
                System.out.println("   📝 Task creation request sent with review info");
            } else {
                // If reviewer is a username string, look up the userId
                try {
                    UserResponse user = userServiceClient.getUserByUsername(initialReviewer);
                    userId = user.getId();
                    System.out.println("   ✓ Resolved username '" + initialReviewer + "' to user ID: " + userId);
                } catch (Exception e) {
                    System.err.println("   ⚠ Failed to fetch user ID for username: " + initialReviewer);
                    System.err.println("   Error: " + e.getMessage());
                    userId = 0L; // Fallback to 0
                }
                
                response = taskServiceClient.createTaskWithReviewInfo(
                    taskName,
                    taskDescription,
                    userId,
                    initialReviewer, // assignedTo
                    initialReviewer, // initialReviewer
                    technicalReviewer,
                    "INITIAL_REVIEW"
                );
                System.out.println("   📝 Task creation request sent (username mode, userId: " + userId + ")");
            }
            
            Long taskId = response.getId();
            System.out.println("   ✓ Created initial review task ID: " + taskId);

            // Link job key with task for automatic completion
            TaskServiceClient.TaskDTO taskUpdate = new TaskServiceClient.TaskDTO();
            taskUpdate.setWorkflowJobKey(jobKey);
            taskServiceClient.updateTaskWithJobKey(taskId, taskUpdate);
            System.out.println("   ✅ Linked task with job key for auto-completion");
            System.out.println("   ℹ️  Task will auto-complete workflow when marked as COMPLETED!");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Error creating initial review task: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Note: This job will remain active until completed via API call
        // DO NOT complete it here - wait for external API call
    }

    /**
     * NEW: Worker for "Wait For Technical Review" service task
     * Creates task for technical reviewer and waits for completion
     */
    private void handleWaitForTechnicalReview(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String masterId = (String) variables.get("masterId");
        String version = (String) variables.get("version");
        String creator = (String) variables.get("creator");
        String initialReviewer = (String) variables.get("initialReviewer");
        String technicalReviewer = (String) variables.get("technicalReviewer");
        Long jobKey = job.getKey();
        
        System.out.println("⏳ Stage 2: Waiting for TECHNICAL REVIEW");
        System.out.println("   Document: " + documentId);
        System.out.println("   Initial Reviewer (completed): " + initialReviewer);
        System.out.println("   Technical Reviewer: " + technicalReviewer);
        System.out.println("   Job Key: " + jobKey);
        
        try {
            // Create task for technical reviewer
            String taskName = "Technical Review: " + masterId + " " + version;
            String taskDescription = 
                "Please perform the technical review of document '" + masterId + "' version " + version + 
                " submitted by " + creator + ".\n\n" +
                "This is the SECOND stage (technical review) of the approval process.\n" +
                "Initial review has been completed by " + initialReviewer + ".\n\n" +
                "Document ID: " + documentId + "\n" +
                "Workflow Instance: " + job.getProcessInstanceKey();

            // Parse reviewer ID
            Long userId = null;
            try {
                userId = Long.parseLong(technicalReviewer);
            } catch (NumberFormatException e) {
                System.out.println("   ℹ️  Technical reviewer is username, not ID: " + technicalReviewer);
            }

            TaskServiceClient.TaskDTO response;
            if (userId != null) {
                // Fetch username from user service
                String username = null;
                try {
                    UserResponse user = userServiceClient.getUserById(userId);
                    username = user.getUsername();
                    System.out.println("   ✓ Resolved user ID " + userId + " to username: " + username);
                } catch (Exception e) {
                    System.err.println("   ⚠ Failed to fetch username for user ID " + userId);
                    username = "User" + userId; // Fallback username
                }
                
                response = taskServiceClient.createTaskWithReviewInfo(
                    taskName,
                    taskDescription,
                    userId,
                    username,
                    initialReviewer,
                    username, // technicalReviewer
                    "TECHNICAL_REVIEW"
                );
                System.out.println("   📝 Task creation request sent with review info");
            } else {
                // If reviewer is a username string, look up the userId
                try {
                    UserResponse user = userServiceClient.getUserByUsername(technicalReviewer);
                    userId = user.getId();
                    System.out.println("   ✓ Resolved username '" + technicalReviewer + "' to user ID: " + userId);
                } catch (Exception e) {
                    System.err.println("   ⚠ Failed to fetch user ID for username: " + technicalReviewer);
                    System.err.println("   Error: " + e.getMessage());
                    userId = 0L; // Fallback to 0
                }
                
                response = taskServiceClient.createTaskWithReviewInfo(
                    taskName,
                    taskDescription,
                    userId,
                    technicalReviewer, // assignedTo
                    initialReviewer,
                    technicalReviewer,
                    "TECHNICAL_REVIEW"
                );
                System.out.println("   📝 Task creation request sent (username mode, userId: " + userId + ")");
            }
            
            Long taskId = response.getId();
            System.out.println("   ✓ Created technical review task ID: " + taskId);

            // Link job key with task for automatic completion
            TaskServiceClient.TaskDTO taskUpdate = new TaskServiceClient.TaskDTO();
            taskUpdate.setWorkflowJobKey(jobKey);
            taskServiceClient.updateTaskWithJobKey(taskId, taskUpdate);
            System.out.println("   ✅ Linked task with job key for auto-completion");
            System.out.println("   ℹ️  Task will auto-complete workflow when marked as COMPLETED!");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Error creating technical review task: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Note: This job will remain active until completed via API call
        // DO NOT complete it here - wait for external API call
    }
}

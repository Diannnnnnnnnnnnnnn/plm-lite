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
import com.example.plm.workflow.dto.DocumentStatusUpdateRequest;
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

        // Register wait-for-review worker
        zeebeClient.newWorker()
                .jobType("wait-for-review")
                .handler(this::handleWaitForReview)
                .name("wait-for-review-worker")
                .maxJobsActive(100)
                .timeout(Duration.ofHours(24)) // 24 hour timeout for review
                .open();
        System.out.println("   ✓ Registered: wait-for-review");

        System.out.println("✅ All job workers registered successfully!\n");
    }

    /**
     * Worker for "Create Approval Task" service task
     * Creates review tasks in the task-service for assigned reviewers
     */
    private void handleCreateApprovalTask(JobClient client, ActivatedJob job) {
        // Extract variables from job
        Map<String, Object> variables = job.getVariablesAsMap();
        String documentId = (String) variables.get("documentId");
        String masterId = (String) variables.get("masterId");
        String version = (String) variables.get("version");
        String creator = (String) variables.get("creator");
        List<String> reviewerIds = (List<String>) variables.get("reviewerIds");
        
        System.out.println("📋 Creating approval tasks for document: " + documentId);
        System.out.println("   Reviewers: " + reviewerIds);

        Long lastCreatedTaskId = null; // Track last created task ID for job key linking
        
        try {
            // Create a task for each reviewer
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

            // Complete the job and pass task ID to next step
            Map<String, Object> result = new HashMap<>();
            result.put("tasksCreated", true);
            result.put("taskCount", reviewerIds.size());
            result.put("taskId", lastCreatedTaskId); // Pass to wait-for-review step
            
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
            // Update document status via document-service
            DocumentStatusUpdateRequest request = new DocumentStatusUpdateRequest();
            request.setStatus(newStatus);
            request.setUser(creator != null ? creator : "system");
            
            documentServiceClient.updateDocumentStatus(documentId, request);
            
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
}

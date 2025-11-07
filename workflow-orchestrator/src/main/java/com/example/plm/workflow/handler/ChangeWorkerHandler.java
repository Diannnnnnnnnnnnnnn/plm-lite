package com.example.plm.workflow.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.plm.workflow.client.DocumentServiceClient;
import com.example.plm.workflow.client.TaskServiceClient;
import com.example.plm.workflow.client.UserServiceClient;
import com.example.plm.workflow.dto.UserResponse;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

/**
 * Workflow Worker Handler for Change Approval Process
 * Handles all Zeebe job workers for the change-approval.bpmn process
 */
@Component
public class ChangeWorkerHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChangeWorkerHandler.class);

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired(required = false)
    private ChangeServiceClient changeServiceClient;

    @Autowired(required = false)
    private DocumentServiceClient documentServiceClient;

    /**
     * Worker: create-change-approval-task
     * Creates a task in the task-service for the reviewer to approve/reject the change
     */
    @JobWorker(type = "create-change-approval-task")
    public Map<String, Object> createChangeApprovalTask(final ActivatedJob job) {
        logger.info("🔧 Creating change approval task");
        logger.info("   Change ID: {}", job.getVariablesAsMap().get("changeId"));
        logger.info("   Title: {}", job.getVariablesAsMap().get("changeTitle"));
        logger.info("   Reviewer: {}", job.getVariablesAsMap().get("reviewerId"));

        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String changeTitle = (String) variables.get("changeTitle");
        String creator = (String) variables.get("creator");
        String reviewerId = (String) variables.get("reviewerId");

        try {
            // Parse reviewer ID as Long
            Long reviewerUserId = Long.parseLong(reviewerId);

            // Fetch reviewer username
            String reviewerUsername = null;
            try {
                UserResponse user = userServiceClient.getUserById(reviewerUserId);
                reviewerUsername = user.getUsername();
                logger.info("   ✓ Resolved user ID {} to username: {}", reviewerUserId, reviewerUsername);
            } catch (Exception e) {
                logger.error("   ⚠ Failed to fetch username for reviewer ID {}: {}", reviewerUserId, e.getMessage());
                reviewerUsername = "User-" + reviewerUserId; // Fallback
            }

            // Create task using NEW API with context and workflow job key
            TaskServiceClient.CreateTaskRequest request = new TaskServiceClient.CreateTaskRequest();
            request.setTaskName("Review Change: " + changeTitle);
            request.setTaskDescription("Please review and approve/reject change '" + changeTitle + "' (ID: " + changeId + ") submitted by " + creator);
            request.setTaskType("REVIEW");
            request.setAssignedTo(reviewerUsername);
            request.setAssignedBy("WORKFLOW_ORCHESTRATOR");
            request.setContextType("CHANGE");
            request.setContextId(changeId);
            request.setWorkflowId(String.valueOf(job.getProcessInstanceKey()));
            request.setPriority(5);

            TaskServiceClient.TaskResponse task = taskServiceClient.createTaskWithContext(request);
            Long taskId = Long.parseLong(task.getId());

            logger.info("   ✓ Created change review task ID: {}", taskId);
            logger.info("   ✓ Task linked to CHANGE: {}", changeId);
            logger.info("   ✓ Task will be updated with workflow job key by wait-for-change-review worker");

            // Return variables with task ID for workflow tracking
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("taskCreated", true);
            return result;

        } catch (Exception e) {
            logger.error("   ❌ Failed to create change approval task: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("taskCreated", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    // NOTE: wait-for-change-review worker removed - replaced with Message Intermediate Catch Event
    // The task-service will send a "change-review-completed" message to the workflow when task is completed

    /**
     * Worker: update-change-status
     * Updates the change status in the change-service
     * Also triggers document update when change is APPROVED (RELEASED status)
     */
    @JobWorker(type = "update-change-status")
    public Map<String, Object> updateChangeStatus(final ActivatedJob job) {
        logger.info("🔧 Worker: update-change-status");
        logger.info("   Process Instance: {}", job.getProcessInstanceKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String newStatus = (String) variables.get("newStatus");
        String documentId = (String) variables.get("documentId");
        String creator = (String) variables.get("creator");

        logger.info("   Change ID: {}", changeId);
        logger.info("   New Status: {}", newStatus);
        logger.info("   Document ID: {}", documentId);

        try {
            // 1. Update change status
            if (changeServiceClient != null) {
                Map<String, String> statusUpdate = new HashMap<>();
                statusUpdate.put("status", newStatus);
                
                changeServiceClient.updateStatus(changeId, statusUpdate);
                logger.info("   ✓ Updated change {} to status: {}", changeId, newStatus);
            } else {
                logger.warn("   ⚠ ChangeServiceClient not available - skipping status update");
            }

            // 2. If change is APPROVED (RELEASED), update the document version
            if ("RELEASED".equals(newStatus) && documentId != null && documentServiceClient != null) {
                try {
                    logger.info("   📄 Change approved - initiating document version update...");
                    documentServiceClient.initiateChangeBasedEdit(documentId, changeId, creator);
                    logger.info("   ✓ Document {} updated: status → IN_WORK, version incremented", documentId);
                } catch (Exception e) {
                    logger.error("   ❌ Failed to update document version: {}", e.getMessage(), e);
                    // Don't fail the entire workflow if document update fails
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("statusUpdated", true);
            result.put("newStatus", newStatus);
            return result;

        } catch (Exception e) {
            logger.error("   ❌ Failed to update change status: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("statusUpdated", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Worker: notify-change-completion
     * Sends notification about change approval/rejection completion
     */
    @JobWorker(type = "notify-change-completion")
    public Map<String, Object> notifyChangeCompletion(final ActivatedJob job) {
        logger.info("🔧 Worker: notify-change-completion");
        logger.info("   Process Instance: {}", job.getProcessInstanceKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String changeId = (String) variables.get("changeId");
        String changeTitle = (String) variables.get("changeTitle");
        String creator = (String) variables.get("creator");
        Boolean approved = (Boolean) variables.get("approved");
        String newStatus = (String) variables.get("newStatus");

        logger.info("   Change ID: {}", changeId);
        logger.info("   Change Title: {}", changeTitle);
        logger.info("   Creator: {}", creator);
        logger.info("   Approved: {}", approved);
        logger.info("   New Status: {}", newStatus);

        try {
            // TODO: Implement actual notification logic
            // For now, just log the notification
            String message = approved != null && approved ?
                "Change '" + changeTitle + "' has been APPROVED" :
                "Change '" + changeTitle + "' has been REJECTED";

            logger.info("   📧 Notification: {}", message);
            logger.info("   📧 Recipient: {}", creator);

            Map<String, Object> result = new HashMap<>();
            result.put("notificationSent", true);
            result.put("message", message);
            return result;

        } catch (Exception e) {
            logger.error("   ❌ Failed to send notification: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("notificationSent", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Feign Client for Change Service
     */
    @FeignClient(name = "change-service")
    public interface ChangeServiceClient {
        @PutMapping("/api/changes/{id}/status")
        ResponseEntity<Void> updateStatus(@PathVariable("id") String id, @RequestBody Map<String, String> statusUpdate);
    }
}


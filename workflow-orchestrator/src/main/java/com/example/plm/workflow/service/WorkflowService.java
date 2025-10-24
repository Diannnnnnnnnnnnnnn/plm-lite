package com.example.plm.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.plm.workflow.client.TaskServiceClient;
import com.example.plm.workflow.client.UserServiceClient;
import com.example.plm.workflow.dto.UserResponse;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Service
public class WorkflowService {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private TaskServiceClient taskServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * Start Document Approval Workflow in Camunda
     * @param documentId The document UUID
     * @param masterId The document master ID (e.g., SPEC-001)
     * @param version The document version
     * @param creator The creator username
     * @param reviewerIds List of reviewer user IDs (legacy support)
     * @param initialReviewer The initial reviewer (for two-stage review)
     * @param technicalReviewer The technical reviewer (for two-stage review)
     * @return The Zeebe process instance key
     */
    public String startDocumentApprovalWorkflow(
            String documentId, 
            String masterId, 
            String version, 
            String creator, 
            List<String> reviewerIds,
            String initialReviewer,
            String technicalReviewer) {
        
        System.out.println("🚀 Starting document approval workflow for: " + documentId);
        System.out.println("   Master ID: " + masterId + ", Version: " + version);
        System.out.println("   Creator: " + creator);
        if (initialReviewer != null && technicalReviewer != null) {
            System.out.println("   Two-Stage Review Mode:");
            System.out.println("   Initial Reviewer: " + initialReviewer);
            System.out.println("   Technical Reviewer: " + technicalReviewer);
        } else {
            System.out.println("   Legacy Mode - Reviewers: " + reviewerIds);
        }

        try {
            // Prepare workflow variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentId", documentId);
            variables.put("masterId", masterId);
            variables.put("version", version);
            variables.put("creator", creator);
            // DON'T initialize 'approved' - let it be set by review completion
            // If we set it to false here, it might interfere with the decision gateway
            
            // NEW: Support both two-stage review and legacy mode
            String processId;
            if (initialReviewer != null && technicalReviewer != null) {
                // Two-stage review mode
                variables.put("initialReviewer", initialReviewer);
                variables.put("technicalReviewer", technicalReviewer);
                processId = "document-approval-with-review";
                System.out.println("   Using process: " + processId);
            } else {
                // Legacy mode
                variables.put("reviewerIds", reviewerIds);
                if (reviewerIds != null && !reviewerIds.isEmpty()) {
                    variables.put("reviewerId", reviewerIds.get(0));
                }
                processId = "document-approval";
                System.out.println("   Using process: " + processId);
            }

            // Start the workflow process
            ProcessInstanceEvent processInstance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            String processInstanceKey = String.valueOf(processInstance.getProcessInstanceKey());
            System.out.println("   ✓ Workflow started successfully!");
            System.out.println("   Process Instance Key: " + processInstanceKey);

            return processInstanceKey;

        } catch (Exception e) {
            System.err.println("❌ Failed to start document approval workflow: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start workflow", e);
        }
    }

    /**
     * Start Change Approval Workflow in Camunda
     */
    public String startChangeApprovalWorkflow(String changeId, String initiatorId) {
        System.out.println("🚀 Starting change approval workflow for: " + changeId);
        // TODO: Implement when ready
        return "dev-workflow-" + changeId;
    }

    /**
     * Complete a user task with variables
     */
    public void completeUserTask(long jobKey, Map<String, Object> variables) {
        System.out.println("✅ Completing user task: " + jobKey);
        System.out.println("   Variables: " + variables);
        System.out.println("   🔍 DEBUG - approved value: " + variables.get("approved") + " (type: " + (variables.get("approved") != null ? variables.get("approved").getClass().getName() : "null") + ")");

        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();
            
            System.out.println("   ✓ Task completed successfully with approved=" + variables.get("approved"));
        } catch (Exception e) {
            System.err.println("❌ Failed to complete task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to complete task", e);
        }
    }

    /**
     * Cancel a running process instance
     */
    public void cancelProcessInstance(long processInstanceKey) {
        System.out.println("🛑 Cancelling process instance: " + processInstanceKey);

        try {
            zeebeClient.newCancelInstanceCommand(processInstanceKey)
                    .send()
                    .join();
            
            System.out.println("   ✓ Process instance cancelled successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to cancel process instance: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to cancel process instance", e);
        }
    }

    public void createReviewTasks(String documentId, String masterId, String version, String creator, List<String> reviewers) {
        System.out.println("WorkflowServiceDev: Creating review tasks for document " + documentId + " with reviewers: " + reviewers);

        // Create a review task for each reviewer
        for (String reviewerId : reviewers) {
            try {
                // reviewerId is already a user ID, use it directly
                Long userId = Long.parseLong(reviewerId);

                // Fetch the username from user service to set assignedTo field
                String username = null;
                try {
                    UserResponse user = userServiceClient.getUserById(userId);
                    username = user.getUsername();
                    System.out.println("Resolved user ID " + userId + " to username: " + username);
                } catch (Exception e) {
                    System.err.println("Failed to fetch username for user ID " + userId + ": " + e.getMessage());
                    // Continue without username - task will be created with userId only
                }

                // Create task using form parameters (task-service uses old API)
                String taskName = "Review Document: " + masterId + " " + version + " [" + documentId + "]";
                String taskDescription = "Please review document '" + masterId + "' version " + version + " submitted by " + creator + ". Document ID: " + documentId;

                TaskServiceClient.TaskDTO response = taskServiceClient.createTask(
                        taskName,
                        taskDescription,
                        userId,
                        username // assignedTo
                );
                System.out.println("Created task for reviewer ID " + userId + " (username: " + username + "): " + response.getId());
            } catch (Exception e) {
                System.err.println("Failed to create task for reviewer " + reviewerId + ": " + e.getMessage());
                e.printStackTrace();
                // Continue with other reviewers even if one fails
            }
        }
    }
}

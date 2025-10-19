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
     * @param reviewerIds List of reviewer user IDs
     * @return The Zeebe process instance key
     */
    public String startDocumentApprovalWorkflow(
            String documentId, 
            String masterId, 
            String version, 
            String creator, 
            List<String> reviewerIds) {
        
        System.out.println("🚀 Starting document approval workflow for: " + documentId);
        System.out.println("   Master ID: " + masterId + ", Version: " + version);
        System.out.println("   Creator: " + creator + ", Reviewers: " + reviewerIds);

        try {
            // Prepare workflow variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("documentId", documentId);
            variables.put("masterId", masterId);
            variables.put("version", version);
            variables.put("creator", creator);
            variables.put("reviewerIds", reviewerIds);
            variables.put("approved", false); // Will be set by user task completion

            // Start the workflow process
            ProcessInstanceEvent processInstance = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("document-approval")
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

        try {
            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();
            
            System.out.println("   ✓ Task completed successfully");
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

                TaskServiceClient.TaskDTO response = taskServiceClient.createTask(taskName, taskDescription, userId);
                System.out.println("Created task for reviewer ID " + userId + " (username: " + username + "): " + response.getId());
            } catch (Exception e) {
                System.err.println("Failed to create task for reviewer " + reviewerId + ": " + e.getMessage());
                e.printStackTrace();
                // Continue with other reviewers even if one fails
            }
        }
    }
}

package com.example.plm.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.plm.workflow.service.WorkflowService;

/**
 * REST Controller for Camunda Workflow Operations
 * Handles workflow instance creation, task completion, and cancellation
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private io.camunda.zeebe.client.ZeebeClient zeebeClient;

    /**
     * Manually Deploy BPMN Workflows
     * Use this endpoint to deploy/redeploy BPMN files to Zeebe
     */
    @PostMapping("/deploy")
    public ResponseEntity<Map<String, Object>> deployWorkflows() {
        System.out.println("🔵 API: Manual BPMN deployment requested");
        
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            // Deploy document-approval.bpmn
            try {
                var deployment = zeebeClient.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/document-approval.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: document-approval.bpmn");
                result.put("document-approval", "SUCCESS");
                result.put("document-approval-key", deployment.getKey());
            } catch (Exception e) {
                System.err.println("   ✗ Failed to deploy document-approval.bpmn: " + e.getMessage());
                result.put("document-approval", "FAILED: " + e.getMessage());
            }

            // Deploy document-approval-with-review.bpmn (two-stage review)
            try {
                var deployment = zeebeClient.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/document-approval-with-review.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: document-approval-with-review.bpmn");
                result.put("document-approval-with-review", "SUCCESS");
                result.put("document-approval-with-review-key", deployment.getKey());
            } catch (Exception e) {
                System.err.println("   ✗ Failed to deploy document-approval-with-review.bpmn: " + e.getMessage());
                result.put("document-approval-with-review", "FAILED: " + e.getMessage());
            }

            // Deploy change-approval.bpmn
            try {
                var deployment = zeebeClient.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/change-approval.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: change-approval.bpmn");
                result.put("change-approval", "SUCCESS");
                result.put("change-approval-key", deployment.getKey());
            } catch (Exception e) {
                System.err.println("   ✗ Failed to deploy change-approval.bpmn: " + e.getMessage());
                result.put("change-approval", "FAILED: " + e.getMessage());
            }

            result.put("status", "COMPLETED");
            result.put("message", "Deployment completed. Check individual results.");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ API: Deployment failed: " + e.getMessage());
            e.printStackTrace();
            
            result.put("status", "ERROR");
            result.put("message", "Deployment failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Start Document Approval Workflow
     * Called by document-service when a document is submitted for review
     */
    @PostMapping("/document-approval/start")
    public ResponseEntity<Map<String, String>> startDocumentApprovalWorkflow(
            @RequestBody StartDocumentApprovalRequest request) {
        
        System.out.println("🔵 API: Starting document approval workflow");
        System.out.println("   Request: " + request);

        try {
            String processInstanceKey = workflowService.startDocumentApprovalWorkflow(
                request.getDocumentId(),
                request.getMasterId(),
                request.getVersion(),
                request.getCreator(),
                request.getReviewerIds(),
                request.getInitialReviewer(),
                request.getTechnicalReviewer()
            );

            Map<String, String> response = new HashMap<>();
            response.put("processInstanceKey", processInstanceKey);
            response.put("status", "STARTED");
            response.put("message", "Document approval workflow started successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API: Failed to start workflow: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to start workflow: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Complete a User Task
     * Called when a user completes a review/approval task
     */
    @PostMapping("/tasks/{jobKey}/complete")
    public ResponseEntity<Map<String, String>> completeUserTask(
            @PathVariable long jobKey,
            @RequestBody Map<String, Object> variables) {
        
        System.out.println("🔵 API: Completing user task: " + jobKey);
        System.out.println("   Variables: " + variables);

        try {
            workflowService.completeUserTask(jobKey, variables);

            Map<String, String> response = new HashMap<>();
            response.put("status", "COMPLETED");
            response.put("message", "Task completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API: Failed to complete task: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to complete task: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Cancel a Process Instance
     * Called when a workflow needs to be cancelled
     */
    @DeleteMapping("/instances/{processInstanceKey}")
    public ResponseEntity<Map<String, String>> cancelProcessInstance(
            @PathVariable long processInstanceKey) {
        
        System.out.println("🔵 API: Cancelling process instance: " + processInstanceKey);

        try {
            workflowService.cancelProcessInstance(processInstanceKey);

            Map<String, String> response = new HashMap<>();
            response.put("status", "CANCELLED");
            response.put("message", "Process instance cancelled successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API: Failed to cancel process instance: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to cancel process instance: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Start Change Approval Workflow (Future implementation)
     */
    @PostMapping("/change-approval/start")
    public ResponseEntity<Map<String, String>> startChangeApprovalWorkflow(
            @RequestBody Map<String, String> request) {
        
        String changeId = request.get("changeId");
        String initiatorId = request.get("initiatorId");

        String processInstanceKey = workflowService.startChangeApprovalWorkflow(changeId, initiatorId);

        Map<String, String> response = new HashMap<>();
        response.put("processInstanceKey", processInstanceKey);
        response.put("status", "STARTED");

        return ResponseEntity.ok(response);
    }

    /**
     * Legacy endpoint for backward compatibility
     * Redirects old document-service calls to new endpoint
     */
    @PostMapping("/workflow/reviews/start")
    public ResponseEntity<Void> startReviewProcessLegacy(
            @RequestParam("documentId") String documentId,
            @RequestParam("masterId") String masterId,
            @RequestParam("version") String version,
            @RequestParam("creator") String creator,
            @RequestBody List<String> reviewers) {
        
        System.out.println("🔵 Legacy API called - redirecting to new workflow endpoint");
        System.out.println("   Document ID: " + documentId);
        
        try {
            StartDocumentApprovalRequest request = new StartDocumentApprovalRequest();
            request.setDocumentId(documentId);
            request.setMasterId(masterId);
            request.setVersion(version);
            request.setCreator(creator);
            request.setReviewerIds(reviewers);
            
            String processInstanceKey = workflowService.startDocumentApprovalWorkflow(
                documentId, masterId, version, creator, reviewers, null, null
            );
            
            System.out.println("   ✓ Workflow started via legacy endpoint");
            System.out.println("   Process Instance Key: " + processInstanceKey);

        return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            System.err.println("❌ Legacy API error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

/**
 * DTO for starting document approval workflow
 */
class StartDocumentApprovalRequest {
    private String documentId;
    private String masterId;
    private String version;
    private String creator;
    private List<String> reviewerIds; // Legacy support
    
    // NEW: Two-stage review support
    private String initialReviewer;
    private String technicalReviewer;

    // Getters and setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public List<String> getReviewerIds() { return reviewerIds; }
    public void setReviewerIds(List<String> reviewerIds) { this.reviewerIds = reviewerIds; }
    
    public String getInitialReviewer() { return initialReviewer; }
    public void setInitialReviewer(String initialReviewer) { this.initialReviewer = initialReviewer; }
    public String getTechnicalReviewer() { return technicalReviewer; }
    public void setTechnicalReviewer(String technicalReviewer) { this.technicalReviewer = technicalReviewer; }

    @Override
    public String toString() {
        return "StartDocumentApprovalRequest{" +
                "documentId='" + documentId + '\'' +
                ", masterId='" + masterId + '\'' +
                ", version='" + version + '\'' +
                ", creator='" + creator + '\'' +
                ", reviewerIds=" + reviewerIds +
                ", initialReviewer='" + initialReviewer + '\'' +
                ", technicalReviewer='" + technicalReviewer + '\'' +
                '}';
    }
}
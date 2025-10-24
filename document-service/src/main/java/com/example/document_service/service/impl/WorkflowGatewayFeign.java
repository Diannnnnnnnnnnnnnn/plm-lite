package com.example.document_service.service.impl;

import com.example.document_service.client.WorkflowOrchestratorClient;
import com.example.document_service.dto.workflow.StartDocumentApprovalRequest;
import com.example.document_service.service.gateway.WorkflowGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Feign-based implementation of WorkflowGateway
 * Delegates workflow operations to Camunda via workflow-orchestrator service
 */
@Component
public class WorkflowGatewayFeign implements WorkflowGateway {

    private final WorkflowOrchestratorClient client;

    public WorkflowGatewayFeign(WorkflowOrchestratorClient client) {
        this.client = client;
    }

    @Override
    public void startReviewProcess(String documentId, String masterId, String version, String creator, List<String> reviewers) {
        System.out.println("🔵 WorkflowGateway: Starting Camunda document approval workflow");
        System.out.println("   Document ID: " + documentId);
        System.out.println("   Master ID: " + masterId);
        System.out.println("   Version: " + version);
        System.out.println("   Creator: " + creator);
        System.out.println("   Reviewers: " + reviewers);

        try {
            // Create request DTO
            StartDocumentApprovalRequest request = new StartDocumentApprovalRequest(
                documentId, masterId, version, creator, reviewers
            );

            // Call workflow-orchestrator to start Camunda workflow
            Map<String, String> response = client.startDocumentApprovalWorkflow(request);
            
            System.out.println("   ✓ Workflow started successfully!");
            System.out.println("   Process Instance Key: " + response.get("processInstanceKey"));
            System.out.println("   Status: " + response.get("status"));
            
        } catch (Exception e) {
            System.err.println("   ❌ Failed to start workflow: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start document approval workflow", e);
        }
    }

    @Override
    public void startTwoStageReviewProcess(String documentId, String masterId, String version, String creator, 
                                           String initialReviewer, String technicalReviewer) {
        System.out.println("🔵 WorkflowGateway: Starting TWO-STAGE document approval workflow");
        System.out.println("   Document ID: " + documentId);
        System.out.println("   Master ID: " + masterId);
        System.out.println("   Version: " + version);
        System.out.println("   Creator: " + creator);
        System.out.println("   Initial Reviewer: " + initialReviewer);
        System.out.println("   Technical Reviewer: " + technicalReviewer);

        try {
            // Create request DTO with two-stage review parameters
            StartDocumentApprovalRequest request = new StartDocumentApprovalRequest();
            request.setDocumentId(documentId);
            request.setMasterId(masterId);
            request.setVersion(version);
            request.setCreator(creator);
            request.setInitialReviewer(initialReviewer);
            request.setTechnicalReviewer(technicalReviewer);

            // Call workflow-orchestrator to start Camunda workflow
            Map<String, String> response = client.startDocumentApprovalWorkflow(request);
            
            System.out.println("   ✓ Two-stage workflow started successfully!");
            System.out.println("   Process Instance Key: " + response.get("processInstanceKey"));
            System.out.println("   Status: " + response.get("status"));
            
        } catch (Exception e) {
            System.err.println("   ❌ Failed to start two-stage workflow: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start document approval workflow", e);
        }
    }

    @Override
    public void notifyApprovalResult(String documentId, boolean approved, String approver, String comment) {
        System.out.println("🔵 WorkflowGateway: Notification for document: " + documentId);
        System.out.println("   Approved: " + approved + ", Approver: " + approver);
        // TODO: Implement task completion when integrated with frontend
        // This will be called when a user completes a review task
    }
}

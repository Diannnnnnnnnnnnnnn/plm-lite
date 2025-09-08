package com.example.document_service.service.impl;

import com.example.document_service.client.WorkflowOrchestratorClient;
import com.example.document_service.service.gateway.WorkflowGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkflowGatewayFeign implements WorkflowGateway {

    private final WorkflowOrchestratorClient client;

    public WorkflowGatewayFeign(WorkflowOrchestratorClient client) {
        this.client = client;
    }

    @Override
    public void startReviewProcess(String documentId, String masterId, String version, String creator, List<String> reviewers) {
        client.startReviewProcess(documentId, masterId, version, creator, reviewers);
    }

    @Override
    public void notifyApprovalResult(String documentId, boolean approved, String approver, String comment) {
        client.notifyApprovalResult(documentId, approved, approver, comment);
    }
}

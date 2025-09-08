package com.example.document_service.service.gateway;

import java.util.List;

public interface WorkflowGateway {
    void startReviewProcess(String documentId, String masterId, String version, String creator, List<String> reviewers);
    void notifyApprovalResult(String documentId, boolean approved, String approver, String comment);
}

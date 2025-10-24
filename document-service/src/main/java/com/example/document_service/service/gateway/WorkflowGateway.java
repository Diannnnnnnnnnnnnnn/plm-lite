package com.example.document_service.service.gateway;

import java.util.List;

public interface WorkflowGateway {
    void startReviewProcess(String documentId, String masterId, String version, String creator, List<String> reviewers);
    
    // NEW: Two-stage review support
    void startTwoStageReviewProcess(String documentId, String masterId, String version, String creator, 
                                    String initialReviewer, String technicalReviewer);
    
    void notifyApprovalResult(String documentId, boolean approved, String approver, String comment);
}

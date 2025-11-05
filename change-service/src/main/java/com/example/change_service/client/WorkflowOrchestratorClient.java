package com.example.change_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Feign Client for Camunda Workflow Orchestrator
 * Communicates with workflow-orchestrator service for Change approval workflows
 */
@FeignClient(name = "workflow-orchestrator", url = "http://localhost:8086")
public interface WorkflowOrchestratorClient {

    /**
     * Start Change Approval Workflow in Camunda
     * This will trigger the change-approval.bpmn process
     */
    @PostMapping("/api/workflows/change-approval/start")
    Map<String, String> startChangeApprovalWorkflow(@RequestBody StartChangeApprovalRequest request);

    /**
     * Complete a User Task in Camunda
     */
    @PostMapping("/api/workflows/tasks/{jobKey}/complete")
    Map<String, String> completeUserTask(@PathVariable("jobKey") long jobKey, 
                                        @RequestBody Map<String, Object> variables);

    /**
     * Cancel a Process Instance in Camunda
     */
    @DeleteMapping("/api/workflows/instances/{processInstanceKey}")
    Map<String, String> cancelProcessInstance(@PathVariable("processInstanceKey") long processInstanceKey);

    /**
     * DTO for starting change approval workflow
     */
    class StartChangeApprovalRequest {
        private String changeId;
        private String changeTitle;
        private String creator;
        private String reviewerId;
        private String documentId;

        public StartChangeApprovalRequest() {}

        public StartChangeApprovalRequest(String changeId, String changeTitle, String creator, String reviewerId, String documentId) {
            this.changeId = changeId;
            this.changeTitle = changeTitle;
            this.creator = creator;
            this.reviewerId = reviewerId;
            this.documentId = documentId;
        }

        // Getters and setters
        public String getChangeId() { return changeId; }
        public void setChangeId(String changeId) { this.changeId = changeId; }
        
        public String getChangeTitle() { return changeTitle; }
        public void setChangeTitle(String changeTitle) { this.changeTitle = changeTitle; }
        
        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }
        
        public String getReviewerId() { return reviewerId; }
        public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }
        
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

        @Override
        public String toString() {
            return "StartChangeApprovalRequest{" +
                    "changeId='" + changeId + '\'' +
                    ", changeTitle='" + changeTitle + '\'' +
                    ", creator='" + creator + '\'' +
                    ", reviewerId='" + reviewerId + '\'' +
                    ", documentId='" + documentId + '\'' +
                    '}';
        }
    }
}


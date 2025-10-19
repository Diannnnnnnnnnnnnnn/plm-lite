package com.example.document_service.client;

import com.example.document_service.dto.workflow.StartDocumentApprovalRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Feign Client for Camunda Workflow Orchestrator
 * Communicates with workflow-orchestrator service for Camunda workflow operations
 */
@FeignClient(name = "workflow-orchestrator", url = "http://localhost:8086")
public interface WorkflowOrchestratorClient {

    /**
     * Start Document Approval Workflow in Camunda
     * This will trigger the document-approval.bpmn process
     */
    @PostMapping("/api/workflows/document-approval/start")
    Map<String, String> startDocumentApprovalWorkflow(@RequestBody StartDocumentApprovalRequest request);

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
}

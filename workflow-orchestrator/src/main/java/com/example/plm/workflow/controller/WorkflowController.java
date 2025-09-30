package com.example.plm.workflow.controller;

import com.example.plm.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/document-approval")
    public ResponseEntity<Map<String, String>> startDocumentApproval(
            @RequestBody Map<String, String> request) {
        String documentId = request.get("documentId");
        String initiatorId = request.get("initiatorId");

        String processInstanceId = workflowService.startDocumentApprovalWorkflow(documentId, initiatorId);

        return ResponseEntity.ok(Map.of("processInstanceId", processInstanceId));
    }

    // Document service endpoints
    @PostMapping("/workflow/reviews/start")
    public ResponseEntity<Void> startReviewProcess(@RequestParam("documentId") String documentId,
                                                   @RequestParam("masterId") String masterId,
                                                   @RequestParam("version") String version,
                                                   @RequestParam("creator") String creator,
                                                   @RequestBody List<String> reviewers) {
        System.out.println("Starting review process for document: " + documentId);
        System.out.println("Master ID: " + masterId + ", Version: " + version + ", Creator: " + creator);
        System.out.println("Reviewers: " + reviewers);

        // Start the workflow process
        String processInstanceId = workflowService.startDocumentApprovalWorkflow(documentId, creator);
        System.out.println("Started workflow process: " + processInstanceId);

        // Create review tasks for each reviewer
        workflowService.createReviewTasks(documentId, masterId, version, creator, reviewers);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/workflow/reviews/notify")
    public ResponseEntity<Void> notifyApprovalResult(@RequestParam("documentId") String documentId,
                                                     @RequestParam("approved") boolean approved,
                                                     @RequestParam("approver") String approver,
                                                     @RequestParam("comment") String comment) {
        System.out.println("Notification for document: " + documentId);
        System.out.println("Approved: " + approved + ", Approver: " + approver + ", Comment: " + comment);

        // In a real implementation, this would complete workflow tasks
        // For now, just log the notification

        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-approval")
    public ResponseEntity<Map<String, String>> startChangeApproval(
            @RequestBody Map<String, String> request) {
        String changeId = request.get("changeId");
        String initiatorId = request.get("initiatorId");

        String processInstanceId = workflowService.startChangeApprovalWorkflow(changeId, initiatorId);

        return ResponseEntity.ok(Map.of("processInstanceId", processInstanceId));
    }

    @PostMapping("/complete-task")
    public ResponseEntity<Void> completeTask(@RequestBody Map<String, Object> request) {
        String jobKey = (String) request.get("jobKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");

        workflowService.completeTask(jobKey, variables);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{processInstanceId}")
    public ResponseEntity<Void> cancelProcess(@PathVariable String processInstanceId) {
        workflowService.cancelProcessInstance(processInstanceId);
        return ResponseEntity.ok().build();
    }
}
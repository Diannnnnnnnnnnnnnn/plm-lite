package com.example.document_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "workflow-orchestrator")
public interface WorkflowOrchestratorClient {

    @PostMapping("/workflow/reviews/start")
    void startReviewProcess(@RequestParam("documentId") String documentId,
                     @RequestParam("masterId") String masterId,
                     @RequestParam("version") String version,
                     @RequestParam("creator") String creator,
                     @RequestBody List<String> reviewers);

    @PostMapping("/workflow/reviews/notify")
    void notifyApprovalResult(@RequestParam("documentId") String documentId,
                        @RequestParam("approved") boolean approved,
                        @RequestParam("approver") String approver,
                        @RequestParam("comment") String comment);
}

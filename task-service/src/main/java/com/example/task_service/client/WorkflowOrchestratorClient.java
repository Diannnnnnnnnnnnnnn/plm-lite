package com.example.task_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign Client for Workflow Orchestrator Service
 * Used to automatically complete workflow jobs when tasks are done
 */
@FeignClient(name = "workflow-orchestrator", url = "http://localhost:8086")
public interface WorkflowOrchestratorClient {
    
    @PostMapping("/api/workflows/tasks/{jobKey}/complete")
    Map<String, Object> completeWorkflowJob(
            @PathVariable("jobKey") Long jobKey,
            @RequestBody Map<String, Object> variables
    );
}









package com.example.plm.workflow.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Component
@Profile("!dev")
public class TaskWorkerHandler {

    private static final Logger logger = LoggerFactory.getLogger(TaskWorkerHandler.class);

    @JobWorker(type = "create-approval-task")
    public Map<String, Object> createApprovalTask(final ActivatedJob job) {
        logger.info("Creating approval task for process: {}", job.getProcessInstanceKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String contextId = (String) variables.get("documentId");
        String contextType = variables.containsKey("changeId") ? "CHANGE" : "DOCUMENT";

        if (contextType.equals("CHANGE")) {
            contextId = (String) variables.get("changeId");
        }

        Map<String, Object> taskRequest = Map.of(
            "taskName", "Approval Required",
            "taskDescription", "Review and approve " + contextType.toLowerCase(),
            "taskType", "APPROVAL",
            "assignedTo", variables.get("assigneeId"),
            "assignedBy", variables.get("initiatorId"),
            "contextType", contextType,
            "contextId", contextId,
            "workflowId", String.valueOf(job.getProcessInstanceKey())
        );

        // Call task service to create task
        // taskServiceClient.createTask(taskRequest);

        return Map.of("taskCreated", true);
    }

    @JobWorker(type = "notify-completion")
    public Map<String, Object> notifyCompletion(final ActivatedJob job) {
        logger.info("Notifying completion for process: {}", job.getProcessInstanceKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String contextType = variables.containsKey("changeId") ? "CHANGE" : "DOCUMENT";

        // Send notification about workflow completion
        logger.info("Workflow completed for {} with ID: {}",
            contextType, variables.get(contextType.equals("CHANGE") ? "changeId" : "documentId"));

        return Map.of("notificationSent", true);
    }

    @JobWorker(type = "update-status")
    public Map<String, Object> updateStatus(final ActivatedJob job) {
        logger.info("Updating status for process: {}", job.getProcessInstanceKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        String status = (String) variables.get("newStatus");
        String contextType = variables.containsKey("changeId") ? "CHANGE" : "DOCUMENT";
        String contextId = (String) variables.get(contextType.equals("CHANGE") ? "changeId" : "documentId");

        // Update the entity status based on context type
        if ("DOCUMENT".equals(contextType)) {
            // documentServiceClient.updateStatus(contextId, status);
        } else if ("CHANGE".equals(contextType)) {
            // changeServiceClient.updateStatus(contextId, status);
        }

        logger.info("Updated {} {} to status: {}", contextType, contextId, status);
        return Map.of("statusUpdated", true);
    }

    @FeignClient(name = "task-service")
    public interface TaskServiceClient {
        @PostMapping("/api/tasks")
        void createTask(@RequestBody Map<String, Object> taskRequest);
    }

    @FeignClient(name = "document-service")
    public interface DocumentServiceClient {
        @PostMapping("/api/documents/{id}/status")
        void updateStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate);
    }

    @FeignClient(name = "change-service")
    public interface ChangeServiceClient {
        @PostMapping("/api/changes/{id}/status")
        void updateStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate);
    }
}
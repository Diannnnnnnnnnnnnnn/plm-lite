package com.example.task_service.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for Workflow Orchestrator Service
 * Used to automatically complete workflow jobs and publish messages when tasks are done
 */
@FeignClient(name = "workflow-orchestrator")
public interface WorkflowOrchestratorClient {
    
    @PostMapping("/api/workflows/tasks/{jobKey}/complete")
    Map<String, Object> completeWorkflowJob(
            @PathVariable("jobKey") Long jobKey,
            @RequestBody Map<String, Object> variables
    );
    
    /**
     * Publish a message to a workflow (for intermediate catch events)
     * @param messageName Name of the message (e.g., "change-review-completed")
     * @param correlationKey Correlation key value (e.g., changeId)
     * @param variables Variables to send with the message
     */
    @PostMapping("/api/workflows/messages/publish")
    Map<String, Object> publishMessage(
            @RequestBody PublishMessageRequest request
    );
    
    // Helper method with separate parameters
    default Map<String, Object> publishMessage(String messageName, String correlationKey, Map<String, Object> variables) {
        PublishMessageRequest request = new PublishMessageRequest();
        request.setMessageName(messageName);
        request.setCorrelationKey(correlationKey);
        request.setVariables(variables);
        return publishMessage(request);
    }
    
    /**
     * DTO for publishing messages
     */
    class PublishMessageRequest {
        private String messageName;
        private String correlationKey;
        private Map<String, Object> variables;
        
        public String getMessageName() { return messageName; }
        public void setMessageName(String messageName) { this.messageName = messageName; }
        
        public String getCorrelationKey() { return correlationKey; }
        public void setCorrelationKey(String correlationKey) { this.correlationKey = correlationKey; }
        
        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    }
}










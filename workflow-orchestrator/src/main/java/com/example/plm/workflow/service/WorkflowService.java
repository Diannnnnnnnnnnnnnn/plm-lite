package com.example.plm.workflow.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("!dev")
public class WorkflowService {

    @Autowired
    private ZeebeClient zeebeClient;

    public String startDocumentApprovalWorkflow(String documentId, String initiatorId) {
        ProcessInstanceEvent processInstance = zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("document-approval")
            .latestVersion()
            .variables(Map.of(
                "documentId", documentId,
                "initiatorId", initiatorId
            ))
            .send()
            .join();

        return String.valueOf(processInstance.getProcessInstanceKey());
    }

    public String startChangeApprovalWorkflow(String changeId, String initiatorId) {
        ProcessInstanceEvent processInstance = zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("change-approval")
            .latestVersion()
            .variables(Map.of(
                "changeId", changeId,
                "initiatorId", initiatorId
            ))
            .send()
            .join();

        return String.valueOf(processInstance.getProcessInstanceKey());
    }

    public void completeTask(String jobKey, Map<String, Object> variables) {
        zeebeClient
            .newCompleteCommand(Long.parseLong(jobKey))
            .variables(variables)
            .send()
            .join();
    }

    public void cancelProcessInstance(String processInstanceKey) {
        zeebeClient
            .newCancelInstanceCommand(Long.parseLong(processInstanceKey))
            .send()
            .join();
    }
}

@Service
@Profile("dev")
class WorkflowServiceDev {

    public String startDocumentApprovalWorkflow(String documentId, String initiatorId) {
        return "dev-workflow-" + documentId;
    }

    public String startChangeApprovalWorkflow(String changeId, String initiatorId) {
        return "dev-workflow-" + changeId;
    }

    public void completeTask(String jobKey, Map<String, Object> variables) {
        // No-op in dev mode
    }

    public void cancelProcessInstance(String processInstanceKey) {
        // No-op in dev mode
    }
}
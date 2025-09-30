package com.example.plm.workflow.service;

// Camunda imports commented out for simplified dev mode
// import io.camunda.zeebe.client.ZeebeClient;
// import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

// Commented out production Camunda-based WorkflowService for simplified dev mode
/*
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
*/

@Service
public class WorkflowService {

    @Autowired
    private TaskServiceClient taskServiceClient;

    public String startDocumentApprovalWorkflow(String documentId, String initiatorId) {
        System.out.println("WorkflowServiceDev: Starting document approval workflow for " + documentId);
        return "dev-workflow-" + documentId;
    }

    public String startChangeApprovalWorkflow(String changeId, String initiatorId) {
        System.out.println("WorkflowServiceDev: Starting change approval workflow for " + changeId);
        return "dev-workflow-" + changeId;
    }

    public void completeTask(String jobKey, Map<String, Object> variables) {
        System.out.println("WorkflowServiceDev: Completing task " + jobKey + " with variables: " + variables);
    }

    public void cancelProcessInstance(String processInstanceKey) {
        System.out.println("WorkflowServiceDev: Cancelling process instance " + processInstanceKey);
    }

    public void createReviewTasks(String documentId, String masterId, String version, String creator, List<String> reviewers) {
        System.out.println("WorkflowServiceDev: Creating review tasks for document " + documentId + " with reviewers: " + reviewers);

        // Create a review task for each reviewer
        for (String reviewer : reviewers) {
            try {
                CreateTaskRequest taskRequest = new CreateTaskRequest();
                taskRequest.setTaskName("Review Document: " + masterId + " " + version);
                taskRequest.setTaskDescription("Please review document '" + masterId + "' version " + version + " submitted by " + creator);
                taskRequest.setTaskType(TaskType.REVIEW);
                taskRequest.setAssignedTo(reviewer);
                taskRequest.setAssignedBy(creator);
                taskRequest.setPriority(1);
                taskRequest.setContextType("DOCUMENT");
                taskRequest.setContextId(documentId);

                TaskResponse response = taskServiceClient.createTask(taskRequest);
                System.out.println("Created task for reviewer " + reviewer + ": " + response.getId());
            } catch (Exception e) {
                System.err.println("Failed to create task for reviewer " + reviewer + ": " + e.getMessage());
                // Continue with other reviewers even if one fails
            }
        }
    }
}

@FeignClient(name = "task-service", url = "http://localhost:8080")
interface TaskServiceClient {
    @PostMapping("/api/tasks")
    TaskResponse createTask(@RequestBody CreateTaskRequest request);
}

class CreateTaskRequest {
    private String taskName;
    private String taskDescription;
    private TaskType taskType;
    private String assignedTo;
    private String assignedBy;
    private Integer priority;
    private String contextType;
    private String contextId;

    // Getters and setters
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }
    public String getContextId() { return contextId; }
    public void setContextId(String contextId) { this.contextId = contextId; }
}

class TaskResponse {
    private String id;
    private String taskName;
    private String taskDescription;
    private TaskType taskType;
    private String assignedTo;
    private String assignedBy;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}

enum TaskType {
    APPROVAL, REVIEW, NOTIFICATION, ACTION, WORKFLOW
}
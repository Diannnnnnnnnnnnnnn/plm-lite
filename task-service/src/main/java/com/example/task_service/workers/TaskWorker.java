package com.example.task_service.workers;

import com.example.task_service.Task;
import com.example.task_service.TaskService;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskWorker {

    @Autowired
    private TaskService taskService;

    @JobWorker(type = "create-task")
    public void handleCreateTask(JobClient client, ActivatedJob job) {
        Map<String, Object> vars = job.getVariablesAsMap();

        String name = (String) vars.get("name");
        String description = (String) vars.get("description");
        Long userId = Long.valueOf(vars.get("userId").toString());

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setUserId(userId);

        taskService.addTask(task);

        client.newCompleteCommand(job.getKey()).send().join();
    }

    @JobWorker(type = "publish-task")
    public void handlePublishTask(JobClient client, ActivatedJob job) {
        Map<String, Object> vars = job.getVariablesAsMap();
        System.out.println("Publishing task: " + vars.get("name"));

        // Add logic to notify or update external systems if needed

        client.newCompleteCommand(job.getKey()).send().join();
    }
}

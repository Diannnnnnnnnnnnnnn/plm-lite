package com.example.task_service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.camunda.zeebe.client.ZeebeClient;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ZeebeClient zeebeClient;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Optional<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        return taskService.addTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Long userId,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setUserId(userId);

        List<MultipartFile> fileList = (files != null) ? List.of(files) : null;

        Task createdTask = taskService.addTask(task, fileList);
        return ResponseEntity.ok(createdTask);
    }
    
    // This is for the workflow
    @Value("${camunda.process.key:task-process}")
    private String processKey;

    // Step 2: Start Process after Task is created
    @PostMapping("/start-process")
    public ResponseEntity<String> startTaskProcess(@RequestParam Long taskId) {
        Optional<Task> optionalTask = taskService.getTaskById(taskId);

        if (optionalTask.isEmpty()) {
            return ResponseEntity.badRequest().body("Task with ID " + taskId + " not found");
        }

        Task task = optionalTask.get();

        Map<String, Object> variables = Map.of(
                "taskId", task.getId(),
                "name", task.getName(),
                "description", task.getDescription(),
                "userId", task.getUserId()
        );

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(processKey)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        return ResponseEntity.ok("Process started for task ID: " + task.getId());
    }


}

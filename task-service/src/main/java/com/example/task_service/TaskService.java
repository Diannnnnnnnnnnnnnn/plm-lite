package com.example.task_service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.task_service.client.FileStorageClient;
import com.example.task_service.client.GraphClient;
import com.example.task_service.elasticsearch.TaskDocument;
import com.example.task_service.elasticsearch.TaskSearchRepository;
import com.example.task_service.messaging.TaskMessageProducer;
import com.example.task_service.model.FileMetadata;
import com.example.task_service.repository.FileMetadataRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserClient userClient; // Inject Feign Client

    @Autowired(required = false)
    private TaskSearchRepository taskSearchRepository;

    @Autowired(required = false)
    private GraphClient graphClient;

    @Autowired(required = false)
    private FileStorageClient fileStorageClient;

    @Autowired(required = false)
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private TaskMessageProducer taskMessageProducer;

    public Task addTask(Task task, List<MultipartFile> files) {
        // Prefer assignedTo if provided (from orchestrator); otherwise, try to resolve via user-service
        if (task.getAssignedTo() == null || task.getAssignedTo().isBlank()) {
            try {
                User user = userClient.getUserById(task.getUserId());
                if (user == null) {
                    System.err.println("⚠ Warning: User not found with ID: " + task.getUserId() + ", but continuing task creation");
                } else {
                    // Set assignedTo field with username for query compatibility
                    task.setAssignedTo(user.getUsername());
                    System.out.println("✓ Assigned task to user: " + user.getUsername() + " (ID: " + task.getUserId() + ")");
                }
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to validate user ID " + task.getUserId() + ": " + e.getMessage());
                System.err.println("   Continuing with task creation anyway...");
            }
        }

        // First save to DB to generate ID
        Task savedTask = taskRepository.save(task);

        // After saving task and before returning (fault-tolerant)
        try {
            taskMessageProducer.sendTaskCreatedMessage(String.valueOf(savedTask.getId()));
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to send RabbitMQ message for task " + savedTask.getId() + ": " + e.getMessage());
        }
        
        // Upload and associate files if provided
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String fileUrl = fileStorageClient.uploadFile(file);
                    FileMetadata metadata = new FileMetadata();
                    metadata.setFilename(file.getOriginalFilename());
                    metadata.setFileUrl(fileUrl);
                    metadata.setTask(savedTask);
                    fileMetadataRepository.save(metadata);
                } catch (Exception e) {
                    System.err.println("⚠ Warning: Failed to upload file " + file.getOriginalFilename() + ": " + e.getMessage());
                }
            }
        }
        // Sync with Elasticsearch (if available)
        if (taskSearchRepository != null) {
            try {
                taskSearchRepository.save(new TaskDocument(
                    savedTask.getId(), savedTask.getName(), savedTask.getDescription(), savedTask.getUserId()
                ));
            } catch (Exception e) {
                System.err.println("⚠ Warning: Failed to index task in Elasticsearch: " + e.getMessage());
            }
        }

        // Send to graph service (fault-tolerant)
        try {
            graphClient.createTask(String.valueOf(savedTask.getId()), savedTask.getName());
            graphClient.assignTask(String.valueOf(savedTask.getUserId()), String.valueOf(savedTask.getId()));
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to sync task with graph service: " + e.getMessage());
        }

        return savedTask;
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    public List<Task> getAllTasks() {         
       return taskRepository.findAll();
    }     

    public Task updateTask(Long id, Task task) {
        Optional<Task> existingOpt = taskRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null; // Handle non-existent ID scenario
        }

        Task existing = existingOpt.get();

        if (task.getName() != null) existing.setName(task.getName());
        if (task.getDescription() != null) existing.setDescription(task.getDescription());
        if (task.getUserId() != null) existing.setUserId(task.getUserId());
        if (task.getAssignedTo() != null && !task.getAssignedTo().isBlank()) existing.setAssignedTo(task.getAssignedTo());
        if (task.getTaskStatus() != null) existing.setTaskStatus(task.getTaskStatus());
        if (task.getDueDate() != null) existing.setDueDate(task.getDueDate());
        if (task.getWorkflowJobKey() != null) existing.setWorkflowJobKey(task.getWorkflowJobKey());

        // createdAt is immutable once set

        return taskRepository.save(existing);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task addTask(Task task) {
        return addTask(task, List.of()); // delegate to the main method with an empty file list
    }

    public List<Task> getTasksByAssignedTo(String assignedTo) {
        return taskRepository.findByAssignedTo(assignedTo);
    }
 
}


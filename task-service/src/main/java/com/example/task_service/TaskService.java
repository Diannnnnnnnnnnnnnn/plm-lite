package com.example.task_service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @Autowired
    private GraphClient graphClient;

    @Autowired
    private FileStorageClient fileStorageClient;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private TaskMessageProducer taskMessageProducer;

    public Task addTask(Task task, List<MultipartFile> files) {
        // Validate if user exists in user-service
        User user = userClient.getUserById(task.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + task.getUserId());
        }

        // First save to DB to generate ID
        Task savedTask = taskRepository.save(task);

        // After saving task and before returning
        taskMessageProducer.sendTaskCreatedMessage(String.valueOf(savedTask.getId()));
        
        // Upload and associate files if provided
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileUrl = fileStorageClient.uploadFile(file);
                FileMetadata metadata = new FileMetadata();
                metadata.setFilename(file.getOriginalFilename());
                metadata.setFileUrl(fileUrl);
                metadata.setTask(savedTask);
                fileMetadataRepository.save(metadata);
            }
        }
        // Sync with Elasticsearch (if available)
        if (taskSearchRepository != null) {
            taskSearchRepository.save(new TaskDocument(
                savedTask.getId(), savedTask.getName(), savedTask.getDescription(), savedTask.getUserId()
            ));
        }

        // Send to graph service
        graphClient.createTask(String.valueOf(savedTask.getId()), savedTask.getName());
        graphClient.assignTask(String.valueOf(savedTask.getUserId()), String.valueOf(savedTask.getId()));

        return savedTask;
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    public List<Task> getAllTasks() {         
       return taskRepository.findAll();
    }     

    public Task updateTask(Long id, Task task) {
        if (taskRepository.existsById(id)) {
            task.setId(id);
            return taskRepository.save(task);
        } else {
            return null; // Handle non-existent ID scenario
        }
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task addTask(Task task) {
        return addTask(task, List.of()); // delegate to the main method with an empty file list
    }    
 
}


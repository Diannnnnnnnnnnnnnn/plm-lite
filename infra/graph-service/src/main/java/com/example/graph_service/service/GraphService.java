package com.example.graph_service.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.graph_service.model.TaskNode;
import com.example.graph_service.model.UserNode;
import com.example.graph_service.repository.TaskNodeRepository;
import com.example.graph_service.repository.UserNodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserNodeRepository userRepo;
    private final TaskNodeRepository taskRepo;

    /**
     * Create a user node in the graph
     * @param id User ID
     * @param name User name (username)
     */
    public void createUser(String id, String name) {
        UserNode user = new UserNode();
        user.setId(id);
        user.setUsername(name);
        user.setEmail(name + "@example.com"); // Default email
        user.setAssignedTasks(new ArrayList<>());
        userRepo.save(user);
    }

    /**
     * Create a task node in the graph
     * @param id Task ID
     * @param title Task title
     */
    public void createTask(String id, String title) {
        taskRepo.save(new TaskNode(id, title));
    }

    /**
     * Assign a task to a user (create ASSIGNED_TO relationship)
     * @param userId User ID
     * @param taskId Task ID
     */
    public void assignTaskToUser(String userId, String taskId) {
        UserNode user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        TaskNode task = taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        // Create the relationship: User -[ASSIGNED_TO]-> Task
        if (user.getAssignedTasks() == null) {
            user.setAssignedTasks(new ArrayList<>());
        }
        user.getAssignedTasks().add(task);
        
        // Save the user which will persist the relationship
        userRepo.save(user);
    }
}

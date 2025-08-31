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

    public void createUser(String id, String name) {
        userRepo.save(new UserNode(id, name, new ArrayList<>()));
    }

    public void createTask(String id, String title) {
        taskRepo.save(new TaskNode(id, title, null));
    }

    public void assignTaskToUser(String userId, String taskId) {
        UserNode user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        TaskNode task = taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        user.getTasks().add(task);
        userRepo.save(user); // This saves the relationship in Neo4j
    }
}

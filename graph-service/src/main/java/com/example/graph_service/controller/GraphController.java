package com.example.graph_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.graph_service.service.GraphService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @PostMapping("/user")
    public ResponseEntity<String> createUser(@RequestParam String id, @RequestParam String name) {
        graphService.createUser(id, name);
        return ResponseEntity.ok("User created");
    }

    @PostMapping("/task")
    public ResponseEntity<String> createTask(@RequestParam String id, @RequestParam String title) {
        graphService.createTask(id, title);
        return ResponseEntity.ok("Task created");
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignTask(@RequestParam String userId, @RequestParam String taskId) {
        graphService.assignTaskToUser(userId, taskId);
        return ResponseEntity.ok("Task assigned to user");
    }
}

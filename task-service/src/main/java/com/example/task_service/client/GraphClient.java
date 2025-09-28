package com.example.task_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "graph-service")
public interface GraphClient {

    @PostMapping("/user")
    void createUser(@RequestParam String id, @RequestParam String name);

    @PostMapping("/task")
    void createTask(@RequestParam String id, @RequestParam String title);

    @PostMapping("/assign")
    void assignTask(@RequestParam String userId, @RequestParam String taskId);
}

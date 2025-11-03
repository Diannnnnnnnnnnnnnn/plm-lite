package com.example.task_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.task_service.elasticsearch.TaskDocument;
import com.example.task_service.elasticsearch.TaskSearchRepository;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
@Profile("!dev")
public class TaskSearchController {

    @Autowired(required = false)
    private TaskSearchRepository taskSearchRepository;

    @GetMapping
    public List<TaskDocument> searchTasks(@RequestParam("keyword") String keyword) {
        if (taskSearchRepository == null) {
            return List.of(); // Return empty list if Elasticsearch is not available
        }
        return taskSearchRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }
}

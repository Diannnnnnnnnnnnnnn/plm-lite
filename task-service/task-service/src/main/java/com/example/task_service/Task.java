package com.example.task_service;

import java.io.Serializable;  // Import Serializable interface
import java.util.ArrayList;
import java.util.List;

import com.example.task_service.model.FileMetadata;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity // JPA annotation to mark this as an entity for MySQL
@Table(name = "tasks") // Optional: maps to the 'tasks' table in MySQL (default name will be lowercase 'tasks')
public class Task implements Serializable { // Implement Serializable interface
    private static final long serialVersionUID = 1L; // Add serialVersionUID for versioning

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID in MySQL
    private Long id; // Auto-increment ID field

    private String name;
    private String description;
    private Long userId; // Field to associate the task with a user
    
    //code for the relation of task and file
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<FileMetadata> files = new ArrayList<>();

    // Constructors
    public Task() {
    }

    public Task(String name, String description, Long userId) {
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
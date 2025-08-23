package com.example.task_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.task_service.client.FileStorageClient;

@RestController
@RequestMapping("/tasks/files")
public class TaskFileController {

    private final FileStorageClient fileStorageClient;

    public TaskFileController(FileStorageClient fileStorageClient) {
        this.fileStorageClient = fileStorageClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String response = fileStorageClient.uploadFile(file);
        return ResponseEntity.ok("File uploaded: " + response);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        return fileStorageClient.downloadFile(filename);
    }
}

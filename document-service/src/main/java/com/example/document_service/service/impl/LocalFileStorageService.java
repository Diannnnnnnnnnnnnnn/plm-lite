package com.example.document_service.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService {

    private final String UPLOAD_DIR = "temp-uploads";

    public LocalFileStorageService() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
        }
    }

    public String saveFile(String filename, MultipartFile file) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.copy(file.getInputStream(), filePath);
            System.out.println("INFO: File saved locally: " + filePath.toAbsolutePath());
            return filename;
        } catch (IOException e) {
            System.err.println("Failed to save file: " + e.getMessage());
            return "error-saving-file";
        }
    }

    public byte[] getFile(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + e.getMessage());
            return new byte[0];
        }
    }
}
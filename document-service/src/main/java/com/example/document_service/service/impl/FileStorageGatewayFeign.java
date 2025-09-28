package com.example.document_service.service.impl;

import com.example.document_service.client.FileStorageClient;
import com.example.document_service.service.gateway.FileStorageGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageGatewayFeign implements FileStorageGateway {

    private final MinIOFileStorageService minIOFileStorageService;
    private final LocalFileStorageService localFileStorageService;
    private final FileStorageClient client;

    public FileStorageGatewayFeign(MinIOFileStorageService minIOFileStorageService,
                                   LocalFileStorageService localFileStorageService,
                                   FileStorageClient client) {
        this.minIOFileStorageService = minIOFileStorageService;
        this.localFileStorageService = localFileStorageService;
        this.client = client;
    }

    @Override
    public String upload(String documentId, MultipartFile file) {
        // Get original filename and ensure proper UTF-8 encoding
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "unnamed_file";
        }

        // Ensure proper encoding of the filename
        try {
            // Re-encode to ensure proper UTF-8 handling
            originalFilename = new String(originalFilename.getBytes("ISO-8859-1"), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            System.out.println("WARN: Failed to re-encode filename, using original: " + e.getMessage());
        }

        String filename = documentId + "_" + originalFilename;
        System.out.println("INFO: Processing upload with filename: " + filename);

        // Try MinIO first
        String savedFilename = minIOFileStorageService.saveFile(filename, file);
        if (!"error-saving-file".equals(savedFilename)) {
            System.out.println("INFO: File uploaded to MinIO storage: " + savedFilename);
            return savedFilename;
        }

        // Fallback to local storage
        System.out.println("WARN: MinIO failed, falling back to local storage");
        savedFilename = localFileStorageService.saveFile(filename, file);
        System.out.println("INFO: File uploaded to local storage: " + savedFilename);
        return savedFilename;
    }

    @Override
    public byte[] download(String fileKey) {
        // Try MinIO first
        try {
            byte[] data = minIOFileStorageService.getFile(fileKey);
            if (data.length > 0) {
                System.out.println("INFO: File downloaded from MinIO storage: " + fileKey);
                return data;
            }
        } catch (Exception e) {
            System.out.println("WARN: MinIO download failed: " + e.getMessage());
        }

        // Fallback to local storage
        System.out.println("WARN: MinIO failed, trying local storage for: " + fileKey);
        byte[] data = localFileStorageService.getFile(fileKey);
        if (data.length > 0) {
            System.out.println("INFO: File downloaded from local storage: " + fileKey);
        } else {
            System.out.println("ERROR: File not found in local storage: " + fileKey);
        }
        return data;
    }
}

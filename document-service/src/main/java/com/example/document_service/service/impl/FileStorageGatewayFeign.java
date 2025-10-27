package com.example.document_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.document_service.client.FileStorageClient;
import com.example.document_service.service.gateway.FileStorageGateway;

@Component
public class FileStorageGatewayFeign implements FileStorageGateway {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageGatewayFeign.class);

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
        logger.info("Processing file upload - Filename: {}", filename);

        // Try MinIO first
        String savedFilename = minIOFileStorageService.saveFile(filename, file);
        if (!"error-saving-file".equals(savedFilename)) {
            logger.info("File uploaded to MinIO storage: {}", savedFilename);
            return savedFilename;
        }

        // Fallback to local storage
        logger.warn("MinIO failed, falling back to local storage for: {}", filename);
        savedFilename = localFileStorageService.saveFile(filename, file);
        logger.info("File uploaded to local storage: {}", savedFilename);
        return savedFilename;
    }

    @Override
    public byte[] download(String fileKey) {
        // Try MinIO first
        try {
            byte[] data = minIOFileStorageService.getFile(fileKey);
            if (data.length > 0) {
                logger.info("File downloaded from MinIO storage: {}", fileKey);
                return data;
            }
        } catch (Exception e) {
            logger.warn("MinIO download failed: {}", e.getMessage());
        }

        // Fallback to local storage
        logger.warn("Trying local storage for file: {}", fileKey);
        byte[] data = localFileStorageService.getFile(fileKey);
        if (data.length > 0) {
            logger.info("File downloaded from local storage: {}", fileKey);
        } else {
            logger.error("File not found in any storage: {}", fileKey);
        }
        return data;
    }

    @Override
    public boolean delete(String fileKey) {
        logger.info("Attempting to delete file: {}", fileKey);
        
        // Try MinIO first
        boolean deletedFromMinio = minIOFileStorageService.deleteFile(fileKey);
        if (deletedFromMinio) {
            logger.info("File deleted from MinIO storage: {}", fileKey);
            return true;
        }

        // Fallback to local storage
        logger.warn("Trying to delete from local storage: {}", fileKey);
        boolean deletedFromLocal = localFileStorageService.deleteFile(fileKey);
        if (deletedFromLocal) {
            logger.info("File deleted from local storage: {}", fileKey);
            return true;
        }

        logger.error("Failed to delete file from any storage: {}", fileKey);
        return false;
    }

    @Override
    public boolean exists(String fileKey) {
        // Check MinIO first
        if (minIOFileStorageService.fileExists(fileKey)) {
            logger.debug("File exists in MinIO: {}", fileKey);
            return true;
        }

        // Check local storage
        boolean existsInLocal = localFileStorageService.fileExists(fileKey);
        logger.debug("File {} in local storage: {}", existsInLocal ? "exists" : "does not exist", fileKey);
        return existsInLocal;
    }

    @Override
    public long getFileSize(String fileKey) {
        // Try MinIO first
        long size = minIOFileStorageService.getFileSize(fileKey);
        if (size > 0) {
            logger.debug("File size from MinIO: {} bytes for {}", size, fileKey);
            return size;
        }

        // Try local storage
        size = localFileStorageService.getFileSize(fileKey);
        logger.debug("File size from local storage: {} bytes for {}", size, fileKey);
        return size;
    }
}

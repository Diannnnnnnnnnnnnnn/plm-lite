package com.example.document_service.service.gateway;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageGateway {
    String upload(String documentId, MultipartFile file);
    byte[] download(String fileKey);
    boolean delete(String fileKey);
    boolean exists(String fileKey);
    long getFileSize(String fileKey);
}

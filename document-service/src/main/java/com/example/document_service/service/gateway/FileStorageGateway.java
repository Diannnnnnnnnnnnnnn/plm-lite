package com.example.document_service.service.gateway;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageGateway {
    String upload(String documentId, MultipartFile file);
    byte[] download(String fileKey);
}

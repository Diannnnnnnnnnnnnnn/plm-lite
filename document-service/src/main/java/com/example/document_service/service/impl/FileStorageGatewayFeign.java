package com.example.document_service.service.impl;

import com.example.document_service.client.FileStorageClient;
import com.example.document_service.service.gateway.FileStorageGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageGatewayFeign implements FileStorageGateway {

    private final FileStorageClient client;

    public FileStorageGatewayFeign(FileStorageClient client) {
        this.client = client;
    }

    @Override
    public String upload(String documentId, MultipartFile file) {
        // The documentId will be used as the filename when uploading
        return client.uploadFile(file);
    }

    @Override
    public byte[] download(String documentId) {
        ResponseEntity<byte[]> response = client.downloadFile(documentId);
        return response.getBody();
    }
}

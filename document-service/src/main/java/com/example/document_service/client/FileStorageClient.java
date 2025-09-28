package com.example.document_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "file-storage-service", configuration = com.example.document_service.config.FeignMultipartSupportConfig.class)
public interface FileStorageClient {

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file, @RequestParam("filename") String filename);

    @GetMapping("/files/download/{filename}")
    ResponseEntity<byte[]> downloadFile(@PathVariable("filename") String filename);
}

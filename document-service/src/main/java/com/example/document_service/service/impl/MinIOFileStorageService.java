package com.example.document_service.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.Item;

@Service
public class MinIOFileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOFileStorageService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * Save file to MinIO
     * @param filename The filename to store in MinIO
     * @param file The file to upload
     * @return The stored filename or "error-saving-file" on failure
     */
    public String saveFile(String filename, MultipartFile file) {
        try {
            // Create bucket if it doesn't exist
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Created MinIO bucket: {}", bucketName);
            }

            // Prepare user metadata for MinIO
            java.util.Map<String, String> userMetadata = new java.util.HashMap<>();
            userMetadata.put("original-filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
            userMetadata.put("content-type", file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            userMetadata.put("file-size", String.valueOf(file.getSize()));
            userMetadata.put("upload-time", java.time.LocalDateTime.now().toString());

            // Upload file to MinIO with metadata
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(userMetadata)
                    .build()
            );



            logger.info("File uploaded to MinIO with metadata - Bucket: {}, File: {}, Size: {} bytes, Original: {}", 
                       bucketName, filename, file.getSize(), file.getOriginalFilename());
            return filename;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Failed to save file to MinIO: {} - Error: {}", filename, e.getMessage(), e);
            return "error-saving-file";
        }
    }

    /**
     * Get file from MinIO
     * @param filename The filename to retrieve
     * @return Byte array of file contents or empty array on failure
     */
    public byte[] getFile(String filename) {
        try {
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );

            byte[] data = stream.readAllBytes();
            stream.close();
            logger.info("File retrieved from MinIO - Bucket: {}, File: {}, Size: {} bytes", 
                       bucketName, filename, data.length);
            return data;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Failed to read file from MinIO: {} - Error: {}", filename, e.getMessage(), e);
            return new byte[0];
        }
    }

    /**
     * Delete file from MinIO
     * @param filename The filename to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteFile(String filename) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
            logger.info("File deleted from MinIO - Bucket: {}, File: {}", bucketName, filename);
            return true;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Failed to delete file from MinIO: {} - Error: {}", filename, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if file exists in MinIO
     * @param filename The filename to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filename) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
            logger.debug("File exists in MinIO - Bucket: {}, File: {}", bucketName, filename);
            return true;
        } catch (Exception e) {
            logger.debug("File does not exist in MinIO - Bucket: {}, File: {}", bucketName, filename);
            return false;
        }
    }

    /**
     * Get file size from MinIO
     * @param filename The filename to check
     * @return File size in bytes, or -1 if file doesn't exist
     */
    public long getFileSize(String filename) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
            logger.debug("File size retrieved - Bucket: {}, File: {}, Size: {} bytes", 
                        bucketName, filename, stat.size());
            return stat.size();
        } catch (Exception e) {
            logger.error("Failed to get file size from MinIO: {} - Error: {}", filename, e.getMessage());
            return -1;
        }
    }

    /**
     * List all files for a specific document (by prefix)
     * @param documentId The document ID prefix
     * @return List of filenames matching the prefix
     */
    public List<String> listFilesForDocument(String documentId) {
        List<String> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(documentId + "_")
                    .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }
            logger.info("Listed {} files for document {} from MinIO", files.size(), documentId);
        } catch (Exception e) {
            logger.error("Failed to list files for document: {} - Error: {}", documentId, e.getMessage(), e);
        }
        return files;
    }

    /**
     * Check if MinIO is healthy and accessible
     * @return true if MinIO is accessible, false otherwise
     */
    public boolean isHealthy() {
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            logger.debug("MinIO health check passed");
            return true;
        } catch (Exception e) {
            logger.error("MinIO health check failed: {}", e.getMessage());
            return false;
        }
    }
}
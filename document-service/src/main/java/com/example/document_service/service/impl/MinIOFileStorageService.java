package com.example.document_service.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinIOFileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public String saveFile(String filename, MultipartFile file) {
        try {
            // Create bucket if it doesn't exist
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("INFO: Created MinIO bucket: " + bucketName);
            }

            // Upload file to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            System.out.println("INFO: File uploaded to MinIO: " + filename + " in bucket: " + bucketName);
            return filename;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.err.println("Failed to save file to MinIO: " + e.getMessage());
            e.printStackTrace();
            return "error-saving-file";
        }
    }

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
            System.out.println("INFO: File retrieved from MinIO: " + filename);
            return data;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.err.println("Failed to read file from MinIO: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
}
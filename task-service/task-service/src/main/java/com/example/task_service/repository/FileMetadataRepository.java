package com.example.task_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_service.model.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}

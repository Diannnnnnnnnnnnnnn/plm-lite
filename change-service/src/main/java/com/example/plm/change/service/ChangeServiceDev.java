package com.example.plm.change.service;

import com.example.plm.change.dto.CreateChangeRequest;
import com.example.plm.change.dto.ChangeResponse;
import com.example.plm.change.model.*;
import com.example.plm.change.repository.mysql.ChangeRepository;
import com.example.plm.change.repository.mysql.ChangeDocumentRepository;
import com.example.plm.change.repository.mysql.ChangePartRepository;
import com.example.plm.common.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("dev")
public class ChangeServiceDev {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ChangeDocumentRepository changeDocumentRepository;

    @Autowired
    private ChangePartRepository changePartRepository;

    @Transactional
    public ChangeResponse createChange(CreateChangeRequest request) {
        // In dev mode, skip document validation
        String changeId = UUID.randomUUID().toString();

        Change change = new Change(
            changeId,
            request.getTitle(),
            request.getStage(),
            request.getChangeClass(),
            request.getProduct(),
            Status.IN_WORK,
            request.getCreator(),
            LocalDateTime.now(),
            request.getChangeReason(),
            request.getChangeDocument()
        );

        change = changeRepository.save(change);
        return mapToResponse(change);
    }

    @Transactional
    public ChangeResponse submitForReview(String changeId) {
        Change change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_WORK) {
            throw new IllegalStateException("Only changes in work can be submitted for review");
        }

        change.setStatus(Status.IN_REVIEW);
        change = changeRepository.save(change);
        return mapToResponse(change);
    }

    @Transactional
    public ChangeResponse approveChange(String changeId) {
        Change change = changeRepository.findById(changeId)
            .orElseThrow(() -> new RuntimeException("Change not found"));

        if (change.getStatus() != Status.IN_REVIEW) {
            throw new IllegalStateException("Only changes in review can be approved");
        }

        change.setStatus(Status.RELEASED);
        change = changeRepository.save(change);
        return mapToResponse(change);
    }

    public Optional<ChangeResponse> getChangeById(String id) {
        return changeRepository.findById(id).map(this::mapToResponse);
    }

    public List<ChangeResponse> getAllChanges() {
        return changeRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> getChangesByStatus(Status status) {
        return changeRepository.findByStatus(status).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> getChangesByCreator(String creator) {
        return changeRepository.findByCreator(creator).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeResponse> searchChanges(String keyword) {
        return changeRepository.findByKeyword(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ChangeSearchDocument> searchChangesElastic(String query) {
        // In dev mode, return empty list since we don't have Elasticsearch
        return List.of();
    }

    private ChangeResponse mapToResponse(Change change) {
        return new ChangeResponse(
            change.getId(),
            change.getTitle(),
            change.getStage(),
            change.getChangeClass(),
            change.getProduct(),
            change.getStatus(),
            change.getCreator(),
            change.getCreateTime(),
            change.getChangeReason(),
            change.getChangeDocument()
        );
    }
}
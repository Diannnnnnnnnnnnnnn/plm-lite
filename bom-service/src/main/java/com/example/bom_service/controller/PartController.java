package com.example.bom_service.controller;

import com.example.bom_service.dto.request.CreatePartRequest;
import com.example.bom_service.dto.request.AddPartUsageRequest;
import com.example.bom_service.dto.request.LinkPartToDocumentRequest;
import com.example.bom_service.dto.response.PartResponse;
import com.example.bom_service.model.Part;
import com.example.bom_service.model.PartUsage;
import com.example.bom_service.model.DocumentPartLink;
import com.example.bom_service.service.PartService;
import com.example.plm.common.model.Stage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    // Part CRUD operations
    
    @PostMapping
    public ResponseEntity<PartResponse> createPart(@Valid @RequestBody CreatePartRequest request) {
        Part part = partService.createPart(request);
        return ResponseEntity.ok(toPartResponse(part));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(@PathVariable String id) {
        Part part = partService.getPartById(id);
        return ResponseEntity.ok(toPartResponse(part));
    }

    @GetMapping
    public ResponseEntity<List<PartResponse>> getAllParts() {
        List<Part> parts = partService.getAllParts();
        List<PartResponse> responses = parts.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/creator/{creator}")
    public ResponseEntity<List<PartResponse>> getPartsByCreator(@PathVariable String creator) {
        List<Part> parts = partService.getPartsByCreator(creator);
        List<PartResponse> responses = parts.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<PartResponse>> getPartsByStage(@PathVariable Stage stage) {
        List<Part> parts = partService.getPartsByStage(stage);
        List<PartResponse> responses = parts.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PartResponse>> searchPartsByTitle(@RequestParam String title) {
        List<Part> parts = partService.searchPartsByTitle(title);
        List<PartResponse> responses = parts.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/stage/{stage}")
    public ResponseEntity<PartResponse> updatePartStage(@PathVariable String id, @PathVariable Stage stage) {
        Part part = partService.updatePartStage(id, stage);
        return ResponseEntity.ok(toPartResponse(part));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable String id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }

    // Elasticsearch operations
    
    @PostMapping("/elasticsearch/reindex")
    public ResponseEntity<String> reindexAllParts() {
        int count = partService.reindexAllParts();
        return ResponseEntity.ok("Successfully re-indexed " + count + " parts to Elasticsearch");
    }

    // BOM hierarchy operations
    
    @PostMapping("/usage")
    public ResponseEntity<String> addPartUsage(@Valid @RequestBody AddPartUsageRequest request) {
        PartUsage usage = partService.addPartUsage(request);
        return ResponseEntity.ok("Part usage added with ID: " + usage.getId());
    }

    @DeleteMapping("/{parentId}/usage/{childId}")
    public ResponseEntity<Void> removePartUsage(@PathVariable String parentId, @PathVariable String childId) {
        partService.removePartUsage(parentId, childId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{parentId}/usage/{childId}/quantity/{quantity}")
    public ResponseEntity<Void> updatePartUsageQuantity(
            @PathVariable String parentId, 
            @PathVariable String childId, 
            @PathVariable Integer quantity) {
        partService.updatePartUsageQuantity(parentId, childId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<PartResponse>> getChildParts(@PathVariable String id) {
        List<Part> children = partService.getChildParts(id);
        List<PartResponse> responses = children.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/parents")
    public ResponseEntity<List<PartResponse>> getParentParts(@PathVariable String id) {
        List<Part> parents = partService.getParentParts(id);
        List<PartResponse> responses = parents.stream()
                .map(this::toPartResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Document-Part linking operations
    
    @PostMapping("/document-link")
    public ResponseEntity<String> linkPartToDocument(@Valid @RequestBody LinkPartToDocumentRequest request) {
        DocumentPartLink link = partService.linkPartToDocument(request);
        return ResponseEntity.ok("Link created with ID: " + link.getLinkId());
    }

    @DeleteMapping("/{partId}/document/{documentId}")
    public ResponseEntity<Void> unlinkPartFromDocument(@PathVariable String partId, @PathVariable String documentId) {
        partService.unlinkPartFromDocument(partId, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<String>> getDocumentsForPart(@PathVariable String id) {
        List<String> documentIds = partService.getDocumentsForPart(id);
        return ResponseEntity.ok(documentIds);
    }

    @GetMapping("/document/{documentId}/parts")
    public ResponseEntity<List<String>> getPartsForDocument(@PathVariable String documentId) {
        List<String> partIds = partService.getPartsForDocument(documentId);
        return ResponseEntity.ok(partIds);
    }

    // Helper method to convert Part to PartResponse
    private PartResponse toPartResponse(Part part) {
        PartResponse response = new PartResponse();
        response.setId(part.getId());
        response.setTitle(part.getTitle());
        response.setDescription(part.getDescription());
        response.setStage(part.getStage());
        response.setStatus(part.getStatus());
        response.setLevel(part.getLevel());
        response.setCreator(part.getCreator());
        response.setCreateTime(part.getCreateTime());
        response.setUpdateTime(part.getUpdateTime());
        response.setDeleted(part.isDeleted());
        response.setDeleteTime(part.getDeleteTime());
        
        // Add child usages if available, excluding deleted parts
        if (part.getChildUsages() != null) {
            List<PartResponse.PartUsageResponse> usageResponses = part.getChildUsages().stream()
                    .filter(usage -> !usage.getChild().isDeleted())
                    .map(this::toPartUsageResponse)
                    .collect(Collectors.toList());
            response.setChildUsages(usageResponses);
        }
        
        // Add document links if available
        if (part.getDocumentLinks() != null) {
            List<String> documentIds = part.getDocumentLinks().stream()
                    .map(DocumentPartLink::getDocumentId)
                    .collect(Collectors.toList());
            response.setDocumentIds(documentIds);
        }
        
        return response;
    }

    private PartResponse.PartUsageResponse toPartUsageResponse(PartUsage usage) {
        PartResponse.PartUsageResponse response = new PartResponse.PartUsageResponse();
        response.setId(usage.getId());
        response.setChildPartId(usage.getChild().getId());
        response.setChildPartTitle(usage.getChild().getTitle());
        response.setQuantity(usage.getQuantity());
        return response;
    }
}

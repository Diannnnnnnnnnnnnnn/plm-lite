package com.example.bom_service.service.impl;

import com.example.bom_service.dto.request.CreatePartRequest;
import com.example.bom_service.dto.request.AddPartUsageRequest;
import com.example.bom_service.dto.request.LinkPartToDocumentRequest;
import com.example.bom_service.dto.response.BomHierarchyResponse;
import com.example.bom_service.exception.NotFoundException;
import com.example.bom_service.exception.ValidationException;
import com.example.bom_service.model.Part;
import com.example.bom_service.model.PartUsage;
import com.example.bom_service.model.DocumentPartLink;
import com.example.bom_service.repository.PartRepository;
import com.example.bom_service.repository.PartUsageRepository;
import com.example.bom_service.repository.DocumentPartLinkRepository;
import com.example.bom_service.service.PartService;
import com.example.plm.common.model.Stage;

// Graph Service imports
import com.example.bom_service.client.GraphServiceClient;
import com.example.bom_service.client.PartSyncDto;
import com.example.bom_service.client.PartUsageDto;
import com.example.bom_service.client.PartDocumentLinkDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final PartUsageRepository partUsageRepository;
    private final DocumentPartLinkRepository documentPartLinkRepository;
    private final GraphServiceClient graphServiceClient;

    public PartServiceImpl(PartRepository partRepository, 
                          PartUsageRepository partUsageRepository,
                          DocumentPartLinkRepository documentPartLinkRepository,
                          GraphServiceClient graphServiceClient) {
        this.partRepository = partRepository;
        this.partUsageRepository = partUsageRepository;
        this.documentPartLinkRepository = documentPartLinkRepository;
        this.graphServiceClient = graphServiceClient;
    }

    @Override
    @Transactional
    public Part createPart(CreatePartRequest request) {
        validateCreatePartRequest(request);
        
        Part part = new Part();
        part.setId(UUID.randomUUID().toString());
        part.setTitle(request.getTitle());
        part.setDescription(request.getDescription());
        part.setStage(request.getStage());
        part.setStatus(request.getStatus()); // Will default to IN_WORK in @PrePersist if null
        part.setLevel(request.getLevel());
        part.setCreator(request.getCreator());
        
        Part savedPart = partRepository.save(part);
        
        // Sync to Neo4j
        syncPartToGraph(savedPart);
        
        return savedPart;
    }

    @Override
    public Part getPartById(String id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Part not found with id: " + id));
    }

    @Override
    public List<Part> getAllParts() {
        return partRepository.findAll();
    }

    @Override
    public List<Part> getPartsByCreator(String creator) {
        return partRepository.findByCreator(creator);
    }

    @Override
    public List<Part> getPartsByStage(Stage stage) {
        return partRepository.findByStage(stage);
    }

    @Override
    public List<Part> searchPartsByTitle(String title) {
        return partRepository.findByTitleContaining(title);
    }

    @Override
    @Transactional
    public Part updatePartStage(String partId, Stage stage) {
        Part part = getPartById(partId);
        part.setStage(stage);
        return partRepository.save(part);
    }

    @Override
    @Transactional
    public void deletePart(String id) {
        Part part = getPartById(id);
        
        // Check if part is used in any BOM structures
        List<PartUsage> parentUsages = partUsageRepository.findByChildId(id);
        List<PartUsage> childUsages = partUsageRepository.findByParentId(id);
        
        if (!parentUsages.isEmpty() || !childUsages.isEmpty()) {
            throw new ValidationException("Cannot delete part that is used in BOM structures. Use soft delete instead.");
        }
        
        // Soft delete
        part.setDeleted(true);
        part.setDeleteTime(java.time.LocalDateTime.now());
        partRepository.save(part);
        log.info("✅ Part {} soft deleted successfully", id);
        
        // Note: Could optionally sync delete to Neo4j here
        // graphServiceClient.deletePart(id);
    }

    @Override
    @Transactional
    public PartUsage addPartUsage(AddPartUsageRequest request) {
        validatePartUsageRequest(request);
        
        Part parent = getPartById(request.getParentPartId());
        Part child = getPartById(request.getChildPartId());
        
        // Check for circular dependency
        if (wouldCreateCircularDependency(request.getParentPartId(), request.getChildPartId())) {
            throw new ValidationException("Adding this relationship would create a circular dependency");
        }
        
        // Check if usage already exists
        PartUsage existingUsage = partUsageRepository.findByParentAndChild(
                request.getParentPartId(), request.getChildPartId());
        if (existingUsage != null) {
            throw new ValidationException("Part usage relationship already exists");
        }
        
        PartUsage partUsage = new PartUsage();
        partUsage.setId(UUID.randomUUID().toString());
        partUsage.setParent(parent);
        partUsage.setChild(child);
        partUsage.setQuantity(request.getQuantity());
        
        PartUsage savedUsage = partUsageRepository.save(partUsage);
        
        // Sync to Neo4j
        syncPartUsageToGraph(savedUsage);
        
        return savedUsage;
    }

    @Override
    @Transactional
    public void removePartUsage(String parentPartId, String childPartId) {
        PartUsage usage = partUsageRepository.findByParentAndChild(parentPartId, childPartId);
        if (usage == null) {
            throw new NotFoundException("Part usage relationship not found");
        }
        partUsageRepository.delete(usage);
    }

    @Override
    @Transactional
    public void updatePartUsageQuantity(String parentPartId, String childPartId, Integer quantity) {
        PartUsage usage = partUsageRepository.findByParentAndChild(parentPartId, childPartId);
        if (usage == null) {
            throw new NotFoundException("Part usage relationship not found");
        }
        usage.setQuantity(quantity);
        partUsageRepository.save(usage);
    }

    @Override
    public List<Part> getChildParts(String parentPartId) {
        return partRepository.findChildrenOf(parentPartId);
    }

    @Override
    public List<Part> getParentParts(String childPartId) {
        return partRepository.findParentsOf(childPartId);
    }

    @Override
    public BomHierarchyResponse getBomHierarchy(String rootPartId) {
        Part rootPart = getPartById(rootPartId);
        return buildHierarchy(rootPart, 1);
    }

    @Override
    @Transactional
    public DocumentPartLink linkPartToDocument(LinkPartToDocumentRequest request) {
        Part part = getPartById(request.getPartId());
        
        // Check if link already exists
        List<DocumentPartLink> existingLinks = documentPartLinkRepository
                .findByPartId(request.getPartId());
        boolean linkExists = existingLinks.stream()
                .anyMatch(link -> link.getDocumentId().equals(request.getDocumentId()));
        
        if (linkExists) {
            throw new ValidationException("Part is already linked to this document");
        }
        
        DocumentPartLink link = new DocumentPartLink();
        link.setLinkId(UUID.randomUUID().toString());
        link.setPart(part);
        link.setDocumentId(request.getDocumentId());
        
        DocumentPartLink savedLink = documentPartLinkRepository.save(link);
        
        // Sync to Neo4j
        syncPartDocumentLinkToGraph(savedLink);
        
        return savedLink;
    }

    @Override
    @Transactional
    public void unlinkPartFromDocument(String partId, String documentId) {
        documentPartLinkRepository.deleteByPartIdAndDocumentId(partId, documentId);
    }

    @Override
    public List<String> getDocumentsForPart(String partId) {
        return documentPartLinkRepository.findByPartId(partId)
                .stream()
                .map(DocumentPartLink::getDocumentId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPartsForDocument(String documentId) {
        return documentPartLinkRepository.findByDocumentId(documentId)
                .stream()
                .map(link -> link.getPart().getId())
                .collect(Collectors.toList());
    }

    // Private helper methods
    
    private void validateCreatePartRequest(CreatePartRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Part title is required");
        }
        if (request.getStage() == null) {
            throw new ValidationException("Part stage is required");
        }
        if (request.getLevel() == null || request.getLevel().trim().isEmpty()) {
            throw new ValidationException("Part level is required");
        }
        if (request.getCreator() == null || request.getCreator().trim().isEmpty()) {
            throw new ValidationException("Part creator is required");
        }
    }

    private void validatePartUsageRequest(AddPartUsageRequest request) {
        if (request.getParentPartId() == null || request.getParentPartId().trim().isEmpty()) {
            throw new ValidationException("Parent part ID is required");
        }
        if (request.getChildPartId() == null || request.getChildPartId().trim().isEmpty()) {
            throw new ValidationException("Child part ID is required");
        }
        if (request.getParentPartId().equals(request.getChildPartId())) {
            throw new ValidationException("Part cannot be its own child");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be a positive number");
        }
    }

    private boolean wouldCreateCircularDependency(String parentId, String childId) {
        // Simple check: see if childId is an ancestor of parentId
        return isAncestor(childId, parentId);
    }

    private boolean isAncestor(String ancestorId, String descendantId) {
        List<PartUsage> parentUsages = partUsageRepository.findByChildId(descendantId);
        
        for (PartUsage usage : parentUsages) {
            String parentId = usage.getParent().getId();
            if (parentId.equals(ancestorId)) {
                return true;
            }
            if (isAncestor(ancestorId, parentId)) {
                return true;
            }
        }
        
        return false;
    }

    private BomHierarchyResponse buildHierarchy(Part part, Integer quantity) {
        BomHierarchyResponse response = new BomHierarchyResponse();
        response.setPartId(part.getId());
        response.setPartTitle(part.getTitle());
        response.setLevel(part.getLevel());
        response.setQuantity(quantity);
        
        List<PartUsage> childUsages = partUsageRepository.findByParentIdOrderByChildTitle(part.getId());
        List<BomHierarchyResponse> children = new ArrayList<>();
        
        for (PartUsage usage : childUsages) {
            BomHierarchyResponse childResponse = buildHierarchy(usage.getChild(), usage.getQuantity());
            children.add(childResponse);
        }
        
        response.setChildren(children);
        return response;
    }
    
    // ==========================================
    // Graph Sync Methods
    // ==========================================
    
    private void syncPartToGraph(Part part) {
        try {
            PartSyncDto dto = new PartSyncDto(
                part.getId(),
                part.getTitle(),
                part.getDescription(),
                part.getStage().name(),
                part.getStatus() != null ? part.getStatus().name() : "IN_WORK",
                part.getLevel(),
                part.getCreator(),
                part.getCreateTime()
            );
            graphServiceClient.syncPart(dto);
            log.info("✅ Part {} synced to graph successfully", part.getId());
        } catch (Exception e) {
            log.warn("⚠️ Failed to sync part {} to graph: {}", part.getId(), e.getMessage());
        }
    }
    
    private void syncPartUsageToGraph(PartUsage partUsage) {
        try {
            PartUsageDto dto = new PartUsageDto(
                partUsage.getParent().getId(),
                partUsage.getChild().getId(),
                partUsage.getQuantity()
            );
            graphServiceClient.syncPartUsage(dto);
            log.info("✅ Part usage synced to graph successfully");
        } catch (Exception e) {
            log.warn("⚠️ Failed to sync part usage to graph: {}", e.getMessage());
        }
    }
    
    private void syncPartDocumentLinkToGraph(DocumentPartLink link) {
        try {
            PartDocumentLinkDto dto = new PartDocumentLinkDto(
                link.getPart().getId(),
                link.getDocumentId()
            );
            graphServiceClient.syncPartDocumentLink(dto);
            log.info("✅ Part-document link synced to graph successfully");
        } catch (Exception e) {
            log.warn("⚠️ Failed to sync part-document link to graph: {}", e.getMessage());
        }
    }
}

package com.example.bom_service.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bom_service.dto.request.CreateBomRequest;
import com.example.bom_service.dto.request.UpdateBomRequest;
import com.example.bom_service.exception.NotFoundException;
import com.example.bom_service.exception.ValidationException;
import com.example.bom_service.model.BomHeader;
import com.example.bom_service.model.BomItem;
import com.example.bom_service.repository.BomHeaderRepository;
import com.example.bom_service.repository.BomItemRepository;
import com.example.bom_service.service.BomService;
import com.example.plm.common.model.Status;
import com.example.plm.common.model.Stage;

@Service
public class BomServiceImpl implements BomService {

    private final BomHeaderRepository headerRepository;
    private final BomItemRepository itemRepository;

    public BomServiceImpl(BomHeaderRepository headerRepository, BomItemRepository itemRepository) {
        this.headerRepository = headerRepository;
        this.itemRepository = itemRepository;
    }

    private void validateCreateRequest(CreateBomRequest request) {
        if (request.getDocumentId() == null || request.getDocumentId().trim().isEmpty()) {
            throw new ValidationException("Document ID is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new ValidationException("Description is required");
        }
        if (request.getCreator() == null || request.getCreator().trim().isEmpty()) {
            throw new ValidationException("Creator is required");
        }
        if (request.getStage() == null) {
            throw new ValidationException("Stage is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("At least one BOM item is required");
        }
    }
    
    @Override
    @Transactional
    public BomHeader create(CreateBomRequest request) {
        validateCreateRequest(request);
        
        BomHeader header = new BomHeader();
        header.setId(UUID.randomUUID().toString());
        header.setDocumentId(request.getDocumentId());
        header.setDescription(request.getDescription());
        header.setCreator(request.getCreator());
        header.setStage(request.getStage());
        header.setStatus(Status.IN_WORK);

        // Create BOM items
        List<BomItem> items = new ArrayList<>();
        for (CreateBomRequest.BomItemRequest itemRequest : request.getItems()) {
            BomItem item = new BomItem();
            item.setId(UUID.randomUUID().toString());
            item.setHeader(header);
            item.setPartNumber(itemRequest.getPartNumber());
            item.setDescription(itemRequest.getDescription());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnit(itemRequest.getUnit());
            item.setReference(itemRequest.getReference());
            items.add(item);
        }
        header.setItems(items);

        return headerRepository.save(header);
    }

    @Override
    public BomHeader getById(String id) {
        return headerRepository.findByIdActive(id)
                .orElseThrow(() -> new NotFoundException("BOM not found with id: " + id));
    }

    @Override
    public List<BomHeader> getByDocumentId(String documentId) {
        return headerRepository.findByDocumentId(documentId);
    }
    
    @Override
    public List<BomHeader> getAll() {
        return headerRepository.findAllActive();
    }
    
    @Override
    @Transactional
    public BomHeader update(String id, UpdateBomRequest request) {
        BomHeader header = getById(id);
        
        if (request.getDescription() != null) {
            header.setDescription(request.getDescription());
        }
        
        if (request.getStage() != null) {
            header.setStage(request.getStage());
        }
        
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Create a map of existing items for easy lookup
            Map<String, BomItem> existingItems = header.getItems().stream()
                .collect(Collectors.toMap(BomItem::getId, item -> item));
            
            List<BomItem> updatedItems = new ArrayList<>();
            
            for (UpdateBomRequest.BomItemRequest itemRequest : request.getItems()) {
                BomItem item;
                
                if (itemRequest.getId() != null && existingItems.containsKey(itemRequest.getId())) {
                    // Update existing item
                    item = existingItems.get(itemRequest.getId());
                    item.setPartNumber(itemRequest.getPartNumber());
                    item.setDescription(itemRequest.getDescription());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnit(itemRequest.getUnit());
                    item.setReference(itemRequest.getReference());
                    existingItems.remove(itemRequest.getId());
                } else {
                    // Create new item
                    item = new BomItem();
                    item.setId(UUID.randomUUID().toString());
                    item.setHeader(header);
                    item.setPartNumber(itemRequest.getPartNumber());
                    item.setDescription(itemRequest.getDescription());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnit(itemRequest.getUnit());
                    item.setReference(itemRequest.getReference());
                }
                
                updatedItems.add(item);
            }
            
            // Delete items that were not included in the update request
            for (BomItem removedItem : existingItems.values()) {
                itemRepository.delete(removedItem);
            }
            
            header.setItems(updatedItems);
        }
        
        return headerRepository.save(header);
    }
    
    @Override
    @Transactional
    public BomHeader updateStage(String id, Stage stage) {
        BomHeader header = getById(id);
        header.setStage(stage);
        return headerRepository.save(header);
    }

    @Override
    @Transactional
    public void delete(String id) {
        BomHeader header = getById(id);
        header.setDeleted(true);
        header.setDeleteTime(LocalDateTime.now());
        headerRepository.save(header);
    }
    
    @Transactional
    public void purgeDeletedBoms(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<BomHeader> oldDeletedBoms = headerRepository.findByDeletedTrueAndDeleteTimeBefore(cutoffDate);
        headerRepository.deleteAll(oldDeletedBoms);
    }
}

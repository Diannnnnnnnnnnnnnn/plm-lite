package com.example.bom_service.service.impl;

import com.example.bom_service.dto.request.CreateBomRequest;
import com.example.bom_service.model.BomHeader;
import com.example.bom_service.model.BomItem;
import com.example.bom_service.repository.BomHeaderRepository;
import com.example.bom_service.repository.BomItemRepository;
import com.example.bom_service.service.BomService;
import com.example.plm.common.model.DocumentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BomServiceImpl implements BomService {

    private final BomHeaderRepository headerRepository;
    private final BomItemRepository itemRepository;

    public BomServiceImpl(BomHeaderRepository headerRepository, BomItemRepository itemRepository) {
        this.headerRepository = headerRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public BomHeader create(CreateBomRequest request) {
        BomHeader header = new BomHeader();
        header.setId(UUID.randomUUID().toString());
        header.setDocumentId(request.getDocumentId());
        header.setDescription(request.getDescription());
        header.setCreator(request.getCreator());
        header.setStage(request.getStage());
        header.setStatus(DocumentStatus.IN_WORK);

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
        return headerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM not found with id: " + id));
    }

    @Override
    public List<BomHeader> getByDocumentId(String documentId) {
        return headerRepository.findByDocumentId(documentId);
    }

    @Override
    @Transactional
    public void delete(String id) {
        headerRepository.deleteById(id);
    }
}

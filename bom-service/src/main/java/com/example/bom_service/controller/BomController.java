package com.example.bom_service.controller;

import com.example.bom_service.dto.request.CreateBomRequest;
import com.example.bom_service.dto.response.BomResponse;
import com.example.bom_service.model.BomHeader;
import com.example.bom_service.model.BomItem;
import com.example.bom_service.service.BomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/boms")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @PostMapping
    public ResponseEntity<BomResponse> create(@RequestBody CreateBomRequest request) {
        BomHeader header = bomService.create(request);
        return ResponseEntity.ok(toResponse(header));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BomResponse> getById(@PathVariable String id) {
        BomHeader header = bomService.getById(id);
        return ResponseEntity.ok(toResponse(header));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<BomResponse>> getByDocumentId(@PathVariable String documentId) {
        List<BomHeader> headers = bomService.getByDocumentId(documentId);
        List<BomResponse> responses = headers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        bomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private BomResponse toResponse(BomHeader header) {
        BomResponse response = new BomResponse();
        response.setId(header.getId());
        response.setDocumentId(header.getDocumentId());
        response.setDescription(header.getDescription());
        response.setCreator(header.getCreator());
        response.setStage(header.getStage());
        response.setStatus(header.getStatus());
        response.setCreateTime(header.getCreateTime());
        response.setUpdateTime(header.getUpdateTime());

        List<BomResponse.BomItemResponse> itemResponses = header.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private BomResponse.BomItemResponse toItemResponse(BomItem item) {
        BomResponse.BomItemResponse response = new BomResponse.BomItemResponse();
        response.setId(item.getId());
        response.setPartNumber(item.getPartNumber());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnit(item.getUnit());
        response.setReference(item.getReference());
        return response;
    }
}

package com.example.bom_service.service;

import java.util.List;

import com.example.bom_service.dto.request.CreateBomRequest;
import com.example.bom_service.dto.request.UpdateBomRequest;
import com.example.bom_service.model.BomHeader;
import com.example.plm.common.model.Stage;

public interface BomService {
    BomHeader create(CreateBomRequest request);
    BomHeader getById(String id);
    List<BomHeader> getByDocumentId(String documentId);
    List<BomHeader> getAll();
    BomHeader update(String id, UpdateBomRequest request);
    BomHeader updateStage(String id, Stage stage);
    void delete(String id);
}

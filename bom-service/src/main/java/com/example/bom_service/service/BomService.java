package com.example.bom_service.service;

import com.example.bom_service.dto.request.CreateBomRequest;
import com.example.bom_service.model.BomHeader;
import java.util.List;

public interface BomService {
    BomHeader create(CreateBomRequest request);
    BomHeader getById(String id);
    List<BomHeader> getByDocumentId(String documentId);
    void delete(String id);
}

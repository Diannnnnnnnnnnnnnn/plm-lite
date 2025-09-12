package com.example.bom_service.service;

import com.example.bom_service.dto.request.CreatePartRequest;
import com.example.bom_service.dto.request.AddPartUsageRequest;
import com.example.bom_service.dto.request.LinkPartToDocumentRequest;
import com.example.bom_service.dto.response.BomHierarchyResponse;
import com.example.bom_service.model.Part;
import com.example.bom_service.model.PartUsage;
import com.example.bom_service.model.DocumentPartLink;
import com.example.plm.common.model.Stage;

import java.util.List;

public interface PartService {
    
    // Part CRUD operations
    Part createPart(CreatePartRequest request);
    Part getPartById(String id);
    List<Part> getAllParts();
    List<Part> getPartsByCreator(String creator);
    List<Part> getPartsByStage(Stage stage);
    List<Part> searchPartsByTitle(String title);
    Part updatePartStage(String partId, Stage stage);
    void deletePart(String id);
    
    // BOM hierarchy operations
    PartUsage addPartUsage(AddPartUsageRequest request);
    void removePartUsage(String parentPartId, String childPartId);
    void updatePartUsageQuantity(String parentPartId, String childPartId, Integer quantity);
    List<Part> getChildParts(String parentPartId);
    List<Part> getParentParts(String childPartId);
    BomHierarchyResponse getBomHierarchy(String rootPartId);
    
    // Document-Part linking operations
    DocumentPartLink linkPartToDocument(LinkPartToDocumentRequest request);
    void unlinkPartFromDocument(String partId, String documentId);
    List<String> getDocumentsForPart(String partId);
    List<String> getPartsForDocument(String documentId);
}

package com.example.document_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.document_service.client.SearchServiceClient;
import com.example.document_service.model.Document;
import com.example.document_service.service.gateway.SearchGateway;
import com.example.plm.common.model.DocumentStatus;
import com.example.plm.common.model.Stage;

@Component
public class SearchGatewayFeign implements SearchGateway {

    private final SearchServiceClient client;

    public SearchGatewayFeign(SearchServiceClient client) {
        this.client = client;
    }

    @Override
    public void index(Document doc) {
        SearchServiceClient.DocumentEsDto dto = new SearchServiceClient.DocumentEsDto();
        dto.setId(doc.getId());
        dto.setMasterId(doc.getMaster() != null ? doc.getMaster().getId() : null);
        dto.setTitle(doc.getTitle());
        dto.setCategory(doc.getMaster() != null ? doc.getMaster().getCategory() : null);
        dto.setStage(doc.getStage());
        dto.setStatus(doc.getStatus());
        dto.setCreator(doc.getCreator());
        dto.setCreateTime(doc.getCreateTime());

        client.indexDocument(dto);
    }

    @Override
    public List<SearchServiceClient.DocumentEsDto> search(String q, Stage stage, DocumentStatus status, String category) {
        return client.search(q, stage != null ? stage.name() : null, 
                              status != null ? status.name() : null, 
                              category);
    }
}

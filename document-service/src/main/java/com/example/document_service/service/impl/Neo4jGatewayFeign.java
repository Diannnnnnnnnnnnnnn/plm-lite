package com.example.document_service.service.impl;

import org.springframework.stereotype.Component;

import com.example.document_service.client.Neo4jClient;
import com.example.document_service.model.Document;
import com.example.document_service.service.gateway.Neo4jGateway;

@Component
public class Neo4jGatewayFeign implements Neo4jGateway {

    private final Neo4jClient client;

    public Neo4jGatewayFeign(Neo4jClient client) {
        this.client = client;
    }

    @Override
    public void upsert(Document doc) {
        Neo4jClient.DocumentNodePayload p = new Neo4jClient.DocumentNodePayload();
        p.setId(doc.getId());
        p.setMasterId(doc.getMaster() != null ? doc.getMaster().getId() : null);
        p.setTitle(doc.getTitle());
        p.setStage(doc.getStage());
        p.setStatus(doc.getStatus());
        client.upsertDocumentNode(p);
    }
}

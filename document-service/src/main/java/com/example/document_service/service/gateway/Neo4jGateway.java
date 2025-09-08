package com.example.document_service.service.gateway;

import com.example.document_service.model.Document;

public interface Neo4jGateway {
    void upsert(Document doc);
}

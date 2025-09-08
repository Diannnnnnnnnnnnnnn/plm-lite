// src/main/java/com/example/document_service/service/SearchService.java
package com.example.document_service.service;

import com.example.document_service.model.Document;

public interface SearchService {
    void index(Document doc);
}

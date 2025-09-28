package com.example.document_service.service.gateway;

import com.example.document_service.client.SearchServiceClient;
import com.example.document_service.model.Document;
import com.example.plm.common.model.Status;
import com.example.plm.common.model.Stage;

import java.util.List;

public interface SearchGateway {

    /** Index a document to the search engine */
    void index(Document doc);

    /** Search documents in the search engine */
    List<SearchServiceClient.DocumentEsDto> search(String q, Stage stage, Status status, String category);
}

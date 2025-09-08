package com.example.document_service;

import com.example.document_service.service.DocumentService;
import com.example.document_service.service.gateway.SearchGateway;
import com.example.plm.common.model.Stage;
import com.example.plm.common.model.DocumentStatus;

public class CompileSanityCheck {
    // This class exists only to force compile-time checks across interfaces
    public void check(DocumentService ds, SearchGateway sg) {
        // attachFileKey signature expectation
        ds.attachFileKey("docId", "fileKey", "user");

        // search signature expectation
        sg.search("q", Stage.CONCEPTUAL_DESIGN, DocumentStatus.IN_WORK, "cat");
    }
}

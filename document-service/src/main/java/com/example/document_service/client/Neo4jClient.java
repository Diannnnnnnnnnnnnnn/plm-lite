package com.example.document_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.example.plm.common.model.DocumentStatus;
import com.example.plm.common.model.Stage;

@FeignClient(name = "graph-service", url = "${graph-service.url}")
public interface Neo4jClient {

    @PostMapping("/graph/documents/upsert")
    void upsertDocumentNode(@RequestBody DocumentNodePayload payload);

    class DocumentNodePayload {
        private String id;
        private String masterId;
        private String title;
        private Stage stage;
        private DocumentStatus status;

        public DocumentNodePayload() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMasterId() { return masterId; }
        public void setMasterId(String masterId) { this.masterId = masterId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Stage getStage() { return stage; }
        public void setStage(Stage stage) { this.stage = stage; }
        public DocumentStatus getStatus() { return status; }
        public void setStatus(DocumentStatus status) { this.status = status; }
    }
}

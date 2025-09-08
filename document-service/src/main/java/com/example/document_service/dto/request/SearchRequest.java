package com.example.document_service.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.DocumentStatus;

public class SearchRequest {

    private String q;
    private Stage stage;
    private DocumentStatus status;
    private String category;

    public SearchRequest() {}
    
    @JsonCreator
    public SearchRequest(@JsonProperty("q") String q,
                         @JsonProperty("stage") Stage stage,
                         @JsonProperty("status") DocumentStatus status,
                         @JsonProperty("category") String category) {
        this.q = q;
        this.stage = stage;
        this.status = status;
        this.category = category;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

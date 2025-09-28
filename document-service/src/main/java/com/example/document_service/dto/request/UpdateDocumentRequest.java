package com.example.document_service.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;

public class UpdateDocumentRequest {

    private String title;
    private Stage stage;
    private Status status;
    private String description;
    private String user;

    public UpdateDocumentRequest() {}

    @JsonCreator
    public UpdateDocumentRequest(@JsonProperty("title") String title,
                                @JsonProperty("stage") Stage stage,
                                @JsonProperty("status") Status status,
                                @JsonProperty("description") String description,
                                @JsonProperty("user") String user) {
        this.title = title;
        this.stage = stage;
        this.status = status;
        this.description = description;
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
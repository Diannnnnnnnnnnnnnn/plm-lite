package com.example.document_service.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.plm.common.model.Stage;

public class UpdateStageRequest {

    private Stage stage;
    private String user;
    private String comment;

    public UpdateStageRequest() {}

    @JsonCreator
    public UpdateStageRequest(@JsonProperty("stage") Stage stage,
                              @JsonProperty("user") String user,
                              @JsonProperty("comment") String comment) {
        this.stage = stage;
        this.user = user;
        this.comment = comment;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

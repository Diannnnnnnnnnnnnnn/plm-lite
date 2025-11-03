package com.example.document_service.dto.request;

import com.example.plm.common.model.Stage;

public class CreateDocumentRequest {
    private String masterId;
    private String title;
    private String description;
    private String creator;
    private String category;
    private Stage stage;
    private String partId;  // Related Part ID (replaces partId)

    public CreateDocumentRequest() {
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    @Override
    public String toString() {
        return "CreateDocumentRequest{" +
                "masterId='" + masterId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creator='" + creator + '\'' +
                ", category='" + category + '\'' +
                ", stage=" + stage +
                '}';
    }
}

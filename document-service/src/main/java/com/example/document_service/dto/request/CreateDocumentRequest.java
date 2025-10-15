package com.example.document_service.dto.request;

import com.example.plm.common.model.Stage;

public class CreateDocumentRequest {
    private String masterId;
    private String title;
    private String creator;
    private String category;
    private Stage stage;
    private String bomId;  // Related BOM ID

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

    public String getBomId() {
        return bomId;
    }

    public void setBomId(String bomId) {
        this.bomId = bomId;
    }

    @Override
    public String toString() {
        return "CreateDocumentRequest{" +
                "masterId='" + masterId + '\'' +
                ", title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", category='" + category + '\'' +
                ", stage=" + stage +
                '}';
    }
}

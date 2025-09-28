package com.example.plm.change.dto;

import com.example.plm.common.model.Stage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateChangeRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Stage is required")
    private Stage stage;

    @NotBlank(message = "Change class is required")
    private String changeClass;

    @NotBlank(message = "Product is required")
    private String product;

    @NotBlank(message = "Creator is required")
    private String creator;

    @NotBlank(message = "Change reason is required")
    private String changeReason;

    @NotBlank(message = "Change document is required")
    private String changeDocument;

    public CreateChangeRequest() {}

    public CreateChangeRequest(String title, Stage stage, String changeClass, String product,
                              String creator, String changeReason, String changeDocument) {
        this.title = title;
        this.stage = stage;
        this.changeClass = changeClass;
        this.product = product;
        this.creator = creator;
        this.changeReason = changeReason;
        this.changeDocument = changeDocument;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public String getChangeClass() { return changeClass; }
    public void setChangeClass(String changeClass) { this.changeClass = changeClass; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getChangeDocument() { return changeDocument; }
    public void setChangeDocument(String changeDocument) { this.changeDocument = changeDocument; }
}
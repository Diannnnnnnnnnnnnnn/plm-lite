package com.example.plm.change.dto;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ChangeResponse {

    private String id;
    private String title;
    private Stage stage;
    private String changeClass;
    private String product;
    private Status status;
    private String creator;
    private LocalDateTime createTime;
    private String changeReason;
    private String changeDocument;
    private List<String> bomIds = new ArrayList<>();

    public ChangeResponse() {}

    public ChangeResponse(String id, String title, Stage stage, String changeClass, String product,
                         Status status, String creator, LocalDateTime createTime, String changeReason,
                         String changeDocument) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.changeClass = changeClass;
        this.product = product;
        this.status = status;
        this.creator = creator;
        this.createTime = createTime;
        this.changeReason = changeReason;
        this.changeDocument = changeDocument;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public String getChangeClass() { return changeClass; }
    public void setChangeClass(String changeClass) { this.changeClass = changeClass; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getChangeDocument() { return changeDocument; }
    public void setChangeDocument(String changeDocument) { this.changeDocument = changeDocument; }

    public List<String> getBomIds() { return bomIds; }
    public void setBomIds(List<String> bomIds) { this.bomIds = bomIds; }
}
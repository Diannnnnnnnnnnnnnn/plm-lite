package com.example.document_service.dto.response;

import com.example.plm.common.model.Status;
import com.example.plm.common.model.Stage;

public class DocumentResponse {
    private String id;
    private String masterId;
    private String title;
    private Status status;
    private Stage stage;
    private String version; // "v<revision>.<version>"
    private String fileKey;

    public DocumentResponse() {}

    public DocumentResponse(String id, String masterId, String title, Status status, Stage stage, String version) {
        this.id = id;
        this.masterId = masterId;
        this.title = title;
        this.status = status;
        this.stage = stage;
        this.version = version;
    }

    public DocumentResponse(String id, String masterId, String title, Status status, Stage stage, String version, String fileKey) {
        this.id = id;
        this.masterId = masterId;
        this.title = title;
        this.status = status;
        this.stage = stage;
        this.version = version;
        this.fileKey = fileKey;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMasterId() { return masterId; }
    public void setMasterId(String masterId) { this.masterId = masterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }
}

package com.example.document_service.dto.response;

import java.time.LocalDateTime;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;

public class DocumentResponse {
    private String id;
    private String masterId;
    private String title;
    private String description;
    private Status status;
    private Stage stage;
    private String version; // "v<revision>.<version>"
    private int revision;
    private int versionNumber;
    private String fileKey;
    private String creator;
    private LocalDateTime createTime;
    private MasterInfo master;

    public DocumentResponse() {}

    public static class MasterInfo {
        private String id;
        private String documentNumber;
        private String creator;
        private LocalDateTime createTime;

        public MasterInfo() {}

        public MasterInfo(String id, String documentNumber, String creator, LocalDateTime createTime) {
            this.id = id;
            this.documentNumber = documentNumber;
            this.creator = creator;
            this.createTime = createTime;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public int getRevision() { return revision; }
    public void setRevision(int revision) { this.revision = revision; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public MasterInfo getMaster() { return master; }
    public void setMaster(MasterInfo master) { this.master = master; }
}

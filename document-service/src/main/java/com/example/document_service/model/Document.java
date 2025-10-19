package com.example.document_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "document")
public class Document {
    @Id
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "master_id")
    private DocumentMaster master;

    private int version;
    private int revision;

    @Enumerated(EnumType.STRING)
    private Stage stage;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    private String title;
    private String description;
    private String creator;
    private LocalDateTime createTime = LocalDateTime.now();
    private String fileKey;
    private String bomId;  // Related BOM ID
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;  // Flag to indicate if this is the current/active version

    public Document() {
        this.id = UUID.randomUUID().toString();
    }

    // Transient computed property
    @Transient
    public String getFullVersion() {
        return "v" + revision + "." + version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentMaster getMaster() {
        return master;
    }

    public void setMaster(DocumentMaster master) {
        this.master = master;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getBomId() {
        return bomId;
    }

    public void setBomId(String bomId) {
        this.bomId = bomId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", master=" + master +
                ", version=" + version +
                ", revision=" + revision +
                ", stage=" + stage +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creator='" + creator + '\'' +
                ", createTime=" + createTime +
                ", fileKey='" + fileKey + '\'' +
                '}';
    }
}

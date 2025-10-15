package com.example.bom_service.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "bom_headers")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class BomHeader {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String documentId;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private String creator;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private com.example.plm.common.model.Stage stage;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private com.example.plm.common.model.Status status;

    @Column
    @JsonProperty("parentId")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String parentId;  // ID of parent BOM, null for root BOMs

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BomItem> items;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    @Column
    private LocalDateTime deleteTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public com.example.plm.common.model.Stage getStage() {
        return stage;
    }

    public void setStage(com.example.plm.common.model.Stage stage) {
        this.stage = stage;
    }

    public com.example.plm.common.model.Status getStatus() {
        return status;
    }

    public void setStatus(com.example.plm.common.model.Status status) {
        this.status = status;
    }

    public List<BomItem> getItems() {
        return items;
    }

    public void setItems(List<BomItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

package com.example.bom_service.model;

import jakarta.persistence.*;
import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Part")
public class Part {
    @Id
    @Column(name = "bigintid")
    private String id;
    
    @Column(name = "titlechar", nullable = false)
    private String title;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Stage stage;
    
    @Column
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(nullable = false)
    private String level;
    
    @Column(nullable = false)
    private String creator;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    @Column
    private LocalDateTime deleteTime;
    
    // Parent-child relationships through PartUsage
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartUsage> childUsages;
    
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartUsage> parentUsages;
    
    // Document relationships
    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentPartLink> documentLinks;
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.IN_WORK; // Default status
        }
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public List<PartUsage> getChildUsages() {
        return childUsages;
    }

    public void setChildUsages(List<PartUsage> childUsages) {
        this.childUsages = childUsages;
    }

    public List<PartUsage> getParentUsages() {
        return parentUsages;
    }

    public void setParentUsages(List<PartUsage> parentUsages) {
        this.parentUsages = parentUsages;
    }

    public List<DocumentPartLink> getDocumentLinks() {
        return documentLinks;
    }

    public void setDocumentLinks(List<DocumentPartLink> documentLinks) {
        this.documentLinks = documentLinks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
}

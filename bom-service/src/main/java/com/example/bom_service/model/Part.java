package com.example.bom_service.model;

import jakarta.persistence.*;
import com.example.plm.common.model.Stage;
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
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Stage stage;
    
    @Column(nullable = false)
    private String level;
    
    @Column(nullable = false)
    private String creator;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
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
}

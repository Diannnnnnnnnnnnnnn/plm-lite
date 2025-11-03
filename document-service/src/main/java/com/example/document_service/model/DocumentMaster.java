package com.example.document_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "DocumentMaster")
public class DocumentMaster {

    @Id
    private String id;
    private String title;
    private String creator;
    private String category;
    private LocalDateTime createTime = LocalDateTime.now();

    public DocumentMaster() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

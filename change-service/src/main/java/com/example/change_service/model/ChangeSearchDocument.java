package com.example.change_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "changes")
public class ChangeSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Keyword)
    private String stage;

    @Field(type = FieldType.Keyword)
    private String changeClass;

    @Field(type = FieldType.Keyword)
    private String product;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String creator;

    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String changeReason;

    @Field(type = FieldType.Keyword)
    private List<String> affectedDocumentIds;

    @Field(type = FieldType.Keyword)
    private List<String> affectedPartIds;

    public ChangeSearchDocument() {}

    public ChangeSearchDocument(String id, String title, String stage, String changeClass, String product,
                               String status, String creator, LocalDateTime createTime, String changeReason,
                               List<String> affectedDocumentIds, List<String> affectedPartIds) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.changeClass = changeClass;
        this.product = product;
        this.status = status;
        this.creator = creator;
        this.createTime = createTime;
        this.changeReason = changeReason;
        this.affectedDocumentIds = affectedDocumentIds;
        this.affectedPartIds = affectedPartIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getChangeClass() { return changeClass; }
    public void setChangeClass(String changeClass) { this.changeClass = changeClass; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public List<String> getAffectedDocumentIds() { return affectedDocumentIds; }
    public void setAffectedDocumentIds(List<String> affectedDocumentIds) { this.affectedDocumentIds = affectedDocumentIds; }

    public List<String> getAffectedPartIds() { return affectedPartIds; }
    public void setAffectedPartIds(List<String> affectedPartIds) { this.affectedPartIds = affectedPartIds; }
}


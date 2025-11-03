package com.example.change_service.model;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Changes")
@Table(name = "Changes")
public class Changes {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Stage stage;

    @NotBlank
    @Column(name = "class", nullable = false)
    private String changeClass;

    @NotBlank
    @Column(name = "product", nullable = false)
    private String product;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Status status;

    @NotBlank
    @Column(name = "creator", nullable = false)
    private String creator;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @NotBlank
    @Column(name = "change_reason", nullable = false)
    private String changeReason;

    @NotBlank
    @Column(name = "change_document", nullable = false)
    private String changeDocument;

    @OneToMany(mappedBy = "change", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChangeDocument> changeDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "change", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChangePart> changeParts = new ArrayList<>();

    public Changes() {}

    public Changes(String id, String title, Stage stage, String changeClass, String product,
                  Status status, String creator, LocalDateTime createTime,
                  String changeReason, String changeDocument) {
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

    public List<ChangeDocument> getChangeDocuments() { return changeDocuments; }
    public void setChangeDocuments(List<ChangeDocument> changeDocuments) { this.changeDocuments = changeDocuments; }

    public List<ChangePart> getChangeParts() { return changeParts; }
    public void setChangeParts(List<ChangePart> changeParts) { this.changeParts = changeParts; }
}

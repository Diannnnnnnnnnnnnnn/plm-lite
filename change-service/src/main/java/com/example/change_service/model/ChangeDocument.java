package com.example.change_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ChangeDocument")
public class ChangeDocument {

    @Id
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changetask_id", nullable = false)
    private Changes change;

    @NotBlank
    @Column(name = "document_id", nullable = false)
    private String documentId;

    public ChangeDocument() {}

    public ChangeDocument(String id, Changes change, String documentId) {
        this.id = id;
        this.change = change;
        this.documentId = documentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Changes getChange() { return change; }
    public void setChange(Changes change) { this.change = change; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}


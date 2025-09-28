package com.example.plm.change.model;

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
    private Change change;

    @NotBlank
    @Column(name = "document_id", nullable = false)
    private String documentId;

    public ChangeDocument() {}

    public ChangeDocument(String id, Change change, String documentId) {
        this.id = id;
        this.change = change;
        this.documentId = documentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Change getChange() { return change; }
    public void setChange(Change change) { this.change = change; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}
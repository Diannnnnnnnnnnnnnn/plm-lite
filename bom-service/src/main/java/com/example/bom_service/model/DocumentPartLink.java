package com.example.bom_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DocumentPartLink")
public class DocumentPartLink {
    @Id
    @Column(name = "link_id")
    private String linkId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;
    
    @Column(name = "document_id", nullable = false)
    private String documentId;

    // Getters and Setters
    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}

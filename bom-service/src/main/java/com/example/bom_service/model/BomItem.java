package com.example.bom_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bom_items")
public class BomItem {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "header_id", nullable = false)
    private BomHeader header;
    
    @Column(nullable = false)
    private String partNumber;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Double quantity;
    
    @Column(nullable = false)
    private String unit;
    
    private String reference;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BomHeader getHeader() {
        return header;
    }

    public void setHeader(BomHeader header) {
        this.header = header;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

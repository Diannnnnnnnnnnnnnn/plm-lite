package com.example.bom_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PartUsage")
public class PartUsage {
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Part parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Part child;
    
    @Column(nullable = false)
    private Integer quantity;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Part getParent() {
        return parent;
    }

    public void setParent(Part parent) {
        this.parent = parent;
    }

    public Part getChild() {
        return child;
    }

    public void setChild(Part child) {
        this.child = child;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

package com.example.bom_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ChangePart")
public class ChangePart {
    @Id
    private String id;
    
    @Column(name = "changetask_id", nullable = false)
    private String changeTaskId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChangeTaskId() {
        return changeTaskId;
    }

    public void setChangeTaskId(String changeTaskId) {
        this.changeTaskId = changeTaskId;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}

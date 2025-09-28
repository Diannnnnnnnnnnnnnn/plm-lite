package com.example.plm.change.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ChangePart")
public class ChangePart {

    @Id
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changetask_id", nullable = false)
    private Change change;

    @NotBlank
    @Column(name = "part_id", nullable = false)
    private String partId;

    public ChangePart() {}

    public ChangePart(String id, Change change, String partId) {
        this.id = id;
        this.change = change;
        this.partId = partId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Change getChange() { return change; }
    public void setChange(Change change) { this.change = change; }

    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }
}
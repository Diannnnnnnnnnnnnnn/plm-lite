package com.example.plm.change.model;

import jakarta.persistence.*;

@Entity
@Table(name = "change_bom")
public class ChangeBom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_id", nullable = false)
    private Change change;

    @Column(name = "bom_id", nullable = false)
    private String bomId;

    public ChangeBom() {}

    public ChangeBom(Change change, String bomId) {
        this.change = change;
        this.bomId = bomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public String getBomId() {
        return bomId;
    }

    public void setBomId(String bomId) {
        this.bomId = bomId;
    }
}

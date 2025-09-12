package com.example.bom_service.dto.response;

import java.util.List;

public class BomHierarchyResponse {
    private String partId;
    private String partTitle;
    private String level;
    private Integer quantity; // Quantity at this level
    private List<BomHierarchyResponse> children;

    // Getters and Setters
    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getPartTitle() {
        return partTitle;
    }

    public void setPartTitle(String partTitle) {
        this.partTitle = partTitle;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<BomHierarchyResponse> getChildren() {
        return children;
    }

    public void setChildren(List<BomHierarchyResponse> children) {
        this.children = children;
    }
}

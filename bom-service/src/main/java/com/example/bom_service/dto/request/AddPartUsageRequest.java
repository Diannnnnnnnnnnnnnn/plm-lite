package com.example.bom_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AddPartUsageRequest {
    
    @NotBlank(message = "Parent part ID is required")
    private String parentPartId;
    
    @NotBlank(message = "Child part ID is required")
    private String childPartId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    // Getters and Setters
    public String getParentPartId() {
        return parentPartId;
    }

    public void setParentPartId(String parentPartId) {
        this.parentPartId = parentPartId;
    }

    public String getChildPartId() {
        return childPartId;
    }

    public void setChildPartId(String childPartId) {
        this.childPartId = childPartId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

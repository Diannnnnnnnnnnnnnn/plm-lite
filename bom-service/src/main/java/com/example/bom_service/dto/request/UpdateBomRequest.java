package com.example.bom_service.dto.request;

import com.example.plm.common.model.Stage;

import java.util.List;

public class UpdateBomRequest {
    private String description;
    private Stage stage;
    private List<BomItemRequest> items;

    public static class BomItemRequest {
        private String id; // Optional - if present, update existing item
        private String partNumber;
        private String description;
        private Double quantity;
        private String unit;
        private String reference;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public List<BomItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BomItemRequest> items) {
        this.items = items;
    }
}

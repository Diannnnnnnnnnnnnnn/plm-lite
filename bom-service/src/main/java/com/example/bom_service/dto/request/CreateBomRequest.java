package com.example.bom_service.dto.request;

import com.example.plm.common.model.Stage;
import java.util.List;

public class CreateBomRequest {
    private String documentId;
    private String description;
    private String creator;
    private Stage stage;
    private String parentId;
    private List<BomItemRequest> items;

    public static class BomItemRequest {
        private String partNumber;
        private String description;
        private Double quantity;
        private String unit;
        private String reference;

        // Getters and Setters
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

    // Getters and Setters for main class
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

package com.example.bom_service.dto.response;

import com.example.plm.common.model.Stage;
import com.example.plm.common.model.Status;
import java.time.LocalDateTime;
import java.util.List;

public class PartResponse {
    private String id;
    private String title;
    private String description;
    private Stage stage;
    private Status status;
    private String level;
    private String creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean deleted;
    private LocalDateTime deleteTime;
    private List<PartUsageResponse> childUsages;
    private List<String> documentIds;

    public static class PartUsageResponse {
        private String id;
        private String childPartId;
        private String childPartTitle;
        private Integer quantity;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getChildPartId() {
            return childPartId;
        }

        public void setChildPartId(String childPartId) {
            this.childPartId = childPartId;
        }

        public String getChildPartTitle() {
            return childPartTitle;
        }

        public void setChildPartTitle(String childPartTitle) {
            this.childPartTitle = childPartTitle;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<PartUsageResponse> getChildUsages() {
        return childUsages;
    }

    public void setChildUsages(List<PartUsageResponse> childUsages) {
        this.childUsages = childUsages;
    }

    public List<String> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(List<String> documentIds) {
        this.documentIds = documentIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }
}

package com.example.bom_service.dto.request;

import com.example.plm.common.model.Stage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePartRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Stage is required")
    private Stage stage;
    
    @NotBlank(message = "Level is required")
    private String level;
    
    @NotBlank(message = "Creator is required")
    private String creator;

    // Getters and Setters
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
}

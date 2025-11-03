package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BOM search result DTO (placeholder for Phase 3)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomSearchResult {
    private String id;
    private String description;
    private String creator;
    private String stage;
    private String status;
    private LocalDateTime createTime;
    private Float score;
    private String type = "BOM";
    private List<BomItemInfo> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomItemInfo {
        private String partId;
        private String partTitle;
        private String partDescription;
        private Integer quantity;
        private String unit;
    }
}


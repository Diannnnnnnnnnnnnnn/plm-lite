package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Part search result DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartSearchResult {
    private String id;
    private String title;
    private String description;
    private String stage;
    private String status;
    private String level;
    private String creator;
    private LocalDateTime createTime;
    private Float score;
    private String type = "PART";
}


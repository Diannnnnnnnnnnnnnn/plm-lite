package com.example.graph_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for syncing Part Usage (BOM hierarchy) from BOM Service to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartUsageRequest {
    private String parentPartId;
    private String childPartId;
    private Integer quantity;
}


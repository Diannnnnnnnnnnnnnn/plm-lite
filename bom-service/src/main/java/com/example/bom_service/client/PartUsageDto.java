package com.example.bom_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for syncing Part Usage (BOM hierarchy) to Graph Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartUsageDto {
    private String parentPartId;
    private String childPartId;
    private Integer quantity;
}


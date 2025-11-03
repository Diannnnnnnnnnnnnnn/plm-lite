package com.example.plm.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified search response containing results from all indices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchResponse {
    private String query;
    private long totalHits;
    private long took; // Time in milliseconds
    private List<DocumentSearchResult> documents = new ArrayList<>();
    private List<BomSearchResult> boms = new ArrayList<>();
    private List<PartSearchResult> parts = new ArrayList<>();
    private List<ChangeSearchResult> changes = new ArrayList<>();
    private List<TaskSearchResult> tasks = new ArrayList<>();
    
    /**
     * Get all results combined (for simple display)
     */
    public List<Object> getAllResults() {
        List<Object> all = new ArrayList<>();
        all.addAll(documents);
        all.addAll(boms);
        all.addAll(parts);
        all.addAll(changes);
        all.addAll(tasks);
        return all;
    }
}


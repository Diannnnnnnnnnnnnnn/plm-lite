package com.example.plm.search.controller;

import com.example.plm.search.model.*;
import com.example.plm.search.service.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified search controller
 * Exposes search endpoints for the Global Search frontend
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // Allow frontend access
public class SearchController {

    private final UnifiedSearchService searchService;

    /**
     * Unified search across all entities
     * GET /api/v1/search?q=query
     */
    @GetMapping
    public UnifiedSearchResponse search(@RequestParam(value = "q", required = false) String query) {
        log.info("Unified search request: query='{}'", query);
        return searchService.searchAll(query);
    }

    /**
     * Search documents only
     * GET /api/v1/search/documents?q=query
     */
    @GetMapping("/documents")
    public List<DocumentSearchResult> searchDocuments(@RequestParam(value = "q", required = false) String query) {
        log.info("Document search request: query='{}'", query);
        return searchService.searchDocuments(query);
    }

    /**
     * Search BOMs only
     * GET /api/v1/search/boms?q=query
     */
    @GetMapping("/boms")
    public List<BomSearchResult> searchBoms(@RequestParam(value = "q", required = false) String query) {
        log.info("BOM search request: query='{}'", query);
        return searchService.searchBoms(query);
    }

    /**
     * Search Parts only
     * GET /api/v1/search/parts?q=query
     */
    @GetMapping("/parts")
    public List<PartSearchResult> searchParts(@RequestParam(value = "q", required = false) String query) {
        log.info("Part search request: query='{}'", query);
        return searchService.searchParts(query);
    }

    /**
     * Search Changes only
     * GET /api/v1/search/changes?q=query
     */
    @GetMapping("/changes")
    public List<ChangeSearchResult> searchChanges(@RequestParam(value = "q", required = false) String query) {
        log.info("Change search request: query='{}'", query);
        return searchService.searchChanges(query);
    }

    /**
     * Search Tasks only
     * GET /api/v1/search/tasks?q=query
     */
    @GetMapping("/tasks")
    public List<TaskSearchResult> searchTasks(@RequestParam(value = "q", required = false) String query) {
        log.info("Task search request: query='{}'", query);
        return searchService.searchTasks(query);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "Search Service is running";
    }
}


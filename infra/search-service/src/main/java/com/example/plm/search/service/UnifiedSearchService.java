package com.example.plm.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.plm.search.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified search service that queries all Elasticsearch indices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    /**
     * Search across all indices
     */
    public UnifiedSearchResponse searchAll(String queryString) {
        long startTime = System.currentTimeMillis();
        
        UnifiedSearchResponse response = new UnifiedSearchResponse();
        response.setQuery(queryString);

        try {
            // Search all indices
            List<DocumentSearchResult> documents = searchDocuments(queryString);
            List<BomSearchResult> boms = searchBoms(queryString);
            List<PartSearchResult> parts = searchParts(queryString);
            List<ChangeSearchResult> changes = searchChanges(queryString);
            List<TaskSearchResult> tasks = searchTasks(queryString);
            
            response.setDocuments(documents);
            response.setBoms(boms);
            response.setParts(parts);
            response.setChanges(changes);
            response.setTasks(tasks);

            // Calculate total hits
            long totalHits = documents.size() + boms.size() + parts.size() + changes.size() + tasks.size();
            response.setTotalHits(totalHits);

        } catch (Exception e) {
            log.error("Error performing unified search", e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }

        long took = System.currentTimeMillis() - startTime;
        response.setTook(took);

        return response;
    }

    /**
     * Search documents index
     */
    public List<DocumentSearchResult> searchDocuments(String queryString) {
        try {
            Query query;
            
            if (queryString == null || queryString.trim().isEmpty()) {
                // Match all documents if no query
                query = Query.of(q -> q.matchAll(ma -> ma));
            } else {
                // Multi-match query across title and description
                query = Query.of(q -> q
                    .multiMatch(mm -> mm
                        .query(queryString)
                        .fields("title^2", "description", "documentNumber", "category", "creator")
                    )
                );
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("documents")
                .query(query)
                .size(100)  // Max results per index
            );

            SearchResponse<JsonNode> searchResponse = elasticsearchClient.search(
                searchRequest,
                JsonNode.class
            );

            List<DocumentSearchResult> results = new ArrayList<>();
            for (Hit<JsonNode> hit : searchResponse.hits().hits()) {
                DocumentSearchResult result = mapToDocumentResult(hit);
                results.add(result);
            }

            log.info("Found {} documents for query: '{}'", results.size(), queryString);
            return results;

        } catch (Exception e) {
            log.error("Error searching documents", e);
            return new ArrayList<>();
        }
    }

    /**
     * Search BOMs index
     */
    public List<BomSearchResult> searchBoms(String queryString) {
        try {
            Query query = buildQuery(queryString, "description^2", "creator", "stage", "status", 
                                    "items.partTitle^1.5", "items.partDescription");

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("boms")
                .query(query)
                .size(100)
            );

            SearchResponse<JsonNode> searchResponse = elasticsearchClient.search(
                searchRequest,
                JsonNode.class
            );

            List<BomSearchResult> results = new ArrayList<>();
            for (Hit<JsonNode> hit : searchResponse.hits().hits()) {
                BomSearchResult result = mapToBomResult(hit);
                results.add(result);
            }

            log.info("Found {} BOMs for query: '{}'", results.size(), queryString);
            return results;

        } catch (Exception e) {
            log.error("Error searching BOMs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Search Parts index
     */
    public List<PartSearchResult> searchParts(String queryString) {
        try {
            // Build main query
            Query mainQuery = buildQuery(queryString, "title^2", "description", "level", "creator", "stage", "status");
            
            // Add filter to exclude deleted parts
            Query filterQuery = Query.of(q -> q
                .bool(b -> b
                    .must(mainQuery)
                    .mustNot(mn -> mn
                        .term(t -> t
                            .field("deleted")
                            .value(true)
                        )
                    )
                )
            );

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("parts")
                .query(filterQuery)
                .size(100)
            );

            SearchResponse<JsonNode> searchResponse = elasticsearchClient.search(
                searchRequest,
                JsonNode.class
            );

            List<PartSearchResult> results = new ArrayList<>();
            for (Hit<JsonNode> hit : searchResponse.hits().hits()) {
                PartSearchResult result = mapToPartResult(hit);
                results.add(result);
            }

            log.info("Found {} Parts for query: '{}'", results.size(), queryString);
            return results;

        } catch (Exception e) {
            log.error("Error searching Parts", e);
            return new ArrayList<>();
        }
    }

    /**
     * Search Changes index
     */
    public List<ChangeSearchResult> searchChanges(String queryString) {
        try {
            Query query = buildQuery(queryString, "title^2", "description", "changeReason", "creator", "stage", "status");

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("changes")
                .query(query)
                .size(100)
            );

            SearchResponse<JsonNode> searchResponse = elasticsearchClient.search(
                searchRequest,
                JsonNode.class
            );

            List<ChangeSearchResult> results = new ArrayList<>();
            for (Hit<JsonNode> hit : searchResponse.hits().hits()) {
                ChangeSearchResult result = mapToChangeResult(hit);
                results.add(result);
            }

            log.info("Found {} Changes for query: '{}'", results.size(), queryString);
            return results;

        } catch (Exception e) {
            log.error("Error searching Changes", e);
            return new ArrayList<>();
        }
    }

    /**
     * Search Tasks index
     */
    public List<TaskSearchResult> searchTasks(String queryString) {
        try {
            Query query = buildQuery(queryString, "name^2", "description", "status", "assignedTo");

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("tasks")
                .query(query)
                .size(100)
            );

            SearchResponse<JsonNode> searchResponse = elasticsearchClient.search(
                searchRequest,
                JsonNode.class
            );

            List<TaskSearchResult> results = new ArrayList<>();
            for (Hit<JsonNode> hit : searchResponse.hits().hits()) {
                TaskSearchResult result = mapToTaskResult(hit);
                results.add(result);
            }

            log.info("Found {} Tasks for query: '{}'", results.size(), queryString);
            return results;

        } catch (Exception e) {
            log.error("Error searching Tasks", e);
            return new ArrayList<>();
        }
    }

    /**
     * Build query (match-all if empty, multi-match otherwise)
     */
    private Query buildQuery(String queryString, String... fields) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return Query.of(q -> q.matchAll(ma -> ma));
        } else {
            return Query.of(q -> q.multiMatch(mm -> mm
                .query(queryString)
                .fields(List.of(fields))
            ));
        }
    }

    /**
     * Map Elasticsearch hit to DocumentSearchResult
     */
    private DocumentSearchResult mapToDocumentResult(Hit<JsonNode> hit) {
        JsonNode source = hit.source();
        
        DocumentSearchResult result = new DocumentSearchResult();
        result.setId(hit.id());
        result.setTitle(getStringField(source, "title"));
        result.setDescription(getStringField(source, "description"));
        result.setDocumentNumber(getStringField(source, "documentNumber"));
        result.setMasterId(getStringField(source, "masterId"));
        result.setStatus(getStringField(source, "status"));
        result.setStage(getStringField(source, "stage"));
        result.setCategory(getStringField(source, "category"));
        result.setContentType(getStringField(source, "contentType"));
        result.setCreator(getStringField(source, "creator"));
        result.setFileSize(getLongField(source, "fileSize"));
        result.setVersion(getStringField(source, "version"));
        // Note: Dates would need custom parsing from JsonNode if needed
        result.setIsActive(getBooleanField(source, "isActive"));
        result.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
        result.setType("DOCUMENT");
        
        return result;
    }

    // Helper methods to safely extract fields from JsonNode
    private String getStringField(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() 
            ? node.get(fieldName).asText() 
            : null;
    }

    private Long getLongField(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() 
            ? node.get(fieldName).asLong() 
            : null;
    }

    private Boolean getBooleanField(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() 
            ? node.get(fieldName).asBoolean() 
            : null;
    }

    /**
     * Map Elasticsearch hit to BomSearchResult
     */
    private BomSearchResult mapToBomResult(Hit<JsonNode> hit) {
        JsonNode source = hit.source();
        
        BomSearchResult result = new BomSearchResult();
        result.setId(hit.id());
        result.setDescription(getStringField(source, "description"));
        result.setCreator(getStringField(source, "creator"));
        result.setStage(getStringField(source, "stage"));
        result.setStatus(getStringField(source, "status"));
        result.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
        
        // Map BOM items
        if (source.has("items") && source.get("items").isArray()) {
            List<BomSearchResult.BomItemInfo> items = new ArrayList<>();
            for (JsonNode itemNode : source.get("items")) {
                BomSearchResult.BomItemInfo item = new BomSearchResult.BomItemInfo();
                item.setPartId(getStringField(itemNode, "partId"));
                item.setPartTitle(getStringField(itemNode, "partTitle"));
                item.setPartDescription(getStringField(itemNode, "partDescription"));
                item.setQuantity(itemNode.has("quantity") ? itemNode.get("quantity").asInt() : null);
                item.setUnit(getStringField(itemNode, "unit"));
                items.add(item);
            }
            result.setItems(items);
        }
        
        return result;
    }

    /**
     * Map Elasticsearch hit to PartSearchResult
     */
    private PartSearchResult mapToPartResult(Hit<JsonNode> hit) {
        JsonNode source = hit.source();
        
        PartSearchResult result = new PartSearchResult();
        result.setId(hit.id());
        result.setTitle(getStringField(source, "title"));
        result.setDescription(getStringField(source, "description"));
        result.setStage(getStringField(source, "stage"));
        result.setStatus(getStringField(source, "status"));
        result.setLevel(getStringField(source, "level"));
        result.setCreator(getStringField(source, "creator"));
        result.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
        
        return result;
    }

    /**
     * Map Elasticsearch hit to ChangeSearchResult
     */
    private ChangeSearchResult mapToChangeResult(Hit<JsonNode> hit) {
        JsonNode source = hit.source();
        
        ChangeSearchResult result = new ChangeSearchResult();
        result.setId(hit.id());
        result.setTitle(getStringField(source, "title"));
        result.setDescription(getStringField(source, "description"));
        result.setStatus(getStringField(source, "status"));
        result.setStage(getStringField(source, "stage"));
        result.setChangeClass(getStringField(source, "changeClass"));
        result.setCreator(getStringField(source, "creator"));
        result.setChangeReason(getStringField(source, "changeReason"));
        result.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
        
        return result;
    }

    /**
     * Map Elasticsearch hit to TaskSearchResult
     */
    private TaskSearchResult mapToTaskResult(Hit<JsonNode> hit) {
        JsonNode source = hit.source();
        
        TaskSearchResult result = new TaskSearchResult();
        result.setId(hit.id());
        result.setTaskName(getStringField(source, "name"));
        result.setDescription(getStringField(source, "description"));
        result.setStatus(getStringField(source, "status"));
        result.setAssignee(getStringField(source, "assignedTo"));
        result.setScore(hit.score() != null ? hit.score().floatValue() : 0f);
        
        return result;
    }
}


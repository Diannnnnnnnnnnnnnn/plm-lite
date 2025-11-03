# Elasticsearch Integration Plan for PLM-Lite

## Executive Summary

This document outlines a comprehensive plan to integrate Elasticsearch into the PLM-Lite microservices system. Elasticsearch will provide powerful full-text search, filtering, and analytics capabilities across all major entities (Documents, Parts, BOMs, Changes, Tasks, Users).

## Current Status Analysis

### ✅ Already Done
- **Dependencies Added**: document-service, change-service, task-service already have `spring-boot-starter-data-elasticsearch`
- **Search Service Scaffold**: Empty search-service exists in `infra/search-service/`
- **Partial Implementation**: 
  - document-service has SearchGateway and SearchServiceClient (currently disabled)
  - change-service has ChangeSearchDocument and ChangeSearchRepository (currently disabled)
  - task-service has TaskDocument and TaskSearchRepository (currently disabled)

### ❌ Current Issues
- Elasticsearch is **explicitly disabled** in @SpringBootApplication exclude lists
- search-service has no implementation (only basic scaffold)
- Integration code is commented out or unused
- No Elasticsearch instance configured
- bom-service and user-service have no ES integration

## Architecture Overview

### Approach: Hybrid Model

We'll use a **hybrid approach** combining centralized and distributed search:

```
┌─────────────────────────────────────────────────────────┐
│                   API Gateway (8080)                     │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────────┐
        │               │                   │
┌───────▼─────┐  ┌─────▼──────┐   ┌───────▼────────┐
│  Document   │  │   Change   │   │  Task Service  │
│  Service    │  │  Service   │   │                │
│  (8081)     │  │  (8084)    │   │    (8082)      │
└──────┬──────┘  └──────┬─────┘   └────────┬───────┘
       │                │                   │
       │ Sync on        │ Sync on          │ Sync on
       │ Create/Update  │ Create/Update    │ Create/Update
       │                │                   │
       └────────────────┼───────────────────┘
                        │
                ┌───────▼────────┐
                │   Elasticsearch│
                │   Cluster      │
                │   (9200)       │
                └────────────────┘
                        │
                ┌───────▼────────┐
                │ Search Service │
                │   (8085)       │
                │ (Unified Query)│
                └────────────────┘
```

### Search Patterns

1. **Direct Indexing**: Each service indexes its own entities to Elasticsearch
2. **Centralized Search**: search-service provides unified search API across all indices
3. **Service-Specific Search**: Each service also exposes its own search endpoints

## Implementation Phases

---

## Phase 1: Infrastructure Setup (Day 1)

### 1.1 Install Elasticsearch

**Option A: Docker (Recommended for Development)**
```yaml
# docker-compose.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: plm-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - plm-network

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: plm-kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - plm-network

volumes:
  elasticsearch-data:
    driver: local

networks:
  plm-network:
    driver: bridge
```

**Option B: Windows Installation**
```batch
# Download and install from https://www.elastic.co/downloads/elasticsearch
# Start with: elasticsearch\bin\elasticsearch.bat
```

### 1.2 Verify Installation
```bash
curl http://localhost:9200
# Should return cluster info
```

### 1.3 Create Startup Scripts

**start-elasticsearch.bat**
```batch
@echo off
echo Starting Elasticsearch...
docker-compose -f docker-compose-elasticsearch.yml up -d
echo.
echo Elasticsearch: http://localhost:9200
echo Kibana: http://localhost:5601
```

**stop-elasticsearch.bat**
```batch
@echo off
docker-compose -f docker-compose-elasticsearch.yml down
```

---

## Phase 2: Document Service Integration (Day 2-3)

### 2.1 Enable Elasticsearch

**File**: `document-service/src/main/java/com/example/document_service/DocumentServiceApplication.java`

**Action**: Remove Elasticsearch from exclude list
```java
@SpringBootApplication(
    exclude = {
        // Remove these lines:
        // ElasticsearchRestClientAutoConfiguration.class,
        // ElasticsearchDataAutoConfiguration.class,
    }
)
```

### 2.2 Create Document Index Model

**File**: `document-service/src/main/java/com/example/document_service/elasticsearch/DocumentSearchIndex.java`

```java
package com.example.document_service.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(indexName = "documents")
public class DocumentSearchIndex {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String masterId;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Keyword)
    private String stage;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private String contentType;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Long)
    private Long fileSize;
    
    @Field(type = FieldType.Integer)
    private Integer version;
    
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
    
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
    
    @Field(type = FieldType.Boolean)
    private Boolean isActive;
    
    // Tags/keywords for better search
    @Field(type = FieldType.Keyword)
    private String[] tags;
}
```

### 2.3 Create Repository

**File**: `document-service/src/main/java/com/example/document_service/elasticsearch/DocumentSearchRepository.java`

```java
package com.example.document_service.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchIndex, String> {
    
    List<DocumentSearchIndex> findByTitleContaining(String title);
    
    List<DocumentSearchIndex> findByCreator(String creator);
    
    List<DocumentSearchIndex> findByStatus(String status);
    
    List<DocumentSearchIndex> findByCategory(String category);
    
    List<DocumentSearchIndex> findByStageAndStatus(String stage, String status);
}
```

### 2.4 Create Search Service

**File**: `document-service/src/main/java/com/example/document_service/service/DocumentSearchService.java`

```java
package com.example.document_service.service;

import com.example.document_service.elasticsearch.DocumentSearchIndex;
import com.example.document_service.elasticsearch.DocumentSearchRepository;
import com.example.document_service.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchService {
    
    private final DocumentSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * Index a document to Elasticsearch
     */
    public void indexDocument(Document document) {
        try {
            DocumentSearchIndex searchIndex = mapToSearchIndex(document);
            searchRepository.save(searchIndex);
            log.info("✅ Document {} indexed to Elasticsearch", document.getId());
        } catch (Exception e) {
            log.error("❌ Failed to index document {}: {}", document.getId(), e.getMessage());
        }
    }
    
    /**
     * Full-text search across title and description
     */
    public List<DocumentSearchIndex> search(String query) {
        // This will search across all text fields
        Criteria criteria = new Criteria("title").contains(query)
                .or("description").contains(query);
        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        SearchHits<DocumentSearchIndex> searchHits = elasticsearchOperations.search(searchQuery, DocumentSearchIndex.class);
        return searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with filters
     */
    public List<DocumentSearchIndex> advancedSearch(
            String query,
            String status,
            String stage,
            String category,
            String creator) {
        
        Criteria criteria = new Criteria();
        
        if (query != null && !query.isEmpty()) {
            criteria = criteria.and(
                new Criteria("title").contains(query)
                    .or("description").contains(query)
            );
        }
        
        if (status != null) {
            criteria = criteria.and(new Criteria("status").is(status));
        }
        
        if (stage != null) {
            criteria = criteria.and(new Criteria("stage").is(stage));
        }
        
        if (category != null) {
            criteria = criteria.and(new Criteria("category").is(category));
        }
        
        if (creator != null) {
            criteria = criteria.and(new Criteria("creator").is(creator));
        }
        
        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        SearchHits<DocumentSearchIndex> searchHits = elasticsearchOperations.search(searchQuery, DocumentSearchIndex.class);
        return searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
    
    /**
     * Delete from index
     */
    public void deleteDocument(String documentId) {
        try {
            searchRepository.deleteById(documentId);
            log.info("✅ Document {} removed from Elasticsearch", documentId);
        } catch (Exception e) {
            log.error("❌ Failed to delete document {} from ES: {}", documentId, e.getMessage());
        }
    }
    
    private DocumentSearchIndex mapToSearchIndex(Document doc) {
        DocumentSearchIndex index = new DocumentSearchIndex();
        index.setId(doc.getId());
        index.setTitle(doc.getTitle());
        index.setDescription(doc.getDescription());
        index.setMasterId(doc.getMaster() != null ? doc.getMaster().getId() : null);
        index.setStatus(doc.getStatus() != null ? doc.getStatus().name() : null);
        index.setStage(doc.getStage() != null ? doc.getStage().name() : null);
        index.setCategory(doc.getMaster() != null ? doc.getMaster().getCategory() : null);
        index.setContentType(doc.getContentType());
        index.setCreator(doc.getCreator());
        index.setFileSize(doc.getFileSize());
        index.setVersion(doc.getVersion());
        index.setCreateTime(doc.getCreateTime());
        index.setUpdateTime(doc.getUpdateTime());
        index.setIsActive(doc.isActive());
        return index;
    }
}
```

### 2.5 Update DocumentServiceImpl

**File**: `document-service/src/main/java/com/example/document_service/service/impl/DocumentServiceImpl.java`

**Action**: Uncomment and update the sync method around line 75-87

```java
@Autowired
private DocumentSearchService documentSearchService;

private void sync(Document d) {
    // Sync to Neo4j Graph Service
    syncDocumentToGraph(d);
    
    // Sync to Elasticsearch
    try {
        documentSearchService.indexDocument(d);
    } catch (Exception e) {
        log.warn("Failed to sync to Elasticsearch: {}", e.getMessage());
    }
}
```

### 2.6 Add Search Controller Endpoints

**File**: `document-service/src/main/java/com/example/document_service/controller/DocumentSearchController.java`

```java
package com.example.document_service.controller;

import com.example.document_service.elasticsearch.DocumentSearchIndex;
import com.example.document_service.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/search")
@RequiredArgsConstructor
public class DocumentSearchController {
    
    private final DocumentSearchService searchService;
    
    @GetMapping
    public List<DocumentSearchIndex> search(@RequestParam String q) {
        return searchService.search(q);
    }
    
    @GetMapping("/advanced")
    public List<DocumentSearchIndex> advancedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String creator) {
        return searchService.advancedSearch(q, status, stage, category, creator);
    }
}
```

### 2.7 Configuration

**File**: `document-service/src/main/resources/application.properties`

**Action**: Add Elasticsearch configuration
```properties
# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5s
spring.elasticsearch.socket-timeout=30s
```

---

## Phase 3: Change Service Integration (Day 4)

### 3.1 Enable Elasticsearch

**File**: `change-service/src/main/java/com/example/plm/change/ChangeServiceApplication.java`

**Action**: Remove Elasticsearch from exclude list (lines 15-19)

### 3.2 Update Existing Model

The service already has `ChangeSearchDocument`. Review and enhance if needed:

**File**: `change-service/src/main/java/com/example/plm/change/model/ChangeSearchDocument.java`

Ensure it has proper annotations for search:
```java
@Document(indexName = "changes")
@Data
public class ChangeSearchDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String changeReason;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Keyword)
    private String stage;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    // Add more fields as needed
}
```

### 3.3 Create Search Service

Similar to DocumentSearchService, create ChangeSearchService with indexing and search methods.

### 3.4 Update ChangeService

Add Elasticsearch indexing in create/update methods:
```java
@Autowired
private ChangeSearchService changeSearchService;

// In create method
changeSearchService.indexChange(savedChange);

// In update method
changeSearchService.indexChange(updatedChange);
```

---

## Phase 4: Task Service Integration (Day 5)

### 4.1 Enable Elasticsearch

**File**: `task-service/src/main/java/com/example/task_service/TaskServiceApplication.java`

**Action**: Remove Elasticsearch from exclude list (lines 9-11)

### 4.2 Update Existing Model

The service already has `TaskDocument`. Enhance annotations:

```java
@Document(indexName = "tasks")
@Data
public class TaskDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Keyword)
    private Long userId;
    
    @Field(type = FieldType.Boolean)
    private boolean completed;
    
    @Field(type = FieldType.Date)
    private LocalDateTime dueDate;
}
```

### 4.3 Uncomment Existing Integration

**File**: `task-service/src/main/java/com/example/task_service/TaskService.java`

The service already has ES integration at lines 85-94. Just ensure it's enabled.

---

## Phase 5: BOM Service Integration (Day 6)

### 5.1 Add Elasticsearch Dependency

**File**: `bom-service/pom.xml`

```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 5.2 Create BOM Index Models

```java
@Document(indexName = "boms")
@Data
public class BomSearchIndex {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String bomType;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
}

@Document(indexName = "parts")
@Data
public class PartSearchIndex {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String partNumber;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private String manufacturer;
}
```

### 5.3 Implement Search Services

Similar pattern as document-service.

---

## Phase 6: User Service Integration (Day 7)

### 6.1 Add Elasticsearch Dependency

**File**: `user-service/pom.xml`

```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

### 6.2 Create User Index Model

```java
@Document(indexName = "users")
@Data
public class UserSearchIndex {
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;
    
    @Field(type = FieldType.Keyword)
    private String email;
    
    @Field(type = FieldType.Keyword)
    private String role;
    
    @Field(type = FieldType.Keyword)
    private String department;
}
```

---

## Phase 7: Unified Search Service (Day 8-9)

### 7.1 Implement search-service

**File**: `infra/search-service/src/main/java/com/example/plm/search/config/ElasticsearchConfig.java`

```java
package com.example.plm.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.plm.search.repository")
public class ElasticsearchConfig {
    // Auto-configuration handles most setup
}
```

### 7.2 Create Unified Search Controller

**File**: `infra/search-service/src/main/java/com/example/plm/search/controller/UnifiedSearchController.java`

```java
package com.example.plm.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class UnifiedSearchController {
    
    private final ElasticsearchOperations operations;
    
    /**
     * Search across ALL indices
     */
    @GetMapping("/global")
    public Map<String, Object> globalSearch(@RequestParam String q) {
        Map<String, Object> results = new HashMap<>();
        
        // Search documents
        results.put("documents", searchIndex("documents", q));
        
        // Search parts
        results.put("parts", searchIndex("parts", q));
        
        // Search boms
        results.put("boms", searchIndex("boms", q));
        
        // Search changes
        results.put("changes", searchIndex("changes", q));
        
        // Search tasks
        results.put("tasks", searchIndex("tasks", q));
        
        // Search users
        results.put("users", searchIndex("users", q));
        
        return results;
    }
    
    /**
     * Search specific entity type
     */
    @GetMapping("/{entityType}")
    public List<Map<String, Object>> searchEntity(
            @PathVariable String entityType,
            @RequestParam String q) {
        return searchIndex(entityType, q);
    }
    
    private List<Map<String, Object>> searchIndex(String indexName, String query) {
        // Implementation for searching a specific index
        // This is a simplified version
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            // Perform search operation
            // Convert to generic map structure
        } catch (Exception e) {
            // Log error
        }
        return results;
    }
}
```

### 7.3 Create Aggregation Service

**File**: `infra/search-service/src/main/java/com/example/plm/search/service/SearchAnalyticsService.java`

```java
package com.example.plm.search.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchAnalyticsService {
    
    /**
     * Get document count by status
     */
    public Map<String, Long> getDocumentsByStatus() {
        // Aggregation query
    }
    
    /**
     * Get most active creators
     */
    public List<Map<String, Object>> getTopCreators() {
        // Aggregation query
    }
    
    /**
     * Get change trends
     */
    public Map<String, Object> getChangeTrends() {
        // Time-based aggregation
    }
}
```

---

## Phase 8: Frontend Integration (Day 10)

### 8.1 Create Search UI Component

**File**: `frontend/src/components/Search/GlobalSearch.js`

```javascript
import React, { useState } from 'react';
import axios from 'axios';

const GlobalSearch = () => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);
    
    const handleSearch = async () => {
        if (!query.trim()) return;
        
        setLoading(true);
        try {
            const response = await axios.get(`http://localhost:8085/api/search/global?q=${query}`);
            setResults(response.data);
        } catch (error) {
            console.error('Search failed:', error);
        } finally {
            setLoading(false);
        }
    };
    
    return (
        <div className="global-search">
            <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                placeholder="Search across all PLM entities..."
            />
            <button onClick={handleSearch} disabled={loading}>
                {loading ? 'Searching...' : 'Search'}
            </button>
            
            {results && (
                <div className="search-results">
                    <SearchResultSection title="Documents" data={results.documents} />
                    <SearchResultSection title="Parts" data={results.parts} />
                    <SearchResultSection title="BOMs" data={results.boms} />
                    <SearchResultSection title="Changes" data={results.changes} />
                    <SearchResultSection title="Tasks" data={results.tasks} />
                </div>
            )}
        </div>
    );
};
```

### 8.2 Add Search to Navigation

Update main navigation to include global search bar.

---

## Phase 9: Data Migration & Reindexing (Day 11)

### 9.1 Create Reindex Controllers

For each service, create a reindex endpoint:

```java
@PostMapping("/admin/reindex")
public ResponseEntity<String> reindexAll() {
    List<Document> allDocs = documentRepository.findAll();
    allDocs.forEach(doc -> searchService.indexDocument(doc));
    return ResponseEntity.ok("Reindexed " + allDocs.size() + " documents");
}
```

### 9.2 Create Master Reindex Script

**File**: `reindex-all-services.bat`

```batch
@echo off
echo Reindexing all PLM entities to Elasticsearch...

curl -X POST http://localhost:8081/api/v1/documents/admin/reindex
echo Documents reindexed

curl -X POST http://localhost:8084/api/changes/admin/reindex
echo Changes reindexed

curl -X POST http://localhost:8082/api/tasks/admin/reindex
echo Tasks reindexed

curl -X POST http://localhost:8089/api/boms/admin/reindex
echo BOMs reindexed

curl -X POST http://localhost:8083/api/users/admin/reindex
echo Users reindexed

echo.
echo All entities reindexed successfully!
```

---

## Phase 10: Testing & Optimization (Day 12-14)

### 10.1 Performance Testing

- Load test with 10,000+ documents
- Measure search response times
- Optimize queries based on results

### 10.2 Index Optimization

```json
// Custom analyzer for better search
PUT /documents/_settings
{
  "analysis": {
    "analyzer": {
      "plm_analyzer": {
        "type": "custom",
        "tokenizer": "standard",
        "filter": ["lowercase", "stop", "snowball"]
      }
    }
  }
}
```

### 10.3 Create Integration Tests

```java
@SpringBootTest
class DocumentSearchServiceTest {
    
    @Test
    void testIndexAndSearch() {
        // Create test document
        // Index to ES
        // Search and verify
    }
}
```

---

## Configuration Summary

### Service Ports
- Elasticsearch: 9200
- Kibana: 5601
- Search Service: 8085
- Document Service: 8081
- Change Service: 8084
- Task Service: 8082
- BOM Service: 8089
- User Service: 8083

### Elasticsearch Indices
- `documents` - Document search
- `parts` - Part search
- `boms` - BOM search
- `changes` - Change search
- `tasks` - Task search
- `users` - User search

---

## Update Startup Scripts

### start-all-services.bat

Add Elasticsearch and search-service:

```batch
REM Start Elasticsearch
echo [Elasticsearch]
echo   Starting...
call start-elasticsearch.bat
timeout /t 20 /nobreak >nul

REM After other services, add:
echo [Search Service]
echo   Port: 8085
echo   Starting in new window...
start "PLM - Search Service (8085)" cmd /k "cd /d %PROJECT_ROOT%\infra\search-service && mvn spring-boot:run"
timeout /t 30 /nobreak >nul
```

---

## Monitoring & Maintenance

### Kibana Dashboards

Create dashboards for:
1. Search query volume
2. Most searched terms
3. Index size trends
4. Search performance metrics

### Health Checks

```java
@RestController
@RequestMapping("/actuator/health")
public class ElasticsearchHealthCheck {
    
    @GetMapping("/elasticsearch")
    public Map<String, String> checkElasticsearch() {
        // Ping ES cluster
        // Return status
    }
}
```

---

## Rollback Plan

If issues arise:

1. **Disable ES in services**: Add back to exclude lists
2. **Stop Elasticsearch**: `docker-compose down`
3. **Remove ES calls**: Comment out indexing code
4. **Revert to MySQL queries**: Use existing repository methods

---

## Success Metrics

- ✅ All 6 indices created and populated
- ✅ Sub-200ms search response time
- ✅ Global search returns results from all entities
- ✅ Frontend search UI functional
- ✅ Reindexing scripts working
- ✅ 100% test coverage for search services

---

## Timeline Summary

| Phase | Duration | Description |
|-------|----------|-------------|
| 1 | 1 day | Infrastructure setup |
| 2 | 2 days | Document service |
| 3 | 1 day | Change service |
| 4 | 1 day | Task service |
| 5 | 1 day | BOM service |
| 6 | 1 day | User service |
| 7 | 2 days | Unified search service |
| 8 | 1 day | Frontend integration |
| 9 | 1 day | Data migration |
| 10 | 3 days | Testing & optimization |
| **Total** | **14 days** | **Complete integration** |

---

## Next Steps

1. **Review this plan** with the team
2. **Set up Elasticsearch** (Phase 1)
3. **Start with Document Service** (Phase 2) as a proof-of-concept
4. **Iterate through remaining services**
5. **Deploy and monitor**

---

## Questions to Decide

1. **Elasticsearch Version**: 8.11.0 or 8.x latest?
2. **Deployment**: Docker vs Bare metal?
3. **Security**: Enable X-Pack security?
4. **Clustering**: Single node or multi-node for production?
5. **Backup Strategy**: Snapshot repository location?

---

## Resources

- Elasticsearch Documentation: https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html
- Spring Data Elasticsearch: https://docs.spring.io/spring-data/elasticsearch/reference/
- Kibana Guide: https://www.elastic.co/guide/en/kibana/current/index.html

---

**Document Version**: 1.0  
**Created**: 2025-10-29  
**Author**: PLM System Architect  
**Status**: Ready for Implementation





# Elasticsearch Integration with Global Search Frontend
## Refined Implementation Plan

> **Goal**: Integrate Elasticsearch with your existing GlobalSearch.js component to enable fast, unified search across ALL PLM entities (Documents, Changes, Tasks, BOMs, Parts, Users).

---

## 📋 Overview

### Current State
- ✅ **GlobalSearch.js** exists with great UI (using mock data)
- ✅ **Elasticsearch dependencies** added to change-service, task-service, document-service
- ✅ **Partial implementations** exist but are disabled
- ❌ **Not connected** to real backend
- ❌ **Elasticsearch** not running

### Target State
```
User types in GlobalSearch.js
    ↓
Frontend calls unified search API
    ↓
Search Service queries Elasticsearch
    ↓
Returns ranked results from all entities
    ↓
GlobalSearch.js displays results with highlighting
```

---

## 🎯 Refined Implementation Plan

### Timeline: 7-10 Days
- **Days 1-2**: Infrastructure + Document Service
- **Days 3-4**: Change, Task, BOM Services
- **Days 5-6**: Unified Search Service + Frontend Integration
- **Day 7**: Testing + Polish

---

## 📅 Day 1-2: Foundation Setup

### Task 1.1: Start Elasticsearch (30 minutes)

**Steps:**
1. Open Command Prompt in project root
2. Run: `start-elasticsearch.bat`
3. Verify at http://localhost:9200
4. Verify Kibana at http://localhost:5601

**Verification:**
```bash
curl http://localhost:9200
# Should return cluster info with version 8.11.0
```

---

### Task 1.2: Document Service - Enable & Implement ES (4 hours)

This is your **proof of concept**. Get one service working first.

#### Step 1: Create Elasticsearch Package & Models

**File**: `document-service/src/main/java/com/example/document_service/elasticsearch/DocumentSearchDocument.java`

```java
package com.example.document_service.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class DocumentSearchDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String documentNumber;
    
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
    
    // Utility method to map from Document entity
    public static DocumentSearchDocument fromDocument(com.example.document_service.model.Document doc) {
        DocumentSearchDocument searchDoc = new DocumentSearchDocument();
        searchDoc.setId(doc.getId());
        searchDoc.setTitle(doc.getTitle());
        searchDoc.setDescription(doc.getDescription());
        searchDoc.setDocumentNumber(doc.getMaster() != null ? doc.getMaster().getDocumentNumber() : null);
        searchDoc.setMasterId(doc.getMaster() != null ? doc.getMaster().getId() : null);
        searchDoc.setStatus(doc.getStatus() != null ? doc.getStatus().name() : null);
        searchDoc.setStage(doc.getStage() != null ? doc.getStage().name() : null);
        searchDoc.setCategory(doc.getMaster() != null ? doc.getMaster().getCategory() : null);
        searchDoc.setContentType(doc.getContentType());
        searchDoc.setCreator(doc.getCreator());
        searchDoc.setFileSize(doc.getFileSize());
        searchDoc.setVersion(doc.getVersion());
        searchDoc.setCreateTime(doc.getCreateTime());
        searchDoc.setUpdateTime(doc.getUpdateTime());
        searchDoc.setIsActive(doc.isActive());
        return searchDoc;
    }
}
```

#### Step 2: Create Repository

**File**: `document-service/src/main/java/com/example/document_service/elasticsearch/DocumentSearchRepository.java`

```java
package com.example.document_service.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchDocument, String> {
    
    List<DocumentSearchDocument> findByTitleContaining(String title);
    
    List<DocumentSearchDocument> findByCreator(String creator);
    
    List<DocumentSearchDocument> findByStatus(String status);
    
    List<DocumentSearchDocument> findByStageAndStatus(String stage, String status);
}
```

#### Step 3: Create Search Service

**File**: `document-service/src/main/java/com/example/document_service/service/DocumentSearchService.java`

```java
package com.example.document_service.service;

import com.example.document_service.elasticsearch.DocumentSearchDocument;
import com.example.document_service.elasticsearch.DocumentSearchRepository;
import com.example.document_service.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchService {
    
    private final DocumentSearchRepository searchRepository;
    
    /**
     * Index a document to Elasticsearch
     */
    public void indexDocument(Document document) {
        try {
            DocumentSearchDocument searchDoc = DocumentSearchDocument.fromDocument(document);
            searchRepository.save(searchDoc);
            log.info("✅ Document {} indexed to Elasticsearch", document.getId());
        } catch (Exception e) {
            log.error("❌ Failed to index document {}: {}", document.getId(), e.getMessage());
        }
    }
    
    /**
     * Delete document from Elasticsearch
     */
    public void deleteDocument(String documentId) {
        try {
            searchRepository.deleteById(documentId);
            log.info("✅ Document {} removed from Elasticsearch", documentId);
        } catch (Exception e) {
            log.error("❌ Failed to delete document {} from ES: {}", documentId, e.getMessage());
        }
    }
    
    /**
     * Search documents by query
     */
    public List<DocumentSearchDocument> search(String query) {
        return searchRepository.findByTitleContaining(query);
    }
}
```

#### Step 4: Update DocumentServiceImpl to Auto-Index

**File**: `document-service/src/main/java/com/example/document_service/service/impl/DocumentServiceImpl.java`

Find the `sync()` method (around line 75) and update:

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
        log.warn("⚠️ Failed to sync to Elasticsearch: {}", e.getMessage());
    }
}
```

Also update the `deleteDocument()` method:

```java
@Override
public void deleteDocument(String documentId) {
    Document document = docRepo.findById(documentId)
            .orElseThrow(() -> new NotFoundException("Document not found"));
    
    // Remove from MySQL
    docRepo.delete(document);
    
    // Remove from Elasticsearch
    try {
        documentSearchService.deleteDocument(documentId);
    } catch (Exception e) {
        log.warn("⚠️ Failed to remove from Elasticsearch: {}", e.getMessage());
    }
}
```

#### Step 5: Add Search Controller Endpoint

**File**: `document-service/src/main/java/com/example/document_service/controller/DocumentSearchController.java`

```java
package com.example.document_service.controller;

import com.example.document_service.elasticsearch.DocumentSearchDocument;
import com.example.document_service.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DocumentSearchController {
    
    private final DocumentSearchService searchService;
    
    @GetMapping("/search/elastic")
    public List<DocumentSearchDocument> searchDocuments(@RequestParam String q) {
        return searchService.search(q);
    }
}
```

#### Step 6: Update Configuration

**File**: `document-service/src/main/resources/application.properties`

Add:
```properties
# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5s
spring.elasticsearch.socket-timeout=30s
spring.data.elasticsearch.repositories.enabled=true
```

#### Step 7: Restart Document Service & Test

```bash
cd document-service
mvn clean spring-boot:run
```

**Test:**
```bash
# Create a document via your frontend or API
# Then check Elasticsearch
curl "http://localhost:9200/documents/_search?pretty"

# Test search endpoint
curl "http://localhost:8081/api/v1/documents/search/elastic?q=test"
```

---

## 📅 Day 3: Change Service ES Integration (3 hours)

**Good news:** Change service already has most ES code! Just enable it.

### Step 1: Enable Elasticsearch

**File**: `change-service/src/main/java/com/example/plm/change/ChangeServiceApplication.java`

Remove ES exclusions (lines 15-19):

```java
@SpringBootApplication(
    scanBasePackages = {"com.example.plm.change", "com.example.plm.common", "com.example.change_service"},
    exclude = {
        // Remove these lines:
        // ElasticsearchRestClientAutoConfiguration.class,
        // org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
        // org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class,
        
        // Keep these:
        EurekaClientAutoConfiguration.class,
        org.springframework.cloud.netflix.eureka.config.EurekaClientConfigServerAutoConfiguration.class
    }
)
```

### Step 2: Update Configuration

**File**: `change-service/src/main/resources/application.yml`

Add under the `spring:` section:

```yaml
spring:
  # ... existing config ...
  
  # Add Elasticsearch config
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 5s
    socket-timeout: 30s
  
  data:
    elasticsearch:
      repositories:
        enabled: true
```

### Step 3: Verify Existing Implementation

The service already has:
- ✅ `ChangeSearchDocument.java` (model)
- ✅ `ChangeSearchRepository.java` (repository)
- ✅ Indexing in `ChangeService.java` (lines 107-122)
- ✅ Search endpoint `/search/elastic` (line 170)

Just verify they're enabled!

### Step 4: Restart & Test

```bash
cd change-service
mvn clean spring-boot:run
```

**Test:**
```bash
# Check Elasticsearch
curl "http://localhost:9200/changes/_search?pretty"

# Test search endpoint
curl "http://localhost:8084/api/changes/search/elastic?query=test"
```

---

## 📅 Day 4: Task & BOM Services (4 hours)

### Task Service (2 hours)

**File**: `task-service/src/main/java/com/example/task_service/TaskServiceApplication.java`

Remove ES exclusions (lines 9-11):

```java
@SpringBootApplication(
    exclude = {
        // Remove these:
        // org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration.class,
        // org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
        // org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class,
        
        // Keep this:
        org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.class
    },
    scanBasePackages = {"com.example.task_service"}
)
```

Add ES config to `application.yml`:

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

The service already has TaskDocument and TaskSearchRepository. Just enable them!

### BOM Service (2 hours)

BOM service needs ES implementation from scratch.

**File**: `bom-service/pom.xml`

Add dependency:

```xml
<!-- Elasticsearch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

**File**: `bom-service/src/main/java/com/example/plm/bom/elasticsearch/BomSearchDocument.java`

```java
package com.example.plm.bom.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(indexName = "boms")
public class BomSearchDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text)
    private String name;
    
    @Field(type = FieldType.Text)
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String bomType;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
}
```

**File**: `bom-service/src/main/java/com/example/plm/bom/elasticsearch/BomSearchRepository.java`

```java
package com.example.plm.bom.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface BomSearchRepository extends ElasticsearchRepository<BomSearchDocument, String> {
    List<BomSearchDocument> findByNameContaining(String name);
}
```

Add indexing to BOM creation/update methods, similar to Document service.

---

## 📅 Day 5-6: Unified Search Service (6 hours)

This is the **key piece** that ties everything together for GlobalSearch.js!

### Create Unified Search Controller

**File**: `infra/search-service/src/main/java/com/example/plm/search/controller/GlobalSearchController.java`

```java
package com.example.plm.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchController {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * Global search across all indices
     * This is what GlobalSearch.js will call!
     */
    @GetMapping("/global")
    public GlobalSearchResponse globalSearch(@RequestParam String q) {
        log.info("🔍 Global search for: {}", q);
        
        GlobalSearchResponse response = new GlobalSearchResponse();
        
        try {
            // Search documents
            response.setDocuments(searchIndex("documents", q));
            
            // Search changes
            response.setChanges(searchIndex("changes", q));
            
            // Search tasks
            response.setTasks(searchIndex("tasks", q));
            
            // Search BOMs
            response.setBoms(searchIndex("boms", q));
            
            log.info("✅ Found {} documents, {} changes, {} tasks, {} BOMs",
                    response.getDocuments().size(),
                    response.getChanges().size(),
                    response.getTasks().size(),
                    response.getBoms().size());
            
        } catch (Exception e) {
            log.error("❌ Search failed: {}", e.getMessage(), e);
        }
        
        return response;
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
    
    /**
     * Helper method to search a specific index
     */
    private List<Map<String, Object>> searchIndex(String indexName, String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Create a multi-field query
            String queryString = String.format(
                "{\n" +
                "  \"multi_match\": {\n" +
                "    \"query\": \"%s\",\n" +
                "    \"fields\": [\"title^3\", \"name^3\", \"taskName^3\", \"description^2\", \"*\"],\n" +
                "    \"fuzziness\": \"AUTO\",\n" +
                "    \"operator\": \"or\"\n" +
                "  }\n" +
                "}",
                query.replace("\"", "\\\"")
            );
            
            Query searchQuery = new StringQuery(queryString);
            
            SearchHits<Map> searchHits = elasticsearchOperations.search(
                searchQuery,
                Map.class,
                org.springframework.data.elasticsearch.core.IndexCoordinates.of(indexName)
            );
            
            for (SearchHit<Map> hit : searchHits) {
                Map<String, Object> source = hit.getContent();
                source.put("_score", hit.getScore());
                source.put("_type", indexName);
                results.add(source);
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Failed to search index {}: {}", indexName, e.getMessage());
        }
        
        return results;
    }
}
```

**File**: `infra/search-service/src/main/java/com/example/plm/search/controller/GlobalSearchResponse.java`

```java
package com.example.plm.search.controller;

import lombok.Data;
import java.util.*;

@Data
public class GlobalSearchResponse {
    private List<Map<String, Object>> documents = new ArrayList<>();
    private List<Map<String, Object>> changes = new ArrayList<>();
    private List<Map<String, Object>> tasks = new ArrayList<>();
    private List<Map<String, Object>> boms = new ArrayList<>();
    
    public int getTotalResults() {
        return documents.size() + changes.size() + tasks.size() + boms.size();
    }
}
```

### Configure Search Service

**File**: `infra/search-service/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: search-service
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 5s
    socket-timeout: 30s

server:
  port: 8091

logging:
  level:
    com.example.plm.search: DEBUG
```

### Start Search Service

```bash
cd infra/search-service
mvn clean spring-boot:run
```

**Test:**
```bash
curl "http://localhost:8091/api/search/global?q=motor"
```

---

## 📅 Day 7: Frontend Integration (4 hours)

Now connect your beautiful GlobalSearch.js to the new search backend!

### Update GlobalSearch.js

**File**: `frontend/src/components/GlobalSearch.js`

Replace the entire component with this updated version:

```javascript
import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  TextField,
  Typography,
  CircularProgress,
  Card,
  CardContent,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  InputAdornment,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Paper,
  Tabs,
  Tab,
  Badge,
  Alert
} from '@mui/material';
import {
  Search as SearchIcon,
  Close as CloseIcon,
  Description as DocumentIcon,
  AccountTree as BOMIcon,
  Assignment as TaskIcon,
  ChangeHistory as ChangeIcon,
} from '@mui/icons-material';
import axios from 'axios';

const SEARCH_SERVICE_URL = 'http://localhost:8091/api/search';

const getItemIcon = (type) => {
  switch (type) {
    case 'documents': return <DocumentIcon color="primary" />;
    case 'tasks': return <TaskIcon color="secondary" />;
    case 'boms': return <BOMIcon color="success" />;
    case 'changes': return <ChangeIcon color="warning" />;
    default: return <SearchIcon />;
  }
};

const getStatusColor = (status) => {
  switch (status?.toLowerCase()) {
    case 'active':
    case 'approved':
    case 'completed': return 'success';
    case 'draft':
    case 'todo':
    case 'in_progress':
    case 'in_work': return 'warning';
    case 'in_review': return 'info';
    case 'rejected':
    case 'obsolete': return 'error';
    default: return 'default';
  }
};

export default function GlobalSearch() {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [currentTab, setCurrentTab] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState(null);
  
  // Search results from Elasticsearch
  const [searchResults, setSearchResults] = useState({
    documents: [],
    changes: [],
    tasks: [],
    boms: []
  });

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Perform search when debounced term changes
  useEffect(() => {
    const performSearch = async () => {
      if (!debouncedSearchTerm || debouncedSearchTerm.length < 2) {
        setSearchResults({ documents: [], changes: [], tasks: [], boms: [] });
        return;
      }

      setIsSearching(true);
      setError(null);

      try {
        const response = await axios.get(`${SEARCH_SERVICE_URL}/global`, {
          params: { q: debouncedSearchTerm }
        });

        console.log('🔍 Search results:', response.data);
        setSearchResults(response.data);

      } catch (err) {
        console.error('❌ Search failed:', err);
        setError('Search failed. Please make sure Elasticsearch is running.');
      } finally {
        setIsSearching(false);
      }
    };

    performSearch();
  }, [debouncedSearchTerm]);

  // Combine all results for filtering
  const allResults = useMemo(() => {
    const combined = [];
    
    Object.entries(searchResults).forEach(([type, items]) => {
      items.forEach(item => {
        combined.push({ ...item, _type: type });
      });
    });
    
    return combined;
  }, [searchResults]);

  // Filter by category
  const filteredResults = useMemo(() => {
    if (selectedCategory === 'all') {
      return allResults;
    }
    return allResults.filter(item => item._type === selectedCategory);
  }, [allResults, selectedCategory]);

  // Group results by type
  const groupedResults = useMemo(() => {
    const groups = {
      documents: [],
      tasks: [],
      boms: [],
      changes: []
    };

    filteredResults.forEach(item => {
      const type = item._type;
      if (groups[type]) {
        groups[type].push(item);
      }
    });

    return groups;
  }, [filteredResults]);

  const totalResults = filteredResults.length;

  const handleClearSearch = () => {
    setSearchTerm('');
    setDebouncedSearchTerm('');
    setSearchResults({ documents: [], changes: [], tasks: [], boms: [] });
  };

  const handleClearFilters = () => {
    setSelectedCategory('all');
  };

  // Highlight matching text
  const highlightText = (text, query) => {
    if (!text || !query) return text;
    const regex = new RegExp(`(${query})`, 'gi');
    const parts = text.split(regex);
    
    return parts.map((part, i) => 
      regex.test(part) ? (
        <mark key={i} style={{ backgroundColor: '#ffeb3b', padding: '0 2px' }}>
          {part}
        </mark>
      ) : (
        part
      )
    );
  };

  const renderResultItem = (item) => {
    const title = item.title || item.name || item.taskName || 'Untitled';
    const description = item.description || item.taskDescription || item.changeReason || '';
    const creator = item.creator || item.assignedTo || 'Unknown';
    const status = item.status || item.taskStatus || 'N/A';
    const score = item._score ? `(${item._score.toFixed(2)})` : '';

    return (
      <Card 
        key={item.id} 
        sx={{ 
          mb: 2, 
          cursor: 'pointer',
          '&:hover': { 
            boxShadow: 3,
            backgroundColor: '#f5f5f5' 
          }
        }}
      >
        <CardContent>
          <Box display="flex" alignItems="center" mb={1}>
            {getItemIcon(item._type)}
            <Typography variant="h6" sx={{ ml: 1, flex: 1 }}>
              {highlightText(title, debouncedSearchTerm)} 
              <Typography component="span" variant="caption" color="text.secondary">
                {' '}{score}
              </Typography>
            </Typography>
            <Chip 
              label={status} 
              color={getStatusColor(status)} 
              size="small" 
            />
          </Box>
          
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {highlightText(description, debouncedSearchTerm)}
          </Typography>
          
          <Box display="flex" gap={1}>
            <Chip 
              label={`ID: ${item.id}`} 
              size="small" 
              variant="outlined" 
            />
            <Chip 
              label={`By: ${creator}`} 
              size="small" 
              variant="outlined" 
            />
            {item.createTime && (
              <Chip 
                label={new Date(item.createTime).toLocaleDateString()} 
                size="small" 
                variant="outlined" 
              />
            )}
          </Box>
        </CardContent>
      </Card>
    );
  };

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Typography variant="h4" gutterBottom>
        🔍 Global Search
      </Typography>
      
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Search across all documents, changes, tasks, and BOMs using Elasticsearch
      </Typography>

      {/* Search Bar */}
      <Paper elevation={3} sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Search for anything... (e.g., 'motor', 'specification', 'design')"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: searchTerm && (
              <InputAdornment position="end">
                <IconButton onClick={handleClearSearch} size="small">
                  <CloseIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        
        {/* Filters */}
        <Box display="flex" gap={2} mt={2} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Category</InputLabel>
            <Select
              value={selectedCategory}
              label="Category"
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <MenuItem value="all">All ({totalResults})</MenuItem>
              <MenuItem value="documents">
                Documents ({searchResults.documents.length})
              </MenuItem>
              <MenuItem value="changes">
                Changes ({searchResults.changes.length})
              </MenuItem>
              <MenuItem value="tasks">
                Tasks ({searchResults.tasks.length})
              </MenuItem>
              <MenuItem value="boms">
                BOMs ({searchResults.boms.length})
              </MenuItem>
            </Select>
          </FormControl>

          {selectedCategory !== 'all' && (
            <IconButton onClick={handleClearFilters} size="small">
              <CloseIcon />
            </IconButton>
          )}

          {isSearching && (
            <CircularProgress size={24} sx={{ ml: 2 }} />
          )}
        </Box>
      </Paper>

      {/* Error Message */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Results */}
      {debouncedSearchTerm && (
        <Box>
          <Typography variant="h6" gutterBottom>
            {isSearching ? (
              'Searching...'
            ) : (
              `Found ${totalResults} result${totalResults !== 1 ? 's' : ''} for "${debouncedSearchTerm}"`
            )}
          </Typography>

          {/* Tabs */}
          <Tabs value={currentTab} onChange={(e, newValue) => setCurrentTab(newValue)} sx={{ mb: 2 }}>
            <Tab 
              label={
                <Badge badgeContent={totalResults} color="primary">
                  All
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={groupedResults.documents.length} color="primary">
                  Documents
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={groupedResults.changes.length} color="warning">
                  Changes
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={groupedResults.tasks.length} color="secondary">
                  Tasks
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={groupedResults.boms.length} color="success">
                  BOMs
                </Badge>
              } 
            />
          </Tabs>

          {/* Results List */}
          <Box>
            {currentTab === 0 && filteredResults.map(item => renderResultItem(item))}
            {currentTab === 1 && groupedResults.documents.map(item => renderResultItem(item))}
            {currentTab === 2 && groupedResults.changes.map(item => renderResultItem(item))}
            {currentTab === 3 && groupedResults.tasks.map(item => renderResultItem(item))}
            {currentTab === 4 && groupedResults.boms.map(item => renderResultItem(item))}
          </Box>

          {totalResults === 0 && !isSearching && (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography variant="h6" color="text.secondary">
                No results found for "{debouncedSearchTerm}"
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Try different keywords or check spelling
              </Typography>
            </Paper>
          )}
        </Box>
      )}

      {/* Empty State */}
      {!debouncedSearchTerm && (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <SearchIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            Start typing to search
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Search across all documents, changes, tasks, and BOMs
          </Typography>
        </Paper>
      )}
    </Box>
  );
}
```

---

## 🎯 Day 8: Reindexing Existing Data

### Create Reindex Endpoints

Add to each service controller:

**Example for DocumentController:**

```java
@PostMapping("/admin/reindex")
public ResponseEntity<Map<String, Object>> reindexAllDocuments() {
    List<Document> allDocs = documentService.getAllDocuments();
    int success = 0;
    int failed = 0;
    
    for (Document doc : allDocs) {
        try {
            documentSearchService.indexDocument(doc);
            success++;
        } catch (Exception e) {
            log.error("Failed to reindex document {}: {}", doc.getId(), e.getMessage());
            failed++;
        }
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("total", allDocs.size());
    result.put("success", success);
    result.put("failed", failed);
    
    return ResponseEntity.ok(result);
}
```

### Create Master Reindex Script

**File**: `reindex-all-elasticsearch.bat`

```batch
@echo off
echo ========================================
echo   Reindexing All PLM Data to Elasticsearch
echo ========================================
echo.

echo Reindexing Documents...
curl -X POST http://localhost:8081/api/v1/documents/admin/reindex
echo.
echo.

echo Reindexing Changes...
curl -X POST http://localhost:8084/api/changes/admin/reindex
echo.
echo.

echo Reindexing Tasks...
curl -X POST http://localhost:8082/api/tasks/admin/reindex
echo.
echo.

echo Reindexing BOMs...
curl -X POST http://localhost:8089/api/boms/admin/reindex
echo.
echo.

echo ========================================
echo   Reindexing Complete!
echo ========================================
pause
```

---

## 🚀 Complete Startup Sequence

### Step 1: Start Infrastructure

```batch
# Terminal 1: Elasticsearch
start-elasticsearch.bat

# Wait 30 seconds for ES to be ready
```

### Step 2: Start Backend Services

```batch
# Run your existing startup script
start-all-services.bat
```

### Step 3: Start Search Service

```batch
# Terminal 2: Search Service
cd infra/search-service
mvn spring-boot:run
```

### Step 4: Reindex Existing Data

```batch
# Terminal 3: One-time reindex
reindex-all-elasticsearch.bat
```

### Step 5: Start Frontend

```batch
# Terminal 4: React Frontend
cd frontend
npm start
```

### Step 6: Test!

1. Open http://localhost:3000
2. Navigate to Global Search
3. Type "motor" or any keyword
4. See results from all services!

---

## ✅ Verification Checklist

### Backend Verification

- [ ] Elasticsearch running at http://localhost:9200
- [ ] Kibana accessible at http://localhost:5601
- [ ] Document service started (8081)
- [ ] Change service started (8084)
- [ ] Task service started (8082)
- [ ] BOM service started (8089)
- [ ] Search service started (8085)

### Elasticsearch Indices Check

```bash
curl http://localhost:9200/_cat/indices?v
```

Should show:
```
health status index     uuid                   pri rep docs.count
yellow open   documents xxx                    1   1         45
yellow open   changes   xxx                    1   1         12
yellow open   tasks     xxx                    1   1         23
yellow open   boms      xxx                    1   1          8
```

### API Endpoints Check

```bash
# Test each service search endpoint
curl "http://localhost:8081/api/v1/documents/search/elastic?q=test"
curl "http://localhost:8084/api/changes/search/elastic?query=test"
curl "http://localhost:8082/search?keyword=test"
curl "http://localhost:8089/api/boms/search/elastic?q=test"

# Test unified search
curl "http://localhost:8085/api/search/global?q=test"
```

### Frontend Check

- [ ] GlobalSearch.js loads without errors
- [ ] Search bar accepts input
- [ ] Results appear as you type (after 300ms debounce)
- [ ] Results show from multiple entity types
- [ ] Filters work correctly
- [ ] Tabs show correct counts
- [ ] Highlighting works

---

## 🐛 Troubleshooting

### Issue: "Connection refused" errors

**Solution:**
```bash
# Check Elasticsearch is running
curl http://localhost:9200

# If not, restart it
start-elasticsearch.bat
```

### Issue: No results in search

**Solution:**
```bash
# Check if indices have data
curl http://localhost:9200/_cat/indices?v

# If docs.count is 0, run reindex
reindex-all-elasticsearch.bat
```

### Issue: Frontend CORS errors

**Solution:** Add `@CrossOrigin(origins = "*")` to all search controllers

### Issue: Elasticsearch out of memory

**Solution:** Increase heap size in `docker-compose-elasticsearch.yml`:
```yaml
- "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase from 512m
```

---

## 📊 Performance Metrics

After implementation, you should see:

| Metric | Target | Measurement |
|--------|--------|-------------|
| Search response time | <50ms | Check browser network tab |
| Index size per 1000 docs | ~5-10MB | `curl http://localhost:9200/_cat/indices?v` |
| Successful indexing rate | >99% | Check service logs |
| Search accuracy | High relevance | User feedback |

---

## 🎉 Success Criteria

You'll know it's working when:

1. ✅ You type in GlobalSearch.js and see real results
2. ✅ Results come from all entity types (docs, changes, tasks, BOMs)
3. ✅ Search is fast (<100ms response)
4. ✅ Results are ranked by relevance
5. ✅ Highlighting shows matching terms
6. ✅ Creating new entities auto-indexes to ES
7. ✅ Search works with typos and partial matches

---

## 📚 Next Steps After Implementation

1. **Add autocomplete/suggestions**
2. **Implement advanced filters** (date ranges, multiple statuses)
3. **Add faceted search** (counts by category)
4. **Implement search history**
5. **Add export search results**
6. **Set up monitoring dashboards** in Kibana

---

## 🆘 Need Help?

**Quick Commands:**
```bash
# Check all service health
curl http://localhost:9200/_cluster/health  # Elasticsearch
curl http://localhost:8081/actuator/health  # Document
curl http://localhost:8085/api/search/global?q=test  # Search

# View logs
docker logs plm-elasticsearch

# Restart a service
cd document-service && mvn spring-boot:run
```

---

**Version:** 1.0  
**Last Updated:** 2025-10-29  
**Estimated Total Time:** 7-10 days  
**Difficulty:** Intermediate


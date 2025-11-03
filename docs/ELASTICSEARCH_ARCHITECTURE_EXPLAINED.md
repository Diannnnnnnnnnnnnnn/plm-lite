# Elasticsearch Architecture - Distributed Indexing vs. Centralized Search

## 🏗️ Overview

This document explains the **two-tier Elasticsearch architecture** used in the PLM system:
1. **Individual Services** (Document, Change, Task, BOM) - Data Owners & Indexers
2. **Search Service** (Port 8091) - Unified Search & Aggregator

---

## 🎯 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend (React - Port 3000)                  │
│                                                                  │
│  GlobalSearch.js → http://localhost:8091/api/search/global      │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ Single API Call
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│             Search Service (Port 8091) - READ ONLY              │
│                                                                  │
│  • Queries ALL Elasticsearch indices                            │
│  • Aggregates results from multiple entities                    │
│  • Returns unified response                                     │
│  • Cross-entity search                                          │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ Read/Query
                               ▼
         ┌─────────────────────────────────────────┐
         │      Elasticsearch (Port 9200)          │
         │              Docker Container            │
         │                                          │
         │  📂 documents   (indexed by doc service) │
         │  📂 changes     (indexed by change svc)  │
         │  📂 tasks       (indexed by task svc)    │
         │  📂 boms        (indexed by bom svc)     │
         └──────────────────┬──────────────────────┘
                            │
                            │ Write/Index
                            ▼
┌────────────────────────────────────────────────────────────────┐
│         Individual Services - WRITE/INDEX                       │
│                                                                 │
│  Document Service (8081) → Indexes documents                   │
│  Change Service (8084)   → Indexes changes                     │
│  Task Service (8082)     → Indexes tasks                       │
│  BOM Service (8089)      → Indexes BOMs                        │
│                                                                 │
│  Each service:                                                  │
│  • Creates/Updates/Deletes in MySQL                            │
│  • Auto-indexes to Elasticsearch                               │
│  • Maintains its own ES index                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📝 Two Distinct Roles

### Role 1: Individual Services (Data Owners & Indexers)

**Services:** Document, Change, Task, BOM

**Purpose:** **WRITE data to Elasticsearch** (Indexing)

**Responsibilities:**
- ✅ Index entities when created
- ✅ Update index when entities modified
- ✅ Delete from index when entities deleted
- ✅ Maintain data consistency between MySQL and ES
- ✅ Own their ES index structure
- ✅ Optionally provide service-specific search

**Location Example:**
```
document-service/
├── src/main/java/.../elasticsearch/
│   ├── DocumentSearchDocument.java       (ES model)
│   ├── DocumentSearchRepository.java     (ES repository)
│   └── DocumentSearchService.java        (Indexing logic)
```

**Code Example:**
```java
// document-service/service/DocumentServiceImpl.java
@Override
public Document create(CreateDocumentRequest req) {
    // 1. Save to MySQL
    Document doc = docRepo.save(document);
    
    // 2. Index to Elasticsearch (automatic)
    documentSearchService.indexDocument(doc);
    
    return doc;
}
```

---

### Role 2: Search Service (Unified Search & Aggregator)

**Service:** Search Service (Port 8091)

**Purpose:** **READ from all ES indices** (Searching)

**Responsibilities:**
- ✅ Query multiple ES indices simultaneously
- ✅ Aggregate results from all entities
- ✅ Provide single unified search API
- ✅ Rank results across entities
- ✅ Enable cross-entity search
- ✅ Future: Analytics, reporting, advanced queries

**Location:**
```
infra/search-service/
├── src/main/java/.../search/
│   └── controller/
│       ├── GlobalSearchController.java    (Unified search API)
│       └── GlobalSearchResponse.java      (Response DTO)
```

**Code Example:**
```java
// infra/search-service/controller/GlobalSearchController.java
@GetMapping("/api/search/global")
public GlobalSearchResponse globalSearch(@RequestParam String q) {
    
    // Query ALL indices
    List<Map> documents = searchIndex("documents", q);
    List<Map> changes = searchIndex("changes", q);
    List<Map> tasks = searchIndex("tasks", q);
    List<Map> boms = searchIndex("boms", q);
    
    // Return aggregated results
    return new GlobalSearchResponse(documents, changes, tasks, boms);
}
```

---

## 🔄 Complete Data Flow

### Scenario: User creates a document, then searches for it

#### Step 1: CREATE DOCUMENT (Write Flow)

```
1. User creates document in frontend
   ↓
2. Frontend → POST http://localhost:8081/api/v1/documents
   ↓
3. Document Service:
   a. Validates request
   b. Saves to MySQL database
   c. Auto-indexes to Elasticsearch ✍️
   ↓
4. Elasticsearch: 'documents' index updated
   ↓
5. Response returned to user
```

**Code:**
```java
// Document Service
@PostMapping("/api/v1/documents")
public DocumentResponse create(@RequestBody CreateDocumentRequest req) {
    Document doc = documentService.create(req);  // Saves to MySQL
    
    // This happens inside create():
    // documentSearchService.indexDocument(doc);  // ✍️ Writes to ES
    
    return DocumentMapper.toResponse(doc);
}
```

#### Step 2: SEARCH FOR DOCUMENT (Read Flow)

```
1. User types "motor" in GlobalSearch
   ↓
2. Frontend → GET http://localhost:8091/api/search/global?q=motor
   ↓
3. Search Service:
   a. Queries 'documents' index 🔍
   b. Queries 'changes' index 🔍
   c. Queries 'tasks' index 🔍
   d. Queries 'boms' index 🔍
   e. Aggregates all results
   ↓
4. Returns unified response
   ↓
5. Frontend displays results grouped by entity type
```

**Code:**
```javascript
// Frontend GlobalSearch.js
const performSearch = async () => {
    const response = await axios.get(
        `http://localhost:8091/api/search/global?q=${query}`
    );
    
    // Single response with all entities
    setSearchResults(response.data);
    // {
    //   documents: [...],
    //   changes: [...],
    //   tasks: [...],
    //   boms: [...]
    // }
};
```

---

## 🎯 Why This Pattern?

### ❌ Alternative 1: Each Service Searches Itself

**Problem:**
```javascript
// Frontend has to call 4 separate APIs
const docs = await fetch('http://localhost:8081/documents/search?q=motor');
const changes = await fetch('http://localhost:8084/changes/search?q=motor');
const tasks = await fetch('http://localhost:8082/tasks/search?q=motor');
const boms = await fetch('http://localhost:8089/boms/search?q=motor');

// Manually merge and rank results
const allResults = [...docs, ...changes, ...tasks, ...boms];
```

**Downsides:**
- ❌ 4 API calls (slow, 4x network overhead)
- ❌ Complex frontend logic
- ❌ Hard to rank across entity types
- ❌ What if one service is down?
- ❌ Can't do cross-entity queries

---

### ❌ Alternative 2: Search Service Does Everything

**Problem:**
```
Search Service:
  • Indexes documents
  • Indexes changes
  • Indexes tasks
  • Indexes BOMs
  • Searches everything
```

**Downsides:**
- ❌ Tight coupling (all services depend on search service)
- ❌ Search service becomes bottleneck
- ❌ Search service needs to know all business logic
- ❌ If search service is down, nothing gets indexed
- ❌ Violates microservices autonomy principle

---

### ✅ Our Pattern: Distributed Index + Centralized Search

**Benefits:**

1. **Separation of Concerns**
   - Each service owns and indexes its own data
   - Search service only searches, doesn't manage data

2. **Loose Coupling**
   - Services don't depend on search service for indexing
   - Search service depends on ES, not on other services
   - Services can function independently

3. **Resilience**
   - Document service down? Changes still searchable
   - Search service down? Documents still get indexed
   - Each service continues to work independently

4. **Scalability**
   - Add new entity? Just index it, search service automatically finds it
   - Can scale search service independently
   - Can scale individual services independently

5. **Simple Frontend**
   - Single API endpoint: `/api/search/global`
   - One unified data format
   - Easy to implement, easy to maintain

6. **Microservices Best Practice**
   - Each service is autonomous
   - Services own their data lifecycle
   - Follows bounded context pattern

---

## 📊 Comparison Table

| Aspect | Individual Service ES | Search Service (8091) |
|--------|---------------------|---------------------|
| **Purpose** | Data owner & indexer | Search aggregator |
| **Operation** | ✍️ WRITE (Create/Update/Delete) | 🔍 READ (Search/Query) |
| **Scope** | Single entity type | ALL entity types |
| **Data Direction** | App → Elasticsearch | Elasticsearch → App |
| **Called By** | Internal (automatic) | Frontend (user-initiated) |
| **Example API** | `/api/v1/documents/search/elastic` | `/api/search/global` |
| **Dependency** | Owns document data | Reads from all indices |
| **Business Logic** | Knows document rules | No business logic |
| **Failure Impact** | That entity not searchable | Search unavailable |
| **Can Work Alone** | Yes | Yes (if indices exist) |
| **Responsibilities** | Maintain index consistency | Query and aggregate |

---

## 🏢 Real-World Analogy

### Library System Analogy

**Individual Services = Department Librarians**

- **Fiction Librarian** (Document Service)
  - When you donate a fiction book, they **catalog it** in the fiction database
  - They maintain the fiction catalog
  - They know fiction book rules
  - They can help you search fiction specifically (optional)

- **History Librarian** (Change Service)
  - Catalogs history books
  - Maintains history catalog
  - Knows history book rules

- **Science Librarian** (Task Service)
  - Catalogs science books
  - Maintains science catalog
  - Knows science book rules

**Search Service = Information Desk**

- **Information Desk** (Search Service)
  - Doesn't catalog books themselves
  - **Searches ALL catalogs** when you ask
  - "Looking for anything about 'physics'?"
    - Checks fiction catalog
    - Checks history catalog
    - Checks science catalog
  - Gives you **combined results** from all departments
  - One place to ask for anything!

**The Key Insight:**
- Librarians own and catalog their books (write)
- Information desk searches across all catalogs (read)
- Visitors go to one place for everything

---

## 💻 Code Examples

### Example 1: Document Service - Indexing (Write)

**File:** `document-service/src/main/java/.../elasticsearch/DocumentSearchService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchService {
    
    private final DocumentSearchRepository searchRepository;
    
    /**
     * Index a document to Elasticsearch
     * Called automatically when document is created/updated
     */
    public void indexDocument(Document document) {
        try {
            DocumentSearchDocument searchDoc = DocumentSearchDocument.fromDocument(document);
            searchRepository.save(searchDoc);  // ✍️ WRITE to Elasticsearch
            log.info("✅ Document {} indexed to Elasticsearch", document.getId());
        } catch (Exception e) {
            log.error("❌ Failed to index document {}: {}", document.getId(), e.getMessage());
        }
    }
    
    /**
     * Remove document from Elasticsearch
     * Called automatically when document is deleted
     */
    public void deleteDocument(String documentId) {
        try {
            searchRepository.deleteById(documentId);
            log.info("✅ Document {} removed from Elasticsearch", documentId);
        } catch (Exception e) {
            log.error("❌ Failed to delete document {}: {}", documentId, e.getMessage());
        }
    }
}
```

**Triggered by:**

```java
// document-service/service/impl/DocumentServiceImpl.java
@Service
public class DocumentServiceImpl implements DocumentService {
    
    @Autowired
    private DocumentSearchService documentSearchService;
    
    @Override
    public Document create(CreateDocumentRequest req) {
        // 1. Save to MySQL
        Document doc = docRepo.save(document);
        
        // 2. Auto-index to Elasticsearch
        sync(doc);  // Contains: documentSearchService.indexDocument(doc)
        
        return doc;
    }
    
    @Override
    public Document updateDocument(String id, UpdateDocumentRequest req) {
        Document doc = docRepo.findById(id).orElseThrow();
        // ... update fields ...
        doc = docRepo.save(doc);
        
        // Re-index to Elasticsearch
        sync(doc);
        
        return doc;
    }
    
    @Override
    public void deleteDocument(String documentId) {
        Document doc = docRepo.findById(documentId).orElseThrow();
        
        // Delete from MySQL
        docRepo.delete(doc);
        
        // Delete from Elasticsearch
        documentSearchService.deleteDocument(documentId);
    }
    
    private void sync(Document d) {
        // Sync to Neo4j
        syncDocumentToGraph(d);
        
        // Sync to Elasticsearch
        try {
            documentSearchService.indexDocument(d);
        } catch (Exception e) {
            log.warn("Failed to sync to Elasticsearch: {}", e.getMessage());
        }
    }
}
```

---

### Example 2: Search Service - Searching (Read)

**File:** `infra/search-service/src/main/java/.../controller/GlobalSearchController.java`

```java
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchController {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * Global search across all indices
     * This is what the frontend GlobalSearch.js calls
     */
    @GetMapping("/global")
    public GlobalSearchResponse globalSearch(@RequestParam String q) {
        log.info("🔍 Global search for: {}", q);
        
        GlobalSearchResponse response = new GlobalSearchResponse();
        
        try {
            // 🔍 READ from all indices
            response.setDocuments(searchIndex("documents", q));
            response.setChanges(searchIndex("changes", q));
            response.setTasks(searchIndex("tasks", q));
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
            // Create multi-field query with fuzzy matching
            String queryString = String.format(
                "{\n" +
                "  \"multi_match\": {\n" +
                "    \"query\": \"%s\",\n" +
                "    \"fields\": [\"title^3\", \"name^3\", \"description^2\", \"*\"],\n" +
                "    \"fuzziness\": \"AUTO\",\n" +
                "    \"operator\": \"or\"\n" +
                "  }\n" +
                "}",
                query.replace("\"", "\\\"")
            );
            
            Query searchQuery = new StringQuery(queryString);
            
            // Execute search
            SearchHits<Map> searchHits = elasticsearchOperations.search(
                searchQuery,
                Map.class,
                IndexCoordinates.of(indexName)
            );
            
            // Convert results
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

**Response DTO:**

```java
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

---

### Example 3: Frontend - Using Search Service

**File:** `frontend/src/components/GlobalSearch.js`

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const SEARCH_SERVICE_URL = 'http://localhost:8091/api/search';

export default function GlobalSearch() {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState({
    documents: [],
    changes: [],
    tasks: [],
    boms: []
  });
  const [isSearching, setIsSearching] = useState(false);

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm.length >= 2) {
        performSearch();
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const performSearch = async () => {
    setIsSearching(true);
    
    try {
      // Single API call to search service
      const response = await axios.get(`${SEARCH_SERVICE_URL}/global`, {
        params: { q: searchTerm }
      });
      
      console.log('🔍 Search results:', response.data);
      
      // Set results from all entity types
      setSearchResults(response.data);
      
    } catch (error) {
      console.error('❌ Search failed:', error);
    } finally {
      setIsSearching(false);
    }
  };

  return (
    <div>
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Search across all PLM entities..."
      />
      
      {isSearching && <p>Searching...</p>}
      
      <div>
        <h3>Documents ({searchResults.documents.length})</h3>
        {searchResults.documents.map(doc => (
          <div key={doc.id}>{doc.title}</div>
        ))}
        
        <h3>Changes ({searchResults.changes.length})</h3>
        {searchResults.changes.map(change => (
          <div key={change.id}>{change.title}</div>
        ))}
        
        <h3>Tasks ({searchResults.tasks.length})</h3>
        {searchResults.tasks.map(task => (
          <div key={task.id}>{task.taskName}</div>
        ))}
        
        <h3>BOMs ({searchResults.boms.length})</h3>
        {searchResults.boms.map(bom => (
          <div key={bom.id}>{bom.name}</div>
        ))}
      </div>
    </div>
  );
}
```

---

## 🔧 Optional: Service-Specific Search

While the **global search** is the primary interface, you can optionally expose **service-specific search endpoints** for advanced use cases.

### Use Cases for Service-Specific Search:
- Admin tools that only need documents
- Service-to-service communication
- Debugging and testing
- Advanced filters specific to one entity type

### Example Implementation:

```java
// document-service/controller/DocumentSearchController.java
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentSearchController {
    
    private final DocumentSearchService searchService;
    
    @GetMapping("/search/elastic")
    public List<DocumentSearchDocument> searchDocuments(@RequestParam String q) {
        return searchService.search(q);
    }
    
    @GetMapping("/search/elastic/advanced")
    public List<DocumentSearchDocument> advancedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String category) {
        return searchService.advancedSearch(q, status, stage, category);
    }
}
```

**When to use:**
```javascript
// For general user search - use global search
const results = await axios.get('http://localhost:8091/api/search/global?q=motor');

// For document-specific admin tool - use service-specific search
const docs = await axios.get('http://localhost:8081/api/v1/documents/search/elastic?q=motor');
```

---

## 📋 Implementation Checklist

### Phase 1: Individual Services (Week 1)

**Document Service:**
- [ ] Create ES models (`DocumentSearchDocument`)
- [ ] Create ES repository (`DocumentSearchRepository`)
- [ ] Create search service (`DocumentSearchService`)
- [ ] Add indexing to create/update/delete methods
- [ ] Test indexing works
- [ ] Verify in Kibana

**Change Service:**
- [ ] Remove ES from exclude list
- [ ] Update configuration
- [ ] Test existing ES code
- [ ] Verify indexing works

**Task Service:**
- [ ] Remove ES from exclude list
- [ ] Update configuration
- [ ] Test existing ES code
- [ ] Verify indexing works

**BOM Service:**
- [ ] Add ES dependency
- [ ] Create ES models
- [ ] Create search service
- [ ] Add indexing logic
- [ ] Test and verify

### Phase 2: Search Service (Week 2)

- [ ] Implement GlobalSearchController
- [ ] Create GlobalSearchResponse DTO
- [ ] Implement searchIndex() helper method
- [ ] Add health checks
- [ ] Test with curl
- [ ] Verify all indices are queried

### Phase 3: Frontend Integration

- [ ] Update GlobalSearch.js
- [ ] Change API URL to port 8091
- [ ] Test search functionality
- [ ] Add error handling
- [ ] Polish UI

### Phase 4: Data Migration

- [ ] Create reindex endpoints in each service
- [ ] Run reindex-all-elasticsearch.bat
- [ ] Verify all data is indexed
- [ ] Test search with real data

---

## 🐛 Common Questions

### Q: Why can't Search Service index data directly?

**A:** Because each service **owns its business logic and data lifecycle**.

- Document Service knows when a document is created, updated, or deleted
- Document Service knows document validation rules
- Document Service handles transactions (MySQL + ES must be consistent)
- Search Service shouldn't know about MySQL or business rules

This is **microservices autonomy** - each service is self-contained.

---

### Q: What if Search Service is down?

**A:** Services continue to work normally!

- Documents still get created in MySQL ✅
- Documents still get indexed to Elasticsearch ✅
- Users can't search temporarily ❌
- When search service comes back up, all data is already indexed ✅

---

### Q: What if Document Service is down?

**A:** Other entities are still searchable!

- Changes, tasks, and BOMs still searchable ✅
- Can't create new documents temporarily ❌
- Existing documents remain searchable ✅

---

### Q: Do I need service-specific search endpoints?

**A:** Usually no, but they're optional for:

- Admin tools
- Service-to-service calls
- Advanced entity-specific filters
- Debugging

For end users, use the global search!

---

### Q: What happens if ES and MySQL get out of sync?

**A:** Run the reindex script:

```batch
reindex-all-elasticsearch.bat
```

This re-indexes all data from MySQL to Elasticsearch.

---

## 🎯 Key Takeaways

1. **Individual Services = Writers**
   - Index their own data to Elasticsearch
   - Maintain consistency with MySQL
   - Can optionally provide entity-specific search

2. **Search Service = Reader**
   - Queries all Elasticsearch indices
   - Aggregates results
   - Provides unified API for frontend

3. **Frontend = Consumer**
   - Calls one simple API
   - Gets results from all entities
   - Easy to implement and maintain

4. **Pattern Benefits**
   - Separation of concerns
   - Loose coupling
   - High resilience
   - Easy to scale
   - Microservices best practice

5. **Remember**
   - Services **write** to their index
   - Search service **reads** from all indices
   - Frontend calls search service
   - Each part is autonomous

---

## 📖 Related Documentation

- **Implementation Guide:** `docs/ELASTICSEARCH_GLOBAL_SEARCH_INTEGRATION.md`
- **Quick Reference:** `ELASTICSEARCH_QUICK_REFERENCE.md`
- **Docker Setup:** `ELASTICSEARCH_DOCKER_SETUP.md`
- **Port Configuration:** `docs/PORT_CONFIGURATION.md`

---

## 🎓 Further Reading

**Microservices Patterns:**
- Database per Service
- API Gateway
- Saga Pattern (for distributed transactions)
- CQRS (Command Query Responsibility Segregation)

**Elasticsearch Best Practices:**
- Index per entity type
- Denormalization for search performance
- Eventual consistency
- Reindexing strategies

---

**Version:** 1.0  
**Last Updated:** 2025-10-29  
**Author:** PLM System Architect

**Quick Reference:**
- Document Service: Indexes documents (write)
- Search Service (8091): Searches everything (read)
- Frontend: Calls search service (simple)




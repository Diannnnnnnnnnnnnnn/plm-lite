# Neo4j Integration Plan for PLM-Lite

## 🎯 Executive Summary

This document outlines the comprehensive plan to integrate Neo4j graph database into the PLM-Lite microservices ecosystem. Neo4j will complement the existing MySQL databases by providing powerful relationship traversal, impact analysis, and complex query capabilities essential for Product Lifecycle Management.

---

## 📊 Current State Assessment

### ✅ What You Have
- **Neo4j Running**: Docker instance on ports 7474 (HTTP) and 7687 (Bolt)
- **Graph Service**: Partially implemented with:
  - Spring Data Neo4j dependencies configured
  - Basic models: `TaskNode`, `UserNode`
  - Simple repository and service layer
  - REST API endpoints for task-user relationships
  - Configured to connect to `bolt://localhost:7687`

### ❌ What's Missing
- Neo4j not in docker-compose infrastructure
- Limited node types (only Task and User)
- No integration with BOM, Document, or Change services
- No data synchronization mechanism
- No advanced graph queries (impact analysis, dependency tracking)

---

## 🏗️ Architecture Overview

### Dual-Database Strategy (MySQL + Neo4j)

```
┌─────────────────────────────────────────────────────────────┐
│                    PLM-Lite Architecture                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ BOM Service  │  │ Doc Service  │  │Change Service│      │
│  │   (MySQL)    │  │   (MySQL)    │  │   (MySQL)    │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
│                    ┌───────▼────────┐                        │
│                    │ Graph Service  │                        │
│                    │    (Neo4j)     │                        │
│                    └────────────────┘                        │
│                                                               │
│  Event-Driven Synchronization (RabbitMQ)                     │
└─────────────────────────────────────────────────────────────┘
```

### Data Storage Principle

**MySQL**: Master data storage
- Structured transactional data
- ACID compliance for business operations
- Primary CRUD operations

**Neo4j**: Relationship-focused queries
- Complex relationship traversal
- Impact analysis and dependency tracking
- Path finding and graph algorithms
- Read-heavy analytical queries

---

## 📦 Phase 1: Infrastructure Setup

### 1.1 Add Neo4j to Docker Compose

**File**: `infra/docker-compose-infrastructure.yaml`

Add Neo4j service:

```yaml
  # ==========================================
  # Neo4j Graph Database
  # ==========================================
  neo4j:
    image: neo4j:5.15-community
    container_name: plm-neo4j
    ports:
      - "7474:7474"   # HTTP Browser UI
      - "7687:7687"   # Bolt protocol
    environment:
      - NEO4J_AUTH=neo4j/password
      - NEO4J_PLUGINS=["apoc"]
      - NEO4J_dbms_security_procedures_unrestricted=apoc.*
      - NEO4J_dbms_memory_heap_initial__size=512m
      - NEO4J_dbms_memory_heap_max__size=2G
      - NEO4J_dbms_memory_pagecache_size=512m
    volumes:
      - neo4j-data:/data
      - neo4j-logs:/logs
      - neo4j-import:/var/lib/neo4j/import
      - neo4j-plugins:/plugins
    networks:
      - plm-network
    healthcheck:
      test: ["CMD-SHELL", "cypher-shell -u neo4j -p password 'RETURN 1'"]
      interval: 10s
      timeout: 10s
      retries: 5
      start_period: 30s
    restart: unless-stopped
```

Add volumes:
```yaml
volumes:
  neo4j-data:
    driver: local
  neo4j-logs:
    driver: local
  neo4j-import:
    driver: local
  neo4j-plugins:
    driver: local
```

### 1.2 Update Graph Service Configuration

**File**: `infra/graph-service/src/main/resources/application.properties`

```properties
spring.application.name=graph-service
server.port=8090

# Neo4j Configuration
spring.neo4j.uri=bolt://neo4j:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password

# Connection pool settings
spring.neo4j.pool.max-connection-pool-size=50
spring.neo4j.pool.idle-time-before-connection-test=PT30S
spring.neo4j.pool.max-connection-lifetime=PT1H
spring.neo4j.pool.connection-acquisition-timeout=PT30S

# Eureka Client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Logging
logging.level.org.neo4j=INFO
logging.level.com.example.graph_service=DEBUG
```

---

## 🎨 Phase 2: Enhanced Graph Model

### 2.1 Node Types to Implement

```
┌─────────────────────────────────────────────────────────┐
│                    Neo4j Graph Model                     │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ╔══════════╗       ╔══════════╗      ╔══════════╗      │
│  ║   User   ║       ║   Task   ║      ║ Document ║      │
│  ╚═════╤════╝       ╚═════╤════╝      ╚═════╤════╝      │
│        │                  │                  │            │
│        │ CREATED          │ ASSIGNED_TO      │ OWNS       │
│        ▼                  ▼                  ▼            │
│  ╔══════════╗       ╔══════════╗      ╔══════════╗      │
│  ║   Part   ║───────║PartUsage ║      ║  Change  ║      │
│  ╚═════╤════╝ CHILD ╚══════════╝      ╚═════╤════╝      │
│        │                                     │            │
│        │ LINKED_TO                           │ AFFECTS    │
│        └─────────────────────────────────────┘            │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### 2.2 Node Models

#### PartNode
```java
@Node("Part")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartNode {
    @Id
    private String id;
    
    private String title;
    private String stage;
    private String level;
    private String creator;
    private LocalDateTime createTime;
    
    // Hierarchical relationships
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private List<PartUsageRelationship> children = new ArrayList<>();
    
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.INCOMING)
    private List<PartUsageRelationship> parents = new ArrayList<>();
    
    // Document links
    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.OUTGOING)
    private List<DocumentNode> linkedDocuments = new ArrayList<>();
    
    // Created by user
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode createdBy;
    
    // Affected by changes
    @Relationship(type = "AFFECTED_BY", direction = Relationship.Direction.INCOMING)
    private List<ChangeNode> affectingChanges = new ArrayList<>();
}
```

#### PartUsageRelationship
```java
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartUsageRelationship {
    @Id
    @GeneratedValue
    private Long id;
    
    private Integer quantity;
    private LocalDateTime createdAt;
    
    @TargetNode
    private PartNode childPart;
}
```

#### DocumentNode
```java
@Node("Document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNode {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String version;
    private String status;
    private LocalDateTime createTime;
    
    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.INCOMING)
    private List<PartNode> linkedParts = new ArrayList<>();
    
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode creator;
    
    @Relationship(type = "UPLOADED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode uploader;
    
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private List<ChangeNode> relatedChanges = new ArrayList<>();
}
```

#### ChangeNode
```java
@Node("Change")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNode {
    @Id
    private String id;
    
    private String title;
    private String description;
    private String status;
    private LocalDateTime createTime;
    
    @Relationship(type = "AFFECTS", direction = Relationship.Direction.OUTGOING)
    private List<PartNode> affectedParts = new ArrayList<>();
    
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.INCOMING)
    private List<DocumentNode> relatedDocuments = new ArrayList<>();
    
    @Relationship(type = "INITIATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode initiator;
    
    @Relationship(type = "REVIEWED_BY", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> reviewers = new ArrayList<>();
}
```

#### Enhanced UserNode
```java
@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNode {
    @Id
    private String id;
    
    private String username;
    private String email;
    private String department;
    private String role;
    
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private List<PartNode> createdParts = new ArrayList<>();
    
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private List<DocumentNode> createdDocuments = new ArrayList<>();
    
    @Relationship(type = "INITIATED_BY", direction = Relationship.Direction.INCOMING)
    private List<ChangeNode> initiatedChanges = new ArrayList<>();
    
    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    private List<TaskNode> assignedTasks = new ArrayList<>();
    
    @Relationship(type = "REPORTS_TO", direction = Relationship.Direction.OUTGOING)
    private UserNode manager;
}
```

#### Enhanced TaskNode
```java
@Node("Task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskNode {
    @Id
    private String id;
    
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createTime;
    
    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.INCOMING)
    private UserNode assignee;
    
    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode creator;
    
    @Relationship(type = "RELATED_TO_CHANGE", direction = Relationship.Direction.OUTGOING)
    private ChangeNode relatedChange;
    
    @Relationship(type = "RELATED_TO_PART", direction = Relationship.Direction.OUTGOING)
    private PartNode relatedPart;
}
```

---

## 🔄 Phase 3: Data Synchronization Strategy

### 3.1 Event-Driven Synchronization

**When to Sync MySQL → Neo4j:**

1. **Part Created** → Create PartNode
2. **Part Usage Added** → Create HAS_CHILD relationship
3. **Document Uploaded** → Create DocumentNode
4. **Part-Document Link** → Create LINKED_TO relationship
5. **Change Task Created** → Create ChangeNode
6. **Task Assigned** → Create ASSIGNED_TO relationship

### 3.2 Message Queue Events

**Create new event DTOs:**

```java
// GraphSyncEvent.java
public class GraphSyncEvent {
    private String entityType; // PART, DOCUMENT, CHANGE, USER, TASK
    private String operation;  // CREATE, UPDATE, DELETE, LINK
    private String entityId;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
}
```

### 3.3 Graph Service Event Listener

```java
@Service
@RequiredArgsConstructor
public class GraphSyncListener {
    
    private final GraphSyncService graphSyncService;
    
    @RabbitListener(queues = "graph-sync-queue")
    public void handleGraphSync(GraphSyncEvent event) {
        switch (event.getEntityType()) {
            case "PART":
                graphSyncService.syncPart(event);
                break;
            case "DOCUMENT":
                graphSyncService.syncDocument(event);
                break;
            case "CHANGE":
                graphSyncService.syncChange(event);
                break;
            // ... more cases
        }
    }
}
```

### 3.4 Publisher in Existing Services

**Example in BomServiceImpl:**

```java
@Service
@RequiredArgsConstructor
public class BomServiceImpl implements BomService {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public Part createPart(CreatePartRequest request) {
        // Save to MySQL
        Part part = partRepository.save(newPart);
        
        // Publish to graph sync queue
        GraphSyncEvent event = new GraphSyncEvent(
            "PART", 
            "CREATE", 
            part.getBigintid(),
            Map.of(
                "title", part.getTitlechar(),
                "stage", part.getStage(),
                "level", part.getLevel(),
                "creator", part.getCreator()
            ),
            LocalDateTime.now()
        );
        
        rabbitTemplate.convertAndSend("graph-sync-exchange", "graph.sync", event);
        
        return part;
    }
}
```

---

## 🎯 Phase 4: Advanced Graph Queries

### 4.1 Impact Analysis Queries

**Find all parts affected by a change:**

```java
@Repository
public interface PartNodeRepository extends Neo4jRepository<PartNode, String> {
    
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        RETURN p
        """)
    List<PartNode> findPartsAffectedByChange(@Param("changeId") String changeId);
    
    @Query("""
        MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
        MATCH (p)-[:HAS_CHILD*]->(child:Part)
        RETURN DISTINCT child
        """)
    List<PartNode> findAllChildPartsAffectedByChange(@Param("changeId") String changeId);
}
```

**Find BOM explosion (complete hierarchy):**

```java
@Query("""
    MATCH path = (p:Part {id: $partId})-[:HAS_CHILD*]->(child:Part)
    RETURN path
    """)
List<Path> findBomExplosion(@Param("partId") String partId);
```

**Find where-used (all parents):**

```java
@Query("""
    MATCH path = (p:Part {id: $partId})<-[:HAS_CHILD*]-(parent:Part)
    RETURN path
    """)
List<Path> findWhereUsed(@Param("partId") String partId);
```

### 4.2 Document Impact Analysis

```java
@Query("""
    MATCH (d:Document {id: $documentId})<-[:LINKED_TO]-(p:Part)
    MATCH (p)-[:HAS_CHILD*0..]->(related:Part)
    RETURN DISTINCT related
    """)
List<PartNode> findAllPartsImpactedByDocument(@Param("documentId") String documentId);
```

### 4.3 Change Propagation Analysis

```java
@Query("""
    MATCH (c:Change {id: $changeId})-[:AFFECTS]->(p:Part)
    MATCH (p)<-[:LINKED_TO]-(d:Document)
    RETURN d
    """)
List<DocumentNode> findDocumentsAffectedByChange(@Param("changeId") String changeId);
```

### 4.4 User Activity Analysis

```java
@Query("""
    MATCH (u:User {id: $userId})-[:CREATED_BY]->(p:Part)
    MATCH (p)<-[:HAS_CHILD]-(parent:Part)
    RETURN COUNT(DISTINCT parent) as partsUsedInAssemblies
    """)
Integer countPartsUsedInAssemblies(@Param("userId") String userId);
```

---

## 📋 Phase 5: REST API Endpoints

### 5.1 Enhanced Graph Controller

```java
@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {
    
    private final GraphQueryService queryService;
    
    // BOM queries
    @GetMapping("/parts/{id}/explosion")
    public ResponseEntity<BomExplosionResponse> getBomExplosion(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getBomExplosion(id));
    }
    
    @GetMapping("/parts/{id}/where-used")
    public ResponseEntity<WhereUsedResponse> getWhereUsed(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getWhereUsed(id));
    }
    
    // Impact analysis
    @GetMapping("/changes/{id}/impact")
    public ResponseEntity<ChangeImpactResponse> getChangeImpact(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getChangeImpact(id));
    }
    
    @GetMapping("/documents/{id}/impact")
    public ResponseEntity<DocumentImpactResponse> getDocumentImpact(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getDocumentImpact(id));
    }
    
    // Relationship queries
    @GetMapping("/parts/{id}/dependencies")
    public ResponseEntity<DependencyGraphResponse> getPartDependencies(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getPartDependencies(id));
    }
    
    // User analytics
    @GetMapping("/users/{id}/contributions")
    public ResponseEntity<UserContributionResponse> getUserContributions(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getUserContributions(id));
    }
    
    // Graph algorithms
    @GetMapping("/parts/{fromId}/path-to/{toId}")
    public ResponseEntity<PathResponse> findShortestPath(
        @PathVariable String fromId, 
        @PathVariable String toId
    ) {
        return ResponseEntity.ok(queryService.findShortestPath(fromId, toId));
    }
}
```

---

## 🔐 Phase 6: Security & Performance

### 6.1 Security Considerations

```properties
# Enable Neo4j security
spring.neo4j.authentication.username=${NEO4J_USER:neo4j}
spring.neo4j.authentication.password=${NEO4J_PASSWORD:password}

# Use encrypted connection in production
spring.neo4j.uri=neo4j+s://neo4j:7687
```

### 6.2 Performance Optimizations

**Create Indexes:**

```cypher
// Run these in Neo4j Browser or initialization script
CREATE INDEX part_id IF NOT EXISTS FOR (p:Part) ON (p.id);
CREATE INDEX user_id IF NOT EXISTS FOR (u:User) ON (u.id);
CREATE INDEX document_id IF NOT EXISTS FOR (d:Document) ON (d.id);
CREATE INDEX change_id IF NOT EXISTS FOR (c:Change) ON (c.id);
CREATE INDEX task_id IF NOT EXISTS FOR (t:Task) ON (t.id);

// Composite indexes
CREATE INDEX part_stage_level IF NOT EXISTS FOR (p:Part) ON (p.stage, p.level);
```

**Create Constraints:**

```cypher
CREATE CONSTRAINT part_id_unique IF NOT EXISTS FOR (p:Part) REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT document_id_unique IF NOT EXISTS FOR (d:Document) REQUIRE d.id IS UNIQUE;
```

---

## 📈 Phase 7: Implementation Roadmap

### Sprint 1: Foundation (Week 1)
- ✅ Add Neo4j to docker-compose
- ✅ Update graph-service configuration
- ✅ Create enhanced node models (Part, Document, Change, User, Task)
- ✅ Test basic Neo4j connectivity

### Sprint 2: Core Integration (Week 2)
- ✅ Implement GraphSyncEvent and message queue
- ✅ Add event publishers to BOM service
- ✅ Add event publishers to Document service
- ✅ Implement GraphSyncListener in graph-service
- ✅ Test Part creation sync

### Sprint 3: Advanced Queries (Week 3)
- ✅ Implement BOM explosion queries
- ✅ Implement where-used queries
- ✅ Implement change impact analysis
- ✅ Implement document impact analysis
- ✅ Add indexes and constraints

### Sprint 4: REST APIs (Week 4)
- ✅ Enhance GraphController with new endpoints
- ✅ Create response DTOs
- ✅ Add Swagger documentation
- ✅ Integration testing

### Sprint 5: Polish & Production (Week 5)
- ✅ Performance tuning
- ✅ Security hardening
- ✅ Monitoring and metrics
- ✅ Documentation updates
- ✅ Production deployment

---

## 🎁 Benefits of Neo4j Integration

### 1. **BOM Management**
- ✅ Fast hierarchical queries (BOM explosion/where-used)
- ✅ Circular dependency detection
- ✅ Multi-level impact analysis

### 2. **Change Management**
- ✅ Understand change propagation
- ✅ Find all affected parts and documents
- ✅ Trace change history and relationships

### 3. **Document Management**
- ✅ Find all parts linked to a document
- ✅ Discover related documents through parts
- ✅ Version relationship tracking

### 4. **User & Organizational Insights**
- ✅ User contribution analytics
- ✅ Organizational hierarchy (reports-to)
- ✅ Collaboration patterns

### 5. **Search & Discovery**
- ✅ Relationship-aware search
- ✅ Similar parts discovery
- ✅ Recommendation engine potential

---

## 🧪 Testing Strategy

### Unit Tests
```java
@SpringBootTest
@Testcontainers
class GraphServiceTest {
    
    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5.15")
        .withAdminPassword("test");
    
    @Test
    void testPartNodeCreation() {
        // Test part node creation
    }
    
    @Test
    void testBomExplosionQuery() {
        // Test BOM explosion
    }
}
```

### Integration Tests
- Test event publishing from BOM service
- Test event consumption in graph service
- Test end-to-end sync flow

---

## 📚 Reference Resources

### Neo4j Documentation
- [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j)
- [Cypher Query Language](https://neo4j.com/docs/cypher-manual/current/)
- [Neo4j Java Driver](https://neo4j.com/docs/java-manual/current/)

### Example Projects
- [Neo4j Spring Boot Examples](https://github.com/neo4j-examples/neo4j-sdn-ogm-tips)

---

## 🚀 Getting Started (Quick Start)

### Step 1: Start Neo4j
```bash
cd infra
docker-compose -f docker-compose-infrastructure.yaml up -d neo4j
```

### Step 2: Access Neo4j Browser
```
http://localhost:7474
Username: neo4j
Password: password
```

### Step 3: Verify Connection
```cypher
RETURN "Hello PLM-Lite Graph!" as message
```

### Step 4: Start Graph Service
```bash
cd infra/graph-service
mvn spring-boot:run
```

### Step 5: Test Basic Operations
```bash
# Create a user
curl -X POST "http://localhost:8090/graph/user?id=user1&name=John Doe"

# Create a task
curl -X POST "http://localhost:8090/graph/task?id=task1&title=Test Task"

# Assign task to user
curl -X POST "http://localhost:8090/graph/assign?userId=user1&taskId=task1"
```

---

## ✅ Success Criteria

- [ ] Neo4j running in docker-compose
- [ ] Graph service connects successfully
- [ ] All node types implemented (Part, Document, Change, User, Task)
- [ ] Event-driven sync working for at least one service
- [ ] BOM explosion query working
- [ ] Change impact analysis working
- [ ] REST APIs documented and tested
- [ ] Performance acceptable (< 100ms for simple queries)

---

## 🤝 Next Steps After Implementation

1. **Elasticsearch Integration** - Full-text search across graph
2. **Neo4j Graph Algorithms** - Community detection, centrality
3. **Real-time Graph Updates** - WebSocket notifications
4. **Graph Visualization UI** - Interactive graph explorer
5. **ML/AI on Graph** - Recommendation engine, anomaly detection

---

**Document Version**: 1.0  
**Last Updated**: October 26, 2025  
**Author**: AI Assistant  
**Status**: Ready for Implementation


# Neo4j Quick Start Guide for PLM-Lite

## 🎯 Overview

This guide will help you quickly set up and test Neo4j integration with your PLM-Lite system.

## 📋 Prerequisites

- ✅ Docker Desktop running
- ✅ Neo4j on ports 7474 and 7687
- ✅ Java 17+ and Maven installed
- ✅ Existing PLM services running (optional for initial testing)

---

## 🚀 Quick Start (5 Minutes)

### Step 1: Start Neo4j

You have two options:

**Option A: Use your existing Docker Neo4j**
```bash
# Your Neo4j is already running on ports 7474 and 7687
# Just verify it's accessible
```

**Option B: Use the PLM infrastructure Docker Compose**
```powershell
# Start Neo4j with other infrastructure services
.\start-neo4j.ps1

# OR manually:
cd infra
docker-compose -f docker-compose-infrastructure.yaml up -d neo4j
```

### Step 2: Access Neo4j Browser

Open your browser to:
```
http://localhost:7474
```

Login credentials:
- **Username**: `neo4j`
- **Password**: `password`

### Step 3: Initialize Neo4j Database

In Neo4j Browser, paste and run the initialization script:

```cypher
// Create constraints
CREATE CONSTRAINT part_id_unique IF NOT EXISTS 
FOR (p:Part) REQUIRE p.id IS UNIQUE;

CREATE CONSTRAINT user_id_unique IF NOT EXISTS 
FOR (u:User) REQUIRE u.id IS UNIQUE;

CREATE CONSTRAINT document_id_unique IF NOT EXISTS 
FOR (d:Document) REQUIRE d.id IS UNIQUE;

CREATE CONSTRAINT change_id_unique IF NOT EXISTS 
FOR (c:Change) REQUIRE c.id IS UNIQUE;

CREATE CONSTRAINT task_id_unique IF NOT EXISTS 
FOR (t:Task) REQUIRE t.id IS UNIQUE;

// Create indexes
CREATE INDEX part_stage_idx IF NOT EXISTS FOR (p:Part) ON (p.stage);
CREATE INDEX part_level_idx IF NOT EXISTS FOR (p:Part) ON (p.level);
CREATE INDEX user_username_idx IF NOT EXISTS FOR (u:User) ON (u.username);
CREATE INDEX document_status_idx IF NOT EXISTS FOR (d:Document) ON (d.status);
CREATE INDEX change_status_idx IF NOT EXISTS FOR (c:Change) ON (c.status);
```

Or run the complete initialization script:
```bash
# In Neo4j Browser, copy and paste contents from:
infra/graph-service/neo4j-init.cypher
```

### Step 4: Start Graph Service

```bash
cd infra/graph-service
mvn clean install
mvn spring-boot:run
```

Wait for the service to start (look for "Started GraphServiceApplication" in logs).

### Step 5: Test the Integration

Run the test script:
```powershell
.\test-neo4j-integration.ps1
```

Or test manually with curl:

```bash
# Create a user
curl -X POST "http://localhost:8090/graph/user?id=user1&name=John%20Doe"

# Create a task
curl -X POST "http://localhost:8090/graph/task?id=task1&title=Test%20Task"

# Assign task to user
curl -X POST "http://localhost:8090/graph/assign?userId=user1&taskId=task1"

# Check health
curl http://localhost:8090/actuator/health
```

### Step 6: Visualize in Neo4j Browser

In Neo4j Browser, run:

```cypher
// View all nodes and relationships
MATCH (n) RETURN n LIMIT 100

// View user-task relationships
MATCH p=()-[r:ASSIGNED_TO]->() RETURN p

// View BOM hierarchy (after adding parts)
MATCH p=(parent:Part)-[:HAS_CHILD*]->(child:Part)
RETURN p
```

---

## 🎨 Understanding the Graph Model

### Node Types

```
┌──────────────────────────────────────────────────┐
│  User          Part         Document    Change   │
│   │             │              │           │      │
│   │             │              │           │      │
│   ├─CREATED_BY──┤              │           │      │
│   │             ├─LINKED_TO────┤           │      │
│   │             │              │           │      │
│   │             ├─AFFECTED_BY──┼─AFFECTS───┘      │
│   │             │              │                  │
│   ├─ASSIGNED_TO─┤              │                  │
│   │           Task             │                  │
│   │                            │                  │
│   └─REPORTS_TO─(Manager)       │                  │
│                                                    │
└──────────────────────────────────────────────────┘
```

### Relationship Types

| Relationship | Direction | Description |
|-------------|-----------|-------------|
| `HAS_CHILD` | Part → Part | BOM hierarchy (with quantity property) |
| `LINKED_TO` | Part → Document | Part-Document associations |
| `CREATED_BY` | Part/Doc → User | Creator relationships |
| `AFFECTS` | Change → Part | Change impact |
| `ASSIGNED_TO` | User → Task | Task assignments |
| `REPORTS_TO` | User → User | Organizational hierarchy |
| `INITIATED_BY` | Change → User | Change initiator |
| `REVIEWED_BY` | Change → User | Change reviewers |

---

## 📚 Example Queries

### BOM Management

```cypher
// Get BOM explosion (all descendants)
MATCH path = (p:Part {id: 'part-001'})-[:HAS_CHILD*]->(child:Part)
RETURN path

// Get where-used (all ancestors)
MATCH path = (p:Part {id: 'part-003'})<-[:HAS_CHILD*]-(parent:Part)
RETURN path

// Find parts at level 2
MATCH (p:Part {level: '2'})
RETURN p.id, p.title, p.stage
```

### Change Impact Analysis

```cypher
// Find all parts affected by a change
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
RETURN p

// Find all child parts affected (recursive)
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
MATCH (p)-[:HAS_CHILD*]->(child:Part)
RETURN DISTINCT child

// Find documents affected by a change
MATCH (c:Change)-[:AFFECTS]->(p:Part)
MATCH (p)-[:LINKED_TO]->(d:Document)
WHERE c.id = 'change-001'
RETURN DISTINCT d
```

### Document Impact

```cypher
// Find all parts linked to a document
MATCH (d:Document {id: 'doc-001'})<-[:LINKED_TO]-(p:Part)
RETURN p

// Find related documents through parts
MATCH (d1:Document {id: 'doc-001'})<-[:LINKED_TO]-(p:Part)-[:LINKED_TO]->(d2:Document)
WHERE d1.id <> d2.id
RETURN DISTINCT d2
```

### User Analytics

```cypher
// Find all parts created by a user
MATCH (u:User {id: 'user-001'})<-[:CREATED_BY]-(p:Part)
RETURN p

// Find user's organizational hierarchy
MATCH path = (u:User {id: 'user-001'})-[:REPORTS_TO*]->(manager:User)
RETURN path

// Find all tasks assigned to a user
MATCH (u:User {id: 'user-001'})-[:ASSIGNED_TO]->(t:Task)
RETURN t
```

### Advanced Queries

```cypher
// Check for circular dependencies
MATCH path = (p1:Part {id: 'part-001'})-[:HAS_CHILD*]->(p2:Part {id: 'part-002'})
WHERE p1.id = p2.id
RETURN path

// Find most affected parts (by changes)
MATCH (c:Change)-[:AFFECTS]->(p:Part)
RETURN p.id, p.title, COUNT(c) as changeCount
ORDER BY changeCount DESC
LIMIT 10

// Find most productive users
MATCH (u:User)<-[:CREATED_BY]-(p:Part)
RETURN u.username, COUNT(p) as partsCreated
ORDER BY partsCreated DESC
```

---

## 🔧 Configuration

### Graph Service Configuration

**File**: `infra/graph-service/src/main/resources/application.properties`

```properties
# Neo4j connection
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password

# Connection pool
spring.neo4j.pool.max-connection-pool-size=50
spring.neo4j.pool.connection-acquisition-timeout=PT30S

# Server port
server.port=8090
```

### Docker Configuration

If using Docker Compose Neo4j (already configured in `docker-compose-infrastructure.yaml`):

```yaml
neo4j:
  image: neo4j:5.15-community
  ports:
    - "7474:7474"
    - "7687:7687"
  environment:
    - NEO4J_AUTH=neo4j/password
```

---

## 🐛 Troubleshooting

### Problem: Can't connect to Neo4j

**Solution 1**: Check if Neo4j is running
```bash
docker ps | grep neo4j
# Should show plm-neo4j container
```

**Solution 2**: Check logs
```bash
docker logs plm-neo4j
```

**Solution 3**: Verify port availability
```bash
netstat -an | findstr "7474"
netstat -an | findstr "7687"
```

### Problem: Authentication failed

**Solution**: Reset Neo4j password
```bash
docker exec -it plm-neo4j cypher-shell -u neo4j -p neo4j
# Then change password to 'password'
```

### Problem: Graph service can't connect to Neo4j

**Solution 1**: If running graph-service locally, use `localhost`:
```properties
spring.neo4j.uri=bolt://localhost:7687
```

**Solution 2**: If running in Docker, use container name:
```properties
spring.neo4j.uri=bolt://neo4j:7687
```

### Problem: Out of memory errors

**Solution**: Increase Neo4j memory in docker-compose:
```yaml
environment:
  - NEO4J_dbms_memory_heap_max__size=4G
  - NEO4J_dbms_memory_pagecache_size=1G
```

---

## 📈 Next Steps

### Phase 1: Basic Integration (Current)
- ✅ Neo4j running
- ✅ Graph service operational
- ✅ Basic User-Task relationships

### Phase 2: BOM Integration (Week 1-2)
- [ ] Add event-driven sync from BOM service
- [ ] Implement Part nodes and relationships
- [ ] Test BOM explosion and where-used queries

### Phase 3: Document Integration (Week 2-3)
- [ ] Sync documents from document-service
- [ ] Implement Part-Document links
- [ ] Document impact analysis queries

### Phase 4: Change Management (Week 3-4)
- [ ] Sync changes from change-service
- [ ] Implement change impact analysis
- [ ] Workflow integration

### Phase 5: Advanced Features (Week 4-5)
- [ ] Graph algorithms (centrality, community detection)
- [ ] Recommendation engine
- [ ] Advanced analytics dashboard

---

## 📚 Resources

### Documentation
- [Neo4j Integration Plan](./NEO4J_INTEGRATION_PLAN.md) - Complete integration guide
- [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j)
- [Cypher Query Language](https://neo4j.com/docs/cypher-manual/current/)

### Graph Service Files
```
infra/graph-service/
├── src/main/java/com/example/graph_service/
│   ├── model/
│   │   ├── PartNode.java          ✅ Enhanced
│   │   ├── DocumentNode.java      ✅ New
│   │   ├── ChangeNode.java        ✅ New
│   │   ├── UserNode.java          ✅ Enhanced
│   │   ├── TaskNode.java          ✅ Enhanced
│   │   └── PartUsageRelationship.java ✅ New
│   ├── repository/
│   │   ├── PartNodeRepository.java      ✅ New
│   │   ├── DocumentNodeRepository.java  ✅ New
│   │   ├── ChangeNodeRepository.java    ✅ New
│   │   ├── UserNodeRepository.java      ✅ Existing
│   │   └── TaskNodeRepository.java      ✅ Existing
│   ├── service/
│   │   └── GraphService.java      ✅ Existing (needs enhancement)
│   └── controller/
│       └── GraphController.java   ✅ Existing (needs enhancement)
├── neo4j-init.cypher             ✅ New - Initialization script
└── application.properties         ✅ Enhanced
```

### Scripts
- `start-neo4j.ps1` - Start Neo4j infrastructure
- `test-neo4j-integration.ps1` - Test basic integration
- `infra/graph-service/neo4j-init.cypher` - Database initialization

---

## ✅ Success Criteria

- [ ] Neo4j accessible at http://localhost:7474
- [ ] Graph service starts without errors
- [ ] Can create users and tasks via REST API
- [ ] Can assign tasks to users
- [ ] Relationships visible in Neo4j Browser
- [ ] Health endpoint shows Neo4j as UP

---

## 🤝 Getting Help

If you encounter issues:

1. Check the logs:
   ```bash
   # Neo4j logs
   docker logs plm-neo4j
   
   # Graph service logs
   cd infra/graph-service && mvn spring-boot:run
   ```

2. Verify configuration:
   ```bash
   # Check Neo4j connectivity
   curl http://localhost:7474
   
   # Check graph service health
   curl http://localhost:8090/actuator/health
   ```

3. Review the comprehensive plan:
   - See [NEO4J_INTEGRATION_PLAN.md](./NEO4J_INTEGRATION_PLAN.md)

---

**Document Version**: 1.0  
**Last Updated**: October 26, 2025  
**Status**: Ready to Use 🚀


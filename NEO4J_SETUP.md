# Neo4j Quick Setup for PLM-Lite

## ✅ What's Been Done

1. **Enhanced Graph Models** - 6 node types (Part, Document, Change, User, Task, PartUsage)
2. **Advanced Repositories** - 3 repositories with 20+ graph queries
3. **Docker Configuration** - Neo4j added to infrastructure
4. **Fixed Compilation Issues** - GraphService updated for new API
5. **Fixed Version Compatibility** - Spring Boot 3.3.5 + Spring Cloud 2023.0.3

## 🚀 Quick Start

### 1. Start Neo4j

```powershell
# Option A: Use your existing Neo4j (ports 7474, 7687)
# Already running? Great! Skip to step 2.

# Option B: Start via docker-compose
cd infra
docker-compose -f docker-compose-infrastructure.yaml up -d neo4j
```

### 2. Initialize Neo4j Database

Open Neo4j Browser: http://localhost:7474

Login: `neo4j` / `password`

Run these initialization commands:

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
CREATE INDEX user_username_idx IF NOT EXISTS FOR (u:User) ON (u.username);
CREATE INDEX document_status_idx IF NOT EXISTS FOR (d:Document) ON (d.status);
```

### 3. Start Graph Service

```powershell
cd infra/graph-service
mvn clean install
mvn spring-boot:run
```

Service will start on port **8090**

### 4. Test the Service

```powershell
# Create a user
curl -X POST "http://localhost:8090/graph/user?id=user1&name=John"

# Create a task
curl -X POST "http://localhost:8090/graph/task?id=task1&title=TestTask"

# Assign task to user
curl -X POST "http://localhost:8090/graph/assign?userId=user1&taskId=task1"

# Check health
curl http://localhost:8090/actuator/health
```

### 5. Visualize in Neo4j Browser

```cypher
// View all nodes
MATCH (n) RETURN n LIMIT 50

// View relationships
MATCH p=()-[r]->() RETURN p LIMIT 50

// View user-task assignments
MATCH p=(u:User)-[:ASSIGNED_TO]->(t:Task) RETURN p
```

## 📊 Graph Model Overview

### Node Types

- **Part** - Product components with BOM hierarchy
- **Document** - Technical documents
- **Change** - Change requests
- **User** - People and organization
- **Task** - Work items

### Key Relationships

- `HAS_CHILD` - Part → Part (BOM hierarchy)
- `LINKED_TO` - Part → Document
- `AFFECTS` - Change → Part
- `ASSIGNED_TO` - User → Task
- `CREATED_BY` - Part/Doc/Change → User
- `REPORTS_TO` - User → User (org hierarchy)

## 🎯 Example Queries

### BOM Explosion
```cypher
MATCH path = (p:Part {id: 'part-001'})-[:HAS_CHILD*]->(child:Part)
RETURN path
```

### Where Used
```cypher
MATCH path = (p:Part {id: 'part-003'})<-[:HAS_CHILD*]-(parent:Part)
RETURN path
```

### Change Impact
```cypher
MATCH (c:Change {id: 'change-001'})-[:AFFECTS]->(p:Part)
MATCH (p)-[:HAS_CHILD*]->(child:Part)
RETURN DISTINCT child
```

### User Tasks
```cypher
MATCH (u:User {id: 'user1'})-[:ASSIGNED_TO]->(t:Task)
RETURN t
```

## 🔧 Configuration

**Graph Service**: `infra/graph-service/src/main/resources/application.properties`

```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password
server.port=8090
```

## 📁 Key Files

### Models
```
infra/graph-service/src/main/java/com/example/graph_service/model/
├── PartNode.java
├── PartUsageRelationship.java
├── DocumentNode.java
├── ChangeNode.java
├── UserNode.java
└── TaskNode.java
```

### Repositories
```
infra/graph-service/src/main/java/com/example/graph_service/repository/
├── PartNodeRepository.java
├── DocumentNodeRepository.java
├── ChangeNodeRepository.java
├── UserNodeRepository.java
└── TaskNodeRepository.java
```

### Services
```
infra/graph-service/src/main/java/com/example/graph_service/
├── service/GraphService.java
└── controller/GraphController.java
```

## 🐛 Troubleshooting

### Neo4j Not Starting
```bash
docker ps | grep neo4j
docker logs plm-neo4j
```

### Can't Connect to Neo4j
- Check Neo4j is running on port 7687
- Verify credentials: neo4j/password
- Check firewall settings

### Service Won't Start
```bash
# Check Neo4j is accessible
curl http://localhost:7474

# Check for port conflicts
netstat -an | findstr "8090"
```

## ✅ Success Checklist

- [ ] Neo4j accessible at http://localhost:7474
- [ ] Constraints and indexes created
- [ ] Graph service compiles without errors
- [ ] Graph service starts on port 8090
- [ ] Health check returns UP
- [ ] Can create users and tasks
- [ ] Can see relationships in Neo4j Browser

## 🎉 You're Ready!

Your Neo4j integration is now functional. Next steps:

1. **Explore the graph** in Neo4j Browser
2. **Try the example queries** above
3. **Plan BOM integration** - sync parts from bom-service
4. **Plan document integration** - sync documents from document-service

---

**Quick Commands Reference:**

```bash
# Start Neo4j
docker-compose -f infra/docker-compose-infrastructure.yaml up -d neo4j

# Start graph service
cd infra/graph-service && mvn spring-boot:run

# Test
curl -X POST "http://localhost:8090/graph/user?id=test&name=Test"

# Neo4j Browser
http://localhost:7474
```

**Created**: October 26, 2025  
**Status**: Ready to Use ✅


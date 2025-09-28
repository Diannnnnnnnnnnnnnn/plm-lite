# Change Service

The Change Service handles change management for documents in the PLM Lite system. It implements the full change lifecycle with proper business rules and multi-database storage.

## Features

### Business Logic
- **Change Creation**: Create changes for released documents only
- **Workflow Management**: IN_WORK → IN_REVIEW → RELEASED status transitions
- **Document Status Updates**: Automatically updates document status when change is approved
- **Relationship Tracking**: Tracks relationships between changes, documents, parts, and users

### Multi-Database Architecture
- **MySQL**: Structured data storage (Change, ChangeDocument, ChangePart entities)
- **Neo4j**: Relationship mapping (Change ↔ Document, Change ↔ Part, Change ↔ User)
- **Elasticsearch**: Full-text search and indexing

## API Endpoints

### Change Management
- `POST /api/changes` - Create a new change
- `GET /api/changes/{id}` - Get change by ID
- `GET /api/changes` - Get all changes
- `PUT /api/changes/{id}/submit-review` - Submit change for review
- `PUT /api/changes/{id}/approve` - Approve change

### Query Operations
- `GET /api/changes/status/{status}` - Get changes by status
- `GET /api/changes/creator/{creator}` - Get changes by creator
- `GET /api/changes/search?keyword={keyword}` - Search changes (SQL)
- `GET /api/changes/search/elastic?query={query}` - Search changes (Elasticsearch)

## Database Schema

### MySQL Tables
```sql
CREATE TABLE Change (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  stage VARCHAR(255) NOT NULL,
  class VARCHAR(255) NOT NULL,
  product VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  creator VARCHAR(255) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  change_reason VARCHAR(255) NOT NULL,
  document_beforechange VARCHAR(255) NOT NULL,
  document_afterchange VARCHAR(255) NOT NULL
);

CREATE TABLE ChangeDocument (
  id VARCHAR(255) PRIMARY KEY,
  changetask_id VARCHAR(255) NOT NULL,
  updated_itemid VARCHAR(255) NOT NULL,
  previous_itemid VARCHAR(255) NOT NULL,
  FOREIGN KEY (changetask_id) REFERENCES Change(id)
);

CREATE TABLE ChangePart (
  id VARCHAR(255) PRIMARY KEY,
  changetask_id VARCHAR(255) NOT NULL,
  part_id VARCHAR(255) NOT NULL,
  FOREIGN KEY (changetask_id) REFERENCES Change(id)
);
```

### Neo4j Relationships
- `(Change)-[:AFFECTS_DOCUMENT]->(Document)`
- `(Change)-[:AFFECTS_PART]->(Part)`
- `(Change)-[:CREATED_BY]->(User)`

## Configuration

### Application Properties
- **Port**: 8083
- **MySQL**: localhost:3306/plm_change_db
- **Neo4j**: bolt://localhost:7687
- **Elasticsearch**: http://localhost:9200

### Development Profile
Use `spring.profiles.active=dev` for:
- H2 in-memory database
- Embedded Neo4j
- Disabled Eureka registration

## Dependencies
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Data Neo4j
- Spring Data Elasticsearch
- Spring Cloud OpenFeign
- MySQL Connector
- H2 Database (dev)

## Running the Service

### Prerequisites
1. MySQL server running on port 3306
2. Neo4j server running on port 7687
3. Elasticsearch server running on port 9200
4. Document service running on port 8082

### Start Commands
```bash
# Production mode
mvn spring-boot:run

# Development mode
mvn spring-boot:run -Dspring.profiles.active=dev

# With specific database
mvn spring-boot:run -Dspring.datasource.url=jdbc:mysql://localhost:3306/custom_db
```

## Testing
```bash
mvn test
```

## Docker Support
```yaml
# docker-compose.yml example
services:
  change-service:
    build: .
    ports:
      - "8083:8083"
    depends_on:
      - mysql
      - neo4j
      - elasticsearch
```
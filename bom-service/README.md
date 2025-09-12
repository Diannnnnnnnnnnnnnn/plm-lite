# BOM Service

A comprehensive Bill of Materials (BOM) service for PLM Lite, designed with a Part-centric hierarchical approach.

## 🎯 Overview

The BOM Service manages Parts and their hierarchical relationships within the PLM system. It follows a business-oriented design where:

1. **Parts are business objects** with metadata (title, stage, level, creator)
2. **BOMs are established based on Part hierarchies** using PartUsage relationships
3. **Parts can be associated with Documents** through DocumentPartLink

## 🏗️ Architecture

### Database Design

```sql
-- Core Part entity
CREATE TABLE `Part` (
  `bigintid` VARCHAR(255) PRIMARY KEY NOT NULL,
  `titlechar` VARCHAR(255) NOT NULL,
  `stage` VARCHAR(255) NOT NULL,
  `level` VARCHAR(255) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` TIMESTAMP NOT NULL
);

-- Part hierarchy relationships
CREATE TABLE `PartUsage` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `child_id` VARCHAR(255) NOT NULL,
  `parent_id` VARCHAR(255) NOT NULL,
  `quantity` INT NOT NULL
);

-- Document-Part associations
CREATE TABLE `DocumentPartLink` (
  `link_id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `part_id` VARCHAR(255) NOT NULL,
  `document_id` VARCHAR(255) NOT NULL
);

-- Change management integration
CREATE TABLE `ChangePart` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `changetask_id` VARCHAR(255) NOT NULL,
  `part_id` VARCHAR(255) NOT NULL
);
```

### Service Responsibilities

1. **MySQL** → Store structured data:
   - Part, PartUsage, DocumentPartLink, ChangePart tables
   
2. **Neo4j** → Store relationships (future integration):
   - Part ↔ Document relationships
   - Part ↔ Part hierarchical relationships
   - Part ↔ Change ↔ User relationships
   
3. **Elasticsearch** → Full-text search (future integration):
   - Index part metadata for searching by title, relations

## 🚀 API Endpoints

### Part CRUD Operations

#### Create Part
```http
POST /parts
Content-Type: application/json

{
  "title": "Engine Assembly",
  "stage": "DETAILED_DESIGN",
  "level": "1",
  "creator": "john.doe"
}
```

#### Get Part
```http
GET /parts/{id}
GET /parts/creator/{creator}
GET /parts/stage/{stage}
GET /parts/search?title={searchTerm}
```

#### Update Part Stage
```http
PATCH /parts/{id}/stage/{stage}
```

#### Delete Part
```http
DELETE /parts/{id}
```

### BOM Hierarchy Operations

#### Add Part Usage (Parent-Child Relationship)
```http
POST /parts/usage
Content-Type: application/json

{
  "parentPartId": "parent-part-id",
  "childPartId": "child-part-id",
  "quantity": 2
}
```

#### Remove Part Usage
```http
DELETE /parts/{parentId}/usage/{childId}
```

#### Update Quantity
```http
PATCH /parts/{parentId}/usage/{childId}/quantity/{newQuantity}
```

#### Get BOM Hierarchy
```http
GET /parts/{id}/bom-hierarchy
```

Response shows complete hierarchy:
```json
{
  "partId": "root-part-id",
  "partTitle": "Engine Assembly",
  "level": "1",
  "quantity": 1,
  "children": [
    {
      "partId": "child-part-id",
      "partTitle": "Cylinder Block",
      "level": "2",
      "quantity": 1,
      "children": [...]
    }
  ]
}
```

#### Get Children/Parents
```http
GET /parts/{id}/children
GET /parts/{id}/parents
```

### Document-Part Linking

#### Link Part to Document
```http
POST /parts/document-link
Content-Type: application/json

{
  "partId": "part-id",
  "documentId": "document-id"
}
```

#### Unlink Part from Document
```http
DELETE /parts/{partId}/document/{documentId}
```

#### Get Associations
```http
GET /parts/{id}/documents
GET /parts/document/{documentId}/parts
```

## 🔧 Business Logic Features

### Circular Dependency Prevention
The service prevents creating circular dependencies in the BOM hierarchy through ancestor checking.

### Soft Delete Support
Parts marked as deleted are kept for 30 days before permanent purging via scheduled tasks.

### Validation
- Part title, stage, level, and creator are required
- Quantity must be positive
- Parts cannot be their own children
- Duplicate relationships are prevented

### Stage Management
Parts can be in different stages of the design process:
- `CONCEPTUAL_DESIGN`
- `DETAILED_DESIGN` 
- `PROTOTYPE`
- `PRODUCTION`
- `OBSOLETE`

## 🔄 Integration Points

### Document Service Integration
- Parts can be linked to documents through DocumentPartLink
- Supports many-to-many relationships between parts and documents

### Change Service Integration (Future)
- ChangePart table ready for change management workflow
- Links parts to change tasks for impact analysis

### Search Service Integration (Future)
- Part metadata indexing for full-text search
- Relationship-aware search capabilities

## 🚦 Error Handling

The service includes comprehensive error handling:

- **ValidationException (400)** - Invalid input data
- **NotFoundException (404)** - Part/relationship not found
- **Global Exception Handler** - Consistent error responses

## 🕐 Scheduled Tasks

- **Daily cleanup** at midnight purges parts deleted more than 30 days ago
- Configurable retention period for soft-deleted records

## 🔗 Dependencies

- **Spring Boot 3.2.0** - Core framework
- **Spring Data JPA** - Database operations
- **MySQL** - Primary data storage
- **Spring Cloud OpenFeign** - Service communication
- **Eureka Client** - Service discovery
- **Validation** - Input validation

## 🎯 Usage Examples

### Creating a Complete BOM Structure

1. **Create root part (Engine)**
```bash
curl -X POST http://localhost:8088/parts \
  -H "Content-Type: application/json" \
  -d '{"title":"Engine Assembly","stage":"DETAILED_DESIGN","level":"1","creator":"engineer1"}'
```

2. **Create child parts**
```bash
curl -X POST http://localhost:8088/parts \
  -H "Content-Type: application/json" \
  -d '{"title":"Cylinder Block","stage":"DETAILED_DESIGN","level":"2","creator":"engineer1"}'
```

3. **Create BOM relationship**
```bash
curl -X POST http://localhost:8088/parts/usage \
  -H "Content-Type: application/json" \
  -d '{"parentPartId":"engine-id","childPartId":"cylinder-id","quantity":1}'
```

4. **View complete hierarchy**
```bash
curl http://localhost:8088/parts/engine-id/bom-hierarchy
```

This design provides a robust, hierarchical BOM management system that integrates seamlessly with the broader PLM ecosystem while maintaining clear separation of concerns and business logic.

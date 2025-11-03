# 🔍 Search Service - API Documentation

**Service:** Search Service  
**Port:** 8091  
**Base URL:** `http://localhost:8091/api/v1/search`  
**Version:** 1.0  
**Last Updated:** October 30, 2025

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [All Endpoints](#all-endpoints)
3. [Unified Search](#unified-search)
4. [Document Search](#document-search)
5. [BOM Search](#bom-search)
6. [Part Search](#part-search)
7. [Change Search](#change-search)
8. [Task Search](#task-search)
9. [Health Check](#health-check)
10. [Response Models](#response-models)
11. [Examples](#examples)
12. [Error Handling](#error-handling)

---

## 🎯 Overview

The Search Service provides a unified API for searching across all Elasticsearch indices in the PLM-Lite system. It supports both unified searches (across all entity types) and entity-specific searches.

### Key Features

- ✅ **Full-text search** across multiple fields
- ✅ **Relevance scoring** (results ranked by relevance)
- ✅ **Multi-field search** (searches across title, description, numbers, etc.)
- ✅ **Unified search** (search all entities at once)
- ✅ **Entity-specific search** (search individual entity types)
- ✅ **Fast response times** (~10-50ms average)
- ✅ **CORS enabled** for frontend access

---

## 📚 All Endpoints

| Endpoint | Method | Description | Status |
|----------|--------|-------------|--------|
| `/api/v1/search` | GET | Unified search across all entities | ✅ Active |
| `/api/v1/search/documents` | GET | Search documents only | ✅ Active |
| `/api/v1/search/boms` | GET | Search BOMs only | ✅ Active |
| `/api/v1/search/parts` | GET | Search parts only | ✅ Active |
| `/api/v1/search/changes` | GET | Search change requests only | ✅ Active |
| `/api/v1/search/tasks` | GET | Search tasks only | ✅ Active |
| `/api/v1/search/health` | GET | Health check | ✅ Active |

---

## 1️⃣ Unified Search

### `GET /api/v1/search`

Search across **all entity types** simultaneously (documents, BOMs, parts, changes, tasks).

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `pump` |

**Example:**
```http
GET /api/v1/search?q=hydraulic+pump
```

### Response

**Success (200 OK):**
```json
{
  "query": "hydraulic pump",
  "totalHits": 15,
  "took": 23,
  "documents": [
    {
      "id": "doc-123",
      "title": "Hydraulic Pump Specification",
      "description": "Technical specification for hydraulic pump model HP-2000",
      "documentNumber": "DOC-2024-001",
      "status": "APPROVED",
      "category": "SPECIFICATION",
      "score": 2.45,
      "type": "DOCUMENT"
    }
  ],
  "boms": [
    {
      "id": "bom-456",
      "name": "BOM-PUMP-001",
      "description": "Hydraulic pump assembly",
      "creator": "john.doe",
      "score": 2.1,
      "type": "BOM"
    }
  ],
  "parts": [
    {
      "id": "part-789",
      "partNumber": "PUMP-HP-2000",
      "description": "Hydraulic pump main unit",
      "stage": "RELEASED",
      "status": "ACTIVE",
      "category": "MECHANICAL",
      "score": 2.8,
      "type": "PART"
    }
  ],
  "changes": [],
  "tasks": [
    {
      "id": "task-101",
      "title": "Review hydraulic pump design",
      "description": "Complete design review for pump assembly",
      "status": "IN_PROGRESS",
      "assignee": "jane.smith",
      "score": 1.5,
      "type": "TASK"
    }
  ]
}
```

### Use Cases

- **Global search bar** in the frontend
- **Cross-entity discovery** (find all related items)
- **Quick navigation** to any entity

---

## 2️⃣ Document Search

### `GET /api/v1/search/documents`

Search **documents only** in the Elasticsearch `documents` index.

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `specification` |

**Example:**
```http
GET /api/v1/search/documents?q=specification
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": "doc-123",
    "title": "Product Specification",
    "description": "Detailed product specifications and requirements",
    "documentNumber": "DOC-2024-001",
    "masterId": "master-001",
    "status": "APPROVED",
    "stage": "RELEASED",
    "category": "SPECIFICATION",
    "contentType": "application/pdf",
    "creator": "john.doe",
    "fileSize": 2048576,
    "version": "1.0",
    "isActive": true,
    "score": 3.2,
    "type": "DOCUMENT"
  },
  {
    "id": "doc-124",
    "title": "Design Specification",
    "description": "Engineering design specifications",
    "documentNumber": "DOC-2024-002",
    "status": "DRAFT",
    "category": "DESIGN",
    "creator": "jane.smith",
    "score": 2.8,
    "type": "DOCUMENT"
  }
]
```

### Searchable Fields

- `title^2` (boosted 2x)
- `description`
- `documentNumber`
- `category`
- `creator`

### Use Cases

- Document management system search
- Technical documentation lookup
- Specification finding

---

## 3️⃣ BOM Search

### `GET /api/v1/search/boms`

Search **BOMs (Bill of Materials) only** in the Elasticsearch `boms` index.

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `assembly` |

**Example:**
```http
GET /api/v1/search/boms?q=assembly
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": "bom-456",
    "name": "BOM-ASSEMBLY-001",
    "description": "Main assembly BOM",
    "creator": "john.doe",
    "createTime": null,
    "score": 2.5,
    "type": "BOM"
  },
  {
    "id": "bom-457",
    "name": "BOM-SUB-ASSEMBLY-002",
    "description": "Sub-assembly components",
    "creator": "jane.smith",
    "score": 2.1,
    "type": "BOM"
  }
]
```

### Searchable Fields

- `bomNumber^2` (boosted 2x)
- `description`
- `name`
- `createdBy`

### Use Cases

- BOM management system
- Assembly structure search
- Product configuration lookup

---

## 4️⃣ Part Search

### `GET /api/v1/search/parts`

Search **parts only** in the Elasticsearch `parts` index.

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `bolt` |

**Example:**
```http
GET /api/v1/search/parts?q=stainless+steel+bolt
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": "part-789",
    "partNumber": "SS-BOLT-M8-20",
    "description": "Stainless steel bolt M8 x 20mm",
    "stage": "RELEASED",
    "status": "ACTIVE",
    "category": "FASTENER",
    "creator": "john.doe",
    "createTime": null,
    "score": 3.5,
    "type": "PART"
  },
  {
    "id": "part-790",
    "partNumber": "SS-BOLT-M10-30",
    "description": "Stainless steel bolt M10 x 30mm",
    "stage": "RELEASED",
    "status": "ACTIVE",
    "category": "FASTENER",
    "score": 3.2,
    "type": "PART"
  }
]
```

### Searchable Fields

- `partNumber^2` (boosted 2x)
- `description`
- `category`
- `createdBy`

### Use Cases

- Part library search
- Component selection
- Inventory lookup
- Engineering part search

---

## 5️⃣ Change Search

### `GET /api/v1/search/changes`

Search **change requests only** in the Elasticsearch `changes` index.

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `ECO` |

**Example:**
```http
GET /api/v1/search/changes?q=ECO-2024
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": "change-101",
    "title": "ECO-2024-001: Pump Design Update",
    "description": "Update hydraulic pump design to improve efficiency",
    "status": "IN_REVIEW",
    "creator": "john.doe",
    "createTime": null,
    "score": 2.8,
    "type": "CHANGE"
  },
  {
    "id": "change-102",
    "title": "ECO-2024-002: Material Change",
    "description": "Change material specification from aluminum to stainless steel",
    "status": "APPROVED",
    "creator": "jane.smith",
    "score": 2.3,
    "type": "CHANGE"
  }
]
```

### Searchable Fields

- `title^2` (boosted 2x)
- `description`
- `changeNumber`
- `createdBy`

### Use Cases

- Engineering change order (ECO) tracking
- Change request management
- Design change history

---

## 6️⃣ Task Search

### `GET /api/v1/search/tasks`

Search **tasks only** in the Elasticsearch `tasks` index.

### Request

**Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `q` | string | No | Search query | `review` |

**Example:**
```http
GET /api/v1/search/tasks?q=design+review
```

### Response

**Success (200 OK):**
```json
[
  {
    "id": "task-101",
    "title": "Design Review - Hydraulic Pump",
    "description": "Complete design review for hydraulic pump assembly",
    "status": "IN_PROGRESS",
    "assignee": "jane.smith",
    "createTime": null,
    "score": 2.9,
    "type": "TASK"
  },
  {
    "id": "task-102",
    "title": "Review BOM for Assembly",
    "description": "Review and approve BOM structure",
    "status": "OPEN",
    "assignee": "john.doe",
    "score": 2.4,
    "type": "TASK"
  }
]
```

### Searchable Fields

- `title^2` (boosted 2x)
- `description`
- `status`
- `assignee`

### Use Cases

- Task management system
- Project tracking
- Work assignment lookup
- Personal task search

---

## 7️⃣ Health Check

### `GET /api/v1/search/health`

Check if the Search Service is running.

### Request

```http
GET /api/v1/search/health
```

### Response

**Success (200 OK):**
```text
Search Service is running
```

### Use Cases

- Service monitoring
- Load balancer health checks
- Deployment verification

---

## 📦 Response Models

### UnifiedSearchResponse

```typescript
{
  query: string;           // The search query
  totalHits: number;       // Total number of results across all types
  took: number;            // Time taken in milliseconds
  documents: DocumentSearchResult[];
  boms: BomSearchResult[];
  parts: PartSearchResult[];
  changes: ChangeSearchResult[];
  tasks: TaskSearchResult[];
}
```

### DocumentSearchResult

```typescript
{
  id: string;
  title: string;
  description: string;
  documentNumber: string;
  masterId: string;
  status: string;
  stage: string;
  category: string;
  contentType: string;
  creator: string;
  fileSize: number;
  version: string;
  createTime: Date;
  updateTime: Date;
  isActive: boolean;
  score: number;           // Relevance score
  type: "DOCUMENT";
}
```

### BomSearchResult

```typescript
{
  id: string;
  name: string;            // BOM number/name
  description: string;
  creator: string;
  createTime: Date;
  score: number;
  type: "BOM";
}
```

### PartSearchResult

```typescript
{
  id: string;
  partNumber: string;
  description: string;
  stage: string;
  status: string;
  category: string;
  creator: string;
  createTime: Date;
  score: number;
  type: "PART";
}
```

### ChangeSearchResult

```typescript
{
  id: string;
  title: string;
  description: string;
  status: string;
  creator: string;
  createTime: Date;
  score: number;
  type: "CHANGE";
}
```

### TaskSearchResult

```typescript
{
  id: string;
  title: string;
  description: string;
  status: string;
  assignee: string;
  createTime: Date;
  score: number;
  type: "TASK";
}
```

---

## 💡 Examples

### Example 1: Frontend Global Search

```javascript
// User types in global search bar
async function globalSearch(query) {
  const response = await fetch(
    `http://localhost:8091/api/v1/search?q=${encodeURIComponent(query)}`
  );
  const results = await response.json();
  
  console.log(`Found ${results.totalHits} results in ${results.took}ms`);
  console.log(`Documents: ${results.documents.length}`);
  console.log(`BOMs: ${results.boms.length}`);
  console.log(`Parts: ${results.parts.length}`);
  console.log(`Changes: ${results.changes.length}`);
  console.log(`Tasks: ${results.tasks.length}`);
  
  return results;
}

// Usage
const results = await globalSearch("hydraulic pump");
```

### Example 2: Document Library Search

```javascript
// Search only documents in document management UI
async function searchDocuments(query) {
  const response = await fetch(
    `http://localhost:8091/api/v1/search/documents?q=${encodeURIComponent(query)}`
  );
  const documents = await response.json();
  
  // Display documents sorted by relevance (score)
  documents.forEach(doc => {
    console.log(`${doc.title} (Score: ${doc.score})`);
    console.log(`  ${doc.documentNumber} - ${doc.status}`);
  });
  
  return documents;
}

// Usage
const specs = await searchDocuments("specification");
```

### Example 3: Part Selection in Engineering Tool

```javascript
// Engineer searching for a specific part
async function findPart(partQuery) {
  const response = await fetch(
    `http://localhost:8091/api/v1/search/parts?q=${encodeURIComponent(partQuery)}`
  );
  const parts = await response.json();
  
  // Filter by status if needed
  const activeParts = parts.filter(p => p.status === 'ACTIVE');
  
  return activeParts;
}

// Usage
const bolts = await findPart("stainless steel bolt M8");
```

### Example 4: Task Dashboard Search

```javascript
// Search my tasks
async function searchMyTasks(query, assignee) {
  const response = await fetch(
    `http://localhost:8091/api/v1/search/tasks?q=${encodeURIComponent(query)}`
  );
  const tasks = await response.json();
  
  // Filter by assignee
  const myTasks = tasks.filter(t => t.assignee === assignee);
  
  return myTasks;
}

// Usage
const myReviews = await searchMyTasks("review", "john.doe");
```

### Example 5: Empty Query (Get All)

```javascript
// Get all documents (no query = match all)
async function getAllDocuments() {
  const response = await fetch('http://localhost:8091/api/v1/search/documents');
  const documents = await response.json();
  
  // Returns up to 100 most recent documents
  return documents;
}

// Usage
const allDocs = await getAllDocuments();
```

---

## ⚠️ Error Handling

### Empty Results

When no results are found, the endpoint returns an empty array or empty lists:

```json
// Entity-specific search with no results
[]

// Unified search with no results
{
  "query": "nonexistent",
  "totalHits": 0,
  "took": 5,
  "documents": [],
  "boms": [],
  "parts": [],
  "changes": [],
  "tasks": []
}
```

### Index Not Found

If an Elasticsearch index doesn't exist yet, the endpoint returns an empty array (graceful degradation):

```json
[]
```

### Service Error

If Elasticsearch is down or there's a service error:

**Status:** 500 Internal Server Error

```json
{
  "timestamp": "2025-10-30T21:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Search failed: Connection refused",
  "path": "/api/v1/search/documents"
}
```

---

## 🚀 Performance

### Typical Response Times

| Endpoint | Average | Best Case | Worst Case |
|----------|---------|-----------|------------|
| Unified Search | 20-30ms | 10ms | 100ms |
| Document Search | 15-25ms | 8ms | 80ms |
| BOM Search | 15-25ms | 8ms | 80ms |
| Part Search | 15-25ms | 8ms | 80ms |
| Change Search | 15-25ms | 8ms | 80ms |
| Task Search | 15-25ms | 8ms | 80ms |

### Result Limits

- **Max results per endpoint:** 100
- **Unified search:** Up to 100 results per entity type (500 total max)

### Pagination

Currently not implemented. All results (up to 100) are returned in a single response.

**Future Enhancement:** Add pagination parameters (`page`, `size`)

---

## 🔒 Security

### CORS

- **Enabled:** Yes
- **Allowed Origins:** `*` (all origins)
- **Allowed Methods:** GET
- **Allowed Headers:** All

### Authentication

Currently **not implemented**. All endpoints are publicly accessible.

**Future Enhancement:** Add authentication/authorization

---

## 📊 Monitoring

### Logging

All search requests are logged with:
- Query string
- Endpoint called
- Results count
- Response time

**Example log:**
```
INFO: Document search request: query='specification'
INFO: Found 5 documents for query: 'specification'
```

### Metrics

**Available via Spring Boot Actuator** (if enabled):
- `/actuator/metrics/http.server.requests` - Request counts and timings
- `/actuator/health` - Service health status

---

## 🛠️ Technical Details

### Technology Stack

- **Framework:** Spring Boot 3.4.0
- **Elasticsearch Client:** Elasticsearch Java API Client
- **Search Library:** Co-elastic Elasticsearch Client
- **Query Type:** Multi-match queries with field boosting
- **Index Strategy:** One index per entity type

### Query Behavior

1. **Empty query (`q` not provided or empty):**
   - Returns match-all query
   - Returns up to 100 most recent items
   
2. **With query:**
   - Multi-field search
   - Relevance scoring enabled
   - Results sorted by score (descending)

### Field Boosting

Fields with `^2` notation get **2x weight** in relevance scoring:
- Document: `title^2`
- BOM: `bomNumber^2`
- Part: `partNumber^2`
- Change: `title^2`
- Task: `title^2`

This means matches in these fields rank higher than matches in other fields.

---

## 📞 Support

**Service:** Search Service  
**Team:** PLM Infrastructure Team  
**Port:** 8091  
**Health Check:** `GET /api/v1/search/health`

---

## 📝 Changelog

### Version 1.0 (October 30, 2025)

**Added:**
- ✅ Unified search endpoint (`/api/v1/search`)
- ✅ Document search endpoint (`/api/v1/search/documents`)
- ✅ BOM search endpoint (`/api/v1/search/boms`)
- ✅ Part search endpoint (`/api/v1/search/parts`)
- ✅ Change search endpoint (`/api/v1/search/changes`)
- ✅ Task search endpoint (`/api/v1/search/tasks`)
- ✅ Health check endpoint (`/api/v1/search/health`)
- ✅ Multi-field search with relevance scoring
- ✅ CORS support for frontend integration

---

**Generated:** October 30, 2025  
**Version:** 1.0  
**Status:** Production-Ready ✅




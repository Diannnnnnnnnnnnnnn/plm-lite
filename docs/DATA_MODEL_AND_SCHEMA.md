# PLM System - Data Model and Database Schema Documentation

## Table of Contents
1. [Overview](#overview)
2. [Database Architecture](#database-architecture)
3. [User Service Schema](#user-service-schema)
4. [BOM Service Schema](#bom-service-schema)
5. [Change Service Schema](#change-service-schema)
6. [Document Service Schema](#document-service-schema)
7. [Task Service Schema](#task-service-schema)
8. [Graph Service Schema (Neo4j)](#graph-service-schema-neo4j)
9. [Common Domain Models](#common-domain-models)
10. [Entity Relationships](#entity-relationships)
11. [Data Flow and Synchronization](#data-flow-and-synchronization)

---

## Overview

The PLM (Product Lifecycle Management) system uses a **microservices architecture** with a **hybrid database approach**:
- **MySQL/H2** databases for transactional data in each service
- **Neo4j** graph database for relationship queries and analytics
- **Redis** for caching and session management
- **MinIO** for file storage

Each microservice owns its own database schema and data, following the **database-per-service pattern**. Data synchronization between relational databases and Neo4j happens through event-driven messaging using RabbitMQ.

---

## Database Architecture

### Service Database Mapping

| Service | Database Type | Production DB | Dev DB | Purpose |
|---------|--------------|---------------|---------|---------|
| user-service | MySQL / H2 | plm_user_db | userdb | User accounts and authentication |
| bom-service | MySQL / H2 | plm_bom_db | bomdb | Parts, BOMs, and part relationships |
| change-service | MySQL / H2 | plm_change_db | changedb | Engineering change requests |
| document-service | MySQL / H2 | plm_document_db | documentdb | Document management and versioning |
| task-service | MySQL / H2 | plm_task_db | taskdb | Tasks and workflow management |
| graph-service | Neo4j | neo4j | neo4j | Graph relationships across entities |
| auth-service | Redis | - | - | Token blacklist and sessions |

### Profile-Based Configuration

The system uses **Spring profiles** to support different database backends:

- **Default Profile (Production)**: MySQL databases on localhost:3306
- **Dev Profile (Development)**: H2 file-based databases in ./data/ directory

**Switching Profiles:**
```bash
# Production (MySQL)
mvn spring-boot:run -Dspring-boot.run.profiles=default

# Development (H2)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

See [MYSQL_MIGRATION_GUIDE.md](../MYSQL_MIGRATION_GUIDE.md) for detailed setup instructions.

---

## User Service Schema

### Entity: `User`

**Table Name:** `User`

**Purpose:** Stores user account information, credentials, and roles.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| username | String | NOT NULL, UNIQUE | User's login username |
| password | String | NOT NULL | Encrypted password (BCrypt) |
| roles | String | - | JSON array of user roles (stored as string) |

#### Notes
- Roles are stored as a JSON string and converted to/from `List<String>` in the application layer
- Common roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_ENGINEER`
- Password is encrypted using BCrypt before storage
- User data is synchronized to Neo4j as `UserNode`

---

## BOM Service Schema

The BOM (Bill of Materials) service manages parts, their hierarchical relationships, and BOM structures.

### Entity: `Part`

**Table Name:** `Part`

**Purpose:** Core entity representing a part/component in the PLM system.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Part ID (e.g., "PART-001") |
| titlechar | String | NOT NULL | Part title/name |
| description | String | - | Detailed description of the part |
| stage | Enum (Stage) | NOT NULL | Lifecycle stage |
| status | Enum (Status) | NOT NULL | Current status |
| level | String | NOT NULL | Part level in hierarchy |
| creator | String | NOT NULL | Username of creator |
| create_time | LocalDateTime | NOT NULL | Creation timestamp |
| updateTime | LocalDateTime | NOT NULL | Last update timestamp |
| deleted | Boolean | NOT NULL, DEFAULT false | Soft delete flag |
| deleteTime | LocalDateTime | - | Deletion timestamp |

#### Relationships
- **One-to-Many** with `PartUsage` (as parent) - children of this part
- **One-to-Many** with `PartUsage` (as child) - parents of this part
- **One-to-Many** with `DocumentPartLink` - linked documents

---

### Entity: `PartUsage`

**Table Name:** `PartUsage`

**Purpose:** Represents parent-child relationships between parts (hierarchical BOM structure).

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Usage relationship ID |
| parent_id | String | FOREIGN KEY → Part.id, NOT NULL | Parent part |
| child_id | String | FOREIGN KEY → Part.id, NOT NULL | Child part |
| quantity | Integer | NOT NULL | Quantity of child in parent |

#### Relationships
- **Many-to-One** with `Part` (parent)
- **Many-to-One** with `Part` (child)

---

### Entity: `BomHeader` ⚠️ DEPRECATED - Use `Part` instead

**Status:** **REMOVED** - Migrated to Part-based system

This entity has been removed. BOM functionality is now provided by:
- **Part** entity for the BOM structure
- **PartUsage** for parent-child relationships with quantities

See the [BOM_TO_PART_MIGRATION_COMPLETE.md](./BOM_TO_PART_MIGRATION_COMPLETE.md) for details.

---

### Entity: `BomItem` ⚠️ DEPRECATED - Use `PartUsage` instead

**Status:** **REMOVED** - Migrated to PartUsage relationships

This entity has been removed. BOM item functionality is now provided by the **PartUsage** entity which creates parent-child relationships between Parts.

---

### Entity: `DocumentPartLink`

**Table Name:** `DocumentPartLink`

**Purpose:** Links documents to parts (many-to-many relationship).

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| link_id | String | PRIMARY KEY | Link ID |
| part_id | String | FOREIGN KEY → Part.id, NOT NULL | Part reference |
| document_id | String | NOT NULL | Document ID (from document-service) |

#### Relationships
- **Many-to-One** with `Part`

---

## Change Service Schema

Manages engineering change requests (ECRs) and their relationships to parts, documents, and BOMs.

### Entity: `Change`

**Table Name:** `Change`

**Purpose:** Core entity for engineering change requests.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Change request ID |
| title | String | NOT NULL | Change title |
| stage | Enum (Stage) | NOT NULL | Lifecycle stage |
| class | String | NOT NULL | Change classification |
| product | String | NOT NULL | Affected product |
| status | Enum (Status) | NOT NULL | Current status |
| creator | String | NOT NULL | Username of creator |
| create_time | LocalDateTime | NOT NULL | Creation timestamp |
| change_reason | String | NOT NULL | Reason for change |
| change_document | String | NOT NULL | Related document reference |

#### Relationships
- **One-to-Many** with `ChangePart` - affected parts
- **One-to-Many** with `ChangeDocument` - related documents
- **One-to-Many** with `ChangeBom` - affected BOMs

---

### Entity: `ChangePart`

**Table Name:** `ChangePart`

**Purpose:** Links change requests to affected parts.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Link ID |
| changetask_id | String | FOREIGN KEY → Change.id, NOT NULL | Change request |
| part_id | String | NOT NULL | Part ID (from bom-service) |

#### Relationships
- **Many-to-One** with `Change`

---

### Entity: `ChangeDocument`

**Table Name:** `ChangeDocument`

**Purpose:** Links change requests to related documents.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Link ID |
| changetask_id | String | FOREIGN KEY → Change.id, NOT NULL | Change request |
| document_id | String | NOT NULL | Document ID (from document-service) |

#### Relationships
- **Many-to-One** with `Change`

---

### Entity: `ChangeBom` ⚠️ DEPRECATED - Use `ChangePart` instead

**Status:** **REMOVED** - Redundant with ChangePart

This table has been removed as it was redundant. All Change-Part relationships are now in the `ChangePart` table.

---

## Document Service Schema

Manages document lifecycle, versioning, and file storage metadata.

### Entity: `DocumentMaster`

**Table Name:** `DocumentMaster`

**Purpose:** Master record for a document (across all versions).

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Master document ID |
| title | String | - | Document title |
| creator | String | - | Username of creator |
| category | String | - | Document category |
| createTime | LocalDateTime | DEFAULT NOW | Creation timestamp |

#### Relationships
- **One-to-Many** with `Document` - all versions of this document

---

### Entity: `Document`

**Table Name:** `Document`

**Purpose:** Individual document version/revision.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Document version ID |
| master_id | String | FOREIGN KEY → DocumentMaster.id, NOT NULL | Master document |
| version | Integer | NOT NULL | Version number |
| revision | Integer | NOT NULL | Revision number |
| stage | Enum (Stage) | - | Lifecycle stage |
| status | Enum (Status) | - | Current status |
| title | String | - | Document title |
| description | String | - | Document description |
| creator | String | - | Username of creator |
| createTime | LocalDateTime | DEFAULT NOW | Creation timestamp |
| fileKey | String | - | MinIO storage key |
| bomId | String | - | Related BOM ID |
| originalFilename | String | - | Original uploaded filename |
| contentType | String | - | MIME type |
| fileSize | Long | - | File size in bytes |
| storageLocation | String | - | Storage location (MINIO/LOCAL) |
| fileUploadedAt | LocalDateTime | - | File upload timestamp |
| isActive | Boolean | NOT NULL, DEFAULT true | Is current active version |
| partId | String | - | Related Part ID (renamed from bomId) |

#### Computed Fields
- `fullVersion` (Transient): Returns "v{revision}.{version}"

#### Relationships
- **Many-to-One** with `DocumentMaster`

---

### Entity: `DocumentHistory`

**Table Name:** `DocumentHistory`

**Purpose:** Audit trail for document changes.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | History entry ID |
| documentId | String | - | Related document ID |
| action | String | - | Action performed |
| oldValue | String | - | Previous value |
| newValue | String | - | New value |
| created_by | String | - | Username who made change |
| comment | String | - | Optional comment |
| timestamp | LocalDateTime | DEFAULT NOW | When change occurred |

---

## Task Service Schema

Manages tasks, workflows, and approval processes.

### Entity: `Task`

**Table Name:** `Task`

**Purpose:** Represents a work task or approval request.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Task ID |
| task_name | String | NOT NULL | Task name/title |
| task_description | String | - | Detailed description |
| task_type | Enum (TaskType) | NOT NULL | Type of task |
| task_status | Enum (TaskStatus) | NOT NULL | Current status |
| assigned_to | String | NOT NULL | Assignee username |
| assigned_by | String | NOT NULL | Assigner username |
| due_date | LocalDateTime | - | Due date |
| created_at | LocalDateTime | NOT NULL | Creation timestamp |
| updated_at | LocalDateTime | - | Last update timestamp |
| priority | Integer | - | Priority level |
| parent_task_id | String | - | Parent task (for subtasks) |
| workflow_id | String | - | Associated workflow instance |
| context_type | String | - | Context entity type (PART, DOCUMENT, CHANGE) |
| context_id | String | - | Context entity ID |

#### Relationships
- **One-to-Many** with `TaskSignoff` - approval records
- **One-to-Many** with `FileMetadata` - attached files

---

### Entity: `TaskSignoff`

**Table Name:** `TaskSignoff`

**Purpose:** Records approvals, rejections, and comments on tasks.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String | PRIMARY KEY | Signoff ID |
| task_id | String | NOT NULL | Related task ID |
| signoff_user | String | NOT NULL | User who signed off |
| signoff_action | Enum (SignoffAction) | NOT NULL | Action taken |
| comments | String | - | Optional comments |
| signoff_timestamp | LocalDateTime | NOT NULL | When signoff occurred |
| is_required | Boolean | NOT NULL, DEFAULT true | Is this a required signoff |

---

### Entity: `FileMetadata`

**Table Name:** `FileMetadata`

**Purpose:** Metadata for files attached to tasks.

#### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PRIMARY KEY, AUTO_INCREMENT | File metadata ID |
| filename | String | - | Original filename |
| fileUrl | String | - | Storage URL |
| task_id | Long | FOREIGN KEY → Task.id | Associated task |

#### Relationships
- **Many-to-One** with `Task`

---

## Graph Service Schema (Neo4j)

The graph database provides relationship queries and analytics across the entire PLM system. Data is synchronized from MySQL databases via event-driven messaging.

### Node: `UserNode`

**Label:** `User`

**Purpose:** User representation in the graph database.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | String | User ID (Primary) |
| username | String | Username |
| email | String | Email address |
| department | String | Department |
| role | String | Primary role |

#### Relationships

| Type | Direction | Target | Description |
|------|-----------|--------|-------------|
| CREATED_BY | INCOMING | PartNode | Parts created by user |
| CREATED_BY | INCOMING | DocumentNode | Documents created by user |
| INITIATED_BY | INCOMING | ChangeNode | Changes initiated by user |
| ASSIGNED_TO | OUTGOING | TaskNode | Tasks assigned to user |
| REPORTS_TO | OUTGOING | UserNode | Manager relationship |
| REPORTS_TO | INCOMING | UserNode | Direct reports |

---

### Node: `PartNode`

**Label:** `Part`

**Purpose:** Part representation in the graph database.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | String | Part ID (Primary) |
| title | String | Part title |
| description | String | Part description |
| stage | String | Lifecycle stage |
| status | String | Current status |
| level | String | Part level |
| creator | String | Creator username |
| createTime | LocalDateTime | Creation timestamp |

#### Relationships

| Type | Direction | Target | Description |
|------|-----------|--------|-------------|
| HAS_CHILD | OUTGOING | PartNode | Child parts (with quantity) |
| HAS_CHILD | INCOMING | PartNode | Parent parts |
| LINKED_TO | OUTGOING | DocumentNode | Linked documents |
| CREATED_BY | OUTGOING | UserNode | Creator |
| AFFECTED_BY | INCOMING | ChangeNode | Affecting changes |

---

### Node: `DocumentNode`

**Label:** `Document`

**Purpose:** Document representation in the graph database.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | String | Document ID (Primary) |
| name | String | Document name |
| description | String | Document description |
| version | String | Version string |
| status | String | Current status |
| fileType | String | File type/extension |
| fileSize | Long | File size in bytes |
| createTime | LocalDateTime | Creation timestamp |

#### Relationships

| Type | Direction | Target | Description |
|------|-----------|--------|-------------|
| LINKED_TO | INCOMING | PartNode | Linked parts |
| CREATED_BY | OUTGOING | UserNode | Creator |
| UPLOADED_BY | OUTGOING | UserNode | Uploader |
| RELATED_TO | OUTGOING | ChangeNode | Related changes |

---

### Node: `ChangeNode`

**Label:** `Change`

**Purpose:** Change request representation in the graph database.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | String | Change ID (Primary) |
| title | String | Change title |
| description | String | Change description |
| status | String | Current status |
| priority | String | Priority level |
| changeType | String | Type of change |
| createTime | LocalDateTime | Creation timestamp |

#### Relationships

| Type | Direction | Target | Description |
|------|-----------|--------|-------------|
| AFFECTS | OUTGOING | PartNode | Affected parts |
| RELATED_TO | INCOMING | DocumentNode | Related documents |
| INITIATED_BY | OUTGOING | UserNode | Initiator |
| REVIEWED_BY | OUTGOING | UserNode | Reviewers |
| HAS_TASK | OUTGOING | TaskNode | Associated tasks |

---

### Node: `TaskNode`

**Label:** `Task`

**Purpose:** Task representation in the graph database.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | String | Task ID (Primary) |
| title | String | Task title |
| description | String | Task description |
| status | String | Current status |
| dueDate | LocalDateTime | Due date |
| createTime | LocalDateTime | Creation timestamp |

#### Relationships

| Type | Direction | Target | Description |
|------|-----------|--------|-------------|
| ASSIGNED_TO | INCOMING | UserNode | Assignee |
| CREATED_BY | OUTGOING | UserNode | Creator |
| RELATED_TO_CHANGE | OUTGOING | ChangeNode | Related change |
| RELATED_TO_PART | OUTGOING | PartNode | Related part |

---

### Relationship: `PartUsageRelationship`

**Type:** `HAS_CHILD`

**Purpose:** Stores quantity and metadata for part hierarchies.

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| id | Long | Relationship ID (Auto-generated) |
| quantity | Integer | Quantity of child in parent |
| createdAt | LocalDateTime | When relationship created |

#### Connects
- Source: `PartNode` (parent)
- Target: `PartNode` (child)

---

## Common Domain Models

These enums are shared across multiple services (in `plm-common` module).

### Enum: `Status`

**Purpose:** Represents the approval/release status of entities.

#### Values
- `DRAFT` - Initial draft state
- `IN_WORK` - Work in progress
- `IN_REVIEW` - Under review
- `IN_TECHNICAL_REVIEW` - Technical review phase
- `APPROVED` - Approved but not released
- `RELEASED` - Released for production
- `OBSOLETE` - No longer active

---

### Enum: `Stage`

**Purpose:** Represents the lifecycle stage of entities.

#### Values
- `CONCEPTUAL_DESIGN` - Early concept phase
- `PRELIMINARY_DESIGN` - Preliminary design
- `DETAILED_DESIGN` - Detailed design phase
- `MANUFACTURING` - Manufacturing phase
- `IN_SERVICE` - In active service
- `RETIRED` - Retired/end of life

---

### Enum: `TaskType`

**Purpose:** Categorizes types of tasks.

#### Values
- `APPROVAL` - Approval request
- `REVIEW` - Review task
- `NOTIFICATION` - Notification only
- `ACTION` - Action item
- `WORKFLOW` - Workflow-driven task

---

### Enum: `TaskStatus`

**Purpose:** Status of a task.

#### Values
- `PENDING` - Not yet started
- `IN_PROGRESS` - Currently being worked on
- `COMPLETED` - Finished
- `CANCELLED` - Cancelled
- `OVERDUE` - Past due date

---

### Enum: `SignoffAction`

**Purpose:** Action taken during task signoff.

#### Values
- `APPROVED` - Approved
- `REJECTED` - Rejected
- `DEFERRED` - Deferred for later
- `COMMENTED` - Comment only (no decision)

---

## Entity Relationships

### Cross-Service Relationships

The system maintains referential integrity across services through:
1. **ID references** (not foreign keys) between services
2. **Event-driven synchronization** via RabbitMQ
3. **Graph database** for querying cross-service relationships

#### Part ↔ Document Relationship
- **BOM Service**: `DocumentPartLink` table stores `part_id` and `document_id`
- **Document Service**: Documents can reference `partId` (renamed from `bomId`)
- **Neo4j**: `LINKED_TO` relationship between `PartNode` and `DocumentNode`

#### Change ↔ Part Relationship
- **Change Service**: `ChangePart` table stores `change_id` and `part_id`
- **Neo4j**: `AFFECTS` relationship from `ChangeNode` to `PartNode`

#### Change ↔ Document Relationship
- **Change Service**: `ChangeDocument` table stores `change_id` and `document_id`
- **Neo4j**: `RELATED_TO` relationship between `ChangeNode` and `DocumentNode`

#### Task ↔ User Relationship
- **Task Service**: `Task.assigned_to` and `Task.assigned_by` store usernames
- **Neo4j**: `ASSIGNED_TO` relationship from `UserNode` to `TaskNode`

#### User ↔ Entity Creation
- **All Services**: `creator` field stores username
- **Neo4j**: `CREATED_BY` relationship from entity nodes to `UserNode`

---

## Data Flow and Synchronization

### Event-Driven Architecture

The system uses **RabbitMQ** for asynchronous communication and data synchronization:

#### Exchange and Queue Configuration

| Exchange | Type | Queue | Purpose |
|----------|------|-------|---------|
| plm-events | topic | user.sync.queue | User synchronization |
| plm-events | topic | part.sync.queue | Part synchronization |
| plm-events | topic | task.sync.queue | Task synchronization |
| plm-events | topic | document.sync.queue | Document synchronization |
| plm-events | topic | change.sync.queue | Change synchronization |

#### Synchronization Flow

1. **Create/Update in MySQL**
   - Service receives REST API request
   - Entity is created/updated in MySQL database
   - Service publishes event to RabbitMQ

2. **Graph Service Listens**
   - Graph service consumes event from queue
   - Creates/updates corresponding node in Neo4j
   - Establishes relationships based on entity data

3. **Real-time Consistency**
   - MySQL serves as source of truth
   - Neo4j provides optimized relationship queries
   - Event-driven updates maintain eventual consistency

#### Example: Part Creation Flow

```
Client → POST /api/parts
  ↓
BOM Service → Save to MySQL (Part table)
  ↓
Publish "part.created" event → RabbitMQ
  ↓
Graph Service consumes event
  ↓
Create PartNode in Neo4j
  ↓
Establish CREATED_BY relationship to UserNode
```

---

## Database Indexes and Performance

### Recommended Indexes (MySQL)

#### User Service
```sql
CREATE INDEX idx_username ON User(username);
```

#### BOM Service
```sql
CREATE INDEX idx_part_creator ON Part(creator);
CREATE INDEX idx_part_stage_status ON Part(stage, status);
CREATE INDEX idx_part_deleted ON Part(deleted);
CREATE INDEX idx_partusage_parent ON PartUsage(parent_id);
CREATE INDEX idx_partusage_child ON PartUsage(child_id);
-- BOM tables removed - migrated to Part-based system
-- CREATE INDEX idx_bomheader_document ON bom_headers(documentId);  -- DEPRECATED
-- CREATE INDEX idx_bomitem_header ON bom_items(header_id);  -- DEPRECATED
```

#### Change Service
```sql
CREATE INDEX idx_change_creator ON `Change`(creator);
CREATE INDEX idx_change_status ON `Change`(status);
CREATE INDEX idx_changepart_change ON ChangePart(changetask_id);
CREATE INDEX idx_changepart_part ON ChangePart(part_id);
CREATE INDEX idx_changedoc_change ON ChangeDocument(changetask_id);
```

#### Document Service
```sql
CREATE INDEX idx_document_master ON Document(master_id);
CREATE INDEX idx_document_creator ON Document(creator);
CREATE INDEX idx_document_status ON Document(status);
CREATE INDEX idx_dochistory_document ON DocumentHistory(documentId);
```

#### Task Service
```sql
CREATE INDEX idx_task_assigned_to ON Task(assigned_to);
CREATE INDEX idx_task_status ON Task(task_status);
CREATE INDEX idx_task_context ON Task(context_type, context_id);
CREATE INDEX idx_tasksignoff_task ON TaskSignoff(task_id);
```

### Neo4j Indexes and Constraints

```cypher
// Create constraints (automatically creates indexes)
CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT part_id IF NOT EXISTS FOR (p:Part) REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT document_id IF NOT EXISTS FOR (d:Document) REQUIRE d.id IS UNIQUE;
CREATE CONSTRAINT change_id IF NOT EXISTS FOR (c:Change) REQUIRE c.id IS UNIQUE;
CREATE CONSTRAINT task_id IF NOT EXISTS FOR (t:Task) REQUIRE t.id IS UNIQUE;

// Additional indexes for common queries
CREATE INDEX user_username IF NOT EXISTS FOR (u:User) ON (u.username);
CREATE INDEX part_creator IF NOT EXISTS FOR (p:Part) ON (p.creator);
CREATE INDEX part_status IF NOT EXISTS FOR (p:Part) ON (p.status);
```

---

## Summary Statistics

### Entity Count by Service

| Service | Entities | Tables/Collections |
|---------|----------|-------------------|
| User Service | 1 | 1 table |
| BOM Service | 3 | 3 tables (Part, PartUsage, DocumentPartLink) |
| Change Service | 3 | 3 tables (change_table, ChangePart, ChangeDocument) |
| Document Service | 3 | 3 tables |
| Task Service | 3 | 3 tables |
| Graph Service | 5 nodes + 1 relationship | Neo4j |
| **Total** | **18 entities** | **13 MySQL tables + Neo4j** |

### Total Fields Managed
- **Relational Databases**: ~120+ fields across all tables
- **Graph Database**: ~50+ properties across all nodes
- **Relationships**: 15+ relationship types in Neo4j

---

## Maintenance and Evolution

### Schema Migration Strategy

1. **Flyway/Liquibase** for MySQL schema versioning
2. **Cypher scripts** for Neo4j schema changes
3. **Backward compatibility** during API changes
4. **Blue-green deployment** for zero-downtime updates

### Data Consistency Checks

Regular consistency checks between MySQL and Neo4j:
- Verify entity counts match
- Validate relationship integrity
- Check for orphaned references
- Audit log comparisons

---

## Conclusion

This PLM system implements a sophisticated data model that:
- ✅ Separates concerns across microservices
- ✅ Maintains data integrity within service boundaries
- ✅ Enables complex relationship queries via Neo4j
- ✅ Supports document versioning and lifecycle management
- ✅ Tracks complete audit history
- ✅ Scales horizontally through service separation
- ✅ Provides eventual consistency through event-driven sync

The hybrid approach (MySQL + Neo4j) provides the best of both worlds: ACID transactions for critical data and powerful graph queries for analytics and relationship discovery.


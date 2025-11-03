# PLM System - Database Schema & ER Diagram

## 📊 Database Overview

The PLM system uses **5 MySQL databases** for microservices:

| Database | Service | Tables | Purpose |
|----------|---------|--------|---------|
| `plm_user_db` | user-service | 1 | User authentication & authorization |
| `plm_bom_db` | bom-service | 3 | Parts, hierarchies, document links |
| `plm_change_db` | change-service | 4 | Engineering change requests |
| `plm_document_db` | document-service | 3 | Document management & versioning |
| `plm_task_db` | task-service | 3 | Tasks, workflows, approvals |

**Total: 14 tables** (after BOM migration)

---

## 🏗️ Entity Relationship Diagram

### User Service (`plm_user_db`)

```
┌─────────────────────────────────┐
│           User                  │
├─────────────────────────────────┤
│ PK  id (BIGINT AUTO_INCREMENT)  │
│     username (UNIQUE)           │
│     password                    │
│     roles (JSON)                │
└─────────────────────────────────┘
```

---

### BOM Service (`plm_bom_db`)

```
                    ┌─────────────────────────────────┐
                    │            Part                 │
                    ├─────────────────────────────────┤
                    │ PK  bigintid                    │
                    │     titlechar                   │
                    │     description                 │
           ┌────────│     stage                       │
           │        │     status                      │
           │        │     level                       │
           │        │     creator                     │
           │        │     create_time                 │
           │        │     updateTime                  │
           │        │     deleted                     │
           │        │     deleteTime                  │
           │        └─────────────────────────────────┘
           │                      │
           │                      │ One-to-Many (parent)
           │                      │
           │        ┌─────────────▼─────────────────┐
           │        │       PartUsage               │
           │        ├───────────────────────────────┤
           │        │ PK  id                        │
           │        │ FK  parent_id → Part          │
           │        │ FK  child_id → Part           │
           │        │     quantity                  │
           │        └───────────────────────────────┘
           │                      │
           │                      │ Many-to-One (child)
           │                      └──────────┐
           │                                 │
           │        ┌────────────────────────▼──────┐
           └────────►  DocumentPartLink             │
                    ├───────────────────────────────┤
                    │ PK  link_id                   │
                    │ FK  part_id → Part            │
                    │     document_id               │
                    └───────────────────────────────┘
```

**Relationships:**
- Part → PartUsage (One-to-Many as parent)
- Part → PartUsage (One-to-Many as child)
- Part → DocumentPartLink (One-to-Many)
- Part creates hierarchical BOM structures through PartUsage

---

### Change Service (`plm_change_db`)

```
                    ┌─────────────────────────────────┐
                    │          Change                 │
                    ├─────────────────────────────────┤
                    │ PK  id                          │
                    │     title                       │
                    │     stage                       │
                    │     class                       │
                    │     product                     │
                    │     status                      │
                    │     creator                     │
                    │     create_time                 │
                    │     change_reason               │
                    │     change_document             │
                    └─────────────────────────────────┘
                              │
                              │ One-to-Many
          ┌───────────────────┴───────────────────┐
          │                                       │
          ▼                                       ▼
┌─────────────────┐                    ┌──────────────────┐
│   ChangePart    │                    │ ChangeDocument   │
├─────────────────┤                    ├──────────────────┤
│ PK  id          │                    │ PK  id           │
│ FK  changetask  │                    │ FK  changetask   │
│     _id         │                    │     _id          │
│     part_id     │                    │     document_id  │
└─────────────────┘                    └──────────────────┘
       │                                       │
       │                                       │
       └───────────────┬───────────────────────┘
                       │
                       │ Links to:
                       ├─► Part (via part_id)
                       └─► Document (via document_id)

Note: change_bom table REMOVED (redundant with ChangePart)
Note: Table names now use PascalCase to match entity class names
```

**Relationships:**
- Change → ChangePart (One-to-Many) → Part
- Change → ChangeDocument (One-to-Many) → Document

**Note:** `change_bom` table removed as it was redundant with `ChangePart`

---

### Document Service (`plm_document_db`)

```
┌─────────────────────────────────┐
│      DocumentMaster             │
├─────────────────────────────────┤
│ PK  id                          │
│     title                       │
│     creator                     │
│     category                    │
│     createTime                  │
└─────────────────────────────────┘
              │
              │ One-to-Many
              │
              ▼
┌─────────────────────────────────┐
│         Document                │
├─────────────────────────────────┤
│ PK  id                          │
│ FK  master_id                   │
│     version                     │
│     revision                    │
│     stage                       │
│     status                      │
│     title                       │
│     description                 │
│     creator                     │
│     createTime                  │
│     fileKey                     │
│     partId (renamed from bomId) │◄──── Links to Part
│     originalFilename            │
│     contentType                 │
│     fileSize                    │
│     storageLocation             │
│     fileUploadedAt              │
│     isActive                    │
└─────────────────────────────────┘
              │
              │ Audit Trail
              │
              ▼
┌─────────────────────────────────┐
│      DocumentHistory            │
├─────────────────────────────────┤
│ PK  id (AUTO_INCREMENT)         │
│     documentId                  │
│     action                      │
│     oldValue                    │
│     newValue                    │
│     created_by                  │
│     comment                     │
│     timestamp                   │
└─────────────────────────────────┘
```

**Relationships:**
- DocumentMaster → Document (One-to-Many versions)
- Document → DocumentHistory (One-to-Many audit logs)
- Document → Part (via partId field)

---

### Task Service (`plm_task_db`)

```
┌─────────────────────────────────┐
│            Task                 │
├─────────────────────────────────┤
│ PK  id                          │
│     task_name                   │
│     task_description            │
│     task_type                   │
│     task_status                 │
│     assigned_to                 │
│     assigned_by                 │
│     due_date                    │
│     created_at                  │
│     updated_at                  │
│     priority                    │
│     parent_task_id              │
│     workflow_id                 │
│     context_type                │
│     context_id                  │
└─────────────────────────────────┘
              │
              │ One-to-Many
      ┌───────┴───────┐
      │               │
      ▼               ▼
┌──────────────┐  ┌──────────────┐
│ TaskSignoff  │  │FileMetadata  │
├──────────────┤  ├──────────────┤
│ PK  id       │  │ PK  id       │
│ FK  task_id  │  │ FK  task_id  │
│ signoff_user │  │ filename     │
│ signoff_     │  │ fileUrl      │
│   action     │  │              │
│ comments     │  │              │
│ signoff_     │  │              │
│   timestamp  │  │              │
│ is_required  │  │              │
└──────────────┘  └──────────────┘
```

**Relationships:**
- Task → TaskSignoff (One-to-Many approvals)
- Task → FileMetadata (One-to-Many attachments)

---

## 🔗 Cross-Service Relationships

These are **logical relationships** maintained through event-driven sync to Neo4j:

```
┌──────────┐     links to      ┌──────────┐
│   Part   │◄─────────────────►│ Document │
└──────────┘   (DocumentPart    └──────────┘
                   Link)              │
     ▲                                │
     │                                │
     │ affects                        │ references
     │                                │
     │                                ▼
┌──────────┐                   ┌──────────┐
│  Change  │◄──────────────────│   Task   │
└──────────┘   creates          └──────────┘
     │
     │ assigned to
     ▼
┌──────────┐
│   User   │
└──────────┘
```

---

## 📋 Table Summary

### plm_user_db
| Table | Records | Purpose |
|-------|---------|---------|
| User | Users | Authentication & authorization |

### plm_bom_db
| Table | Records | Purpose |
|-------|---------|---------|
| Part | Parts/Products | Core part/product entity |
| PartUsage | Relationships | Parent-child with quantity |
| DocumentPartLink | Links | Part-Document associations |

### plm_change_db
| Table | Records | Purpose |
|-------|---------|---------|
| Change | Changes/ECRs | Engineering change requests |
| ChangePart | Links | Change-Part relationships |
| ChangeDocument | Links | Change-Document links |

### plm_document_db
| Table | Records | Purpose |
|-------|---------|---------|
| DocumentMaster | Masters | Version containers |
| Document | Versions | Individual versions/revisions |
| DocumentHistory | Audit logs | Change history |

### plm_task_db
| Table | Records | Purpose |
|-------|---------|---------|
| Task | Tasks | Work items & approvals |
| TaskSignoff | Signoffs | Approval records |
| FileMetadata | Files | Task attachments |

---

## 🔑 Key Changes After Migration

### Updated Fields
1. **change_bom.bom_id** → **change_bom.part_id**
2. **document.bomId** → **document.partId**

### Removed Tables
- ~~bom_headers~~ (replaced by Part)
- ~~bom_items~~ (replaced by PartUsage)

### Benefits
- ✅ Single Part entity for all components
- ✅ True hierarchies via PartUsage
- ✅ Automatic Neo4j synchronization
- ✅ Cleaner data model

---

## 📊 Database Statistics

- **Total Databases**: 5
- **Total Tables**: 13
- **Total Fields**: ~115+
- **Foreign Keys**: 14+
- **Indexes**: 23+

---

## 🔧 Useful Queries

### Get Part with Children
```sql
SELECT p.*, 
       pu.quantity,
       c.titlechar as child_title
FROM Part p
LEFT JOIN PartUsage pu ON p.bigintid = pu.parent_id
LEFT JOIN Part c ON pu.child_id = c.bigintid
WHERE p.bigintid = 'PART-001';
```

### Get Document Versions
```sql
SELECT d.*, dm.title as master_title
FROM Document d
JOIN DocumentMaster dm ON d.master_id = dm.id
WHERE dm.id = 'DOC-MASTER-001'
ORDER BY d.revision DESC, d.version DESC;
```

### Get Changes Affecting Part
```sql
SELECT c.*, cp.part_id
FROM `Change` c
JOIN ChangePart cp ON c.id = cp.changetask_id
WHERE cp.part_id = 'PART-001';
```

---

For the complete SQL schema, see: [MYSQL_SCHEMA_COMPLETE.sql](./MYSQL_SCHEMA_COMPLETE.sql)


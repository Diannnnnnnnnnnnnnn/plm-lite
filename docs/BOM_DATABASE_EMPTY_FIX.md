# BOM Database Empty - Tables Not Created

## Problem
The `plm_bom_db` MySQL database exists but has no tables:
```sql
mysql> use plm_bom_db;
Database changed
mysql> SHOW TABLES;
Empty set (0.00 sec)
```

This causes 404 errors when trying to fetch BOM or Part data.

## Root Cause
The BOM service is configured to use MySQL with Hibernate's `ddl-auto: update`, which should auto-create tables when the service starts. However, the tables were never created, which means either:

1. The BOM service hasn't been properly started yet
2. The service encountered an error during startup
3. The service started before the MySQL database was ready

## Solution: Restart BOM Service

The BOM service needs to be restarted to trigger Hibernate table creation.

### Step 1: Stop the BOM Service

Find and stop the running BOM service process (PID 36220):

**Option A: Windows Task Manager**
1. Open Task Manager (Ctrl+Shift+Esc)
2. Find "java.exe" process with PID 36220
3. Right-click → End Task

**Option B: PowerShell**
```powershell
# Kill the process
Stop-Process -Id 36220 -Force
```

**Option C: If using batch scripts**
```bash
# Stop all services
stop-all-services.bat
```

### Step 2: Start BOM Service

**Option A: Using Maven (Recommended for development)**
```bash
cd bom-service
mvn spring-boot:run
```

**Option B: Using compiled JAR**
```bash
cd bom-service\target
java -jar bom-service-0.0.1-SNAPSHOT.jar
```

**Option C: Using batch script**
```bash
# If you have a startup script
start-all-services.bat
```

### Step 3: Verify Tables Created

Watch the startup logs - you should see Hibernate creating tables:
```
Hibernate: create table bom_header (...)
Hibernate: create table bom_item (...)
Hibernate: create table part (...)
Hibernate: create table part_usage (...)
Hibernate: create table document_part_link (...)
```

Then check MySQL:
```sql
USE plm_bom_db;
SHOW TABLES;
```

Expected output:
```
+----------------------+
| Tables_in_plm_bom_db |
+----------------------+
| bom_header           |
| bom_item             |
| document_part_link   |
| part                 |
| part_usage           |
+----------------------+
```

## Expected Tables

### 1. bom_header
Stores BOM header information:
- `id` (VARCHAR) - Primary key
- `document_id` (VARCHAR) - Associated document
- `description` (TEXT)
- `creator` (VARCHAR)
- `stage` (VARCHAR)
- `status` (VARCHAR)
- `parent_id` (VARCHAR)
- `is_active` (BOOLEAN)
- `create_time` (DATETIME)
- `update_time` (DATETIME)

### 2. bom_item
Stores BOM line items:
- `id` (VARCHAR) - Primary key
- `header_id` (VARCHAR) - Foreign key to bom_header
- `part_number` (VARCHAR)
- `description` (TEXT)
- `quantity` (INT)
- `unit` (VARCHAR)
- `reference` (VARCHAR)

### 3. part
Stores part master data:
- `id` (VARCHAR) - Primary key
- `part_number` (VARCHAR) - Unique
- `description` (TEXT)
- `unit` (VARCHAR)
- `category` (VARCHAR)
- `status` (VARCHAR)
- `stage` (VARCHAR)
- `revision` (VARCHAR)
- `creator` (VARCHAR)
- `is_active` (BOOLEAN)
- `create_time` (DATETIME)
- `update_time` (DATETIME)

### 4. part_usage
Stores part-to-part relationships (BOM structure):
- `id` (VARCHAR) - Primary key
- `parent_part_id` (VARCHAR)
- `child_part_id` (VARCHAR)
- `quantity` (INT)
- `is_active` (BOOLEAN)

### 5. document_part_link
Links parts to documents:
- `link_id` (VARCHAR) - Primary key
- `part_id` (VARCHAR)
- `document_id` (VARCHAR)
- `link_type` (VARCHAR)
- `description` (TEXT)
- `create_time` (DATETIME)

## Alternative: Manual Table Creation

If auto-creation fails, you can create tables manually:

```sql
USE plm_bom_db;

-- Create bom_header table
CREATE TABLE bom_header (
    id VARCHAR(255) PRIMARY KEY,
    document_id VARCHAR(255),
    description TEXT,
    creator VARCHAR(255),
    stage VARCHAR(50),
    status VARCHAR(50),
    parent_id VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_document_id (document_id),
    INDEX idx_creator (creator),
    INDEX idx_status (status)
);

-- Create bom_item table
CREATE TABLE bom_item (
    id VARCHAR(255) PRIMARY KEY,
    header_id VARCHAR(255),
    part_number VARCHAR(255),
    description TEXT,
    quantity INT,
    unit VARCHAR(50),
    reference VARCHAR(255),
    FOREIGN KEY (header_id) REFERENCES bom_header(id) ON DELETE CASCADE,
    INDEX idx_header_id (header_id),
    INDEX idx_part_number (part_number)
);

-- Create part table
CREATE TABLE part (
    id VARCHAR(255) PRIMARY KEY,
    part_number VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    unit VARCHAR(50),
    category VARCHAR(100),
    status VARCHAR(50),
    stage VARCHAR(50),
    revision VARCHAR(50),
    creator VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_part_number (part_number),
    INDEX idx_category (category),
    INDEX idx_status (status)
);

-- Create part_usage table
CREATE TABLE part_usage (
    id VARCHAR(255) PRIMARY KEY,
    parent_part_id VARCHAR(255),
    child_part_id VARCHAR(255),
    quantity INT,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (parent_part_id) REFERENCES part(id),
    FOREIGN KEY (child_part_id) REFERENCES part(id),
    INDEX idx_parent_part (parent_part_id),
    INDEX idx_child_part (child_part_id)
);

-- Create document_part_link table
CREATE TABLE document_part_link (
    link_id VARCHAR(255) PRIMARY KEY,
    part_id VARCHAR(255),
    document_id VARCHAR(255),
    link_type VARCHAR(50),
    description TEXT,
    create_time DATETIME,
    FOREIGN KEY (part_id) REFERENCES part(id),
    INDEX idx_part_id (part_id),
    INDEX idx_document_id (document_id)
);
```

## Troubleshooting

### If tables still don't appear after restart:

1. **Check BOM service logs** for errors:
   - Look for "Error creating bean"
   - Look for "Table creation failed"
   - Look for MySQL connection errors

2. **Verify MySQL permissions:**
   ```sql
   SHOW GRANTS FOR 'plm_user'@'localhost';
   ```
   Should include: `CREATE, ALTER, DROP, INSERT, UPDATE, DELETE, SELECT`

3. **Check Hibernate configuration:**
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: update  # Should be 'update' or 'create'
   ```

4. **Verify MySQL connection:**
   ```bash
   mysql -u plm_user -pplm_password -e "SELECT 1"
   ```

## Why This Matters for Change Management

The empty BOM database is why you're seeing:
- ✅ Changes can be created successfully
- ❌ **404 errors when viewing change details** (if change has BOM/Part)
- ❌ Cannot fetch BOM information for product field
- ❌ Cannot fetch Part information for product field

Once the BOM tables are created and BOMs/Parts exist, the change viewing will work perfectly!

## Quick Fix Summary

```bash
# 1. Stop BOM service
Stop-Process -Id 36220 -Force

# 2. Restart BOM service
cd bom-service
mvn spring-boot:run

# 3. Verify tables created
mysql -u plm_user -pplm_password -e "USE plm_bom_db; SHOW TABLES;"

# 4. You should see 5 tables listed
```

After this, create some BOMs/Parts, then your changes will display properly!










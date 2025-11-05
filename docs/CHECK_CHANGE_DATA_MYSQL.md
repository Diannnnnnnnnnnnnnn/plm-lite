# How to Check Change Data in MySQL

## Option 1: MySQL Command Line

### 1. Connect to MySQL
```bash
# Windows
mysql -u plm_user -p
# Enter password: plm_password
```

### 2. Select the Change Database
```sql
USE plm_change_db;
```

### 3. View All Changes
```sql
SELECT * FROM change_table;
```

### 4. View Specific Change Details
```sql
-- View the most recent change
SELECT * FROM change_table 
ORDER BY create_time DESC 
LIMIT 1;

-- View change by ID
SELECT * FROM change_table 
WHERE id = 'your-change-id-here';
```

### 5. View Related Data

**View Change-BOM Relationships:**
```sql
SELECT * FROM change_bom;
```

**View Change-Part Relationships:**
```sql
SELECT * FROM change_part;
```

**View Change with all details (joined):**
```sql
SELECT 
    c.*,
    GROUP_CONCAT(DISTINCT cb.bom_id) as bom_ids,
    GROUP_CONCAT(DISTINCT cp.part_id) as part_ids
FROM change_table c
LEFT JOIN change_bom cb ON c.id = cb.change_id
LEFT JOIN change_part cp ON c.id = cp.change_id
GROUP BY c.id
ORDER BY c.create_time DESC;
```

## Option 2: MySQL Workbench (GUI)

### 1. Open MySQL Workbench
- Launch MySQL Workbench application

### 2. Connect to Database
- Host: `localhost`
- Port: `3306`
- Username: `plm_user`
- Password: `plm_password`

### 3. Navigate
1. In the left panel, expand "Schemas"
2. Find and expand `plm_change_db`
3. Expand "Tables"
4. Right-click on `change_table` → "Select Rows - Limit 1000"

### 4. View Data
You'll see all the change records with columns:
- `id` - UUID of the change
- `title` - Change title
- `stage` - Design stage (CONCEPTUAL_DESIGN, etc.)
- `class` - Change class (Minor, Major, etc.)
- `product` - Product/BOM ID
- `status` - IN_WORK, IN_REVIEW, or RELEASED
- `creator` - Username who created it
- `create_time` - Timestamp
- `change_reason` - Reason for the change
- `change_document` - Associated document ID

## Option 3: DBeaver (Free Alternative)

### 1. Download DBeaver
- Download from https://dbeaver.io/

### 2. Create Connection
- Database: MySQL
- Host: localhost
- Port: 3306
- Database: plm_change_db
- Username: plm_user
- Password: plm_password

### 3. View Data
- Navigate to plm_change_db → Tables → change_table
- Right-click → View Data

## Quick Check Script

Save this as `check_changes.sql` and run it:

```sql
USE plm_change_db;

-- Show database info
SELECT 
    'Total Changes' as metric, 
    COUNT(*) as count 
FROM change_table

UNION ALL

SELECT 
    'Changes by Status',
    CONCAT(status, ': ', COUNT(*))
FROM change_table
GROUP BY status;

-- Show recent changes
SELECT 
    id,
    title,
    stage,
    class as change_class,
    status,
    creator,
    create_time,
    change_reason,
    change_document
FROM change_table
ORDER BY create_time DESC
LIMIT 5;

-- Show changes with relationships
SELECT 
    c.id,
    c.title,
    c.status,
    c.creator,
    c.create_time,
    COUNT(DISTINCT cb.bom_id) as bom_count,
    COUNT(DISTINCT cp.part_id) as part_count
FROM change_table c
LEFT JOIN change_bom cb ON c.id = cb.change_id
LEFT JOIN change_part cp ON c.id = cp.change_id
GROUP BY c.id, c.title, c.status, c.creator, c.create_time
ORDER BY c.create_time DESC
LIMIT 10;
```

Run it with:
```bash
mysql -u plm_user -p plm_change_db < check_changes.sql
```

## Verify the Latest Change

To verify the change you just created:

```sql
USE plm_change_db;

-- Get the most recent change
SELECT 
    id,
    title,
    stage,
    class as change_class,
    product,
    status,
    creator,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as created_at,
    change_reason,
    change_document
FROM change_table
ORDER BY create_time DESC
LIMIT 1;
```

Expected output should show:
- **title**: The title you entered
- **status**: `IN_WORK`
- **creator**: Your username (e.g., `labubu`)
- **created_at**: Just now
- **change_document**: The document ID you selected

## Common Queries

### Count changes by creator
```sql
SELECT 
    creator,
    COUNT(*) as change_count
FROM change_table
GROUP BY creator
ORDER BY change_count DESC;
```

### Find changes for a specific product
```sql
SELECT * FROM change_table
WHERE product = 'your-product-id'
ORDER BY create_time DESC;
```

### Find changes affecting a specific document
```sql
SELECT * FROM change_table
WHERE change_document = 'your-document-id'
ORDER BY create_time DESC;
```

## Troubleshooting

### If you get "Access Denied"
The credentials might be wrong. Check your configuration in:
- `change-service/src/main/resources/application.yml`

### If database doesn't exist
Run the initialization script:
```bash
mysql -u root -p < init-mysql-databases.sql
```

### If table doesn't exist
The table should be auto-created by Hibernate. Check the change-service startup logs for any errors.










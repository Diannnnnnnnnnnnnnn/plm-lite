# Switch All Services from H2 to MySQL

## Problem
All services (BOM, User, Task) are currently using H2 databases (in-memory or file-based) instead of MySQL, causing:
- ❌ MySQL databases are empty (no tables)
- ✅ Data exists but in H2 databases
- ⚠️ Data may be lost on service restart

## Current Status

### BOM Service (port 8089)
- **Current:** H2 in-memory (`jdbc:h2:mem:bomdb`)
- **Data:** Parts exist (2 parts found)
- **MySQL:** `plm_bom_db` is empty

### User Service (port 8083)
- **Current:** H2 file (`./data/userdb.mv.db`)
- **Data:** Users exist
- **MySQL:** `plm_user_db` is empty

### Task Service (port 8082)
- **Current:** H2 file (`./data/taskdb.mv.db`)
- **Data:** Tasks exist
- **MySQL:** `plm_task_db` is empty

### Change Service (port 8084)
- **Current:** MySQL `plm_change_db` ✅
- **Status:** Already using MySQL correctly

### Document Service (port 8081)
- **Current:** H2 file (`./data/documentdb.mv.db`)
- **Data:** Documents exist
- **MySQL:** `plm_document_db` probably empty

## Solution Steps

### Step 1: Stop All Services

```powershell
# Stop all Java processes
Get-Process java | Stop-Process -Force

# Or use your stop script if you have one
.\stop-all-services.bat
```

### Step 2: Backup H2 Data (Optional)

If you want to keep your existing data temporarily:

```powershell
# Create backup directory
New-Item -ItemType Directory -Force -Path backup\h2_databases

# Copy H2 files
Copy-Item -Recurse data backup\h2_databases\
Copy-Item user-service\data\*.db backup\h2_databases\user-service\ -Force
Copy-Item task-service\data\*.db backup\h2_databases\task-service\ -Force
Copy-Item document-service\data\*.db backup\h2_databases\document-service\ -Force
```

### Step 3: Clean and Rebuild Services

```powershell
# Clean BOM service (already done)
cd bom-service
mvn clean install -DskipTests
cd ..

# Clean User service
cd user-service
mvn clean install -DskipTests
cd ..

# Clean Task service
cd task-service
mvn clean install -DskipTests
cd ..

# Clean Document service
cd document-service
mvn clean install -DskipTests
cd ..
```

### Step 4: Verify MySQL Databases Exist

```sql
-- Connect to MySQL
mysql -u plm_user -pplm_password

-- Check databases
SHOW DATABASES LIKE 'plm%';

-- Expected output:
-- plm_bom_db
-- plm_change_db
-- plm_document_db
-- plm_task_db
-- plm_user_db
```

If any are missing:
```sql
CREATE DATABASE IF NOT EXISTS plm_bom_db;
CREATE DATABASE IF NOT EXISTS plm_user_db;
CREATE DATABASE IF NOT EXISTS plm_task_db;
CREATE DATABASE IF NOT EXISTS plm_document_db;
GRANT ALL PRIVILEGES ON plm_bom_db.* TO 'plm_user'@'localhost';
GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'localhost';
GRANT ALL PRIVILEGES ON plm_task_db.* TO 'plm_user'@'localhost';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 5: Start Services with MySQL Profile

Start each service ensuring they use the `default` profile (MySQL):

```powershell
# Start User Service
cd user-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"
cd ..

# Wait 30 seconds for tables to be created

# Start Task Service
cd task-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"
cd ..

# Wait 30 seconds

# Start Document Service  
cd document-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"
cd ..

# Wait 30 seconds

# Start BOM Service
cd bom-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"
cd ..

# Wait 30 seconds

# Start Change Service (already using MySQL)
cd change-service
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run -Dspring-boot.run.profiles=default"
cd ..
```

Or use your startup script if configured for MySQL.

### Step 6: Verify MySQL Tables Created

```sql
-- Check User Service tables
USE plm_user_db;
SHOW TABLES;
-- Expected: users, user_roles, etc.

-- Check Task Service tables
USE plm_task_db;
SHOW TABLES;
-- Expected: task, assignments, etc.

-- Check BOM Service tables
USE plm_bom_db;
SHOW TABLES;
-- Expected: part, bom_header, bom_item, part_usage, document_part_link

-- Check Document Service tables
USE plm_document_db;
SHOW TABLES;
-- Expected: document, document_revision, etc.

-- Check Change Service tables (should already exist)
USE plm_change_db;
SHOW TABLES;
-- Expected: change_table, change_bom, change_part
```

### Step 7: Recreate Your Data

Since the data was in H2, you'll need to recreate it in MySQL:

**For Users:** Create users again through the UI or API
**For Documents:** Upload documents again
**For Parts/BOMs:** Create parts/BOMs again
**For Tasks:** Tasks will be created as needed
**For Changes:** Your changes are already in MySQL! ✅

## Quick Start Script

Save this as `start-all-mysql.ps1`:

```powershell
Write-Host "Starting all services with MySQL..." -ForegroundColor Cyan

$services = @(
    "user-service",
    "task-service", 
    "document-service",
    "bom-service",
    "change-service"
)

foreach ($service in $services) {
    Write-Host "`nStarting $service..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $service; mvn spring-boot:run"
    Start-Sleep -Seconds 35
}

Write-Host "`nAll services started! Check each window for startup status." -ForegroundColor Green
```

## Verification

After starting all services, verify they're using MySQL:

```powershell
# Check if all services are running
netstat -ano | findstr "8081 8082 8083 8084 8089"

# Test each service
Invoke-RestMethod -Uri "http://localhost:8083/users" | ConvertTo-Json  # User Service
Invoke-RestMethod -Uri "http://localhost:8082/tasks" | ConvertTo-Json   # Task Service
Invoke-RestMethod -Uri "http://localhost:8089/parts" | ConvertTo-Json   # BOM Service
Invoke-RestMethod -Uri "http://localhost:8084/api/changes" | ConvertTo-Json  # Change Service
```

## Why This Happened

Spring Boot reads configuration files in this order (higher priority first):
1. Command-line arguments
2. `application.properties`
3. `application.yml`

The BOM service had an `application.properties` file in the compiled `target/classes/` that specified H2, overriding the MySQL configuration in `application.yml`.

## Future Prevention

1. Always check `target/classes/` for unexpected configuration files
2. Use Maven clean before rebuilding: `mvn clean install`
3. Verify which database is being used in startup logs
4. Check for this line in logs: `HikariPool-1 - Starting...` and see the JDBC URL

## Status After Migration

- ✅ All data in MySQL (persistent)
- ✅ Tables auto-created by Hibernate
- ✅ No data loss on service restart
- ✅ Consistent database across all services
- ✅ Ready for production

**Current Action:** Rebuild and restart services to use MySQL!

























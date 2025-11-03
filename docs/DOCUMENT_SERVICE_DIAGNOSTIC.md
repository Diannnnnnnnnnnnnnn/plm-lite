# Document Service Diagnostic Guide

## Current Status: 503 Service Unavailable

This means the service IS running but one or more dependencies are unhealthy.

---

## Step-by-Step Diagnostic Checklist

### Step 1: Check the Document Service Console Window

**Please look at the Document Service PowerShell window and find error messages.**

Look for these specific error patterns:

#### ❌ **Database Connection Errors:**

```
Failed to configure a DataSource: 'url' attribute is not specified
Cannot create PoolableConnectionFactory
Communications link failure
Access denied for user 'plm_user'@'localhost'
Unknown database 'plm_document_db'
Connection refused: connect
Could not connect to address=(host=localhost)(port=3306)
```

#### ❌ **Elasticsearch Errors:**

```
Failed to connect to Elasticsearch
Connection refused: localhost:9200
NoNodeAvailableException
```

#### ❌ **MinIO Errors:**

```
Connection refused: localhost:9000
MinIO is not available
```

---

### Step 2: Quick Dependency Check

Run these commands to check if dependencies are running:

```powershell
# Check if MySQL is running
netstat -ano | findstr ":3306"

# Check if Elasticsearch is running (we know this works)
curl http://localhost:9200

# Check if MinIO is running
netstat -ano | findstr ":9000"
```

**Expected Results:**
- MySQL (3306): Should show LISTENING
- Elasticsearch (9200): Should return cluster info
- MinIO (9000): Should show LISTENING

---

### Step 3: Common Issues & Solutions

#### Issue 1: MySQL Not Running

**Symptom:**
```
Connection refused: connect
Communications link failure
```

**Solutions:**

**Option A: Start MySQL**
```powershell
# If using Docker
docker ps | findstr mysql

# If not running, start it
docker start mysql
# or
net start MySQL80  # Windows service
```

**Option B: Switch to H2 (Fastest Fix)**
```powershell
# In Document Service window: Ctrl+C
# Then run:
cd document-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This uses H2 embedded database (no MySQL needed).

---

#### Issue 2: Wrong MySQL Credentials

**Symptom:**
```
Access denied for user 'plm_user'@'localhost'
```

**Solution:**

Edit `document-service/src/main/resources/application.properties`:

```properties
# Check these lines (15-17):
spring.datasource.username=plm_user      # Change if needed
spring.datasource.password=plm_password  # Change if needed
```

Or create the MySQL user:
```sql
CREATE USER 'plm_user'@'localhost' IDENTIFIED BY 'plm_password';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'localhost';
FLUSH PRIVILEGES;
```

---

#### Issue 3: Database Doesn't Exist

**Symptom:**
```
Unknown database 'plm_document_db'
```

**Solution:**

The URL has `createDatabaseIfNotExist=true` so it should auto-create.

If it doesn't work, manually create:
```sql
CREATE DATABASE plm_document_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

#### Issue 4: MinIO Not Running

**Symptom:**
```
Connection refused: localhost:9000
MinIO unavailable
```

**Solutions:**

**Option A: Disable MinIO** (if not needed for testing)

Edit `document-service/src/main/resources/application.properties`:
```properties
# Comment out or remove MinIO config
#minio.url=http://localhost:9000
#minio.access-key=minio
#minio.secret-key=password
```

**Option B: Start MinIO**
```powershell
docker run -d -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"
```

---

### Step 4: Health Check Breakdown

If you can access: `http://localhost:8081/actuator/health`

It will show which component is failing:

```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    },
    "elasticsearch": {
      "status": "UP"
    }
  }
}
```

This tells you exactly which dependency is failing.

---

## Recommended Quick Fix Path

### Path 1: Use H2 Database (Fastest - No MySQL needed)

```powershell
# 1. Stop Document Service (Ctrl+C in its window)

# 2. Start with dev profile
cd document-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Wait for "Started DocumentServiceApplication"

# 4. Test
curl http://localhost:8081/actuator/health
```

**Pros:**
- ✅ No MySQL setup needed
- ✅ Works immediately
- ✅ Good for testing ES integration

**Cons:**
- ⚠️ Data stored in file (not production setup)
- ⚠️ Need MySQL eventually for production

---

### Path 2: Fix MySQL Connection

```powershell
# 1. Check if MySQL is installed and running
Get-Service MySQL* | Select-Object Name, Status

# 2. If not running, start it
net start MySQL80

# 3. Test connection
mysql -u plm_user -pplm_password

# 4. Create database if needed
mysql -u root -p
CREATE DATABASE plm_document_db;
CREATE USER 'plm_user'@'localhost' IDENTIFIED BY 'plm_password';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'localhost';

# 5. Restart Document Service
```

---

## What to Share With Me

Please copy and paste from the **Document Service console window**:

1. **Any RED error messages**
2. **The last 20-30 lines** before the service says "Started" (if it does)
3. **Any lines containing:**
   - "Failed"
   - "Connection refused"
   - "Access denied"
   - "Unknown database"
   - "Exception"

---

## Quick Test After Fix

Once Document Service starts successfully:

```powershell
# 1. Check health
curl http://localhost:8081/actuator/health

# 2. Try to create a document
$docData = @{
    title = "Test Document"
    type = "Technical"
    description = "Test"
    stage = "DEVELOPMENT"
    status = "IN_WORK"
    creator = "test-user"
    version = "1"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents" `
    -Method Post -Body $docData -ContentType "application/json"

# 3. Re-run comprehensive test
cd C:\Users\diang\Desktop\plm-lite
powershell -File scripts/comprehensive-es-integration-test.ps1
```

---

## Expected After Fix

✅ Document Service health: UP  
✅ Can create documents  
✅ Documents auto-indexed to ES  
✅ Test score: 95%+ (57+/60 tests)

---

**What errors do you see in the Document Service console window?**

Share them with me and I'll help you fix them immediately!




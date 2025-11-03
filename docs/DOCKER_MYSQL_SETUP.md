# Docker MySQL Setup Guide

## Problem
If you see this error:
```
'mysql' is not recognized as an internal or external command
```

This means the MySQL command-line client is not installed locally. Since you're using Docker for MySQL, use the Docker-specific scripts instead.

## Quick Solution

### Step 1: Ensure MySQL Container is Running

**Option A - Start new MySQL container:**
```bash
start-mysql-docker.bat
```

**Option B - Manual Docker command:**
```bash
docker run -d -p 3306:3306 --name mysql-plm -e MYSQL_ROOT_PASSWORD=root mysql:8.0
```

**Check if running:**
```bash
docker ps
```

You should see a container named `mysql-plm` running on port 3306.

### Step 2: Initialize Databases (Docker Version)

**Use the Docker-specific script:**
```bash
init-mysql-databases-docker.bat
```

**Or PowerShell:**
```powershell
.\init-mysql-databases-docker.ps1
```

**Prompts you'll see:**
- MySQL root password: `root` (press Enter for default)
- Container name: `mysql-plm` (press Enter for default)

### Step 3: Verify Databases Created

```bash
docker exec -it mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"
```

You should see:
```
plm_user_db
plm_bom_db
plm_document_db
plm_task_db
plm_change_db
```

### Step 4: Start PLM Services

```bash
start-all-services.bat
```

## Alternative: Manual Database Initialization

If the scripts don't work, you can initialize manually:

```bash
# Copy SQL file into container and execute
docker exec -i mysql-plm mysql -uroot -proot < init-mysql-databases.sql

# Or execute commands directly
docker exec -it mysql-plm mysql -uroot -proot
```

Then paste these commands:
```sql
CREATE DATABASE IF NOT EXISTS plm_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_bom_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_document_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_task_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_change_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'plm_user'@'%' IDENTIFIED BY 'plm_password';

GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_bom_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_task_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_change_db.* TO 'plm_user'@'%';

FLUSH PRIVILEGES;
```

## Docker MySQL Management

### Useful Commands

**Start MySQL container:**
```bash
docker start mysql-plm
```

**Stop MySQL container:**
```bash
docker stop mysql-plm
```

**View MySQL logs:**
```bash
docker logs mysql-plm
```

**Access MySQL shell:**
```bash
docker exec -it mysql-plm mysql -uroot -proot
```

**Connect with plm_user:**
```bash
docker exec -it mysql-plm mysql -uplm_user -pplm_password
```

**Check databases:**
```bash
docker exec -it mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"
```

**Check tables in a database:**
```bash
docker exec -it mysql-plm mysql -uroot -proot -e "USE plm_user_db; SHOW TABLES;"
```

**Remove container (⚠️ deletes all data):**
```bash
docker rm -f mysql-plm
```

## Troubleshooting

### Container not found

**Problem:**
```
Error: No such container: mysql-plm
```

**Solution:**
```bash
# Check running containers
docker ps

# Check all containers
docker ps -a

# If it exists but stopped, start it
docker start mysql-plm

# If it doesn't exist, create it
start-mysql-docker.bat
```

### Port already in use

**Problem:**
```
Error: port is already allocated
```

**Solution:**
```bash
# Check what's using port 3306
netstat -ano | findstr :3306

# Stop the process or use different port
docker run -d -p 3307:3306 --name mysql-plm-alt -e MYSQL_ROOT_PASSWORD=root mysql:8.0
```

Then update service configs to use port 3307.

### Access denied

**Problem:**
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost'
```

**Solution:**
Check the root password. Default in our setup is `root`.

```bash
docker exec -it mysql-plm mysql -uroot -proot
```

### Databases not showing

**Problem:**
Databases don't appear after running init script.

**Solution:**
```bash
# Re-run initialization
init-mysql-databases-docker.bat

# Or manually check
docker exec -it mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"
```

## Script Comparison

| Script | Use When |
|--------|----------|
| `init-mysql-databases.bat` | MySQL installed locally (not Docker) |
| `init-mysql-databases-docker.bat` | MySQL running in Docker ✅ |
| `init-mysql-databases-docker.ps1` | Docker + PowerShell preference |

## Full Workflow

```bash
# 1. Start MySQL container
start-mysql-docker.bat

# 2. Initialize databases
init-mysql-databases-docker.bat

# 3. Verify setup
docker exec -it mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"

# 4. Start PLM services
start-all-services.bat

# 5. Test connection
curl http://localhost:8083/users
```

## Connection Details

**From Docker container:**
- Host: `localhost` or `127.0.0.1`
- Port: `3306`
- Root password: `root`
- User: `plm_user`
- Password: `plm_password`

**From Spring Boot services:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plm_user_db
    username: plm_user
    password: plm_password
```

## Next Steps

After successful initialization:

1. ✅ MySQL container running
2. ✅ Databases created
3. ✅ User created with permissions
4. ➡️ Start services: `start-all-services.bat`
5. ➡️ Verify tables created by Hibernate
6. ➡️ Test API endpoints

Good luck! 🚀






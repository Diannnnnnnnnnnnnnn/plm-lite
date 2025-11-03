# Production Data Cleanup Guide

## Overview

This guide provides comprehensive instructions for cleaning all data from your PLM system before deploying to production. This includes data from MySQL, MinIO, Elasticsearch, and Neo4j.

## ⚠️ WARNING

**This process will permanently delete ALL data from your system.** This action cannot be undone. Make sure you have:
- ✅ Backed up any important data
- ✅ Confirmed this is what you want to do
- ✅ Reviewed all production configuration files

## Quick Start

### Option 1: Clean Everything (Recommended)

Run the master cleanup script that handles all services:

**Windows (CMD):**
```cmd
cleanup-all-data.bat
```

**Windows (PowerShell):**
```powershell
.\cleanup-all-data.ps1
```

This script will:
1. Stop all running services
2. Clean MySQL databases
3. Clean MinIO object storage
4. Clean Elasticsearch indices
5. Clean Neo4j graph database
6. Clean local file system data (temp uploads, local databases)

### Option 2: Clean Individual Services

You can also clean each service separately if needed:

#### MySQL Only
```cmd
cleanup-mysql-data.bat          # Windows CMD
.\cleanup-mysql-data.ps1         # PowerShell
```

#### MinIO Only
```cmd
cleanup-minio-data.bat          # Windows CMD
.\cleanup-minio-data.ps1         # PowerShell
```

#### Elasticsearch Only
```cmd
cleanup-elasticsearch-data.bat  # Windows CMD
.\cleanup-elasticsearch-data.ps1 # PowerShell
```

#### Neo4j Only
```cmd
cleanup-neo4j-data.bat          # Windows CMD
.\cleanup-neo4j-data.ps1         # PowerShell
```

#### Local Files Only
```cmd
cleanup-local-files.bat         # Windows CMD
.\cleanup-local-files.ps1        # PowerShell
```

## What Gets Cleaned

### MySQL Databases
The following databases will be **dropped completely**:
- `plm_auth` - Authentication and authorization data
- `plm_parts` - Part master data
- `plm_bom` - Bill of Materials data
- `plm_documents` - Document metadata
- `plm_changes` - Change management records
- `plm_tasks` - Task management data
- `plm_workflows` - Workflow execution data
- `plm_users` - User profile data

### MinIO Object Storage
- All files in the `plm-documents` bucket will be deleted
- This includes all uploaded documents, CAD files, images, etc.
- The bucket itself remains but will be empty

### Elasticsearch Indices
The following indices will be **deleted completely**:
- `parts` - Part search index
- `documents` - Document search index
- `changes` - Change request search index
- `tasks` - Task search index
- `boms` - BOM search index

These will be recreated automatically when services restart.

### Neo4j Graph Database
- All nodes (Parts, Documents, Changes, Tasks, Users, etc.)
- All relationships between nodes
- The entire graph structure will be empty

### Local File System Data
- `data/taskdb.mv.db` - Task service embedded H2 database
- `document-service/data/documentdb.mv.db` - Document service embedded database
- `document-service/temp-uploads/*` - All temporary uploaded files (128+ test files)

## Prerequisites

Before running the cleanup scripts, ensure:

1. **Docker is running**
   ```cmd
   docker ps
   ```

2. **You have stopped all PLM services** (scripts will do this automatically)
   ```cmd
   stop-all-services.bat
   ```

3. **Infrastructure services are accessible**
   - MySQL container: `mysql-plm`
   - MinIO container: `plm-minio`
   - Elasticsearch container: `plm-elasticsearch`
   - Neo4j container: `plm-neo4j`

## Step-by-Step Cleanup Process

### Step 1: Stop All Services

```cmd
stop-all-services.bat
```

Wait for all services to shut down completely (about 30 seconds).

### Step 2: Run Cleanup

```cmd
cleanup-all-data.bat
```

You will be prompted to confirm by typing `YES`.

### Step 3: Verify Cleanup

#### Verify MySQL
```cmd
docker exec mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"
```
You should only see system databases (mysql, information_schema, performance_schema, sys).

#### Verify MinIO
Access MinIO Console at http://localhost:9001
- Username: `minio`
- Password: `password`
- Check that `plm-documents` bucket is empty

#### Verify Elasticsearch
```cmd
curl http://localhost:9200/_cat/indices?v
```
Should show no PLM-related indices.

#### Verify Neo4j
Access Neo4j Browser at http://localhost:7474
- Username: `neo4j`
- Password: `password`
- Run: `MATCH (n) RETURN count(n)`
- Should return 0

#### Verify Local Files
```cmd
# Check if local database files are deleted
dir data\taskdb.mv.db
dir document-service\data\documentdb.mv.db

# Check if temp uploads are empty
dir document-service\temp-uploads
```
All files should be missing or directory should be empty.

## Post-Cleanup: Production Setup

### Step 1: Update Production Configuration

Review and update these configuration files for production:

#### Database Passwords
- `auth-service/src/main/resources/application.yml`
- `bom-service/src/main/resources/application.yml`
- `change-service/src/main/resources/application.yml`
- `document-service/src/main/resources/application.properties`
- `task-service/src/main/resources/application.yml`
- `user-service/src/main/resources/application.properties`

**Change default passwords:**
- MySQL: `root` → strong password
- MinIO: `minio/password` → strong credentials
- Neo4j: `neo4j/password` → strong password
- Redis: `plm_redis_password` → strong password

#### Application Settings
- Set `spring.profiles.active=prod` in all services
- Update CORS settings for production frontend URL
- Configure proper logging levels (INFO or WARN)
- Set up proper file size limits
- Configure rate limiting if needed

### Step 2: Start Infrastructure Services

```cmd
# Start MySQL
start-mysql-docker.bat

# Start infrastructure (MinIO, Neo4j, Redis)
docker-compose -f infra/docker-compose-infrastructure.yaml up -d

# Start Elasticsearch
docker-compose -f docker-compose-elasticsearch.yml up -d
```

Wait for all services to be healthy:
```cmd
docker ps
```

### Step 3: Initialize Databases

```cmd
init-mysql-databases-docker.bat
```

This creates empty databases with proper schemas.

### Step 4: Start Application Services

```cmd
start-all-services.bat
```

Monitor logs to ensure all services start successfully.

### Step 5: Create Initial Admin User

Use the authentication service API to create the first admin user:

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "your-secure-password",
    "email": "admin@yourcompany.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

## Troubleshooting

### MySQL Container Not Found
```cmd
start-mysql-docker.bat
```

### MinIO/Neo4j Not Running
```cmd
docker-compose -f infra/docker-compose-infrastructure.yaml up -d
```

### Elasticsearch Not Running
```cmd
docker-compose -f docker-compose-elasticsearch.yml up -d
```

### Permission Denied Errors
Run PowerShell as Administrator:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Network Errors
Ensure Docker networks exist:
```cmd
docker network ls | findstr plm
```

If missing:
```cmd
docker network create plm-network
```

## Security Checklist for Production

- [ ] Changed all default passwords (MySQL, MinIO, Neo4j, Redis)
- [ ] Updated CORS configuration for production domain
- [ ] Set up HTTPS/SSL certificates
- [ ] Configured proper firewall rules
- [ ] Set up database backups
- [ ] Configured log rotation
- [ ] Set up monitoring and alerting
- [ ] Reviewed and hardened security settings
- [ ] Set up proper authentication (OAuth2, SSO, etc.)
- [ ] Configured rate limiting
- [ ] Set up proper error handling (no stack traces in production)
- [ ] Reviewed and minimized exposed ports
- [ ] Set up proper session management
- [ ] Configured HTTPS for MinIO and Neo4j consoles

## Backup Strategy

Before going to production, set up regular backups:

### MySQL Backup
```cmd
docker exec mysql-plm mysqldump -uroot -proot --all-databases > backup.sql
```

### MinIO Backup
Use MinIO's built-in replication or backup to S3-compatible storage.

### Neo4j Backup
```cmd
docker exec plm-neo4j neo4j-admin dump --database=neo4j --to=/data/backup.dump
```

### Elasticsearch Backup
Configure snapshot repository and automated snapshots.

## Additional Resources

- [MySQL Docker Documentation](https://hub.docker.com/_/mysql)
- [MinIO Documentation](https://min.io/docs/minio/linux/index.html)
- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Neo4j Documentation](https://neo4j.com/docs/)

## Support

If you encounter issues:
1. Check Docker container logs: `docker logs <container-name>`
2. Verify all containers are healthy: `docker ps`
3. Check network connectivity between containers
4. Review application logs in each service

---

**Last Updated:** November 2, 2025
**Version:** 1.0


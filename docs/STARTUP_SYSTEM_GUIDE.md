# PLM Lite - Startup System Guide

This guide explains the new intelligent startup system that makes it easy to start and manage your PLM application.

## Overview

The new startup system includes:

1. **Master Docker Compose** - All infrastructure in one file
2. **Intelligent Startup Scripts** - Automatically detect what's running and start only what's needed
3. **Status Checker** - Quick health check for all services
4. **Better Management** - Easier to start, stop, and monitor the system

## Quick Start

### Option 1: PowerShell (Recommended)
```powershell
.\start-plm-system.ps1
```

### Option 2: Batch File
```batch
start-plm-system.bat
```

### Check System Status
```powershell
.\check-system-status.ps1
```

## What Gets Started

### 1. Docker Infrastructure (Automatic)
- **Elasticsearch** (9200, 9300) - Search engine for documents, parts, tasks
- **Kibana** (5601) - Elasticsearch UI and management
- **Zeebe** (26500, 9600, 8088) - Camunda workflow engine
- **Operate** (8181) - Camunda Operate UI for workflow monitoring
- **Tasklist** (8182) - Camunda Tasklist UI for human tasks
- **Connectors** (8085) - Camunda connectors runtime
- **MinIO** (9000, 9001) - S3-compatible file storage
- **Redis** (6379) - Caching layer
- **MySQL** (3306) - Primary relational database
- **Neo4j** (7474, 7687) - Graph database for relationships

### 2. Backend Services (Automatic)
Services start in dependency order:

1. **Graph Service** (8090) - Neo4j graph operations
2. **Workflow Orchestrator** (8086) - Zeebe workflow management
3. **User Service** (8083) - User management and authentication
4. **Task Service** (8082) - Task management with Elasticsearch
5. **Document Service** (8081) - Document management with Elasticsearch
6. **BOM Service** (8089) - Bill of Materials management
7. **Change Service** (8084) - Change request management
8. **Search Service** (8091) - Unified search across all entities

### 3. Frontend (Automatic)
- **React Application** (3000) - Main user interface

## Key Features

### Intelligent State Detection

The startup script automatically:
- ✅ Checks if Docker is running
- ✅ Detects which containers are already running
- ✅ Detects which services are already running
- ✅ Only starts what's needed
- ✅ Waits for services to be healthy before proceeding

### Health Checking

Each service is health-checked:
- Docker containers use built-in health checks
- Backend services checked via actuator endpoints
- Frontend checked via port availability

### Proper Startup Order

Services start in the correct dependency order:
1. Infrastructure (Elasticsearch, Zeebe, databases)
2. Core services (Graph, Workflow, User)
3. Application services (Task, Document, BOM, Change)
4. Search service
5. Frontend

## Files and Their Purpose

### docker-compose-master.yml
**Purpose**: Single Docker Compose file for all infrastructure

**Contains**:
- All Elasticsearch stack components
- Complete Camunda Platform 8.7
- Storage services (MinIO, Redis)
- Databases (MySQL, Neo4j)
- Proper networking and health checks
- Volume management for data persistence

**Usage**:
```bash
# Start all infrastructure
docker-compose -f docker-compose-master.yml up -d

# Stop all infrastructure
docker-compose -f docker-compose-master.yml down

# View logs
docker-compose -f docker-compose-master.yml logs -f

# View specific service logs
docker-compose -f docker-compose-master.yml logs -f elasticsearch
```

### start-plm-system.bat / start-plm-system.ps1
**Purpose**: Intelligent startup script for the entire system

**Features**:
- Checks Docker status
- Starts infrastructure if needed
- Health checks all containers
- Starts backend services in order
- Starts frontend
- Provides comprehensive status output

**When to use**: Starting the system from scratch or after a reboot

### check-system-status.ps1
**Purpose**: Quick status check for all components

**Shows**:
- Docker engine status
- All container statuses with health info
- All backend service statuses
- Frontend status
- Summary statistics
- Quick action commands

**When to use**: Checking what's running, debugging startup issues

## Common Scenarios

### First Time Setup

1. Start the system:
   ```powershell
   .\start-plm-system.ps1
   ```

2. Wait for all services to start (~5-10 minutes)

3. Initialize databases:
   ```batch
   .\init-mysql-databases-docker.bat
   .\reindex-all-elasticsearch.bat
   ```

4. Access the application:
   - Frontend: http://localhost:3000
   - Operate: http://localhost:8181
   - Kibana: http://localhost:5601

### Daily Development

If you shut down your computer each night:

1. Start the system:
   ```powershell
   .\start-plm-system.ps1
   ```

2. The script will:
   - Start Docker containers that were stopped
   - Start any backend services that aren't running
   - Skip anything that's already running

3. Begin developing!

### After System Reboot

Everything stopped after reboot:

1. Just run:
   ```powershell
   .\start-plm-system.ps1
   ```

2. Wait ~10 minutes for full startup

3. Everything will be running

### Partial Startup

Some containers running but services stopped:

1. Run:
   ```powershell
   .\start-plm-system.ps1
   ```

2. Script will:
   - Detect running containers
   - Skip infrastructure startup
   - Start only stopped services

### Checking Status

Anytime you want to see what's running:

```powershell
.\check-system-status.ps1
```

Output shows:
- ✓ Green = Running and healthy
- ⚠ Yellow = Running but not healthy
- ✗ Red = Not running

## Management Commands

### Start Everything
```powershell
.\start-plm-system.ps1
```

### Check Status
```powershell
.\check-system-status.ps1
```

### View Docker Containers
```bash
docker ps
```

### Stop Backend Services Only
```batch
stop-all-services.bat
```

### Stop Infrastructure Only
```bash
docker-compose -f docker-compose-master.yml down
```

### Stop Everything
```batch
# Stop all backend services first
stop-all-services.bat

# Then stop infrastructure
docker-compose -f docker-compose-master.yml down
```

### View Logs

**All infrastructure logs:**
```bash
docker-compose -f docker-compose-master.yml logs -f
```

**Specific service logs:**
```bash
docker-compose -f docker-compose-master.yml logs -f elasticsearch
docker-compose -f docker-compose-master.yml logs -f zeebe
```

**Individual service logs:**
Check the terminal windows where services are running

### Restart a Service

**Restart a Docker service:**
```bash
docker-compose -f docker-compose-master.yml restart elasticsearch
```

**Restart a backend service:**
1. Close its terminal window
2. Run the startup script again (it will restart only that service)

## Troubleshooting

### Script Says Docker Not Running

**Problem**: Docker Desktop is not started

**Solution**: 
1. Start Docker Desktop
2. Wait for it to be ready
3. Run the startup script again

### Services Showing "STARTING..."

**Problem**: Services haven't finished initializing

**Solution**: Wait longer (some services take 2-3 minutes)

### Service Shows "UNHEALTHY"

**Problem**: Service started but failed health check

**Solution**:
1. Check logs: `docker-compose -f docker-compose-master.yml logs service-name`
2. Restart the service: `docker-compose -f docker-compose-master.yml restart service-name`

### Backend Service Won't Start

**Problem**: Java service fails to start

**Solution**:
1. Check if infrastructure is healthy: `.\check-system-status.ps1`
2. Check the service's terminal window for error messages
3. Ensure databases are initialized
4. Check if port is already in use

### Port Already in Use

**Problem**: Another application is using a required port

**Solution**:
1. Find what's using the port:
   ```powershell
   Get-NetTCPConnection -LocalPort 8080 | Select-Object -Property LocalPort, OwningProcess
   ```
2. Stop that application or change the port in the service configuration

### Frontend Won't Start

**Problem**: npm start fails

**Solution**:
1. Check if port 3000 is available
2. Ensure npm dependencies are installed:
   ```bash
   cd frontend
   npm install
   ```
3. Check for error messages in the terminal

## Service URLs Reference

### User Interfaces
- **Main Application**: http://localhost:3000
- **Camunda Operate**: http://localhost:8181
- **Camunda Tasklist**: http://localhost:8182
- **Kibana**: http://localhost:5601
- **MinIO Console**: http://localhost:9001 (minio/password)
- **Neo4j Browser**: http://localhost:7474 (neo4j/password)

### API Endpoints
- **Graph Service**: http://localhost:8090
- **Workflow Orchestrator**: http://localhost:8086
- **User Service**: http://localhost:8083
- **Task Service**: http://localhost:8082
- **Document Service**: http://localhost:8081
- **BOM Service**: http://localhost:8089
- **Change Service**: http://localhost:8084
- **Search Service**: http://localhost:8091

### Infrastructure
- **Elasticsearch**: http://localhost:9200
- **Zeebe Gateway**: localhost:26500 (gRPC)
- **Zeebe REST**: http://localhost:8088
- **MySQL**: localhost:3306 (root/root)
- **Redis**: localhost:6379

## Data Persistence

### Docker Volumes

All data is stored in Docker volumes:
- `elasticsearch-data` - Search indices
- `zeebe-data` - Workflow data
- `minio-data` - File storage
- `mysql-data` - Database data
- `neo4j-data` - Graph data
- `redis-data` - Cache data

### Backup Data
```bash
# Backup all volumes
docker-compose -f docker-compose-master.yml down
docker run --rm -v elasticsearch-data:/data -v /c/backup:/backup alpine tar czf /backup/elasticsearch.tar.gz -C /data .

# Or use the cleanup scripts to reset data
cleanup-all-data.bat
```

## Performance Tips

1. **Allocate enough memory to Docker Desktop**
   - Recommended: At least 8GB RAM
   - Settings → Resources → Memory

2. **Use SSD for Docker volumes**
   - Significantly improves database and search performance

3. **Start only what you need**
   - If not working on workflows, can skip Zeebe/Operate/Tasklist
   - Edit docker-compose-master.yml to comment out unused services

4. **Warm up services**
   - First startup is slow (downloading images)
   - Subsequent startups are much faster
   - Keep containers running during development

## Next Steps

1. **Read the service-specific documentation**
   - Each service has its own README
   - Check API documentation in `docs/`

2. **Learn the APIs**
   - Visit actuator endpoints: `http://localhost:808X/actuator`
   - Check Swagger/OpenAPI docs if available

3. **Customize the startup**
   - Edit docker-compose-master.yml for your needs
   - Modify startup scripts for your workflow

4. **Set up your IDE**
   - Import Maven projects
   - Configure run configurations
   - Set up debugging

## Support

If you encounter issues:

1. Check `.\check-system-status.ps1`
2. Review service logs
3. Check existing documentation in `docs/`
4. Review the comprehensive guides:
   - `README.md`
   - `STARTUP_GUIDE.md`
   - `README-DATABASE.md`

Happy developing! 🚀



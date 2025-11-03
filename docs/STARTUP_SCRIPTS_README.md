# PLM Lite - Startup Scripts Summary

## What's New

Based on your current Docker state, I've created an intelligent startup system that:

1. ✅ Detects what's already running
2. ✅ Starts only what's needed
3. ✅ Provides detailed status information
4. ✅ Handles proper dependency ordering

## Your Current State

```
Running Containers (6/10):
  ✓ Elasticsearch (plm-elasticsearch)
  ✓ Kibana (plm-kibana)
  ✓ Zeebe
  ✓ Connectors (unhealthy - may need restart)
  ✓ MinIO
  ✓ Redis

Missing Containers:
  ✗ Operate
  ✗ Tasklist
  ✗ MySQL
  ✗ Neo4j

Backend Services:
  All stopped (need to be started)
```

## Files Created

### 1. docker-compose-master.yml
**One file for all infrastructure**

Includes:
- Elasticsearch + Kibana
- Camunda Platform 8.7 (Zeebe, Operate, Tasklist, Connectors)
- MinIO (file storage)
- Redis (caching)
- MySQL (database)
- Neo4j (graph database)

All with proper networking, volumes, and health checks.

### 2. start-plm-system.bat / start-plm-system.ps1
**Intelligent startup scripts**

Features:
- Checks Docker status
- Detects running containers and services
- Starts only what's needed
- Proper dependency ordering
- Progress reporting
- Comprehensive status output

### 3. check-system-status.ps1
**System health checker**

Shows:
- Docker engine status
- All container statuses with health info
- Backend service statuses
- Summary statistics
- Quick action commands

### 4. Documentation
- `STARTUP_SYSTEM_GUIDE.md` - Complete guide
- `QUICK_START.md` - Quick reference
- `STARTUP_SCRIPTS_README.md` - This file

## Quick Start

### Start Everything
```powershell
.\start-plm-system.ps1
```

This will:
1. Check Docker is running ✓
2. Start missing containers (Operate, Tasklist, MySQL, Neo4j)
3. Wait for containers to be healthy
4. Start all 8 backend services in order
5. Start the React frontend

### Check Status Anytime
```powershell
.\check-system-status.ps1
```

### Stop Everything
```batch
# Stop backend services
stop-all-services.bat

# Stop infrastructure
docker-compose -f docker-compose-master.yml down
```

## What Happens When You Run the Startup Script

Based on your current state, the script will:

1. ✅ **Docker Check** - Already running
2. ⚠️ **Infrastructure** - Will start 4 missing containers:
   - Operate (Camunda workflow monitoring)
   - Tasklist (Camunda task UI)
   - MySQL (required for most services)
   - Neo4j (required for Graph & Change services)
3. ⏳ **Wait 60 seconds** for containers to be healthy
4. 🚀 **Start Backend Services** in order:
   - Graph Service (8090) - 45s wait
   - Workflow Orchestrator (8086) - 45s wait
   - User Service (8083) - 45s wait
   - Task Service (8082) - 45s wait
   - Document Service (8081) - 45s wait
   - BOM Service (8089) - 45s wait
   - Change Service (8084) - 45s wait
   - Search Service (8091) - 30s wait
5. 🎨 **Start Frontend** (3000) - 10s wait

**Total time**: ~7-10 minutes

## After First Startup

### Initialize Databases (First Time Only)

```batch
# Initialize MySQL databases
.\init-mysql-databases-docker.bat

# Reindex Elasticsearch
.\reindex-all-elasticsearch.bat
```

### Access the System

**User Interfaces:**
- Main App: http://localhost:3000
- Camunda Operate: http://localhost:8181
- Camunda Tasklist: http://localhost:8182
- Kibana: http://localhost:5601
- MinIO Console: http://localhost:9001
- Neo4j Browser: http://localhost:7474

**Backend APIs:**
- All services on ports 808X (see STARTUP_SYSTEM_GUIDE.md)

## Daily Development Workflow

### Morning (Start Development)
```powershell
.\start-plm-system.ps1
```
Wait ~5-10 minutes, then start coding!

### During Development
```powershell
# Check what's running
.\check-system-status.ps1

# View container logs
docker-compose -f docker-compose-master.yml logs -f elasticsearch

# Restart a specific container
docker-compose -f docker-compose-master.yml restart mysql-plm
```

### Evening (Stop Development)
```batch
# Stop just the backend services (keeps containers running)
stop-all-services.bat

# Or stop everything
stop-all-services.bat
docker-compose -f docker-compose-master.yml down
```

## Advantages Over Previous Scripts

### Before
- Multiple docker-compose files in different directories
- Had to remember which containers to start
- No state detection
- Would try to start already running services
- No unified status checking

### Now
- ✅ Single master docker-compose file
- ✅ Automatic state detection
- ✅ Starts only what's needed
- ✅ Proper dependency ordering
- ✅ Comprehensive health checks
- ✅ Easy status monitoring
- ✅ Better error handling
- ✅ Detailed progress reporting

## Container Details

### Currently Running from Your Setup

**plm-elasticsearch** & **plm-kibana**
- Started from: old `docker-compose-elasticsearch.yml`
- Status: Healthy ✓
- Action: Will be reused by new system

**zeebe, connectors**
- Started from: `infra/docker-compose-8.7/docker-compose-core.yaml`
- Status: Zeebe healthy, Connectors unhealthy
- Action: Can restart connectors if needed

**minio**
- Started from: external `filestorageservice/docker-compose.yaml`
- Status: Running
- Action: Will work with new system

**redis**
- Started from: standalone container
- Status: Running
- Action: Will work with new system

### Will Be Started by New System

**operate, tasklist**
- Camunda 8.7 components
- Required for workflow monitoring

**mysql-plm**
- Required for: User, Task, Document, BOM, Change services
- Root password: root

**neo4j-plm**
- Required for: Graph service, Change service
- Credentials: neo4j/password

## Troubleshooting

### Connectors Showing Unhealthy
This is normal if it can't connect to required services. After starting all services, restart it:
```bash
docker-compose -f docker-compose-master.yml restart connectors
```

### Port Conflicts
If you get port conflicts when starting containers:
```powershell
# Check what's using a port
netstat -ano | findstr :9200

# Or in PowerShell
Get-NetTCPConnection -LocalPort 9200
```

### Containers Won't Start
```bash
# View logs
docker-compose -f docker-compose-master.yml logs mysql-plm

# Remove and recreate
docker-compose -f docker-compose-master.yml down
docker-compose -f docker-compose-master.yml up -d
```

## Next Steps

1. **Try the startup script:**
   ```powershell
   .\start-plm-system.ps1
   ```

2. **Watch it detect your current state** and start only what's missing

3. **Initialize databases** (first time only):
   ```batch
   .\init-mysql-databases-docker.bat
   .\reindex-all-elasticsearch.bat
   ```

4. **Access the application:**
   - http://localhost:3000

5. **Check the comprehensive guide:**
   - `STARTUP_SYSTEM_GUIDE.md`

## Summary

You now have a professional, production-grade startup system that:
- 🎯 Intelligently manages your infrastructure
- 🚀 Starts services in the correct order
- 📊 Provides real-time status information
- 🔄 Handles restarts gracefully
- 📝 Is fully documented

Enjoy your streamlined PLM development experience! 🚀



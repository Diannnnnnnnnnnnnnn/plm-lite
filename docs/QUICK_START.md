# PLM Lite - Quick Start

**TL;DR**: Run `.\start-plm-system.ps1` and wait 10 minutes.

## First Time Setup

### 1. Prerequisites
- Docker Desktop installed and running
- Java 17+ installed
- Maven installed
- Node.js 16+ installed

### 2. Start Everything
```powershell
.\start-plm-system.ps1
```

Wait 10 minutes for full startup.

### 3. Initialize Data (First Time Only)
```batch
.\init-mysql-databases-docker.bat
.\reindex-all-elasticsearch.bat
```

### 4. Access
- **Application**: http://localhost:3000
- **Workflow UI**: http://localhost:8181
- **Search UI**: http://localhost:5601

## Daily Use

### Start System
```powershell
.\start-plm-system.ps1
```

### Check Status
```powershell
.\check-system-status.ps1
```

### Stop System
```batch
stop-all-services.bat
docker-compose -f docker-compose-master.yml down
```

## Common Issues

### Docker Not Running
→ Start Docker Desktop

### Port Already in Use
→ Check what's using it: `netstat -ano | findstr :PORT`

### Service Won't Start
→ Check logs in the service's terminal window

### Slow Startup
→ Normal on first run (downloading Docker images)

## Key Files

| File | Purpose |
|------|---------|
| `start-plm-system.ps1` | Start everything |
| `check-system-status.ps1` | Check what's running |
| `docker-compose-master.yml` | Infrastructure definition |
| `stop-all-services.bat` | Stop backend services |

## Service URLs

### Main UIs
- Frontend: http://localhost:3000
- Operate: http://localhost:8181
- Kibana: http://localhost:5601

### Backend APIs
- User: http://localhost:8083
- Task: http://localhost:8082
- Document: http://localhost:8081
- Change: http://localhost:8084
- BOM: http://localhost:8089
- Search: http://localhost:8091

## Need More Help?

Read the full guide: `STARTUP_SYSTEM_GUIDE.md`



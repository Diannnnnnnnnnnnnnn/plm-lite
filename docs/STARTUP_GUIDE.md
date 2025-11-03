# PLM System Startup Guide

## 🚀 Quick Start

### Windows Users (Batch Files)

**Start all services:**
```cmd
start-all-services.bat
```

**Stop all services:**
```cmd
stop-all-services.bat
```

### PowerShell Users

**Start all services:**
```powershell
.\start-services-mysql.ps1
```

**Stop all services:**
```powershell
.\stop-all-services.ps1
```

**Check service status:**
```powershell
.\check-services.ps1
```

---

## 📋 What Happens When You Start

1. **8 CMD windows** will open:
   - Graph Service (Port 8090) - Neo4j backend
   - Workflow Orchestrator (Port 8086) - BPMN workflows
   - User Service (Port 8083)
   - Task Service (Port 8082)
   - Document Service (Port 8081)
   - BOM Service (Port 8089)
   - Change Service (Port 8084)
   - Frontend (Port 3000) - React UI

2. Each backend service will:
   - Connect to **MySQL** database (or Neo4j for Graph Service)
   - Create tables automatically (Hibernate DDL)
   - Start accepting requests

3. Frontend will:
   - Start React development server
   - Auto-open browser at http://localhost:3000

4. Total startup time: **~6 minutes**

---

## ✅ Verify Services Are Running

### Option 1: Check Ports
```cmd
netstat -ano | findstr "8081 8082 8083 8084 8089 8090 3000"
```

Or use PowerShell:
```powershell
.\check-services.ps1
```

### Option 2: Check MySQL Tables
Open MySQL and run:
```sql
SHOW DATABASES LIKE 'plm%';

USE plm_bom_db;
SHOW TABLES;

USE plm_user_db;
SHOW TABLES;

USE plm_task_db;
SHOW TABLES;

USE plm_document_db;
SHOW TABLES;

USE plm_change_db;
SHOW TABLES;
```

You should see tables in each database.

### Option 3: Test Endpoints
Open browser and visit:
- **Frontend UI**: http://localhost:3000 ⭐
- http://localhost:8090 (Graph Service)
- http://localhost:8089/parts (BOM Service)
- http://localhost:8083/users (User Service)
- http://localhost:8082/tasks (Task Service)
- http://localhost:8084/api/changes (Change Service)

---

## 📊 Database Configuration

All services now use **MySQL** with these databases:

| Service | Database | Tables |
|---------|----------|--------|
| User Service | plm_user_db | users, roles, etc. |
| Task Service | plm_task_db | tasks, file_metadata, etc. |
| Document Service | plm_document_db | document, document_revision |
| BOM Service | plm_bom_db | part, bom_header, bom_item, part_usage |
| Change Service | plm_change_db | change_table, change_bom, change_part |

**Connection Details:**
- Host: localhost:3306
- Username: plm_user
- Password: plm_password

---

## 🔧 Troubleshooting

### Services won't start
- Check if MySQL is running
- Check if ports 8081-8084, 8089 are free
- Check service window for error messages

### MySQL tables not created
- Wait 2-3 minutes after service starts
- Check service logs for Hibernate errors
- Verify MySQL credentials in application.yml

### "Port already in use" error
1. Stop all services: `stop-all-services.bat`
2. Verify no processes running:
   ```cmd
   tasklist | findstr java
   tasklist | findstr node
   ```
3. Kill if needed:
   ```cmd
   taskkill /F /IM java.exe
   taskkill /F /IM node.exe
   ```
4. Restart: `start-all-services.bat`

### Frontend not starting
- Check if port 3000 is free
- Check if Node.js is installed: `node --version`
- Check if npm dependencies installed: `cd frontend && npm install`

---

## 📝 Next Steps After Startup

1. ✅ **Open Frontend** - Browser should auto-open at http://localhost:3000
2. ✅ **Create Users** - via User Management UI
3. ✅ **Upload Documents** - via Document Management UI  
4. ✅ **Create Parts/BOMs** - via BOM Management UI
5. ✅ **Create Changes** - Link to parts/BOMs created above

All data will now **persist** across service restarts! 🎉

---

## ⚠️ Important Notes

- **7 Windows** - Don't close them while using the system
- **Old H2 data is NOT migrated** - This is a fresh start with MySQL
- **Data persists** - Stopping/restarting services won't lose data
- **Neo4j** - Graph and Change services use Neo4j (localhost:7687)
- **Redis** - Some services use Redis for caching (localhost:6379)
- **Frontend** - Runs on Node.js, separate from backend services

---

## 🛑 Stopping Services

### Graceful Shutdown
Close each service window (7 windows total)

### Force Stop
Run: `stop-all-services.bat`

This will stop both backend (Java) and frontend (Node.js) services.

Or manually:
```cmd
taskkill /F /IM java.exe
taskkill /F /IM node.exe
```

---

## 📁 Files Created

- `start-all-services.bat` - Start all services (Windows)
- `stop-all-services.bat` - Stop all services (Windows)
- `start-services-mysql.ps1` - Start all services (PowerShell)
- `stop-all-services.ps1` - Stop all services (PowerShell)
- `check-services.ps1` - Check service status (PowerShell)

---

**Ready to go! Run `start-all-services.bat` to begin! 🚀**


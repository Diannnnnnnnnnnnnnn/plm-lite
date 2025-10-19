# 🚀 Camunda Integration - Quick Start

## Start Everything (Windows)

### 1. Start Camunda Docker (if not running)
```powershell
cd infra\docker-compose-8.7
docker-compose -f docker-compose-core.yaml up -d
```

### 2. Start Backend Services

**Terminal 1 - Workflow Orchestrator** (NEW - Camunda Integration)
```powershell
cd workflow-orchestrator
mvn spring-boot:run
```
✓ Wait for: "✓ Zeebe worker activated"

**Terminal 2 - Document Service**
```powershell
cd document-service
mvn spring-boot:run
```

**Terminal 3 - Task Service**
```powershell
cd task-service
mvn spring-boot:run
```

**Terminal 4 - User Service**
```powershell
cd user-service
mvn spring-boot:run
```

### 3. Start Frontend
```powershell
cd frontend
npm start
```

---

## Test the Workflow

1. **Login to PLM** → http://localhost:3001
2. **Create/Upload a Document**
3. **Submit for Review** → Select reviewers
4. **Check Task Management** → Tasks should appear for reviewers

---

## Monitor Workflows

| Service | URL | Purpose |
|---------|-----|---------|
| **Camunda Operate** | http://localhost:8181 | View workflow instances & progress |
| **Camunda Tasklist** | http://localhost:8182 | View user tasks |
| **Elasticsearch** | http://localhost:9200 | Workflow data storage |
| **Zeebe Gateway** | localhost:26500 | Workflow engine (gRPC) |

---

## Quick Checks

### Is Camunda Running?
```powershell
docker ps | findstr zeebe
```

### View Workflow Logs
```powershell
# Workflow Orchestrator
cd workflow-orchestrator
mvn spring-boot:run

# Watch for:
# ✓ Connected to Zeebe at localhost:26500
# ✓ BPMN process deployed: document-approval
# 📋 Creating approval tasks...
```

### Check Zeebe Health
```powershell
docker logs zeebe
```

---

## Common Issues

**❌ "Connection refused" to Zeebe**
```powershell
# Restart Zeebe
cd infra\docker-compose-8.7
docker-compose -f docker-compose-core.yaml restart zeebe
```

**❌ BPMN not deploying**
- Check: `workflow-orchestrator/src/main/resources/bpmn/document-approval.bpmn` exists
- Restart workflow-orchestrator

**❌ Tasks not creating**
- Verify Task Service is running on port 8082
- Check workflow-orchestrator logs for errors

---

## What Happens When You Submit a Document?

```
1. Frontend → Document Service
   └─ "Submit document for review"

2. Document Service → Workflow Orchestrator
   └─ "Start Camunda workflow"

3. Workflow Orchestrator → Camunda Zeebe
   └─ "Start document-approval process"

4. Camunda → Job Worker (create-approval-task)
   └─ "Create review tasks for each reviewer"

5. Job Worker → Task Service
   └─ "Create tasks in database"

6. Frontend → Task Management
   └─ "Display tasks to reviewers"
```

---

## Useful Commands

### Compile workflow-orchestrator
```powershell
cd workflow-orchestrator
mvn clean compile -DskipTests
```

### View all Zeebe processes
```powershell
curl http://localhost:9200/zeebe-record-process/_search?pretty
```

### Stop Camunda
```powershell
cd infra\docker-compose-8.7
docker-compose -f docker-compose-core.yaml down
```

---

## Files Changed

### New Files Created
- `workflow-orchestrator/src/main/java/.../handler/DocumentWorkflowWorkers.java`
- `start-camunda-workflow.bat`
- `CAMUNDA_INTEGRATION_GUIDE.md`
- `CAMUNDA_QUICK_START.md` (this file)

### Modified Files
- `workflow-orchestrator/pom.xml` - Enabled Camunda dependencies
- `workflow-orchestrator/src/main/resources/application.yml` - Zeebe configuration
- `workflow-orchestrator/src/main/java/.../service/WorkflowService.java` - Zeebe client integration
- `workflow-orchestrator/src/main/java/.../controller/WorkflowController.java` - REST APIs
- `document-service/src/main/java/.../client/WorkflowOrchestratorClient.java` - Feign client
- `document-service/src/main/java/.../service/impl/WorkflowGatewayFeign.java` - Gateway implementation

---

## Need More Help?

📖 **Full Guide:** See `CAMUNDA_INTEGRATION_GUIDE.md`  
🌐 **Camunda Docs:** https://docs.camunda.io/  
🔧 **Troubleshooting:** Check Camunda Operate at http://localhost:8181

---

**Ready to test?** Follow the steps above and watch your workflows come to life! 🎉


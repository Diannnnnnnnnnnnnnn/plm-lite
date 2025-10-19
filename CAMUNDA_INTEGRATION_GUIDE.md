# Camunda Workflow Integration Guide

## 📋 Overview

This guide explains how to use the **Camunda 8 Workflow Engine** integration for PLM Lite's Document Review Workflow.

### What's Been Implemented

✅ **Camunda Zeebe Client** - Connected to your local Docker Camunda instance  
✅ **BPMN Process** - `document-approval.bpmn` workflow process  
✅ **Zeebe Job Workers** - Service task handlers for workflow automation  
✅ **REST APIs** - Workflow orchestrator endpoints  
✅ **Document Service Integration** - Automatic workflow triggering on document submit  

---

## 🏗️ Architecture

```
┌─────────────────┐
│   Frontend      │
│  (React App)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│ Document Service│────▶│ Workflow         │────▶│  Camunda Zeebe  │
│  (Port 8081)    │     │ Orchestrator     │     │  (Port 26500)   │
└─────────────────┘     │  (Port 8086)     │     └─────────────────┘
                        └──────────────────┘
                                │
                                ▼
                        ┌──────────────────┐
                        │  Task Service    │
                        │  (Port 8082)     │
                        └──────────────────┘
```

### Workflow Flow:

1. **User submits document for review** → Frontend calls Document Service
2. **Document Service** → Changes status to `IN_REVIEW` and calls Workflow Orchestrator
3. **Workflow Orchestrator** → Starts Camunda BPMN process `document-approval`
4. **Camunda Zeebe** → Executes workflow steps:
   - **Service Task**: Create approval tasks (handled by `create-approval-task` worker)
   - **User Task**: Review document (assigned to reviewers, appears in Task Management)
   - **Gateway**: Approved or Rejected decision
   - **Service Task**: Update document status (handled by `update-status` worker)
   - **Service Task**: Notify completion (handled by `notify-completion` worker)

---

## 🚀 Setup Instructions

### Prerequisites

✅ Docker Desktop installed and running  
✅ Camunda 8.7 Docker containers running  
✅ Java 17+ and Maven installed  
✅ Backend services compiled  

### Step 1: Start Camunda Docker Containers

```powershell
cd infra\docker-compose-8.7
docker-compose -f docker-compose-core.yaml up -d
```

**Verify Camunda is running:**
```powershell
docker ps
```

You should see these containers:
- `zeebe` (Port 26500) - Workflow engine
- `operate` (Port 8181) - Workflow monitoring UI
- `tasklist` (Port 8182) - Task management UI
- `elasticsearch` (Port 9200) - Data storage

### Step 2: Compile Workflow Orchestrator

```powershell
cd workflow-orchestrator
mvn clean compile -DskipTests
```

### Step 3: Start Workflow Orchestrator Service

**Option A: Using the startup script (Windows)**
```powershell
cd plm-lite
.\start-camunda-workflow.bat
```

**Option B: Manual startup**
```powershell
cd workflow-orchestrator
mvn spring-boot:run
```

**Look for these startup messages:**
```
🚀 Starting workflow-orchestrator...
✓ Connected to Zeebe at localhost:26500
✓ BPMN process deployed: document-approval
✓ Zeebe worker activated: create-approval-task
✓ Zeebe worker activated: update-status
✓ Zeebe worker activated: notify-completion
```

### Step 4: Start Other Backend Services

Make sure these services are running:

```powershell
# Terminal 1: Document Service
cd document-service
mvn spring-boot:run

# Terminal 2: Task Service
cd task-service
mvn spring-boot:run

# Terminal 3: User Service
cd user-service
mvn spring-boot:run
```

### Step 5: Start Frontend

```powershell
cd frontend
npm start
```

---

## 🧪 Testing the Integration

### Test 1: Submit Document for Review

1. **Login to PLM Lite** (http://localhost:3001)
2. **Go to Document Management**
3. **Create a new document** or select existing document
4. **Click "Submit for Review"**
5. **Select reviewers** from the list
6. **Click "Submit"**

**Expected Backend Logs:**

**Document Service:**
```
INFO: Submitting document for review: doc-123
🔵 WorkflowGateway: Starting Camunda document approval workflow
   Document ID: doc-123
   Master ID: SPEC-001
   Version: 1.0
   Creator: john_doe
   Reviewers: [2, 3]
   ✓ Workflow started successfully!
   Process Instance Key: 2251799813685249
```

**Workflow Orchestrator:**
```
🔵 API: Starting document approval workflow
   Request: StartDocumentApprovalRequest{documentId='doc-123', ...}
🚀 Starting document approval workflow for: doc-123
   ✓ Workflow started successfully!
   Process Instance Key: 2251799813685249

📋 Creating approval tasks for document: doc-123
   Reviewers: [2, 3]
   ✓ Resolved user ID 2 to username: jane_smith
   ✓ Created task ID 45 for jane_smith
   ✓ Resolved user ID 3 to username: bob_jones
   ✓ Created task ID 46 for bob_jones
```

**Task Service:**
```
INFO: Creating task: Review Document: SPEC-001 1.0
   Assigned to: jane_smith (ID: 2)
```

### Test 2: View Workflow in Camunda Operate

1. **Open Camunda Operate:** http://localhost:8181
2. **Login:** (No auth required in dev mode)
3. **Navigate to "Instances"**
4. **Find your process instance** by Process Instance Key
5. **View the workflow progress** - you'll see which tasks are completed and which are active

### Test 3: View Tasks in Camunda Tasklist

1. **Open Camunda Tasklist:** http://localhost:8182
2. **Login as a reviewer**
3. **View assigned tasks**
4. **Complete tasks** (Note: Frontend integration for task completion is pending)

### Test 4: View Tasks in PLM Task Management

1. **In PLM Frontend, go to Task Management**
2. **Review tasks should appear** for assigned reviewers
3. **Task details should include:**
   - Task name: "Review Document: SPEC-001 1.0"
   - Description with document info
   - Workflow instance ID

---

## 🔧 Configuration

### Workflow Orchestrator Configuration

**File:** `workflow-orchestrator/src/main/resources/application.yml`

```yaml
camunda:
  client:
    mode: simple
    zeebe:
      enabled: true
      gateway-address: localhost:26500  # Zeebe gateway
      rest-address: http://localhost:8088  # Zeebe REST API
      prefer-rest-over-grpc: false
    worker:
      max-jobs-active: 32
      request-timeout: 10s
      threads: 3
```

### Document Service Configuration

The document service automatically calls the workflow orchestrator when a document is submitted for review. No additional configuration needed.

---

## 📊 Monitoring & Debugging

### 1. **Camunda Operate** (Process Monitoring)

URL: http://localhost:8181

Features:
- View all process instances
- Monitor workflow progress
- See which tasks are active
- View process variables
- Inspect incidents/errors

### 2. **Camunda Tasklist** (Task Management)

URL: http://localhost:8182

Features:
- View user tasks
- Complete tasks
- Add comments
- Claim tasks

### 3. **Backend Logs**

**Enable DEBUG logging for Camunda:**

Add to `workflow-orchestrator/src/main/resources/application.yml`:

```yaml
logging:
  level:
    com.example.plm.workflow: DEBUG
    io.camunda.zeebe: DEBUG
    io.camunda: DEBUG
```

**Restart workflow-orchestrator** to see detailed logs.

### 4. **Elasticsearch** (Data Storage)

URL: http://localhost:9200

View workflow data:
```bash
# List all Zeebe indices
curl http://localhost:9200/_cat/indices?v | findstr zeebe

# View process instances
curl http://localhost:9200/zeebe-record-process-instance/_search?pretty
```

---

## 🐛 Troubleshooting

### Issue: "Failed to start workflow: Connection refused"

**Cause:** Zeebe is not running or not accessible

**Solution:**
```powershell
# Check if Zeebe container is running
docker ps | findstr zeebe

# If not running, start Camunda
cd infra\docker-compose-8.7
docker-compose -f docker-compose-core.yaml up -d

# Check Zeebe health
docker logs zeebe
```

### Issue: "BPMN process not found: document-approval"

**Cause:** BPMN file not deployed

**Solution:**
1. Verify BPMN file exists: `workflow-orchestrator/src/main/resources/bpmn/document-approval.bpmn`
2. Restart workflow-orchestrator
3. Check logs for deployment message

### Issue: "No workers available for job type: create-approval-task"

**Cause:** Job workers not registered

**Solution:**
1. Verify `DocumentWorkflowWorkers.java` is in the correct package
2. Check `@JobWorker` annotations are present
3. Restart workflow-orchestrator
4. Look for worker activation messages in logs

### Issue: "Task not created in Task Service"

**Cause:** Task Service not running or Feign client error

**Solution:**
1. Verify Task Service is running on port 8082
2. Check workflow-orchestrator logs for Feign errors
3. Test Task Service endpoint manually:
   ```bash
   curl http://localhost:8082/tasks
   ```

---

## 📝 Next Steps

### Completed ✅
- [x] Camunda Zeebe integration
- [x] BPMN workflow deployment
- [x] Job workers implementation
- [x] REST API endpoints
- [x] Document service integration
- [x] Task creation automation

### Pending 🔜
- [ ] Frontend: Display workflow instance ID in document details
- [ ] Frontend: Task completion integration (call Camunda API to complete user tasks)
- [ ] Frontend: Workflow progress visualization
- [ ] Change Request workflow integration (similar to document workflow)
- [ ] BOM approval workflow
- [ ] Email notifications on workflow completion
- [ ] Workflow history and audit trail in UI

---

## 📚 Additional Resources

- **Camunda 8 Documentation:** https://docs.camunda.io/
- **Zeebe Client Java:** https://docs.camunda.io/docs/apis-tools/java-client/
- **BPMN 2.0 Modeling:** https://docs.camunda.io/docs/components/modeler/bpmn/

---

## 🎯 API Reference

### Workflow Orchestrator APIs

**Base URL:** `http://localhost:8086/api/workflows`

#### 1. Start Document Approval Workflow

```http
POST /document-approval/start
Content-Type: application/json

{
  "documentId": "doc-uuid",
  "masterId": "SPEC-001",
  "version": "1.0",
  "creator": "john_doe",
  "reviewerIds": ["2", "3"]
}
```

**Response:**
```json
{
  "processInstanceKey": "2251799813685249",
  "status": "STARTED",
  "message": "Document approval workflow started successfully"
}
```

#### 2. Complete User Task

```http
POST /tasks/{jobKey}/complete
Content-Type: application/json

{
  "approved": true,
  "comment": "Looks good!",
  "reviewer": "jane_smith"
}
```

#### 3. Cancel Process Instance

```http
DELETE /instances/{processInstanceKey}
```

---

## 📞 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review backend logs for error messages
3. Verify all services are running
4. Check Camunda Operate for process incidents

---

**Last Updated:** 2025-01-19  
**Version:** 1.0  
**Author:** PLM Lite Development Team


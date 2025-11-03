# Elasticsearch with Docker - Complete Setup Guide

## ✅ Configuration Summary

### Port Configuration
- **Elasticsearch**: 9200 (Running in Docker)
- **Kibana**: 5601 (Running in Docker)
- **Search Service**: 8091 ⭐ (Your Spring Boot service)

### Architecture
```
┌─────────────────────────────────────────────────────┐
│         Frontend (React - Port 3000)                 │
│  GlobalSearch.js → http://localhost:8091/api/search  │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│    Search Service (Port 8091) - Spring Boot         │
│    Running on host machine                          │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│    Elasticsearch (Port 9200) - Docker Container     │
│    Container: plm-elasticsearch                     │
└─────────────────────────────────────────────────────┘
```

## 🚀 Quick Start (3 Steps)

### Step 1: Start Elasticsearch in Docker (2 minutes)
```batch
start-elasticsearch.bat
```

This will:
- Start Elasticsearch container on port 9200
- Start Kibana container on port 5601
- Configure Docker networking
- Set up data persistence

**Verify Elasticsearch:**
```bash
curl http://localhost:9200
```

**Verify Kibana:**
Open http://localhost:5601 in browser

---

### Step 2: Start All Services (5 minutes)
```batch
start-all-services-with-search.bat
```

This will start (in order):
1. Graph Service (8090)
2. Workflow Orchestrator (8086)
3. User Service (8083)
4. Task Service (8082)
5. Document Service (8081)
6. BOM Service (8089)
7. Change Service (8084)
8. **Search Service (8091)** ⭐
9. Frontend (3000)

**Verify Search Service:**
```bash
curl http://localhost:8091/actuator/health
```

---

### Step 3: Reindex Existing Data (1 minute)
```batch
reindex-all-elasticsearch.bat
```

This will index all existing data from MySQL into Elasticsearch.

**Test Search:**
```bash
curl "http://localhost:8091/api/search/global?q=motor"
```

---

## 📦 What's Been Configured

### 1. Docker Compose File
**File**: `docker-compose-elasticsearch.yml`

```yaml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: plm-elasticsearch
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
```

### 2. Search Service Configuration
**File**: `infra/search-service/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: search-service
  elasticsearch:
    uris: http://localhost:9200  # Connects to Docker ES

server:
  port: 8091  # Your requested port
```

### 3. Frontend Configuration
**File**: `frontend/src/components/GlobalSearch.js`

```javascript
const SEARCH_SERVICE_URL = 'http://localhost:8091/api/search';
```

---

## 🔧 Service Configuration Details

### Search Service (Port 8091)

**Endpoints:**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/search/global` | GET | Search across all entities |
| `/api/search/documents` | GET | Search documents only |
| `/api/search/changes` | GET | Search changes only |
| `/api/search/tasks` | GET | Search tasks only |
| `/api/search/boms` | GET | Search BOMs only |
| `/actuator/health` | GET | Health check |

**Example Request:**
```bash
curl "http://localhost:8091/api/search/global?q=motor"
```

**Example Response:**
```json
{
  "documents": [
    {
      "id": "DOC-001",
      "title": "Motor Specification",
      "creator": "John Doe",
      "_score": 2.5
    }
  ],
  "changes": [...],
  "tasks": [...],
  "boms": [...]
}
```

---

## 🐳 Docker Management

### Start Elasticsearch
```batch
start-elasticsearch.bat
```

Or manually:
```batch
docker-compose -f docker-compose-elasticsearch.yml up -d
```

### Stop Elasticsearch
```batch
stop-elasticsearch.bat
```

Or manually:
```batch
docker-compose -f docker-compose-elasticsearch.yml down
```

### Check Container Status
```batch
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE                               STATUS
abc123...      elasticsearch:8.11.0               Up 5 minutes (healthy)
def456...      kibana:8.11.0                      Up 5 minutes (healthy)
```

### View Logs
```batch
# Elasticsearch logs
docker logs plm-elasticsearch

# Follow logs in real-time
docker logs -f plm-elasticsearch

# Kibana logs
docker logs plm-kibana
```

### Check Container Resources
```batch
docker stats plm-elasticsearch
```

### Restart Container
```batch
docker restart plm-elasticsearch
```

---

## ✅ Verification Steps

### 1. Check Elasticsearch is Running
```bash
curl http://localhost:9200
```

Expected:
```json
{
  "name" : "plm-elasticsearch",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "8.11.0"
  }
}
```

### 2. Check Cluster Health
```bash
curl http://localhost:9200/_cluster/health
```

Expected:
```json
{
  "status": "yellow",  // Yellow is OK for single-node
  "number_of_nodes": 1
}
```

### 3. Check Indices
```bash
curl http://localhost:9200/_cat/indices?v
```

Expected (after reindexing):
```
health status index     pri rep docs.count
yellow open   documents   1   1         45
yellow open   changes     1   1         12
yellow open   tasks       1   1         23
yellow open   boms        1   1          8
```

### 4. Check Search Service
```bash
curl http://localhost:8091/actuator/health
```

Expected:
```json
{
  "status": "UP"
}
```

### 5. Test Search
```bash
curl "http://localhost:8091/api/search/global?q=test"
```

Should return search results from all entities.

---

## 🎨 Frontend Integration

Your GlobalSearch.js is configured to use port 8091:

```javascript
const SEARCH_SERVICE_URL = 'http://localhost:8091/api/search';

// When user types in search
const performSearch = async () => {
  const response = await axios.get(`${SEARCH_SERVICE_URL}/global`, {
    params: { q: debouncedSearchTerm }
  });
  setSearchResults(response.data);
};
```

**To test:**
1. Open http://localhost:3000
2. Navigate to Global Search
3. Type any keyword (e.g., "motor", "document", "design")
4. See results from all services!

---

## 🐛 Troubleshooting

### Problem: Docker won't start
**Error**: "Docker daemon is not running"

**Solution:**
1. Open Docker Desktop
2. Wait for it to start (green icon)
3. Retry: `start-elasticsearch.bat`

---

### Problem: Port 9200 already in use
**Error**: "Bind for 0.0.0.0:9200 failed"

**Solution:**
```batch
# Check what's using port 9200
netstat -ano | findstr :9200

# Stop existing Elasticsearch
docker stop plm-elasticsearch

# Or stop all ES containers
docker ps -a | findstr elasticsearch
docker rm -f <container_id>
```

---

### Problem: Port 8091 already in use
**Error**: "Port 8091 is already in use"

**Solution:**
```batch
# Check what's using port 8091
netstat -ano | findstr :8091

# Kill the process
taskkill /F /PID <process_id>
```

---

### Problem: Search service can't connect to Elasticsearch
**Error**: "Connection refused to localhost:9200"

**Solution:**
1. Verify ES is running: `curl http://localhost:9200`
2. Check Docker container: `docker ps | findstr elasticsearch`
3. Check logs: `docker logs plm-elasticsearch`
4. Restart ES: `docker restart plm-elasticsearch`

---

### Problem: No search results
**Symptoms**: Search returns empty arrays

**Solution:**
```bash
# Check if data is indexed
curl http://localhost:9200/_cat/indices?v

# If docs.count = 0, reindex
reindex-all-elasticsearch.bat

# Check service logs for indexing errors
```

---

### Problem: Out of memory
**Error**: Elasticsearch crashes or becomes slow

**Solution:**
1. **Increase Docker Memory:**
   - Open Docker Desktop
   - Settings → Resources
   - Set Memory to 4GB+
   - Apply & Restart

2. **Or reduce ES heap:**
   Edit `docker-compose-elasticsearch.yml`:
   ```yaml
   - "ES_JAVA_OPTS=-Xms256m -Xmx256m"
   ```

---

### Problem: Frontend CORS errors
**Error**: "Access-Control-Allow-Origin" blocked

**Solution:**
Add to Search Service controllers:
```java
@CrossOrigin(origins = "*")
```

Already configured in the provided code!

---

## 📊 Performance Tuning

### For Development (Default)
```yaml
# docker-compose-elasticsearch.yml
- "ES_JAVA_OPTS=-Xms512m -Xmx512m"
```

### For Production (Better Performance)
```yaml
# docker-compose-elasticsearch.yml
- "ES_JAVA_OPTS=-Xms2g -Xmx2g"
```

**Docker Desktop Settings:**
- Memory: 6GB+
- CPUs: 4+
- Swap: 1GB

---

## 📈 Monitoring with Kibana

**Access Kibana:** http://localhost:5601

### Useful Kibana Features:

**1. Dev Tools** (Query Elasticsearch)
```
GET /documents/_search
{
  "query": {
    "match": { "title": "motor" }
  }
}
```

**2. Stack Monitoring**
- Cluster health
- Node statistics
- Index performance

**3. Discover**
- Browse your indices
- Create filters
- Export data

**4. Dashboards**
- Create visualizations
- Monitor search patterns
- Track performance

---

## 🔄 Daily Workflow

### Starting Your Day
```batch
# 1. Start Docker Elasticsearch (if not already running)
start-elasticsearch.bat

# 2. Start all services
start-all-services-with-search.bat

# 3. Optional: Start frontend separately if needed
cd frontend && npm start
```

### During Development
- Create documents/changes/tasks → Auto-indexed ✅
- Search in frontend → Real-time results ✅
- Use Kibana for debugging queries

### End of Day
```batch
# Stop all services (Ctrl+C in each window)
# Or run: stop-all-services.bat

# Stop Elasticsearch (optional - can keep running)
stop-elasticsearch.bat
```

---

## 📚 Useful Commands Reference

### Docker
```bash
# List running containers
docker ps

# View all containers (including stopped)
docker ps -a

# Remove a container
docker rm -f plm-elasticsearch

# View volumes
docker volume ls

# Check disk usage
docker system df

# Clean up unused resources
docker system prune
```

### Elasticsearch
```bash
# Cluster health
curl http://localhost:9200/_cluster/health

# List indices
curl http://localhost:9200/_cat/indices?v

# Count documents
curl http://localhost:9200/documents/_count

# Get index mapping
curl http://localhost:9200/documents/_mapping

# Delete an index (careful!)
curl -X DELETE http://localhost:9200/documents
```

### Search Service
```bash
# Health check
curl http://localhost:8091/actuator/health

# Test global search
curl "http://localhost:8091/api/search/global?q=test"

# Test document search
curl "http://localhost:8091/api/search/documents?q=motor"
```

---

## 🎯 Next Steps

After everything is running:

1. ✅ **Test Basic Search**
   - Open http://localhost:3000
   - Go to Global Search
   - Search for "motor" or "test"

2. ✅ **Create Test Data**
   - Create some documents via UI
   - Watch them get indexed
   - Search for them immediately

3. ✅ **Explore Kibana**
   - Open http://localhost:5601
   - Go to Dev Tools
   - Run sample queries

4. ✅ **Implement Remaining Services**
   - Follow the main integration guide
   - Add search to Document service
   - Enable in Change/Task services

5. ✅ **Customize Search UI**
   - Add filters
   - Add highlighting
   - Add autocomplete

---

## 📖 Additional Documentation

- **Main Integration Guide:** `docs/ELASTICSEARCH_GLOBAL_SEARCH_INTEGRATION.md`
- **Quick Reference:** `ELASTICSEARCH_QUICK_REFERENCE.md`
- **Port Configuration:** `docs/PORT_CONFIGURATION.md`
- **Docker Details:** `START_ELASTICSEARCH_DOCKER.md`

---

## ✅ Configuration Checklist

- [x] Elasticsearch running on port 9200 (Docker)
- [x] Kibana running on port 5601 (Docker)
- [x] Search service configured for port 8091
- [x] Frontend configured to use port 8091
- [x] All startup scripts updated
- [x] All documentation updated
- [x] Docker Compose file ready
- [x] Reindex script ready

---

## 🎉 You're All Set!

**Everything is configured to use:**
- ✅ Elasticsearch in Docker (port 9200)
- ✅ Search Service on port 8091
- ✅ Frontend calling port 8091

**To start coding:**
```batch
# 1. Start infrastructure
start-elasticsearch.bat

# 2. Follow the implementation guide
# Open: docs/ELASTICSEARCH_GLOBAL_SEARCH_INTEGRATION.md

# 3. Start with Document service (Day 1-2)
```

**Need help?** Check `ELASTICSEARCH_QUICK_REFERENCE.md` for commands and troubleshooting!

---

**Last Updated:** 2025-10-29  
**Version:** 1.0  
**Elasticsearch Version:** 8.11.0 (Docker)  
**Search Service Port:** 8091 ✅




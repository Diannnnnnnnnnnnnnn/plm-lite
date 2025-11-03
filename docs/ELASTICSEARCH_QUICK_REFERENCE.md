# Elasticsearch Global Search - Quick Reference

## 🚀 Quick Start (30 Minutes)

### 1. Start Elasticsearch (2 min)
```batch
start-elasticsearch.bat
```
Wait for: http://localhost:9200 ✅

### 2. Start All Services (5 min)
```batch
start-all-services-with-search.bat
```

### 3. Reindex Existing Data (1 min)
```batch
reindex-all-elasticsearch.bat
```

### 4. Test Search
- Open http://localhost:3000
- Go to Global Search
- Type "motor" or any keyword
- See results! 🎉

---

## 📋 Service Ports Reference

| Service | Port | URL |
|---------|------|-----|
| **Elasticsearch** | 9200 | http://localhost:9200 |
| **Kibana** | 5601 | http://localhost:5601 |
| **Search Service** | 8091 | http://localhost:8091/api/search/global?q=test |
| Document Service | 8081 | http://localhost:8081 |
| Change Service | 8084 | http://localhost:8084 |
| Task Service | 8082 | http://localhost:8082 |
| BOM Service | 8089 | http://localhost:8089 |
| Frontend | 3000 | http://localhost:3000 |

---

## 🔍 Quick Test Commands

```bash
# Check Elasticsearch
curl http://localhost:9200

# Check indices
curl http://localhost:9200/_cat/indices?v

# Test global search
curl "http://localhost:8091/api/search/global?q=motor"

# Test document search
curl "http://localhost:8081/api/v1/documents/search/elastic?q=test"

# Test change search
curl "http://localhost:8084/api/changes/search/elastic?query=test"
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│         Frontend (React - Port 3000)             │
│                                                  │
│  GlobalSearch.js ─────────────────┐             │
└──────────────────────────────────┬─────────────┘
                                   │
                    Query: "motor" │
                                   ▼
         ┌─────────────────────────────────────┐
         │   Search Service (8085)             │
         │   /api/search/global?q=motor        │
         └────────────────┬────────────────────┘
                          │
            Queries all   │   Single API call
            indices       │
                          ▼
         ┌─────────────────────────────────────┐
         │   Elasticsearch (9200)              │
         │                                     │
         │   📂 documents    📂 changes        │
         │   📂 tasks        📂 boms           │
         └────────────┬────────────────────────┘
                      │
            Auto-sync │ on create/update
                      │
         ┌────────────┴────────────────────────┐
         │   Backend Services                  │
         │   - Document (8081)                 │
         │   - Change (8084)                   │
         │   - Task (8082)                     │
         │   - BOM (8089)                      │
         └─────────────────────────────────────┘
```

---

## ✅ Daily Workflow

### When Starting Work:
```batch
1. start-elasticsearch.bat
2. start-all-services-with-search.bat
3. cd frontend && npm start
```

### When Stopping Work:
```batch
1. Close all service windows (Ctrl+C)
2. stop-elasticsearch.bat
```

---

## 🔧 Common Tasks

### Add New Document
1. Create document via frontend
2. ✅ Auto-indexed to Elasticsearch
3. ✅ Immediately searchable

### Reindex All Data
```batch
reindex-all-elasticsearch.bat
```

### Check Index Health
```bash
curl http://localhost:9200/_cluster/health
```

### View Index Contents (Kibana)
1. Open http://localhost:5601
2. Go to Dev Tools
3. Run:
```json
GET /documents/_search
{
  "query": { "match_all": {} }
}
```

### Clear All Indices (Reset)
```bash
curl -X DELETE http://localhost:9200/documents
curl -X DELETE http://localhost:9200/changes
curl -X DELETE http://localhost:9200/tasks
curl -X DELETE http://localhost:9200/boms
```
Then reindex.

---

## 🐛 Troubleshooting

### Problem: "Connection refused"
**Solution:**
```batch
# Check if ES is running
curl http://localhost:9200

# If not, start it
start-elasticsearch.bat
```

### Problem: No search results
**Solution:**
```batch
# Check if data is indexed
curl http://localhost:9200/_cat/indices?v

# If docs.count = 0, reindex
reindex-all-elasticsearch.bat
```

### Problem: Service won't start
**Solution:**
```batch
# Check logs in service window
# Common issue: Port already in use

# Find process using port
netstat -ano | findstr :8091

# Kill process
taskkill /F /PID <process_id>
```

### Problem: Slow search
**Solution:**
```bash
# Check ES health
curl http://localhost:9200/_cluster/health

# Restart ES if needed
stop-elasticsearch.bat
start-elasticsearch.bat
```

---

## 📊 Search Features

### Basic Search
```
Type: "motor"
Results: All entities containing "motor"
```

### Fuzzy Search (Typo Tolerance)
```
Type: "moter" (typo)
Results: Still finds "motor" ✅
```

### Multi-word Search
```
Type: "motor assembly"
Results: Ranked by relevance
```

### Field-specific Search (Advanced)
```
Use Kibana Dev Tools:

GET /documents/_search
{
  "query": {
    "match": { "creator": "John Doe" }
  }
}
```

---

## 📈 Performance Benchmarks

| Operation | Expected Time |
|-----------|---------------|
| Search query | <50ms |
| Index document | <10ms |
| Reindex 1000 docs | ~5 seconds |
| Startup time | ~30 seconds |

---

## 🎯 Implementation Checklist

### Infrastructure ✅
- [ ] Elasticsearch running (9200)
- [ ] Kibana running (5601)
- [ ] Docker Compose configured

### Backend Services ✅
- [ ] Document service ES enabled
- [ ] Change service ES enabled
- [ ] Task service ES enabled
- [ ] BOM service ES enabled
- [ ] Search service running (8085)

### Frontend ✅
- [ ] GlobalSearch.js updated
- [ ] API endpoint configured
- [ ] CORS enabled

### Data ✅
- [ ] Indices created
- [ ] Data reindexed
- [ ] Search works

---

## 🔗 Useful Links

- **Elasticsearch Docs:** https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html
- **Kibana Dev Tools:** http://localhost:5601/app/dev_tools
- **Cluster Health:** http://localhost:9200/_cluster/health
- **Indices Stats:** http://localhost:9200/_cat/indices?v

---

## 📞 Quick Help

**Can't find something?**
```bash
# Search for a specific ID
curl "http://localhost:8085/api/search/global?q=DOC-001"

# Count documents in index
curl http://localhost:9200/documents/_count

# Get index mapping
curl http://localhost:9200/documents/_mapping
```

**Need to debug?**
1. Check service logs in terminal windows
2. Check Elasticsearch logs: `docker logs plm-elasticsearch`
3. Use Kibana Dev Tools for manual queries
4. Check browser console for frontend errors

---

**Last Updated:** 2025-10-29  
**Version:** 1.0

**📖 Full Documentation:**
- [Complete Integration Guide](docs/ELASTICSEARCH_GLOBAL_SEARCH_INTEGRATION.md)
- [Quick Start](docs/ELASTICSEARCH_QUICKSTART.md)
- [Comparison with Current](docs/ELASTICSEARCH_VS_CURRENT_SEARCH.md)


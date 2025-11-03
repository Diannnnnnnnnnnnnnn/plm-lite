# 🚀 Elasticsearch Integration - Quick Start

**Last Updated:** October 30, 2025  
**Status:** ✅ **READY TO USE**

---

## 🎯 What's Been Integrated

✅ **Phase 1: Document Service**
- Elasticsearch auto-indexing on create/update/delete
- 2 documents already indexed and searchable

✅ **Phase 2: Search Service + Frontend**
- Unified search API (port 8091)
- Global Search frontend connected
- Real-time search as you type

---

## ⚡ Quick Start (3 Steps)

### 1. Start All Services

```powershell
# Terminal 1: Elasticsearch & Kibana (if not already running)
docker-compose -f docker-compose-elasticsearch.yml up -d

# Terminal 2: Document Service
cd document-service
mvn spring-boot:run

# Terminal 3: Search Service
cd infra/search-service
mvn spring-boot:run

# Terminal 4: Frontend
cd frontend
npm start
```

### 2. Verify Everything Works

```powershell
# Quick health check
Invoke-RestMethod -Uri "http://localhost:9200" # Elasticsearch
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search/health" # Search Service
```

### 3. Test It!

1. Open http://localhost:3000
2. Go to **Global Search**
3. Search for **"Technical"** or **"version"**
4. See instant results! 🎉

---

## 📝 Common Commands

### Check Document Count
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:9200/documents/_search?size=0"
Write-Host "Total documents: $($result.hits.total.value)"
```

### Search via API
```powershell
# Search for "Technical"
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search?q=Technical"

# Get all documents
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search"
```

### Reindex All Documents
```powershell
powershell -ExecutionPolicy Bypass -File scripts/reindex-all-documents.ps1
```

### View in Kibana
1. Open http://localhost:5601
2. Go to **Dev Tools** → **Console**
3. Run: `GET /documents/_search`

---

## 🔧 Troubleshooting

### "No results found"
```powershell
# Refresh the index
Invoke-RestMethod -Uri "http://localhost:9200/documents/_refresh" -Method Post

# Reindex all documents
powershell -ExecutionPolicy Bypass -File scripts/reindex-all-documents.ps1
```

### "Search service unavailable"
```powershell
# Check if Search Service is running
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search/health"
```

### Services Won't Start
- **Elasticsearch:** Check Docker Desktop is running
- **Document Service:** Check MySQL is running (port 3306)
- **Search Service:** Check Elasticsearch is accessible
- **Frontend:** Run `npm install` if needed

---

## 📚 Full Documentation

For detailed information, see:
- **`ELASTICSEARCH_INTEGRATION_COMPLETE.md`** - Complete guide
- **`TEST_ELASTICSEARCH_INTEGRATION.md`** - Full test suite
- **`ELASTICSEARCH_QUICK_REFERENCE.md`** - Command reference

---

## 🎯 What's Next?

Your system is fully functional with:
- ✅ Real-time document search
- ✅ Auto-indexing on document changes
- ✅ Fast, unified search API
- ✅ Frontend integration

### Optional Future Enhancements (Phase 3):
- Integrate BOM Service with Elasticsearch
- Integrate Change Service with Elasticsearch
- Integrate Task Service with Elasticsearch
- Add advanced search features (filters, sorting, aggregations)

---

## 🆘 Need Help?

**Check Service Status:**
```powershell
# Elasticsearch
docker ps | Select-String "elasticsearch"

# Services
Test-NetConnection -ComputerName localhost -Port 8081 # Document Service
Test-NetConnection -ComputerName localhost -Port 8091 # Search Service
```

**View Logs:**
- Elasticsearch: `docker logs plm-elasticsearch`
- Kibana: `docker logs plm-kibana`
- Document Service: Check PowerShell window
- Search Service: Check PowerShell window

---

**🎉 Enjoy your new Elasticsearch-powered search!**




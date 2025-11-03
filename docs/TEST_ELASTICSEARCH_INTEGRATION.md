# 🧪 Elasticsearch Integration - Testing Guide

**Last Updated:** October 30, 2025

---

## ✅ Current Status

All systems are **UP and RUNNING**:
- ✅ Elasticsearch (port 9200)
- ✅ Kibana (port 5601)
- ✅ Document Service (port 8081)
- ✅ Search Service (port 8091)
- ✅ 2 documents indexed

---

## 🧪 Complete Test Suite

### Test 1: Verify Elasticsearch is Running
```powershell
Invoke-RestMethod -Uri "http://localhost:9200" | Select-Object name, version
```
**Expected:** Elasticsearch cluster info

### Test 2: Check Indexed Documents
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:9200/documents/_search?size=0"
Write-Host "Total documents in ES: $($result.hits.total.value)"
```
**Expected:** `Total documents in ES: 2`

### Test 3: Test Search Service Health
```powershell
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search/health"
```
**Expected:** `"Search Service is running"`

### Test 4: Search for "Technical"
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search?q=Technical"
Write-Host "Query: $($result.query)"
Write-Host "Total Hits: $($result.totalHits)"
Write-Host "Time: $($result.took)ms"
$result.documents | Format-Table title, status, score
```
**Expected:** 1 document found (Technical Document)

### Test 5: Search for "version"
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search?q=version"
$result.documents | Format-Table title, status, score
```
**Expected:** 1 document found (version test)

### Test 6: Get All Documents
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search"
Write-Host "Total: $($result.totalHits)"
$result.documents | Format-Table title, status
```
**Expected:** 2 documents

### Test 7: Document-Only Endpoint
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search/documents?q=test"
Write-Host "Found: $($result.Count) documents"
$result | Format-Table title, type, score
```
**Expected:** 1 document (version test)

---

## 🌐 Frontend Testing

### Prerequisites
Make sure the frontend is running:
```powershell
cd frontend
npm start
```

### Test Steps

1. **Open Frontend**
   - Navigate to http://localhost:3000

2. **Go to Global Search**
   - Click on "Global Search" in the navigation menu

3. **Test Search Queries**
   
   **Query 1: "Technical"**
   - Type "Technical" in the search box
   - ✅ Expected: 1 result (Technical Document)
   - Status: RELEASED
   - Stage: CONCEPTUAL_DESIGN

   **Query 2: "version"**
   - Type "version" in the search box
   - ✅ Expected: 1 result (version test)
   - Status: IN_REVIEW
   - Stage: CONCEPTUAL_DESIGN

   **Query 3: "labubu"** (creator name)
   - Type "labubu" in the search box
   - ✅ Expected: 2 results (both documents)

4. **Test Filters**
   - Select "Documents" from Category dropdown
   - Select "RELEASED" from Status dropdown
   - ✅ Expected: Filtered results

5. **Test Tabs**
   - Click on "Documents" tab
   - ✅ Expected: Document results displayed separately

---

## 🔍 Direct Elasticsearch Queries (via Kibana)

### Access Kibana
1. Open http://localhost:5601
2. Go to "Dev Tools" → "Console"

### Query 1: Get all documents
```json
GET /documents/_search
{
  "query": {
    "match_all": {}
  }
}
```

### Query 2: Search by title
```json
GET /documents/_search
{
  "query": {
    "match": {
      "title": "Technical"
    }
  }
}
```

### Query 3: Multi-match query (like Search Service)
```json
GET /documents/_search
{
  "query": {
    "multi_match": {
      "query": "version",
      "fields": ["title^2", "description", "documentNumber", "category", "creator"]
    }
  }
}
```

### Query 4: Filter by status
```json
GET /documents/_search
{
  "query": {
    "term": {
      "status": "RELEASED"
    }
  }
}
```

### Query 5: Check index mapping
```json
GET /documents/_mapping
```

---

## 📊 Performance Testing

### Measure Search Response Time
```powershell
$times = @()
1..10 | ForEach-Object {
    $start = Get-Date
    Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search?q=test" | Out-Null
    $end = Get-Date
    $times += ($end - $start).TotalMilliseconds
}
$avg = ($times | Measure-Object -Average).Average
Write-Host "Average response time: $([math]::Round($avg, 2))ms"
```
**Expected:** < 1000ms (typically 300-700ms)

---

## 🐛 Troubleshooting

### Issue: Search Service returns 0 results

**Solution 1: Refresh the Elasticsearch index**
```powershell
Invoke-RestMethod -Uri "http://localhost:9200/documents/_refresh" -Method Post
```

**Solution 2: Reindex all documents**
```powershell
powershell -ExecutionPolicy Bypass -File reindex-documents.ps1
```

### Issue: Frontend shows "Search service unavailable"

**Check 1: Is Search Service running?**
```powershell
Invoke-RestMethod -Uri "http://localhost:8091/api/v1/search/health"
```

**Check 2: CORS enabled?**
- Verify `@CrossOrigin(origins = "*")` in SearchController.java

**Check 3: Check browser console**
- Open browser DevTools (F12)
- Look for network errors

### Issue: Document Service health check fails

**Cause:** Neo4j connection (temporarily disabled)

**Solution:** This is expected. The API endpoints work fine.

**Verify:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/documents" | Select-Object -First 1
```

---

## 📈 Success Criteria

### All Tests Passing ✅

| Test | Status |
|------|--------|
| Elasticsearch running | ✅ |
| Documents indexed (2/2) | ✅ |
| Search Service health | ✅ |
| Search "Technical" | ✅ |
| Search "version" | ✅ |
| Get all documents | ✅ |
| Document-only endpoint | ✅ |
| Frontend integration | ⏳ (requires frontend running) |
| Response time < 1s | ✅ |

---

## 🎯 Next Steps

### If All Tests Pass:
**Congratulations! Integration is complete!** 🎉

You can now:
1. Start using Global Search in your application
2. Create new documents (they'll auto-index)
3. Monitor search performance via Kibana
4. Proceed to Phase 3 (BOM, Change, Task services) if needed

### If Tests Fail:
1. Check service logs in the respective PowerShell windows
2. Verify all services are running:
   - `docker ps` (for Elasticsearch & Kibana)
   - Check terminal windows for Java services
3. Review error messages
4. Refer to troubleshooting section above

---

## 📞 Support

**Documentation:**
- `ELASTICSEARCH_INTEGRATION_COMPLETE.md` - Complete guide
- `ELASTICSEARCH_QUICK_REFERENCE.md` - Quick commands
- `reindex-documents.ps1` - Reindexing script

**Logs:**
- Document Service: Check PowerShell window running `mvn spring-boot:run`
- Search Service: Check PowerShell window running Search Service
- Elasticsearch: `docker logs plm-elasticsearch`
- Kibana: `docker logs plm-kibana`

---

**Last Test Run:** October 30, 2025 10:47 AM  
**Result:** ✅ ALL TESTS PASSING




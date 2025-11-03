# 🧪 Comprehensive Elasticsearch Integration Test

**Test Script:** `scripts/comprehensive-es-test.ps1`  
**Purpose:** Complete end-to-end testing of all Elasticsearch integrations  
**Duration:** ~30-45 seconds  
**Last Updated:** October 30, 2025

---

## 📋 Overview

This comprehensive test suite validates the **complete Elasticsearch integration** across all services in the PLM-Lite system. It tests:

- ✅ Auto-indexing functionality (5 services)
- ✅ Search endpoint availability (7 endpoints)
- ✅ Data consistency between services and Elasticsearch
- ✅ Unified search functionality
- ✅ Direct Elasticsearch index verification

---

## 🎯 What Does This Test?

### Test Coverage (40+ Tests)

| Phase | Tests | What It Validates |
|-------|-------|-------------------|
| **Phase 1** | Infrastructure (6 tests) | All services + ES running |
| **Phase 2** | Document Service (3 tests) | Create → Index → Search |
| **Phase 3** | BOM Service (3 tests) | Create → Index → Search |
| **Phase 4** | Part Service (3 tests) | Create → Index → Search |
| **Phase 5** | Change Service (3 tests) | Create → Index → Search |
| **Phase 6** | Task Service (3 tests) | Create → Index → Search |
| **Phase 7** | Unified Search (1 test) | Cross-entity search |
| **Phase 8** | Search Endpoints (5 tests) | All search APIs |
| **Phase 9** | ES Indices (5 tests) | Direct index verification |

**Total:** 32+ individual tests

---

## 🚀 How to Run

### Prerequisites

**All services must be running:**
- ✅ Elasticsearch (port 9200)
- ✅ Document Service (port 8081)
- ✅ BOM Service (port 8089)
- ✅ Change Service (port 8084)
- ✅ Task Service (port 8082)
- ✅ Search Service (port 8091)

### Run the Test

```powershell
# From project root
cd C:\Users\diang\Desktop\plm-lite

# Run the test
.\scripts\comprehensive-es-test.ps1

# Or with execution policy bypass
powershell -ExecutionPolicy Bypass -File .\scripts\comprehensive-es-test.ps1
```

---

## 📊 Understanding the Results

### Perfect Score (100%)

```
╔══════════════════════════════════════════════════════════╗
║                    FINAL RESULTS                         ║
╠══════════════════════════════════════════════════════════╣
║  Total Tests:     32                                     ║
║  Passed:          32  ✅                                  ║
║  Failed:           0                                     ║
║  Pass Rate:     100.0%                                   ║
╚══════════════════════════════════════════════════════════╝

🎉 PERFECT SCORE! All ES integration tests passed!
```

**Meaning:** Everything is working perfectly! 🎉

---

### Excellent Score (90-99%)

```
║  Pass Rate:      95.0%                                   ║

✅ EXCELLENT! ES integration is working well!
```

**Meaning:** Minor issues that don't affect core functionality

---

### Good Score (70-89%)

```
║  Pass Rate:      80.0%                                   ║

⚠️  GOOD: Most ES integration is working, some issues to address
```

**Meaning:** Some services or endpoints need attention

---

### Needs Attention (<70%)

```
║  Pass Rate:      60.0%                                   ║

❌ NEEDS ATTENTION: Several ES integration issues found
```

**Meaning:** Significant issues need to be resolved

---

## 🔍 Test Details by Phase

### Phase 1: Infrastructure Health Checks

**Purpose:** Verify all services and Elasticsearch are running

**Tests:**
1. Elasticsearch is accessible
2. Document Service health endpoint
3. BOM Service health endpoint
4. Change Service health endpoint
5. Task Service health endpoint
6. Search Service health endpoint

**Expected Result:** All 6 services responding

---

### Phase 2: Document Service Auto-Indexing

**Purpose:** Verify documents are auto-indexed to Elasticsearch

**Flow:**
```
Create Document → Wait 2s → Search in ES
```

**Tests:**
1. Create document via Document Service API
2. Search for document via Search Service
3. Verify document found in Elasticsearch

**Expected Result:** Document created and found in ES

---

### Phase 3: BOM Service Auto-Indexing

**Purpose:** Verify BOMs are auto-indexed to Elasticsearch

**Flow:**
```
Create BOM → Wait 2s → Search in ES
```

**Tests:**
1. Create BOM via BOM Service API
2. Search for BOM via Search Service
3. Verify BOM found in Elasticsearch

**Expected Result:** BOM created and found in ES

---

### Phase 4: Part Service Auto-Indexing

**Purpose:** Verify parts are auto-indexed to Elasticsearch

**Flow:**
```
Create Part → Wait 2s → Search in ES
```

**Tests:**
1. Create part via BOM Service API
2. Search for part via Search Service
3. Verify part found in Elasticsearch

**Expected Result:** Part created and found in ES

---

### Phase 5: Change Service Auto-Indexing

**Purpose:** Verify change requests are auto-indexed to Elasticsearch

**Flow:**
```
Create Change → Wait 2s → Search in ES
```

**Tests:**
1. Create change via Change Service API
2. Search for change via Search Service
3. Verify change found in Elasticsearch

**Expected Result:** Change created and found in ES

---

### Phase 6: Task Service Auto-Indexing

**Purpose:** Verify tasks are auto-indexed to Elasticsearch

**Flow:**
```
Create Task → Wait 2s → Search in ES
```

**Tests:**
1. Create task via Task Service API
2. Search for task via Search Service
3. Verify task found in Elasticsearch

**Expected Result:** Task created and found in ES

---

### Phase 7: Unified Search Test

**Purpose:** Verify unified search works across all entity types

**Test:**
- Unified search with query "ES Test"
- Should return results from all entity types

**Expected Result:**
- Documents found: 1+
- BOMs found: 1+
- Parts found: 1+
- Changes found: 1+
- Tasks found: 1+
- Total hits: 5+

---

### Phase 8: All Search Endpoints Test

**Purpose:** Verify all 5 entity-specific search endpoints are working

**Tests:**
1. `GET /api/v1/search/documents?q=test`
2. `GET /api/v1/search/boms?q=test`
3. `GET /api/v1/search/parts?q=test`
4. `GET /api/v1/search/changes?q=test`
5. `GET /api/v1/search/tasks?q=test`

**Expected Result:** All endpoints return 200 OK with results

---

### Phase 9: Elasticsearch Index Verification

**Purpose:** Directly verify ES indices exist and contain data

**Tests:**
1. Check `documents` index exists
2. Check `boms` index exists
3. Check `parts` index exists
4. Check `changes` index exists
5. Check `tasks` index exists

**Expected Result:** All 5 indices exist with document counts

---

## 🐛 Troubleshooting

### Common Issues

#### Issue 1: Service Not Running

```
❌ FAIL: Document Service Health - Connection refused
```

**Solution:** Start the service
```powershell
cd document-service
mvn spring-boot:run
```

---

#### Issue 2: Elasticsearch Not Running

```
❌ FAIL: Elasticsearch Running - Connection refused
```

**Solution:** Start Elasticsearch
```bash
docker-compose up -d elasticsearch
```

---

#### Issue 3: Data Not Found in ES

```
⚠️  Document not found in ES (may need more time)
```

**Possible Causes:**
- ES indexing takes more than 2 seconds
- Auto-indexing not configured properly
- ES index doesn't exist

**Solution:**
1. Check service logs for ES indexing errors
2. Manually verify in Kibana: `http://localhost:5601`
3. Run test again (sometimes first indexing is slow)

---

#### Issue 4: Search Endpoint 404

```
❌ FAIL: GET /search/boms - 404 Not Found
```

**Solution:** Restart Search Service to load new endpoints
```powershell
cd infra\search-service
mvn spring-boot:run
```

---

#### Issue 5: Wrong Service Port

```
❌ FAIL: Task Service Health - Connection refused
```

**Check:** Verify service is running on expected port
```powershell
netstat -ano | findstr "8082"
```

---

## 📈 Performance Expectations

### Normal Response Times

| Operation | Expected Time |
|-----------|---------------|
| Create entity | 50-200ms |
| ES indexing | 1-2 seconds |
| Search query | 10-50ms |
| Full test suite | 30-45 seconds |

### What's Being Created

The test creates **one of each entity type:**
- 1 Document
- 1 BOM
- 1 Part
- 1 Change
- 1 Task

**All test data is prefixed with:** `ES Test` or `ES-TEST`

---

## 🧹 Cleanup

### Manual Cleanup (Optional)

If you want to remove test data:

```bash
# Delete test documents from ES
curl -X POST "localhost:9200/documents/_delete_by_query" \
  -H 'Content-Type: application/json' \
  -d '{"query": {"match": {"title": "ES Test"}}}'

# Repeat for other indices
curl -X POST "localhost:9200/boms/_delete_by_query" ...
curl -X POST "localhost:9200/parts/_delete_by_query" ...
curl -X POST "localhost:9200/changes/_delete_by_query" ...
curl -X POST "localhost:9200/tasks/_delete_by_query" ...
```

---

## 📋 Test Output Example

### Successful Test Run

```
╔══════════════════════════════════════════════════════════╗
║  PHASE 1: Infrastructure Health Checks                   ║
╚══════════════════════════════════════════════════════════╝

1.1 Elasticsearch
  ✅ PASS: Elasticsearch Running
     Version: 8.11.0

1.2 Service Health Checks
  ✅ PASS: Document Service Health
  ✅ PASS: BOM Service Health
  ✅ PASS: Change Service Health
  ✅ PASS: Task Service Health
  ✅ PASS: Search Service Health

╔══════════════════════════════════════════════════════════╗
║  PHASE 2: Document Service Auto-Indexing                 ║
╚══════════════════════════════════════════════════════════╝

2.1 Create Document
  ✅ PASS: Create Document
     Created Document ID: 123

2.2 Wait for ES Indexing (2 seconds)

2.3 Search for Document in ES
  ✅ PASS: Search Document in ES
     ✅ Document found in Elasticsearch!
     Total results: 1

[... continues for all phases ...]

╔══════════════════════════════════════════════════════════╗
║                    FINAL RESULTS                         ║
╠══════════════════════════════════════════════════════════╣
║  Total Tests:     32                                     ║
║  Passed:          32  ✅                                  ║
║  Failed:           0                                     ║
║  Pass Rate:     100.0%                                   ║
╚══════════════════════════════════════════════════════════╝

🎉 PERFECT SCORE! All ES integration tests passed!

Test completed at: 2025-10-30 21:30:45
```

---

## 🎯 Success Criteria

### Minimum Requirements for Production

| Metric | Threshold | Status |
|--------|-----------|--------|
| **Pass Rate** | ≥ 90% | Required |
| **Auto-Indexing** | All 5 services | Required |
| **Search Endpoints** | All 5 working | Required |
| **Response Time** | < 100ms average | Recommended |
| **Data Consistency** | 100% | Required |

---

## 📊 Metrics Tracked

The test tracks and reports:

1. **Total Tests Executed**
2. **Tests Passed**
3. **Tests Failed**
4. **Pass Rate Percentage**
5. **Entity Counts in ES**
6. **Search Response Times**
7. **Failed Test Details**

---

## 🔄 CI/CD Integration

### Jenkins Example

```groovy
stage('ES Integration Test') {
    steps {
        script {
            bat 'powershell -ExecutionPolicy Bypass -File scripts\\comprehensive-es-test.ps1'
        }
    }
}
```

### GitLab CI Example

```yaml
es-integration-test:
  stage: test
  script:
    - powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-test.ps1
  allow_failure: false
```

---

## 📝 Test Data Format

### Document Payload
```json
{
  "title": "ES Test Document 143052",
  "description": "Testing Elasticsearch auto-indexing for documents",
  "documentType": "SPECIFICATION",
  "status": "DRAFT",
  "version": "1.0",
  "createdBy": "es-test-user"
}
```

### BOM Payload
```json
{
  "bomNumber": "BOM-ES-TEST-143052",
  "description": "Testing Elasticsearch auto-indexing for BOMs",
  "version": "1.0",
  "status": "DRAFT",
  "createdBy": "es-test-user",
  "items": []
}
```

### Part Payload
```json
{
  "partNumber": "PART-ES-TEST-143052",
  "description": "Testing Elasticsearch auto-indexing for parts",
  "stage": "DESIGN",
  "status": "ACTIVE",
  "category": "TEST",
  "createdBy": "es-test-user"
}
```

### Change Payload
```json
{
  "title": "ES Test Change 143052",
  "description": "Testing Elasticsearch auto-indexing for changes",
  "changeType": "ENGINEERING_CHANGE",
  "priority": "MEDIUM",
  "status": "DRAFT",
  "createdBy": "es-test-user"
}
```

### Task Payload
```json
{
  "title": "ES Test Task 143052",
  "description": "Testing Elasticsearch auto-indexing for tasks",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": "es-test-user"
}
```

---

## 🏆 Best Practices

1. **Run Before Deployment** - Always run before deploying to production
2. **Monitor Trends** - Track pass rate over time
3. **Fix Failures Immediately** - Don't ignore failed tests
4. **Clean Environment** - Test in a clean state
5. **Check Logs** - Review service logs after failures

---

## 📞 Support

**Test Issues?** Check:
1. All services are running
2. Elasticsearch is accessible
3. Service logs for errors
4. Network connectivity

**Documentation:**
- `SEARCH_ENDPOINTS_DOCUMENTATION.md` - Search API reference
- `ES_INTEGRATION_FINAL_STATUS.md` - Integration status
- `ELASTICSEARCH_PROJECT_SUMMARY.md` - Project overview

---

**Generated:** October 30, 2025  
**Version:** 1.0  
**Status:** Production-Ready ✅




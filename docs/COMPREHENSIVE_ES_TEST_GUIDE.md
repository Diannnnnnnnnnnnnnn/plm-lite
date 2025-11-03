# Comprehensive Elasticsearch Integration Test Guide

## Overview

This document describes the comprehensive test suite for validating Elasticsearch integration across all 5 PLM services.

**Test Script:** `scripts/comprehensive-es-integration-test.ps1`

---

## What Does It Test?

### 📊 Complete Coverage

The test suite validates **10 critical areas**:

1. **Infrastructure** - ES, Kibana, all 6 services
2. **Document Service** - Create, Update, Search, ES indexing
3. **Part Service** - Create, Update, Search, ES indexing
4. **Task Service** - Create, Update, Search, ES indexing
5. **Change Service** - ES index validation, search endpoints
6. **BOM Service** - Create, Search, ES indexing
7. **Unified Search** - Cross-index search, performance
8. **Performance** - Response times, concurrent queries
9. **Data Consistency** - SQL ↔ ES synchronization
10. **Cleanup** - Resource cleanup after tests

### 🎯 Test Statistics

- **Total Test Cases:** 60+ automated tests
- **Services Tested:** 6 (Document, BOM, Part, Change, Task, Search)
- **Indices Validated:** 5 (documents, boms, parts, changes, tasks)
- **Operations Tested:** Create, Read, Update, Delete, Search
- **Performance Benchmarks:** Yes
- **Consistency Checks:** Yes
- **Reports Generated:** JSON + HTML

---

## Prerequisites

### Required Services Running

```powershell
# 1. Start Elasticsearch
docker-compose -f docker-compose-elasticsearch.yml up -d

# 2. Start all PLM services
# Document Service (Port 8081)
# BOM Service (Port 8089)
# Change Service (Port 8084)
# Task Service (Port 8083)
# Search Service (Port 8091)
```

### System Requirements

- PowerShell 5.1 or higher
- Network access to all services
- Write permissions for test reports directory

---

## How to Run

### Basic Usage

```powershell
# Navigate to project root
cd C:\Users\diang\Desktop\plm-lite

# Run the comprehensive test
powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-integration-test.ps1
```

### Advanced Options

```powershell
# Skip cleanup (preserve test data for inspection)
.\scripts\comprehensive-es-integration-test.ps1 -SkipCleanup

# Verbose output (show detailed information)
.\scripts\comprehensive-es-integration-test.ps1 -Verbose

# Custom report path
.\scripts\comprehensive-es-integration-test.ps1 -ReportPath "C:\my-reports"

# Combine options
.\scripts\comprehensive-es-integration-test.ps1 -Verbose -SkipCleanup -ReportPath ".\reports"
```

---

## Test Phases Explained

### Phase 1: Infrastructure Tests

**What it tests:**
- ✅ Elasticsearch cluster health
- ✅ Kibana accessibility
- ✅ All 6 microservices running
- ✅ Elasticsearch indices existence
- ✅ Index document counts

**Example Output:**
```
[PASS] Elasticsearch is accessible
       Cluster: plm-cluster, Status: green, Nodes: 1
[PASS] Document Service is running
[PASS] Index 'documents' exists
       Docs: 2, Size: 15.2kb
```

---

### Phase 2: Document Service Tests

**What it tests:**
1. **Create Document**
   - Document creation via API
   - Auto-indexing to ES
   - Data accuracy in ES
   - Field mapping validation

2. **Update Document**
   - Document update via API
   - Re-indexing after update
   - Updated data in ES

3. **Search Documents**
   - Search endpoint functionality
   - Result accuracy

**Example Output:**
```
[PASS] Document created via API
       Document ID: doc-123abc
[PASS] Document auto-indexed to ES
[PASS] Document title matches in ES
[PASS] Document stage matches in ES
[PASS] Document updated via API
[PASS] Document re-indexed after update
```

---

### Phase 3: Part Service Tests

**What it tests:**
1. **Create Part**
   - Part creation via API
   - Auto-indexing to ES
   - Field validation in ES

2. **Update Part**
   - Stage update via API
   - Re-indexing after update
   - Correct stage in ES

3. **Search Parts**
   - Direct ES search
   - Result validation

**Example Output:**
```
[PASS] Part created via API
       Part ID: part-xyz789
[PASS] Part auto-indexed to ES
[PASS] Part stage updated via API
[PASS] Part re-indexed after update
       Stage in ES: PRODUCTION
```

---

### Phase 4: Task Service Tests

**What it tests:**
1. **Create Task**
   - Task creation via API
   - Auto-indexing to ES
   - Data validation

2. **Update Task**
   - Task update via API
   - Re-indexing after update
   - Name and status updates

3. **Search Tasks**
   - ES search functionality
   - Result validation

**Example Output:**
```
[PASS] Task created via API
       Task ID: 42
[PASS] Task auto-indexed to ES
[PASS] Task updated via API
[PASS] Task re-indexed after update
       Name updated in ES
```

---

### Phase 5: Change Service Tests

**What it tests:**
- Changes index accessibility
- Index queryability
- Document structure validation
- Search endpoint availability

**Note:** Creating changes requires released documents, so this phase focuses on validation rather than full CRUD testing.

**Example Output:**
```
[PASS] Changes index is accessible
[PASS] Changes index is queryable
       Total changes indexed: 5
       Sample change: ECO-2024-001
[PASS] Change document has required fields
[PASS] Change Service ES search endpoint exists
```

---

### Phase 6: BOM Service Tests

**What it tests:**
1. **Create BOM**
   - BOM creation via API
   - Auto-indexing to ES
   - Title validation

2. **Search BOMs**
   - ES search functionality
   - Result accuracy

**Example Output:**
```
[PASS] BOM created via API
       BOM ID: bom-456def
[PASS] BOM auto-indexed to ES
[PASS] BOM title matches in ES
[PASS] BOM search in ES works
```

---

### Phase 7: Unified Search Tests

**What it tests:**
- Unified search endpoint
- Multi-index search
- Response structure
- Response time
- Document-only search
- Edge cases (empty queries, special chars)

**Example Output:**
```
[PASS] Unified search endpoint works
       Query: 'test'
       Total Hits: 15
       Response Time: 28ms
       Documents found: 5
       Parts found: 3
       BOMs found: 2
[PASS] Unified search response time acceptable
[PASS] Document-only search endpoint works
[PASS] Empty query handled gracefully
[PASS] Special characters in query handled
```

---

### Phase 8: Performance Tests

**What it tests:**
- Average ES query response time
- Maximum response time
- Minimum response time
- Concurrent query handling (5 simultaneous)

**Benchmarks:**
- Average: < 100ms ✅
- Maximum: < 500ms ✅
- Concurrent: All succeed ✅

**Example Output:**
```
[PASS] Average ES query time acceptable
       Average: 32.45ms
       Min: 18.23ms, Max: 67.89ms
[PASS] Max ES query time acceptable
[PASS] Concurrent queries successful
       5/5 queries succeeded
```

---

### Phase 9: Data Consistency Tests

**What it tests:**
- SQL database count vs ES count
- Individual document field consistency
- Title, stage, status matching

**Acceptable Variance:** ±2 documents (due to indexing timing)

**Example Output:**
```
[PASS] Document count consistency
       SQL Documents: 7, ES Documents: 7
[PASS] Document title consistency
       Title matches between SQL and ES
```

---

### Phase 10: Cleanup

**What it does:**
- Prompts user for cleanup confirmation
- Deletes all test documents
- Deletes all test parts
- Deletes all test tasks
- Deletes all test BOMs
- Removes data from both SQL and ES

**Options:**
- Interactive: Choose Y/N when prompted
- Skip: Use `-SkipCleanup` flag

---

## Understanding Test Results

### Console Output

The test displays real-time results:

```
[PASS] Test Name                       <- Green: Test passed
[FAIL] Test Name                       <- Red: Test failed
       Error: Detailed error message   <- Yellow: Error details
       Expected: X, Got: Y             <- Yellow: Mismatch info
```

### Exit Codes

- **0** = All tests passed
- **> 0** = Number of failed tests (e.g., 3 = 3 tests failed)

### Test Reports

Two report files are generated:

#### 1. JSON Report
**Location:** `test-reports/es-integration-test-YYYYMMDD-HHmmss.json`

**Contents:**
```json
{
  "timestamp": "2025-10-30 14:35:22",
  "duration": 45.67,
  "totalTests": 62,
  "passedTests": 60,
  "failedTests": 2,
  "skippedTests": 0,
  "successRate": 96.8,
  "resources": {
    "documents": ["doc-123", "doc-456"],
    "parts": ["part-789"],
    "tasks": [42, 43],
    "boms": ["bom-abc"]
  },
  "results": [...]
}
```

#### 2. HTML Report
**Location:** `test-reports/es-integration-test-YYYYMMDD-HHmmss.html`

**Features:**
- Visual dashboard with metrics
- Color-coded results table
- Pass/fail status for each test
- Expected vs actual values
- Error messages
- Sortable, filterable table

**Open in browser:**
```powershell
start test-reports\es-integration-test-YYYYMMDD-HHmmss.html
```

---

## Success Criteria

### Production Ready ✅

Tests are considered passing if:

| Metric | Threshold | Status |
|--------|-----------|--------|
| **Success Rate** | ≥ 95% | ✅ |
| **Infrastructure** | 100% | ✅ |
| **CRUD Operations** | ≥ 90% | ✅ |
| **Search Tests** | ≥ 90% | ✅ |
| **Performance** | Avg < 100ms | ✅ |
| **Consistency** | ≥ 95% | ✅ |

### Acceptable ⚠️

Minor issues, needs review:

| Metric | Threshold |
|--------|-----------|
| **Success Rate** | 80-94% |
| **Some services** | Not running |

### Failing ❌

Requires immediate attention:

| Metric | Threshold |
|--------|-----------|
| **Success Rate** | < 80% |
| **Infrastructure** | ES not running |
| **Critical failures** | Multiple |

---

## Troubleshooting

### Common Issues

#### 1. Elasticsearch Not Running

**Symptom:**
```
[FAIL] Elasticsearch is accessible
       Error: Connection refused
```

**Solution:**
```powershell
docker-compose -f docker-compose-elasticsearch.yml up -d
# Wait 30 seconds for ES to start
```

#### 2. Service Not Running

**Symptom:**
```
[FAIL] Document Service is running
       Error: Cannot connect
```

**Solution:**
```powershell
cd document-service
mvn spring-boot:run
```

#### 3. Indexing Failures

**Symptom:**
```
[FAIL] Document auto-indexed to ES
       Expected: 1, Got: 0
```

**Possible Causes:**
- ES is slow to index (wait longer)
- Service ES integration disabled
- Network issues

**Solution:**
```powershell
# Check service logs for ES errors
# Verify ES configuration in application.yml
# Manually check ES: curl http://localhost:9200/documents/_search
```

#### 4. Performance Tests Failing

**Symptom:**
```
[FAIL] Average ES query time acceptable
       Expected: <100ms, Got: 250ms
```

**Possible Causes:**
- ES under load
- Large dataset
- Slow hardware

**Solution:**
- This is often acceptable in test environments
- Monitor production performance separately

#### 5. Consistency Tests Failing

**Symptom:**
```
[FAIL] Document count consistency
       SQL: 10, ES: 5
```

**Possible Causes:**
- Indexing lag
- Previous indexing failures

**Solution:**
```powershell
# Reindex all documents
powershell -File scripts/reindex-all-documents.ps1
```

---

## Continuous Integration (CI)

### Running in CI/CD Pipeline

```yaml
# Example GitHub Actions
name: ES Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Start Elasticsearch
        run: docker-compose -f docker-compose-elasticsearch.yml up -d
      
      - name: Wait for ES
        run: Start-Sleep -Seconds 30
      
      - name: Start Services
        run: |
          # Start all services (parallel or sequential)
      
      - name: Run Tests
        run: |
          powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-integration-test.ps1 -SkipCleanup
      
      - name: Upload Reports
        uses: actions/upload-artifact@v2
        with:
          name: test-reports
          path: test-reports/
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    stages {
        stage('Start ES') {
            steps {
                bat 'docker-compose -f docker-compose-elasticsearch.yml up -d'
                sleep 30
            }
        }
        stage('Run Tests') {
            steps {
                bat 'powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-integration-test.ps1'
            }
        }
        stage('Archive Reports') {
            steps {
                archiveArtifacts 'test-reports/**'
            }
        }
    }
}
```

---

## Interpreting Results

### Example: All Tests Pass

```
=== TEST SUMMARY ===
Total Tests: 62
Passed: 62
Failed: 0
Skipped: 0
Success Rate: 100%

========================================
 ALL TESTS PASSED!
 ES Integration is PRODUCTION READY
========================================
```

**Action:** ✅ Deploy to production

---

### Example: Minor Failures

```
=== TEST SUMMARY ===
Total Tests: 62
Passed: 58
Failed: 4
Success Rate: 93.5%

========================================
 MOST TESTS PASSED
 Review failures above
========================================
```

**Action:** 
1. Review failed tests
2. Determine if failures are critical
3. Fix if needed or document as known issues
4. May still be production-ready depending on failures

---

### Example: Major Failures

```
=== TEST SUMMARY ===
Total Tests: 62
Passed: 40
Failed: 22
Success Rate: 64.5%

========================================
 MULTIPLE TESTS FAILED
 ES Integration needs attention
========================================
```

**Action:** ❌ Do NOT deploy
1. Review all failures
2. Check service logs
3. Fix integration issues
4. Re-run tests

---

## Best Practices

### Before Running Tests

1. ✅ Ensure all services are running
2. ✅ Check Elasticsearch health
3. ✅ Clear previous test data (optional)
4. ✅ Check disk space for reports

### During Tests

1. ✅ Don't interrupt the test run
2. ✅ Monitor console output
3. ✅ Note any warnings or errors

### After Tests

1. ✅ Review HTML report in detail
2. ✅ Compare with previous test runs
3. ✅ Archive reports for history
4. ✅ Clean up test data if not using `-SkipCleanup`

### Regular Testing

- **Daily:** Run during development
- **Pre-commit:** Run before major commits
- **Pre-deployment:** Always run before deploying
- **Production monitoring:** Run in staging environment

---

## Test Customization

### Modifying Test Parameters

Edit the script to customize:

```powershell
# Line 26-31: Service URLs
$ES_URL = "http://localhost:9200"
$DOC_SERVICE = "http://localhost:8081"
# ... change ports if needed

# Line 105-107: Indexing wait time
function Wait-ForIndexing {
    param([int]$Seconds = 2)  # Increase if indexing is slow
}

# Line 877-884: Performance test iterations
$iterations = 10  # Increase for more accurate benchmarks
```

### Adding Custom Tests

Add new test functions following the pattern:

```powershell
function Test-CustomFeature {
    Write-TestSection "Custom Feature"
    
    # Your test code
    $result = Invoke-APICall -Uri "your-endpoint"
    Test-Assert "Custom test name" $result.Success
}

# Add to main execution (line ~1050)
Test-CustomFeature
```

---

## Support

### Getting Help

1. **Check this guide first**
2. **Review test output and reports**
3. **Check service logs**
4. **Examine ES indices directly:**
   ```bash
   curl http://localhost:9200/_cat/indices?v
   curl http://localhost:9200/documents/_search?pretty
   ```

### Common Commands

```powershell
# Check ES health
curl http://localhost:9200/_cluster/health

# List all indices
curl http://localhost:9200/_cat/indices?v

# Count documents in index
curl http://localhost:9200/documents/_count

# View recent test reports
dir test-reports\ -Sort LastWriteTime | Select-Object -Last 5

# Open latest HTML report
start (dir test-reports\*.html | Sort-Object LastWriteTime | Select-Object -Last 1).FullName
```

---

## Summary

✅ **Comprehensive test coverage** across all 5 services  
✅ **60+ automated tests** with detailed assertions  
✅ **Performance benchmarks** included  
✅ **Consistency validation** between SQL and ES  
✅ **Detailed reporting** in JSON and HTML formats  
✅ **Production-ready** validation criteria  
✅ **Easy to run** with simple command  

**This test suite gives you confidence that your Elasticsearch integration is working correctly and is ready for production use!** 🚀

---

**Last Updated:** October 30, 2025  
**Test Script Version:** 1.0  
**Compatible With:** PLM-Lite Elasticsearch Integration v1.0


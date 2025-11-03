# Elasticsearch Integration - Quick Start Guide

## Overview

This guide will help you quickly get started with Elasticsearch integration in the PLM-Lite system. Follow these steps to enable search capabilities across all your PLM entities.

## Prerequisites

Before you begin, ensure you have:

- ✅ Docker Desktop installed and running
- ✅ All PLM services configured (MySQL, Neo4j, etc.)
- ✅ At least 4GB RAM available for Docker

## Step 1: Start Elasticsearch (5 minutes)

### Option A: Using the Startup Script (Recommended)

1. Open Command Prompt in the project root directory
2. Run the startup script:
   ```batch
   start-elasticsearch.bat
   ```

3. Wait for the script to complete (30-60 seconds)
4. Verify installation by visiting:
   - Elasticsearch: http://localhost:9200
   - Kibana: http://localhost:5601

### Option B: Manual Docker Compose

```batch
docker-compose -f docker-compose-elasticsearch.yml up -d
```

### Verify Installation

Open your browser and go to http://localhost:9200. You should see:

```json
{
  "name" : "plm-elasticsearch",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "8.11.0",
    ...
  }
}
```

## Step 2: Enable Elasticsearch in Document Service (10 minutes)

This is the first service we'll integrate as a proof-of-concept.

### 2.1 Update Application Configuration

Edit `document-service/src/main/resources/application.properties` and add:

```properties
# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.connection-timeout=5s
spring.elasticsearch.socket-timeout=30s

# Optional: Disable in dev mode
# spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
```

### 2.2 Remove Elasticsearch from Exclude List

The Elasticsearch dependency is already added in the pom.xml. The integration is just commented out.

In `DocumentServiceImpl.java`, uncomment the search sync (around line 79-86):

**Before:**
```java
// TODO: Re-enable search when search-service is available
/*
try {
    searchGateway.index(d);
} catch (Exception e) {
    log.warn("Failed to sync to search service: {}", e.getMessage());
}
*/
```

**After:**
```java
// Sync to search service
try {
    searchGateway.index(d);
} catch (Exception e) {
    log.warn("Failed to sync to search service: {}", e.getMessage());
}
```

### 2.3 Restart Document Service

```batch
cd document-service
mvn clean spring-boot:run
```

Watch the logs for:
```
✅ Document indexed to Elasticsearch
```

## Step 3: Test Basic Search (5 minutes)

### 3.1 Create Test Data

Use your existing PLM frontend or API to create a few test documents.

### 3.2 Verify Indexing

Check Elasticsearch indices:
```bash
curl http://localhost:9200/_cat/indices?v
```

You should see a `documents` index.

### 3.3 Test Search Query

```bash
# Search for documents
curl http://localhost:9200/documents/_search?q=title:test

# Or using Kibana Dev Tools (http://localhost:5601)
GET /documents/_search
{
  "query": {
    "match": {
      "title": "test"
    }
  }
}
```

## Step 4: Use Kibana for Monitoring (5 minutes)

### 4.1 Access Kibana

Open http://localhost:5601 in your browser.

### 4.2 Explore Data

1. Go to **Management** → **Dev Tools**
2. Try these queries:

```json
# Check cluster health
GET /_cluster/health

# View all indices
GET /_cat/indices?v

# Search documents
GET /documents/_search
{
  "query": {
    "match_all": {}
  }
}

# Search with filters
GET /documents/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "specification" } }
      ],
      "filter": [
        { "term": { "status": "RELEASED" } }
      ]
    }
  }
}
```

### 4.3 Create Index Pattern

1. Go to **Management** → **Stack Management** → **Index Patterns**
2. Create a pattern: `documents*`
3. Select `createTime` as the time field
4. Go to **Discover** to explore your documents visually

## Step 5: Integrate Remaining Services (Optional)

Once document-service is working, you can integrate other services following the same pattern:

### Quick Reference

| Service | Status | Priority | Estimated Time |
|---------|--------|----------|----------------|
| Document Service | ✅ Done | High | Complete |
| Change Service | 🟡 Partial | High | 2-3 hours |
| Task Service | 🟡 Partial | Medium | 2-3 hours |
| BOM Service | ❌ Not Started | High | 3-4 hours |
| User Service | ❌ Not Started | Low | 2-3 hours |

## Common Issues & Solutions

### Issue 1: Elasticsearch won't start

**Solution:**
```batch
# Check if port 9200 is already in use
netstat -ano | findstr :9200

# Kill the process if needed
taskkill /F /PID <process_id>

# Or restart Docker Desktop
```

### Issue 2: Connection refused

**Symptoms:** `Connection refused` errors in service logs

**Solution:**
1. Verify Elasticsearch is running: `curl http://localhost:9200`
2. Check Docker container status: `docker ps | findstr elasticsearch`
3. Check service configuration has correct URI

### Issue 3: Documents not being indexed

**Solution:**
1. Check service logs for errors
2. Verify `sync()` method is being called
3. Check Elasticsearch logs: `docker logs plm-elasticsearch`
4. Try manual reindex (see below)

### Issue 4: Out of memory

**Symptoms:** Elasticsearch crashes or becomes unresponsive

**Solution:**
1. Increase Docker memory limit (Docker Desktop → Settings → Resources)
2. Adjust ES heap size in `docker-compose-elasticsearch.yml`:
   ```yaml
   - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase from 512m
   ```

## Manual Reindexing

If you need to reindex existing data:

### For Document Service

```java
// Add this endpoint to DocumentController
@PostMapping("/admin/reindex")
public ResponseEntity<String> reindexAllDocuments() {
    List<Document> allDocs = documentService.getAllDocuments();
    int count = 0;
    for (Document doc : allDocs) {
        try {
            searchGateway.index(doc);
            count++;
        } catch (Exception e) {
            log.error("Failed to index document {}: {}", doc.getId(), e.getMessage());
        }
    }
    return ResponseEntity.ok("Reindexed " + count + " documents");
}
```

Then call:
```bash
curl -X POST http://localhost:8081/api/v1/documents/admin/reindex
```

## Performance Tips

### 1. Bulk Indexing

For large datasets, use bulk operations:

```java
searchRepository.saveAll(documentList);
```

### 2. Async Indexing

Don't block the main thread:

```java
@Async
public void indexDocumentAsync(Document doc) {
    searchRepository.save(mapToIndex(doc));
}
```

### 3. Index Settings

Optimize for search speed:

```json
PUT /documents/_settings
{
  "index": {
    "refresh_interval": "30s",
    "number_of_replicas": 0
  }
}
```

## Useful Commands

### Elasticsearch Operations

```bash
# Check cluster health
curl http://localhost:9200/_cluster/health

# List all indices
curl http://localhost:9200/_cat/indices?v

# Get index mapping
curl http://localhost:9200/documents/_mapping

# Delete an index (careful!)
curl -X DELETE http://localhost:9200/documents

# Count documents in index
curl http://localhost:9200/documents/_count
```

### Docker Operations

```bash
# View Elasticsearch logs
docker logs -f plm-elasticsearch

# View Kibana logs
docker logs -f plm-kibana

# Restart Elasticsearch
docker restart plm-elasticsearch

# Check resource usage
docker stats plm-elasticsearch
```

## Next Steps

After completing this quickstart:

1. ✅ Review the full [ELASTICSEARCH_INTEGRATION_PLAN.md](./ELASTICSEARCH_INTEGRATION_PLAN.md)
2. ✅ Integrate Change Service (already has most code)
3. ✅ Integrate Task Service (already has most code)
4. ✅ Add BOM Service integration
5. ✅ Build unified search UI
6. ✅ Implement search analytics

## Support & Resources

### Documentation
- [Full Integration Plan](./ELASTICSEARCH_INTEGRATION_PLAN.md)
- [Elasticsearch Official Docs](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Spring Data Elasticsearch](https://docs.spring.io/spring-data/elasticsearch/reference/)

### Troubleshooting
- Check service logs in respective windows
- Review Elasticsearch logs: `docker logs plm-elasticsearch`
- Use Kibana Dev Tools for query testing
- Monitor cluster health in Kibana

### Getting Help
- Review error messages in service logs
- Check Elasticsearch cluster health
- Verify network connectivity between services
- Test queries in Kibana Dev Tools

---

**Last Updated:** 2025-10-29  
**Version:** 1.0  
**Status:** Ready to Use

**Estimated Time to Complete:** 30-45 minutes  
**Difficulty:** Intermediate





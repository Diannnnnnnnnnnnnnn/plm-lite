# Starting Elasticsearch with Docker - Quick Guide

## 🐳 Running Elasticsearch in Docker

### Prerequisites
- Docker Desktop installed and running
- At least 4GB RAM allocated to Docker

### Quick Start

#### Option 1: Using the Startup Script (Recommended)
```batch
start-elasticsearch.bat
```

This will:
- Start Elasticsearch on port 9200
- Start Kibana on port 5601
- Set up proper networking
- Configure health checks

#### Option 2: Manual Docker Compose
```batch
docker-compose -f docker-compose-elasticsearch.yml up -d
```

### Verify Installation

**1. Check Elasticsearch:**
```bash
curl http://localhost:9200
```

Expected response:
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

**2. Check Kibana:**
Open browser: http://localhost:5601

**3. Check Docker containers:**
```batch
docker ps
```

Should show:
```
CONTAINER ID   IMAGE                                                  STATUS
abc123...      docker.elastic.co/elasticsearch/elasticsearch:8.11.0   Up 2 minutes (healthy)
def456...      docker.elastic.co/kibana/kibana:8.11.0                Up 2 minutes (healthy)
```

### Configuration Details

**Docker Compose Configuration:**
- **File:** `docker-compose-elasticsearch.yml`
- **Network:** `plm-network` (bridge)
- **Volumes:** `elasticsearch-data` (persisted)

**Elasticsearch Settings:**
- Port: 9200 (REST API)
- Port: 9300 (Node communication)
- Mode: Single-node (development)
- Security: Disabled (for development)
- Heap Size: 512MB (adjustable)

**Kibana Settings:**
- Port: 5601
- Connects to: http://elasticsearch:9200 (internal Docker network)

### Stopping Elasticsearch

**Option 1: Using the Stop Script**
```batch
stop-elasticsearch.bat
```

**Option 2: Manual**
```batch
docker-compose -f docker-compose-elasticsearch.yml down
```

**To remove data volumes:**
```batch
docker-compose -f docker-compose-elasticsearch.yml down -v
```

### Troubleshooting Docker Setup

#### Problem: Docker not running
**Error:** `error during connect: This error may indicate that the docker daemon is not running`

**Solution:**
1. Open Docker Desktop
2. Wait for it to start
3. Verify: `docker ps`
4. Retry startup script

#### Problem: Port already in use
**Error:** `Bind for 0.0.0.0:9200 failed: port is already allocated`

**Solution:**
```batch
# Find process using port 9200
netstat -ano | findstr :9200

# Stop existing Elasticsearch
docker stop plm-elasticsearch

# Or stop conflicting process
taskkill /F /PID <process_id>
```

#### Problem: Out of memory
**Error:** Elasticsearch crashes or becomes unresponsive

**Solution 1: Increase Docker Memory**
1. Open Docker Desktop
2. Go to Settings → Resources
3. Increase Memory to 4GB or more
4. Click "Apply & Restart"

**Solution 2: Reduce Elasticsearch Heap**
Edit `docker-compose-elasticsearch.yml`:
```yaml
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # Reduce from 512m
```

#### Problem: Container won't start
**Check logs:**
```batch
docker logs plm-elasticsearch
```

**Common issues:**
- Insufficient memory
- Port conflict
- Previous container not removed

**Clean restart:**
```batch
# Stop and remove containers
docker-compose -f docker-compose-elasticsearch.yml down

# Remove old containers if any
docker rm -f plm-elasticsearch plm-kibana

# Start fresh
start-elasticsearch.bat
```

### Accessing Elasticsearch from Services

Your Spring Boot services will connect using:
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

**Note:** Use `localhost:9200` (not the internal Docker hostname) because your services run outside Docker.

### Useful Docker Commands

```bash
# View container logs
docker logs plm-elasticsearch
docker logs plm-kibana

# Follow logs in real-time
docker logs -f plm-elasticsearch

# Check container stats (CPU, memory)
docker stats plm-elasticsearch

# Restart container
docker restart plm-elasticsearch

# Execute command in container
docker exec -it plm-elasticsearch bash

# View container network
docker network inspect plm-network

# Check volume data
docker volume inspect elasticsearch-data
```

### Health Checks

Both containers have health checks configured:

**Elasticsearch:**
```bash
curl http://localhost:9200/_cluster/health
```

**Kibana:**
```bash
curl http://localhost:5601/api/status
```

### Data Persistence

Data is stored in Docker volume: `elasticsearch-data`

**To backup data:**
```bash
# Create snapshot
docker exec plm-elasticsearch \
  curl -X PUT "localhost:9200/_snapshot/my_backup" \
  -H 'Content-Type: application/json' \
  -d'{"type": "fs", "settings": {"location": "/usr/share/elasticsearch/backup"}}'
```

**To view volume location:**
```bash
docker volume inspect elasticsearch-data
```

### Production Considerations

**For production deployment:**

1. **Enable Security:**
```yaml
environment:
  - xpack.security.enabled=true
  - ELASTIC_PASSWORD=your_secure_password
```

2. **Increase Resources:**
```yaml
environment:
  - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
```

3. **Add Multiple Nodes:**
```yaml
services:
  elasticsearch-01:
    ...
  elasticsearch-02:
    ...
  elasticsearch-03:
    ...
```

4. **Configure SSL/TLS:**
```yaml
environment:
  - xpack.security.enabled=true
  - xpack.security.http.ssl.enabled=true
```

5. **Set up Monitoring:**
```yaml
environment:
  - xpack.monitoring.enabled=true
```

### Service Integration

**Once Elasticsearch is running in Docker:**

1. **Update service configs:** (Already configured in our setup)
```properties
spring.elasticsearch.uris=http://localhost:9200
```

2. **Start your services:**
```batch
start-all-services-with-search.bat
```

3. **Verify connection:**
Check service logs for:
```
✅ Connected to Elasticsearch cluster
```

### Performance Tuning

**For better performance:**

```yaml
# In docker-compose-elasticsearch.yml
environment:
  - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase heap
  - bootstrap.memory_lock=true     # Lock memory
  - indices.memory.index_buffer_size=30%  # Index buffer
```

**Docker resources:**
- Allocate at least 4GB RAM to Docker
- Allocate at least 2 CPUs
- Use SSD for better I/O performance

### Monitoring with Kibana

**After starting, access Kibana at http://localhost:5601**

1. **Dev Tools** - Query Elasticsearch directly
2. **Stack Monitoring** - View cluster health
3. **Discover** - Explore your data
4. **Dashboard** - Create visualizations

### Next Steps

After Elasticsearch is running:

1. ✅ **Start your services:** `start-all-services-with-search.bat`
2. ✅ **Reindex data:** `reindex-all-elasticsearch.bat`
3. ✅ **Test search:** `curl "http://localhost:8091/api/search/global?q=test"`
4. ✅ **Open frontend:** http://localhost:3000

---

## Summary

**To start Elasticsearch in Docker:**
```batch
start-elasticsearch.bat
```

**To verify:**
```bash
curl http://localhost:9200
```

**To stop:**
```batch
stop-elasticsearch.bat
```

**Docker advantages:**
- ✅ Easy setup
- ✅ Isolated environment
- ✅ Easy cleanup
- ✅ Consistent across machines
- ✅ Production-like setup

---

**Last Updated:** 2025-10-29  
**Version:** 1.0  
**Elasticsearch Version:** 8.11.0




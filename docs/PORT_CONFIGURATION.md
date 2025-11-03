# PLM System - Port Configuration Reference

## 🔌 All Service Ports

### Infrastructure Services
| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **Elasticsearch** | 9200 | http://localhost:9200 | Document indexing & search |
| **Kibana** | 5601 | http://localhost:5601 | Elasticsearch UI & Dev Tools |
| **Neo4j** | 7687 | bolt://localhost:7687 | Graph database (Bolt protocol) |
| **Neo4j Browser** | 7474 | http://localhost:7474 | Neo4j web interface |
| **MySQL** | 3306 | localhost:3306 | Relational database |
| **Redis** | 6379 | localhost:6379 | Cache & session storage |
| **RabbitMQ** | 5672 | localhost:5672 | Message broker (AMQP) |
| **RabbitMQ Management** | 15672 | http://localhost:15672 | RabbitMQ web UI |
| **MinIO** | 9000 | http://localhost:9000 | Object storage API |
| **MinIO Console** | 9001 | http://localhost:9001 | MinIO web console |

### PLM Backend Services
| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **Document Service** | 8081 | http://localhost:8081 | Document management |
| **Task Service** | 8082 | http://localhost:8082 | Task management |
| **User Service** | 8083 | http://localhost:8083 | User management |
| **Change Service** | 8084 | http://localhost:8084 | Change management |
| **Workflow Orchestrator** | 8086 | http://localhost:8086 | Workflow engine (Zeebe) |
| **BOM Service** | 8089 | http://localhost:8089 | BOM & Part management |
| **Graph Service** | 8090 | http://localhost:8090 | Neo4j integration |
| **Search Service** | 8091 | http://localhost:8091 | Unified Elasticsearch search |

### Frontend
| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **React Frontend** | 3000 | http://localhost:3000 | User interface |

## 📋 Quick Reference by Port Number

```
3000  - React Frontend
5601  - Kibana
5672  - RabbitMQ
6379  - Redis
7474  - Neo4j Browser
7687  - Neo4j Bolt
8081  - Document Service
8082  - Task Service
8083  - User Service
8084  - Change Service
8086  - Workflow Orchestrator
8089  - BOM Service
8090  - Graph Service
8091  - Search Service ⭐ NEW
9000  - MinIO API
9001  - MinIO Console
9200  - Elasticsearch
15672 - RabbitMQ Management
```

## 🔍 Search Service Details (Port 8091)

### Endpoints

**Global Search (Unified)**
```bash
GET http://localhost:8091/api/search/global?q={query}
```

**Entity-specific Search**
```bash
GET http://localhost:8091/api/search/documents?q={query}
GET http://localhost:8091/api/search/changes?q={query}
GET http://localhost:8091/api/search/tasks?q={query}
GET http://localhost:8091/api/search/boms?q={query}
```

**Health Check**
```bash
GET http://localhost:8091/actuator/health
```

### Example Usage

```bash
# Search across all entities
curl "http://localhost:8091/api/search/global?q=motor"

# Response format:
{
  "documents": [...],
  "changes": [...],
  "tasks": [...],
  "boms": [...]
}
```

## 🚀 Starting Services

### Start Everything
```batch
# Start Elasticsearch (Docker)
start-elasticsearch.bat

# Start all services including search
start-all-services-with-search.bat
```

### Individual Service Startup
```batch
# Search Service only
cd infra/search-service
mvn spring-boot:run
```

## ✅ Port Conflict Resolution

### Check if Port is in Use

**Windows:**
```batch
netstat -ano | findstr :8091
```

**Kill Process:**
```batch
taskkill /F /PID <process_id>
```

### Common Port Conflicts

**Port 9200 (Elasticsearch)**
- Usually Elasticsearch already running
- Solution: `docker stop plm-elasticsearch` or use `stop-elasticsearch.bat`

**Port 8091 (Search Service)**
- Check if service already running
- Check if another Java process is using it
- Solution: Stop the conflicting process

**Port 3000 (React)**
- Another React app running
- Solution: `taskkill /F /IM node.exe` (careful - kills all Node processes)

## 🔧 Changing Ports

### To Change Search Service Port

**1. Update application.yml:**
```yaml
# infra/search-service/src/main/resources/application.yml
server:
  port: 8091  # Change this value
```

**2. Update frontend configuration:**
```javascript
// frontend/src/components/GlobalSearch.js
const SEARCH_SERVICE_URL = 'http://localhost:8091/api/search';
```

**3. Update scripts:**
- `start-all-services-with-search.bat`
- `reindex-all-elasticsearch.bat`

### To Change Other Service Ports

Each service has its own `application.yml` or `application.properties`:
```properties
server.port=8XXX
```

## 🐳 Docker Port Mapping

**Elasticsearch:**
```yaml
ports:
  - "9200:9200"  # host:container
  - "9300:9300"
```

**Kibana:**
```yaml
ports:
  - "5601:5601"
```

## 🔒 Firewall Configuration

For production, configure firewall rules:

**Allow incoming connections:**
```batch
# Elasticsearch (internal only)
netsh advfirewall firewall add rule name="Elasticsearch" dir=in action=allow protocol=TCP localport=9200

# Search Service (from frontend)
netsh advfirewall firewall add rule name="Search Service" dir=in action=allow protocol=TCP localport=8091
```

## 📊 Service Dependencies

```
React Frontend (3000)
    ↓
Search Service (8091)
    ↓
Elasticsearch (9200)
    ↑
Document (8081), Change (8084), Task (8082), BOM (8089)
    ↓
MySQL (3306), Neo4j (7687), Redis (6379)
```

## 🧪 Testing Ports

**Test all services:**
```bash
curl http://localhost:9200                           # Elasticsearch
curl http://localhost:5601/api/status               # Kibana
curl http://localhost:8081/actuator/health          # Document
curl http://localhost:8082/actuator/health          # Task
curl http://localhost:8083/actuator/health          # User
curl http://localhost:8084/actuator/health          # Change
curl http://localhost:8089/actuator/health          # BOM
curl http://localhost:8090/actuator/health          # Graph
curl http://localhost:8091/actuator/health          # Search
```

## 📝 Environment Variables

**For services running outside Docker:**
```properties
# Document Service
SPRING_ELASTICSEARCH_URIS=http://localhost:9200

# Search Service
SPRING_ELASTICSEARCH_URIS=http://localhost:9200
SERVER_PORT=8091

# All Services
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/plm_db
SPRING_DATA_NEO4J_URI=bolt://localhost:7687
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

## 🆘 Troubleshooting

### Service Won't Start

**Check if port is available:**
```batch
netstat -ano | findstr :8091
```

**View service logs:**
- Check terminal window where service is running
- Look for port binding errors

### Can't Connect to Service

**Verify service is running:**
```bash
curl http://localhost:8091/actuator/health
```

**Check firewall:**
```batch
netsh advfirewall show allprofiles state
```

**Test with telnet:**
```batch
telnet localhost 8091
```

---

**Last Updated:** 2025-10-29  
**Version:** 1.0

**Quick Start:**
1. Start Elasticsearch: `start-elasticsearch.bat`
2. Start all services: `start-all-services-with-search.bat`
3. Check all ports: Open each URL in browser or use curl




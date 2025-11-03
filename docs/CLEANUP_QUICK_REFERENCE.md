# Production Cleanup - Quick Reference Card

## 🚀 Quick Commands

### Clean Everything
```cmd
cleanup-all-data.bat
```

### Individual Services
```cmd
cleanup-mysql-data.bat          # MySQL databases
cleanup-minio-data.bat          # Object storage
cleanup-elasticsearch-data.bat  # Search indices
cleanup-neo4j-data.bat          # Graph database
cleanup-local-files.bat         # Local files & temp uploads
```

## 📋 Cleanup Checklist

### Before Cleanup
- [ ] Backup any important data
- [ ] Stop all services: `stop-all-services.bat`
- [ ] Verify Docker is running: `docker ps`

### Run Cleanup
- [ ] Execute: `cleanup-all-data.bat`
- [ ] Type `YES` to confirm

### Verify Cleanup
```cmd
# MySQL - should show only system databases
docker exec mysql-plm mysql -uroot -proot -e "SHOW DATABASES;"

# MinIO - check console at http://localhost:9001 (minio/password)

# Elasticsearch - should show no PLM indices
curl http://localhost:9200/_cat/indices?v

# Neo4j - check browser at http://localhost:7474 (neo4j/password)
# Run: MATCH (n) RETURN count(n)  -- should be 0
```

## 🔄 Production Restart Sequence

1. **Start Infrastructure**
   ```cmd
   start-mysql-docker.bat
   docker-compose -f infra/docker-compose-infrastructure.yaml up -d
   docker-compose -f docker-compose-elasticsearch.yml up -d
   ```

2. **Initialize Databases**
   ```cmd
   init-mysql-databases-docker.bat
   ```

3. **Start Services**
   ```cmd
   start-all-services.bat
   ```

4. **Create Admin User** (via API or database)

## 🔐 Security Updates

### Passwords to Change
| Service        | Location                                  | Default           |
|----------------|-------------------------------------------|-------------------|
| MySQL          | start-mysql-docker.bat                    | root/root         |
| MinIO          | infra/docker-compose-infrastructure.yaml  | minio/password    |
| Neo4j          | infra/docker-compose-infrastructure.yaml  | neo4j/password    |
| Redis          | infra/docker-compose-infrastructure.yaml  | plm_redis_password|

### Configuration Files to Review
- All `application.yml` / `application.properties` files
- Docker Compose files
- Start scripts

## 📊 Data Locations

| Service        | Container Name     | Volume/Data Location                   |
|----------------|--------------------|----------------------------------------|
| MySQL          | mysql-plm          | Container storage                      |
| MinIO          | plm-minio          | minio-data volume                      |
| Elasticsearch  | plm-elasticsearch  | elasticsearch-data volume              |
| Neo4j          | plm-neo4j          | neo4j-data volume                      |
| Local Files    | N/A                | data/, document-service/data/          |
| Temp Uploads   | N/A                | document-service/temp-uploads/         |

## 🆘 Troubleshooting

### Container Not Running
```cmd
# Check status
docker ps -a | findstr plm

# Start specific container
docker start <container-name>
```

### Network Issues
```cmd
# List networks
docker network ls | findstr plm

# Create network if missing
docker network create plm-network
```

### Permission Errors (PowerShell)
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 🎯 Verification URLs

| Service              | URL                          | Credentials        |
|----------------------|------------------------------|--------------------|
| MinIO Console        | http://localhost:9001        | minio / password   |
| Neo4j Browser        | http://localhost:7474        | neo4j / password   |
| Kibana (ES UI)       | http://localhost:5601        | None (default)     |
| Redis Commander      | http://localhost:8085        | None (auto-config) |

## 📞 Emergency Commands

### Stop Everything
```cmd
stop-all-services.bat
docker-compose -f infra/docker-compose-infrastructure.yaml down
docker-compose -f docker-compose-elasticsearch.yml down
docker stop mysql-plm
```

### View Logs
```cmd
docker logs mysql-plm
docker logs plm-minio
docker logs plm-elasticsearch
docker logs plm-neo4j
```

### Complete Reset (Nuclear Option)
```cmd
# Stop and remove containers + volumes
docker-compose -f infra/docker-compose-infrastructure.yaml down -v
docker-compose -f docker-compose-elasticsearch.yml down -v
docker rm -f mysql-plm
```

## 📝 Notes

- Always type `YES` (uppercase) to confirm cleanup
- Cleanup process takes approximately 1-2 minutes
- Services must be restarted after cleanup
- Elasticsearch indices auto-recreate when services start
- MinIO bucket structure is preserved (only files deleted)

---

**For detailed instructions, see:** `PRODUCTION_DATA_CLEANUP_GUIDE.md`


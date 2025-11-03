# Database Setup Guide

## Quick Start

### Option 1: MySQL (Recommended for Production)

1. **Start MySQL:**
   ```bash
   # If using Docker:
   docker run -d -p 3306:3306 --name mysql-plm \
     -e MYSQL_ROOT_PASSWORD=root \
     mysql:8.0
   ```

2. **Initialize Databases:**
   ```bash
   init-mysql-databases.bat
   # OR
   .\init-mysql-databases.ps1
   ```

3. **Start Services:**
   ```bash
   start-all-services.bat
   # OR
   start-all-services-mysql.bat
   ```

### Option 2: H2 (Development/Testing)

1. **Start Services:**
   ```bash
   start-all-services-dev.bat
   ```

That's it! H2 databases are created automatically in the `./data/` directory.

## Database Details

### MySQL Configuration

**Connection Details:**
- Host: `localhost`
- Port: `3306`
- Username: `plm_user`
- Password: `plm_password`

**Databases:**
- `plm_user_db` - User accounts and authentication
- `plm_bom_db` - Parts and Bill of Materials
- `plm_document_db` - Documents and file metadata
- `plm_task_db` - Tasks and workflows
- `plm_change_db` - Engineering change requests

**Access MySQL:**
```bash
mysql -h localhost -P 3306 -u plm_user -pplm_password

# List databases
SHOW DATABASES;

# Use a database
USE plm_user_db;
SHOW TABLES;
```

### H2 Configuration

**Database Files:**
- `./data/userdb.mv.db` - User service
- `./data/bomdb.mv.db` - BOM service
- `./data/documentdb.mv.db` - Document service
- `./data/taskdb.mv.db` - Task service
- `./data/changedb.mv.db` - Change service

**H2 Console Access:**
- User Service: http://localhost:8083/h2-console
- BOM Service: http://localhost:8089/h2-console
- Document Service: http://localhost:8081/h2-console
- Change Service: http://localhost:8084/h2-console

**H2 Connection Info (for console):**
- JDBC URL: `jdbc:h2:file:./data/userdb` (or respective service db)
- Username: `sa`
- Password: `password`

## Switching Between Databases

### Using Startup Scripts

| Script | Database | Use Case |
|--------|----------|----------|
| `start-all-services.bat` | MySQL | Default (production-like) |
| `start-all-services-mysql.bat` | MySQL | Explicit MySQL mode |
| `start-all-services-dev.bat` | H2 | Development/testing |

### Manual Profile Selection

**Individual Service:**
```bash
cd user-service

# MySQL
mvn spring-boot:run -Dspring-boot.run.profiles=default

# H2
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Neo4j (Graph Database)

All profiles use Neo4j for relationship queries.

**Connection:**
- URI: `bolt://localhost:7687`
- Username: `neo4j`
- Password: `password`

**Start Neo4j:**
```bash
start-neo4j.ps1
# OR manually:
docker run -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:latest
```

**Neo4j Browser:** http://localhost:7474

## When to Use Each Database

### Use MySQL when:
- ✅ Running production or staging environment
- ✅ Need concurrent access from multiple instances
- ✅ Require backup and restore capabilities
- ✅ Performance is critical
- ✅ Testing production-like scenarios

### Use H2 when:
- ✅ Local development
- ✅ Running tests
- ✅ Quick prototyping
- ✅ No MySQL available
- ✅ Isolated environment needed

## Troubleshooting

### MySQL Connection Failed

**Check MySQL is running:**
```bash
# Docker
docker ps | grep mysql

# Windows Service
sc query MySQL80
```

**Test Connection:**
```bash
mysql -h localhost -P 3306 -u root -p
```

**Reinitialize Databases:**
```bash
init-mysql-databases.bat
```

### H2 Database Locked

**Stop all services:**
```bash
stop-all-services.bat
```

**Remove lock files:**
```bash
del data\*.lock
```

### Tables Not Created

**Check Hibernate DDL setting:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Should be 'update' or 'create'
```

**Check service logs** for Hibernate schema creation messages.

## Data Persistence

### MySQL
- Data persists in MySQL server
- Backup: Use `mysqldump`
- Restore: Use `mysql < backup.sql`

### H2
- Data persists in `./data/*.mv.db` files
- Backup: Copy `.mv.db` files
- Restore: Replace `.mv.db` files

## Security Recommendations

### Production Checklist

- [ ] Change MySQL root password
- [ ] Change `plm_user` password
- [ ] Update service configurations with new password
- [ ] Disable H2 console in production
- [ ] Use environment variables for credentials
- [ ] Enable MySQL SSL/TLS
- [ ] Configure firewall rules
- [ ] Set up regular backups

### Update Credentials

**1. Change MySQL password:**
```sql
ALTER USER 'plm_user'@'%' IDENTIFIED BY 'new_secure_password';
FLUSH PRIVILEGES;
```

**2. Update service configs:**
Edit `application.yml` or `application.properties` in each service:
```yaml
spring:
  datasource:
    password: new_secure_password
```

**3. Use environment variables (recommended):**
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

Then set environment variable:
```bash
set DB_PASSWORD=new_secure_password
```

## Further Reading

- [H2 to MySQL Migration Plan](docs/H2_TO_MYSQL_MIGRATION_PLAN.md) - Detailed migration guide
- [MySQL Migration Guide](MYSQL_MIGRATION_GUIDE.md) - Quick reference
- [Data Model Documentation](docs/DATA_MODEL_AND_SCHEMA.md) - Complete schema reference

## Need Help?

1. Check service logs for error messages
2. Verify database connectivity
3. Ensure correct profile is active
4. Review configuration files
5. Check this troubleshooting section






# MySQL Migration Guide

## Overview

The PLM system has been configured to support both MySQL (production) and H2 (development) databases using Spring profiles.

## Quick Start

### 1. Initialize MySQL Databases

Run the initialization script to create databases and users:

**Windows (Command Prompt):**
```bash
init-mysql-databases.bat
```

**Windows (PowerShell):**
```powershell
.\init-mysql-databases.ps1
```

**Manual (MySQL CLI):**
```bash
mysql -u root -p < init-mysql-databases.sql
```

This will create:
- Databases: `plm_user_db`, `plm_bom_db`, `plm_document_db`, `plm_task_db`, `plm_change_db`
- User: `plm_user` with password `plm_password`

### 2. Start Services

**With MySQL (Production/Default):**
```bash
start-all-services.bat
# OR
start-all-services-mysql.bat
```

**With H2 (Development):**
```bash
start-all-services-dev.bat
```

## Database Configuration Summary

### MySQL (Default Profile)

| Service | Database | Port |
|---------|----------|------|
| user-service | plm_user_db | 8083 |
| bom-service | plm_bom_db | 8089 |
| document-service | plm_document_db | 8081 |
| task-service | plm_task_db | 8082 |
| change-service | plm_change_db | 8084 |

**Connection Details:**
- Host: localhost
- Port: 3306
- Username: plm_user
- Password: plm_password

### H2 (Dev Profile)

| Service | Database File |
|---------|--------------|
| user-service | ./data/userdb |
| bom-service | ./data/bomdb |
| document-service | ./data/documentdb |
| task-service | ./data/taskdb |
| change-service | ./data/changedb |

**H2 Console Access:**
- User Service: http://localhost:8083/h2-console
- BOM Service: http://localhost:8089/h2-console
- Document Service: http://localhost:8081/h2-console
- Task Service: http://localhost:8082/h2-console (if enabled)
- Change Service: http://localhost:8084/h2-console

**H2 Connection Settings:**
- Driver: org.h2.Driver
- URL: jdbc:h2:file:./data/{service}db
- Username: sa
- Password: password

## Switching Between Databases

### Method 1: Startup Scripts

Use the appropriate startup script:
- `start-all-services.bat` → MySQL (default)
- `start-all-services-mysql.bat` → MySQL (explicit)
- `start-all-services-dev.bat` → H2 (development)

### Method 2: Manual Profile Selection

Start individual services with profile parameter:

**Maven:**
```bash
# MySQL
cd user-service
mvn spring-boot:run -Dspring-boot.run.profiles=default

# H2
cd user-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Java JAR:**
```bash
# MySQL
java -jar user-service.jar --spring.profiles.active=default

# H2
java -jar user-service.jar --spring.profiles.active=dev
```

### Method 3: Environment Variable

```bash
# MySQL
set SPRING_PROFILES_ACTIVE=default
mvn spring-boot:run

# H2
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

## Troubleshooting

### MySQL Connection Issues

**Problem:** Can't connect to MySQL
```
Solution:
1. Check MySQL is running:
   docker ps | grep mysql
   
2. Test MySQL connection:
   mysql -h localhost -P 3306 -u plm_user -pplm_password
   
3. Check databases exist:
   mysql -u plm_user -pplm_password -e "SHOW DATABASES;"
```

**Problem:** Database not found
```
Solution:
Run the initialization script again:
   init-mysql-databases.bat
```

**Problem:** Access denied for user
```
Solution:
Reset user permissions:
   mysql -u root -p
   GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'%';
   FLUSH PRIVILEGES;
```

### H2 Database Issues

**Problem:** H2 console not accessible
```
Solution:
1. Check service is running with dev profile
2. Verify H2 console is enabled in configuration:
   spring.h2.console.enabled=true
```

**Problem:** Database file locked
```
Solution:
Stop all services and delete .lock files:
   del data\*.lock
```

## Data Migration (Optional)

If you need to migrate existing H2 data to MySQL:

### Export from H2

1. Access H2 console
2. Run: `SCRIPT TO 'backup.sql'`
3. Save the SQL script

### Import to MySQL

1. Edit the SQL script to make it MySQL-compatible
2. Run: `mysql -u plm_user -pplm_password plm_user_db < backup.sql`

**Note:** Table creation is handled by Hibernate, so you may only need to export/import data rows.

## Best Practices

### Development
- Use **H2 dev profile** for local development
- Fast startup, no external dependencies
- Data persists in ./data/ directory

### Testing
- Use **H2 dev profile** for unit/integration tests
- Each service has isolated database

### Production/Staging
- Use **MySQL default profile**
- Better performance for concurrent access
- Proper ACID compliance
- Easier to backup and restore

## Configuration Files

### Service Configuration Files

| Service | Configuration File | Type |
|---------|-------------------|------|
| user-service | application.yml | YAML |
| bom-service | application.yml | YAML |
| task-service | application.yml | YAML |
| document-service | application.properties + application-dev.properties | Properties |
| change-service | application.yml | YAML |

### Profile Activation

All services default to MySQL (`spring.profiles.active=default`) but can be overridden.

## Verification

After starting services with MySQL:

1. **Check MySQL databases:**
   ```sql
   mysql -u plm_user -pplm_password
   SHOW DATABASES;
   USE plm_user_db;
   SHOW TABLES;
   ```

2. **Check service logs:**
   Look for: "HikariPool-1 - Start completed" (MySQL connection pool)

3. **Test API:**
   ```bash
   # Create a user
   curl -X POST http://localhost:8083/users \
     -H "Content-Type: application/json" \
     -d '{"username":"test","roles":["ROLE_USER"],"password":"password123"}'
   ```

4. **Verify in database:**
   ```sql
   SELECT * FROM users;
   ```

## Rollback to H2

If you encounter issues with MySQL:

1. Stop all services
2. Use dev startup script: `start-all-services-dev.bat`
3. Services will use H2 databases in ./data/

Your H2 data files are preserved and untouched by MySQL operations.

## Security Notes

### Default Credentials

**MySQL:**
- User: `plm_user`
- Password: `plm_password`

**⚠️ IMPORTANT:** Change these credentials for production use!

Update in:
- `init-mysql-databases.sql`
- Service configuration files (application.yml/properties)

### H2 Console

**⚠️ WARNING:** H2 console is enabled in dev profile. Disable for production:
```yaml
spring:
  h2:
    console:
      enabled: false
```

## Next Steps

1. ✅ Initialize MySQL databases
2. ✅ Start services with MySQL profile
3. ✅ Verify connections
4. ✅ Test API operations
5. ⚠️ Change default passwords
6. ⚠️ Configure backups
7. ⚠️ Set up monitoring

## Support

For issues or questions:
1. Check service logs in respective terminal windows
2. Verify MySQL is running and accessible
3. Ensure correct profile is active
4. Review this guide's troubleshooting section






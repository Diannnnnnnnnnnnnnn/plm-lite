# H2 to MySQL Migration Plan

## Overview

This plan outlines the migration from H2 databases to MySQL for production use, while maintaining H2 support for development environments using Spring profiles.

**Target Services:**
- user-service
- bom-service
- document-service
- task-service
- change-service

**Strategy:**
- Use Spring profiles to support both databases
- Default profile → MySQL (production)
- Dev profile → H2 (development/testing)
- Keep existing H2 data files as backup

---

## Current State Analysis

### Service Database Status

| Service | Current DB | Type | Data Location |
|---------|-----------|------|---------------|
| user-service | H2 | File | `./data/userdb` |
| bom-service | H2 | Memory | In-memory (no persistence) |
| document-service | H2 | File | `./data/documentdb` |
| task-service | H2 | File | `./data/taskdb` |
| change-service | H2 | File | `./data/changedb` (dev profile active) |

### MySQL Instance

- **Host:** localhost (Docker)
- **Port:** 3306
- **Status:** Running ✓

---

## Migration Steps

### Step 1: Verify MySQL and Create Databases

**Actions:**
1. Test MySQL connection
2. Create required databases
3. Create MySQL user with appropriate permissions
4. Verify connection from Spring Boot

**SQL Commands:**
```sql
-- Connect to MySQL
mysql -h localhost -P 3306 -u root -p

-- Create databases
CREATE DATABASE IF NOT EXISTS plm_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_bom_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_document_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_task_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS plm_change_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional if not using root)
CREATE USER IF NOT EXISTS 'plm_user'@'%' IDENTIFIED BY 'plm_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_bom_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_task_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_change_db.* TO 'plm_user'@'%';
FLUSH PRIVILEGES;

-- Verify databases
SHOW DATABASES;
```

**Testing:**
```bash
# Test connection
mysql -h localhost -P 3306 -u plm_user -pplm_password -e "SHOW DATABASES;"
```

---

### Step 2: Update Maven Dependencies

**Action:** Add MySQL connector dependency to each service's `pom.xml`

**Services to Update:**
- user-service/pom.xml
- bom-service/pom.xml
- document-service/pom.xml
- task-service/pom.xml
- change-service/pom.xml (verify it's already added)

**Dependency to Add:**
```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Note:** Keep H2 dependency for dev profile:
```xml
<!-- H2 Database (for development) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Step 3: Configure Spring Profiles

**Pattern:** Create profile-based configuration with:
- **Default profile** → MySQL (production)
- **Dev profile** → H2 (development)

#### 3.1 User Service Configuration

**File:** `user-service/src/main/resources/application.yml`

**Strategy:** Convert from `.properties` to `.yml` for better profile management

**Configuration Structure:**
```yaml
spring:
  application:
    name: user-service
  profiles:
    active: default  # Default to MySQL

# Common configuration (shared across profiles)
server:
  port: 8083

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    enabled: true
    register-with-eureka: true
    fetch-registry: true

# Redis, RabbitMQ, and other configs...

---
# MySQL Profile (Default/Production)
spring:
  config:
    activate:
      on-profile: default
  datasource:
    url: jdbc:mysql://localhost:3306/plm_user_db?createDatabaseIfNotExist=true&serverTimezone=UTC
    username: plm_user
    password: plm_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

---
# H2 Profile (Development)
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:file:./data/userdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect
```

#### 3.2 BOM Service Configuration

**File:** `bom-service/src/main/resources/application.yml`

**Key Changes:**
- Default: MySQL with persistent storage
- Dev: H2 file-based (change from in-memory to file for data persistence)

```yaml
spring:
  application:
    name: bom-service
  profiles:
    active: default

server:
  port: 8089

---
# MySQL Profile (Default/Production)
spring:
  config:
    activate:
      on-profile: default
  datasource:
    url: jdbc:mysql://localhost:3306/plm_bom_db?createDatabaseIfNotExist=true&serverTimezone=UTC
    username: plm_user
    password: plm_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

---
# H2 Profile (Development)
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:file:./data/bomdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.H2Dialect
```

#### 3.3 Document Service Configuration

**File:** `document-service/src/main/resources/application.properties` → Keep as is, just add MySQL profile

**Add to existing file:**
```properties
# Default profile uses MySQL
spring.profiles.active=default

# ===============================
# MySQL Datasource (Default Profile)
# ===============================
spring.datasource.url=jdbc:mysql://localhost:3306/plm_document_db?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=plm_user
spring.datasource.password=plm_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

**Create:** `document-service/src/main/resources/application-dev.properties`
```properties
# H2 File Database (Development Profile)
spring.datasource.url=jdbc:h2:file:./data/documentdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

#### 3.4 Task Service Configuration

**File:** `task-service/src/main/resources/application.yml`

Similar pattern to user-service.

#### 3.5 Change Service Configuration

**File:** `change-service/src/main/resources/application.yml`

**Current State:** Already has MySQL config, just needs profile adjustment

**Change:**
```yaml
spring:
  profiles:
    active: default  # Change from 'dev' to 'default'
```

---

### Step 4: Data Migration Strategy

**Options:**

#### Option A: Fresh Start (Recommended for Development)
- Let Hibernate create tables automatically (`ddl-auto: update`)
- Re-initialize with sample data
- Keep H2 data files as backup

#### Option B: Migrate Existing Data
- Export data from H2 databases
- Import into MySQL
- More complex, but preserves existing data

**Recommendation:** Start with Option A for initial setup, then migrate specific data if needed.

---

### Step 5: Update Startup Scripts

**Files to Update:**
- `start-all-services.bat`
- `start-all-services.ps1`

**Add profile parameter:**

**For MySQL (production):**
```batch
start "User Service" cmd /c "cd user-service && mvn spring-boot:run -Dspring-boot.run.profiles=default"
```

**For H2 (development):**
```batch
start "User Service" cmd /c "cd user-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
```

**Better approach:** Create separate startup scripts:
- `start-all-services-mysql.bat` (default profile)
- `start-all-services-dev.bat` (dev profile)

---

### Step 6: Testing Plan

#### Phase 1: Individual Service Testing
1. Test each service with MySQL profile
2. Verify database connectivity
3. Check table creation
4. Test CRUD operations

#### Phase 2: Integration Testing
1. Start all services with MySQL
2. Test cross-service communication
3. Verify Neo4j synchronization
4. Test complete workflows

#### Phase 3: Dev Profile Testing
1. Switch to dev profile
2. Verify H2 still works
3. Test hot-switching between profiles

---

### Step 7: Rollback Strategy

**If issues occur:**

1. **Switch back to H2:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Data is preserved:**
   - H2 data files remain in `./data/` directories
   - MySQL data remains in MySQL

3. **No permanent changes:**
   - Configuration is profile-based
   - Easy to switch back

---

## Configuration Summary

### Profile Activation Methods

**Method 1: Application Properties (Default)**
```properties
spring.profiles.active=default
```

**Method 2: Command Line**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=default
java -jar app.jar --spring.profiles.active=default
```

**Method 3: Environment Variable**
```bash
set SPRING_PROFILES_ACTIVE=default
```

**Method 4: IDE Configuration**
- IntelliJ: Run Configuration → Active Profiles: `default`
- Eclipse: Run Configuration → Arguments: `--spring.profiles.active=default`

---

## Database Connection Details

### MySQL (Default Profile)

| Service | Database | URL |
|---------|----------|-----|
| user-service | plm_user_db | jdbc:mysql://localhost:3306/plm_user_db |
| bom-service | plm_bom_db | jdbc:mysql://localhost:3306/plm_bom_db |
| document-service | plm_document_db | jdbc:mysql://localhost:3306/plm_document_db |
| task-service | plm_task_db | jdbc:mysql://localhost:3306/plm_task_db |
| change-service | plm_change_db | jdbc:mysql://localhost:3306/plm_change_db |

**Credentials:**
- Username: `plm_user`
- Password: `plm_password`

### H2 (Dev Profile)

| Service | Database | URL |
|---------|----------|-----|
| user-service | userdb | jdbc:h2:file:./data/userdb |
| bom-service | bomdb | jdbc:h2:file:./data/bomdb |
| document-service | documentdb | jdbc:h2:file:./data/documentdb |
| task-service | taskdb | jdbc:h2:file:./data/taskdb |
| change-service | changedb | jdbc:h2:file:./data/changedb |

**Credentials:**
- Username: `sa`
- Password: `password` (or empty)

**H2 Console Access:**
- URL: `http://localhost:{service-port}/h2-console`
- Available only when dev profile is active

---

## Timeline and Order of Execution

### Phase 1: Preparation (15 minutes)
1. ✓ Verify MySQL is running
2. Create databases and user
3. Test MySQL connectivity

### Phase 2: Configuration Update (30 minutes)
4. Add MySQL dependencies to pom.xml files
5. Update/create configuration files for all services
6. Review and validate configurations

### Phase 3: Testing (45 minutes)
7. Test user-service with MySQL
8. Test bom-service with MySQL
9. Test document-service with MySQL
10. Test task-service with MySQL
11. Test change-service with MySQL
12. Test all services together

### Phase 4: Startup Scripts (15 minutes)
13. Create new startup scripts with profile support
14. Test both MySQL and dev profiles

### Phase 5: Documentation (15 minutes)
15. Update DATA_MODEL_AND_SCHEMA.md
16. Update README files
17. Document profile usage

**Total Estimated Time:** ~2 hours

---

## Post-Migration Checklist

- [ ] All services start successfully with MySQL
- [ ] Tables are created in MySQL databases
- [ ] CRUD operations work correctly
- [ ] Cross-service calls function properly
- [ ] Neo4j synchronization works
- [ ] H2 dev profile still works
- [ ] Startup scripts updated
- [ ] Documentation updated
- [ ] Team notified of new profile requirements

---

## Common Issues and Solutions

### Issue 1: Connection Refused
**Cause:** MySQL not accessible
**Solution:**
```bash
# Check MySQL is running
docker ps | grep mysql
# Check port is accessible
telnet localhost 3306
```

### Issue 2: Authentication Failed
**Cause:** Wrong credentials
**Solution:**
```sql
-- Reset user password
ALTER USER 'plm_user'@'%' IDENTIFIED BY 'plm_password';
FLUSH PRIVILEGES;
```

### Issue 3: Database Not Found
**Cause:** Database not created
**Solution:**
```sql
CREATE DATABASE IF NOT EXISTS plm_user_db;
```

### Issue 4: Table Creation Fails
**Cause:** Insufficient permissions
**Solution:**
```sql
GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'%';
```

### Issue 5: Character Encoding Issues
**Cause:** Wrong charset
**Solution:** Use UTF8MB4 in database creation
```sql
ALTER DATABASE plm_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Next Steps

After reviewing this plan, we can proceed with:

1. **Immediate:** Create MySQL databases and user
2. **Next:** Update one service (user-service) as a proof of concept
3. **Then:** Roll out to remaining services
4. **Finally:** Update documentation and scripts

**Ready to proceed?** Let me know if you'd like to:
- Start with Step 1 (MySQL setup)
- Modify any part of the plan
- Add additional requirements


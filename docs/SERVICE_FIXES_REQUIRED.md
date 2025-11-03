# Service Fixes Required for ES Integration Test

## Issues Found

The comprehensive test revealed that services are **running** but have configuration issues:

1. **Missing `/api/v1` prefix** on controller mappings
2. **Missing Spring Boot Actuator** dependency for health endpoints

---

## ✅ Fixed: BOM Service

### Changes Made:

1. **Added `/api/v1` prefix to controllers:**
   - `PartController`: `/parts` → `/api/v1/parts`
   - `BomController`: `/boms` → `/api/v1/boms`

2. **Added Actuator dependency** to `bom-service/pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

3. **Added missing import** to `PartServiceImpl.java`:
   ```java
   import com.example.bom_service.service.PartSearchService;
   ```

4. **Fixed lambda expression** in `BomServiceImpl.java`

### To Apply:
**Restart the BOM Service window** (Ctrl+C and re-run `mvn spring-boot:run`)

---

## ⚠️ TODO: Document Service

**File:** `document-service/src/main/java/.../controller/DocumentController.java`

**Required Change:**
```java
// Change from:
@RequestMapping("/documents")

// To:
@RequestMapping("/api/v1/documents")
```

**Also check:** Does it have `spring-boot-starter-actuator` in pom.xml?

---

## ⚠️ TODO: Change Service

**File:** `change-service/src/main/java/.../controller/ChangeController.java`

**Required Change:**
```java
// Change from:
@RequestMapping("/changes")  // or whatever it currently is

// To:
@RequestMapping("/api/changes")  // Note: no /v1 based on test
```

**Also check:** Does it have `spring-boot-starter-actuator` in pom.xml?

---

## ⚠️ TODO: Task Service

**File:** `task-service/src/main/java/.../TaskController.java`

**Required Change:**
```java
// Change from:
@RequestMapping("/tasks")  // or whatever it currently is

// To:
@RequestMapping("/api/tasks")  // Note: no /v1 based on test
```

**Also check:** Does it have `spring-boot-starter-actuator` in pom.xml?

---

## Quick Fix Steps

### For Each Service:

1. **Stop the service** (Ctrl+C in its window)

2. **Fix the controller mapping:**
   - Add proper API prefix to `@RequestMapping`

3. **Add Actuator** (if missing) in `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

4. **Restart the service:**
   ```bash
   mvn spring-boot:run
   ```

5. **Test the endpoint:**
   ```powershell
   # Test actuator
   curl http://localhost:PORT/actuator/health
   
   # Test API
   curl http://localhost:PORT/api/v1/RESOURCE  # or /api/RESOURCE
   ```

---

## After All Services Fixed

Run the comprehensive test again:

```powershell
cd C:\Users\diang\Desktop\plm-lite
powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-integration-test.ps1
```

Expected result: **95%+ pass rate** 🎉

---

## Port Reference

| Service | Port | Health Endpoint | API Endpoint |
|---------|------|-----------------|--------------|
| Document | 8081 | `/actuator/health` | `/api/v1/documents` |
| BOM | 8089 | `/actuator/health` | `/api/v1/boms`, `/api/v1/parts` |
| Change | 8084 | `/actuator/health` | `/api/changes` |
| Task | 8083 | `/actuator/health` | `/api/tasks` |
| Search | 8091 | `/api/v1/search/health` | `/api/v1/search` |

---

**Status:** BOM Service fixed ✅ | Others need fixing ⚠️


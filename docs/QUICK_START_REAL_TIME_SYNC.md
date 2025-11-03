# Quick Start: Real-Time Sync (5 Steps)

## ✅ What's Done

1. ✅ Graph Service REST API created (`GraphSyncController`)
2. ✅ Graph Sync logic implemented (`GraphSyncService`)
3. ✅ BOM Service Feign client created (`GraphServiceClient`)
4. ✅ Graceful fallback implemented
5. ✅ All DTOs created

**You're 80% done!** Just need to add sync calls to your services.

---

## 🚀 Complete in 5 Steps

### Step 1: Start Graph Service (2 min)
```bash
cd infra/graph-service
mvn spring-boot:run
```

Verify: `curl http://localhost:8090/api/graph/sync/health`

### Step 2: Update BOM Service Main Class (1 min)

Add `@EnableFeignClients` if not present:

```java
@SpringBootApplication
@EnableFeignClients  // ✅ ADD THIS
public class BomServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BomServiceApplication.class, args);
    }
}
```

### Step 3: Inject Client in PartServiceImpl (2 min)

```java
@Service
public class PartServiceImpl implements PartService {
    
    private final GraphServiceClient graphServiceClient;  // ✅ ADD
    
    public PartServiceImpl(
            PartRepository partRepository,
            PartUsageRepository partUsageRepository,
            DocumentPartLinkRepository documentPartLinkRepository,
            GraphServiceClient graphServiceClient) {  // ✅ ADD
        // ... assignments ...
        this.graphServiceClient = graphServiceClient;  // ✅ ADD
    }
}
```

### Step 4: Add Sync Calls (10 min)

Copy-paste these 4 methods into `PartServiceImpl.java`:

```java
// After createPart() saves to MySQL:
private void syncPartToGraph(Part part) {
    try {
        PartSyncDto dto = new PartSyncDto(
            part.getId(),
            part.getTitle(),
            part.getStage().name(),
            part.getLevel(),
            part.getCreator(),
            part.getCreateTime()
        );
        graphServiceClient.syncPart(dto);
    } catch (Exception e) {
        log.warn("Failed to sync part: {}", e.getMessage());
    }
}

// After addPartUsage() saves to MySQL:
private void syncPartUsageToGraph(PartUsage partUsage) {
    try {
        PartUsageDto dto = new PartUsageDto(
            partUsage.getParentId(),
            partUsage.getChildId(),
            partUsage.getQuantity()
        );
        graphServiceClient.syncPartUsage(dto);
    } catch (Exception e) {
        log.warn("Failed to sync part usage: {}", e.getMessage());
    }
}

// After linkPartToDocument() saves to MySQL:
private void syncPartDocumentLinkToGraph(DocumentPartLink link) {
    try {
        PartDocumentLinkDto dto = new PartDocumentLinkDto(
            link.getPartId(),
            link.getDocumentId()
        );
        graphServiceClient.syncPartDocumentLink(dto);
    } catch (Exception e) {
        log.warn("Failed to sync link: {}", e.getMessage());
    }
}

// After deletePart() deletes from MySQL:
private void deletePartFromGraph(String partId) {
    try {
        graphServiceClient.deletePart(partId);
    } catch (Exception e) {
        log.warn("Failed to delete from graph: {}", e.getMessage());
    }
}
```

Then call these methods:
```java
// In createPart():
Part savedPart = partRepository.save(part);
syncPartToGraph(savedPart);  // ✅ ADD THIS LINE

// In addPartUsage():
partUsageRepository.save(partUsage);
syncPartUsageToGraph(partUsage);  // ✅ ADD THIS LINE

// In linkPartToDocument():
documentPartLinkRepository.save(link);
syncPartDocumentLinkToGraph(link);  // ✅ ADD THIS LINE

// In deletePart():
partRepository.delete(part);
deletePartFromGraph(id);  // ✅ ADD THIS LINE
```

### Step 5: Test It! (5 min)

```bash
# Restart BOM Service
cd bom-service
mvn spring-boot:run

# Create a test part
curl -X POST http://localhost:8088/parts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Engine V8",
    "stage": "DETAILED_DESIGN",
    "level": "1",
    "creator": "engineer1"
  }'

# Verify in Neo4j Browser (http://localhost:7474)
MATCH (p:Part {title: 'Test Engine V8'}) RETURN p
```

**You should see the part in Neo4j!** 🎉

---

## 🎯 What Happens

```
User creates Part
    ↓
BOM Service:
  1. Saves to MySQL ✅
  2. Calls Graph Service ✅
    ↓
Graph Service:
  3. Saves to Neo4j ✅
    ↓
Both databases synced! 🎉
```

---

## 🐛 If Something Goes Wrong

### Graph Service Down?
**No problem!** BOM Service still works, sync is just logged as warning.

### Feign Client Error?
Check logs for: `No qualifying bean of type GraphServiceClient`
Solution: Add `@EnableFeignClients` to main class

### Sync Not Working?
1. Check Graph Service is running: `curl http://localhost:8090/actuator/health`
2. Check BOM Service logs for warnings
3. Verify DTOs match between services

---

## 📚 Full Documentation

- **Why Direct?** `DIRECT_VS_EVENT_DRIVEN.md`
- **Detailed Guide** `NEO4J_DIRECT_INTEGRATION_GUIDE.md`
- **Setup**: `NEO4J_SETUP.md`

---

## ✅ Done!

That's it! Once you complete these 5 steps, every Part created in BOM Service will automatically appear in Neo4j.

**Total time: ~20 minutes** ⏱️

---

**Next**: Repeat the same pattern for Document Service and Change Service! 🚀


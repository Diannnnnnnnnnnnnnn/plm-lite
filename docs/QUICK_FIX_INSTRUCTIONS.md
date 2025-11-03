# Quick Fix Instructions - Almost There! 🎯

## ✅ Task Service - FIXED!

**Issue Found:** Task Service was running on port **8082** instead of **8083**

**Fix Applied:** Changed `application.yml` to use port 8083

### **Action Required: Restart Task Service**

1. Go to the **Task Service PowerShell window**
2. Press **Ctrl+C** to stop it
3. Press **Up Arrow** then **Enter** to restart
4. Wait for: `Started TaskServiceApplication in X.XXX seconds`
5. Verify it now says: `Tomcat started on port 8083`

---

## ⚠️ Document Service - Needs Diagnosis

**Issue:** Returns 503 Server Unavailable

**Most likely causes:**
1. **MySQL not running** (if using default profile)
2. **Wrong database credentials**
3. **Elasticsearch connection issue**

### **Please Check Document Service Console:**

Look for errors containing:
- `Failed to configure a DataSource`
- `Connection refused`
- `Access denied for user`
- `Unknown database`
- `Communications link failure`

### **Quick Fix Options:**

**Option 1: Use H2 Database (Fastest)**
```powershell
# In Document Service window:
# Press Ctrl+C to stop
# Then run:
cd document-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Option 2: Fix MySQL Connection**

If you want to use MySQL:
1. Check if MySQL is running: `netstat -ano | findstr ":3306"`
2. Verify credentials in `document-service/src/main/resources/application.properties`
3. Make sure database exists and user has access

---

## 🧪 After Fixes: Re-run Test

Once both services are restarted and healthy:

```powershell
cd C:\Users\diang\Desktop\plm-lite
powershell -ExecutionPolicy Bypass -File scripts/comprehensive-es-integration-test.ps1
```

**Expected result:** 
- **95%+ success rate** (57+/60 tests)
- All 5 services healthy
- All CRUD operations working
- All ES indexing verified

---

## 📊 Current Progress

✅ **Elasticsearch** - Healthy and working  
✅ **BOM Service** - UP and ES integration working  
✅ **Change Service** - UP and ES integration working  
✅ **Search Service** - Working perfectly  
✅ **Task Service** - Fixed, needs restart  
⚠️ **Document Service** - Needs diagnostic

**We're at 80% completion!** Just need to fix Document Service and we're done! 🚀

---

## 🎯 Next Steps

1. **Restart Task Service** (with new port 8083)
2. **Check Document Service** error messages
3. **Fix Document Service** (likely just switch to H2)
4. **Re-run test** → See 95%+ pass rate!

---

## 💡 Quick Health Check

After restarting Task Service, run this to check all services:

```powershell
Write-Host "Service Health Check:" -ForegroundColor Cyan
curl http://localhost:8081/actuator/health 2>&1 | Select-Object -First 2
curl http://localhost:8089/actuator/health 2>&1 | Select-Object -First 2
curl http://localhost:8084/actuator/health 2>&1 | Select-Object -First 2  
curl http://localhost:8083/actuator/health 2>&1 | Select-Object -First 2
curl http://localhost:8091/api/v1/search/health 2>&1 | Select-Object -First 2
```

All should return status 200 or show `"status":"UP"`

---

**You're almost there!** Just 2 quick fixes and the comprehensive test will pass! 🎉




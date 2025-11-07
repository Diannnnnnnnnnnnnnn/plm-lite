# 🚀 Quick Start: NGINX + API Gateway + Auth Service

**Last Updated:** November 6, 2025  
**Status:** ✅ Ready to Use  

---

## ⚡ Quick Start (3 Commands)

```powershell
# 1. Start Infrastructure
cd infra && docker-compose -f docker-compose-infrastructure.yaml up -d && cd ..

# 2. Start All Services
.\start-all-services.ps1

# 3. Open Browser
# Navigate to: http://localhost:8111
```

---

## 🎯 Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | **http://localhost:8111** | **Main Application** |
| API Gateway | http://localhost:8080 | Backend routing |
| Auth Service | http://localhost:8110 | JWT generation |
| Eureka Dashboard | http://localhost:8761 | Service registry |

---

## 🔑 Test Credentials

| Username | Password | Role |
|----------|----------|------|
| demo | demo | USER |
| guodian | password | REVIEWER |
| labubu | password | EDITOR |
| vivi | password | APPROVER |

---

## 📝 What Changed

### **Ports**
- NGINX: `8084` → `8111` (new entry point)
- API Gateway: `8081` → `8080`
- Auth Service: `8110` (unchanged)

### **Frontend API Calls**
- Old: `http://localhost:8083/users`
- New: `http://localhost:8111/api/users`

### **Architecture**
```
Browser → NGINX (8111) → API Gateway (8080) → Services
                ↓
            JWT Validation
```

---

## ✅ Verify Installation

```bash
# Test NGINX
curl http://localhost:8111/health
# Expected: healthy

# Test API Gateway
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Test Login
curl -X POST http://localhost:8111/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}'
# Expected: {"token":"eyJ..."}
```

---

## 🐛 Common Issues

**NGINX won't start:**
```bash
docker logs plm-nginx
```

**Services not registered:**
```bash
# Wait 30 seconds, then check:
http://localhost:8761
```

**JWT validation fails:**
```bash
# Ensure JWT_SECRET matches in:
# - auth-service/application.properties
# - api-gateway/application.properties
```

---

## 📚 Full Documentation

- [Complete Implementation Guide](docs/API_GATEWAY_AUTH_NGINX_IMPLEMENTATION_COMPLETE.md)
- [Integration Plan](docs/API_GATEWAY_AUTH_NGINX_INTEGRATION_PLAN.md)

---

## 🎉 Done!

Your PLM-Lite system is now secured with JWT authentication and accessible via a single entry point at **http://localhost:8111**


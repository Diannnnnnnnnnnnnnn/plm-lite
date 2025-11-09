# API Gateway + Auth Service + NGINX Integration Plan

**Document Version:** 1.0  
**Date:** November 6, 2025  
**Status:** Ready for Implementation  

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [Port Configuration](#port-configuration)
4. [Implementation Phases](#implementation-phases)
5. [Detailed File Changes](#detailed-file-changes)
6. [Docker Configuration](#docker-configuration)
7. [Frontend Integration](#frontend-integration)
8. [Testing & Verification](#testing--verification)
9. [Deployment Instructions](#deployment-instructions)
10. [Benefits & Rationale](#benefits--rationale)

---

## Executive Summary

This document outlines the complete integration plan for connecting **NGINX (port 8111)**, **API Gateway (port 8080)**, and **Auth Service (port 8110)** into a cohesive, secure microservices architecture.

### Key Objectives

- ✅ Create single entry point via NGINX (port 8111)
- ✅ Route all traffic through API Gateway (port 8080)
- ✅ Implement JWT authentication at gateway level
- ✅ Secure all backend microservices
- ✅ Containerize NGINX using Docker
- ✅ Update frontend to use unified API endpoint

### Timeline

- **Estimated Time:** 12 hours total
- **Implementation:** Automated via scripts
- **Testing:** 2 hours
- **Deployment:** Same day

---

## Architecture Overview

### Current State (Before Integration)

```
Browser → Multiple Direct Service Calls
  ├─→ User Service (8083)
  ├─→ Document Service (8081)
  ├─→ Task Service (8082)
  └─→ Other Services...

Problems:
  ❌ No centralized authentication
  ❌ CORS issues
  ❌ Security vulnerabilities
  ❌ Difficult to manage
```

### Target State (After Integration)

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                             │
│                    http://localhost:8111                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              NGINX (Port 8111) - DOCKER CONTAINER                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  / (root)        → Frontend (React) :3001                │   │
│  │  /api/*          → API Gateway :8080                     │   │
│  │  /auth/*         → API Gateway :8080 → Auth :8110       │   │
│  │  /eureka/*       → Eureka Server :8761 (monitoring)     │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│         API GATEWAY (Port 8080) - EXISTING GATEWAY               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ✓ JWT Authentication Filter (NEW)                       │   │
│  │  ✓ Route to Auth Service (8110)                          │   │
│  │  ✓ Route to Backend Services via Eureka                  │   │
│  │  ✓ Add User Context Headers (X-User-Id, X-Username)      │   │
│  │  ✓ Token Validation Cache (Redis)                        │   │
│  └──────────────────────────────────────────────────────────┘   │
└──┬────────┬───────────┬─────────────┬──────────┬──────────────┘
   │        │           │             │          │
   ▼        ▼           ▼             ▼          ▼
┌──────┐ ┌──────┐ ┌───────────┐ ┌────────┐ ┌──────────┐
│Auth  │ │User  │ │Document   │ │Task    │ │  Other   │
│:8110 │ │:8083 │ │:8081      │ │:8082   │ │ Services │
└──────┘ └──────┘ └───────────┘ └────────┘ └──────────┘
```

### Request Flow

1. **User Browser** → Accesses http://localhost:8111
2. **NGINX** → Routes to appropriate service
3. **API Gateway** → Validates JWT token
4. **Auth Service** → Validates credentials (if login)
5. **Backend Services** → Receive authenticated requests with user context
6. **Response** → Returns through same chain

---

## Port Configuration

### Complete Port Map

| Component | Port | Status | Notes |
|-----------|------|--------|-------|
| **NGINX** | **8111** | ✅ New | Single entry point (Docker) |
| **API Gateway** | **8080** | ✅ Updated | Changed from 8081 |
| **Auth Service** | **8110** | ✅ Existing | JWT generation/validation |
| Frontend (React) | 3001 | ✅ Current | Development server |
| Eureka Server | 8761 | ✅ Current | Service discovery |
| User Service | 8083 | ✅ Current | Via gateway |
| Document Service | 8081 | ✅ Current | Via gateway |
| Task Service | 8082 | ✅ Current | Via gateway |
| BOM Service | 8089 | ✅ Current | Via gateway |
| Change Service | 8084 | ✅ Current | Via gateway |
| Workflow Orchestrator | 8086 | ✅ Current | Via gateway |
| Graph Service | 8090 | ✅ Current | Via gateway |
| Search Service | 8091 | ✅ Current | Via gateway |
| Redis Cache | 6379 | ✅ Current | Token caching |
| Neo4j | 7474, 7687 | ✅ Current | Graph database |
| MySQL | 3306 | ✅ Current | Relational data |

### Port Change Summary

| Service | Old Port | New Port | Reason |
|---------|----------|----------|--------|
| API Gateway | 8081 | 8080 | User specification |
| NGINX | 8084 | 8111 | User specification |

---

## Implementation Phases

### Phase 1: Port Configuration Updates ⚡ (30 minutes)

**Objective:** Update API Gateway to use port 8080

**Files to Modify:**
- `api-gateway/src/main/resources/application.properties`
- `start-all-services.ps1`
- `start-all-services.bat`

**Changes:**
```properties
# api-gateway/src/main/resources/application.properties
server.port=8080  # Changed from 8081
```

**Verification:**
```bash
curl http://localhost:8080/actuator/health
```

---

### Phase 2: NGINX Docker Setup ⚡ (1 hour)

**Objective:** Create Docker container for NGINX on port 8111

**Files to Create:**
1. `infra/nginx/Dockerfile`
2. `infra/nginx/docker-compose.yml`
3. Update `infra/nginx/nginx.conf`

**Key Features:**
- Runs in Docker container
- Proxies to API Gateway (8080)
- Handles CORS
- WebSocket support
- Health checks

**Verification:**
```bash
docker ps | grep plm-nginx
curl http://localhost:8111/health
```

---

### Phase 3: JWT Authentication in API Gateway ⚡⚡ (4-5 hours)

**Objective:** Add JWT validation to API Gateway

**Components to Create:**

1. **JWT Authentication Filter**
   - Extracts JWT from Authorization header
   - Validates with Auth Service
   - Caches results in Redis
   - Adds user context headers

2. **Security Configuration**
   - Public paths: `/auth/**`, `/actuator/health`
   - Protected paths: All others require JWT

3. **Auth Service Client**
   - Feign client to communicate with Auth Service
   - Token validation endpoint integration

4. **User Context DTO**
   - Transfer user information between services

**Files to Create:**
- `api-gateway/src/main/java/com/example/api_gateway/filter/JwtAuthenticationFilter.java`
- `api-gateway/src/main/java/com/example/api_gateway/config/SecurityConfig.java`
- `api-gateway/src/main/java/com/example/api_gateway/service/AuthServiceClient.java`
- `api-gateway/src/main/java/com/example/api_gateway/dto/UserContext.java`
- `api-gateway/src/main/java/com/example/api_gateway/util/JwtUtil.java`

**Files to Update:**
- `api-gateway/pom.xml` (add JWT + Redis dependencies)
- `api-gateway/src/main/resources/application.properties`

**Verification:**
```bash
# Should fail without token
curl http://localhost:8080/api/users

# Should succeed with valid token
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users
```

---

### Phase 4: Frontend Integration ⚡⚡ (3-4 hours)

**Objective:** Update frontend to use NGINX (8111) with JWT authentication

**Files to Create:**
1. `frontend/.env` - Environment configuration
2. `frontend/src/utils/apiClient.js` - Centralized axios with JWT
3. `frontend/src/services/authService.js` - Login/logout/token management

**Files to Update:**
1. `frontend/src/services/userService.js`
2. `frontend/src/services/documentService.js`
3. `frontend/src/services/taskService.js`
4. `frontend/src/services/bomService.js`
5. `frontend/src/services/changeService.js`
6. `frontend/src/services/partService.js`
7. `frontend/src/components/Auth/Auth.js`
8. `frontend/package.json` (add jwt-decode)

**Key Features:**
- Single API client with interceptors
- Automatic JWT attachment
- Auto-redirect on 401
- Token expiration handling

**Verification:**
```bash
# Install dependencies
cd frontend
npm install jwt-decode

# Test login flow
npm start
# Navigate to http://localhost:8111
```

---

### Phase 5: Update Startup Scripts ⚡ (30 minutes)

**Objective:** Update all startup scripts with new ports and NGINX

**Files to Update:**
- `start-all-services.ps1`
- `start-all-services.bat`
- `README.md`

**Key Changes:**
- Add API Gateway startup (port 8080)
- Add NGINX Docker startup (port 8111)
- Update documentation
- Update port references

---

### Phase 6: Complete Docker Stack ⚡ (1 hour)

**Objective:** Create unified Docker Compose for full deployment

**Files to Create:**
1. `docker-compose-full-stack.yml` - Complete deployment
2. `infra/docker-compose-gateway-nginx.yml` - Gateway + NGINX only

**Features:**
- Single command deployment
- Network configuration
- Service dependencies
- Health checks
- Volume persistence

---

## Detailed File Changes

### 1. API Gateway Configuration

**File: `api-gateway/src/main/resources/application.properties`**

```properties
# Application Name
spring.application.name=api-gateway

# Server Port - UPDATED to 8080
server.port=8080

# Enable Service Discovery
spring.cloud.discovery.locator.enabled=true
spring.cloud.discovery.locator.lower-case-service-id=true

# Eureka Configuration
eureka.client.enabled=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# JWT Configuration (NEW)
jwt.secret=${JWT_SECRET:change-this-to-a-very-long-256-bit-secret-string-please}
jwt.expiration-minutes=60

# Auth Service Configuration (NEW)
auth.service.url=http://localhost:8110
auth.service.validate-endpoint=/api/auth/validate

# Redis Configuration for Token Caching (NEW)
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=plm_redis_password
spring.cache.type=redis
spring.cache.redis.time-to-live=600000

# Gateway Routes Configuration
# Auth Service (port 8110)
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://localhost:8110
spring.cloud.gateway.routes[0].predicates[0]=Path=/auth/**
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/auth/(?<segment>.*), /api/auth/$\{segment}

# User Service
spring.cloud.gateway.routes[1].id=user-service
spring.cloud.gateway.routes[1].uri=lb://user-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/users/**
spring.cloud.gateway.routes[1].filters[0]=RewritePath=/api/users/(?<segment>.*), /users/$\{segment}

# Task Service
spring.cloud.gateway.routes[2].id=task-service
spring.cloud.gateway.routes[2].uri=lb://task-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/tasks/**
spring.cloud.gateway.routes[2].filters[0]=RewritePath=/api/tasks/(?<segment>.*), /tasks/$\{segment}

# Document Service
spring.cloud.gateway.routes[3].id=document-service
spring.cloud.gateway.routes[3].uri=lb://document-service
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/documents/**

# BOM Service
spring.cloud.gateway.routes[4].id=bom-service
spring.cloud.gateway.routes[4].uri=lb://bom-service
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/boms/**
spring.cloud.gateway.routes[4].filters[0]=RewritePath=/api/boms/(?<segment>.*), /boms/$\{segment}

# Change Service
spring.cloud.gateway.routes[5].id=change-service
spring.cloud.gateway.routes[5].uri=lb://change-service
spring.cloud.gateway.routes[5].predicates[0]=Path=/api/changes/**
spring.cloud.gateway.routes[5].filters[0]=RewritePath=/api/changes/(?<segment>.*), /changes/$\{segment}

# Workflow Orchestrator
spring.cloud.gateway.routes[6].id=workflow-orchestrator
spring.cloud.gateway.routes[6].uri=http://localhost:8086
spring.cloud.gateway.routes[6].predicates[0]=Path=/api/workflows/**

# Graph Service
spring.cloud.gateway.routes[7].id=graph-service
spring.cloud.gateway.routes[7].uri=lb://graph-service
spring.cloud.gateway.routes[7].predicates[0]=Path=/api/graph/**

# CORS Configuration
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedOrigins=http://localhost:3001,http://localhost:8111
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS,PATCH
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowedHeaders=*
spring.cloud.gateway.globalcors.corsConfigurations.[/**].allowCredentials=true
spring.cloud.gateway.globalcors.corsConfigurations.[/**].exposedHeaders=Authorization

# Logging
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.com.example.api_gateway=DEBUG
```

---

### 2. API Gateway Dependencies

**File: `api-gateway/pom.xml` (Add these dependencies)**

```xml
<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Redis for Token Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- WebFlux (if not already added) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## Docker Configuration

### NGINX Dockerfile

**File: `infra/nginx/Dockerfile`**

```dockerfile
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 8111
EXPOSE 8111

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8111/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

---

### NGINX Docker Compose

**File: `infra/nginx/docker-compose.yml`**

```yaml
version: '3.8'

services:
  nginx:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: plm-nginx
    ports:
      - "8111:8111"
    networks:
      - plm-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8111/health"]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 5s

networks:
  plm-network:
    driver: bridge
```

---

### NGINX Configuration

**File: `infra/nginx/nginx.conf`**

```nginx
server {
    listen 8111;
    server_name localhost;

    # Increase buffer sizes for large requests
    client_max_body_size 100M;
    proxy_buffers 8 16k;
    proxy_buffer_size 32k;

    # Frontend - React App (Development)
    location / {
        proxy_pass http://host.docker.internal:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }

    # API Gateway - All Backend APIs (Port 8080)
    location /api/ {
        proxy_pass http://host.docker.internal:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
        
        # Pass through Authorization header
        proxy_set_header Authorization $http_authorization;
        proxy_pass_header Authorization;
    }

    # Auth Service via API Gateway (Port 8080)
    location /auth/ {
        proxy_pass http://host.docker.internal:8080/auth/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }

    # Eureka Dashboard (Optional - for monitoring)
    location /eureka/ {
        proxy_pass http://host.docker.internal:8761/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Health Check Endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

---

## Frontend Integration

### Environment Configuration

**File: `frontend/.env`**

```env
# API Base URL - Points to NGINX
REACT_APP_API_URL=http://localhost:8111

# Optional: Development overrides
# REACT_APP_API_URL=http://localhost:8080
```

---

### Centralized API Client

**File: `frontend/src/utils/apiClient.js`**

```javascript
import axios from 'axios';

// Use NGINX as the entry point (port 8111)
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8111';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Request interceptor - Add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log(`[API] ${config.method.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle 401 errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      console.log('[API] Unauthorized - redirecting to login');
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

### Auth Service

**File: `frontend/src/services/authService.js`**

```javascript
import apiClient from '../utils/apiClient';
import { jwtDecode } from 'jwt-decode';

class AuthService {
  async login(username, password) {
    try {
      const response = await apiClient.post('/auth/login', {
        username,
        password,
      });

      const { token } = response.data;
      
      // Store JWT token
      localStorage.setItem('jwt_token', token);
      
      // Decode and store user info
      const userInfo = jwtDecode(token);
      localStorage.setItem('user', JSON.stringify(userInfo));
      
      return userInfo;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
    
    // Optional: Call backend logout endpoint
    try {
      apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
      }
    }
    return null;
  }

  getToken() {
    return localStorage.getItem('jwt_token');
  }

  isAuthenticated() {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded = jwtDecode(token);
      const currentTime = Date.now() / 1000;
      return decoded.exp > currentTime;
    } catch (error) {
      return false;
    }
  }
}

const authServiceInstance = new AuthService();
export default authServiceInstance;
```

---

## Testing & Verification

### Phase 1: Port Configuration Test

```bash
# Test API Gateway is running on port 8080
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

---

### Phase 2: NGINX Test

```bash
# Test NGINX is running on port 8111
curl http://localhost:8111/health

# Expected: healthy

# Test NGINX health check
docker ps | grep plm-nginx

# Check logs
docker logs plm-nginx
```

---

### Phase 3: JWT Authentication Test

```bash
# 1. Login and get JWT token
curl -X POST http://localhost:8111/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}'

# Expected: {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}

# 2. Test protected endpoint without token (should fail)
curl http://localhost:8111/api/users

# Expected: 401 Unauthorized

# 3. Test protected endpoint with token (should succeed)
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
  http://localhost:8111/api/users

# Expected: [...user list...]
```

---

### Phase 4: Frontend Integration Test

```bash
# 1. Install dependencies
cd frontend
npm install jwt-decode

# 2. Start frontend
npm start

# 3. Test in browser
# Navigate to: http://localhost:8111
# Login with: demo/demo
# Check browser console for API calls
# Verify Authorization header is present
```

---

### End-to-End Test Checklist

- [ ] All services start successfully
- [ ] NGINX accessible on port 8111
- [ ] API Gateway accessible on port 8080
- [ ] Auth Service accessible on port 8110
- [ ] Login returns JWT token
- [ ] Frontend stores JWT token
- [ ] API calls include Authorization header
- [ ] Protected endpoints require JWT
- [ ] Invalid/expired JWT returns 401
- [ ] User can access all features
- [ ] Logout clears JWT token

---

## Deployment Instructions

### Development Deployment (Current Setup)

```powershell
# 1. Start Infrastructure
cd infra
docker-compose -f docker-compose-infrastructure.yaml up -d

# 2. Start All Services
cd ..
.\start-all-services.ps1

# 3. Access Application
# Open browser: http://localhost:8111
```

---

### Production Deployment (Future)

```bash
# Option 1: Docker Compose
docker-compose -f docker-compose-full-stack.yml up -d

# Option 2: Kubernetes
kubectl apply -f k8s/

# Option 3: Individual Services
# Start each service with production profiles
```

---

## Benefits & Rationale

### Security Benefits

✅ **Centralized Authentication**
- Single point for JWT validation
- Consistent security policies
- Easier to audit and monitor

✅ **Defense in Depth**
- Multiple security layers
- NGINX → API Gateway → Services
- Isolated services not exposed directly

✅ **Token Management**
- Redis caching reduces auth service load
- Automatic token expiration
- Blacklist support for logout

---

### Operational Benefits

✅ **Single Entry Point**
- Simplified client configuration
- Easy to add SSL/TLS
- Centralized logging and monitoring

✅ **Service Discovery**
- Eureka handles internal routing
- Services can move/scale freely
- Automatic failover

✅ **Scalability**
- Easy to add load balancing
- Horizontal scaling support
- Cache layer reduces bottlenecks

---

### Developer Benefits

✅ **Simplified Frontend**
- Single API endpoint
- Consistent error handling
- Automatic token management

✅ **Backend Isolation**
- Services focus on business logic
- No auth code duplication
- Easy to add new services

✅ **Testing**
- Mock gateway for unit tests
- Integration tests simplified
- Consistent test patterns

---

## Troubleshooting

### Common Issues

**Issue 1: NGINX won't start**
```bash
# Check if port 8111 is already in use
netstat -ano | findstr :8111

# Kill process or change port
```

**Issue 2: API Gateway returns 503**
```bash
# Check Eureka dashboard
http://localhost:8761

# Ensure services are registered
# Wait 30s for service discovery
```

**Issue 3: JWT validation fails**
```bash
# Check JWT secret matches in:
# - auth-service/application.properties
# - api-gateway/application.properties

# Verify Redis is running
redis-cli -a plm_redis_password ping
```

**Issue 4: CORS errors**
```bash
# Check CORS configuration in:
# - api-gateway/application.properties
# - infra/nginx/nginx.conf

# Verify allowedOrigins includes frontend URL
```

---

## Appendix

### A. File Checklist

**New Files to Create:**
- [ ] `infra/nginx/Dockerfile`
- [ ] `infra/nginx/docker-compose.yml`
- [ ] `frontend/.env`
- [ ] `frontend/src/utils/apiClient.js`
- [ ] `frontend/src/services/authService.js`
- [ ] `api-gateway/src/main/java/com/example/api_gateway/filter/JwtAuthenticationFilter.java`
- [ ] `api-gateway/src/main/java/com/example/api_gateway/config/SecurityConfig.java`
- [ ] `api-gateway/src/main/java/com/example/api_gateway/service/AuthServiceClient.java`
- [ ] `api-gateway/src/main/java/com/example/api_gateway/dto/UserContext.java`
- [ ] `api-gateway/src/main/java/com/example/api_gateway/util/JwtUtil.java`

**Files to Update:**
- [ ] `api-gateway/pom.xml`
- [ ] `api-gateway/src/main/resources/application.properties`
- [ ] `infra/nginx/nginx.conf`
- [ ] `start-all-services.ps1`
- [ ] `start-all-services.bat`
- [ ] `frontend/package.json`
- [ ] `frontend/src/services/*.js` (all service files)
- [ ] `frontend/src/components/Auth/Auth.js`
- [ ] `README.md`

---

### B. Dependencies to Add

**API Gateway (`api-gateway/pom.xml`):**
- jjwt-api (0.11.5)
- jjwt-impl (0.11.5)
- jjwt-jackson (0.11.5)
- spring-boot-starter-data-redis-reactive
- spring-boot-starter-webflux

**Frontend (`frontend/package.json`):**
- jwt-decode (^4.0.0)

---

### C. Environment Variables

**Required Environment Variables:**

```bash
# JWT Secret (must match across auth-service and api-gateway)
JWT_SECRET=change-this-to-a-very-long-256-bit-secret-string-please

# Redis Password
REDIS_PASSWORD=plm_redis_password

# Optional overrides
REACT_APP_API_URL=http://localhost:8111
```

---

### D. Network Diagram

```
Internet/External Network
         │
         ▼
    [Firewall]
         │
         ▼
    NGINX:8111 ◄─── SSL/TLS Termination (Future)
         │
         ▼
  API Gateway:8080 ◄─── JWT Validation
         │
    ┌────┴────┐
    ▼         ▼
 Eureka    Auth:8110
  :8761
    │
    ├─→ User:8083
    ├─→ Document:8081
    ├─→ Task:8082
    ├─→ BOM:8089
    ├─→ Change:8084
    ├─→ Workflow:8086
    ├─→ Graph:8090
    └─→ Search:8091
         │
         ▼
    Data Layer
    (Redis, MySQL, Neo4j)
```

---

## Conclusion

This integration plan provides a complete roadmap for implementing a secure, scalable, and maintainable microservices architecture. By following these phases systematically, you'll achieve:

- **Single entry point** via NGINX (8111)
- **Centralized authentication** via API Gateway (8080)
- **Secure services** with JWT validation
- **Production-ready** architecture

**Next Step:** Proceed with Option A implementation - automated creation of all files and configurations.

---

**Document End**




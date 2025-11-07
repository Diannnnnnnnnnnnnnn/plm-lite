# Eureka Service Discovery - Quick Reference

## 🚀 Quick Start

### Start Eureka Server
```bash
cd infra/eureka-server
mvn spring-boot:run
```

### Access Dashboard
- **URL:** http://localhost:8761
- **Purpose:** View all registered services

## 📋 Service Registry

| Service                 | Port | Status URL                      |
|-------------------------|------|---------------------------------|
| eureka-server           | 8761 | http://localhost:8761           |
| user-service            | 8083 | http://localhost:8083/actuator/health |
| task-service            | 8082 | http://localhost:8082/actuator/health |
| document-service        | 8081 | http://localhost:8081/actuator/health |
| change-service          | 8084 | http://localhost:8084/actuator/health |
| bom-service             | 8089 | http://localhost:8089/actuator/health |
| graph-service           | 8090 | http://localhost:8090/actuator/health |
| workflow-orchestrator   | 8086 | http://localhost:8086/actuator/health |
| file-storage-service    | 9900 | http://localhost:9900/actuator/health |
| auth-service            | 8110 | http://localhost:8110/actuator/health |

## ⚙️ Configuration Template

### Enable Eureka Client (YAML)
```yaml
spring:
  application:
    name: my-service

eureka:
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}
```

### Enable Eureka Client (Properties)
```properties
spring.application.name=my-service
eureka.client.enabled=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

## 🔌 FeignClient Usage

### ✅ Correct (With Eureka)
```java
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable Long id);
}
```

### ❌ Incorrect (Hardcoded URL)
```java
@FeignClient(name = "user-service", url = "http://localhost:8083")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable Long id);
}
```

## 🛠️ Common Commands

### Check Service Registration
```bash
# View all registered services
curl http://localhost:8761/eureka/apps | jq

# Check specific service
curl http://localhost:8761/eureka/apps/USER-SERVICE | jq
```

### Health Check
```bash
# Service health
curl http://localhost:8083/actuator/health

# Eureka health
curl http://localhost:8761/actuator/health
```

### Maven Dependency
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| Service not registered | 1. Check Eureka is running<br>2. Verify `eureka.client.enabled=true`<br>3. Wait 30-60 seconds |
| Connection refused | Start Eureka server before other services |
| Service name not found | Ensure `spring.application.name` matches FeignClient name |
| Multiple instances | Each instance needs unique `instance-id` |

## ⏱️ Timing Parameters

| Parameter | Default | Purpose |
|-----------|---------|---------|
| Registration delay | ~30s | Time for service to appear in Eureka |
| Heartbeat interval | 30s | How often service sends heartbeat |
| Eviction timeout | 90s | Time before unhealthy service removed |
| Cache refresh | 30s | How often clients refresh service list |

## 📊 Service States

| State | Meaning |
|-------|---------|
| **UP** | Service is healthy and registered |
| **DOWN** | Service is registered but unhealthy |
| **STARTING** | Service is initializing |
| **OUT_OF_SERVICE** | Service intentionally offline |
| **UNKNOWN** | Health check failed |

## 🔄 Startup Order

```
1. Eureka Server (8761)           ← Start FIRST
2. Graph Service (8090)
3. User Service (8083)
4. Auth Service (8110)
5. Other Services...
6. Frontend (3000)
```

## 📞 API Endpoints

### Eureka Server
- **Dashboard:** `GET http://localhost:8761/`
- **Applications:** `GET http://localhost:8761/eureka/apps`
- **Specific App:** `GET http://localhost:8761/eureka/apps/{APP-NAME}`
- **Instance:** `GET http://localhost:8761/eureka/apps/{APP}/{INSTANCE-ID}`

### Service Actuator
- **Health:** `GET http://localhost:{port}/actuator/health`
- **Info:** `GET http://localhost:{port}/actuator/info`
- **Metrics:** `GET http://localhost:{port}/actuator/metrics`

## 🎯 Best Practices

1. ✅ **Always start Eureka first**
2. ✅ **Use service names in FeignClients** (not URLs)
3. ✅ **Enable Spring Boot Actuator** for health checks
4. ✅ **Use unique instance IDs** for multiple instances
5. ✅ **Monitor Eureka dashboard** regularly
6. ✅ **Configure proper timeouts** for your use case
7. ✅ **Secure Eureka in production** with authentication

## 🔐 Production Checklist

- [ ] Eureka server has authentication enabled
- [ ] Multiple Eureka instances for HA
- [ ] Health check endpoints configured
- [ ] Proper network security groups
- [ ] Monitoring and alerting setup
- [ ] Service names follow naming conventions
- [ ] Fallback/circuit breaker configured

## 📚 Related Documentation

- **Full Guide:** `EUREKA_SERVICE_DISCOVERY_GUIDE.md`
- **Architecture:** `MICROSERVICES_ARCHITECTURE.md`
- **Deployment:** `DEPLOYMENT_GUIDE.md`
- **API Gateway:** `API_GATEWAY_GUIDE.md`

---

**Quick Test:**
```bash
# 1. Start Eureka
cd infra/eureka-server && mvn spring-boot:run

# 2. Start a service
cd ../../user-service && mvn spring-boot:run

# 3. Check registration (wait 30s)
curl http://localhost:8761/eureka/apps/USER-SERVICE

# 4. Success if you see status: UP
```


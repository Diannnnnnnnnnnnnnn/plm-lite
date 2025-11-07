# Eureka Service Discovery Integration Guide

## 📋 Overview

This guide explains the Eureka service discovery integration in the PLM-Lite system. Eureka enables dynamic service discovery, allowing microservices to find and communicate with each other without hardcoded URLs.

## 🎯 What is Eureka?

**Netflix Eureka** is a service registry for resilient mid-tier load balancing and failover. In our PLM system:

- **Eureka Server**: Central registry where all services register themselves
- **Eureka Clients**: All microservices that register with and discover other services through Eureka

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Eureka Server (8761)                     │
│              Service Registry & Discovery                    │
└──────────────────┬──────────────────────────────────────────┘
                   │
         ┌─────────┼─────────┬─────────┬─────────┬─────────┐
         │         │         │         │         │         │
    ┌────▼───┐ ┌──▼────┐ ┌──▼────┐ ┌──▼────┐ ┌──▼────┐ ┌──▼────┐
    │ User   │ │ Task  │ │ Doc   │ │Change │ │ BOM   │ │ Graph │
    │Service │ │Service│ │Service│ │Service│ │Service│ │Service│
    └────────┘ └───────┘ └───────┘ └───────┘ └───────┘ └───────┘
         │         │         │         │         │         │
         └─────────┴─────────┴─────────┴─────────┴─────────┘
                   All services discover each other
```

## 🚀 Quick Start

### 1. Start Eureka Server

The Eureka server must start **before** any other services.

**Using batch script (Windows):**
```bash
cd infra/eureka-server
mvn spring-boot:run
```

**Using PowerShell:**
```powershell
.\start-all-services.ps1  # Automatically starts Eureka first
```

**Access Eureka Dashboard:**
- URL: http://localhost:8761
- View all registered services and their health status

### 2. Services Auto-Register

Once Eureka is running, all services will automatically:
1. Register themselves on startup
2. Send heartbeats every 30 seconds
3. Discover other services via Eureka
4. Use load-balanced service names instead of hardcoded URLs

## 📦 Service Configuration

### Eureka Server Configuration

**Location:** `infra/eureka-server/src/main/resources/application.properties`

```properties
spring.application.name=eureka-server
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Eureka Client Configuration (Services)

All microservices are configured as Eureka clients:

**Example (YAML):**
```yaml
spring:
  application:
    name: user-service  # Service identifier

eureka:
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
```

**Example (Properties):**
```properties
spring.application.name=document-service
eureka.client.enabled=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
```

## 🔌 Service-to-Service Communication

### Before Eureka (Hardcoded URLs)

```java
@FeignClient(name = "user-service", url = "http://localhost:8083")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable Long id);
}
```

**Problems:**
- ❌ Hardcoded hostname and port
- ❌ Cannot scale horizontally
- ❌ No automatic failover
- ❌ Manual configuration changes required

### After Eureka (Service Discovery)

```java
@FeignClient(name = "user-service")  // Just the service name!
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable Long id);
}
```

**Benefits:**
- ✅ Automatic service discovery
- ✅ Load balancing across multiple instances
- ✅ Automatic failover
- ✅ No configuration changes needed

## 📊 Registered Services

| Service Name              | Port | Purpose                          |
|---------------------------|------|----------------------------------|
| `eureka-server`           | 8761 | Service registry                 |
| `user-service`            | 8083 | User management                  |
| `task-service`            | 8082 | Task management                  |
| `document-service`        | 8081 | Document management              |
| `change-service`          | 8084 | Change management                |
| `bom-service`             | 8089 | Bill of Materials                |
| `graph-service`           | 8090 | Neo4j graph operations           |
| `workflow-orchestrator`   | 8086 | Camunda workflow management      |
| `file-storage-service`    | 9900 | MinIO file storage               |
| `auth-service`            | 8110 | Authentication & authorization   |
| `api-gateway`             | 8081 | API Gateway (optional)           |

## 🛠️ Features Enabled

### 1. Dynamic Service Discovery
Services automatically find each other without hardcoded configuration.

### 2. Load Balancing
When multiple instances of a service run, Eureka automatically distributes requests.

```java
// Automatically load-balances between all user-service instances
@FeignClient(name = "user-service")
public interface UserClient { ... }
```

### 3. Health Monitoring
Eureka tracks service health through heartbeats:
- Services send heartbeat every 30 seconds
- Unhealthy services are removed from registry
- Services re-register when they recover

### 4. Instance Metadata
Each service instance includes metadata:
- Instance ID (unique)
- IP Address
- Port
- Health status
- Registration timestamp

## 🔧 Advanced Configuration

### Multiple Eureka Servers (Production)

For high availability in production:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
```

### Custom Health Check

```yaml
eureka:
  instance:
    health-check-url-path: /actuator/health
    status-page-url-path: /actuator/info
```

### Lease Renewal & Expiration

```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30  # Heartbeat interval
    lease-expiration-duration-in-seconds: 90  # When to evict
```

## 🐛 Troubleshooting

### Service Not Appearing in Eureka

**Check 1: Eureka Server Running**
```bash
curl http://localhost:8761/
```

**Check 2: Service Configuration**
```yaml
eureka:
  client:
    enabled: true  # Must be true
```

**Check 3: Network Connectivity**
```bash
ping localhost
telnet localhost 8761
```

### Service Discovery Not Working

**Check 1: Service Name Match**
```java
// FeignClient name must match spring.application.name
@FeignClient(name = "user-service")  // Must match exactly!
```

**Check 2: Eureka Client Dependency**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### Connection Refused Errors

**Solution 1: Start Eureka First**
```bash
# Always start Eureka before other services
cd infra/eureka-server
mvn spring-boot:run
```

**Solution 2: Wait for Registration**
```
Services take 30-60 seconds to fully register and become discoverable
```

## 📈 Monitoring & Maintenance

### Eureka Dashboard

Access: http://localhost:8761

**Information Available:**
- All registered instances
- Instance status (UP, DOWN, STARTING)
- Registration time
- Last heartbeat
- Lease information

### Service Health Endpoints

Each service exposes health endpoints:

```bash
# Check service health
curl http://localhost:8083/actuator/health

# Check Eureka registration status
curl http://localhost:8083/actuator/info
```

### Logs

Check Eureka server logs:
```bash
cd infra/eureka-server
mvn spring-boot:run

# Look for:
# "Registered instance USER-SERVICE/..."
# "Renew threshold is: 1"
```

## 🎓 Best Practices

### 1. Start Order
Always start services in this order:
1. **Eureka Server** (8761)
2. **Core Services** (user, graph)
3. **Business Services** (task, document, change, bom)
4. **Orchestration** (workflow-orchestrator)
5. **Frontend**

### 2. Service Naming
- Use lowercase with hyphens: `user-service`, not `UserService`
- Keep names consistent across configuration and code
- Use descriptive names that indicate purpose

### 3. Instance IDs
- Always include random value for multiple instances
- Format: `${spring.application.name}:${random.value}`

### 4. Health Checks
- Enable Spring Boot Actuator
- Configure proper health check endpoints
- Monitor Eureka dashboard regularly

### 5. Timeouts
Configure appropriate timeouts:
```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

## 🔐 Security Considerations

### Production Security

In production, secure Eureka:

```yaml
# Enable Spring Security on Eureka Server
eureka:
  server:
    enable-self-preservation: false  # Disable in dev only

# Configure authentication
spring:
  security:
    user:
      name: admin
      password: ${EUREKA_PASSWORD}
```

### Client Authentication

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://admin:${EUREKA_PASSWORD}@eureka-server:8761/eureka/
```

## 🚦 Deployment Scenarios

### Local Development
- Single Eureka instance
- All services on localhost
- Default configuration works

### Docker Deployment
```yaml
eureka:
  instance:
    hostname: eureka-server
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### Kubernetes Deployment
- Use Kubernetes Service Discovery instead
- Or configure Eureka with proper DNS resolution
- Consider using Spring Cloud Kubernetes

## 📚 Additional Resources

### Official Documentation
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Server Configuration](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
- [Service Discovery Pattern](https://microservices.io/patterns/service-registry.html)

### Related Guides
- `API_GATEWAY_GUIDE.md` - API Gateway integration with Eureka
- `MICROSERVICES_ARCHITECTURE.md` - Overall architecture
- `DEPLOYMENT_GUIDE.md` - Production deployment

## ✅ Verification Checklist

After setting up Eureka:

- [ ] Eureka Server accessible at http://localhost:8761
- [ ] All services show as "UP" in Eureka dashboard
- [ ] Service count matches expected number
- [ ] Services can communicate via service names
- [ ] Health endpoints return 200 OK
- [ ] No connection refused errors in logs
- [ ] Startup scripts include Eureka server
- [ ] FeignClients use service names (no URLs)

## 🎉 Benefits Summary

### Before Eureka
```
Service A → http://localhost:8083/users/1 → Service B
```
- Hardcoded URLs
- Single point of failure
- Manual configuration
- No load balancing

### After Eureka
```
Service A → user-service → Eureka → Service B (instance 1, 2, or 3)
```
- Dynamic discovery
- Automatic failover
- Zero configuration
- Built-in load balancing

---

**Need Help?**
- Check Eureka dashboard: http://localhost:8761
- Review service logs for registration errors
- Verify configuration in `application.yml`/`application.properties`
- Ensure Eureka server started before other services


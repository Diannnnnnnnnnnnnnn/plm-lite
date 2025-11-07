@echo off
REM ============================================
REM Start All PLM Services with MySQL + Frontend
REM ============================================

echo.
echo ========================================
echo   Starting PLM System - All Services
echo ========================================
echo.

REM Get the current directory
set PROJECT_ROOT=%CD%

REM Start Eureka Server First (all services register with it)
echo [Eureka Server - Service Discovery]
echo   Port: 8761
echo   Starting in new window...
start "PLM - Eureka Server (8761)" cmd /k "cd /d %PROJECT_ROOT%\infra\eureka-server && echo ========================================== && echo   Eureka Server - Port 8761 && echo   Service Discovery ^& Registry && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 30 seconds for startup...
echo.
timeout /t 30 /nobreak >nul

REM Start API Gateway (Port 8080 - NEW)
echo [API Gateway]
echo   Port: 8080
echo   Starting in new window...
start "PLM - API Gateway (8080)" cmd /k "cd /d %PROJECT_ROOT%\api-gateway && echo ========================================== && echo   API Gateway - Port 8080 && echo   JWT Authentication ^& Routing && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 15 seconds for startup...
echo.
timeout /t 15 /nobreak >nul

REM Start Graph Service (other services depend on it)
echo [Graph Service]
echo   Port: 8090
echo   Starting in new window...
start "PLM - Graph Service (8090)" cmd /k "cd /d %PROJECT_ROOT%\infra\graph-service && echo ========================================== && echo   Graph Service - Port 8090 && echo   Database: Neo4j && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 40 seconds for startup...
echo.
timeout /t 40 /nobreak >nul

REM Start Workflow Orchestrator (tasks depend on it)
echo [Workflow Orchestrator]
echo   Port: 8086
echo   Starting in new window...
start "PLM - Workflow Orchestrator (8086)" cmd /k "cd /d %PROJECT_ROOT%\workflow-orchestrator && echo ========================================== && echo   Workflow Orchestrator - Port 8086 && echo   Database: H2 (dev) / MySQL (prod) && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 40 seconds for startup...
echo.
timeout /t 40 /nobreak >nul

REM Start User Service
echo [User Service]
echo   Port: 8083
echo   Starting in new window...
start "PLM - User Service (8083)" cmd /k "cd /d %PROJECT_ROOT%\user-service && echo ========================================== && echo   User Service - Port 8083 && echo   Database: MySQL && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 45 seconds for startup...
echo.
timeout /t 45 /nobreak >nul

REM Start Auth Service (Port 8110 - NEW)
echo [Auth Service]
echo   Port: 8110
echo   Starting in new window...
start "PLM - Auth Service (8110)" cmd /k "cd /d %PROJECT_ROOT%\auth-service && echo ========================================== && echo   Auth Service - Port 8110 && echo   JWT Token Generation && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 30 seconds for startup...
echo.
timeout /t 30 /nobreak >nul

REM Start Task Service
echo [Task Service]
echo   Port: 8082
echo   Starting in new window...
start "PLM - Task Service (8082)" cmd /k "cd /d %PROJECT_ROOT%\task-service && echo ========================================== && echo   Task Service - Port 8082 && echo   Database: MySQL && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 45 seconds for startup...
echo.
timeout /t 45 /nobreak >nul

REM Start Document Service
echo [Document Service]
echo   Port: 8081
echo   Starting in new window...
start "PLM - Document Service (8081)" cmd /k "cd /d %PROJECT_ROOT%\document-service && echo ========================================== && echo   Document Service - Port 8081 && echo   Database: MySQL && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 45 seconds for startup...
echo.
timeout /t 45 /nobreak >nul

REM Start BOM Service
echo [BOM Service]
echo   Port: 8089
echo   Starting in new window...
start "PLM - BOM Service (8089)" cmd /k "cd /d %PROJECT_ROOT%\bom-service && echo ========================================== && echo   BOM Service - Port 8089 && echo   Database: MySQL && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 45 seconds for startup...
echo.
timeout /t 45 /nobreak >nul

REM Start Change Service
echo [Change Service]
echo   Port: 8084
echo   Starting in new window...
start "PLM - Change Service (8084)" cmd /k "cd /d %PROJECT_ROOT%\change-service && echo ========================================== && echo   Change Service - Port 8084 && echo   Database: MySQL + Neo4j && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 30 seconds for startup...
echo.
timeout /t 30 /nobreak >nul

REM Start Search Service
echo [Search Service]
echo   Port: 8091
echo   Starting in new window...
start "PLM - Search Service (8091)" cmd /k "cd /d %PROJECT_ROOT%\infra\search-service && echo ========================================== && echo   Search Service - Port 8091 && echo   Unified Search with Elasticsearch && echo ========================================== && echo. && mvn spring-boot:run"
echo   Waiting 30 seconds for startup...
echo.
timeout /t 30 /nobreak >nul

REM Start NGINX in Docker (Port 8111 - NEW)
echo [NGINX - Entry Point]
echo   Port: 8111
echo   Starting Docker container...
cd /d %PROJECT_ROOT%\infra\nginx
docker-compose up -d
cd /d %PROJECT_ROOT%
echo   NGINX container started
echo.
timeout /t 5 /nobreak >nul

REM Start Frontend (React)
echo [Frontend - React]
echo   Port: 3001
echo   Starting in new window...
start "PLM - Frontend (3001)" cmd /k "cd /d %PROJECT_ROOT%\frontend && echo ========================================== && echo   Frontend - Port 3001 && echo   React Development Server && echo ========================================== && echo. && npm start"
echo   Frontend starting...
echo.
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   All Services Started Successfully!
echo ========================================
echo.
echo Infrastructure:
echo   Eureka Server:        http://localhost:8761/
echo   Redis Cache:          localhost:6379
echo.
echo Gateway Layer:
echo   NGINX (Entry Point):  http://localhost:8111 ^<-- START HERE!
echo   API Gateway:          http://localhost:8080
echo.
echo Backend Services:
echo   Auth Service:         http://localhost:8110
echo   User Service:         http://localhost:8083
echo   Graph Service:        http://localhost:8090
echo   Document Service:     http://localhost:8081
echo   Task Service:         http://localhost:8082
echo   BOM Service:          http://localhost:8089
echo   Change Service:       http://localhost:8084
echo   Workflow Orchestrator: http://localhost:8086
echo   Search Service:       http://localhost:8091
echo.
echo Frontend:
echo   React UI:             http://localhost:3001
echo   Via NGINX:            http://localhost:8111
echo.
echo Service Discovery:
echo   All services registered with Eureka
echo   View registry:        http://localhost:8761/
echo.
echo Total Windows Opened: 12
echo   - 1 Eureka Server
echo   - 1 API Gateway (JWT ^& Routing)
echo   - 1 Auth Service (JWT Generation)
echo   - 8 Backend services
echo   - 1 NGINX (Docker)
echo   - 1 Frontend
echo.
echo ACCESS YOUR APPLICATION:
echo   URL: http://localhost:8111
echo   Login: demo/demo (or guodian/password, vivi/password)
echo.
echo Next Steps:
echo   1. Wait 2-3 minutes for all services to start
echo   2. Open browser: http://localhost:8111
echo   3. Login and test the application
echo.
echo To stop all services:
echo   Run: stop-all-services.bat
echo   Or: taskkill /F /IM java.exe ^&^& taskkill /F /IM node.exe
echo   And: cd infra\nginx ^&^& docker-compose down
echo.
pause

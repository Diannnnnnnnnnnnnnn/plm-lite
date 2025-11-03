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

REM Start Graph Service First (other services depend on it)
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

REM Start Frontend (React)
echo [Frontend - React]
echo   Port: 3000
echo   Starting in new window...
start "PLM - Frontend (3000)" cmd /k "cd /d %PROJECT_ROOT%\frontend && echo ========================================== && echo   Frontend - Port 3000 && echo   React Development Server && echo ========================================== && echo. && npm start"
echo   Frontend starting...
echo.
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   All Services Started Successfully!
echo ========================================
echo.
echo Backend Services:
echo   Graph Service:        http://localhost:8090
echo   Workflow Orchestrator: http://localhost:8086
echo   User Service:         http://localhost:8083
echo   Task Service:         http://localhost:8082
echo   Document Service:     http://localhost:8081
echo   BOM Service:          http://localhost:8089
echo   Change Service:       http://localhost:8084
echo   Search Service:       http://localhost:8091
echo.
echo Frontend:
echo   React UI:             http://localhost:3000
echo.
echo Database Status:
echo   Most services use MySQL
echo   Graph service uses Neo4j
echo   Workflow uses H2 (dev mode) or MySQL (prod mode)
echo   Data will persist across restarts
echo.
echo Total Windows Opened: 9
echo   - 8 Backend services (including Search)
echo   - 1 Frontend
echo.
echo Next Steps:
echo   1. Wait for all services to finish starting (~6 minutes)
echo   2. Open browser: http://localhost:3000
echo   3. Re-create your data (users, documents, parts)
echo   4. Test creating changes and workflows!
echo.
echo To stop all services:
echo   Run: stop-all-services.bat
echo   Or: taskkill /F /IM java.exe ^&^& taskkill /F /IM node.exe
echo.
pause

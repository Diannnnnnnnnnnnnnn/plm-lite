@echo off
REM =====================================================
REM PLM Lite - Intelligent Startup Script
REM Checks current state and starts what's needed
REM =====================================================

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   PLM Lite - System Startup
echo ========================================
echo.

set PROJECT_ROOT=%CD%

REM ==================== Check Docker ====================
echo [1/4] Checking Docker Status...
docker ps >nul 2>&1
if errorlevel 1 (
    echo   ERROR: Docker is not running!
    echo   Please start Docker Desktop and try again.
    pause
    exit /b 1
)
echo   Docker is running ✓
echo.

REM ==================== Start Infrastructure ====================
echo [2/4] Starting Infrastructure Services...
echo.

REM Check if containers are already running
set NEED_START=0

docker ps --format "{{.Names}}" | findstr /C:"plm-elasticsearch" >nul
if errorlevel 1 set NEED_START=1

docker ps --format "{{.Names}}" | findstr /C:"zeebe" >nul
if errorlevel 1 set NEED_START=1

docker ps --format "{{.Names}}" | findstr /C:"redis" >nul
if errorlevel 1 set NEED_START=1

docker ps --format "{{.Names}}" | findstr /C:"minio" >nul
if errorlevel 1 set NEED_START=1

if !NEED_START!==1 (
    echo   Starting Docker containers...
    echo   This may take a few minutes on first run...
    echo.
    docker-compose -f docker-compose-master.yml up -d
    
    if errorlevel 1 (
        echo.
        echo   ERROR: Failed to start Docker containers!
        pause
        exit /b 1
    )
    
    echo.
    echo   Waiting for services to be healthy...
    timeout /t 60 /nobreak >nul
) else (
    echo   All containers already running ✓
)

echo.
echo   Checking service health...

REM Check Elasticsearch
curl -s http://localhost:9200 >nul 2>&1
if errorlevel 1 (
    echo   - Elasticsearch: STARTING... (may need more time)
) else (
    echo   - Elasticsearch: HEALTHY ✓
)

REM Check Zeebe
curl -s http://localhost:9600/ready >nul 2>&1
if errorlevel 1 (
    echo   - Zeebe: STARTING... (may need more time)
) else (
    echo   - Zeebe: HEALTHY ✓
)

REM Check Redis
powershell -Command "Test-NetConnection -ComputerName localhost -Port 6379 -InformationLevel Quiet" >nul 2>&1
if errorlevel 1 (
    echo   - Redis: STARTING...
) else (
    echo   - Redis: HEALTHY ✓
)

REM Check MySQL
powershell -Command "Test-NetConnection -ComputerName localhost -Port 3306 -InformationLevel Quiet" >nul 2>&1
if errorlevel 1 (
    echo   - MySQL: STARTING...
) else (
    echo   - MySQL: HEALTHY ✓
)

REM Check Neo4j
powershell -Command "Test-NetConnection -ComputerName localhost -Port 7687 -InformationLevel Quiet" >nul 2>&1
if errorlevel 1 (
    echo   - Neo4j: STARTING...
) else (
    echo   - Neo4j: HEALTHY ✓
)

REM Check MinIO
curl -s http://localhost:9000/minio/health/live >nul 2>&1
if errorlevel 1 (
    echo   - MinIO: STARTING...
) else (
    echo   - MinIO: HEALTHY ✓
)

echo.

REM ==================== Start Backend Services ====================
echo [3/4] Starting Backend Services...
echo.

REM Check which services are already running
set START_SERVICES=0

for %%P in (8090 8086 8083 8082 8081 8089 8084 8091) do (
    powershell -Command "Test-NetConnection -ComputerName localhost -Port %%P -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        set START_SERVICES=1
    )
)

if !START_SERVICES!==1 (
    echo   Starting services in order...
    echo.
    
    REM Graph Service (8090) - Others depend on it
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8090 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [1/8] Starting Graph Service (8090)...
        start "PLM - Graph Service (8090)" cmd /k "cd /d %PROJECT_ROOT%\infra\graph-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [1/8] Graph Service already running ✓
    )
    
    REM Workflow Orchestrator (8086)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8086 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [2/8] Starting Workflow Orchestrator (8086)...
        start "PLM - Workflow Orchestrator (8086)" cmd /k "cd /d %PROJECT_ROOT%\workflow-orchestrator && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [2/8] Workflow Orchestrator already running ✓
    )
    
    REM User Service (8083)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8083 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [3/8] Starting User Service (8083)...
        start "PLM - User Service (8083)" cmd /k "cd /d %PROJECT_ROOT%\user-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [3/8] User Service already running ✓
    )
    
    REM Task Service (8082)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8082 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [4/8] Starting Task Service (8082)...
        start "PLM - Task Service (8082)" cmd /k "cd /d %PROJECT_ROOT%\task-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [4/8] Task Service already running ✓
    )
    
    REM Document Service (8081)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [5/8] Starting Document Service (8081)...
        start "PLM - Document Service (8081)" cmd /k "cd /d %PROJECT_ROOT%\document-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [5/8] Document Service already running ✓
    )
    
    REM BOM Service (8089)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8089 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [6/8] Starting BOM Service (8089)...
        start "PLM - BOM Service (8089)" cmd /k "cd /d %PROJECT_ROOT%\bom-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [6/8] BOM Service already running ✓
    )
    
    REM Change Service (8084)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8084 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [7/8] Starting Change Service (8084)...
        start "PLM - Change Service (8084)" cmd /k "cd /d %PROJECT_ROOT%\change-service && mvn spring-boot:run"
        timeout /t 45 /nobreak >nul
    ) else (
        echo   [7/8] Change Service already running ✓
    )
    
    REM Search Service (8091)
    powershell -Command "Test-NetConnection -ComputerName localhost -Port 8091 -InformationLevel Quiet" >nul 2>&1
    if errorlevel 1 (
        echo   [8/8] Starting Search Service (8091)...
        start "PLM - Search Service (8091)" cmd /k "cd /d %PROJECT_ROOT%\infra\search-service && mvn spring-boot:run"
        timeout /t 30 /nobreak >nul
    ) else (
        echo   [8/8] Search Service already running ✓
    )
    
) else (
    echo   All backend services already running ✓
)

echo.

REM ==================== Start Frontend ====================
echo [4/4] Starting Frontend...
echo.

powershell -Command "Test-NetConnection -ComputerName localhost -Port 3000 -InformationLevel Quiet" >nul 2>&1
if errorlevel 1 (
    echo   Starting React frontend...
    start "PLM - Frontend (3000)" cmd /k "cd /d %PROJECT_ROOT%\frontend && npm start"
    timeout /t 10 /nobreak >nul
) else (
    echo   Frontend already running ✓
)

echo.
echo ========================================
echo   PLM System Started Successfully!
echo ========================================
echo.
echo Infrastructure Services:
echo   Elasticsearch:        http://localhost:9200
echo   Kibana:               http://localhost:5601
echo   Zeebe:                http://localhost:8088
echo   Operate:              http://localhost:8181
echo   Tasklist:             http://localhost:8182
echo   Connectors:           http://localhost:8085
echo   MinIO Console:        http://localhost:9001
echo   MySQL:                localhost:3306
echo   Neo4j Browser:        http://localhost:7474
echo   Redis:                localhost:6379
echo.
echo Backend Services:
echo   Graph Service:        http://localhost:8090
echo   Workflow Orchestrator:http://localhost:8086
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
echo Management Commands:
echo   View containers:      docker ps
echo   Stop infrastructure:  docker-compose -f docker-compose-master.yml down
echo   Stop all services:    stop-all-services.bat
echo.
echo Next Steps:
echo   1. Wait ~5 minutes for all services to fully start
echo   2. Initialize databases (if first time):
echo      - MySQL: init-mysql-databases-docker.bat
echo      - Elasticsearch: reindex-all-elasticsearch.bat
echo   3. Open browser: http://localhost:3000
echo.
pause



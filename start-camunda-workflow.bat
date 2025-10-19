@echo off
REM =============================================================================
REM  Camunda Workflow Integration - Startup Script
REM  This script compiles and starts the workflow-orchestrator service
REM =============================================================================

echo.
echo ========================================
echo  PLM Lite - Camunda Workflow Startup
echo ========================================
echo.

REM Step 1: Verify Camunda is running
echo [Step 1] Checking if Camunda Docker containers are running...
docker ps | findstr /C:"zeebe" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Camunda Zeebe is not running!
    echo.
    echo Please start Camunda first:
    echo    cd infra\docker-compose-8.7
    echo    docker-compose -f docker-compose-core.yaml up -d
    echo.
    pause
    exit /b 1
)
echo [OK] Camunda Zeebe is running
echo.

REM Step 2: Compile workflow-orchestrator
echo [Step 2] Compiling workflow-orchestrator service...
cd workflow-orchestrator
call mvn clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful
echo.

REM Step 3: Start workflow-orchestrator
echo [Step 3] Starting workflow-orchestrator service...
echo.
echo ========================================
echo  Workflow Orchestrator Starting...
echo  Port: 8086
echo  Zeebe Gateway: localhost:26500
echo ========================================
echo.
echo The BPMN workflows will be automatically deployed on startup.
echo Watch for messages like:
echo   - "BPMN process deployed: document-approval"
echo   - "Zeebe workers activated"
echo.

call mvn spring-boot:run

pause


@echo off
echo ========================================
echo Starting PLM-Lite Services
echo ========================================

REM Kill any existing Java and Node processes
echo Stopping any running services...
powershell -Command "Stop-Process -Name java -Force -ErrorAction SilentlyContinue"
powershell -Command "Stop-Process -Name node -Force -ErrorAction SilentlyContinue"
timeout /t 3 /nobreak > nul

echo.
echo Starting backend services...
echo.

REM Start Graph Service (Port 8090)
echo Starting Graph Service on port 8090...
start "Graph Service" cmd /c "cd infra\graph-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

REM Start BOM Service (Port 8089)
echo Starting BOM Service on port 8089...
start "BOM Service" cmd /c "cd bom-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start Change Service (Port 8084)
echo Starting Change Service on port 8084...
start "Change Service" cmd /c "cd change-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start Document Service (Port 8081)
echo Starting Document Service on port 8081...
start "Document Service" cmd /c "cd document-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start Task Service (Port 8082)
echo Starting Task Service on port 8082...
start "Task Service" cmd /c "cd task-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start User Service (Port 8083)
echo Starting User Service on port 8083...
start "User Service" cmd /c "cd user-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start Workflow Orchestrator (Port 8086)
echo Starting Workflow Orchestrator on port 8086...
start "Workflow Orchestrator" cmd /c "cd workflow-orchestrator && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

echo.
echo Waiting for backend services to initialize (60 seconds)...
timeout /t 60 /nobreak

echo.
echo Starting frontend...
start "Frontend" cmd /c "cd frontend && npm start"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Backend Services:
echo   - Graph Service:         http://localhost:8090
echo   - Document Service:      http://localhost:8081
echo   - Task Service:          http://localhost:8082
echo   - User Service:          http://localhost:8083
echo   - Change Service:        http://localhost:8084
echo   - Workflow Orchestrator: http://localhost:8086
echo   - BOM Service:           http://localhost:8089
echo.
echo Frontend:
echo   - React App:             http://localhost:3001
echo.
echo Please wait 1-2 minutes for all services to fully start.
echo.
pause

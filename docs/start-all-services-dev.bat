@echo off
echo ========================================
echo Starting PLM-Lite Services (H2 Dev)
echo ========================================
echo.
echo Using H2 file databases (Development Mode)
echo.

REM Kill any existing Java and Node processes
echo Stopping any running services...
powershell -Command "Stop-Process -Name java -Force -ErrorAction SilentlyContinue"
powershell -Command "Stop-Process -Name node -Force -ErrorAction SilentlyContinue"
timeout /t 3 /nobreak > nul

echo.
echo Starting backend services with H2 dev profile...
echo.

REM Start Graph Service (Port 8090)
echo Starting Graph Service on port 8090...
start "Graph Service" cmd /c "cd infra\graph-service && mvn spring-boot:run"
timeout /t 5 /nobreak > nul

REM Start BOM Service (Port 8089) - Dev Profile
echo Starting BOM Service on port 8089 (H2)...
start "BOM Service" cmd /c "cd bom-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 2 /nobreak > nul

REM Start Change Service (Port 8084) - Dev Profile
echo Starting Change Service on port 8084 (H2)...
start "Change Service" cmd /c "cd change-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 2 /nobreak > nul

REM Start Document Service (Port 8081) - Dev Profile
echo Starting Document Service on port 8081 (H2)...
start "Document Service" cmd /c "cd document-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 2 /nobreak > nul

REM Start Task Service (Port 8082) - Dev Profile
echo Starting Task Service on port 8082 (H2)...
start "Task Service" cmd /c "cd task-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 2 /nobreak > nul

REM Start User Service (Port 8083) - Dev Profile
echo Starting User Service on port 8083 (H2)...
start "User Service" cmd /c "cd user-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
timeout /t 2 /nobreak > nul

REM Start Workflow Orchestrator (Port 8086)
echo Starting Workflow Orchestrator on port 8086...
start "Workflow Orchestrator" cmd /c "cd workflow-orchestrator && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

REM Start Search Service (Port 8091)
echo Starting Search Service on port 8091...
start "Search Service" cmd /c "cd infra\search-service && mvn spring-boot:run"
timeout /t 2 /nobreak > nul

echo.
echo Waiting for backend services to initialize (60 seconds)...
timeout /t 60 /nobreak

echo.
echo Starting frontend...
start "Frontend" cmd /c "cd frontend && npm start"

echo.
echo ========================================
echo All services are starting with H2!
echo ========================================
echo.
echo Backend Services:
echo   - Graph Service:         http://localhost:8090
echo   - Document Service:      http://localhost:8081 (H2: ./data/documentdb)
echo   - Task Service:          http://localhost:8082 (H2: ./data/taskdb)
echo   - User Service:          http://localhost:8083 (H2: ./data/userdb)
echo   - Change Service:        http://localhost:8084 (H2: ./data/changedb)
echo   - Workflow Orchestrator: http://localhost:8086
echo   - BOM Service:           http://localhost:8089 (H2: ./data/bomdb)
echo   - Search Service:        http://localhost:8091
echo.
echo Frontend:
echo   - React App:             http://localhost:3001
echo.
echo Database: H2 file-based databases in ./data/
echo H2 Consoles:
echo   - User Service:     http://localhost:8083/h2-console
echo   - Task Service:     http://localhost:8082/h2-console (if enabled)
echo   - Document Service: http://localhost:8081/h2-console
echo   - BOM Service:      http://localhost:8089/h2-console
echo   - Change Service:   http://localhost:8084/h2-console
echo.
echo Please wait 1-2 minutes for all services to fully start.
echo.
pause






@echo off
echo ========================================
echo   PLM Workflow System Diagnostic
echo ========================================
echo.

echo [1] Checking Workflow Orchestrator (Port 8086)...
curl -s http://localhost:8086/actuator/health
echo.
echo.

echo [2] Checking Document Service (Port 8081)...
curl -s http://localhost:8081/actuator/health
echo.
echo.

echo [3] Checking Task Service (Port 8082)...
curl -s http://localhost:8082/actuator/health
echo.
echo.

echo [4] Listing all tasks in Task Service...
curl -s http://localhost:8082/api/tasks | json_pp
echo.

echo ========================================
echo   Diagnostic Complete
echo ========================================
pause






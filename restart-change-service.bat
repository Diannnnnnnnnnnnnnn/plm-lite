@echo off
echo ========================================
echo Restarting Change Service
echo ========================================

REM Kill change-service process on port 8084
echo.
echo Stopping change-service...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8084') do (
    echo Found process %%a
    taskkill /F /PID %%a 2>nul
)
echo Change service stopped
timeout /t 3 /nobreak > nul

REM Start change-service
echo.
echo Starting change-service on port 8084...
start "Change Service" cmd /c "cd change-service && mvn spring-boot:run"

echo.
echo ========================================
echo Change Service is restarting!
echo ========================================
echo.
echo Wait 30-60 seconds for the service to fully start.
echo Change Service: http://localhost:8084
echo.
pause


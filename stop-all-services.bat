@echo off
echo ========================================
echo Stopping PLM-Lite Services
echo ========================================

echo Stopping all Java processes (backend services)...
powershell -Command "Stop-Process -Name java -Force -ErrorAction SilentlyContinue"

echo Stopping all Node processes (frontend)...
powershell -Command "Stop-Process -Name node -Force -ErrorAction SilentlyContinue"

echo.
echo ========================================
echo All services have been stopped!
echo ========================================
echo.
pause

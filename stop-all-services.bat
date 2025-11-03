@echo off
REM ============================================
REM Stop All PLM Services (Backend + Frontend)
REM ============================================

echo.
echo Stopping all PLM services...
echo.

REM Stop all Java processes (Backend services)
echo Stopping backend services (Java)...
taskkill /F /IM java.exe 2>nul
if %ERRORLEVEL% EQU 0 (
    echo   Backend services stopped.
) else (
    echo   No Java services were running.
)

REM Stop all Node.js processes (Frontend)
echo Stopping frontend (Node.js)...
taskkill /F /IM node.exe 2>nul
if %ERRORLEVEL% EQU 0 (
    echo   Frontend stopped.
) else (
    echo   No Node.js services were running.
)

echo.
echo All services stopped successfully!
echo.
pause

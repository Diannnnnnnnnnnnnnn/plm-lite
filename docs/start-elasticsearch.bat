@echo off
REM ============================================
REM Start Elasticsearch and Kibana for PLM-Lite
REM ============================================

echo.
echo ========================================
echo   Starting Elasticsearch Infrastructure
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop first.
    echo.
    pause
    exit /b 1
)

echo Starting Elasticsearch and Kibana...
echo.

docker-compose -f docker-compose-elasticsearch.yml up -d

echo.
echo Waiting for services to be healthy...
echo This may take 30-60 seconds...
echo.

timeout /t 10 /nobreak >nul

REM Wait for Elasticsearch to be ready
:wait_es
echo Checking Elasticsearch status...
curl -s http://localhost:9200/_cluster/health >nul 2>&1
if errorlevel 1 (
    echo   Still starting...
    timeout /t 5 /nobreak >nul
    goto wait_es
)

echo.
echo ========================================
echo   Elasticsearch Infrastructure Ready!
echo ========================================
echo.
echo Services:
echo   Elasticsearch: http://localhost:9200
echo   Kibana:        http://localhost:5601
echo.
echo Cluster Info:
curl -s http://localhost:9200 | findstr "name cluster_name version number"
echo.
echo.
echo Next Steps:
echo   1. Wait 20-30 seconds for Kibana to fully start
echo   2. Open Kibana at http://localhost:5601
echo   3. Run start-all-services.bat to start PLM services
echo.
echo To stop Elasticsearch:
echo   Run: stop-elasticsearch.bat
echo.
pause





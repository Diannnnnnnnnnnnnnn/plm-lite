@echo off
REM =====================================================
REM Clean Elasticsearch Data - Delete All Indices
REM =====================================================

echo ========================================
echo Cleaning Elasticsearch Data
echo ========================================
echo.

REM Check if Elasticsearch is running
docker ps --filter "name=plm-elasticsearch" --format "{{.Names}}" | findstr /C:"plm-elasticsearch" >nul

if %ERRORLEVEL% NEQ 0 (
    echo Elasticsearch container 'plm-elasticsearch' is not running.
    echo Please start Elasticsearch first:
    echo   docker-compose -f docker-compose-elasticsearch.yml up -d
    exit /b 1
)

echo Waiting for Elasticsearch to be ready...
timeout /t 5 /nobreak > nul

echo.
echo Deleting all indices...
echo.

REM Delete all indices (except system indices)
curl -X DELETE "localhost:9200/parts" 2>nul
echo Parts index deleted

curl -X DELETE "localhost:9200/documents" 2>nul
echo Documents index deleted

curl -X DELETE "localhost:9200/changes" 2>nul
echo Changes index deleted

curl -X DELETE "localhost:9200/tasks" 2>nul
echo Tasks index deleted

curl -X DELETE "localhost:9200/boms" 2>nul
echo BOMs index deleted

echo.
echo All Elasticsearch indices deleted successfully!
echo Indices will be recreated automatically when services start.
echo.



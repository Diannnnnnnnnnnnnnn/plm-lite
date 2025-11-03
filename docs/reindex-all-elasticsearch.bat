@echo off
REM ============================================
REM Reindex All PLM Data to Elasticsearch
REM ============================================

echo.
echo ========================================
echo   Reindexing All PLM Data
echo   to Elasticsearch
echo ========================================
echo.

echo This will index all existing data from MySQL to Elasticsearch.
echo Make sure all services are running!
echo.
pause

echo.
echo [1/4] Reindexing Documents...
echo ----------------------------------------
curl -X POST http://localhost:8081/api/v1/documents/admin/reindex
echo.
echo Documents reindexed ✅
echo.
timeout /t 2 /nobreak >nul

echo.
echo [2/4] Reindexing Changes...
echo ----------------------------------------
curl -X POST http://localhost:8084/api/changes/admin/reindex
echo.
echo Changes reindexed ✅
echo.
timeout /t 2 /nobreak >nul

echo.
echo [3/4] Reindexing Tasks...
echo ----------------------------------------
curl -X POST http://localhost:8082/api/tasks/admin/reindex
echo.
echo Tasks reindexed ✅
echo.
timeout /t 2 /nobreak >nul

echo.
echo [4/4] Reindexing BOMs...
echo ----------------------------------------
curl -X POST http://localhost:8089/api/boms/admin/reindex
echo.
echo BOMs reindexed ✅
echo.

echo.
echo ========================================
echo   Reindexing Complete!
echo ========================================
echo.
echo Verification:
echo   Check indices: curl http://localhost:9200/_cat/indices?v
echo   Test search:   curl "http://localhost:8091/api/search/global?q=motor"
echo.
echo Next: Open http://localhost:3000 and try Global Search!
echo.
pause


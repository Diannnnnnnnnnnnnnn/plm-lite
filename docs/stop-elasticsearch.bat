@echo off
REM ============================================
REM Stop Elasticsearch and Kibana
REM ============================================

echo.
echo ========================================
echo   Stopping Elasticsearch Infrastructure
echo ========================================
echo.

docker-compose -f docker-compose-elasticsearch.yml down

echo.
echo Elasticsearch and Kibana stopped.
echo.
echo Note: Data is preserved in Docker volume 'elasticsearch-data'
echo To completely remove data:
echo   docker-compose -f docker-compose-elasticsearch.yml down -v
echo.
pause





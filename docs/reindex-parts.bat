@echo off
echo ========================================
echo Reindexing Parts to Elasticsearch
echo ========================================
echo.

echo Making POST request to BOM service...
curl -X POST http://localhost:8089/api/v1/parts/elasticsearch/reindex

echo.
echo.
echo ========================================
echo Reindexing Complete!
echo ========================================
pause





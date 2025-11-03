@echo off
REM =====================================================
REM Clean Neo4j Data - Delete All Nodes and Relationships
REM =====================================================

echo ========================================
echo Cleaning Neo4j Data
echo ========================================
echo.

REM Check if Neo4j is running
docker ps --filter "name=plm-neo4j" --format "{{.Names}}" | findstr /C:"plm-neo4j" >nul

if %ERRORLEVEL% NEQ 0 (
    echo Neo4j container 'plm-neo4j' is not running.
    echo Please start infrastructure services first:
    echo   docker-compose -f infra/docker-compose-infrastructure.yaml up -d
    exit /b 1
)

echo Waiting for Neo4j to be ready...
timeout /t 5 /nobreak > nul

echo.
echo Deleting all nodes and relationships...
echo.

REM Delete all nodes and relationships in batches
docker exec plm-neo4j cypher-shell -u neo4j -p password "MATCH (n) DETACH DELETE n;"

echo.
echo All Neo4j graph data deleted successfully!
echo Graph database is now empty.
echo.



# =====================================================
# Clean Neo4j Data - Delete All Nodes and Relationships
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning Neo4j Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Neo4j is running
$neo4jRunning = docker ps --filter "name=plm-neo4j" --format "{{.Names}}" | Select-String "plm-neo4j"

if (-not $neo4jRunning) {
    Write-Host "Neo4j container 'plm-neo4j' is not running." -ForegroundColor Red
    Write-Host "Please start infrastructure services first:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f infra/docker-compose-infrastructure.yaml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Waiting for Neo4j to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Deleting all nodes and relationships..." -ForegroundColor Yellow
Write-Host ""

# Delete all nodes and relationships in batches
docker exec plm-neo4j cypher-shell -u neo4j -p password "MATCH (n) DETACH DELETE n;"

Write-Host ""
Write-Host "All Neo4j graph data deleted successfully!" -ForegroundColor Green
Write-Host "Graph database is now empty." -ForegroundColor Green
Write-Host ""



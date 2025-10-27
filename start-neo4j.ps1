# ==========================================
# Start Neo4j for PLM-Lite
# ==========================================

Write-Host "🚀 Starting Neo4j Graph Database..." -ForegroundColor Cyan

# Navigate to infrastructure directory
Set-Location -Path "infra"

# Start Neo4j using docker-compose
Write-Host "Starting Neo4j container..." -ForegroundColor Yellow
docker-compose -f docker-compose-infrastructure.yaml up -d neo4j

# Wait for Neo4j to be ready
Write-Host "Waiting for Neo4j to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check Neo4j health
$maxAttempts = 30
$attempt = 0
$isReady = $false

while (-not $isReady -and $attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:7474" -Method GET -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $isReady = $true
        }
    } catch {
        $attempt++
        Write-Host "Attempt $attempt/$maxAttempts - Neo4j not ready yet..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
}

if ($isReady) {
    Write-Host "✅ Neo4j is ready!" -ForegroundColor Green
    Write-Host ""
    Write-Host "===========================================" -ForegroundColor Cyan
    Write-Host "Neo4j Information:" -ForegroundColor Cyan
    Write-Host "===========================================" -ForegroundColor Cyan
    Write-Host "Browser UI: http://localhost:7474" -ForegroundColor White
    Write-Host "Bolt URL:   bolt://localhost:7687" -ForegroundColor White
    Write-Host "Username:   neo4j" -ForegroundColor White
    Write-Host "Password:   password" -ForegroundColor White
    Write-Host "===========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Next Steps:" -ForegroundColor Yellow
    Write-Host "1. Open Neo4j Browser: http://localhost:7474" -ForegroundColor White
    Write-Host "2. Login with credentials above" -ForegroundColor White
    Write-Host "3. Run initialization script: graph-service/neo4j-init.cypher" -ForegroundColor White
    Write-Host "4. Start graph-service: cd infra/graph-service && mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "❌ Neo4j failed to start within the timeout period" -ForegroundColor Red
    Write-Host "Check Docker logs: docker logs plm-neo4j" -ForegroundColor Yellow
}

# Return to root directory
Set-Location -Path ".."


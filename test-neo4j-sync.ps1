# Test Neo4j Real-Time Sync
Write-Host "🔍 Neo4j Real-Time Sync Diagnostic Test" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check Graph Service Health
Write-Host "1️⃣  Testing Graph Service Health..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/api/graph/sync/health" -UseBasicParsing
    Write-Host "✅ Graph Service is running!" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Graph Service is NOT running!" -ForegroundColor Red
    Write-Host "   Please start it: cd infra\graph-service && mvn spring-boot:run" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Check Neo4j Connection
Write-Host "2️⃣  Testing Neo4j Connection..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:7474" -UseBasicParsing
    Write-Host "✅ Neo4j is running!" -ForegroundColor Green
} catch {
    Write-Host "❌ Neo4j is NOT running!" -ForegroundColor Red
    Write-Host "   Please start it or check docker-compose" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 3: Create a test Part via BOM Service
Write-Host "3️⃣  Creating test Part..." -ForegroundColor Yellow
$partBody = @{
    title = "Test Part for Neo4j Sync"
    stage = "IN_WORK"
    level = "PART"
    creator = "test-user"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Method Post -Uri "http://localhost:8089/api/bom/parts" -Body $partBody -ContentType "application/json"
    $partId = $response.id
    Write-Host "✅ Part created successfully!" -ForegroundColor Green
    Write-Host "   Part ID: $partId" -ForegroundColor Gray
    Write-Host "   Title: $($response.title)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to create Part!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Make sure BOM Service is running on port 8089" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test 4: Wait and check Neo4j
Write-Host "4️⃣  Waiting 2 seconds for sync..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "5️⃣  Verification Steps:" -ForegroundColor Yellow
Write-Host "   1. Open Neo4j Browser: http://localhost:7474" -ForegroundColor Cyan
Write-Host "   2. Login with: neo4j / password" -ForegroundColor Cyan
Write-Host "   3. Run this query:" -ForegroundColor Cyan
Write-Host ""
Write-Host "      MATCH (p:Part {id: '$partId'})" -ForegroundColor White
Write-Host "      RETURN p" -ForegroundColor White
Write-Host ""
Write-Host "   4. You should see your test part!" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 Alternative: Check all Parts in Neo4j:" -ForegroundColor Yellow
Write-Host "   MATCH (p:Part) RETURN p LIMIT 25" -ForegroundColor White
Write-Host ""

Write-Host "✅ Test completed!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 BOM Service Logs:" -ForegroundColor Yellow
Write-Host "   Look for: '✅ Part $partId synced to graph successfully'" -ForegroundColor Gray
Write-Host "   Or: '⚠️ Graph Service unavailable'" -ForegroundColor Gray
Write-Host ""


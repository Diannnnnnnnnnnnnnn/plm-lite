# Test script to verify user sync to Neo4j

Write-Host "`n=== Testing User Sync to Neo4j ===`n" -ForegroundColor Cyan

# Step 1: Check if graph-service is running
Write-Host "1. Checking graph-service health..." -ForegroundColor Yellow
try {
    $graphHealth = Invoke-RestMethod -Uri "http://localhost:8090/api/graph/sync/health" -Method Get
    Write-Host "   ✅ Graph service is running: $graphHealth" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Graph service is NOT running or unreachable!" -ForegroundColor Red
    Write-Host "   Please start graph-service first: cd infra/graph-service && mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Step 2: Check if user-service is running
Write-Host "`n2. Checking user-service health..." -ForegroundColor Yellow
try {
    $userHealth = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health" -Method Get
    Write-Host "   ✅ User service is running" -ForegroundColor Green
} catch {
    Write-Host "   ❌ User service is NOT running!" -ForegroundColor Red
    Write-Host "   Please start user-service first: cd user-service && mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Step 3: Check Neo4j is running
Write-Host "`n3. Checking Neo4j..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:7474" -Method Get -TimeoutSec 5
    Write-Host "   ✅ Neo4j is running" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Neo4j is NOT running!" -ForegroundColor Red
    Write-Host "   Please start Neo4j: docker start plm-neo4j" -ForegroundColor Yellow
    exit 1
}

# Step 4: Create a test user
Write-Host "`n4. Creating a test user..." -ForegroundColor Yellow
$testUser = @{
    username = "neo4j_test_user_$(Get-Date -Format 'HHmmss')"
    password = "password123"
    roles = @("ROLE_USER")
} | ConvertTo-Json

try {
    $newUser = Invoke-RestMethod -Uri "http://localhost:8083/users" `
        -Method Post `
        -ContentType "application/json" `
        -Body $testUser
    
    Write-Host "   ✅ User created with ID: $($newUser.id)" -ForegroundColor Green
    $userId = $newUser.id
    
    # Wait a bit for sync to happen
    Write-Host "   Waiting 3 seconds for sync to complete..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    
} catch {
    Write-Host "   ❌ Failed to create user: $_" -ForegroundColor Red
    exit 1
}

# Step 5: Check if user exists in Neo4j
Write-Host "`n5. Checking if user exists in Neo4j..." -ForegroundColor Yellow
Write-Host "   Please run this query in Neo4j Browser (http://localhost:7474):" -ForegroundColor Cyan
Write-Host "   " -NoNewline
Write-Host "MATCH (u:User {id: '$userId'}) RETURN u" -ForegroundColor White

Write-Host "`n   Alternative - Check all users:" -ForegroundColor Cyan
Write-Host "   " -NoNewline
Write-Host "MATCH (u:User) RETURN u.id, u.username ORDER BY u.id DESC LIMIT 10" -ForegroundColor White

Write-Host "`n=== Test Complete ===`n" -ForegroundColor Cyan
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open Neo4j Browser: http://localhost:7474" -ForegroundColor White
Write-Host "2. Login (username: neo4j, password: password)" -ForegroundColor White
Write-Host "3. Run the query above to check if user was synced" -ForegroundColor White
Write-Host "`nIf user is NOT in Neo4j, check user-service logs for errors.`n" -ForegroundColor Gray


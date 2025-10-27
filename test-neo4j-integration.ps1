# ==========================================
# Test Neo4j Integration for PLM-Lite
# ==========================================

Write-Host "🧪 Testing Neo4j Integration..." -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8090"

# Helper function to make HTTP requests
function Invoke-ApiRequest {
    param (
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null
    )
    
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Method $Method -Uri $Uri -Body $Body -ContentType "application/json" -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Method $Method -Uri $Uri -ErrorAction Stop
        }
        return @{ Success = $true; Data = $response }
    } catch {
        return @{ Success = $false; Error = $_.Exception.Message }
    }
}

# Test 1: Health Check
Write-Host "Test 1: Graph Service Health Check" -ForegroundColor Yellow
$result = Invoke-ApiRequest -Method GET -Uri "$baseUrl/actuator/health"
if ($result.Success) {
    Write-Host "✅ Graph service is healthy" -ForegroundColor Green
    Write-Host "   Neo4j Status: $($result.Data.components.neo4j.status)" -ForegroundColor Gray
} else {
    Write-Host "❌ Graph service health check failed: $($result.Error)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Create Users
Write-Host "Test 2: Create Users" -ForegroundColor Yellow
$user1 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/user?id=test-user-1&name=John Doe"
$user2 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/user?id=test-user-2&name=Jane Smith"

if ($user1.Success -and $user2.Success) {
    Write-Host "✅ Users created successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to create users" -ForegroundColor Red
}
Write-Host ""

# Test 3: Create Tasks
Write-Host "Test 3: Create Tasks" -ForegroundColor Yellow
$task1 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/task?id=test-task-1&title=Review Design"
$task2 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/task?id=test-task-2&title=Update BOM"

if ($task1.Success -and $task2.Success) {
    Write-Host "✅ Tasks created successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to create tasks" -ForegroundColor Red
}
Write-Host ""

# Test 4: Assign Tasks to Users
Write-Host "Test 4: Assign Tasks to Users" -ForegroundColor Yellow
$assign1 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/assign?userId=test-user-1&taskId=test-task-1"
$assign2 = Invoke-ApiRequest -Method POST -Uri "$baseUrl/graph/assign?userId=test-user-2&taskId=test-task-2"

if ($assign1.Success -and $assign2.Success) {
    Write-Host "✅ Tasks assigned successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to assign tasks" -ForegroundColor Red
}
Write-Host ""

# Test Summary
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To verify the graph in Neo4j Browser:" -ForegroundColor Yellow
Write-Host "1. Open http://localhost:7474" -ForegroundColor White
Write-Host "2. Run this query:" -ForegroundColor White
Write-Host ""
Write-Host "   MATCH (n) RETURN n LIMIT 100" -ForegroundColor Cyan
Write-Host ""
Write-Host "Or view relationships:" -ForegroundColor White
Write-Host ""
Write-Host "   MATCH p=()-[r:ASSIGNED_TO]->() RETURN p" -ForegroundColor Cyan
Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan


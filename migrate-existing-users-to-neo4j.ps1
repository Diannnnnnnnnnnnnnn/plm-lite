# Script to migrate existing users from MySQL to Neo4j

Write-Host "`n=== Migrating Existing Users to Neo4j ===`n" -ForegroundColor Cyan

# Step 1: Get all users from user-service
Write-Host "1. Fetching all users from user-service..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "http://localhost:8083/users" -Method Get
    Write-Host "   Found $($users.Count) users in database" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Failed to fetch users: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Sync each user to Neo4j
Write-Host "`n2. Syncing users to Neo4j..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

foreach ($user in $users) {
    try {
        # Get first role or null
        $role = if ($user.roles -and $user.roles.Count -gt 0) { $user.roles[0] } else { $null }
        
        # Create sync DTO
        $syncDto = @{
            id = [string]$user.id
            username = $user.username
            email = $null
            department = $null
            role = $role
            managerId = $null
        } | ConvertTo-Json
        
        # Sync to graph-service
        $response = Invoke-RestMethod -Uri "http://localhost:8090/api/graph/sync/user" `
            -Method Post `
            -Body $syncDto `
            -ContentType "application/json"
        
        Write-Host "   ✅ Synced: $($user.username) (ID: $($user.id))" -ForegroundColor Green
        $successCount++
        
    } catch {
        Write-Host "   ❌ Failed: $($user.username) - $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
    
    # Small delay to avoid overwhelming the service
    Start-Sleep -Milliseconds 100
}

# Step 3: Summary
Write-Host "`n=== Migration Complete ===`n" -ForegroundColor Cyan
Write-Host "Successfully synced: $successCount users" -ForegroundColor Green
if ($failCount -gt 0) {
    Write-Host "Failed to sync: $failCount users" -ForegroundColor Red
}

# Step 4: Verify in Neo4j
Write-Host "`n=== Verification ===`n" -ForegroundColor Cyan
Write-Host "Run this query in Neo4j Browser (http://localhost:7474):" -ForegroundColor Yellow
Write-Host ""
Write-Host "MATCH (u:User) RETURN u.id, u.username, u.role ORDER BY u.id" -ForegroundColor White
Write-Host ""
Write-Host "You should see all $($users.Count) users listed." -ForegroundColor Gray
Write-Host ""


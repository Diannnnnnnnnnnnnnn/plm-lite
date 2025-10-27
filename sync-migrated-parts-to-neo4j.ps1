# Sync Migrated Parts to Neo4j
# Run this AFTER running migrate-bom-to-part.sql

Write-Host "🔄 Syncing Migrated Parts to Neo4j" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

$bomUrl = "http://localhost:8089"
$graphUrl = "http://localhost:8090"

# Check services
Write-Host "Checking services..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "$graphUrl/api/graph/sync/health" -UseBasicParsing | Out-Null
    Write-Host "✅ Graph Service is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Graph Service is NOT running!" -ForegroundColor Red
    exit 1
}

try {
    Invoke-WebRequest -Uri "$bomUrl/parts" -UseBasicParsing | Out-Null
    Write-Host "✅ BOM Service is running" -ForegroundColor Green
} catch {
    Write-Host "❌ BOM Service is NOT running!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Get all parts
Write-Host "Fetching all parts from database..." -ForegroundColor Yellow
try {
    $parts = Invoke-RestMethod -Uri "$bomUrl/parts" -Method Get
    Write-Host "✅ Found $($parts.Count) parts to sync" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to fetch parts" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Sync each part to Neo4j
Write-Host "Syncing parts to Neo4j..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

foreach ($part in $parts) {
    try {
        $syncRequest = @{
            id = $part.id
            title = $part.title
            description = $part.description
            stage = $part.stage
            status = $part.status
            level = $part.level
            creator = $part.creator
            createTime = $part.createTime
        } | ConvertTo-Json

        Invoke-RestMethod -Uri "$graphUrl/api/graph/sync/part" `
                          -Method Post `
                          -Body $syncRequest `
                          -ContentType "application/json" | Out-Null
        
        Write-Host "  ✅ Synced: $($part.title) [$($part.id)]" -ForegroundColor Green
        $successCount++
    } catch {
        Write-Host "  ❌ Failed: $($part.title) - $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""

# Sync Part Usages (relationships)
Write-Host "Syncing part relationships..." -ForegroundColor Yellow

foreach ($part in $parts) {
    try {
        # Get children for this part
        $children = Invoke-RestMethod -Uri "$bomUrl/parts/$($part.id)/children" -Method Get
        
        foreach ($child in $children) {
            # Extract first child usage to get quantity
            $usage = $part.childUsages | Where-Object { $_.childPartId -eq $child.id } | Select-Object -First 1
            
            if ($usage) {
                $usageRequest = @{
                    parentPartId = $part.id
                    childPartId = $child.id
                    quantity = $usage.quantity
                } | ConvertTo-Json

                Invoke-RestMethod -Uri "$graphUrl/api/graph/sync/part-usage" `
                                  -Method Post `
                                  -Body $usageRequest `
                                  -ContentType "application/json" | Out-Null
                
                Write-Host "  ✅ Linked: $($part.title) -> $($child.title) (qty: $($usage.quantity))" -ForegroundColor Green
            }
        }
    } catch {
        # Skip if no children or error
    }
}

Write-Host ""

# Summary
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "✅ SYNC COMPLETE" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Parts synced successfully: $successCount" -ForegroundColor Green
Write-Host "Parts failed: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host ""
Write-Host "📊 Verify in Neo4j Browser:" -ForegroundColor Yellow
Write-Host "   http://localhost:7474" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Run this query:" -ForegroundColor Yellow
Write-Host "   MATCH (p:Part) RETURN p LIMIT 50" -ForegroundColor White
Write-Host ""
Write-Host "   Check relationships:" -ForegroundColor Yellow
Write-Host "   MATCH (p1:Part)-[r:HAS_CHILD]->(p2:Part) RETURN p1, r, p2 LIMIT 25" -ForegroundColor White
Write-Host ""


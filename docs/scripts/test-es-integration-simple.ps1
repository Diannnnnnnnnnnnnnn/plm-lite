# Elasticsearch Integration Test Script - Part, Change, and Task Services

Write-Host "`n=== ELASTICSEARCH INTEGRATION TEST ===" -ForegroundColor Cyan

$testsPassed = 0
$testsFailed = 0

function Test-Feature {
    param([string]$Name, [bool]$Passed)
    if ($Passed) {
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:testsPassed++
    } else {
        Write-Host "[FAIL] $Name" -ForegroundColor Red
        $script:testsFailed++
    }
}

# Check Elasticsearch
Write-Host "`n--- Infrastructure Checks ---" -ForegroundColor Yellow
try {
    $esHealth = Invoke-RestMethod "http://localhost:9200/_cluster/health" -ErrorAction Stop
    Test-Feature "Elasticsearch is running" $true
} catch {
    Test-Feature "Elasticsearch is running" $false
    Write-Host "Please start Elasticsearch first" -ForegroundColor Red
    exit 1
}

# Check services
try {
    Invoke-RestMethod "http://localhost:8089/actuator/health" -ErrorAction Stop | Out-Null
    Test-Feature "BOM Service is running" $true
} catch {
    Test-Feature "BOM Service is running" $false
}

try {
    Invoke-RestMethod "http://localhost:8084/actuator/health" -ErrorAction Stop | Out-Null
    Test-Feature "Change Service is running" $true
} catch {
    Test-Feature "Change Service is running" $false
}

try {
    Invoke-RestMethod "http://localhost:8082/api/tasks" -ErrorAction Stop | Out-Null
    Test-Feature "Task Service is running" $true
} catch {
    Test-Feature "Task Service is running" $false
}

# PART SERVICE TESTS
Write-Host "`n--- Part Service Tests ---" -ForegroundColor Yellow

$testPartId = $null
try {
    $partData = @{
        title = "Test Motor - ES Integration"
        description = "Test part for ES integration"
        stage = "DEVELOPMENT"
        status = "IN_WORK"
        level = "L1"
        creator = "test-user"
    }
    
    $result = Invoke-RestMethod -Uri "http://localhost:8089/api/v1/parts" `
        -Method Post `
        -Body ($partData | ConvertTo-Json) `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    $testPartId = $result.id
    Test-Feature "Create Part" $true
    Write-Host "  Part ID: $testPartId" -ForegroundColor Gray
    
    Start-Sleep -Seconds 2
    
    $esResult = Invoke-RestMethod "http://localhost:9200/parts/_search?q=id:$testPartId" -ErrorAction Stop
    $indexed = $esResult.hits.total.value -gt 0
    Test-Feature "Part auto-indexed to ES" $indexed
    
} catch {
    Test-Feature "Create Part" $false
    Write-Host "  Error: $_" -ForegroundColor Red
}

# Update part
if ($testPartId) {
    try {
        Invoke-RestMethod -Uri "http://localhost:8089/api/v1/parts/$testPartId/stage?stage=PRODUCTION" `
            -Method Put -ErrorAction Stop | Out-Null
        Test-Feature "Update Part" $true
        
        Start-Sleep -Seconds 2
        
        $esDoc = Invoke-RestMethod "http://localhost:9200/parts/_doc/$testPartId" -ErrorAction Stop
        $updated = $esDoc._source.stage -eq "PRODUCTION"
        Test-Feature "Part re-indexed after update" $updated
    } catch {
        Test-Feature "Update Part" $false
    }
}

# TASK SERVICE TESTS
Write-Host "`n--- Task Service Tests ---" -ForegroundColor Yellow

$testTaskId = $null
try {
    $taskData = @{
        name = "Test Task - ES Integration"
        description = "Test task for ES"
        userId = 1
        assignedTo = "test-user"
        taskStatus = "PENDING"
    }
    
    $result = Invoke-RestMethod -Uri "http://localhost:8082/api/tasks" `
        -Method Post `
        -Body ($taskData | ConvertTo-Json) `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    $testTaskId = $result.id
    Test-Feature "Create Task" $true
    Write-Host "  Task ID: $testTaskId" -ForegroundColor Gray
    
    Start-Sleep -Seconds 2
    
    $esResult = Invoke-RestMethod "http://localhost:9200/tasks/_search?q=id:$testTaskId" -ErrorAction Stop
    $indexed = $esResult.hits.total.value -gt 0
    Test-Feature "Task auto-indexed to ES" $indexed
    
} catch {
    Test-Feature "Create Task" $false
    Write-Host "  Error: $_" -ForegroundColor Red
}

# Update task
if ($testTaskId) {
    try {
        $updateData = @{
            name = "Updated Task - ES Test"
            description = "Updated for ES testing"
            taskStatus = "IN_PROGRESS"
        }
        
        Invoke-RestMethod -Uri "http://localhost:8082/api/tasks/$testTaskId" `
            -Method Put `
            -Body ($updateData | ConvertTo-Json) `
            -ContentType "application/json" `
            -ErrorAction Stop | Out-Null
        Test-Feature "Update Task" $true
        
        Start-Sleep -Seconds 2
        
        $esDoc = Invoke-RestMethod "http://localhost:9200/tasks/_doc/$testTaskId" -ErrorAction Stop
        $updated = $esDoc._source.name -like "*Updated*"
        Test-Feature "Task re-indexed after update" $updated
    } catch {
        Test-Feature "Update Task" $false
    }
}

# CHANGE SERVICE TESTS
Write-Host "`n--- Change Service Tests ---" -ForegroundColor Yellow

try {
    $changes = Invoke-RestMethod "http://localhost:9200/changes/_search" -ErrorAction Stop
    $count = $changes.hits.total.value
    Test-Feature "Changes index accessible" $true
    Write-Host "  $count changes indexed" -ForegroundColor Gray
} catch {
    Test-Feature "Changes index accessible" $false
}

# UNIFIED SEARCH TEST
Write-Host "`n--- Unified Search Service ---" -ForegroundColor Yellow

try {
    $searchResult = Invoke-RestMethod "http://localhost:8091/api/v1/search?q=test" -ErrorAction Stop
    Test-Feature "Unified search works" $true
    Write-Host "  Total hits: $($searchResult.totalHits)" -ForegroundColor Gray
} catch {
    Test-Feature "Unified search works" $false
}

# CLEANUP
Write-Host "`n--- Cleanup ---" -ForegroundColor Yellow
Write-Host "Delete test data? (Y/N): " -NoNewline
$cleanup = Read-Host

if ($cleanup -eq "Y" -or $cleanup -eq "y") {
    if ($testPartId) {
        try {
            Invoke-RestMethod "http://localhost:8089/api/v1/parts/$testPartId" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "[OK] Test part deleted" -ForegroundColor Green
        } catch {
            Write-Host "[WARN] Could not delete test part" -ForegroundColor Yellow
        }
    }
    
    if ($testTaskId) {
        try {
            Invoke-RestMethod "http://localhost:8082/api/tasks/$testTaskId" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "[OK] Test task deleted" -ForegroundColor Green
        } catch {
            Write-Host "[WARN] Could not delete test task" -ForegroundColor Yellow
        }
    }
}

# SUMMARY
Write-Host "`n=== TEST SUMMARY ===" -ForegroundColor Cyan
$total = $testsPassed + $testsFailed
Write-Host "Total: $total | Passed: $testsPassed | Failed: $testsFailed" -ForegroundColor White
$rate = [math]::Round(($testsPassed / $total) * 100, 1)
Write-Host "Success Rate: $rate%" -ForegroundColor $(if ($rate -ge 90) { "Green" } elseif ($rate -ge 70) { "Yellow" } else { "Red" })

if ($testsFailed -eq 0) {
    Write-Host "`nALL TESTS PASSED!" -ForegroundColor Green
} else {
    Write-Host "`nSome tests failed. Review errors above." -ForegroundColor Yellow
}

Write-Host ""


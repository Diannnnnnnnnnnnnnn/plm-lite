# ===================================================
# Elasticsearch Integration Test Script
# Part, Change, and Task Services
# ===================================================

Write-Host "`n🧪 ELASTICSEARCH INTEGRATION TEST - PART, CHANGE, TASK SERVICES" -ForegroundColor Cyan
Write-Host "=" * 70 -ForegroundColor Cyan

$testsPassed = 0
$testsFailed = 0
$testsTotal = 0

# ===================================================
# Helper Functions
# ===================================================

function Test-Result {
    param (
        [string]$TestName,
        [bool]$Passed,
        [string]$Message = ""
    )
    
    $script:testsTotal++
    
    if ($Passed) {
        Write-Host "✅ $TestName" -ForegroundColor Green
        if ($Message) {
            Write-Host "   $Message" -ForegroundColor Gray
        }
        $script:testsPassed++
    } else {
        Write-Host "❌ $TestName" -ForegroundColor Red
        if ($Message) {
            Write-Host "   $Message" -ForegroundColor Yellow
        }
        $script:testsFailed++
    }
}

function Invoke-SafeRestMethod {
    param (
        [string]$Uri,
        [string]$Method = "Get",
        [object]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Body ($Body | ConvertTo-Json -Depth 10) -ContentType $ContentType -ErrorAction Stop
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -ErrorAction Stop
        }
    } catch {
        Write-Host "   Error: $_" -ForegroundColor Red
        return $null
    }
}

# ===================================================
# 1. INFRASTRUCTURE CHECKS
# ===================================================

Write-Host "`n📋 PHASE 1: Infrastructure Checks" -ForegroundColor Yellow
Write-Host "-" * 70

# Check Elasticsearch
try {
    $esHealth = Invoke-RestMethod "http://localhost:9200/_cluster/health" -ErrorAction Stop
    Test-Result "Elasticsearch is running" $true "Status: $($esHealth.status)"
} catch {
    Test-Result "Elasticsearch is running" $false "Cannot connect to Elasticsearch"
    Write-Host "`n⚠️  Please start Elasticsearch: docker-compose -f docker-compose-elasticsearch.yml up -d" -ForegroundColor Yellow
    exit 1
}

# Check BOM Service
try {
    $bomHealth = Invoke-RestMethod "http://localhost:8089/actuator/health" -ErrorAction Stop
    Test-Result "BOM Service is running" $true "Port 8089"
} catch {
    Test-Result "BOM Service is running" $false "Cannot connect to BOM Service"
}

# Check Change Service  
try {
    $changeHealth = Invoke-RestMethod "http://localhost:8084/actuator/health" -ErrorAction Stop
    Test-Result "Change Service is running" $true "Port 8084"
} catch {
    Test-Result "Change Service is running" $false "Cannot connect to Change Service"
}

# Check Task Service
try {
    $taskHealth = Invoke-RestMethod "http://localhost:8082/api/tasks" -ErrorAction Stop
    Test-Result "Task Service is running" $true "Port 8082"
} catch {
    Test-Result "Task Service is running" $false "Cannot connect to Task Service"
}

# ===================================================
# 2. PART SERVICE TESTS
# ===================================================

Write-Host "`n📋 PHASE 2: Part Service Elasticsearch Integration" -ForegroundColor Yellow
Write-Host "-" * 70

# Check if parts index exists
try {
    $partsIndex = Invoke-RestMethod "http://localhost:9200/parts" -ErrorAction Stop
    Test-Result "Parts index exists" $true
} catch {
    Test-Result "Parts index exists" $false "Index may be created on first part creation"
}

# Create a test part
$testPartId = $null
try {
    $createPartRequest = @{
        title = "Test Motor Assembly - ES Integration"
        description = "Motor assembly for Elasticsearch integration testing"
        stage = "DEVELOPMENT"
        status = "IN_WORK"
        level = "L1"
        creator = "test-user"
    }
    
    $createdPart = Invoke-SafeRestMethod -Uri "http://localhost:8089/api/v1/parts" -Method Post -Body $createPartRequest
    
    if ($createdPart -and $createdPart.id) {
        $testPartId = $createdPart.id
        Test-Result "Part created successfully" $true "Part ID: $testPartId"
        
        # Wait for indexing
        Start-Sleep -Seconds 2
        
        # Check if part was indexed
        try {
            $esSearch = Invoke-RestMethod "http://localhost:9200/parts/_search?q=id:$testPartId" -ErrorAction Stop
            $found = $esSearch.hits.total.value -gt 0
            Test-Result "Part auto-indexed to Elasticsearch" $found "Found $($esSearch.hits.total.value) document(s)"
        } catch {
            Test-Result "Part auto-indexed to Elasticsearch" $false "Failed to query ES"
        }
        
    } else {
        Test-Result "Part created successfully" $false "No part returned"
    }
} catch {
    Test-Result "Part created successfully" $false "Request failed"
}

# Update the part
if ($testPartId) {
    try {
        $updateResult = Invoke-SafeRestMethod -Uri "http://localhost:8089/api/v1/parts/$testPartId/stage?stage=PRODUCTION" -Method Put
        Test-Result "Part updated successfully" ($updateResult -ne $null) "Stage changed to PRODUCTION"
        
        # Wait for re-indexing
        Start-Sleep -Seconds 2
        
        # Verify updated in ES
        try {
            $esDoc = Invoke-RestMethod "http://localhost:9200/parts/_doc/$testPartId" -ErrorAction Stop
            $correctStage = $esDoc._source.stage -eq "PRODUCTION"
            Test-Result "Part re-indexed after update" $correctStage "Stage in ES: $($esDoc._source.stage)"
        } catch {
            Test-Result "Part re-indexed after update" $false "Failed to retrieve from ES"
        }
    } catch {
        Test-Result "Part updated successfully" $false "Update request failed"
    }
}

# Search for the part
if ($testPartId) {
    try {
        $searchResults = Invoke-SafeRestMethod -Uri "http://localhost:9200/parts/_search?q=Motor" -Method Get
        $found = $searchResults.hits.total.value -gt 0
        Test-Result "Part searchable via Elasticsearch" $found "Found $($searchResults.hits.total.value) result(s)"
    } catch {
        Test-Result "Part searchable via Elasticsearch" $false
    }
}

# ===================================================
# 3. CHANGE SERVICE TESTS
# ===================================================

Write-Host "`n📋 PHASE 3: Change Service Elasticsearch Integration" -ForegroundColor Yellow
Write-Host "-" * 70

# Check if changes index exists
try {
    $changesIndex = Invoke-RestMethod "http://localhost:9200/changes" -ErrorAction Stop
    Test-Result "Changes index exists" $true
} catch {
    Test-Result "Changes index exists" $false "Index may be created on first change"
}

# Note: Creating a change requires a released document, which is complex to set up
# So we'll just verify the ES endpoint exists
try {
    $changeSearch = Invoke-RestMethod "http://localhost:9200/changes/_search?size=10" -ErrorAction Stop
    $changeCount = $changeSearch.hits.total.value
    Test-Result "Changes index is queryable" $true "Found $changeCount change(s) indexed"
    
    if ($changeCount -gt 0) {
        Write-Host "   Sample change: $($changeSearch.hits.hits[0]._source.title)" -ForegroundColor Gray
    }
} catch {
    Test-Result "Changes index is queryable" $false
}

# Check Change Service search endpoint
try {
    $changeServiceSearch = Invoke-RestMethod "http://localhost:8084/api/changes/search/elastic?q=test" -ErrorAction Stop
    Test-Result "Change Service ES search endpoint works" $true "Endpoint is accessible"
} catch {
    Test-Result "Change Service ES search endpoint works" $false "Endpoint may not exist or service is down"
}

# ===================================================
# 4. TASK SERVICE TESTS
# ===================================================

Write-Host "`n📋 PHASE 4: Task Service Elasticsearch Integration" -ForegroundColor Yellow
Write-Host "-" * 70

# Check if tasks index exists
try {
    $tasksIndex = Invoke-RestMethod "http://localhost:9200/tasks" -ErrorAction Stop
    Test-Result "Tasks index exists" $true
} catch {
    Test-Result "Tasks index exists" $false "Index may be created on first task"
}

# Create a test task
$testTaskId = $null
try {
    $createTaskRequest = @{
        name = "Test Task - ES Integration"
        description = "Task for testing Elasticsearch integration"
        userId = 1
        assignedTo = "test-user"
        taskStatus = "PENDING"
    }
    
    $createdTask = Invoke-SafeRestMethod -Uri "http://localhost:8082/api/tasks" -Method Post -Body $createTaskRequest
    
    if ($createdTask -and $createdTask.id) {
        $testTaskId = $createdTask.id
        Test-Result "Task created successfully" $true "Task ID: $testTaskId"
        
        # Wait for indexing
        Start-Sleep -Seconds 2
        
        # Check if task was indexed
        try {
            $esSearch = Invoke-RestMethod "http://localhost:9200/tasks/_search?q=id:$testTaskId" -ErrorAction Stop
            $found = $esSearch.hits.total.value -gt 0
            Test-Result "Task auto-indexed to Elasticsearch" $found "Found $($esSearch.hits.total.value) document(s)"
        } catch {
            Test-Result "Task auto-indexed to Elasticsearch" $false "Failed to query ES"
        }
        
    } else {
        Test-Result "Task created successfully" $false "No task returned"
    }
} catch {
    Test-Result "Task created successfully" $false "Request failed"
}

# Update the task
if ($testTaskId) {
    try {
        $updateTaskRequest = @{
            name = "Updated Task - ES Integration Test"
            description = "Updated description for ES testing"
            taskStatus = "IN_PROGRESS"
        }
        
        $updateResult = Invoke-SafeRestMethod -Uri "http://localhost:8082/api/tasks/$testTaskId" -Method Put -Body $updateTaskRequest
        Test-Result "Task updated successfully" ($updateResult -ne $null) "Status changed to IN_PROGRESS"
        
        # Wait for re-indexing
        Start-Sleep -Seconds 2
        
        # Verify updated in ES
        try {
            $esDoc = Invoke-RestMethod "http://localhost:9200/tasks/_doc/$testTaskId" -ErrorAction Stop
            $correctName = $esDoc._source.name -like "*Updated*"
            Test-Result "Task re-indexed after update" $correctName "Name updated in ES"
        } catch {
            Test-Result "Task re-indexed after update" $false "Failed to retrieve from ES"
        }
    } catch {
        Test-Result "Task updated successfully" $false "Update request failed"
    }
}

# Search for the task
if ($testTaskId) {
    try {
        $searchResults = Invoke-SafeRestMethod -Uri "http://localhost:9200/tasks/_search?q=Integration" -Method Get
        $found = $searchResults.hits.total.value -gt 0
        Test-Result "Task searchable via Elasticsearch" $found "Found $($searchResults.hits.total.value) result(s)"
    } catch {
        Test-Result "Task searchable via Elasticsearch" $false
    }
}

# ===================================================
# 5. UNIFIED SEARCH TEST
# ===================================================

Write-Host "`n📋 PHASE 5: Unified Search Service" -ForegroundColor Yellow
Write-Host "-" * 70

# Check Search Service
try {
    $searchHealth = Invoke-RestMethod "http://localhost:8091/api/v1/search/health" -ErrorAction Stop
    Test-Result "Search Service is running" $true "Port 8091"
    
    # Test unified search
    try {
        $unifiedSearch = Invoke-RestMethod "http://localhost:8091/api/v1/search?q=test" -ErrorAction Stop
        $totalHits = $unifiedSearch.totalHits
        Test-Result "Unified search endpoint works" $true "Total hits: $totalHits"
        
        if ($unifiedSearch.documents) {
            Write-Host "   Documents found: $($unifiedSearch.documents.Count)" -ForegroundColor Gray
        }
        if ($unifiedSearch.boms) {
            Write-Host "   BOMs found: $($unifiedSearch.boms.Count)" -ForegroundColor Gray
        }
        if ($unifiedSearch.parts) {
            Write-Host "   Parts found: $($unifiedSearch.parts.Count)" -ForegroundColor Gray
        }
        
    } catch {
        Test-Result "Unified search endpoint works" $false "Search request failed"
    }
    
} catch {
    Test-Result "Search Service is running" $false "Service not accessible"
}

# ===================================================
# 6. CLEANUP (Optional)
# ===================================================

Write-Host "`n📋 PHASE 6: Cleanup" -ForegroundColor Yellow
Write-Host "-" * 70

# Ask if user wants to clean up test data
Write-Host "Do you want to delete the test data created during this test? (Y/N): " -NoNewline -ForegroundColor Cyan
$cleanup = Read-Host

if ($cleanup -eq "Y" -or $cleanup -eq "y") {
    # Delete test part
    if ($testPartId) {
        try {
            Invoke-RestMethod "http://localhost:8089/api/v1/parts/$testPartId" -Method Delete -ErrorAction Stop
            Write-Host "✅ Test part deleted" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Could not delete test part" -ForegroundColor Yellow
        }
    }
    
    # Delete test task
    if ($testTaskId) {
        try {
            Invoke-RestMethod "http://localhost:8082/api/tasks/$testTaskId" -Method Delete -ErrorAction Stop
            Write-Host "✅ Test task deleted" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Could not delete test task" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "Test data preserved for manual inspection" -ForegroundColor Gray
}

# ===================================================
# SUMMARY
# ===================================================

Write-Host "`n" + ("=" * 70) -ForegroundColor Cyan
Write-Host "📊 TEST SUMMARY" -ForegroundColor Cyan
Write-Host ("=" * 70) -ForegroundColor Cyan

Write-Host "`nTotal Tests: $testsTotal" -ForegroundColor White
Write-Host "Passed: $testsPassed" -ForegroundColor Green
Write-Host "Failed: $testsFailed" -ForegroundColor Red

$successRate = [math]::Round(($testsPassed / $testsTotal) * 100, 1)
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })

if ($testsFailed -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! Elasticsearch integration is working perfectly!" -ForegroundColor Green
} elseif ($successRate -ge 70) {
    Write-Host "`n⚠️  MOST TESTS PASSED - Some features may need attention" -ForegroundColor Yellow
} else {
    Write-Host "`n❌ MULTIPLE TESTS FAILED - Please review the errors above" -ForegroundColor Red
}

Write-Host "`n" + ("=" * 70) -ForegroundColor Cyan

# Export results to JSON
$results = @{
    timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    totalTests = $testsTotal
    passed = $testsPassed
    failed = $testsFailed
    successRate = $successRate
}

$results | ConvertTo-Json | Out-File "elasticsearch-integration-test-results.json"
Write-Host "Results saved to: elasticsearch-integration-test-results.json" -ForegroundColor Gray

Write-Host ""


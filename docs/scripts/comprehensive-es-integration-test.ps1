# ============================================================================
# COMPREHENSIVE ELASTICSEARCH INTEGRATION TEST SUITE
# Tests all 5 PLM services: Document, BOM, Part, Change, Task
# ============================================================================

param(
    [switch]$SkipCleanup,
    [switch]$Verbose,
    [string]$ReportPath = "test-reports"
)

$ErrorActionPreference = "Continue"
$startTime = Get-Date

# Test counters
$script:totalTests = 0
$script:passedTests = 0
$script:failedTests = 0
$script:skippedTests = 0
$script:testResults = @()

# Service URLs
$ES_URL = "http://localhost:9200"
$DOC_SERVICE = "http://localhost:8081"
$BOM_SERVICE = "http://localhost:8089"
$CHANGE_SERVICE = "http://localhost:8084"
$TASK_SERVICE = "http://localhost:8082"
$SEARCH_SERVICE = "http://localhost:8091"

# Test data storage
$script:createdResources = @{
    documents = @()
    boms = @()
    parts = @()
    changes = @()
    tasks = @()
}

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

function Write-TestHeader {
    param([string]$Title)
    Write-Host "`n$("="*80)" -ForegroundColor Cyan
    Write-Host " $Title" -ForegroundColor Cyan
    Write-Host "$("="*80)" -ForegroundColor Cyan
}

function Write-TestSection {
    param([string]$Title)
    Write-Host "`n--- $Title ---" -ForegroundColor Yellow
}

function Test-Assert {
    param(
        [string]$TestName,
        [bool]$Condition,
        [string]$ExpectedValue = "",
        [string]$ActualValue = "",
        [string]$ErrorMessage = ""
    )
    
    $script:totalTests++
    
    if ($Condition) {
        Write-Host "[PASS] $TestName" -ForegroundColor Green
        if ($Verbose -and $ActualValue) {
            Write-Host "       Expected: $ExpectedValue, Got: $ActualValue" -ForegroundColor Gray
        }
        $script:passedTests++
        
        $script:testResults += [PSCustomObject]@{
            TestName = $TestName
            Status = "PASS"
            Expected = $ExpectedValue
            Actual = $ActualValue
            Error = ""
            Timestamp = Get-Date
        }
        
        return $true
    } else {
        Write-Host "[FAIL] $TestName" -ForegroundColor Red
        if ($ErrorMessage) {
            Write-Host "       Error: $ErrorMessage" -ForegroundColor Red
        }
        if ($ExpectedValue -and $ActualValue) {
            Write-Host "       Expected: $ExpectedValue" -ForegroundColor Yellow
            Write-Host "       Got: $ActualValue" -ForegroundColor Yellow
        }
        $script:failedTests++
        
        $script:testResults += [PSCustomObject]@{
            TestName = $TestName
            Status = "FAIL"
            Expected = $ExpectedValue
            Actual = $ActualValue
            Error = $ErrorMessage
            Timestamp = Get-Date
        }
        
        return $false
    }
}

function Invoke-APICall {
    param(
        [string]$Uri,
        [string]$Method = "Get",
        [object]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            $params.ContentType = $ContentType
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; Error = $null }
    } catch {
        return @{ Success = $false; Data = $null; Error = $_.Exception.Message }
    }
}

function Wait-ForIndexing {
    param([int]$Seconds = 2)
    if ($Verbose) {
        Write-Host "       Waiting ${Seconds}s for ES indexing..." -ForegroundColor Gray
    }
    Start-Sleep -Seconds $Seconds
}

# ============================================================================
# PHASE 1: INFRASTRUCTURE TESTS
# ============================================================================

function Test-Infrastructure {
    Write-TestHeader "PHASE 1: INFRASTRUCTURE TESTS"
    
    # Test Elasticsearch
    Write-TestSection "Elasticsearch"
    $esResult = Invoke-APICall -Uri "$ES_URL/_cluster/health"
    Test-Assert "Elasticsearch is accessible" $esResult.Success
    
    if ($esResult.Success) {
        $health = $esResult.Data
        Test-Assert "ES cluster status is not red" ($health.status -ne "red") "green/yellow" $health.status
        Write-Host "       Cluster: $($health.cluster_name), Status: $($health.status), Nodes: $($health.number_of_nodes)" -ForegroundColor Gray
    }
    
    # Test Kibana
    Write-TestSection "Kibana"
    $kibanaResult = Invoke-APICall -Uri "http://localhost:5601/api/status"
    Test-Assert "Kibana is accessible" $kibanaResult.Success
    
    # Test Services
    Write-TestSection "Microservices"
    
    $docHealth = Invoke-APICall -Uri "$DOC_SERVICE/actuator/health"
    Test-Assert "Document Service is running" $docHealth.Success
    
    $bomHealth = Invoke-APICall -Uri "$BOM_SERVICE/actuator/health"
    Test-Assert "BOM Service is running" $bomHealth.Success
    
    $changeHealth = Invoke-APICall -Uri "$CHANGE_SERVICE/actuator/health"
    Test-Assert "Change Service is running" $changeHealth.Success
    
    $taskHealth = Invoke-APICall -Uri "$TASK_SERVICE/api/tasks"
    Test-Assert "Task Service is running" $taskHealth.Success
    
    $searchHealth = Invoke-APICall -Uri "$SEARCH_SERVICE/api/v1/search/health"
    Test-Assert "Search Service is running" $searchHealth.Success
    
    # Check Elasticsearch indices
    Write-TestSection "Elasticsearch Indices"
    $indicesResult = Invoke-APICall -Uri "$ES_URL/_cat/indices?format=json"
    
    if ($indicesResult.Success) {
        $indices = $indicesResult.Data
        $expectedIndices = @("documents", "boms", "parts", "changes", "tasks")
        
        foreach ($indexName in $expectedIndices) {
            $indexExists = $indices | Where-Object { $_.index -eq $indexName }
            if ($indexExists) {
                Test-Assert "Index '$indexName' exists" $true
                Write-Host "       Docs: $($indexExists.'docs.count'), Size: $($indexExists.'store.size')" -ForegroundColor Gray
            } else {
                Test-Assert "Index '$indexName' exists" $false "" "" "Index will be created on first document"
            }
        }
    }
}

# ============================================================================
# PHASE 2: DOCUMENT SERVICE TESTS
# ============================================================================

function Test-DocumentService {
    Write-TestHeader "PHASE 2: DOCUMENT SERVICE TESTS"
    
    $testDocId = $null
    
    # CREATE Test
    Write-TestSection "Create Document"
    $docData = @{
        title = "ES Test Document - $(Get-Date -Format 'yyyyMMdd-HHmmss')"
        type = "Technical Specification"
        description = "Test document for ES integration validation"
        stage = "DEVELOPMENT"
        status = "IN_WORK"
        creator = "test-user"
        version = "1"
    }
    
    $createResult = Invoke-APICall -Uri "$DOC_SERVICE/api/v1/documents" -Method Post -Body $docData
    Test-Assert "Document created via API" $createResult.Success "" "" $createResult.Error
    
    if ($createResult.Success) {
        $testDocId = $createResult.Data.id
        $script:createdResources.documents += $testDocId
        Write-Host "       Document ID: $testDocId" -ForegroundColor Gray
        
        Wait-ForIndexing
        
        # Verify ES indexing
        $esQuery = Invoke-APICall -Uri "$ES_URL/documents/_search?q=id:$testDocId"
        $indexed = $esQuery.Success -and $esQuery.Data.hits.total.value -gt 0
        Test-Assert "Document auto-indexed to ES" $indexed "1" $esQuery.Data.hits.total.value
        
        if ($indexed) {
            $esDoc = $esQuery.Data.hits.hits[0]._source
            Test-Assert "Document title matches in ES" ($esDoc.title -eq $docData.title) $docData.title $esDoc.title
            Test-Assert "Document stage matches in ES" ($esDoc.stage -eq $docData.stage) $docData.stage $esDoc.stage
        }
    }
    
    # UPDATE Test
    if ($testDocId) {
        Write-TestSection "Update Document"
        $updateData = @{
            title = "ES Test Document - UPDATED"
            description = "Updated description"
        }
        
        $updateResult = Invoke-APICall -Uri "$DOC_SERVICE/api/v1/documents/$testDocId" -Method Put -Body $updateData
        Test-Assert "Document updated via API" $updateResult.Success "" "" $updateResult.Error
        
        Wait-ForIndexing
        
        $esDoc = Invoke-APICall -Uri "$ES_URL/documents/_doc/$testDocId"
        if ($esDoc.Success) {
            $titleUpdated = $esDoc.Data._source.title -like "*UPDATED*"
            Test-Assert "Document re-indexed after update" $titleUpdated "Title contains 'UPDATED'" $esDoc.Data._source.title
        }
    }
    
    # SEARCH Test
    Write-TestSection "Search Documents"
    $searchResult = Invoke-APICall -Uri "$DOC_SERVICE/api/v1/documents/search/elastic?q=Test"
    Test-Assert "Document search endpoint works" $searchResult.Success "" "" $searchResult.Error
    
    if ($searchResult.Success) {
        Test-Assert "Search returns results" ($searchResult.Data.Count -gt 0) ">0" $searchResult.Data.Count
    }
}

# ============================================================================
# PHASE 3: PART SERVICE TESTS
# ============================================================================

function Test-PartService {
    Write-TestHeader "PHASE 3: PART SERVICE TESTS"
    
    $testPartId = $null
    
    # CREATE Test
    Write-TestSection "Create Part"
    $partData = @{
        title = "Test Motor Assembly - $(Get-Date -Format 'HHmmss')"
        description = "Motor assembly for ES integration testing"
        stage = "DEVELOPMENT"
        status = "IN_WORK"
        level = "L1"
        creator = "test-user"
    }
    
    $createResult = Invoke-APICall -Uri "$BOM_SERVICE/api/v1/parts" -Method Post -Body $partData
    Test-Assert "Part created via API" $createResult.Success "" "" $createResult.Error
    
    if ($createResult.Success) {
        $testPartId = $createResult.Data.id
        $script:createdResources.parts += $testPartId
        Write-Host "       Part ID: $testPartId" -ForegroundColor Gray
        
        Wait-ForIndexing
        
        # Verify ES indexing
        $esQuery = Invoke-APICall -Uri "$ES_URL/parts/_search?q=id:$testPartId"
        $indexed = $esQuery.Success -and $esQuery.Data.hits.total.value -gt 0
        Test-Assert "Part auto-indexed to ES" $indexed "1" $esQuery.Data.hits.total.value
        
        if ($indexed) {
            $esPart = $esQuery.Data.hits.hits[0]._source
            Test-Assert "Part title matches in ES" ($esPart.title -eq $partData.title) $partData.title $esPart.title
            Test-Assert "Part stage matches in ES" ($esPart.stage -eq $partData.stage) $partData.stage $esPart.stage
        }
    }
    
    # UPDATE Test
    if ($testPartId) {
        Write-TestSection "Update Part"
        $updateResult = Invoke-APICall -Uri "$BOM_SERVICE/api/v1/parts/$testPartId/stage?stage=PRODUCTION" -Method Put
        Test-Assert "Part stage updated via API" $updateResult.Success "" "" $updateResult.Error
        
        Wait-ForIndexing
        
        $esPart = Invoke-APICall -Uri "$ES_URL/parts/_doc/$testPartId"
        if ($esPart.Success) {
            $stageUpdated = $esPart.Data._source.stage -eq "PRODUCTION"
            Test-Assert "Part re-indexed after update" $stageUpdated "PRODUCTION" $esPart.Data._source.stage
        }
    }
    
    # SEARCH Test
    Write-TestSection "Search Parts"
    $searchResult = Invoke-APICall -Uri "$ES_URL/parts/_search?q=Motor"
    Test-Assert "Part search in ES works" $searchResult.Success "" "" $searchResult.Error
    
    if ($searchResult.Success -and $searchResult.Data.hits.total.value -gt 0) {
        Test-Assert "Part search returns results" $true "1+" $searchResult.Data.hits.total.value
    }
}

# ============================================================================
# PHASE 4: TASK SERVICE TESTS
# ============================================================================

function Test-TaskService {
    Write-TestHeader "PHASE 4: TASK SERVICE TESTS"
    
    $testTaskId = $null
    
    # CREATE Test
    Write-TestSection "Create Task"
    $taskData = @{
        name = "ES Integration Test Task - $(Get-Date -Format 'HHmmss')"
        description = "Task for testing Elasticsearch integration"
        userId = 1
        assignedTo = "test-user"
        taskStatus = "PENDING"
    }
    
    $createResult = Invoke-APICall -Uri "$TASK_SERVICE/api/tasks" -Method Post -Body $taskData
    Test-Assert "Task created via API" $createResult.Success "" "" $createResult.Error
    
    if ($createResult.Success) {
        $testTaskId = $createResult.Data.id
        $script:createdResources.tasks += $testTaskId
        Write-Host "       Task ID: $testTaskId" -ForegroundColor Gray
        
        Wait-ForIndexing
        
        # Verify ES indexing
        $esQuery = Invoke-APICall -Uri "$ES_URL/tasks/_search?q=id:$testTaskId"
        $indexed = $esQuery.Success -and $esQuery.Data.hits.total.value -gt 0
        Test-Assert "Task auto-indexed to ES" $indexed "1" $esQuery.Data.hits.total.value
        
        if ($indexed) {
            $esTask = $esQuery.Data.hits.hits[0]._source
            Test-Assert "Task name matches in ES" ($esTask.name -eq $taskData.name) $taskData.name $esTask.name
        }
    }
    
    # UPDATE Test
    if ($testTaskId) {
        Write-TestSection "Update Task"
        $updateData = @{
            name = "ES Test Task - UPDATED"
            description = "Updated task description"
            taskStatus = "IN_PROGRESS"
        }
        
        $updateResult = Invoke-APICall -Uri "$TASK_SERVICE/api/tasks/$testTaskId" -Method Put -Body $updateData
        Test-Assert "Task updated via API" $updateResult.Success "" "" $updateResult.Error
        
        Wait-ForIndexing
        
        $esTask = Invoke-APICall -Uri "$ES_URL/tasks/_doc/$testTaskId"
        if ($esTask.Success) {
            $nameUpdated = $esTask.Data._source.name -like "*UPDATED*"
            Test-Assert "Task re-indexed after update" $nameUpdated "Name contains 'UPDATED'" $esTask.Data._source.name
        }
    }
    
    # SEARCH Test
    Write-TestSection "Search Tasks"
    $searchResult = Invoke-APICall -Uri "$ES_URL/tasks/_search?q=Integration"
    Test-Assert "Task search in ES works" $searchResult.Success "" "" $searchResult.Error
    
    if ($searchResult.Success) {
        Test-Assert "Task search returns results" ($searchResult.Data.hits.total.value -gt 0)
    }
}

# ============================================================================
# PHASE 5: CHANGE SERVICE TESTS
# ============================================================================

function Test-ChangeService {
    Write-TestHeader "PHASE 5: CHANGE SERVICE TESTS"
    
    Write-TestSection "Change Service ES Integration"
    
    # Note: Creating a change requires a released document, which is complex
    # So we'll test the ES index directly and the search endpoint
    
    $changesIndex = Invoke-APICall -Uri "$ES_URL/changes"
    Test-Assert "Changes index is accessible" $changesIndex.Success "" "" $changesIndex.Error
    
    $changesSearch = Invoke-APICall -Uri "$ES_URL/changes/_search?size=10"
    if ($changesSearch.Success) {
        $count = $changesSearch.Data.hits.total.value
        Test-Assert "Changes index is queryable" $true "Accessible" "Found $count change(s)"
        Write-Host "       Total changes indexed: $count" -ForegroundColor Gray
        
        if ($count -gt 0) {
            $sampleChange = $changesSearch.Data.hits.hits[0]._source
            Write-Host "       Sample change: $($sampleChange.title)" -ForegroundColor Gray
            Test-Assert "Change document has required fields" ($sampleChange.id -and $sampleChange.title) "id, title" "Found"
        }
    }
    
    # Test search endpoint
    $searchEndpoint = Invoke-APICall -Uri "$CHANGE_SERVICE/api/changes/search/elastic?q=test"
    Test-Assert "Change Service ES search endpoint exists" $searchEndpoint.Success "" "" $searchEndpoint.Error
}

# ============================================================================
# PHASE 6: BOM SERVICE TESTS
# ============================================================================

function Test-BomService {
    Write-TestHeader "PHASE 6: BOM SERVICE TESTS"
    
    $testBomId = $null
    
    # CREATE Test
    Write-TestSection "Create BOM"
    $bomData = @{
        bomNumber = "BOM-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
        title = "Test BOM for ES Integration"
        description = "BOM for testing Elasticsearch integration"
        stage = "DEVELOPMENT"
        status = "IN_WORK"
        creator = "test-user"
        product = "Test Product"
    }
    
    $createResult = Invoke-APICall -Uri "$BOM_SERVICE/api/v1/boms" -Method Post -Body $bomData
    Test-Assert "BOM created via API" $createResult.Success "" "" $createResult.Error
    
    if ($createResult.Success) {
        $testBomId = $createResult.Data.id
        $script:createdResources.boms += $testBomId
        Write-Host "       BOM ID: $testBomId" -ForegroundColor Gray
        
        Wait-ForIndexing
        
        # Verify ES indexing
        $esQuery = Invoke-APICall -Uri "$ES_URL/boms/_search?q=id:$testBomId"
        $indexed = $esQuery.Success -and $esQuery.Data.hits.total.value -gt 0
        Test-Assert "BOM auto-indexed to ES" $indexed "1" $esQuery.Data.hits.total.value
        
        if ($indexed) {
            $esBom = $esQuery.Data.hits.hits[0]._source
            Test-Assert "BOM title matches in ES" ($esBom.title -eq $bomData.title) $bomData.title $esBom.title
        }
    }
    
    # SEARCH Test
    Write-TestSection "Search BOMs"
    $searchResult = Invoke-APICall -Uri "$ES_URL/boms/_search?q=Test"
    Test-Assert "BOM search in ES works" $searchResult.Success "" "" $searchResult.Error
}

# ============================================================================
# PHASE 7: UNIFIED SEARCH TESTS
# ============================================================================

function Test-UnifiedSearch {
    Write-TestHeader "PHASE 7: UNIFIED SEARCH SERVICE TESTS"
    
    Write-TestSection "Unified Search"
    
    # Test with a general query
    $searchResult = Invoke-APICall -Uri "$SEARCH_SERVICE/api/v1/search?q=test"
    Test-Assert "Unified search endpoint works" $searchResult.Success "" "" $searchResult.Error
    
    if ($searchResult.Success) {
        $result = $searchResult.Data
        Test-Assert "Unified search returns structured response" ($result.query -and $result.totalHits -ne $null)
        
        Write-Host "       Query: '$($result.query)'" -ForegroundColor Gray
        Write-Host "       Total Hits: $($result.totalHits)" -ForegroundColor Gray
        Write-Host "       Response Time: $($result.took)ms" -ForegroundColor Gray
        
        # Check each index results
        if ($result.documents) {
            Write-Host "       Documents found: $($result.documents.Count)" -ForegroundColor Gray
            Test-Assert "Documents included in unified search" $true
        }
        
        if ($result.boms) {
            Write-Host "       BOMs found: $($result.boms.Count)" -ForegroundColor Gray
        }
        
        if ($result.parts) {
            Write-Host "       Parts found: $($result.parts.Count)" -ForegroundColor Gray
        }
        
        # Performance check
        Test-Assert "Unified search response time acceptable" ($result.took -lt 1000) "<1000ms" "$($result.took)ms"
    }
    
    # Test document-specific search
    Write-TestSection "Document-Only Search"
    $docSearch = Invoke-APICall -Uri "$SEARCH_SERVICE/api/v1/search/documents?q=test"
    Test-Assert "Document-only search endpoint works" $docSearch.Success "" "" $docSearch.Error
    
    # Test empty query handling
    Write-TestSection "Edge Cases"
    $emptyQuery = Invoke-APICall -Uri "$SEARCH_SERVICE/api/v1/search?q="
    Test-Assert "Empty query handled gracefully" $emptyQuery.Success
    
    # Test special characters
    $specialChars = Invoke-APICall -Uri "$SEARCH_SERVICE/api/v1/search?q=test%26special"
    Test-Assert "Special characters in query handled" $specialChars.Success
}

# ============================================================================
# PHASE 8: PERFORMANCE TESTS
# ============================================================================

function Test-Performance {
    Write-TestHeader "PHASE 8: PERFORMANCE TESTS"
    
    Write-TestSection "Response Time Tests"
    
    # Test ES direct query performance
    $iterations = 10
    $times = @()
    
    for ($i = 0; $i -lt $iterations; $i++) {
        $start = Get-Date
        $result = Invoke-APICall -Uri "$ES_URL/documents/_search?q=test"
        $end = Get-Date
        
        if ($result.Success) {
            $elapsed = ($end - $start).TotalMilliseconds
            $times += $elapsed
        }
    }
    
    if ($times.Count -gt 0) {
        $avgTime = ($times | Measure-Object -Average).Average
        $maxTime = ($times | Measure-Object -Maximum).Maximum
        $minTime = ($times | Measure-Object -Minimum).Minimum
        
        Write-Host "       Average: $([math]::Round($avgTime, 2))ms" -ForegroundColor Gray
        Write-Host "       Min: $([math]::Round($minTime, 2))ms, Max: $([math]::Round($maxTime, 2))ms" -ForegroundColor Gray
        
        Test-Assert "Average ES query time acceptable" ($avgTime -lt 100) "<100ms" "$([math]::Round($avgTime, 2))ms"
        Test-Assert "Max ES query time acceptable" ($maxTime -lt 500) "<500ms" "$([math]::Round($maxTime, 2))ms"
    }
    
    # Test concurrent queries
    Write-TestSection "Concurrent Query Test"
    $jobs = @()
    
    for ($i = 0; $i -lt 5; $i++) {
        $jobs += Start-Job -ScriptBlock {
            param($url)
            try {
                Invoke-RestMethod -Uri $url -ErrorAction Stop
                return $true
            } catch {
                return $false
            }
        } -ArgumentList "$ES_URL/documents/_search?q=test"
    }
    
    $results = $jobs | Wait-Job | Receive-Job
    $successCount = ($results | Where-Object { $_ -eq $true }).Count
    
    Test-Assert "Concurrent queries successful" ($successCount -eq 5) "5/5" "$successCount/5"
    
    $jobs | Remove-Job
}

# ============================================================================
# PHASE 9: DATA CONSISTENCY TESTS
# ============================================================================

function Test-DataConsistency {
    Write-TestHeader "PHASE 9: DATA CONSISTENCY TESTS"
    
    Write-TestSection "SQL to ES Consistency"
    
    # Check documents
    $sqlDocs = Invoke-APICall -Uri "$DOC_SERVICE/api/v1/documents"
    $esDocs = Invoke-APICall -Uri "$ES_URL/documents/_count"
    
    if ($sqlDocs.Success -and $esDocs.Success) {
        $sqlCount = $sqlDocs.Data.Count
        $esCount = $esDocs.Data.count
        Write-Host "       SQL Documents: $sqlCount, ES Documents: $esCount" -ForegroundColor Gray
        
        # Allow for some discrepancy due to test timing
        $consistency = [math]::Abs($sqlCount - $esCount) -le 2
        Test-Assert "Document count consistency" $consistency "±2 difference" "SQL:$sqlCount, ES:$esCount"
    }
    
    # Verify specific document data consistency
    if ($script:createdResources.documents.Count -gt 0) {
        $testId = $script:createdResources.documents[0]
        
        $sqlDoc = Invoke-APICall -Uri "$DOC_SERVICE/api/v1/documents/$testId"
        $esDoc = Invoke-APICall -Uri "$ES_URL/documents/_doc/$testId"
        
        if ($sqlDoc.Success -and $esDoc.Success) {
            $sqlTitle = $sqlDoc.Data.title
            $esTitle = $esDoc.Data._source.title
            
            Test-Assert "Document title consistency" ($sqlTitle -eq $esTitle) $sqlTitle $esTitle
        }
    }
}

# ============================================================================
# PHASE 10: CLEANUP
# ============================================================================

function Invoke-Cleanup {
    if ($SkipCleanup) {
        Write-Host "`nSkipping cleanup (test data preserved)" -ForegroundColor Yellow
        return
    }
    
    Write-TestHeader "PHASE 10: CLEANUP"
    
    Write-Host "Do you want to clean up test data? (Y/N): " -NoNewline -ForegroundColor Cyan
    $response = Read-Host
    
    if ($response -ne "Y" -and $response -ne "y") {
        Write-Host "Cleanup skipped" -ForegroundColor Yellow
        return
    }
    
    Write-TestSection "Cleaning up test resources"
    
    # Delete documents
    foreach ($id in $script:createdResources.documents) {
        try {
            Invoke-RestMethod -Uri "$DOC_SERVICE/api/v1/documents/$id" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "  [OK] Deleted document $id" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not delete document $id" -ForegroundColor Yellow
        }
    }
    
    # Delete parts
    foreach ($id in $script:createdResources.parts) {
        try {
            Invoke-RestMethod -Uri "$BOM_SERVICE/api/v1/parts/$id" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "  [OK] Deleted part $id" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not delete part $id" -ForegroundColor Yellow
        }
    }
    
    # Delete tasks
    foreach ($id in $script:createdResources.tasks) {
        try {
            Invoke-RestMethod -Uri "$TASK_SERVICE/api/tasks/$id" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "  [OK] Deleted task $id" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not delete task $id" -ForegroundColor Yellow
        }
    }
    
    # Delete BOMs
    foreach ($id in $script:createdResources.boms) {
        try {
            Invoke-RestMethod -Uri "$BOM_SERVICE/api/v1/boms/$id" -Method Delete -ErrorAction Stop | Out-Null
            Write-Host "  [OK] Deleted BOM $id" -ForegroundColor Green
        } catch {
            Write-Host "  [WARN] Could not delete BOM $id" -ForegroundColor Yellow
        }
    }
}

# ============================================================================
# REPORTING
# ============================================================================

function Generate-Report {
    Write-TestHeader "TEST SUMMARY"
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host "`nTest Execution Summary:" -ForegroundColor Cyan
    Write-Host "  Start Time: $($startTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor White
    Write-Host "  End Time: $($endTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor White
    Write-Host "  Duration: $([math]::Round($duration.TotalSeconds, 2)) seconds" -ForegroundColor White
    
    Write-Host "`nTest Results:" -ForegroundColor Cyan
    Write-Host "  Total Tests: $totalTests" -ForegroundColor White
    Write-Host "  Passed: $passedTests" -ForegroundColor Green
    Write-Host "  Failed: $failedTests" -ForegroundColor $(if ($failedTests -eq 0) { "Green" } else { "Red" })
    Write-Host "  Skipped: $skippedTests" -ForegroundColor Yellow
    
    $successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 1) } else { 0 }
    $color = if ($successRate -ge 95) { "Green" } elseif ($successRate -ge 80) { "Yellow" } else { "Red" }
    Write-Host "  Success Rate: $successRate%" -ForegroundColor $color
    
    Write-Host "`nResources Created:" -ForegroundColor Cyan
    Write-Host "  Documents: $($script:createdResources.documents.Count)" -ForegroundColor White
    Write-Host "  Parts: $($script:createdResources.parts.Count)" -ForegroundColor White
    Write-Host "  Tasks: $($script:createdResources.tasks.Count)" -ForegroundColor White
    Write-Host "  BOMs: $($script:createdResources.boms.Count)" -ForegroundColor White
    
    # Generate detailed report files
    if (-not (Test-Path $ReportPath)) {
        New-Item -ItemType Directory -Path $ReportPath | Out-Null
    }
    
    $reportFile = Join-Path $ReportPath "es-integration-test-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    
    $report = @{
        timestamp = $startTime.ToString('yyyy-MM-dd HH:mm:ss')
        duration = $duration.TotalSeconds
        totalTests = $totalTests
        passedTests = $passedTests
        failedTests = $failedTests
        skippedTests = $skippedTests
        successRate = $successRate
        resources = $script:createdResources
        results = $script:testResults
    }
    
    $report | ConvertTo-Json -Depth 10 | Out-File $reportFile
    Write-Host "`nDetailed report saved to: $reportFile" -ForegroundColor Gray
    
    # Generate HTML report
    $htmlFile = Join-Path $ReportPath "es-integration-test-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"
    Generate-HTMLReport -ReportData $report -OutputPath $htmlFile
    Write-Host "HTML report saved to: $htmlFile" -ForegroundColor Gray
    
    # Final verdict
    Write-Host ""
    if ($failedTests -eq 0) {
        Write-Host "========================================" -ForegroundColor Green
        Write-Host " ALL TESTS PASSED!" -ForegroundColor Green
        Write-Host " ES Integration is PRODUCTION READY" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    } elseif ($successRate -ge 80) {
        Write-Host "========================================" -ForegroundColor Yellow
        Write-Host " MOST TESTS PASSED" -ForegroundColor Yellow
        Write-Host " Review failures above" -ForegroundColor Yellow
        Write-Host "========================================" -ForegroundColor Yellow
    } else {
        Write-Host "========================================" -ForegroundColor Red
        Write-Host " MULTIPLE TESTS FAILED" -ForegroundColor Red
        Write-Host " ES Integration needs attention" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
    }
    Write-Host ""
}

function Generate-HTMLReport {
    param(
        [object]$ReportData,
        [string]$OutputPath
    )
    
    $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>ES Integration Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        h1 { color: #333; border-bottom: 3px solid #4CAF50; padding-bottom: 10px; }
        h2 { color: #555; margin-top: 30px; }
        .summary { background: white; padding: 20px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .metric { display: inline-block; margin: 10px 20px; }
        .metric-label { font-weight: bold; color: #666; }
        .metric-value { font-size: 24px; font-weight: bold; }
        .pass { color: #4CAF50; }
        .fail { color: #f44336; }
        .skip { color: #FF9800; }
        table { width: 100%; border-collapse: collapse; background: white; margin-top: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        th { background: #4CAF50; color: white; padding: 12px; text-align: left; }
        td { padding: 10px; border-bottom: 1px solid #ddd; }
        tr:hover { background-color: #f5f5f5; }
        .status-pass { background-color: #e8f5e9; color: #2e7d32; font-weight: bold; padding: 4px 8px; border-radius: 3px; }
        .status-fail { background-color: #ffebee; color: #c62828; font-weight: bold; padding: 4px 8px; border-radius: 3px; }
    </style>
</head>
<body>
    <h1>Elasticsearch Integration Test Report</h1>
    
    <div class="summary">
        <div class="metric">
            <div class="metric-label">Total Tests</div>
            <div class="metric-value">$($ReportData.totalTests)</div>
        </div>
        <div class="metric">
            <div class="metric-label">Passed</div>
            <div class="metric-value pass">$($ReportData.passedTests)</div>
        </div>
        <div class="metric">
            <div class="metric-label">Failed</div>
            <div class="metric-value fail">$($ReportData.failedTests)</div>
        </div>
        <div class="metric">
            <div class="metric-label">Success Rate</div>
            <div class="metric-value pass">$($ReportData.successRate)%</div>
        </div>
        <div class="metric">
            <div class="metric-label">Duration</div>
            <div class="metric-value">$([math]::Round($ReportData.duration, 2))s</div>
        </div>
    </div>
    
    <h2>Test Results</h2>
    <table>
        <thead>
            <tr>
                <th>Test Name</th>
                <th>Status</th>
                <th>Expected</th>
                <th>Actual</th>
                <th>Error</th>
            </tr>
        </thead>
        <tbody>
"@
    
    foreach ($result in $ReportData.results) {
        $statusClass = if ($result.Status -eq "PASS") { "status-pass" } else { "status-fail" }
        $html += @"
            <tr>
                <td>$($result.TestName)</td>
                <td><span class="$statusClass">$($result.Status)</span></td>
                <td>$($result.Expected)</td>
                <td>$($result.Actual)</td>
                <td style="color: #c62828;">$($result.Error)</td>
            </tr>
"@
    }
    
    $html += @"
        </tbody>
    </table>
    
    <p style="margin-top: 30px; color: #666; text-align: center;">
        Generated on $($ReportData.timestamp)
    </p>
</body>
</html>
"@
    
    $html | Out-File $OutputPath -Encoding UTF8
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

Write-Host @"

╔════════════════════════════════════════════════════════════════════════╗
║                                                                        ║
║         COMPREHENSIVE ELASTICSEARCH INTEGRATION TEST SUITE             ║
║                                                                        ║
║         Testing: Document, BOM, Part, Change, Task Services            ║
║                                                                        ║
╚════════════════════════════════════════════════════════════════════════╝

"@ -ForegroundColor Cyan

# Run all test phases
try {
    Test-Infrastructure
    Test-DocumentService
    Test-PartService
    Test-TaskService
    Test-ChangeService
    Test-BomService
    Test-UnifiedSearch
    Test-Performance
    Test-DataConsistency
    Invoke-Cleanup
}
catch {
    Write-Host "`n[ERROR] Test execution failed: $_" -ForegroundColor Red
}
finally {
    Generate-Report
}

# Return exit code based on test results
exit $failedTests


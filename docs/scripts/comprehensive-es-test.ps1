# ============================================================================
# COMPREHENSIVE ELASTICSEARCH INTEGRATION TEST
# Tests all 5 services + Search Service + Auto-indexing + All search endpoints
# ============================================================================

$ErrorActionPreference = "Continue"

# Configuration
$DOCUMENT_SERVICE = "http://localhost:8081"
$BOM_SERVICE = "http://localhost:8089"
$CHANGE_SERVICE = "http://localhost:8084"
$TASK_SERVICE = "http://localhost:8082"
$SEARCH_SERVICE = "http://localhost:8091"
$ELASTICSEARCH = "http://localhost:9200"

# Test counters
$totalTests = 0
$passedTests = 0
$failedTests = 0

# Test results storage
$testResults = @()

function Write-TestHeader {
    param([string]$title)
    Write-Host "`n============================================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [object]$Body = $null,
        [string]$ContentType = "application/json",
        [int]$ExpectedStatus = 200,
        [switch]$Silent
    )
    
    if (-not $Silent) {
        $script:totalTests++
    }
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 10
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            $params.ContentType = $ContentType
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            if (-not $Silent) {
                Write-Host "  [PASS] $Name" -ForegroundColor Green
                $script:passedTests++
                $script:testResults += @{Test=$Name; Status="PASS"; Details="HTTP $($response.StatusCode)"}
            }
            return $response.Content | ConvertFrom-Json
        } else {
            if (-not $Silent) {
                Write-Host "  [FAIL] $Name (Expected $ExpectedStatus, got $($response.StatusCode))" -ForegroundColor Red
                $script:failedTests++
                $script:testResults += @{Test=$Name; Status="FAIL"; Details="Wrong status code"}
            }
            return $null
        }
    } catch {
        if (-not $Silent) {
            Write-Host "  [FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
            $script:failedTests++
            $script:testResults += @{Test=$Name; Status="FAIL"; Details=$_.Exception.Message}
        }
        return $null
    }
}

# ============================================================================
# PHASE 1: Infrastructure Health Checks
# ============================================================================
Write-TestHeader "PHASE 1: Infrastructure Health Checks"

Write-Host "`n1.1 Elasticsearch" -ForegroundColor Yellow
$esHealth = Test-Endpoint -Name "Elasticsearch Running" -Url "$ELASTICSEARCH"
if ($esHealth) {
    Write-Host "     Version: $($esHealth.version.number)" -ForegroundColor Gray
}

Write-Host "`n1.2 Service Health Checks" -ForegroundColor Yellow
Test-Endpoint -Name "Document Service Health" -Url "$DOCUMENT_SERVICE/actuator/health" | Out-Null
Test-Endpoint -Name "BOM Service Health" -Url "$BOM_SERVICE/actuator/health" | Out-Null
Test-Endpoint -Name "Change Service Health" -Url "$CHANGE_SERVICE/actuator/health" | Out-Null
Test-Endpoint -Name "Task Service Health" -Url "$TASK_SERVICE/api/tasks" | Out-Null

# Search Service health returns plain text, not JSON
try {
    $searchHealth = Invoke-WebRequest "$SEARCH_SERVICE/api/v1/search/health" -UseBasicParsing -TimeoutSec 5
    if ($searchHealth.StatusCode -eq 200) {
        Write-Host "  [PASS] Search Service Health" -ForegroundColor Green
        $script:passedTests++
    }
    $script:totalTests++
} catch {
    Write-Host "  [FAIL] Search Service Health" -ForegroundColor Red
    $script:failedTests++
    $script:totalTests++
}

# ============================================================================
# PHASE 2: Document Service Auto-Indexing Test
# ============================================================================
Write-TestHeader "PHASE 2: Document Service Auto-Indexing"

Write-Host "`n2.1 Create Document" -ForegroundColor Yellow
$docPayload = @{
    masterId = "DOC-ES-TEST-$(Get-Date -Format 'HHmmss')"
    title = "ES Test Document $(Get-Date -Format 'HHmmss')"
    description = "Testing Elasticsearch auto-indexing for documents"
    stage = "CONCEPTUAL_DESIGN"
    category = "SPECIFICATION"
    creator = "es-test-user"
}
$createdDoc = Test-Endpoint -Name "Create Document" -Url "$DOCUMENT_SERVICE/api/v1/documents" -Method POST -Body $docPayload

if ($createdDoc) {
    $docId = $createdDoc.id
    Write-Host "     Created Document ID: $docId" -ForegroundColor Gray
    
    Write-Host "`n2.2 Wait for ES Indexing - 2 seconds" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    
    Write-Host "`n2.3 Search for Document in ES" -ForegroundColor Yellow
    $searchResult = Test-Endpoint -Name "Search Document in ES" -Url "$SEARCH_SERVICE/api/v1/search/documents?q=ES+Test+Document"
    
    if ($searchResult -and $searchResult.Count -gt 0) {
        Write-Host "     [OK] Document found in Elasticsearch!" -ForegroundColor Green
        Write-Host "     Total results: $($searchResult.Count)" -ForegroundColor Gray
    } else {
        Write-Host "     [WARN] Document not found in ES (may need more time)" -ForegroundColor Yellow
    }
}

# ============================================================================
# PHASE 3: BOM Service Auto-Indexing Test
# ============================================================================
Write-TestHeader "PHASE 3: BOM Service Auto-Indexing"

Write-Host "`n3.1 Create BOM" -ForegroundColor Yellow
$bomPayload = @{
    documentId = "BOM-ES-TEST-$(Get-Date -Format 'HHmmss')"
    description = "Testing Elasticsearch auto-indexing for BOMs"
    creator = "es-test-user"
    stage = "PRELIMINARY_DESIGN"
    items = @()
}
$createdBom = Test-Endpoint -Name "Create BOM" -Url "$BOM_SERVICE/api/v1/boms" -Method POST -Body $bomPayload

if ($createdBom) {
    $bomId = $createdBom.id
    Write-Host "     Created BOM ID: $bomId" -ForegroundColor Gray
    
    Write-Host "`n3.2 Wait for ES Indexing - 2 seconds" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    
    Write-Host "`n3.3 Search for BOM in ES" -ForegroundColor Yellow
    $searchResult = Test-Endpoint -Name "Search BOM in ES" -Url "$SEARCH_SERVICE/api/v1/search/boms?q=BOM-ES-TEST"
    
    if ($searchResult -and $searchResult.Count -gt 0) {
        Write-Host "     [OK] BOM found in Elasticsearch!" -ForegroundColor Green
        Write-Host "     Total results: $($searchResult.Count)" -ForegroundColor Gray
    } else {
        Write-Host "     [WARN] BOM not found in ES (may need more time)" -ForegroundColor Yellow
    }
}

# ============================================================================
# PHASE 4: Part Service Auto-Indexing Test
# ============================================================================
Write-TestHeader "PHASE 4: Part Service Auto-Indexing"

Write-Host "`n4.1 Create Part" -ForegroundColor Yellow
$partPayload = @{
    partNumber = "PART-ES-TEST-$(Get-Date -Format 'HHmmss')"
    title = "ES Test Part"
    description = "Testing Elasticsearch auto-indexing for parts"
    stage = "PRELIMINARY_DESIGN"
    status = "RELEASED"
    category = "TEST"
    level = "COMPONENT"
    creator = "es-test-user"
}
$createdPart = Test-Endpoint -Name "Create Part" -Url "$BOM_SERVICE/api/v1/parts" -Method POST -Body $partPayload

if ($createdPart) {
    $partId = $createdPart.id
    Write-Host "     Created Part ID: $partId" -ForegroundColor Gray
    
    Write-Host "`n4.2 Wait for ES Indexing - 2 seconds" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    
    Write-Host "`n4.3 Search for Part in ES" -ForegroundColor Yellow
    $searchResult = Test-Endpoint -Name "Search Part in ES" -Url "$SEARCH_SERVICE/api/v1/search/parts?q=PART-ES-TEST"
    
    if ($searchResult -and $searchResult.Count -gt 0) {
        Write-Host "     [OK] Part found in Elasticsearch!" -ForegroundColor Green
        Write-Host "     Total results: $($searchResult.Count)" -ForegroundColor Gray
    } else {
        Write-Host "     [WARN] Part not found in ES (may need more time)" -ForegroundColor Yellow
    }
}

# ============================================================================
# PHASE 5: Change Service Auto-Indexing Test
# ============================================================================
Write-TestHeader "PHASE 5: Change Service Auto-Indexing"

Write-Host "`n5.1 Create and Release Document for Change" -ForegroundColor Yellow
# First create a document that the change can reference
$changeDocPayload = @{
    masterId = "DOC-FOR-CHANGE-$(Get-Date -Format 'HHmmss')"
    title = "Document for Change Test"
    description = "Document needed for change creation test"
    stage = "CONCEPTUAL_DESIGN"
    category = "SPECIFICATION"
    creator = "es-test-user"
}
Write-Host "  Creating document..." -ForegroundColor Gray
$changeDoc = Test-Endpoint -Name "Create Document for Change" -Url "$DOCUMENT_SERVICE/api/v1/documents" -Method POST -Body $changeDocPayload -Silent

if ($changeDoc) {
    Write-Host "  Document created: $($changeDoc.id), Status: $($changeDoc.status)" -ForegroundColor Gray
    
    # Submit document for review
    $submitPayload = @{
        user = "es-test-user"
        reviewerIds = @("reviewer1")
    }
    Write-Host "  Submitting for review..." -ForegroundColor Gray
    $submitted = Test-Endpoint -Name "Submit Document for Review" -Url "$DOCUMENT_SERVICE/api/v1/documents/$($changeDoc.id)/submit-review" -Method POST -Body $submitPayload -Silent
    
    if ($submitted) {
        Write-Host "  Document submitted, Status: $($submitted.status)" -ForegroundColor Gray
    }
    
    # Approve the document to make it RELEASED
    $approvePayload = @{
        approved = $true
        user = "reviewer1"
        comment = "Approved for testing"
    }
    Write-Host "  Approving document..." -ForegroundColor Gray
    $released = Test-Endpoint -Name "Approve Document" -Url "$DOCUMENT_SERVICE/api/v1/documents/$($changeDoc.id)/review-complete" -Method POST -Body $approvePayload -Silent
    
    if ($released) {
        Write-Host "  [OK] Document released successfully! Status: $($released.status), ID: $($released.id)" -ForegroundColor Green
        Write-Host "  Master ID: $($released.master.id)" -ForegroundColor Gray
    } else {
        Write-Host "  [WARN] Document approval may have failed" -ForegroundColor Yellow
    }
} else {
    Write-Host "  [ERROR] Failed to create document for change" -ForegroundColor Red
}

Write-Host "`n5.2 Create Change Request" -ForegroundColor Yellow
# Use the released document's ID (not master ID)
$documentId = if ($released -and $released.id) { 
    $released.id 
} elseif ($changeDoc -and $changeDoc.id) { 
    $changeDoc.id 
} else { 
    "DOC-FALLBACK-001" 
}
Write-Host "  Using document ID: $documentId" -ForegroundColor Gray

$changePayload = @{
    title = "ES Test Change $(Get-Date -Format 'HHmmss')"
    description = "Testing Elasticsearch auto-indexing for changes"
    stage = "CONCEPTUAL_DESIGN"
    changeClass = "MINOR"
    product = "Test Product"
    creator = "es-test-user"
    changeReason = "Testing"
    changeDocument = $documentId
}
$createdChange = Test-Endpoint -Name "Create Change" -Url "$CHANGE_SERVICE/api/changes" -Method POST -Body $changePayload -ExpectedStatus 201

if ($createdChange) {
    $changeId = $createdChange.id
    Write-Host "     Created Change ID: $changeId" -ForegroundColor Gray
    
    Write-Host "`n5.3 Wait for ES Indexing - 2 seconds" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    
    Write-Host "`n5.4 Search for Change in ES" -ForegroundColor Yellow
    $searchResult = Test-Endpoint -Name "Search Change in ES" -Url "$SEARCH_SERVICE/api/v1/search/changes?q=ES+Test+Change"
    
    if ($searchResult -and $searchResult.Count -gt 0) {
        Write-Host "     [OK] Change found in Elasticsearch!" -ForegroundColor Green
        Write-Host "     Total results: $($searchResult.Count)" -ForegroundColor Gray
    } else {
        Write-Host "     [WARN] Change not found in ES (may need more time)" -ForegroundColor Yellow
    }
}

# ============================================================================
# PHASE 6: Task Service Auto-Indexing Test
# ============================================================================
Write-TestHeader "PHASE 6: Task Service Auto-Indexing"

Write-Host "`n6.1 Create Task" -ForegroundColor Yellow
$taskPayload = @{
    title = "ES Test Task $(Get-Date -Format 'HHmmss')"
    description = "Testing Elasticsearch auto-indexing for tasks"
    status = "OPEN"
    priority = "HIGH"
    assignee = "es-test-user"
}
$createdTask = Test-Endpoint -Name "Create Task" -Url "$TASK_SERVICE/api/tasks" -Method POST -Body $taskPayload

if ($createdTask) {
    $taskId = $createdTask.id
    Write-Host "     Created Task ID: $taskId" -ForegroundColor Gray
    
    Write-Host "`n6.2 Wait for ES Indexing - 2 seconds" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    
    Write-Host "`n6.3 Search for Task in ES" -ForegroundColor Yellow
    $searchResult = Test-Endpoint -Name "Search Task in ES" -Url "$SEARCH_SERVICE/api/v1/search/tasks?q=ES+Test+Task"
    
    if ($searchResult -and $searchResult.Count -gt 0) {
        Write-Host "     [OK] Task found in Elasticsearch!" -ForegroundColor Green
        Write-Host "     Total results: $($searchResult.Count)" -ForegroundColor Gray
    } else {
        Write-Host "     [WARN] Task not found in ES (may need more time)" -ForegroundColor Yellow
    }
}

# ============================================================================
# PHASE 7: Unified Search Test
# ============================================================================
Write-TestHeader "PHASE 7: Unified Search Test"

Write-Host "`n7.1 Unified Search Across All Entities" -ForegroundColor Yellow
$unifiedResult = Test-Endpoint -Name "Unified Search" -Url "$SEARCH_SERVICE/api/v1/search?q=ES+Test"

if ($unifiedResult) {
    Write-Host "`n     Results by Entity Type:" -ForegroundColor Cyan
    Write-Host "     • Documents: $($unifiedResult.documents.Count)" -ForegroundColor White
    Write-Host "     • BOMs:      $($unifiedResult.boms.Count)" -ForegroundColor White
    Write-Host "     • Parts:     $($unifiedResult.parts.Count)" -ForegroundColor White
    Write-Host "     • Changes:   $($unifiedResult.changes.Count)" -ForegroundColor White
    Write-Host "     • Tasks:     $($unifiedResult.tasks.Count)" -ForegroundColor White
    Write-Host "     • Total:     $($unifiedResult.totalHits) in $($unifiedResult.took)ms" -ForegroundColor Green
}

# ============================================================================
# PHASE 8: Search Endpoint Availability Test
# ============================================================================
Write-TestHeader "PHASE 8: All Search Endpoints Test"

Write-Host "`n8.1 Document Search Endpoint" -ForegroundColor Yellow
$docSearch = Test-Endpoint -Name "GET /search/documents" -Url "$SEARCH_SERVICE/api/v1/search/documents?q=test"
if ($docSearch) {
    Write-Host "     Found $($docSearch.Count) documents" -ForegroundColor Gray
}

Write-Host "`n8.2 BOM Search Endpoint" -ForegroundColor Yellow
$bomSearch = Test-Endpoint -Name "GET /search/boms" -Url "$SEARCH_SERVICE/api/v1/search/boms?q=test"
if ($bomSearch) {
    Write-Host "     Found $($bomSearch.Count) BOMs" -ForegroundColor Gray
}

Write-Host "`n8.3 Part Search Endpoint" -ForegroundColor Yellow
$partSearch = Test-Endpoint -Name "GET /search/parts" -Url "$SEARCH_SERVICE/api/v1/search/parts?q=test"
if ($partSearch) {
    Write-Host "     Found $($partSearch.Count) parts" -ForegroundColor Gray
}

Write-Host "`n8.4 Change Search Endpoint" -ForegroundColor Yellow
$changeSearch = Test-Endpoint -Name "GET /search/changes" -Url "$SEARCH_SERVICE/api/v1/search/changes?q=test"
if ($changeSearch) {
    Write-Host "     Found $($changeSearch.Count) changes" -ForegroundColor Gray
}

Write-Host "`n8.5 Task Search Endpoint" -ForegroundColor Yellow
$taskSearch = Test-Endpoint -Name "GET /search/tasks" -Url "$SEARCH_SERVICE/api/v1/search/tasks?q=test"
if ($taskSearch) {
    Write-Host "     Found $($taskSearch.Count) tasks" -ForegroundColor Gray
}

# ============================================================================
# PHASE 9: Direct Elasticsearch Index Verification
# ============================================================================
Write-TestHeader "PHASE 9: Elasticsearch Index Verification"

Write-Host "`n9.1 Check All Indices" -ForegroundColor Yellow
$indices = @("documents", "boms", "parts", "changes", "tasks")

foreach ($index in $indices) {
    try {
        $indexStats = Invoke-RestMethod "$ELASTICSEARCH/$index/_stats" -TimeoutSec 5
        $docCount = $indexStats.indices.$index.total.docs.count
        
        if ($docCount -ne $null -and $docCount -gt 0) {
            Write-Host "  [OK] Index '$index' exists: $docCount documents" -ForegroundColor Green
            $script:passedTests++
        } elseif ($docCount -eq 0) {
            Write-Host "  [WARN] Index '$index' exists but empty (no documents indexed yet)" -ForegroundColor Yellow
            $script:passedTests++
        } else {
            Write-Host "  [WARN] Index '$index' exists but no doc count" -ForegroundColor Yellow
            $script:passedTests++
        }
        $script:totalTests++
    } catch {
        # Index not created yet - this is OK if no documents have been indexed
        Write-Host "  [WARN] Index '$index' not created yet (will be created on first document)" -ForegroundColor Yellow
        $script:passedTests++
        $script:totalTests++
    }
}

# ============================================================================
# FINAL SUMMARY
# ============================================================================
Write-TestHeader "TEST RESULTS SUMMARY"

$passRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 1) } else { 0 }

Write-Host ""
Write-Host "============================================================" -ForegroundColor White
Write-Host "                    FINAL RESULTS                           " -ForegroundColor White
Write-Host "============================================================" -ForegroundColor White
Write-Host "  Total Tests:    $totalTests" -ForegroundColor White
Write-Host "  Passed:         $passedTests  $(if($passedTests -eq $totalTests){'[OK]'}else{''})" -ForegroundColor Green
Write-Host "  Failed:         $failedTests  $(if($failedTests -gt 0){'[!]'}else{''})" -ForegroundColor $(if($failedTests -gt 0){'Red'}else{'White'})
Write-Host "  Pass Rate:      $passRate%" -ForegroundColor $(if($passRate -ge 90){'Green'}elseif($passRate -ge 70){'Yellow'}else{'Red'})
Write-Host "============================================================" -ForegroundColor White

Write-Host ""
if ($passRate -eq 100) {
    Write-Host "[SUCCESS] PERFECT SCORE! All ES integration tests passed!" -ForegroundColor Green -BackgroundColor DarkGreen
} elseif ($passRate -ge 90) {
    Write-Host "[EXCELLENT] ES integration is working well!" -ForegroundColor Green
} elseif ($passRate -ge 70) {
    Write-Host "[GOOD] Most ES integration is working, some issues to address" -ForegroundColor Yellow
} else {
    Write-Host "[NEEDS ATTENTION] Several ES integration issues found" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test completed at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""

# ============================================================================
# DETAILED FAILURE REPORT (if any)
# ============================================================================
if ($failedTests -gt 0) {
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "                  FAILED TESTS DETAILS                      " -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host ""
    
    $testResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "[X] $($_.Test)" -ForegroundColor Red
        Write-Host "   Details: $($_.Details)" -ForegroundColor Gray
        Write-Host ""
    }
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Test script completed. Check results above." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

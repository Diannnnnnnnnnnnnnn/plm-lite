# Reindex All Documents from MySQL to Elasticsearch
# This script queries all documents from Document Service and indexes them to Elasticsearch

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Elasticsearch Document Reindexing Tool" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Configuration
$DOCUMENT_SERVICE_URL = "http://localhost:8081/api/v1/documents"
$ELASTICSEARCH_URL = "http://localhost:9200"
$INDEX_NAME = "documents"

# Step 1: Check Elasticsearch connection
Write-Host "[Step 1] Checking Elasticsearch connection..." -ForegroundColor Yellow
try {
    $esHealth = Invoke-RestMethod -Uri "$ELASTICSEARCH_URL/_cluster/health"
    Write-Host "✅ Elasticsearch Status: $($esHealth.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Cannot connect to Elasticsearch at $ELASTICSEARCH_URL" -ForegroundColor Red
    Write-Host "   Make sure Elasticsearch is running: docker ps" -ForegroundColor Yellow
    exit 1
}

# Step 2: Get all documents from MySQL via Document Service
Write-Host "`n[Step 2] Fetching documents from Document Service..." -ForegroundColor Yellow
try {
    $documents = Invoke-RestMethod -Uri $DOCUMENT_SERVICE_URL
    Write-Host "✅ Found $($documents.Count) documents in database" -ForegroundColor Green
} catch {
    Write-Host "❌ Cannot connect to Document Service at $DOCUMENT_SERVICE_URL" -ForegroundColor Red
    Write-Host "   Make sure Document Service is running on port 8081" -ForegroundColor Yellow
    exit 1
}

if ($documents.Count -eq 0) {
    Write-Host "`n⚠️  No documents to index." -ForegroundColor Yellow
    exit 0
}

# Step 3: Index each document to Elasticsearch
Write-Host "`n[Step 3] Indexing documents to Elasticsearch..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

foreach ($doc in $documents) {
    # Build ES document structure matching DocumentSearchDocument.java
    $esDoc = @{
        id = $doc.id
        title = $doc.title
        description = $doc.description
        documentNumber = $doc.masterId
        masterId = $doc.masterId
        status = $doc.status
        stage = $doc.stage
        category = $doc.category
        contentType = $doc.contentType
        creator = $doc.creator
        fileSize = $doc.fileSize
        version = $doc.version
        createTime = $doc.createTime
        updateTime = if ($doc.fileUploadedAt) { $doc.fileUploadedAt } else { $doc.createTime }
        isActive = $doc.active
    } | ConvertTo-Json

    try {
        # Index document to Elasticsearch
        $result = Invoke-RestMethod -Uri "$ELASTICSEARCH_URL/$INDEX_NAME/_doc/$($doc.id)" `
            -Method Put `
            -Body $esDoc `
            -ContentType "application/json"
        
        Write-Host "  ✅ Indexed: $($doc.title)" -ForegroundColor Green
        $successCount++
    } catch {
        Write-Host "  ❌ Failed: $($doc.title) - $_" -ForegroundColor Red
        $failCount++
    }
}

# Step 4: Refresh the index
Write-Host "`n[Step 4] Refreshing Elasticsearch index..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$ELASTICSEARCH_URL/$INDEX_NAME/_refresh" -Method Post | Out-Null
    Write-Host "✅ Index refreshed" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Index refresh failed, but documents may still be indexed" -ForegroundColor Yellow
}

# Step 5: Verify count
Write-Host "`n[Step 5] Verifying indexed documents..." -ForegroundColor Yellow
try {
    $esResult = Invoke-RestMethod -Uri "$ELASTICSEARCH_URL/$INDEX_NAME/_search?size=0"
    $esCount = $esResult.hits.total.value
    Write-Host "✅ Documents in Elasticsearch: $esCount" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Could not verify document count" -ForegroundColor Yellow
    $esCount = "?"
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Reindexing Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total documents:       $($documents.Count)" -ForegroundColor White
Write-Host "Successfully indexed:  $successCount" -ForegroundColor Green
Write-Host "Failed:                $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "White" })
Write-Host "ES document count:     $esCount" -ForegroundColor White

if ($esCount -eq $documents.Count) {
    Write-Host "`n🎉 SUCCESS! All documents indexed!" -ForegroundColor Green
} elseif ($successCount -gt 0) {
    Write-Host "`n⚠️  PARTIAL SUCCESS: Some documents indexed" -ForegroundColor Yellow
} else {
    Write-Host "`n❌ FAILED: No documents were indexed" -ForegroundColor Red
}

Write-Host "`n========================================`n" -ForegroundColor Cyan




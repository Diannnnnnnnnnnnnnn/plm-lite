# =====================================================
# Clean MinIO Data - Remove All Objects
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cleaning MinIO Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if MinIO is running
$minioRunning = docker ps --filter "name=plm-minio" --format "{{.Names}}" | Select-String "plm-minio"

if (-not $minioRunning) {
    Write-Host "MinIO container 'plm-minio' is not running." -ForegroundColor Red
    Write-Host "Please start infrastructure services first:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f infra/docker-compose-infrastructure.yaml up -d" -ForegroundColor Yellow
    exit 1
}

Write-Host "Removing all objects from MinIO bucket 'plm-documents'..." -ForegroundColor Yellow
Write-Host ""

# Get the network name
$network = docker network ls --filter "name=plm" --format "{{.Name}}" | Select-Object -First 1

if (-not $network) {
    $network = "plm-lite_plm-network"
}

# Remove all objects from the bucket using MinIO client
docker run --rm --network $network `
    -e MC_HOST_plmminio=http://minio:password@plm-minio:9000 `
    minio/mc `
    rm --recursive --force plmminio/plm-documents/

Write-Host ""
Write-Host "All MinIO objects removed successfully!" -ForegroundColor Green
Write-Host "Bucket 'plm-documents' is now empty." -ForegroundColor Green
Write-Host ""



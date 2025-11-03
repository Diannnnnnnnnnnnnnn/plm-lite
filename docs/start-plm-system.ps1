# =====================================================
# PLM Lite - Intelligent Startup Script (PowerShell)
# Checks current state and starts what's needed
# =====================================================

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PLM Lite - System Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = Get-Location

# ==================== Check Docker ====================
Write-Host "[1/4] Checking Docker Status..." -ForegroundColor Yellow

try {
    docker ps | Out-Null
    Write-Host "  Docker is running [OK]" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: Docker is not running!" -ForegroundColor Red
    Write-Host "  Please start Docker Desktop and try again." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# ==================== Start Infrastructure ====================
Write-Host "[2/4] Starting Infrastructure Services..." -ForegroundColor Yellow
Write-Host ""

# Check if containers are already running
$needStart = $false
$requiredContainers = @("plm-elasticsearch", "zeebe", "redis", "minio", "mysql-plm", "neo4j-plm")

foreach ($container in $requiredContainers) {
    $running = docker ps --format "{{.Names}}" | Select-String -Pattern $container -Quiet
    if (-not $running) {
        $needStart = $true
        break
    }
}

if ($needStart) {
    Write-Host "  Starting Docker containers..." -ForegroundColor Cyan
    Write-Host "  This may take a few minutes on first run..." -ForegroundColor Gray
    Write-Host ""
    
    docker-compose -f docker-compose-master.yml up -d
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "  ERROR: Failed to start Docker containers!" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
    
    Write-Host ""
    Write-Host "  Waiting for services to be healthy..." -ForegroundColor Cyan
    Start-Sleep -Seconds 60
} else {
    Write-Host "  All containers already running [OK]" -ForegroundColor Green
}

Write-Host ""
Write-Host "  Checking service health..." -ForegroundColor Cyan

# Function to test HTTP endpoint
function Test-HttpEndpoint {
    param([string]$Url)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        return $true
    } catch {
        return $false
    }
}

# Function to test TCP port
function Test-Port {
    param([string]$HostName, [int]$Port)
    $connection = Test-NetConnection -ComputerName $HostName -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
    return $connection
}

# Check services
$services = @(
    @{Name="Elasticsearch"; Test={Test-HttpEndpoint "http://localhost:9200"}},
    @{Name="Zeebe"; Test={Test-HttpEndpoint "http://localhost:9600/ready"}},
    @{Name="Redis"; Test={Test-Port "localhost" 6379}},
    @{Name="MySQL"; Test={Test-Port "localhost" 3306}},
    @{Name="Neo4j"; Test={Test-Port "localhost" 7687}},
    @{Name="MinIO"; Test={Test-HttpEndpoint "http://localhost:9000/minio/health/live"}}
)

foreach ($service in $services) {
    $healthy = & $service.Test
    if ($healthy) {
        Write-Host ("  - {0,-15}: HEALTHY [OK]" -f $service.Name) -ForegroundColor Green
    } else {
        Write-Host ("  - {0,-15}: STARTING..." -f $service.Name) -ForegroundColor Yellow
    }
}

Write-Host ""

# ==================== Start Backend Services ====================
Write-Host "[3/4] Starting Backend Services..." -ForegroundColor Yellow
Write-Host ""

# Define services with their ports and directories
$backendServices = @(
    @{Name="Graph Service"; Port=8090; Dir="infra\graph-service"; Order=1},
    @{Name="Workflow Orchestrator"; Port=8086; Dir="workflow-orchestrator"; Order=2},
    @{Name="User Service"; Port=8083; Dir="user-service"; Order=3},
    @{Name="Task Service"; Port=8082; Dir="task-service"; Order=4},
    @{Name="Document Service"; Port=8081; Dir="document-service"; Order=5},
    @{Name="BOM Service"; Port=8089; Dir="bom-service"; Order=6},
    @{Name="Change Service"; Port=8084; Dir="change-service"; Order=7},
    @{Name="Search Service"; Port=8091; Dir="infra\search-service"; Order=8}
)

# Check if any service needs to be started
$needStartServices = $false
foreach ($service in $backendServices) {
    if (-not (Test-Port "localhost" $service.Port)) {
        $needStartServices = $true
        break
    }
}

if ($needStartServices) {
    Write-Host "  Starting services in order..." -ForegroundColor Cyan
    Write-Host ""
    
    foreach ($service in ($backendServices | Sort-Object Order)) {
        if (-not (Test-Port "localhost" $service.Port)) {
            Write-Host ("  [{0}/8] Starting {1} ({2})..." -f $service.Order, $service.Name, $service.Port) -ForegroundColor Cyan
            
            $windowTitle = "PLM - {0} ({1})" -f $service.Name, $service.Port
            $serviceDir = Join-Path $ProjectRoot $service.Dir
            
            Start-Process cmd -ArgumentList "/k", "cd /d `"$serviceDir`" && mvn spring-boot:run" -WindowStyle Normal
            
            # Wait time varies by service
            $waitTime = if ($service.Order -le 2) { 45 } else { 45 }
            Start-Sleep -Seconds $waitTime
        } else {
            Write-Host ("  [{0}/8] {1} already running [OK]" -f $service.Order, $service.Name) -ForegroundColor Green
        }
    }
} else {
    Write-Host "  All backend services already running [OK]" -ForegroundColor Green
}

Write-Host ""

# ==================== Start Frontend ====================
Write-Host "[4/4] Starting Frontend..." -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Port "localhost" 3000)) {
    Write-Host "  Starting React frontend..." -ForegroundColor Cyan
    $frontendDir = Join-Path $ProjectRoot "frontend"
    Start-Process cmd -ArgumentList "/k", "cd /d `"$frontendDir`" && npm start" -WindowStyle Normal
    Start-Sleep -Seconds 10
} else {
    Write-Host "  Frontend already running [OK]" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  PLM System Started Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Infrastructure Services:" -ForegroundColor Cyan
Write-Host "  Elasticsearch:        http://localhost:9200"
Write-Host "  Kibana:               http://localhost:5601"
Write-Host "  Zeebe:                http://localhost:8088"
Write-Host "  Operate:              http://localhost:8181"
Write-Host "  Tasklist:             http://localhost:8182"
Write-Host "  Connectors:           http://localhost:8085"
Write-Host "  MinIO Console:        http://localhost:9001 (minio/password)"
Write-Host "  MySQL:                localhost:3306 (root/root)"
Write-Host "  Neo4j Browser:        http://localhost:7474 (neo4j/password)"
Write-Host "  Redis:                localhost:6379"
Write-Host ""

Write-Host "Backend Services:" -ForegroundColor Cyan
Write-Host "  Graph Service:        http://localhost:8090"
Write-Host "  Workflow Orchestrator:http://localhost:8086"
Write-Host "  User Service:         http://localhost:8083"
Write-Host "  Task Service:         http://localhost:8082"
Write-Host "  Document Service:     http://localhost:8081"
Write-Host "  BOM Service:          http://localhost:8089"
Write-Host "  Change Service:       http://localhost:8084"
Write-Host "  Search Service:       http://localhost:8091"
Write-Host ""

Write-Host "Frontend:" -ForegroundColor Cyan
Write-Host "  React UI:             http://localhost:3000"
Write-Host ""

Write-Host "Management Commands:" -ForegroundColor Yellow
Write-Host "  View containers:      docker ps"
Write-Host "  View container logs:  docker-compose -f docker-compose-master.yml logs -f [service]"
Write-Host "  Stop infrastructure:  docker-compose -f docker-compose-master.yml down"
Write-Host "  Stop all services:    .\stop-all-services.ps1"
Write-Host ""

Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Wait ~5 minutes for all services to fully start"
Write-Host "  2. Initialize databases (if first time):"
Write-Host "     - MySQL: .\init-mysql-databases-docker.bat"
Write-Host "     - Elasticsearch: .\reindex-all-elasticsearch.bat"
Write-Host "  3. Open browser: http://localhost:3000"
Write-Host ""

Read-Host "Press Enter to exit"


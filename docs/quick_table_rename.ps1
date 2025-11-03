# Quick table rename script for PLM databases (PowerShell)
# Run this in PowerShell to rename all tables to match entity names

Write-Host "Starting table rename migration..." -ForegroundColor Cyan

# Change these if your MySQL credentials are different
$mysqlUser = "root"
$mysqlPass = "your_password"
$mysqlPath = "mysql"

function Execute-MySQLCommand {
    param(
        [string]$database,
        [string]$command
    )
    
    $arguments = @(
        "-u", $mysqlUser,
        "-p$mysqlPass",
        $database,
        "-e", $command
    )
    
    & $mysqlPath $arguments
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Success" -ForegroundColor Green
    } else {
        Write-Host "  Failed" -ForegroundColor Red
    }
}

# Rename tables in plm_user_db
Write-Host ""
Write-Host "Renaming tables in plm_user_db..." -ForegroundColor Yellow
Execute-MySQLCommand "plm_user_db" "RENAME TABLE users TO User;"

# Rename tables in plm_change_db
Write-Host ""
Write-Host "Renaming tables in plm_change_db..." -ForegroundColor Yellow
Execute-MySQLCommand "plm_change_db" "RENAME TABLE change_table TO Change;"

# Rename tables in plm_task_db
Write-Host ""
Write-Host "Renaming tables in plm_task_db..." -ForegroundColor Yellow
Execute-MySQLCommand "plm_task_db" "RENAME TABLE tasks TO Task;"
Execute-MySQLCommand "plm_task_db" "RENAME TABLE task_signoffs TO TaskSignoff;"
Execute-MySQLCommand "plm_task_db" "RENAME TABLE file_metadata TO FileMetadata;"

# Rename tables in plm_document_db
Write-Host ""
Write-Host "Renaming tables in plm_document_db..." -ForegroundColor Yellow
Execute-MySQLCommand "plm_document_db" "RENAME TABLE document_master TO DocumentMaster;"
Execute-MySQLCommand "plm_document_db" "RENAME TABLE document TO Document;"
Execute-MySQLCommand "plm_document_db" "RENAME TABLE document_history TO DocumentHistory;"

Write-Host ""
Write-Host "Migration complete! Please restart your services." -ForegroundColor Green
Write-Host "Note: You may need to restart change-service, task-service, document-service, and user-service." -ForegroundColor Cyan

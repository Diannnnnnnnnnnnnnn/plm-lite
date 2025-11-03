# Data Cleanup Scripts Summary

## 📁 Created Files

The following cleanup scripts have been created for your PLM system:

### Master Cleanup Scripts
| File                    | Purpose                                      |
|-------------------------|----------------------------------------------|
| `cleanup-all-data.bat`  | Master cleanup script (Windows CMD)          |
| `cleanup-all-data.ps1`  | Master cleanup script (PowerShell)           |

### Individual Service Cleanup Scripts

#### MySQL Scripts
| File                     | Purpose                                     |
|--------------------------|---------------------------------------------|
| `cleanup-mysql-data.bat` | Drop all PLM databases (CMD)                |
| `cleanup-mysql-data.ps1` | Drop all PLM databases (PowerShell)         |

**Databases cleaned:**
- plm_auth
- plm_parts
- plm_bom
- plm_documents
- plm_changes
- plm_tasks
- plm_workflows
- plm_users

#### MinIO Scripts
| File                     | Purpose                                     |
|--------------------------|---------------------------------------------|
| `cleanup-minio-data.bat` | Delete all objects from MinIO (CMD)         |
| `cleanup-minio-data.ps1` | Delete all objects from MinIO (PowerShell)  |

**Data cleaned:**
- All files in `plm-documents` bucket

#### Elasticsearch Scripts
| File                            | Purpose                                     |
|---------------------------------|---------------------------------------------|
| `cleanup-elasticsearch-data.bat`| Delete all ES indices (CMD)                 |
| `cleanup-elasticsearch-data.ps1`| Delete all ES indices (PowerShell)          |

**Indices cleaned:**
- parts
- documents
- changes
- tasks
- boms

#### Neo4j Scripts
| File                      | Purpose                                     |
|---------------------------|---------------------------------------------|
| `cleanup-neo4j-data.bat`  | Delete all graph nodes/relationships (CMD)  |
| `cleanup-neo4j-data.ps1`  | Delete all graph nodes/relationships (PS)   |

**Data cleaned:**
- All nodes (Parts, Documents, Changes, Tasks, Users)
- All relationships

#### Local File System Scripts
| File                       | Purpose                                     |
|----------------------------|---------------------------------------------|
| `cleanup-local-files.bat`  | Delete local files and temp uploads (CMD)   |
| `cleanup-local-files.ps1`  | Delete local files and temp uploads (PS)    |

**Files cleaned:**
- `data/taskdb.mv.db` - Task service H2 database
- `document-service/data/documentdb.mv.db` - Document service database
- `document-service/temp-uploads/*` - All temporary uploads (128+ files)

### Documentation Files
| File                                    | Purpose                                    |
|-----------------------------------------|--------------------------------------------|
| `PRODUCTION_DATA_CLEANUP_GUIDE.md`      | Comprehensive cleanup and setup guide      |
| `CLEANUP_QUICK_REFERENCE.md`            | Quick reference card with commands         |
| `DATA_CLEANUP_SCRIPTS_SUMMARY.md`       | This file - scripts overview               |

## 🚀 Quick Start

### Option 1: Clean Everything (Recommended)

```cmd
cleanup-all-data.bat
```

Type `YES` when prompted to confirm.

### Option 2: Clean Individual Components

Run only the scripts you need:

```cmd
cleanup-mysql-data.bat
cleanup-minio-data.bat
cleanup-elasticsearch-data.bat
cleanup-neo4j-data.bat
cleanup-local-files.bat
```

## ⚠️ Important Notes

1. **Confirmation Required**: All scripts require you to type `YES` to proceed
2. **Irreversible**: Once deleted, data cannot be recovered
3. **Services Stop**: The master script stops all services before cleanup
4. **Prerequisites**: Ensure Docker containers are running before cleanup

## 📋 Execution Order

When running the master script, cleanup happens in this order:

1. **Stop all services** → Ensures data integrity
2. **Clean MySQL** → Drops all databases
3. **Clean MinIO** → Removes all stored files
4. **Clean Elasticsearch** → Deletes all indices
5. **Clean Neo4j** → Clears graph database
6. **Clean local files** → Removes temp files and local DBs

## 🔍 What's NOT Cleaned

The following are preserved:

- **Docker volumes** (structure remains, just emptied)
- **MinIO bucket** (`plm-documents` bucket still exists, just empty)
- **Application configuration files**
- **Database schemas** (will be recreated on restart)
- **Docker images and containers**
- **Service code**

## 🔐 Production Deployment Checklist

After running cleanup scripts:

- [ ] Update all default passwords
  - [ ] MySQL: `root` password
  - [ ] MinIO: `minio/password` credentials
  - [ ] Neo4j: `neo4j/password` credentials
  - [ ] Redis: `plm_redis_password`

- [ ] Review configuration files
  - [ ] Set `spring.profiles.active=prod`
  - [ ] Update CORS settings
  - [ ] Configure logging levels
  - [ ] Set proper file size limits

- [ ] Start infrastructure services
  - [ ] MySQL: `start-mysql-docker.bat`
  - [ ] Infrastructure: `docker-compose -f infra/docker-compose-infrastructure.yaml up -d`
  - [ ] Elasticsearch: `docker-compose -f docker-compose-elasticsearch.yml up -d`

- [ ] Initialize databases
  - [ ] Run: `init-mysql-databases-docker.bat`

- [ ] Start application services
  - [ ] Run: `start-all-services.bat`

- [ ] Create initial admin user
  - [ ] Via API or database

## 📊 Script Features

### Safety Features
- ✅ Confirmation prompts (requires typing `YES`)
- ✅ Service status checks before cleanup
- ✅ Clear error messages if services not running
- ✅ Informative output showing what was cleaned

### Convenience Features
- ✅ Both CMD and PowerShell versions
- ✅ Master script for one-command cleanup
- ✅ Individual scripts for selective cleanup
- ✅ Automatic service stopping
- ✅ Post-cleanup instructions

### Error Handling
- ✅ Checks if Docker containers are running
- ✅ Graceful handling of missing files
- ✅ Clear error messages with resolution steps
- ✅ Non-zero exit codes on failure

## 🔧 Troubleshooting

### Script Won't Run (PowerShell)
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Container Not Found
Ensure containers are running:
```cmd
docker ps
```

Start missing containers:
```cmd
start-mysql-docker.bat
docker-compose -f infra/docker-compose-infrastructure.yaml up -d
docker-compose -f docker-compose-elasticsearch.yml up -d
```

### Network Errors
Check Docker network exists:
```cmd
docker network ls | findstr plm
```

Create if missing:
```cmd
docker network create plm-network
```

## 📖 Additional Resources

- **Detailed Guide**: See `PRODUCTION_DATA_CLEANUP_GUIDE.md`
- **Quick Reference**: See `CLEANUP_QUICK_REFERENCE.md`
- **Database Setup**: See `README-DATABASE.md`
- **Service Startup**: See `README-STARTUP.md`

## ⚡ Example Usage

### Complete Fresh Start
```cmd
# 1. Clean everything
cleanup-all-data.bat

# 2. Start infrastructure
start-mysql-docker.bat
docker-compose -f infra/docker-compose-infrastructure.yaml up -d
docker-compose -f docker-compose-elasticsearch.yml up -d

# 3. Initialize databases
init-mysql-databases-docker.bat

# 4. Start services
start-all-services.bat
```

### Clean Only Specific Data
```cmd
# Clean just test uploads
cleanup-local-files.bat

# Clean just search indices
cleanup-elasticsearch-data.bat
```

## 📞 Support

If you encounter issues:

1. **Check logs**: `docker logs <container-name>`
2. **Verify status**: `docker ps`
3. **Check documentation**: Review the guide files
4. **Review scripts**: All scripts have inline comments

## 🎯 Script Verification

All scripts have been created and tested for:
- ✅ Windows 10/11 compatibility
- ✅ PowerShell 5.1+ compatibility
- ✅ Docker Desktop integration
- ✅ Proper error handling
- ✅ User confirmation
- ✅ Clear output messages

---

**Total Scripts Created**: 14 files (10 executable scripts + 3 documentation files + this summary)

**Last Updated**: November 2, 2025



-- =====================================================
-- PLM System - MySQL Database Initialization Script
-- =====================================================
-- This script creates all required databases and users
-- for the PLM microservices architecture
-- =====================================================

-- Create databases with UTF8MB4 character set
CREATE DATABASE IF NOT EXISTS plm_user_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS plm_bom_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS plm_document_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS plm_task_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS plm_change_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create PLM user with password
CREATE USER IF NOT EXISTS 'plm_user'@'%' IDENTIFIED BY 'plm_password';

-- Grant all privileges on PLM databases to the user
GRANT ALL PRIVILEGES ON plm_user_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_bom_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_document_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_task_db.* TO 'plm_user'@'%';
GRANT ALL PRIVILEGES ON plm_change_db.* TO 'plm_user'@'%';

-- Apply privilege changes
FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES LIKE 'plm_%';

-- Display user grants
SHOW GRANTS FOR 'plm_user'@'%';






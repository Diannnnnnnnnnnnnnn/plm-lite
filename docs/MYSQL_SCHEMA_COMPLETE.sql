-- =====================================================
-- PLM System - Complete MySQL Schema
-- =====================================================
-- Updated: 2025-11-02 (After BOM → Part Migration)
-- =====================================================

-- =====================================================
-- Database: plm_user_db
-- Service: user-service
-- =====================================================

CREATE TABLE `User` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `roles` VARCHAR(500),  -- JSON array stored as string
  
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Database: plm_bom_db
-- Service: bom-service
-- =====================================================

-- Part Table (replaces BomHeader)
CREATE TABLE `Part` (
  `bigintid` VARCHAR(255) PRIMARY KEY NOT NULL,
  `titlechar` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `stage` VARCHAR(50) NOT NULL,
  `status` VARCHAR(50),
  `level` VARCHAR(255) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `updateTime` DATETIME NOT NULL,
  `deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleteTime` DATETIME,
  
  INDEX `idx_part_creator` (`creator`),
  INDEX `idx_part_stage_status` (`stage`, `status`),
  INDEX `idx_part_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Part Usage Relationships (Parent-Child with Quantity)
CREATE TABLE `PartUsage` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `parent_id` VARCHAR(255) NOT NULL,
  `child_id` VARCHAR(255) NOT NULL,
  `quantity` INT NOT NULL,
  
  FOREIGN KEY (`parent_id`) REFERENCES `Part` (`bigintid`) ON DELETE CASCADE,
  FOREIGN KEY (`child_id`) REFERENCES `Part` (`bigintid`) ON DELETE CASCADE,
  
  INDEX `idx_partusage_parent` (`parent_id`),
  INDEX `idx_partusage_child` (`child_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Document-Part Link Table
CREATE TABLE `DocumentPartLink` (
  `link_id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `part_id` VARCHAR(255) NOT NULL,
  `document_id` VARCHAR(255) NOT NULL,
  
  FOREIGN KEY (`part_id`) REFERENCES `Part` (`bigintid`) ON DELETE CASCADE,
  
  INDEX `idx_docpartlink_part` (`part_id`),
  INDEX `idx_docpartlink_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DEPRECATED TABLES (Removed after migration)
-- =====================================================
-- The following tables are NO LONGER USED:
-- - bom_headers (replaced by Part)
-- - bom_items (replaced by PartUsage)
-- =====================================================

-- =====================================================
-- Database: plm_change_db
-- Service: change-service
-- =====================================================

-- Change/ECR Table
CREATE TABLE `Change` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `stage` VARCHAR(50) NOT NULL,
  `class` VARCHAR(100) NOT NULL,
  `product` VARCHAR(255) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `creator` VARCHAR(255) NOT NULL,
  `create_time` DATETIME NOT NULL,
  `change_reason` TEXT NOT NULL,
  `change_document` VARCHAR(255) NOT NULL,
  
  INDEX `idx_change_creator` (`creator`),
  INDEX `idx_change_status` (`status`),
  INDEX `idx_change_stage` (`stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DEPRECATED TABLE (Removed - redundant with ChangePart)
-- =====================================================
-- change_bom table is NO LONGER USED
-- All Change-Part relationships are now in ChangePart table
-- =====================================================

-- Change-Part Links
CREATE TABLE `ChangePart` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `changetask_id` VARCHAR(255) NOT NULL,
  `part_id` VARCHAR(255) NOT NULL,
  
  FOREIGN KEY (`changetask_id`) REFERENCES `change_table` (`id`) ON DELETE CASCADE,
  
  INDEX `idx_changepart_change` (`changetask_id`),
  INDEX `idx_changepart_part` (`part_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Change-Document Relationships
CREATE TABLE `ChangeDocument` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `changetask_id` VARCHAR(255) NOT NULL,
  `document_id` VARCHAR(255) NOT NULL,
  
  FOREIGN KEY (`changetask_id`) REFERENCES `change_table` (`id`) ON DELETE CASCADE,
  
  INDEX `idx_changedoc_change` (`changetask_id`),
  INDEX `idx_changedoc_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Database: plm_document_db
-- Service: document-service
-- =====================================================

-- Document Master (version container)
CREATE TABLE `DocumentMaster` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `title` VARCHAR(255),
  `creator` VARCHAR(255),
  `category` VARCHAR(255),
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  INDEX `idx_docmaster_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Document (individual versions/revisions)
CREATE TABLE `Document` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `master_id` VARCHAR(255) NOT NULL,
  `version` INT NOT NULL,
  `revision` INT NOT NULL,
  `stage` VARCHAR(50),
  `status` VARCHAR(50),
  `title` VARCHAR(255),
  `description` TEXT,
  `creator` VARCHAR(255),
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `fileKey` VARCHAR(500),
  `partId` VARCHAR(255),  -- CHANGED FROM bomId to partId
  `originalFilename` VARCHAR(500),
  `contentType` VARCHAR(255),
  `fileSize` BIGINT,
  `storageLocation` VARCHAR(50),
  `fileUploadedAt` DATETIME,
  `isActive` BOOLEAN NOT NULL DEFAULT TRUE,
  
  FOREIGN KEY (`master_id`) REFERENCES `DocumentMaster` (`id`) ON DELETE CASCADE,
  
  INDEX `idx_document_master` (`master_id`),
  INDEX `idx_document_creator` (`creator`),
  INDEX `idx_document_status` (`status`),
  INDEX `idx_document_partid` (`partId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Document History/Audit Trail
CREATE TABLE `DocumentHistory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `documentId` VARCHAR(255),
  `action` VARCHAR(100),
  `oldValue` TEXT,
  `newValue` TEXT,
  `created_by` VARCHAR(255),
  `comment` TEXT,
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  INDEX `idx_dochistory_document` (`documentId`),
  INDEX `idx_dochistory_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Database: plm_task_db
-- Service: task-service
-- =====================================================

-- Task Table
CREATE TABLE `Task` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `task_name` VARCHAR(500) NOT NULL,
  `task_description` TEXT,
  `task_type` VARCHAR(50) NOT NULL,
  `task_status` VARCHAR(50) NOT NULL,
  `assigned_to` VARCHAR(255) NOT NULL,
  `assigned_by` VARCHAR(255) NOT NULL,
  `due_date` DATETIME,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME,
  `priority` INT,
  `parent_task_id` VARCHAR(255),
  `workflow_id` VARCHAR(255),
  `context_type` VARCHAR(50),
  `context_id` VARCHAR(255),
  
  INDEX `idx_task_assigned_to` (`assigned_to`),
  INDEX `idx_task_status` (`task_status`),
  INDEX `idx_task_context` (`context_type`, `context_id`),
  INDEX `idx_task_workflow` (`workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Task Signoffs/Approvals
CREATE TABLE `TaskSignoff` (
  `id` VARCHAR(255) PRIMARY KEY NOT NULL,
  `task_id` VARCHAR(255) NOT NULL,
  `signoff_user` VARCHAR(255) NOT NULL,
  `signoff_action` VARCHAR(50) NOT NULL,
  `comments` TEXT,
  `signoff_timestamp` DATETIME NOT NULL,
  `is_required` BOOLEAN NOT NULL DEFAULT TRUE,
  
  FOREIGN KEY (`task_id`) REFERENCES `Task` (`id`) ON DELETE CASCADE,
  
  INDEX `idx_tasksignoff_task` (`task_id`),
  INDEX `idx_tasksignoff_user` (`signoff_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- File Metadata (attached to tasks)
CREATE TABLE `FileMetadata` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `filename` VARCHAR(500),
  `fileUrl` VARCHAR(1000),
  `task_id` VARCHAR(255),
  
  FOREIGN KEY (`task_id`) REFERENCES `Task` (`id`) ON DELETE CASCADE,
  
  INDEX `idx_filemeta_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ENUM VALUES (Used across tables)
-- =====================================================

-- Stage enum values:
--   CONCEPTUAL_DESIGN
--   PRELIMINARY_DESIGN
--   DETAILED_DESIGN
--   MANUFACTURING
--   IN_SERVICE
--   RETIRED

-- Status enum values:
--   DRAFT
--   IN_WORK
--   IN_REVIEW
--   IN_TECHNICAL_REVIEW
--   APPROVED
--   RELEASED
--   OBSOLETE

-- TaskType enum values:
--   APPROVAL
--   REVIEW
--   NOTIFICATION
--   ACTION
--   WORKFLOW

-- TaskStatus enum values:
--   PENDING
--   IN_PROGRESS
--   COMPLETED
--   CANCELLED
--   OVERDUE

-- SignoffAction enum values:
--   APPROVED
--   REJECTED
--   DEFERRED
--   COMMENTED

-- =====================================================
-- DATA MIGRATION NOTES
-- =====================================================
-- If upgrading from BOM-based system, run:
--
-- ALTER TABLE change_bom CHANGE COLUMN bom_id part_id VARCHAR(255);
-- ALTER TABLE document CHANGE COLUMN bomId partId VARCHAR(255);
--
-- Then drop old tables:
-- DROP TABLE IF EXISTS bom_items;
-- DROP TABLE IF EXISTS bom_headers;
-- =====================================================


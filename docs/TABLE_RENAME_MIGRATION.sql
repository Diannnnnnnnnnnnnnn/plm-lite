-- ============================================
-- Table Name Standardization Migration Script
-- ============================================
-- This script renames all tables to match their entity class names (PascalCase)
-- Execute these statements in the respective databases
--
-- IMPORTANT: Backup your databases before running these migrations!
-- ============================================

-- ============================================
-- 1. PLM_USER_DB: Rename 'users' to 'User'
-- ============================================
USE plm_user_db;

-- Rename the table
RENAME TABLE `users` TO `User`;

-- ============================================
-- 2. PLM_CHANGE_DB: Rename tables
-- ============================================
USE plm_change_db;

-- Rename change_table to Change
RENAME TABLE `change_table` TO `Change`;

-- Note: change_bom has been removed from the application
-- If it exists in your database, you can optionally drop it:
-- DROP TABLE IF EXISTS `change_bom`;

-- ============================================
-- 3. PLM_TASK_DB: Rename tables
-- ============================================
USE plm_task_db;

-- Rename tasks to Task
RENAME TABLE `tasks` TO `Task`;

-- Rename task_signoffs to TaskSignoff
RENAME TABLE `task_signoffs` TO `TaskSignoff`;

-- Rename file_metadata to FileMetadata
RENAME TABLE `file_metadata` TO `FileMetadata`;

-- ============================================
-- 4. PLM_DOCUMENT_DB: Rename tables
-- ============================================
USE plm_document_db;

-- Rename document_master to DocumentMaster
RENAME TABLE `document_master` TO `DocumentMaster`;

-- Rename document to Document
RENAME TABLE `document` TO `Document`;

-- Rename document_history to DocumentHistory
RENAME TABLE `document_history` TO `DocumentHistory`;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify the tables were renamed correctly:

USE plm_user_db;
SHOW TABLES;
-- Expected: User

USE plm_change_db;
SHOW TABLES;
-- Expected: Change, ChangePart, ChangeDocument

USE plm_task_db;
SHOW TABLES;
-- Expected: Task, TaskSignoff, FileMetadata

USE plm_document_db;
SHOW TABLES;
-- Expected: DocumentMaster, Document, DocumentHistory

-- ============================================
-- ROLLBACK SCRIPT (if needed)
-- ============================================
-- If you need to revert the changes, uncomment and run:

-- USE plm_user_db;
-- RENAME TABLE `User` TO `users`;

-- USE plm_change_db;
-- RENAME TABLE `Change` TO `change_table`;

-- USE plm_task_db;
-- RENAME TABLE `Task` TO `tasks`;
-- RENAME TABLE `TaskSignoff` TO `task_signoffs`;
-- RENAME TABLE `FileMetadata` TO `file_metadata`;

-- USE plm_document_db;
-- RENAME TABLE `DocumentMaster` TO `document_master`;
-- RENAME TABLE `Document` TO `document`;
-- RENAME TABLE `DocumentHistory` TO `document_history`;



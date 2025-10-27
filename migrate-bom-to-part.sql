-- =========================================
-- BOM Header → Part Migration Script
-- =========================================
-- This script migrates existing BOM Headers and Items to the Part-based system
-- Run this AFTER deploying the enhanced Part model

-- Step 1: Add new columns to Part table (if not already added by JPA)
-- This is handled by Hibernate DDL with spring.jpa.hibernate.ddl-auto=update

-- Step 2: Migrate BOM Headers to Parts
-- Each BOM Header becomes a root Part with level='ASSEMBLY'
INSERT INTO part (
    bigintid,
    titlechar,
    description,
    stage,
    status,
    level,
    creator,
    create_time,
    update_time,
    deleted,
    delete_time
)
SELECT 
    id AS bigintid,
    description AS titlechar,  -- BOM description becomes Part title
    description,                -- Keep description
    stage,
    status,
    'ASSEMBLY' AS level,        -- BOM Headers are assemblies
    creator,
    create_time,
    update_time,
    deleted,
    delete_time
FROM bom_headers
WHERE deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM part WHERE part.bigintid = bom_headers.id
  );

-- Step 3: Create Parts for unique part numbers in BOM Items
-- BOM Items reference parts by part_number (string), we need actual Part entities
INSERT INTO part (
    bigintid,
    titlechar,
    description,
    stage,
    status,
    level,
    creator,
    create_time,
    update_time,
    deleted
)
SELECT DISTINCT
    CONCAT('PART-', part_number) AS bigintid,  -- Generate ID from part number
    part_number AS titlechar,
    description,
    'IN_WORK' AS stage,         -- Default stage
    'DRAFT' AS status,           -- Default status
    'PART' AS level,
    'MIGRATION' AS creator,
    NOW() AS create_time,
    NOW() AS update_time,
    false AS deleted
FROM bom_items
WHERE NOT EXISTS (
    SELECT 1 FROM part WHERE part.bigintid = CONCAT('PART-', bom_items.part_number)
);

-- Step 4: Create PartUsage relationships from BOM Items
-- Link BOM Header (now Part) to its items (now Parts) via PartUsage
INSERT INTO part_usage (
    id,
    parent_id,
    child_id,
    quantity
)
SELECT 
    CONCAT('USAGE-', bi.id) AS id,
    bi.header_id AS parent_id,         -- BOM Header ID is now Parent Part ID
    CONCAT('PART-', bi.part_number) AS child_id,  -- Part number → Part ID
    CAST(bi.quantity AS INTEGER) AS quantity
FROM bom_items bi
WHERE EXISTS (SELECT 1 FROM part WHERE part.bigintid = bi.header_id)
  AND EXISTS (SELECT 1 FROM part WHERE part.bigintid = CONCAT('PART-', bi.part_number))
  AND NOT EXISTS (
      SELECT 1 FROM part_usage 
      WHERE part_usage.id = CONCAT('USAGE-', bi.id)
  );

-- Step 5: Migrate Document links
-- BOM Headers have documentId, create DocumentPartLink entries
INSERT INTO document_part_link (
    link_id,
    part_id,
    document_id
)
SELECT 
    CONCAT('LINK-', bh.id, '-', bh.document_id) AS link_id,
    bh.id AS part_id,
    bh.document_id
FROM bom_headers bh
WHERE bh.document_id IS NOT NULL
  AND bh.document_id != ''
  AND bh.deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM document_part_link 
      WHERE document_part_link.part_id = bh.id 
        AND document_part_link.document_id = bh.document_id
  );

-- Step 6: Verification Queries
-- Run these to verify migration success

-- Check: How many BOM Headers were migrated?
SELECT 
    'BOM Headers (deleted=false)' AS metric,
    COUNT(*) AS count
FROM bom_headers
WHERE deleted = false

UNION ALL

SELECT 
    'Parts (from BOMs)' AS metric,
    COUNT(*) AS count
FROM part
WHERE creator != 'MIGRATION';  -- Exclude parts created from BOM items

-- Check: How many BOM Items became Part relationships?
SELECT 
    'BOM Items' AS metric,
    COUNT(*) AS count
FROM bom_items

UNION ALL

SELECT 
    'Part Usages (from migration)' AS metric,
    COUNT(*) AS count
FROM part_usage
WHERE id LIKE 'USAGE-%';

-- Check: Document links migrated?
SELECT 
    'BOM Headers with documents' AS metric,
    COUNT(*) AS count
FROM bom_headers
WHERE document_id IS NOT NULL AND document_id != '' AND deleted = false

UNION ALL

SELECT 
    'Document-Part links (from migration)' AS metric,
    COUNT(*) AS count
FROM document_part_link
WHERE link_id LIKE 'LINK-%';

-- Sample: Show migrated Part hierarchy
SELECT 
    parent.titlechar AS parent_part,
    parent.status,
    pu.quantity,
    child.titlechar AS child_part,
    child.description
FROM part parent
JOIN part_usage pu ON parent.bigintid = pu.parent_id
JOIN part child ON child.bigintid = pu.child_id
LIMIT 10;

-- =========================================
-- Rollback Script (if needed)
-- =========================================

-- CAUTION: Only run this if migration failed and you need to revert

-- DELETE FROM document_part_link WHERE link_id LIKE 'LINK-%';
-- DELETE FROM part_usage WHERE id LIKE 'USAGE-%';
-- DELETE FROM part WHERE creator = 'MIGRATION';
-- DELETE FROM part WHERE bigintid IN (SELECT id FROM bom_headers WHERE deleted = false);

-- =========================================
-- Post-Migration Steps
-- =========================================

-- 1. Sync all migrated Parts to Neo4j
--    Run this PowerShell script:
--    .\sync-migrated-parts-to-neo4j.ps1

-- 2. Verify in Neo4j Browser:
--    MATCH (p:Part) RETURN count(p)
--    MATCH ()-[r:HAS_CHILD]->() RETURN count(r)

-- 3. Update frontend to use /parts API instead of /boms

-- 4. After successful migration and verification:
--    - Mark BOM tables as deprecated
--    - Remove BOM endpoints (or keep for backward compatibility)
--    - Update documentation

-- =========================================
-- Notes
-- =========================================

-- * This migration creates Parts from BOM Headers with same IDs
-- * BOM Items are converted to Part entities with generated IDs (PART-{part_number})
-- * PartUsage relationships preserve quantities
-- * Document links are preserved
-- * Soft-deleted BOMs are excluded
-- * The migration is idempotent - safe to run multiple times


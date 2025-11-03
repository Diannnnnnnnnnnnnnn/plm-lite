-- Migrate task data from old columns (name, description) to new columns (task_name, task_description)
-- Run this in MySQL to fix existing tasks

USE plm_task_db;

-- Update existing tasks: copy name -> task_name, description -> task_description
UPDATE tasks 
SET 
    task_name = name,
    task_description = description
WHERE 
    task_name IS NULL 
    AND name IS NOT NULL;

-- Show results
SELECT id, name, task_name, description, task_description, assigned_to 
FROM tasks 
WHERE assigned_to = 'vivi';

-- After verifying the migration worked, you can optionally drop the old columns:
-- ALTER TABLE tasks DROP COLUMN name;
-- ALTER TABLE tasks DROP COLUMN description;


-- Fix: Increase description column length from 255 to 1000 characters
-- This is needed for workflow-generated task descriptions

-- Run this in H2 Console at: http://localhost:8082/h2-console
-- JDBC URL: jdbc:h2:file:./data/taskdb
-- Username: sa
-- Password: password

-- Alter the description column to support longer text
ALTER TABLE tasks ALTER COLUMN description VARCHAR(1000);

-- Verify the change
SELECT COLUMN_NAME, TYPE_NAME, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'TASKS' AND COLUMN_NAME = 'DESCRIPTION';

-- Should show: DESCRIPTION | CHARACTER VARYING | 1000




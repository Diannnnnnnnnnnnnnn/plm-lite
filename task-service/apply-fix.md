# Fix: Description Column Too Short

## The Problem
The `description` column in the `tasks` table is limited to 255 characters, but the two-stage review workflow creates descriptions that are ~299 characters long.

**Error:**
```
Value too long for column "DESCRIPTION CHARACTER VARYING(255)": 
"Please perform the initial review of document '7878' version v0.1 submitted... (299)"
```

## Solution Applied
Updated `Task.java` to specify `@Column(length = 1000)` for the description field.

## How to Apply (Choose ONE method)

### Method 1: Manual SQL (No Restart Required) ⚡ FASTEST

1. Open **H2 Console** in your browser: http://localhost:8082/h2-console

2. Login with:
   - **JDBC URL:** `jdbc:h2:file:./data/taskdb`
   - **Username:** `sa`
   - **Password:** `password`

3. Run this SQL command:
   ```sql
   ALTER TABLE tasks ALTER COLUMN description VARCHAR(1000);
   ```

4. Verify it worked:
   ```sql
   SELECT COLUMN_NAME, TYPE_NAME, CHARACTER_MAXIMUM_LENGTH 
   FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'TASKS' AND COLUMN_NAME = 'DESCRIPTION';
   ```
   
   Should show: `DESCRIPTION | CHARACTER VARYING | 1000`

5. **Done!** Try your workflow again immediately.

### Method 2: Restart task-service (Automatic)

1. **Stop** the task-service (Ctrl+C or close the terminal/IDE)

2. **Restart** it:
   ```bash
   cd task-service
   mvn spring-boot:run
   ```

3. Watch for Hibernate to alter the column:
   ```
   Hibernate: alter table tasks alter column description varchar(1000)
   ```

4. **Done!** Try your workflow again.

## After Applying the Fix

Your workflow should now work correctly. You should see:

```
✓ Resolved user ID 2 to username: vivi
✓ Created initial review task ID: 365
✅ Linked task with job key for auto-completion
```

Instead of the 500 error.

## Why This Happened

The workflow creates detailed task descriptions that include:
- Review instructions
- Document information  
- Two-stage process explanation
- Document ID
- Workflow instance key

This totals ~299 characters, exceeding the default 255 character limit.



#!/bin/bash
# Quick table rename script for PLM databases

echo "Starting table rename migration..."

# Change these if your MySQL credentials are different
MYSQL_USER="root"
MYSQL_PASS="your_password"  # Change this!

# Rename tables in plm_user_db
echo "Renaming tables in plm_user_db..."
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_user_db -e "RENAME TABLE users TO \`User\`;"

# Rename tables in plm_change_db
echo "Renaming tables in plm_change_db..."
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_change_db -e "RENAME TABLE change_table TO \`Change\`;"

# Rename tables in plm_task_db
echo "Renaming tables in plm_task_db..."
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_task_db -e "RENAME TABLE tasks TO \`Task\`;"
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_task_db -e "RENAME TABLE task_signoffs TO \`TaskSignoff\`;"
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_task_db -e "RENAME TABLE file_metadata TO \`FileMetadata\`;"

# Rename tables in plm_document_db
echo "Renaming tables in plm_document_db..."
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_document_db -e "RENAME TABLE document_master TO \`DocumentMaster\`;"
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_document_db -e "RENAME TABLE document TO \`Document\`;"
mysql -u $MYSQL_USER -p$MYSQL_PASS plm_document_db -e "RENAME TABLE document_history TO \`DocumentHistory\`;"

echo "Migration complete! Restart your services."



# Testing Guide: Duplicate Master ID Validation

## Overview
This guide provides step-by-step instructions to test the new Master ID duplicate validation feature.

## Prerequisites
1. Ensure both backend (document-service) and frontend are running
2. Have access to the Document Manager in the frontend
3. Have at least one document already created (or create one during testing)

## Test Scenarios

### Test 1: Create a Document with a Unique Master ID ✅

**Steps:**
1. Navigate to the Document Manager page in the frontend
2. Click the "New Document" button
3. Fill in the form:
   - Upload a file (any file)
   - **Document Title:** "Test Document 1"
   - **Document Number (Master ID):** "TEST-001" (make sure this doesn't exist)
   - **Description:** "This is a test document"
   - **Stage:** Select any stage (e.g., "Conceptual Design")
4. Click "Upload Document"

**Expected Result:**
- ✅ Document is created successfully
- ✅ Success message is displayed: "Document 'Test Document 1' uploaded successfully!"
- ✅ The new document appears in the document list with Master ID "TEST-001"

### Test 2: Attempt to Create a Document with a Duplicate Master ID ❌

**Steps:**
1. Navigate to the Document Manager page
2. Click the "New Document" button
3. Fill in the form:
   - Upload a file (any file)
   - **Document Title:** "Test Document 2"
   - **Document Number (Master ID):** "TEST-001" (use the SAME Master ID from Test 1)
   - **Description:** "This should fail"
   - **Stage:** Select any stage
4. Click "Upload Document"

**Expected Result:**
- ❌ Document creation fails
- ⚠️ An error alert is displayed with the message:
  ```
  ⚠️ Duplicate Master ID

  Master ID 'TEST-001' is already in use. 
  Please use a different Master ID or create a new version of the existing document.

  Please choose a different Document Number (Master ID) or leave it empty to auto-generate one.
  ```
- ❌ The document is NOT created
- ❌ No new document appears in the document list

### Test 3: Validate Required Field Enforcement ⚠️

**Steps:**
1. Navigate to the Document Manager page
2. Click the "New Document" button
3. Fill in the form:
   - Upload a file (any file)
   - **Document Title:** "Test Document 3"
   - **Document Number (Master ID):** Leave this field **EMPTY**
   - **Description:** "Testing empty Master ID"
   - **Stage:** Select any stage
4. Observe the "Upload Document" button

**Expected Result:**
- ⚠️ The "Upload Document" button is **DISABLED** (grayed out)
- ⚠️ If you somehow manage to submit (e.g., through browser dev tools), an alert appears:
  ```
  Please provide a Document Number (Master ID). 
  This is a unique identifier for your document.
  ```
- ❌ The document is NOT created

### Test 4: Verify Helper Text is Displayed 📝

**Steps:**
1. Navigate to the Document Manager page
2. Click the "New Document" button
3. Look at the "Document Number (Master ID)" field

**Expected Result:**
- 📝 The field label shows: "Document Number (Master ID)"
- 📝 A red asterisk (*) appears indicating it's required
- 📝 Helper text below the field shows: "This is a unique identifier for your document. It cannot be reused."
- 📝 Placeholder text shows: "e.g., SPEC-001, TD-001"

### Test 5: Case Sensitivity Test (Optional) 🔤

**Steps:**
1. Create a document with Master ID: "test-001"
2. Try to create another document with Master ID: "TEST-001"

**Expected Result:**
- Depends on database collation settings
- Most likely: ❌ Will fail with duplicate error (case-insensitive)
- Alternative: ✅ Will succeed (case-sensitive)

### Test 6: Special Characters Test 🔣

**Steps:**
1. Try creating documents with various Master IDs:
   - "TEST-001" (with hyphen)
   - "TEST_001" (with underscore)
   - "TEST.001" (with period)
   - "TEST 001" (with space)
   - "TEST#001" (with special character)

**Expected Result:**
- ✅ All should be accepted by the frontend
- ✅ Backend should accept alphanumeric and common separators
- ⚠️ Some special characters might be rejected by the backend validation

## API Testing (Optional - Using Postman/curl)

### Test API Directly

**Request 1: Create Document with New Master ID**
```bash
POST http://localhost:8081/api/v1/documents
Content-Type: application/json

{
  "masterId": "API-TEST-001",
  "title": "API Test Document",
  "description": "Testing via API",
  "creator": "TestUser",
  "category": "Test",
  "stage": "CONCEPTUAL_DESIGN"
}
```

**Expected Response:**
```json
{
  "id": "some-uuid",
  "master": {
    "id": "API-TEST-001",
    "documentNumber": "API-TEST-001"
  },
  "title": "API Test Document",
  "status": "IN_WORK",
  "revision": 0,
  "version": 1
  // ... other fields
}
```

**Request 2: Create Document with Duplicate Master ID**
```bash
POST http://localhost:8081/api/v1/documents
Content-Type: application/json

{
  "masterId": "API-TEST-001",
  "title": "Duplicate Test",
  "description": "This should fail",
  "creator": "TestUser",
  "category": "Test",
  "stage": "CONCEPTUAL_DESIGN"
}
```

**Expected Response:**
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Master ID 'API-TEST-001' is already in use. Please use a different Master ID or create a new version of the existing document.",
  "path": "uri=/api/v1/documents"
}
```

## Troubleshooting

### Issue: Changes Not Reflecting

**Solution:**
1. Restart the document-service:
   ```bash
   # Stop the service (Ctrl+C or use stop-all-services.bat)
   # Rebuild the service
   cd document-service
   mvn clean install -DskipTests
   # Restart the service
   ```

2. Refresh the frontend:
   ```bash
   # If using React development server
   # Press Ctrl+C and restart
   cd frontend
   npm start
   
   # Or hard refresh the browser (Ctrl+Shift+R)
   ```

### Issue: Button Not Disabled When Field Empty

**Solution:**
- Clear browser cache
- Hard refresh the page (Ctrl+Shift+R)
- Check browser console for JavaScript errors

### Issue: Error Message Not Displaying

**Solution:**
1. Open browser Developer Tools (F12)
2. Check the Console tab for errors
3. Check the Network tab to see the API response
4. Verify the error response contains the expected message

### Issue: Backend Not Throwing ValidationException

**Solution:**
1. Check if the document-service compiled successfully
2. Check the application logs for any errors
3. Verify the changes were saved correctly in DocumentServiceImpl.java
4. Restart the document-service

## Database Verification

To verify the Master ID in the database:

**H2 Database Console** (if using embedded H2):
1. Navigate to http://localhost:8081/h2-console
2. Use JDBC URL: `jdbc:h2:file:./data/documentdb`
3. Query:
   ```sql
   SELECT * FROM document_master;
   ```

**Expected:**
- Each Master ID appears only once
- No duplicate IDs in the `document_master` table

## Success Criteria

All tests pass if:
- ✅ Unique Master IDs can be created successfully
- ❌ Duplicate Master IDs are rejected with clear error message
- ⚠️ Empty Master ID field disables the submit button
- 📝 Helper text guides users appropriately
- 🔧 API returns proper HTTP status codes (200 for success, 400 for validation error)

## Reporting Issues

If any test fails, please provide:
1. Test scenario number and description
2. Steps you followed
3. Expected result vs actual result
4. Screenshots of error messages
5. Browser console logs (if frontend issue)
6. Application logs (if backend issue)

## Cleanup After Testing

To clean up test documents:
1. Go to Document Manager
2. Click on each test document
3. Click the "Delete" button
4. Confirm deletion

Or use the API:
```bash
DELETE http://localhost:8081/api/v1/documents/{document-id}
```


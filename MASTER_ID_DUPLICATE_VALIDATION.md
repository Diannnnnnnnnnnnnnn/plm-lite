# Master ID Duplicate Validation Implementation

## Overview
This document describes the implementation of Master ID (Document Number) duplicate validation to prevent users from creating new documents with already-used Master IDs.

## Changes Made

### Backend Changes

#### 1. DocumentServiceImpl.java
**File:** `document-service/src/main/java/com/example/document_service/service/impl/DocumentServiceImpl.java`

**Changes:**
- Added validation in the `create()` method to check if a Master ID already exists before creating a new document
- If a duplicate Master ID is detected, a `ValidationException` is thrown with a clear error message
- Removed the fallback logic that would reuse an existing DocumentMaster

**Code Changes (Line 103-139):**
```java
@Transactional
@Override
public Document create(CreateDocumentRequest req) {
    validateCreateRequest(req);
    
    // Check if a document with this masterID already exists
    if (masterRepo.existsById(req.getMasterId())) {
        throw new ValidationException(
            "Master ID '" + req.getMasterId() + "' is already in use. " +
            "Please use a different Master ID or create a new version of the existing document."
        );
    }
    
    // Create new DocumentMaster (no longer reuses existing ones)
    DocumentMaster master = new DocumentMaster();
    master.setId(req.getMasterId());
    master.setTitle(req.getTitle());
    master.setCreator(req.getCreator());
    master.setCategory(req.getCategory());
    master = masterRepo.save(master);
    
    // ... rest of document creation logic
}
```

**Impact:**
- ✅ Master IDs are now enforced as unique identifiers
- ✅ Clear error message returned to frontend when duplicate is detected
- ✅ Existing GlobalExceptionHandler properly handles ValidationException and returns HTTP 400

### Frontend Changes

#### 2. DocumentManager.js
**File:** `frontend/src/components/Documents/DocumentManager.js`

**Changes:**

##### a. Enhanced Error Handling (Line 546-561)
Added specific error handling for duplicate Master ID errors with user-friendly messaging:

```javascript
} catch (error) {
  console.error('Error uploading document:', error);
  console.error('Error details:', error.response?.data || error.message);
  
  // Check if this is a duplicate masterID error
  const errorMessage = error.response?.data?.message || error.message;
  if (errorMessage && errorMessage.includes('already in use')) {
    alert(
      '⚠️ Duplicate Master ID\n\n' +
      errorMessage + '\n\n' +
      'Please choose a different Document Number (Master ID) or leave it empty to auto-generate one.'
    );
  } else {
    alert(`Failed to upload document: ${errorMessage}`);
  }
}
```

##### b. Enhanced Form Field (Line 1248-1258)
Updated the Document Number field to indicate it's required and must be unique:

```javascript
<TextField
  fullWidth
  label="Document Number (Master ID)"
  variant="outlined"
  value={newDocument.documentNumber}
  onChange={(e) => setNewDocument({...newDocument, documentNumber: e.target.value})}
  margin="normal"
  placeholder="e.g., SPEC-001, TD-001"
  required
  helperText="This is a unique identifier for your document. It cannot be reused."
/>
```

##### c. Enhanced Validation (Line 492-495)
Added explicit validation check for Document Number before submission:

```javascript
if (!newDocument.documentNumber) {
  alert('Please provide a Document Number (Master ID). This is a unique identifier for your document.');
  return;
}
```

##### d. Updated Button Validation (Line 1331-1337)
Updated the Upload Document button to be disabled when Document Number is missing:

```javascript
<Button
  variant="contained"
  onClick={handleCreateDocument}
  disabled={!selectedFile || !newDocument.title || !newDocument.documentNumber}
>
  Upload Document
</Button>
```

## User Experience Flow

### Scenario 1: Creating a Document with a New Master ID
1. User opens "New Document" dialog
2. User fills in all required fields including a unique Document Number (e.g., "SPEC-001")
3. User clicks "Upload Document"
4. ✅ Document is created successfully

### Scenario 2: Attempting to Create a Document with a Duplicate Master ID
1. User opens "New Document" dialog
2. User fills in all fields including a Document Number that already exists (e.g., "SPEC-001")
3. User clicks "Upload Document"
4. ❌ Backend detects duplicate Master ID
5. ⚠️ User sees a clear error message:
   ```
   ⚠️ Duplicate Master ID
   
   Master ID 'SPEC-001' is already in use. 
   Please use a different Master ID or create a new version of the existing document.
   
   Please choose a different Document Number (Master ID) or leave it empty to auto-generate one.
   ```
6. User must choose a different Document Number and try again

### Scenario 3: Missing Document Number
1. User opens "New Document" dialog
2. User fills in some fields but leaves Document Number empty
3. Upload Document button is **disabled** (cannot click)
4. If somehow the validation is bypassed, an alert will show:
   ```
   Please provide a Document Number (Master ID). 
   This is a unique identifier for your document.
   ```

## Technical Details

### API Flow
1. **Frontend** → POST `/api/v1/documents`
   - Sends `CreateDocumentRequest` with `masterId` field
   
2. **Backend** → `DocumentController.create()`
   - Calls `DocumentService.create()`
   
3. **Backend** → `DocumentServiceImpl.create()`
   - Validates request
   - **NEW:** Checks if `masterId` already exists using `masterRepo.existsById()`
   - If exists → throws `ValidationException`
   - If not exists → creates new `DocumentMaster` and `Document`
   
4. **Backend** → `GlobalExceptionHandler`
   - Catches `ValidationException`
   - Returns HTTP 400 with error message in `ApiError` format
   
5. **Frontend** → `handleCreateDocument()` catch block
   - Receives error response
   - Checks if error message contains "already in use"
   - Shows appropriate alert message to user

### Error Response Format
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Master ID 'SPEC-001' is already in use. Please use a different Master ID or create a new version of the existing document.",
  "path": "uri=/api/v1/documents"
}
```

## Benefits

1. **Data Integrity**: Ensures each document has a unique Master ID
2. **User Clarity**: Clear, actionable error messages guide users to correct their input
3. **Prevent Confusion**: Avoids accidental reuse of existing Master IDs
4. **Professional UX**: 
   - Required field indicator (*)
   - Helper text explaining the field's purpose
   - Disabled button when validation fails
   - Clear error messages with emoji indicators

## Testing Recommendations

### Test Case 1: Create New Document with Unique Master ID
- **Input:** documentNumber = "TEST-001" (new)
- **Expected:** ✅ Document created successfully

### Test Case 2: Create Document with Duplicate Master ID
- **Input:** documentNumber = "TEST-001" (already exists)
- **Expected:** ❌ Error message displayed, document not created

### Test Case 3: Create Document without Master ID
- **Input:** documentNumber = "" (empty)
- **Expected:** ❌ Button disabled, validation message if bypassed

### Test Case 4: Case Sensitivity Check
- **Input:** Create "test-001", then try "TEST-001"
- **Expected:** Depends on database collation settings

## Future Enhancements (Optional)

1. **Real-time Validation**: Check Master ID availability as user types
2. **Auto-suggest**: Suggest next available Master ID based on pattern
3. **Snackbar Notifications**: Replace `alert()` with Material-UI Snackbar for better UX
4. **Master ID Generator**: Add a button to auto-generate unique Master IDs
5. **Duplicate Warning Dialog**: Show details of existing document with same Master ID

## Rollback Instructions

If these changes need to be reverted:

1. In `DocumentServiceImpl.java`, remove the `existsById()` check and restore the `orElseGet()` logic
2. In `DocumentManager.js`, remove the enhanced error handling and revert field labels
3. Redeploy both frontend and backend

## Notes

- The Master ID field is now **required** (enforced both frontend and backend)
- Existing documents are not affected by this change
- The validation applies only to new document creation, not updates
- Document versioning (revision/version numbers) is separate from Master ID validation


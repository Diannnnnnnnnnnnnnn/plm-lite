# Fix: Change Creator Showing "System User" Instead of Actual User

## Issue Description
In the Change Request detail dialog, the "Created By" field was showing "System User" instead of the actual logged-in username. This occurred because the code was incorrectly retrieving the user information from localStorage.

## Root Cause
The `ChangeManager.js` component was using:
```javascript
localStorage.getItem('username') || 'System User'
```

However, the user data is actually stored as a JSON object under the key `'user'`, not as a direct string under `'username'`. The correct approach is to:
1. Get the user object from `localStorage.getItem('user')`
2. Parse the JSON
3. Extract the `username` field

## Solution

### Changes Made
**File:** `frontend/src/components/Changes/ChangeManager.js`

#### 1. Added Helper Function (Lines 136-148)
Added a `getCurrentUsername()` helper function consistent with the pattern used in DocumentManager:

```javascript
// Helper function to get current logged-in username
const getCurrentUsername = () => {
  try {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const userData = JSON.parse(storedUser);
      return userData.username || 'Unknown User';
    }
  } catch (error) {
    console.error('Error getting current user:', error);
  }
  return 'Unknown User';
};
```

#### 2. Updated `handleReviewerSelection` Function (Line 444)
**Before:**
```javascript
const reviewData = {
  user: localStorage.getItem('username') || 'System User',
  reviewerIds: reviewerIds
};
```

**After:**
```javascript
const reviewData = {
  user: getCurrentUsername(),
  reviewerIds: reviewerIds
};
```

#### 3. Updated `handleCreateChange` Function (Line 499)
**Before:**
```javascript
const currentUser = localStorage.getItem('username') || 'System User';
```

**After:**
```javascript
const currentUser = getCurrentUsername();
```

## Benefits

1. **Accurate User Attribution**: Change requests now correctly show the actual user who created them
2. **Consistent Pattern**: Uses the same user retrieval pattern as DocumentManager
3. **Better Error Handling**: Includes try-catch for JSON parsing errors
4. **Fallback Value**: Returns 'Unknown User' instead of 'System User' when user data is unavailable, which is more accurate

## Testing

### Test Case 1: Create a New Change Request
1. Log in to the application
2. Navigate to the Changes section
3. Click "New Change" button
4. Fill in the form and create a change
5. Open the change details dialog

**Expected Result:**
- ✅ "Created By" field shows your actual username (e.g., "john_doe")
- ❌ Should NOT show "System User"

### Test Case 2: Submit Change for Review
1. Open an existing change in DRAFT status
2. Click "Submit for Review"
3. Select reviewers and submit

**Expected Result:**
- ✅ The review request is submitted with the correct username
- ✅ Change history/audit trail shows the correct user

### Test Case 3: View Existing Changes
1. Navigate to the Changes section
2. Click on any existing change to view details

**Expected Result:**
- ✅ "Created By" field shows the original creator's actual username
- ✅ All changes display with correct creator information

### Test Case 4: User Not Logged In (Edge Case)
1. Clear localStorage or access without proper login
2. Try to create a change

**Expected Result:**
- ⚠️ "Created By" field shows "Unknown User" (fallback behavior)
- ⚠️ No JavaScript errors in console

## Impact Areas

### 1. Change Creation
- New changes are now created with the correct username
- Backend receives accurate creator information

### 2. Change Review Workflow
- Review submissions now correctly identify the submitter
- Audit trail maintains accurate user information

### 3. Change Details Display
- Change detail dialog shows correct creator
- Change cards in list view show correct creator

### 4. Database Records
- Future changes will have accurate creator field
- Historical changes retain their original creator data

## Related Files

- `frontend/src/components/Changes/ChangeManager.js` - Main fix location
- `frontend/src/components/Documents/DocumentManager.js` - Reference pattern
- `change-service/` - Backend service (no changes needed)

## Data Consistency Note

**Important:** This fix only affects newly created changes going forward. Existing changes in the database that were created with "System User" will continue to show "System User" until they are edited or recreated.

To update existing changes (if needed):
1. Query the database for changes with creator = "System User"
2. Manually update them with the correct creator if known
3. Or accept them as historical data with limited attribution

## LocalStorage User Object Structure

For reference, the user object in localStorage follows this structure:
```javascript
{
  "username": "john_doe",
  "id": "user-123",
  "email": "john@example.com",
  // ... other user fields
}
```

This object is stored as a JSON string under the key `'user'` in localStorage.

## Verification Checklist

- ✅ Helper function `getCurrentUsername()` added
- ✅ `handleReviewerSelection` updated to use `getCurrentUsername()`
- ✅ `handleCreateChange` updated to use `getCurrentUsername()`
- ✅ No linter errors introduced
- ✅ Consistent with DocumentManager pattern
- ✅ Error handling included
- ✅ Fallback value provided

## Rollback Instructions

If this change needs to be reverted:

1. Remove the `getCurrentUsername()` helper function
2. Replace `getCurrentUsername()` calls with:
   ```javascript
   localStorage.getItem('username') || 'System User'
   ```
3. Refresh the frontend

However, this is not recommended as the fix addresses a genuine bug.

## Future Enhancements

1. **Global User Context**: Consider creating a React Context or custom hook for user management to avoid code duplication
2. **User Service**: Create a centralized user service that handles all user-related operations
3. **Authentication Check**: Add authentication validation before allowing change creation
4. **User Profile Integration**: Display full name and profile picture in addition to username

## Notes

- This fix aligns the Change Manager with the Document Manager implementation
- No backend changes were required
- The fix is backward compatible with existing data
- Testing should focus on newly created changes


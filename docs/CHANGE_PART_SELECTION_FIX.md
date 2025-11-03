# Fix: Change Management Part Selection Not Showing Parts

## Problem
When creating a change in the Change Management UI, clicking "Select Product or Part from BOM" showed an empty list, even though parts had been created in the system.

## Root Cause
The Change Manager was only loading **BOM headers** (old structure) using `bomService.getAllBoms()`, but not loading **standalone Parts** (new structure) using `bomService.getAllParts()`.

Since the system has migrated to a Part-centric architecture, most users create standalone Parts rather than BOM headers, so these parts weren't appearing in the selection dialog.

## Solution

### Changes Made to `frontend/src/components/Changes/ChangeManager.js`

#### 1. Added State for Parts
```javascript
const [availableParts, setAvailableParts] = useState([]);
```

#### 2. Updated `loadBOMs()` to Load Both BOMs and Parts
```javascript
const loadBOMs = async () => {
  try {
    setLoadingBOMs(true);
    // Load both BOMs (old structure) and Parts (new structure)
    const [boms, parts] = await Promise.all([
      bomService.getAllBoms().catch(() => []),
      bomService.getAllParts().catch(() => [])
    ]);
    setAvailableBOMs(boms);
    setAvailableParts(parts);
    console.log('Loaded BOMs:', boms);
    console.log('Loaded Parts:', parts);
  } catch (error) {
    console.error('Error loading BOMs/Parts:', error);
    setError('Failed to load BOMs and Parts');
  } finally {
    setLoadingBOMs(false);
  }
};
```

#### 3. Added `handleSelectPart()` Function
```javascript
const handleSelectPart = (part) => {
  setNewChange({
    ...newChange,
    product: part.id,
    selectedProductInfo: {
      id: part.id,
      description: part.description || part.title,
      partNumber: part.partNumber,
      type: 'Part',
      stage: part.stage
    }
  });
  setBomSelectionOpen(false);
  setBomSearchTerm('');
};
```

#### 4. Added `getFilteredParts()` Function
```javascript
const getFilteredParts = () => {
  if (!bomSearchTerm) return availableParts;
  return availableParts.filter(part =>
    (part.description && part.description.toLowerCase().includes(bomSearchTerm.toLowerCase())) ||
    (part.title && part.title.toLowerCase().includes(bomSearchTerm.toLowerCase())) ||
    (part.partNumber && part.partNumber.toLowerCase().includes(bomSearchTerm.toLowerCase())) ||
    (part.id && part.id.toLowerCase().includes(bomSearchTerm.toLowerCase())) ||
    (part.creator && part.creator.toLowerCase().includes(bomSearchTerm.toLowerCase()))
  );
};
```

#### 5. Updated Selection Dialog UI
The dialog now displays two sections:

**A. Standalone Parts Section** (shown first)
- Displays all standalone parts created in the system
- Shows part number, stage, status as chips
- Highlights in secondary color (different from BOMs)
- Clicking selects the part directly

**B. BOM Headers Section** (legacy)
- Displays old BOM headers with expandable items
- Only shown if BOMs exist
- Labeled as "Legacy" to indicate the old structure

```jsx
{/* Standalone Parts Section */}
{getFilteredParts().length > 0 && (
  <>
    <ListItem sx={{ bgcolor: 'grey.200', py: 1 }}>
      <ListItemText
        primary={<Typography variant="subtitle2" fontWeight="bold">Standalone Parts</Typography>}
        secondary={`${getFilteredParts().length} part(s) available`}
      />
    </ListItem>
    {getFilteredParts().map((part) => (
      <ListItem key={part.id} disablePadding>
        <ListItemButton onClick={() => handleSelectPart(part)}>
          {/* Part display UI */}
        </ListItemButton>
      </ListItem>
    ))}
  </>
)}

{/* BOMs Section (Legacy) */}
{getFilteredBOMs().length > 0 && (
  <>
    <ListItem sx={{ bgcolor: 'grey.200', py: 1, mt: 2 }}>
      <ListItemText
        primary={<Typography variant="subtitle2" fontWeight="bold">BOM Headers (Legacy)</Typography>}
        secondary={`${getFilteredBOMs().length} BOM(s) with parts`}
      />
    </ListItem>
    {/* Existing BOM display code */}
  </>
)}
```

## User Experience Improvements

1. **Standalone Parts Visible**: Users can now see and select the parts they've created
2. **Clear Organization**: Parts and BOMs are separated into distinct sections
3. **Visual Distinction**: 
   - Standalone parts use secondary color (orange/teal)
   - BOMs use primary color (blue)
4. **Comprehensive Search**: Search works across both parts and BOMs
5. **Empty State**: Shows helpful message if no parts or BOMs exist

## Testing

To verify the fix:

1. **Create a standalone part:**
   - Go to BOM Manager
   - Click "Create New Part"
   - Fill in details and save

2. **Create a change:**
   - Go to Change Management
   - Click "Create Change"
   - Click the "Select Product or Part from BOM" field
   - **Expected**: You should see your created part in the "Standalone Parts" section

3. **Select the part:**
   - Click on the part in the list
   - **Expected**: The part should be selected and displayed in the field
   - Click "Create" to save the change
   - **Expected**: Change should be created with `partIds` containing the selected part ID

## Related Files Modified

### Frontend
- `frontend/src/components/Changes/ChangeManager.js`
  - Added `availableParts` state
  - Updated `loadBOMs()` to load parts
  - Added `handleSelectPart()` function
  - Added `getFilteredParts()` function
  - Updated BOM selection dialog UI

### Backend (Previous Fix)
- Already handled in `CHANGE_BOM_PART_FIX.md`
- Backend correctly processes both `bomIds` and `partIds`

## Additional Notes

- The old BOM structure is still supported for backward compatibility
- The system prioritizes showing standalone parts (new structure) over BOMs (legacy)
- Parts within BOMs can still be selected by expanding the BOM
- All three selection methods send the correct data type to the backend:
  - Standalone Part → `partIds`
  - Part from BOM → `partIds` 
  - BOM Header → `bomIds`

## Deployment

No special deployment steps needed. Just refresh the frontend after deploying the updated code.

```bash
# If running dev server, it will auto-reload
# If not, restart the frontend:
cd frontend
npm start
```


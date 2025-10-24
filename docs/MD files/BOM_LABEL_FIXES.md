# BOM Label Fixes - "Document ID" → "BOM ID"

## ✅ Changes Completed

Fixed all instances where BOM-related fields were incorrectly labeled as "Document ID" instead of "BOM ID".

---

## 📝 Files Changed

### **1. BOMManager.js**

#### **Create BOM Dialog** (Line 917)
```javascript
// BEFORE:
label="Document ID"

// AFTER:
label="BOM ID"
```
- **Context**: Form field for entering BOM identifier when creating a new BOM
- **Field**: `newBOM.documentId`

#### **BOM Details Dialog** (Lines 637, 641)
```javascript
// BEFORE:
<Typography variant="body2" color="textSecondary">BOM ID</Typography>
<Typography variant="body1">{selectedNode.id}</Typography>
...
<Typography variant="body2" color="textSecondary">Document ID</Typography>
<Typography variant="body1">{selectedNode.documentId}</Typography>

// AFTER:
<Typography variant="body2" color="textSecondary">System ID</Typography>
<Typography variant="body1">{selectedNode.id}</Typography>
...
<Typography variant="body2" color="textSecondary">BOM ID</Typography>
<Typography variant="body1">{selectedNode.documentId}</Typography>
```
- **Context**: Detailed information display when viewing a BOM
- **Changes**: 
  - `id` field label changed from "BOM ID" → "System ID" (internal UUID)
  - `documentId` field label changed from "Document ID" → "BOM ID" (user-facing identifier)

---

### **2. DocumentManager.js**

#### **BOM List Display** (Lines 1094, 1097)
```javascript
// BEFORE:
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  BOM ID: {bom.id} • Creator: {bom.creator}
</Typography>
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  Document: {bom.documentId} • Created: {new Date(bom.createTime).toLocaleDateString()}
</Typography>

// AFTER:
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  BOM ID: {bom.documentId} • Creator: {bom.creator}
</Typography>
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  System ID: {bom.id} • Created: {new Date(bom.createTime).toLocaleDateString()}
</Typography>
```
- **Context**: BOM list displayed in the document drawer
- **Changes**: Swapped the display order and labels for clarity

---

### **3. ChangeManager.js**

#### **BOM List Display** (Lines 1262, 1265)
```javascript
// BEFORE:
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  BOM ID: {bom.id} • Creator: {bom.creator}
</Typography>
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  Document: {bom.documentId} • Created: {new Date(bom.createTime).toLocaleDateString()}
</Typography>

// AFTER:
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  BOM ID: {bom.documentId} • Creator: {bom.creator}
</Typography>
<Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
  System ID: {bom.id} • Created: {new Date(bom.createTime).toLocaleDateString()}
</Typography>
```
- **Context**: BOM list displayed in the change management drawer
- **Changes**: Swapped the display order and labels for clarity

---

## 🎯 Rationale

### **BOM Data Structure:**
```javascript
{
  id: "uuid-generated-system-id",      // Internal system identifier (UUID)
  documentId: "BOM-12345",              // User-facing BOM identifier
  description: "Motor Assembly BOM",
  stage: "PRODUCTION",
  status: "RELEASED",
  creator: "John Doe",
  createTime: "2024-01-15T08:00:00"
}
```

### **Field Usage:**
- **`id`**: Auto-generated UUID by the system, used for internal references and database relations
- **`documentId`**: User-entered identifier (e.g., "BOM-12345"), the main identifier visible to users

### **Why the Change:**
1. **User Confusion**: The field labeled "Document ID" was confusing because it's a BOM, not a document
2. **Consistency**: All BOM-related identifiers should be labeled as "BOM ID"
3. **Clarity**: Separating "System ID" (internal UUID) from "BOM ID" (user identifier) makes it clear which one users should reference

---

## ✅ Unchanged Items (Correct Labels)

The following instances of "Document ID" were **NOT changed** because they correctly refer to documents:

### **DocumentManager.js**
- Line 1442: Document details dialog showing actual document ID ✅
- Line 1458: Master Document ID for document version tracking ✅
- Line 1714: Document version history showing document ID ✅
- Line 2005: Version details dialog showing document ID ✅
- Line 2021: Master Document ID in version details ✅

### **TaskManager.js**
- Line 1205: Task details showing document ID for document review tasks ✅

### **ChangeManager.js**
- Multiple instances of `changeDocument` property (internal field name) ✅

---

## 📊 Summary Table

| File | Line(s) | Old Label | New Label | Field |
|------|---------|-----------|-----------|-------|
| BOMManager.js | 917 | Document ID | BOM ID | `newBOM.documentId` |
| BOMManager.js | 637 | BOM ID | System ID | `selectedNode.id` |
| BOMManager.js | 641 | Document ID | BOM ID | `selectedNode.documentId` |
| DocumentManager.js | 1094 | BOM ID (id) | BOM ID (documentId) | Swapped |
| DocumentManager.js | 1097 | Document (documentId) | System ID (id) | Swapped |
| ChangeManager.js | 1262 | BOM ID (id) | BOM ID (documentId) | Swapped |
| ChangeManager.js | 1265 | Document (documentId) | System ID (id) | Swapped |

---

## 🧪 Testing

### **How to Verify:**

1. **Create BOM Dialog:**
   - Navigate to BOM Management
   - Click "Create BOM"
   - Verify the field label says "BOM ID" (not "Document ID") ✅

2. **BOM Details Dialog:**
   - Click on any BOM to view details
   - Verify two ID fields:
     - "System ID": Shows UUID (e.g., "abc-123-def-456")
     - "BOM ID": Shows user identifier (e.g., "BOM-12345") ✅

3. **BOM in Document Drawer:**
   - Open Documents page
   - Open the document drawer (right side)
   - Click "BOMs" tab
   - Verify BOM list shows:
     - "BOM ID: BOM-12345 • Creator: John Doe"
     - "System ID: uuid... • Created: 1/15/2024" ✅

4. **BOM in Change Drawer:**
   - Open Changes page
   - Open the change drawer (right side)
   - Click "BOMs" tab
   - Verify BOM list shows same format as above ✅

---

## 🔧 Technical Notes

### **Backend Field Name (`documentId`)**
The backend uses `documentId` as the field name for BOM identifiers. This naming might be confusing but is maintained for backward compatibility. The frontend now correctly displays this as "BOM ID" to users.

### **Future Considerations**
If refactoring the backend in the future, consider renaming:
- `BOM.documentId` → `BOM.bomId` or `BOM.identifier`
- This would require database migration and API updates

For now, the frontend labels provide clarity without requiring backend changes.

---

*Last Updated: October 18, 2025*
*Files Modified: 3 (BOMManager.js, DocumentManager.js, ChangeManager.js)*
*Lines Changed: 7 total*


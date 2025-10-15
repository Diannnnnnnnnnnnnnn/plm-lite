import React, { useState, useEffect } from 'react';
import documentService from '../../services/documentService';
import ReviewerSelectionDialog from './ReviewerSelectionDialog';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  IconButton,
  Chip,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Menu,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  ListItemIcon,
  InputAdornment,
  CircularProgress,
  Tooltip,
  ToggleButton,
  ToggleButtonGroup
} from '@mui/material';
import bomService from '../../services/bomService';
import {
  Add as AddIcon,
  CloudUpload as UploadIcon,
  Download as DownloadIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  MoreVert as MoreVertIcon,
  Description as DocumentIcon,
  Image as ImageIcon,
  PictureAsPdf as PdfIcon,
  AccountTree as BOMIcon,
  Category as PartIcon,
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  Close as CloseIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Visibility as ViewIcon,
  PlayArrow as SubmitIcon,
  ViewList as ListViewIcon,
  Description as CardViewIcon
} from '@mui/icons-material';

const mockDocuments = [
  {
    id: 'DOC-001',
    title: 'Product Specification v2.1.pdf',
    stage: 'PRODUCTION',
    status: 'APPROVED',
    creator: 'John Doe',
    createTime: '2024-01-15T08:00:00',
    version: 2,
    revision: 1,
    fileKey: 'files/product-spec-v2.1.pdf',
    master: {
      id: 'MASTER-001',
      documentNumber: 'SPEC-001'
    }
  },
  {
    id: 'DOC-002',
    title: 'Technical Drawing TD-001.dwg',
    stage: 'DESIGN',
    status: 'DRAFT',
    creator: 'Jane Smith',
    createTime: '2024-01-14T08:00:00',
    version: 1,
    revision: 0,
    fileKey: 'files/technical-drawing-td-001.dwg',
    master: {
      id: 'MASTER-002',
      documentNumber: 'TD-001'
    }
  },
  {
    id: 'DOC-003',
    title: 'Assembly Instructions.docx',
    stage: 'REVIEW',
    status: 'IN_REVIEW',
    creator: 'Mike Johnson',
    createTime: '2024-01-13T08:00:00',
    version: 1,
    revision: 5,
    fileKey: 'files/assembly-instructions.docx',
    master: {
      id: 'MASTER-003',
      documentNumber: 'INST-001'
    }
  }
];

const getFileIcon = (type) => {
  switch (type.toLowerCase()) {
    case 'pdf': return <PdfIcon color="error" />;
    case 'image': case 'jpg': case 'png': return <ImageIcon color="primary" />;
    default: return <DocumentIcon color="action" />;
  }
};

const getStatusColor = (status) => {
  switch (status) {
    case 'APPROVED': return 'success';
    case 'IN_REVIEW': return 'warning';
    case 'DRAFT': return 'default';
    case 'OBSOLETE': return 'error';
    default: return 'default';
  }
};

const getStageColor = (stage) => {
  switch (stage) {
    case 'MANUFACTURING':
    case 'PRODUCTION':
    case 'IN_SERVICE': return 'success';
    case 'DETAILED_DESIGN':
    case 'REVIEW': return 'warning';
    case 'CONCEPTUAL_DESIGN':
    case 'DESIGN': return 'info';
    case 'PRELIMINARY_DESIGN':
    case 'DEVELOPMENT': return 'primary';
    case 'RETIRED': return 'error';
    default: return 'default';
  }
};

export default function DocumentManager() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');
  const [filterStage, setFilterStage] = useState('All');
  const [currentTab, setCurrentTab] = useState(0);
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [filteredDocuments, setFilteredDocuments] = useState([]);
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [documentDetailsOpen, setDocumentDetailsOpen] = useState(false);
  const [selectedDocumentForDetails, setSelectedDocumentForDetails] = useState(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingDocument, setEditingDocument] = useState(null);
  const [editFormData, setEditFormData] = useState({
    title: '',
    stage: '',
    status: '',
    description: ''
  });
  const [reviewerDialogOpen, setReviewerDialogOpen] = useState(false);
  const [documentToReview, setDocumentToReview] = useState(null);

  // Load documents from API
  const loadDocuments = async () => {
    try {
      setLoading(true);
      const fetchedDocuments = await documentService.getAllDocuments();
      console.log('Loaded documents from API:', fetchedDocuments);
      setDocuments(fetchedDocuments);
    } catch (error) {
      console.error('Error loading documents:', error);
      // Fallback to empty array on error
      setDocuments([]);
    } finally {
      setLoading(false);
    }
  };

  // Load documents on component mount
  useEffect(() => {
    loadDocuments();
  }, []);

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Filter documents
  useEffect(() => {
    let filtered = documents;

    // Filter by status
    if (filterStatus !== 'All') {
      filtered = filtered.filter(doc => doc.status === filterStatus);
    }

    // Filter by stage
    if (filterStage !== 'All') {
      filtered = filtered.filter(doc => doc.stage === filterStage);
    }

    // Filter by search term
    if (debouncedSearchTerm) {
      const searchLower = debouncedSearchTerm.toLowerCase();
      filtered = filtered.filter(doc =>
        doc.title.toLowerCase().includes(searchLower) ||
        doc.creator.toLowerCase().includes(searchLower) ||
        (doc.master && doc.master.documentNumber.toLowerCase().includes(searchLower)) ||
        doc.stage.toLowerCase().includes(searchLower) ||
        doc.status.toLowerCase().includes(searchLower)
      );
    }

    setFilteredDocuments(filtered);
  }, [documents, filterStatus, filterStage, debouncedSearchTerm]);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [bomSelectionOpen, setBomSelectionOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [availableBOMs, setAvailableBOMs] = useState([]);
  const [loadingBOMs, setLoadingBOMs] = useState(false);
  const [bomSearchTerm, setBomSearchTerm] = useState('');
  const [expandedBOMs, setExpandedBOMs] = useState(new Set());
  const [newDocument, setNewDocument] = useState({
    title: '',
    documentNumber: '',
    description: '',
    stage: 'CONCEPTUAL_DESIGN',
    relatedProduct: '',
    selectedProductInfo: null
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragOver, setIsDragOver] = useState(false);

  const handleMenuClick = (event, document) => {
    setAnchorEl(event.currentTarget);
    setSelectedDocument(document);
  };

  const handleDocumentClick = (document) => {
    setSelectedDocumentForDetails(document);
    setDocumentDetailsOpen(true);
  };

  const handleViewDocument = (document) => {
    handleDocumentClick(document);
    setAnchorEl(null);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedDocument(null);
  };

  const handleDeleteDocument = async () => {
    if (!selectedDocument) return;

    if (window.confirm(`Are you sure you want to delete "${selectedDocument.title}"?`)) {
      try {
        await documentService.deleteDocument(selectedDocument.id);
        // Refresh the document list
        const updatedDocuments = await documentService.getAllDocuments();
        setDocuments(updatedDocuments);
        setAnchorEl(null);
        setSelectedDocument(null);
        console.log('Document deleted successfully');
      } catch (error) {
        console.error('Error deleting document:', error);
        alert('Failed to delete document: ' + error.message);
      }
    }
  };

  const handleDeleteDocumentFromDetails = async () => {
    if (!selectedDocumentForDetails) return;

    if (window.confirm(`Are you sure you want to delete "${selectedDocumentForDetails.title}"?`)) {
      try {
        await documentService.deleteDocument(selectedDocumentForDetails.id);
        // Refresh the document list
        const updatedDocuments = await documentService.getAllDocuments();
        setDocuments(updatedDocuments);
        setDocumentDetailsOpen(false);
        setSelectedDocumentForDetails(null);
        console.log('Document deleted successfully');
      } catch (error) {
        console.error('Error deleting document:', error);
        alert('Failed to delete document: ' + error.message);
      }
    }
  };

  const handleUploadDialog = () => {
    setUploadDialogOpen(true);
  };

  const handleFileSelect = (file) => {
    setSelectedFile(file);
    if (file && !newDocument.title) {
      // Auto-fill title from filename
      const nameWithoutExtension = file.name.replace(/\.[^/.]+$/, "");
      setNewDocument({...newDocument, title: nameWithoutExtension});
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      handleFileSelect(files[0]); // Take the first file
    }
  };

  const handleFileInputChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleBrowseClick = () => {
    const fileInput = document.getElementById('file-upload-input');
    if (fileInput) {
      fileInput.click();
    }
  };

  const loadBOMs = async () => {
    try {
      setLoadingBOMs(true);
      const boms = await bomService.getAllBoms();
      setAvailableBOMs(boms);
    } catch (error) {
      console.error('Error loading BOMs:', error);
    } finally {
      setLoadingBOMs(false);
    }
  };

  const handleBOMSelection = () => {
    loadBOMs();
    setBomSelectionOpen(true);
  };

  const handleSelectBOM = (bom) => {
    setNewDocument({
      ...newDocument,
      relatedProduct: bom.id,
      selectedProductInfo: {
        id: bom.id,
        description: bom.description,
        stage: bom.stage,
        type: 'BOM',
        bomId: bom.id  // Store BOM ID
      }
    });
    setBomSelectionOpen(false);
    setBomSearchTerm('');
  };

  const handleSelectBOMItem = (item, bomId, bomDescription) => {
    setNewDocument({
      ...newDocument,
      relatedProduct: `${bomId}-${item.id}`,
      selectedProductInfo: {
        id: `${bomId}-${item.id}`,
        description: `${item.description} (from ${bomDescription})`,
        partNumber: item.partNumber,
        type: 'Part',
        bomId: bomId  // Store the actual BOM ID separately
      }
    });
    setBomSelectionOpen(false);
    setBomSearchTerm('');
  };

  const clearSelectedProduct = () => {
    setNewDocument({
      ...newDocument,
      relatedProduct: '',
      selectedProductInfo: null
    });
  };

  const toggleBOMExpansion = (bomId) => {
    const newExpanded = new Set(expandedBOMs);
    if (newExpanded.has(bomId)) {
      newExpanded.delete(bomId);
    } else {
      newExpanded.add(bomId);
    }
    setExpandedBOMs(newExpanded);
  };

  const getFilteredBOMs = () => {
    if (!bomSearchTerm) return availableBOMs;
    return availableBOMs.filter(bom =>
      bom.description.toLowerCase().includes(bomSearchTerm.toLowerCase()) ||
      bom.id.toLowerCase().includes(bomSearchTerm.toLowerCase()) ||
      bom.creator.toLowerCase().includes(bomSearchTerm.toLowerCase()) ||
      (bom.items && bom.items.some(item =>
        item.description.toLowerCase().includes(bomSearchTerm.toLowerCase()) ||
        item.partNumber.toLowerCase().includes(bomSearchTerm.toLowerCase())
      ))
    );
  };

  const handleCreateDocument = async () => {
    console.log('handleCreateDocument called');
    console.log('selectedFile:', selectedFile);
    console.log('newDocument:', newDocument);

    if (!selectedFile) {
      alert('Please select a file to upload');
      return;
    }

    if (!newDocument.title) {
      alert('Please provide a document title');
      return;
    }

    try {
      // Prepare document data for creation
      const documentData = {
        title: newDocument.title,
        description: newDocument.description,
        stage: newDocument.stage,
        documentNumber: newDocument.documentNumber || undefined,
        relatedProduct: newDocument.selectedProductInfo?.bomId || undefined  // Use bomId instead of id
      };

      console.log('Creating document with data:', {
        file: selectedFile.name,
        ...documentData
      });

      alert('Starting upload...');
      const response = await documentService.uploadDocument(documentData, selectedFile, 'Current User');
      console.log('Document uploaded successfully:', response);
      alert('Upload successful!');

      // Create new document object for the list using backend response
      const newDocumentObj = {
        ...response.document,
        fileName: selectedFile.name,
        fileSize: selectedFile.size,
        fileKey: response.fileKey,
        relatedProduct: newDocument.selectedProductInfo ? {
          id: newDocument.selectedProductInfo.id,
          description: newDocument.selectedProductInfo.description,
          type: newDocument.selectedProductInfo.type
        } : null
      };

      // Reload documents from API to get the latest data
      await loadDocuments();

      // Show success message
      console.log(`Document "${newDocument.title}" uploaded successfully!`);
      alert(`Document "${newDocument.title}" uploaded successfully!`);
      // TODO: Replace with proper toast notification when available

      // Reset form
      setUploadDialogOpen(false);
      setSelectedFile(null);
      setNewDocument({
        title: '',
        documentNumber: '',
        description: '',
        stage: 'CONCEPTUAL_DESIGN',
        relatedProduct: '',
        selectedProductInfo: null
      });
      setExpandedBOMs(new Set());

    } catch (error) {
      console.error('Error uploading document:', error);
      console.error('Error details:', error.response?.data || error.message);
      alert(`Failed to upload document: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleSubmitForReview = async (documentId) => {
    setDocumentToReview(documentId);
    setReviewerDialogOpen(true);
  };

  const handleReviewerSelection = async (reviewerIds) => {
    try {
      const reviewData = {
        user: 'current-user',
        reviewerIds: reviewerIds
      };

      console.log('Submitting document for review:', documentToReview, 'with reviewers:', reviewerIds);
      await documentService.submitForReview(documentToReview, reviewData);

      await loadDocuments();
      setDocumentDetailsOpen(false);
      alert('Document submitted for review successfully');
    } catch (error) {
      console.error('Error submitting document for review:', error);
      alert(`Failed to submit document for review: ${error.response?.data?.message || error.message}`);
    }
  };

  // Handle document download
  const handleDownload = async (documentItem) => {
    try {
      console.log('Downloading document:', documentItem.id);
      const blob = await documentService.downloadDocument(documentItem.id);

      // Create a download link and trigger it
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = documentItem.title || `document-${documentItem.id}`;
      document.body.appendChild(link);
      link.click();

      // Clean up
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      console.log('Download initiated for:', documentItem.title);
    } catch (error) {
      console.error('Failed to download document:', error);
      alert(`Failed to download document: ${error.response?.data?.message || error.message}`);
    }
  };

  // Handle document edit
  const handleEdit = (documentItem) => {
    console.log('Edit document:', documentItem.id);
    setEditingDocument(documentItem);
    setEditFormData({
      title: documentItem.title || '',
      stage: documentItem.stage || '',
      status: documentItem.status || '',
      description: documentItem.description || ''
    });
    setEditDialogOpen(true);
    setDocumentDetailsOpen(false);
  };

  // Handle edit form changes
  const handleEditFormChange = (field, value) => {
    setEditFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  // Handle edit form submission
  const handleEditSubmit = async () => {
    try {
      console.log('Updating document:', editingDocument.id, editFormData);

      // Call the update API - add user information
      const updateRequest = {
        ...editFormData,
        user: 'Current User' // TODO: Get from authentication context
      };
      const updatedDocument = await documentService.updateDocument(editingDocument.id, updateRequest);

      // Update the document in the local state
      setDocuments(prevDocs =>
        prevDocs.map(doc =>
          doc.id === editingDocument.id ? { ...doc, ...updatedDocument } : doc
        )
      );

      // Close the dialog and reset form
      setEditDialogOpen(false);
      setEditingDocument(null);
      setEditFormData({
        title: '',
        stage: '',
        status: '',
        description: ''
      });

      console.log('Document updated successfully');
    } catch (error) {
      console.error('Failed to update document:', error);
      alert(`Failed to update document: ${error.response?.data?.message || error.message}`);
    }
  };

  // Handle edit dialog close
  const handleEditDialogClose = () => {
    setEditDialogOpen(false);
    setEditingDocument(null);
    setEditFormData({
      title: '',
      stage: '',
      status: '',
      description: ''
    });
  };

  return (
    <Box>
      {/* Search and Filters */}
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} md={4}>
          <Box sx={{ display: 'flex', gap: 1, height: '40px', alignItems: 'center' }}>
            <TextField
              fullWidth
              placeholder="Search documents..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
                endAdornment: searchTerm && (
                  <IconButton
                    size="small"
                    onClick={() => setSearchTerm('')}
                    sx={{ mr: 1 }}
                  >
                    <CloseIcon fontSize="small" />
                  </IconButton>
                )
              }}
            />
            <Tooltip title="Filters">
              <ToggleButton
                value="filter"
                onClick={() => setFilterDialogOpen(true)}
                size="small"
                sx={{
                  width: '40px',
                  height: '40px',
                  flexShrink: 0,
                  border: '1px solid rgba(0, 0, 0, 0.23)',
                  '&:hover': {
                    border: '1px solid rgba(0, 0, 0, 0.87)',
                    backgroundColor: 'rgba(0, 0, 0, 0.04)'
                  }
                }}
              >
                <FilterIcon />
              </ToggleButton>
            </Tooltip>
          </Box>
        </Grid>
        <Grid item xs={12} md={5}>
        </Grid>
        <Grid item xs={12} md={1}>
          {/* View Toggle Buttons */}
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', height: '40px', alignItems: 'center' }}>
            <ToggleButtonGroup
              value={currentTab}
              exclusive
              onChange={(e, newValue) => newValue !== null && setCurrentTab(newValue)}
              aria-label="view mode"
              size="small"
            >
              <ToggleButton value={0} aria-label="card view">
                <Tooltip title="Card View">
                  <CardViewIcon />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value={1} aria-label="table view">
                <Tooltip title="Table View">
                  <ListViewIcon />
                </Tooltip>
              </ToggleButton>
            </ToggleButtonGroup>
          </Box>
        </Grid>
        <Grid item xs={12} md={2}>
          <Button
            fullWidth
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleUploadDialog}
            size="small"
            sx={{ height: '40px' }}
          >
            New Document
          </Button>
        </Grid>
      </Grid>

      {/* Loading State */}
      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" sx={{ py: 8 }}>
          <CircularProgress size={40} />
          <Typography variant="body1" sx={{ ml: 2 }}>
            Loading documents...
          </Typography>
        </Box>
      ) : (
        <>
          {/* Document Display - Scrollable Container */}
          <Box sx={{
            maxHeight: 'calc(100vh - 250px)',
            overflowY: 'auto',
            pr: 1,
            '&::-webkit-scrollbar': {
              width: '8px',
            },
            '&::-webkit-scrollbar-track': {
              backgroundColor: '#f1f1f1',
              borderRadius: '4px',
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: '#888',
              borderRadius: '4px',
              '&:hover': {
                backgroundColor: '#555',
              },
            },
          }}>
            {currentTab === 1 ? (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Document</TableCell>
                      <TableCell>Document Number</TableCell>
                      <TableCell>Stage</TableCell>
                      <TableCell>Creator</TableCell>
                      <TableCell>Version</TableCell>
                      <TableCell>Created</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredDocuments.map((document) => (
                      <TableRow
                        key={document.id}
                        hover
                        sx={{
                          cursor: 'pointer',
                          '&:hover': {
                            backgroundColor: 'action.hover'
                          }
                        }}
                        onClick={() => handleDocumentClick(document)}
                      >
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1}>
                            {getFileIcon(document.fileKey?.split('.').pop() || 'document')}
                            <Typography variant="body2">{document.title}</Typography>
                          </Box>
                        </TableCell>
                        <TableCell>{document.master?.documentNumber || 'N/A'}</TableCell>
                        <TableCell>
                          <Chip
                            label={document.stage}
                            color={getStageColor(document.stage)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>{document.creator}</TableCell>
                        <TableCell>v{document.revision}.{document.version}</TableCell>
                        <TableCell>{new Date(document.createTime).toLocaleDateString()}</TableCell>
                        <TableCell>
                          <Chip
                            label={document.status}
                            color={getStatusColor(document.status)}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleMenuClick(e, document);
                            }}
                          >
                            <MoreVertIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            ) : (
              <Grid container spacing={3}>
                {filteredDocuments.map((document) => (
                  <Grid item xs={12} md={6} lg={4} key={document.id}>
                    <Card
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        cursor: 'pointer',
                        '&:hover': {
                          boxShadow: 4,
                          transform: 'translateY(-2px)',
                          transition: 'all 0.2s ease-in-out'
                        }
                      }}
                      onClick={() => handleDocumentClick(document)}
                    >
                      <CardContent sx={{ flexGrow: 1 }}>
                        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                          <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold' }}>
                            {document.title}
                          </Typography>
                          <IconButton
                            size="small"
                            onClick={(e) => handleMenuClick(e, document)}
                          >
                            <MoreVertIcon />
                          </IconButton>
                        </Box>
                        <Typography variant="body2" color="textSecondary" gutterBottom>
                          {document.master?.documentNumber || 'No document number'}
                        </Typography>
                        <Box display="flex" gap={1} mb={2} flexWrap="wrap">
                          <Chip
                            label={document.status}
                            color={getStatusColor(document.status)}
                            size="small"
                          />
                          <Chip
                            label={document.stage}
                            color={getStageColor(document.stage)}
                            size="small"
                          />
                        </Box>
                        <Typography variant="body2" color="textSecondary" paragraph>
                          Version: v{document.revision}.{document.version}
                        </Typography>
                        <Box display="flex" justifyContent="space-between" alignItems="center">
                          <Typography variant="body2" color="textSecondary">
                            Created by: {document.creator}
                          </Typography>
                          <Typography variant="body2" color="textSecondary">
                            {new Date(document.createTime).toLocaleDateString()}
                          </Typography>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </Box>
        </>
      )}

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => handleViewDocument(selectedDocument)}>
          <ViewIcon sx={{ mr: 1 }} fontSize="small" />
          View Details
        </MenuItem>
        <MenuItem onClick={handleMenuClose}>
          <DownloadIcon sx={{ mr: 1 }} fontSize="small" />
          Download
        </MenuItem>
        <MenuItem onClick={handleMenuClose}>
          <EditIcon sx={{ mr: 1 }} fontSize="small" />
          Edit
        </MenuItem>
        <MenuItem onClick={handleDeleteDocument}>
          <DeleteIcon sx={{ mr: 1 }} fontSize="small" />
          Delete
        </MenuItem>
      </Menu>

      {/* BOM/Product Selection Dialog */}
      <Dialog
        open={bomSelectionOpen}
        onClose={() => setBomSelectionOpen(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <BOMIcon />
            Select Related Product or Part from BOM
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 2 }}>
            <TextField
              fullWidth
              placeholder="Search BOMs, products, or parts..."
              variant="outlined"
              value={bomSearchTerm}
              onChange={(e) => setBomSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
            />
          </Box>

          {loadingBOMs ? (
            <Box display="flex" justifyContent="center" py={4}>
              <CircularProgress />
            </Box>
          ) : (
            <Paper sx={{ maxHeight: 500, overflow: 'auto' }}>
              <List>
                {getFilteredBOMs().length === 0 ? (
                  <ListItem>
                    <ListItemText
                      primary="No BOMs found"
                      secondary="Try adjusting your search terms"
                    />
                  </ListItem>
                ) : (
                  getFilteredBOMs().map((bom) => (
                    <Box key={bom.id}>
                      {/* BOM Header */}
                      <ListItem disablePadding>
                        <ListItemButton
                          onClick={() => handleSelectBOM(bom)}
                          sx={{
                            bgcolor: 'primary.light',
                            color: 'primary.contrastText',
                            '&:hover': { bgcolor: 'primary.main' }
                          }}
                        >
                          <ListItemIcon>
                            <BOMIcon sx={{ color: 'primary.contrastText' }} />
                          </ListItemIcon>
                          <ListItemText
                            primary={
                              <Box>
                                <Typography variant="subtitle1" component="div" sx={{ fontWeight: 'bold' }}>
                                  {bom.description}
                                </Typography>
                                <Box display="flex" gap={1} mt={0.5}>
                                  <Chip
                                    label={bom.stage}
                                    size="small"
                                    sx={{ bgcolor: 'primary.dark', color: 'white' }}
                                  />
                                  <Chip
                                    label={bom.status}
                                    size="small"
                                    sx={{ bgcolor: 'primary.dark', color: 'white' }}
                                  />
                                </Box>
                              </Box>
                            }
                            secondary={
                              <Box sx={{ mt: 1 }}>
                                <Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
                                  BOM ID: {bom.id} • Creator: {bom.creator}
                                </Typography>
                                <Typography variant="body2" sx={{ color: 'primary.contrastText', opacity: 0.8 }}>
                                  Document: {bom.documentId} • Created: {new Date(bom.createTime).toLocaleDateString()}
                                </Typography>
                              </Box>
                            }
                          />
                          <IconButton
                            onClick={(e) => {
                              e.stopPropagation();
                              toggleBOMExpansion(bom.id);
                            }}
                            sx={{ color: 'primary.contrastText' }}
                          >
                            {expandedBOMs.has(bom.id) ? <ExpandMoreIcon /> : <ChevronRightIcon />}
                          </IconButton>
                        </ListItemButton>
                      </ListItem>

                      {/* BOM Items (Parts) */}
                      {expandedBOMs.has(bom.id) && bom.items && bom.items.length > 0 && (
                        <Box sx={{ pl: 4, bgcolor: 'grey.50' }}>
                          {bom.items.map((item) => (
                            <ListItem key={item.id} disablePadding>
                              <ListItemButton
                                onClick={() => handleSelectBOMItem(item, bom.id, bom.description)}
                                sx={{
                                  py: 1,
                                  '&:hover': { bgcolor: 'grey.200' }
                                }}
                              >
                                <ListItemIcon>
                                  <PartIcon color="secondary" />
                                </ListItemIcon>
                                <ListItemText
                                  primary={
                                    <Box>
                                      <Typography variant="body1" component="div">
                                        {item.description}
                                      </Typography>
                                      <Typography variant="body2" color="textSecondary">
                                        Part Number: {item.partNumber}
                                      </Typography>
                                    </Box>
                                  }
                                  secondary={
                                    <Box sx={{ mt: 0.5 }}>
                                      <Typography variant="body2" color="textSecondary">
                                        Quantity: {item.quantity} {item.unit} • Reference: {item.reference}
                                      </Typography>
                                    </Box>
                                  }
                                />
                              </ListItemButton>
                            </ListItem>
                          ))}
                        </Box>
                      )}

                      {/* Show message if BOM has no items */}
                      {expandedBOMs.has(bom.id) && (!bom.items || bom.items.length === 0) && (
                        <Box sx={{ pl: 4, py: 2, bgcolor: 'grey.50' }}>
                          <Typography variant="body2" color="textSecondary" style={{ fontStyle: 'italic' }}>
                            No parts/items defined in this BOM
                          </Typography>
                        </Box>
                      )}
                    </Box>
                  ))
                )}
              </List>
            </Paper>
          )}

          <Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
            <Typography variant="body2" color="info.dark">
              <strong>Document Relationship Options:</strong>
              <br />
              • Select a <strong>BOM/Product</strong> (blue) if the document relates to the entire assembly
              <br />
              • Select a <strong>specific Part</strong> (white) if the document relates to a particular component
              <br />
              • This helps organize documents by their related products/parts in the BOM structure
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBomSelectionOpen(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>

      {/* Upload Dialog */}
      <Dialog open={uploadDialogOpen} onClose={() => setUploadDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Upload New Document</DialogTitle>
        <DialogContent>
          <Box
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={handleBrowseClick}
            sx={{
              border: `2px dashed ${isDragOver ? '#1976d2' : '#ccc'}`,
              borderRadius: 2,
              p: 4,
              textAlign: 'center',
              mb: 2,
              cursor: 'pointer',
              backgroundColor: isDragOver ? 'rgba(25, 118, 210, 0.04)' : 'transparent',
              transition: 'all 0.2s ease',
              '&:hover': {
                borderColor: 'primary.main',
                backgroundColor: 'rgba(25, 118, 210, 0.04)'
              }
            }}
          >
            <UploadIcon sx={{
              fontSize: 48,
              color: isDragOver ? 'primary.main' : 'text.secondary',
              mb: 2
            }} />
            <Typography variant="h6" gutterBottom color={isDragOver ? 'primary' : 'inherit'}>
              {selectedFile ? selectedFile.name : 'Drag and drop files here'}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {selectedFile ? `Size: ${(selectedFile.size / 1024 / 1024).toFixed(2)} MB` : 'or click to browse'}
            </Typography>
            <input
              id="file-upload-input"
              type="file"
              style={{ display: 'none' }}
              onChange={handleFileInputChange}
              accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.jpg,.jpeg,.png,.gif,.dwg,.step,.stp"
            />
          </Box>
          <TextField
            fullWidth
            label="Document Title"
            variant="outlined"
            value={newDocument.title}
            onChange={(e) => setNewDocument({...newDocument, title: e.target.value})}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Document Number"
            variant="outlined"
            value={newDocument.documentNumber}
            onChange={(e) => setNewDocument({...newDocument, documentNumber: e.target.value})}
            margin="normal"
            placeholder="e.g., SPEC-001, TD-001"
          />
          <TextField
            fullWidth
            label="Description"
            variant="outlined"
            value={newDocument.description}
            onChange={(e) => setNewDocument({...newDocument, description: e.target.value})}
            margin="normal"
            multiline
            rows={2}
            placeholder="Brief description of the document"
          />
          <TextField
            fullWidth
            label="Related Product/Part (from BOM)"
            value={newDocument.selectedProductInfo ?
              `${newDocument.selectedProductInfo.description} (${newDocument.selectedProductInfo.type})` : ''}
            placeholder="Click to select a product or part from BOM"
            onClick={handleBOMSelection}
            InputProps={{
              readOnly: true,
              startAdornment: (
                <InputAdornment position="start">
                  {newDocument.selectedProductInfo?.type === 'Part' ? <PartIcon /> : <BOMIcon />}
                </InputAdornment>
              ),
              endAdornment: newDocument.selectedProductInfo && (
                <InputAdornment position="end">
                  <IconButton
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      clearSelectedProduct();
                    }}
                  >
                    <CloseIcon />
                  </IconButton>
                </InputAdornment>
              )
            }}
            sx={{ cursor: 'pointer' }}
            variant="outlined"
            margin="normal"
          />
          <FormControl fullWidth variant="outlined" margin="normal">
            <InputLabel>Stage</InputLabel>
            <Select
              value={newDocument.stage}
              onChange={(e) => setNewDocument({...newDocument, stage: e.target.value})}
              label="Stage"
            >
              <MenuItem value="CONCEPTUAL_DESIGN">Conceptual Design</MenuItem>
              <MenuItem value="PRELIMINARY_DESIGN">Preliminary Design</MenuItem>
              <MenuItem value="DETAILED_DESIGN">Detailed Design</MenuItem>
              <MenuItem value="MANUFACTURING">Manufacturing</MenuItem>
              <MenuItem value="IN_SERVICE">In Service</MenuItem>
              <MenuItem value="RETIRED">Retired</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setUploadDialogOpen(false);
            setSelectedFile(null);
            setNewDocument({
              title: '',
              documentNumber: '',
              description: '',
              stage: 'DESIGN',
              relatedProduct: '',
              selectedProductInfo: null
            });
          }}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreateDocument}
            disabled={!selectedFile || !newDocument.title}
          >
            Upload Document
          </Button>
        </DialogActions>
      </Dialog>

      {/* Filter Dialog */}
      <Dialog open={filterDialogOpen} onClose={() => setFilterDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Filter Documents</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                label="Status"
              >
                <MenuItem value="All">All Statuses</MenuItem>
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="ARCHIVED">Archived</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Stage</InputLabel>
              <Select
                value={filterStage}
                onChange={(e) => setFilterStage(e.target.value)}
                label="Stage"
              >
                <MenuItem value="All">All Stages</MenuItem>
                <MenuItem value="CONCEPTUAL_DESIGN">Conceptual Design</MenuItem>
                <MenuItem value="PRELIMINARY_DESIGN">Preliminary Design</MenuItem>
                <MenuItem value="DETAILED_DESIGN">Detailed Design</MenuItem>
                <MenuItem value="MANUFACTURING">Manufacturing</MenuItem>
                <MenuItem value="IN_SERVICE">In Service</MenuItem>
                <MenuItem value="RETIRED">Retired</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setFilterStatus('All');
            setFilterStage('All');
          }}>
            Clear Filters
          </Button>
          <Button onClick={() => setFilterDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setFilterDialogOpen(false)}>
            Apply Filters
          </Button>
        </DialogActions>
      </Dialog>

      {/* Document Details Dialog */}
      <Dialog
        open={documentDetailsOpen}
        onClose={() => setDocumentDetailsOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box display="flex" alignItems="center" gap={1}>
              {selectedDocumentForDetails && getFileIcon(selectedDocumentForDetails.fileKey?.split('.').pop() || 'document')}
              <Typography variant="h5" component="div">
                Document Details
              </Typography>
            </Box>
            <IconButton
              onClick={() => setDocumentDetailsOpen(false)}
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          {selectedDocumentForDetails && (
            <Box sx={{ pt: 1 }}>
              {/* Document Header */}
              <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="h6" component="div" sx={{ mb: 1, fontWeight: 'bold' }}>
                  {selectedDocumentForDetails.title}
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Document Number: {selectedDocumentForDetails.master?.documentNumber || 'Not assigned'}
                </Typography>
                <Box display="flex" gap={1} flexWrap="wrap">
                  <Chip
                    label={selectedDocumentForDetails.status}
                    color={getStatusColor(selectedDocumentForDetails.status)}
                    size="small"
                  />
                  <Chip
                    label={selectedDocumentForDetails.stage}
                    color={getStageColor(selectedDocumentForDetails.stage)}
                    size="small"
                  />
                  <Chip
                    label={`v${selectedDocumentForDetails.revision}.${selectedDocumentForDetails.version}`}
                    variant="outlined"
                    size="small"
                  />
                </Box>
              </Box>

              {/* Document Information Grid */}
              <Grid container spacing={3}>
                {/* Basic Information */}
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, height: '100%' }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Basic Information
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Document ID
                        </Typography>
                        <Typography variant="body1">
                          {selectedDocumentForDetails.id}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          File Key
                        </Typography>
                        <Typography variant="body1" sx={{ fontFamily: 'monospace', fontSize: '0.9rem' }}>
                          {selectedDocumentForDetails.fileKey || 'Not specified'}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Master Document ID
                        </Typography>
                        <Typography variant="body1">
                          {selectedDocumentForDetails.master?.id || 'N/A'}
                        </Typography>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* Creation & Version Information */}
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, height: '100%' }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Version & Timeline
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Created By
                        </Typography>
                        <Typography variant="body1">
                          {selectedDocumentForDetails.creator}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Created Date
                        </Typography>
                        <Typography variant="body1">
                          {new Date(selectedDocumentForDetails.createTime).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Current Version
                        </Typography>
                        <Typography variant="body1">
                          Revision {selectedDocumentForDetails.revision}, Version {selectedDocumentForDetails.version}
                        </Typography>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* File Information */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      File Information
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {getFileIcon(selectedDocumentForDetails.fileKey?.split('.').pop() || 'document')}
                        <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                          {selectedDocumentForDetails.title}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="textSecondary">
                        ({selectedDocumentForDetails.fileKey?.split('.').pop()?.toUpperCase() || 'Unknown'} file)
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                      <Button
                        variant="contained"
                        startIcon={<DownloadIcon />}
                        size="small"
                        onClick={() => handleDownload(selectedDocumentForDetails)}
                      >
                        Download
                      </Button>
                      <Button
                        variant="outlined"
                        startIcon={<ViewIcon />}
                        size="small"
                      >
                        Preview
                      </Button>
                      <Button
                        variant="outlined"
                        startIcon={<EditIcon />}
                        size="small"
                        onClick={() => handleEdit(selectedDocumentForDetails)}
                      >
                        Edit
                      </Button>
                    </Box>
                  </Paper>
                </Grid>

                {/* Workflow Status */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Workflow Status
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="subtitle1">Current Stage</Typography>
                        <Chip
                          label={selectedDocumentForDetails.stage}
                          color={getStageColor(selectedDocumentForDetails.stage)}
                        />
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="subtitle1">Current Status</Typography>
                        <Chip
                          label={selectedDocumentForDetails.status}
                          color={getStatusColor(selectedDocumentForDetails.status)}
                        />
                      </Box>
                      {/* Workflow progression visualization */}
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                          Stage Progression
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                          {['CONCEPTUAL_DESIGN', 'PRELIMINARY_DESIGN', 'DETAILED_DESIGN', 'MANUFACTURING'].map((stage, index) => (
                            <Box key={stage} sx={{ display: 'flex', alignItems: 'center' }}>
                              <Chip
                                label={stage.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                                size="small"
                                variant={selectedDocumentForDetails.stage === stage ? 'filled' : 'outlined'}
                                color={selectedDocumentForDetails.stage === stage ? getStageColor(stage) : 'default'}
                              />
                              {index < 3 && (
                                <ChevronRightIcon sx={{ mx: 0.5, color: 'text.secondary' }} />
                              )}
                            </Box>
                          ))}
                        </Box>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {selectedDocumentForDetails?.status === 'IN_WORK' && (
            <Button
              variant="contained"
              startIcon={<SubmitIcon />}
              onClick={() => handleSubmitForReview(selectedDocumentForDetails?.id)}
              color="primary"
            >
              Submit for Review
            </Button>
          )}
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={() => handleDownload(selectedDocumentForDetails)}
          >
            Download
          </Button>
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            onClick={() => handleEdit(selectedDocumentForDetails)}
          >
            Edit
          </Button>
          <Button
            variant="outlined"
            color="error"
            startIcon={<DeleteIcon />}
            onClick={handleDeleteDocumentFromDetails}
          >
            Delete
          </Button>
          <Button onClick={() => setDocumentDetailsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Document Dialog */}
      <Dialog open={editDialogOpen} onClose={handleEditDialogClose} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Document</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="Title"
              value={editFormData.title}
              onChange={(e) => handleEditFormChange('title', e.target.value)}
              fullWidth
              variant="outlined"
            />

            <FormControl fullWidth>
              <InputLabel>Stage</InputLabel>
              <Select
                value={editFormData.stage}
                label="Stage"
                onChange={(e) => handleEditFormChange('stage', e.target.value)}
              >
                <MenuItem value="CONCEPTUAL_DESIGN">Conceptual Design</MenuItem>
                <MenuItem value="PRELIMINARY_DESIGN">Preliminary Design</MenuItem>
                <MenuItem value="DETAILED_DESIGN">Detailed Design</MenuItem>
                <MenuItem value="MANUFACTURING">Manufacturing</MenuItem>
                <MenuItem value="IN_SERVICE">In Service</MenuItem>
                <MenuItem value="RETIRED">Retired</MenuItem>
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Status</InputLabel>
              <Select
                value={editFormData.status}
                label="Status"
                onChange={(e) => handleEditFormChange('status', e.target.value)}
              >
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="IN_WORK">In Work</MenuItem>
                <MenuItem value="IN_REVIEW">In Review</MenuItem>
                <MenuItem value="APPROVED">Approved</MenuItem>
                <MenuItem value="RELEASED">Released</MenuItem>
                <MenuItem value="OBSOLETE">Obsolete</MenuItem>
              </Select>
            </FormControl>

            <TextField
              label="Description"
              value={editFormData.description}
              onChange={(e) => handleEditFormChange('description', e.target.value)}
              fullWidth
              multiline
              rows={3}
              variant="outlined"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleEditDialogClose}>Cancel</Button>
          <Button onClick={handleEditSubmit} variant="contained" color="primary">
            Save Changes
          </Button>
        </DialogActions>
      </Dialog>

      {/* Reviewer Selection Dialog */}
      <ReviewerSelectionDialog
        open={reviewerDialogOpen}
        onClose={() => setReviewerDialogOpen(false)}
        onSubmit={handleReviewerSelection}
        documentId={documentToReview}
      />
    </Box>
  );
}
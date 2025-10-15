import React, { useState, useEffect } from 'react';
import ReviewerSelectionDialog from '../Documents/ReviewerSelectionDialog';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  CircularProgress,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  ListItemIcon,
  InputAdornment,
  Tooltip,
  ToggleButton,
  ToggleButtonGroup
} from '@mui/material';
import documentService from '../../services/documentService';
import bomService from '../../services/bomService';
import changeService from '../../services/changeService';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  MoreVert as MoreVertIcon,
  PlayArrow as SubmitIcon,
  CheckCircle as ApproveIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Assignment as ChangeIcon,
  Timeline as WorkflowIcon,
  Description as DocumentIcon,
  AttachFile as AttachIcon,
  Close as CloseIcon,
  AccountTree as BOMIcon,
  Category as PartIcon,
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  ViewList as ListViewIcon
} from '@mui/icons-material';

const mockChanges = [
  {
    id: 'CHG-001',
    title: 'Motor Assembly Design Update',
    stage: 'DESIGN',
    changeClass: 'Major',
    product: 'Electric Motor v2.0',
    status: 'DRAFT',
    creator: 'John Doe',
    createTime: '2024-01-15T08:00:00',
    changeReason: 'Performance improvement based on customer feedback',
    changeDocument: 'DOC-001'
  },
  {
    id: 'CHG-002',
    title: 'PCB Layout Modification',
    stage: 'DEVELOPMENT',
    changeClass: 'Minor',
    product: 'Control Board v1.5',
    status: 'IN_REVIEW',
    creator: 'Jane Smith',
    createTime: '2024-01-14T09:30:00',
    changeReason: 'Component obsolescence replacement',
    changeDocument: 'DOC-002'
  },
  {
    id: 'CHG-003',
    title: 'Safety Protocol Enhancement',
    stage: 'PRODUCTION',
    changeClass: 'Critical',
    product: 'Safety System v3.0',
    status: 'APPROVED',
    creator: 'Mike Johnson',
    createTime: '2024-01-13T14:20:00',
    changeReason: 'Regulatory compliance update',
    changeDocument: 'DOC-003'
  }
];

const getStatusColor = (status) => {
  switch (status) {
    case 'DRAFT': return 'default';
    case 'IN_REVIEW': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'error';
    case 'IMPLEMENTED': return 'info';
    default: return 'default';
  }
};

const getStageColor = (stage) => {
  switch (stage) {
    case 'DESIGN': return 'info';
    case 'DEVELOPMENT': return 'primary';
    case 'PRODUCTION': return 'success';
    case 'OBSOLETE': return 'error';
    default: return 'default';
  }
};

const getClassColor = (changeClass) => {
  switch (changeClass) {
    case 'Critical': return 'error';
    case 'Major': return 'warning';
    case 'Minor': return 'success';
    default: return 'default';
  }
};

export default function ChangeManager() {
  const [changes, setChanges] = useState([]);
  const [filteredChanges, setFilteredChanges] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);

  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');
  const [filterStage, setFilterStage] = useState('All');
  const [filterClass, setFilterClass] = useState('All');
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);

  // Load changes on component mount
  useEffect(() => {
    loadChanges();
  }, []);

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const loadChanges = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await changeService.getAllChanges();
      console.log('Loaded changes from API:', data);
      setChanges(data);
    } catch (error) {
      console.error('Error loading changes:', error);
      setError('Failed to load changes. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [changeDetailsOpen, setChangeDetailsOpen] = useState(false);
  const [selectedChangeForDetails, setSelectedChangeForDetails] = useState(null);
  const [documentSelectionOpen, setDocumentSelectionOpen] = useState(false);
  const [bomSelectionOpen, setBomSelectionOpen] = useState(false);
  const [selectedChange, setSelectedChange] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);
  const [availableDocuments, setAvailableDocuments] = useState([]);
  const [availableBOMs, setAvailableBOMs] = useState([]);
  const [loadingDocuments, setLoadingDocuments] = useState(false);
  const [loadingBOMs, setLoadingBOMs] = useState(false);
  const [documentSearchTerm, setDocumentSearchTerm] = useState('');
  const [bomSearchTerm, setBomSearchTerm] = useState('');
  const [expandedBOMs, setExpandedBOMs] = useState(new Set());
  const [reviewerDialogOpen, setReviewerDialogOpen] = useState(false);
  const [changeToReview, setChangeToReview] = useState(null);

  const [newChange, setNewChange] = useState({
    title: '',
    changeClass: 'Minor',
    product: '',
    stage: 'CONCEPTUAL_DESIGN',
    changeReason: '',
    changeDocument: '',
    selectedDocumentInfo: null,
    selectedProductInfo: null
  });

  useEffect(() => {
    let filtered = changes;

    if (filterStatus !== 'All') {
      filtered = filtered.filter(change => change.status === filterStatus);
    }

    if (filterStage !== 'All') {
      filtered = filtered.filter(change => change.stage === filterStage);
    }

    if (filterClass !== 'All') {
      filtered = filtered.filter(change => change.changeClass === filterClass);
    }

    if (debouncedSearchTerm) {
      const searchLower = debouncedSearchTerm.toLowerCase();
      filtered = filtered.filter(change =>
        change.title.toLowerCase().includes(searchLower) ||
        change.product.toLowerCase().includes(searchLower) ||
        change.creator.toLowerCase().includes(searchLower) ||
        change.changeReason.toLowerCase().includes(searchLower) ||
        change.changeClass.toLowerCase().includes(searchLower) ||
        change.stage.toLowerCase().includes(searchLower) ||
        change.status.toLowerCase().includes(searchLower) ||
        change.id.toLowerCase().includes(searchLower)
      );
    }

    setFilteredChanges(filtered);
  }, [changes, filterStatus, filterStage, filterClass, debouncedSearchTerm]);

  const loadDocuments = async () => {
    try {
      setLoadingDocuments(true);
      const documents = await documentService.getAllDocuments();
      setAvailableDocuments(documents);
    } catch (error) {
      console.error('Error loading documents:', error);
      setError('Failed to load documents');
    } finally {
      setLoadingDocuments(false);
    }
  };

  const loadBOMs = async () => {
    try {
      setLoadingBOMs(true);
      const boms = await bomService.getAllBoms();
      setAvailableBOMs(boms);
    } catch (error) {
      console.error('Error loading BOMs:', error);
      setError('Failed to load BOMs');
    } finally {
      setLoadingBOMs(false);
    }
  };

  const handleDocumentSelection = () => {
    loadDocuments();
    setDocumentSelectionOpen(true);
  };

  const handleBOMSelection = () => {
    loadBOMs();
    setBomSelectionOpen(true);
  };

  const handleSelectDocument = (document) => {
    setNewChange({
      ...newChange,
      changeDocument: document.id,
      selectedDocumentInfo: {
        id: document.id,
        title: document.title,
        creator: document.creator
      }
    });
    setDocumentSelectionOpen(false);
    setDocumentSearchTerm('');
  };

  const handleSelectBOM = (bom) => {
    setNewChange({
      ...newChange,
      product: bom.id,
      selectedProductInfo: {
        id: bom.id,
        description: bom.description,
        stage: bom.stage,
        type: 'BOM'
      }
    });
    setBomSelectionOpen(false);
    setBomSearchTerm('');
  };

  const handleSelectBOMItem = (item, bomId, bomDescription) => {
    setNewChange({
      ...newChange,
      product: `${bomId}-${item.id}`,
      selectedProductInfo: {
        id: `${bomId}-${item.id}`,
        description: `${item.description} (from ${bomDescription})`,
        partNumber: item.partNumber,
        type: 'Part'
      }
    });
    setBomSelectionOpen(false);
    setBomSearchTerm('');
  };

  const clearSelectedDocument = () => {
    setNewChange({
      ...newChange,
      changeDocument: '',
      selectedDocumentInfo: null
    });
  };

  const clearSelectedProduct = () => {
    setNewChange({
      ...newChange,
      product: '',
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

  const getFilteredDocuments = () => {
    if (!documentSearchTerm) return availableDocuments;
    return availableDocuments.filter(doc =>
      doc.title.toLowerCase().includes(documentSearchTerm.toLowerCase()) ||
      doc.creator.toLowerCase().includes(documentSearchTerm.toLowerCase()) ||
      (doc.master && doc.master.documentNumber.toLowerCase().includes(documentSearchTerm.toLowerCase()))
    );
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

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  const handleMenuClick = (event, change) => {
    setAnchorEl(event.currentTarget);
    setSelectedChange(change);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedChange(null);
  };

  const handleChangeClick = async (change) => {
    // Fetch related BOM and document names
    let enrichedChange = { ...change };

    try {
      // Fetch BOM name if product ID exists
      if (change.product) {
        try {
          const bom = await bomService.getBomById(change.product);
          enrichedChange.productName = bom.description || bom.documentId;
        } catch (error) {
          console.error('Error fetching BOM:', error);
          enrichedChange.productName = change.product;
        }
      }

      // Fetch document name if changeDocument ID exists
      if (change.changeDocument) {
        try {
          const doc = await documentService.getDocumentById(change.changeDocument);
          enrichedChange.documentName = doc.title || doc.masterId;
        } catch (error) {
          console.error('Error fetching document:', error);
          enrichedChange.documentName = change.changeDocument;
        }
      }
    } catch (error) {
      console.error('Error enriching change data:', error);
    }

    setSelectedChangeForDetails(enrichedChange);
    setChangeDetailsOpen(true);
  };

  const handleViewChange = (change) => {
    handleChangeClick(change);
    handleMenuClose();
  };

  const handleSubmitForReview = async (changeId) => {
    setChangeToReview(changeId);
    setReviewerDialogOpen(true);
  };

  const handleReviewerSelection = async (reviewerIds) => {
    try {
      const reviewData = {
        user: localStorage.getItem('username') || 'System User',
        reviewerIds: reviewerIds
      };

      console.log('Submitting change for review:', changeToReview, 'with reviewers:', reviewerIds);
      await changeService.submitForReview(changeToReview, reviewData);

      await loadChanges();
      setChangeDetailsOpen(false);
      setReviewerDialogOpen(false);
      alert('Change submitted for review successfully');
    } catch (error) {
      console.error('Error submitting change for review:', error);
      alert(`Failed to submit change for review: ${error.response?.data?.message || error.message}`);
    }
  };

  const handleApproveChange = async (changeId) => {
    try {
      setLoading(true);
      console.log('Approving change:', changeId);
      await changeService.approveChange(changeId);
      await loadChanges(); // Reload to get updated status
      handleMenuClose();
    } catch (error) {
      console.error('Error approving change:', error);
      setError('Failed to approve change: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteChange = async (changeId) => {
    if (!window.confirm('Are you sure you want to delete this change? This action cannot be undone.')) {
      return;
    }
    try {
      setLoading(true);
      console.log('Deleting change:', changeId);
      // Add delete API call when backend implements it
      // await changeService.deleteChange(changeId);
      await loadChanges();
      setChangeDetailsOpen(false);
      handleMenuClose();
    } catch (error) {
      console.error('Error deleting change:', error);
      setError('Failed to delete change: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCreateChange = async () => {
    try {
      setLoading(true);
      const currentUser = localStorage.getItem('username') || 'System User';
      const changeData = {
        title: newChange.title,
        changeClass: newChange.changeClass,
        product: newChange.product,
        stage: newChange.stage,
        creator: currentUser,
        changeReason: newChange.changeReason,
        changeDocument: newChange.changeDocument,
        documentIds: newChange.changeDocument ? [newChange.changeDocument] : [],
        bomIds: newChange.product ? [newChange.product] : []
      };

      await changeService.createChange(changeData);
      await loadChanges(); // Reload to show new change
      setCreateDialogOpen(false);
      setNewChange({
        title: '',
        changeClass: 'Minor',
        product: '',
        stage: 'CONCEPTUAL_DESIGN',
        changeReason: '',
        changeDocument: '',
        selectedDocumentInfo: null,
        selectedProductInfo: null
      });
      setExpandedBOMs(new Set());
    } catch (error) {
      console.error('Error creating change:', error);
      setError('Failed to create change: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const renderChangeCard = (change) => (
    <Card
      key={change.id}
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
      onClick={() => handleChangeClick(change)}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold' }}>
            {change.title}
          </Typography>
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              handleMenuClick(e, change);
            }}
          >
            <MoreVertIcon />
          </IconButton>
        </Box>

        <Typography variant="body2" color="textSecondary" gutterBottom>
          {change.id} • {change.product}
        </Typography>

        <Box display="flex" gap={1} mb={2} flexWrap="wrap">
          <Chip
            label={change.status}
            color={getStatusColor(change.status)}
            size="small"
          />
          <Chip
            label={change.stage}
            color={getStageColor(change.stage)}
            size="small"
          />
          <Chip
            label={change.changeClass}
            color={getClassColor(change.changeClass)}
            size="small"
          />
        </Box>

        <Typography variant="body2" color="textSecondary" paragraph>
          {change.changeReason}
        </Typography>

        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="body2" color="textSecondary">
            Created by: {change.creator}
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {new Date(change.createTime).toLocaleDateString()}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );

  const renderChangeTable = () => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Change ID</TableCell>
            <TableCell>Title</TableCell>
            <TableCell>Product</TableCell>
            <TableCell>Class</TableCell>
            <TableCell>Stage</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Creator</TableCell>
            <TableCell>Created</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {filteredChanges.map((change) => (
            <TableRow
              key={change.id}
              hover
              sx={{
                cursor: 'pointer',
                '&:hover': {
                  backgroundColor: 'action.hover'
                }
              }}
              onClick={() => handleChangeClick(change)}
            >
              <TableCell>{change.id}</TableCell>
              <TableCell>{change.title}</TableCell>
              <TableCell>{change.product}</TableCell>
              <TableCell>
                <Chip
                  label={change.changeClass}
                  color={getClassColor(change.changeClass)}
                  size="small"
                />
              </TableCell>
              <TableCell>
                <Chip
                  label={change.stage}
                  color={getStageColor(change.stage)}
                  size="small"
                />
              </TableCell>
              <TableCell>
                <Chip
                  label={change.status}
                  color={getStatusColor(change.status)}
                  size="small"
                />
              </TableCell>
              <TableCell>{change.creator}</TableCell>
              <TableCell>{new Date(change.createTime).toLocaleDateString()}</TableCell>
              <TableCell>
                <IconButton
                  size="small"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleMenuClick(e, change);
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
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} md={4}>
          <Box sx={{ display: 'flex', gap: 1, height: '40px', alignItems: 'center' }}>
            <TextField
              fullWidth
              placeholder="Search changes..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
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
                  <ChangeIcon />
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
            onClick={() => setCreateDialogOpen(true)}
            size="small"
            sx={{ height: '40px' }}
          >
            New Change
          </Button>
        </Grid>
      </Grid>

      {/* Content Display */}
      {currentTab === 0 ? (
        <Box sx={{ maxHeight: 'calc(100vh - 250px)', overflowY: 'auto', pr: 1 }}>
          <Grid container spacing={3}>
            {filteredChanges.map((change) => (
              <Grid item xs={12} md={6} lg={4} key={change.id}>
                {renderChangeCard(change)}
              </Grid>
            ))}
          </Grid>
        </Box>
      ) : (
        <Box sx={{ maxHeight: 'calc(100vh - 250px)', overflowY: 'auto' }}>
          {renderChangeTable()}
        </Box>
      )}

      {/* Action Menu */}
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
        <MenuItem onClick={() => handleViewChange(selectedChange)}>
          <ViewIcon sx={{ mr: 1 }} fontSize="small" />
          View Details
        </MenuItem>
        {selectedChange?.status === 'DRAFT' && (
          <MenuItem onClick={() => handleSubmitForReview(selectedChange.id)}>
            <SubmitIcon sx={{ mr: 1 }} fontSize="small" />
            Submit for Review
          </MenuItem>
        )}
        {selectedChange?.status === 'IN_REVIEW' && (
          <MenuItem onClick={() => handleApproveChange(selectedChange.id)}>
            <ApproveIcon sx={{ mr: 1 }} fontSize="small" />
            Approve
          </MenuItem>
        )}
        <MenuItem onClick={handleMenuClose}>
          <EditIcon sx={{ mr: 1 }} fontSize="small" />
          Edit
        </MenuItem>
      </Menu>

      {/* Create Change Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Create New Change Request</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Change Title"
                  variant="outlined"
                  value={newChange.title}
                  onChange={(e) => setNewChange({...newChange, title: e.target.value})}
                />
              </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Product/Part (from BOM)"
                variant="outlined"
                value={newChange.selectedProductInfo ?
                  `${newChange.selectedProductInfo.description} (${newChange.selectedProductInfo.type})` : ''}
                placeholder="Click to select a product or part from BOM"
                onClick={handleBOMSelection}
                InputProps={{
                  readOnly: true,
                  startAdornment: (
                    <InputAdornment position="start">
                      {newChange.selectedProductInfo?.type === 'Part' ? <PartIcon /> : <BOMIcon />}
                    </InputAdornment>
                  ),
                  endAdornment: newChange.selectedProductInfo && (
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
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth variant="outlined">
                <InputLabel>Change Class</InputLabel>
                <Select
                  value={newChange.changeClass}
                  onChange={(e) => setNewChange({...newChange, changeClass: e.target.value})}
                  label="Change Class"
                >
                  <MenuItem value="Minor">Minor</MenuItem>
                  <MenuItem value="Major">Major</MenuItem>
                  <MenuItem value="Critical">Critical</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth variant="outlined">
                <InputLabel>Stage</InputLabel>
                <Select
                  value={newChange.stage}
                  onChange={(e) => setNewChange({...newChange, stage: e.target.value})}
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
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Change Document"
                variant="outlined"
                value={newChange.selectedDocumentInfo ? `${newChange.selectedDocumentInfo.title} (${newChange.selectedDocumentInfo.id})` : ''}
                placeholder="Click to select a document"
                onClick={handleDocumentSelection}
                InputProps={{
                  readOnly: true,
                  startAdornment: (
                    <InputAdornment position="start">
                      <DocumentIcon />
                    </InputAdornment>
                  ),
                  endAdornment: newChange.selectedDocumentInfo && (
                    <InputAdornment position="end">
                      <IconButton
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          clearSelectedDocument();
                        }}
                      >
                        <CloseIcon />
                      </IconButton>
                    </InputAdornment>
                  )
                }}
                sx={{ cursor: 'pointer' }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Change Reason"
                variant="outlined"
                multiline
                rows={3}
                value={newChange.changeReason}
                onChange={(e) => setNewChange({...newChange, changeReason: e.target.value})}
              />
            </Grid>
          </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateChange}>Create Change</Button>
        </DialogActions>
      </Dialog>

      {/* Change Details Dialog */}
      <Dialog
        open={changeDetailsOpen}
        onClose={() => setChangeDetailsOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box display="flex" alignItems="center" gap={1}>
              <ChangeIcon color="primary" />
              <Typography variant="h5" component="div">
                Change Request Details
              </Typography>
            </Box>
            <IconButton
              onClick={() => setChangeDetailsOpen(false)}
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          {selectedChangeForDetails && (
            <Box sx={{ pt: 1 }}>
              {/* Change Header */}
              <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="h6" component="div" sx={{ mb: 1, fontWeight: 'bold' }}>
                  {selectedChangeForDetails.title}
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Change ID: {selectedChangeForDetails.id} • Product: {selectedChangeForDetails.product}
                </Typography>
                <Box display="flex" gap={1} flexWrap="wrap">
                  <Chip
                    label={selectedChangeForDetails.status}
                    color={getStatusColor(selectedChangeForDetails.status)}
                    size="small"
                  />
                  <Chip
                    label={selectedChangeForDetails.stage}
                    color={getStageColor(selectedChangeForDetails.stage)}
                    size="small"
                  />
                  <Chip
                    label={selectedChangeForDetails.changeClass}
                    color={getClassColor(selectedChangeForDetails.changeClass)}
                    size="small"
                  />
                </Box>
              </Box>

              {/* Change Information Grid */}
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
                          Change ID
                        </Typography>
                        <Typography variant="body1">
                          {selectedChangeForDetails.id}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Related Product
                        </Typography>
                        <Typography variant="body1">
                          {selectedChangeForDetails.productName || selectedChangeForDetails.product || 'Not specified'}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Change Document
                        </Typography>
                        <Typography variant="body1">
                          {selectedChangeForDetails.documentName || selectedChangeForDetails.changeDocument || 'Not specified'}
                        </Typography>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* Timeline & Ownership */}
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, height: '100%' }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Timeline & Ownership
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Created By
                        </Typography>
                        <Typography variant="body1">
                          {selectedChangeForDetails.creator}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Created Date
                        </Typography>
                        <Typography variant="body1">
                          {new Date(selectedChangeForDetails.createTime).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Change Classification
                        </Typography>
                        <Chip
                          label={selectedChangeForDetails.changeClass}
                          color={getClassColor(selectedChangeForDetails.changeClass)}
                          size="small"
                        />
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* Change Description */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Change Description & Reason
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                        Change Reason
                      </Typography>
                      <Typography variant="body1" sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                        {selectedChangeForDetails.changeReason}
                      </Typography>
                    </Box>
                  </Paper>
                </Grid>

                {/* Workflow Status */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Workflow Status & Progress
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="subtitle1">Current Stage</Typography>
                        <Chip
                          label={selectedChangeForDetails.stage}
                          color={getStageColor(selectedChangeForDetails.stage)}
                        />
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="subtitle1">Current Status</Typography>
                        <Chip
                          label={selectedChangeForDetails.status}
                          color={getStatusColor(selectedChangeForDetails.status)}
                        />
                      </Box>
                      {/* Workflow progression visualization */}
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                          Status Progression
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                          {['DRAFT', 'IN_REVIEW', 'APPROVED', 'IMPLEMENTED'].map((status, index) => (
                            <Box key={status} sx={{ display: 'flex', alignItems: 'center' }}>
                              <Chip
                                label={status.replace('_', ' ')}
                                size="small"
                                variant={selectedChangeForDetails.status === status ? 'filled' : 'outlined'}
                                color={selectedChangeForDetails.status === status ? getStatusColor(status) : 'default'}
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
          {(selectedChangeForDetails?.status === 'DRAFT' || selectedChangeForDetails?.status === 'IN_WORK') && (
            <Button
              variant="contained"
              startIcon={<SubmitIcon />}
              onClick={() => handleSubmitForReview(selectedChangeForDetails.id)}
              color="primary"
            >
              Submit for Review
            </Button>
          )}
          {selectedChangeForDetails?.status === 'IN_REVIEW' && (
            <Button
              variant="contained"
              startIcon={<ApproveIcon />}
              onClick={() => {
                handleApproveChange(selectedChangeForDetails.id);
                setChangeDetailsOpen(false);
              }}
              color="success"
            >
              Approve Change
            </Button>
          )}
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            onClick={() => {
              console.log('Edit change:', selectedChangeForDetails.id);
              setChangeDetailsOpen(false);
            }}
          >
            Edit
          </Button>
          <Button
            variant="outlined"
            startIcon={<WorkflowIcon />}
            onClick={() => {
              console.log('View workflow history:', selectedChangeForDetails.id);
            }}
          >
            View History
          </Button>
          <Box sx={{ flexGrow: 1 }} />
          <Button
            variant="outlined"
            startIcon={<DeleteIcon />}
            onClick={() => handleDeleteChange(selectedChangeForDetails?.id)}
            color="error"
          >
            Delete
          </Button>
          <Button onClick={() => setChangeDetailsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

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
            Select Product or Part from BOM
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
              <strong>Selection Options:</strong>
              <br />
              • Click on a <strong>BOM header</strong> (blue) to select the entire product/assembly
              <br />
              • Click the expand arrow to view individual parts
              <br />
              • Click on a <strong>part item</strong> (white) to select a specific component
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBomSelectionOpen(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>

      {/* Document Selection Dialog */}
      <Dialog
        open={documentSelectionOpen}
        onClose={() => setDocumentSelectionOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <DocumentIcon />
            Select Document
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 2 }}>
            <TextField
              fullWidth
              placeholder="Search documents..."
              variant="outlined"
              value={documentSearchTerm}
              onChange={(e) => setDocumentSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
            />
          </Box>

          {loadingDocuments ? (
            <Box display="flex" justifyContent="center" py={4}>
              <CircularProgress />
            </Box>
          ) : (
            <Paper sx={{ maxHeight: 400, overflow: 'auto' }}>
              <List>
                {getFilteredDocuments().length === 0 ? (
                  <ListItem>
                    <ListItemText
                      primary="No documents found"
                      secondary="Try adjusting your search terms"
                    />
                  </ListItem>
                ) : (
                  getFilteredDocuments().map((document) => (
                    <ListItem key={document.id} disablePadding>
                      <ListItemButton onClick={() => handleSelectDocument(document)}>
                        <ListItemIcon>
                          <DocumentIcon color="primary" />
                        </ListItemIcon>
                        <ListItemText
                          primary={
                            <Box>
                              <Typography variant="subtitle1" component="div">
                                {document.title}
                              </Typography>
                              <Box display="flex" gap={1} mt={0.5}>
                                <Chip
                                  label={document.stage}
                                  size="small"
                                  color="primary"
                                  variant="outlined"
                                />
                                <Chip
                                  label={document.status}
                                  size="small"
                                  color="secondary"
                                  variant="outlined"
                                />
                              </Box>
                            </Box>
                          }
                          secondary={
                            <Box sx={{ mt: 1 }}>
                              <Typography variant="body2" color="textSecondary">
                                ID: {document.id} • Creator: {document.creator}
                              </Typography>
                              <Typography variant="body2" color="textSecondary">
                                Document Number: {document.master?.documentNumber || 'N/A'}
                              </Typography>
                              <Typography variant="body2" color="textSecondary">
                                Version: v{document.revision}.{document.version} •
                                Created: {new Date(document.createTime).toLocaleDateString()}
                              </Typography>
                            </Box>
                          }
                        />
                      </ListItemButton>
                    </ListItem>
                  ))
                )}
              </List>
            </Paper>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDocumentSelectionOpen(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>

      {/* Filter Dialog */}
      <Dialog open={filterDialogOpen} onClose={() => setFilterDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Filter Changes</DialogTitle>
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
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="IN_REVIEW">In Review</MenuItem>
                <MenuItem value="APPROVED">Approved</MenuItem>
                <MenuItem value="REJECTED">Rejected</MenuItem>
                <MenuItem value="IMPLEMENTED">Implemented</MenuItem>
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
                <MenuItem value="DESIGN">Design</MenuItem>
                <MenuItem value="DEVELOPMENT">Development</MenuItem>
                <MenuItem value="PRODUCTION">Production</MenuItem>
                <MenuItem value="OBSOLETE">Obsolete</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Class</InputLabel>
              <Select
                value={filterClass}
                onChange={(e) => setFilterClass(e.target.value)}
                label="Class"
              >
                <MenuItem value="All">All Classes</MenuItem>
                <MenuItem value="Critical">Critical</MenuItem>
                <MenuItem value="Major">Major</MenuItem>
                <MenuItem value="Minor">Minor</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setFilterStatus('All');
            setFilterStage('All');
            setFilterClass('All');
          }}>
            Clear Filters
          </Button>
          <Button onClick={() => setFilterDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setFilterDialogOpen(false)}>
            Apply Filters
          </Button>
        </DialogActions>
      </Dialog>

      {/* Reviewer Selection Dialog */}
      <ReviewerSelectionDialog
        open={reviewerDialogOpen}
        onClose={() => setReviewerDialogOpen(false)}
        onSubmit={handleReviewerSelection}
        documentId={changeToReview}
      />
    </Box>
  );
}
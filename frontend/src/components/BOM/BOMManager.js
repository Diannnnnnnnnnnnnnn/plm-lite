import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
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
  IconButton,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  Tooltip,
  ToggleButton,
  ToggleButtonGroup,
  Snackbar,
  Alert,
  CircularProgress,
  DialogContentText
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  AccountTree as TreeIcon,
  FilterList as FilterIcon,
  MoreVert as MoreVertIcon,
  Search as SearchIcon,
  Description as DocumentIcon,
  ViewList as ListViewIcon
} from '@mui/icons-material';
import TreeView from './TreeView';
import bomService from '../../services/bomService';
import documentService from '../../services/documentService';

const getStatusColor = (status) => {
  switch (status) {
    case 'ACTIVE': case 'RELEASED': case 'APPROVED': return 'success';
    case 'DRAFT': case 'IN_WORK': return 'warning';
    case 'OBSOLETE': return 'error';
    case 'IN_REVIEW': case 'IN_TECHNICAL_REVIEW': return 'info';
    default: return 'default';
  }
};

export default function BOMManager() {
  const [boms, setBOMs] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');
  const [filterStage, setFilterStage] = useState('All');
  const [currentTab, setCurrentTab] = useState(0);
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [filteredBOMs, setFilteredBOMs] = useState([]);
  const [detailsTab, setDetailsTab] = useState(0);
  const [contextMenu, setContextMenu] = useState(null);
  const [selectedParentBOM, setSelectedParentBOM] = useState(null);
  const [childPartsDialogOpen, setChildPartsDialogOpen] = useState(false);
  const [bomDocuments, setBomDocuments] = useState({});  // Map of bomId -> documents array
  const [documentDetailsOpen, setDocumentDetailsOpen] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [availableParts, setAvailableParts] = useState([]);
  const [selectedChildPart, setSelectedChildPart] = useState('');
  const [childPartQuantity, setChildPartQuantity] = useState(1);

  // Load Parts from API on component mount
  useEffect(() => {
    const loadParts = async () => {
      setLoading(true);
      setError(null);
      try {
        // Load all parts (which are the new BOMs)
        const partsData = await bomService.getAllParts();
        console.log('Loaded Parts from API:', partsData);
        // Parts already have hierarchy via childUsages, build tree
        const partHierarchy = buildPartHierarchy(partsData);
        setBOMs(partHierarchy);
      } catch (error) {
        console.error('Error loading Parts:', error);
        setError('Failed to load parts: ' + (error.response?.data?.message || error.message));
        setSnackbarMessage('Failed to load parts');
        setSnackbarOpen(true);
      } finally {
        setLoading(false);
      }
    };
    loadParts();
  }, []);

  // Helper function to build hierarchy from Parts
  const buildPartHierarchy = (parts) => {
    // Create a map of all parts by ID for quick lookup
    const partMap = {};
    parts.forEach(part => {
      partMap[part.id] = {
        ...part,
        documentId: part.id,
        description: part.title,
        children: []
      };
    });

    // Track which parts are children (to filter out from root level)
    const childPartIds = new Set();
    
    // Build relationships from childUsages
    parts.forEach(part => {
      if (part.childUsages && part.childUsages.length > 0) {
        part.childUsages.forEach(usage => {
          const childPart = partMap[usage.childPartId];
          if (childPart && partMap[part.id]) {
            // Add child with usage information
            partMap[part.id].children.push({
              ...childPart,
              quantity: usage.quantity,
              usageId: usage.id
            });
            childPartIds.add(usage.childPartId);
          }
        });
      }
    });

    // Return only root-level parts (those not used as children)
    return parts
      .filter(part => !childPartIds.has(part.id))
      .map(part => partMap[part.id]);
  };

  // Helper function to refresh parts data
  const refreshParts = async () => {
    try {
      setLoading(true);
      const updatedParts = await bomService.getAllParts();
      const partHierarchy = buildPartHierarchy(updatedParts);
      setBOMs(partHierarchy);
      return partHierarchy;
    } catch (error) {
      console.error('Error refreshing parts:', error);
      setSnackbarMessage('Failed to refresh parts');
      setSnackbarOpen(true);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  // Function to get related documents for a Part
  const getRelatedDocuments = (partId) => {
    return bomDocuments[partId] || [];
  };

  // Helper function to get flattened list of all BOMs for parent selection
  const getAllBOMs = (bomList, level = 0) => {
    let result = [];
    bomList.forEach(bom => {
      result.push({ ...bom, level });
      if (bom.children && bom.children.length > 0) {
        result = [...result, ...getAllBOMs(bom.children, level + 1)];
      }
    });
    return result;
  };
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const [newBOM, setNewBOM] = useState({
    title: '',
    description: '',
    creator: 'Current User',
    stage: 'DETAILED_DESIGN',
    status: 'IN_WORK',
    level: 'ASSEMBLY',
    parentPartId: null
  });
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingBOM, setEditingBOM] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingBOM, setDeletingBOM] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');

  useEffect(() => {
    let filtered = boms;

    // Filter by status
    if (filterStatus !== 'All') {
      filtered = filtered.filter(bom => bom.status === filterStatus);
    }

    // Filter by stage
    if (filterStage !== 'All') {
      filtered = filtered.filter(bom => bom.stage === filterStage);
    }

    // Filter by search term
    if (searchTerm) {
      filtered = filtered.filter(bom =>
        bom.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        bom.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
        bom.creator.toLowerCase().includes(searchTerm.toLowerCase()) ||
        bom.documentId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (bom.items && bom.items.some(item =>
          item.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
          item.partNumber.toLowerCase().includes(searchTerm.toLowerCase())
        ))
      );
    }

    setFilteredBOMs(filtered);
  }, [boms, filterStatus, filterStage, searchTerm]);

  // Load documents for a specific Part when needed
  useEffect(() => {
    const loadPartDocuments = async () => {
      if (selectedNode && detailsTab === 1 && selectedNode.id) {
        // Check if documents are already loaded
        if (bomDocuments[selectedNode.id]) return;

        try {
          const documents = await documentService.getDocumentsByBomId(selectedNode.id);
          setBomDocuments(prev => ({
            ...prev,
            [selectedNode.id]: documents
          }));
        } catch (error) {
          console.error('Error loading documents for Part:', error);
          setBomDocuments(prev => ({
            ...prev,
            [selectedNode.id]: []
          }));
        }
      }
    };
    
    loadPartDocuments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedNode, detailsTab]);

  const handleNodeSelect = (node) => {
    setSelectedNode(node);
  };



  const handleContextMenuClose = () => {
    setContextMenu(null);
  };

  const handleCreateChildBOM = (parentNode) => {
    setSelectedParentBOM(parentNode);
    setNewBOM({
      ...newBOM,
      parentPartId: parentNode.id
    });
    setCreateDialogOpen(true);
    handleContextMenuClose();
  };

  const handleAddChildPart = async (bomNode) => {
    setSelectedNode(bomNode);
    // Load all available parts for selection
    try {
      const allParts = await bomService.getAllParts();
      // Filter out the current part and its children to prevent circular references
      const filtered = allParts.filter(p => p.id !== bomNode.id);
      setAvailableParts(filtered);
    } catch (error) {
      console.error('Error loading parts:', error);
      setSnackbarMessage('Failed to load available parts');
      setSnackbarOpen(true);
    }
    setChildPartsDialogOpen(true);
    handleContextMenuClose();
  };

  const handleAddChildPartSubmit = async () => {
    try {
      if (!selectedChildPart) {
        setSnackbarMessage('Please select a child part');
        setSnackbarOpen(true);
        return;
      }

      setLoading(true);
      await bomService.addPartUsage(selectedNode.id, selectedChildPart, childPartQuantity);
      await refreshParts();

      // Update selected node
      const updatedPart = await bomService.getPartById(selectedNode.id);
      setSelectedNode({
        ...updatedPart,
        documentId: updatedPart.id,
        description: updatedPart.title
      });

      setChildPartsDialogOpen(false);
      setSelectedChildPart('');
      setChildPartQuantity(1);
      setSnackbarMessage('Child part added successfully!');
      setSnackbarOpen(true);
    } catch (error) {
      console.error('Error adding child part:', error);
      setSnackbarMessage('Failed to add child part: ' + (error.response?.data?.message || error.message));
      setSnackbarOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveChildPart = async (childPartId) => {
    try {
      setLoading(true);
      await bomService.removePartUsage(selectedNode.id, childPartId);
      await refreshParts();

      // Update selected node
      const updatedPart = await bomService.getPartById(selectedNode.id);
      setSelectedNode({
        ...updatedPart,
        documentId: updatedPart.id,
        description: updatedPart.title
      });

      setSnackbarMessage('Child part removed successfully!');
      setSnackbarOpen(true);
    } catch (error) {
      console.error('Error removing child part:', error);
      setSnackbarMessage('Failed to remove child part: ' + (error.response?.data?.message || error.message));
      setSnackbarOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const handleDocumentClick = (document) => {
    setSelectedDocument(document);
    setDocumentDetailsOpen(true);
  };

  const handleCreateBOM = async () => {
    try {
      setLoading(true);
      
      // Validate required fields
      if (!newBOM.title) {
        setSnackbarMessage('Title is required');
        setSnackbarOpen(true);
        return;
      }

      // Create Part (new BOM system)
      const partData = {
        title: newBOM.title,
        description: newBOM.description,
        creator: newBOM.creator,
        stage: newBOM.stage,
        status: newBOM.status,
        level: newBOM.level
      };

      const createdPart = await bomService.createPart(partData);
      console.log('Part created successfully:', createdPart);

      // If there's a parent, create the relationship
      if (selectedParentBOM && selectedParentBOM.id) {
        await bomService.addPartUsage(selectedParentBOM.id, createdPart.id, 1);
        console.log('Part relationship created');
      }

      // Refresh the Part list
      await refreshParts();

      setNewBOM({
        title: '',
        description: '',
        creator: 'Current User',
        stage: 'DETAILED_DESIGN',
        status: 'IN_WORK',
        level: 'ASSEMBLY',
        parentPartId: null
      });
      setSelectedParentBOM(null);
      setCreateDialogOpen(false);
      setSnackbarMessage('Part created successfully!');
      setSnackbarOpen(true);
    } catch (error) {
      console.error('Error creating Part:', error);
      setSnackbarMessage('Failed to create Part: ' + (error.response?.data?.message || error.message));
      setSnackbarOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const handleEditBOM = (bom) => {
    setEditingBOM({
      id: bom.id,
      title: bom.title || bom.description,
      description: bom.description,
      stage: bom.stage,
      status: bom.status,
      level: bom.level,
      creator: bom.creator
    });
    setEditDialogOpen(true);
  };

  const handleUpdateBOM = async () => {
    try {
      setLoading(true);
      
      if (!editingBOM.title) {
        setSnackbarMessage('Title is required');
        setSnackbarOpen(true);
        return;
      }

      const updateData = {
        title: editingBOM.title,
        description: editingBOM.description,
        stage: editingBOM.stage,
        status: editingBOM.status,
        level: editingBOM.level,
        creator: editingBOM.creator
      };

      await bomService.updatePart(editingBOM.id, updateData);
      await refreshParts();

      // Update selected node if it's the one being edited
      if (selectedNode && selectedNode.id === editingBOM.id) {
        const updatedPart = await bomService.getPartById(editingBOM.id);
        setSelectedNode({
          ...updatedPart,
          documentId: updatedPart.id,
          description: updatedPart.title
        });
      }

      setEditDialogOpen(false);
      setEditingBOM(null);
      setSnackbarMessage('Part updated successfully!');
      setSnackbarOpen(true);
    } catch (error) {
      console.error('Error updating Part:', error);
      setSnackbarMessage('Failed to update Part: ' + (error.response?.data?.message || error.message));
      setSnackbarOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBOM = (bom) => {
    setDeletingBOM(bom);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    try {
      setLoading(true);
      await bomService.deletePart(deletingBOM.id);
      await refreshParts();

      // Clear selected node if it's the one being deleted
      if (selectedNode && selectedNode.id === deletingBOM.id) {
        setSelectedNode(null);
      }

      setDeleteDialogOpen(false);
      setDeletingBOM(null);
      setSnackbarMessage('Part deleted successfully!');
      setSnackbarOpen(true);
    } catch (error) {
      console.error('Error deleting Part:', error);
      setSnackbarMessage('Failed to delete Part: ' + (error.response?.data?.message || error.message));
      setSnackbarOpen(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{
      '&::-webkit-scrollbar': {
        display: 'none'
      },
      scrollbarWidth: 'none'
    }}>
      {/* Search and Filters */}
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} md={4}>
          <Box sx={{ display: 'flex', gap: 1, height: '40px', alignItems: 'center' }}>
            <TextField
              fullWidth
              placeholder="Search BOMs..."
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
              <ToggleButton value={0} aria-label="tree view">
                <Tooltip title="Tree View">
                  <TreeIcon />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value={1} aria-label="list view">
                <Tooltip title="List View">
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
            New BOM
          </Button>
        </Grid>
      </Grid>

      {currentTab === 0 ? (
        <Grid container spacing={2} sx={{
          height: 'calc(100vh - 150px)',
          '&::-webkit-scrollbar': {
            display: 'none'
          },
          scrollbarWidth: 'none'
        }}>
        {/* Left Panel - Tree Structure */}
        <Grid item xs={12} md={4}>
          <Box sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            '&::-webkit-scrollbar': {
              display: 'none'
            },
            scrollbarWidth: 'none',
            overflow: 'hidden'
          }}>
            <TreeView
              data={filteredBOMs}
              onNodeSelect={handleNodeSelect}
              selectedNode={selectedNode}
            />
          </Box>
        </Grid>

        {/* Right Panel - BOM Details */}
        <Grid item xs={12} md={8}>
          <Box sx={{
            height: '100%',
            '&::-webkit-scrollbar': {
              display: 'none'
            },
            scrollbarWidth: 'none',
            overflow: 'hidden'
          }}>
            {selectedNode ? (
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
                      {selectedNode.description}
                    </Typography>
                    <Box display="flex" gap={1}>
                      <IconButton
                        size="small"
                        onClick={() => handleAddChildPart(selectedNode)}
                        title="Add Child Part"
                      >
                        <AddIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleCreateChildBOM(selectedNode)}
                        title="Add Child BOM"
                      >
                        <TreeIcon />
                      </IconButton>
                      <IconButton 
                        size="small" 
                        onClick={() => handleEditBOM(selectedNode)}
                        title="Edit Part"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton 
                        size="small" 
                        onClick={() => handleDeleteBOM(selectedNode)}
                        title="Delete Part"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  </Box>

                  <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid item xs={6} sm={4}>
                      <Typography variant="body2" color="textSecondary">BOM ID</Typography>
                      <Typography variant="body1">{selectedNode.documentId}</Typography>
                    </Grid>
                    <Grid item xs={6} sm={4}>
                      <Typography variant="body2" color="textSecondary">Stage</Typography>
                      <Typography variant="body1">{selectedNode.stage}</Typography>
                    </Grid>
                    <Grid item xs={6} sm={4}>
                      <Typography variant="body2" color="textSecondary">Created</Typography>
                      <Typography variant="body1">
                        {new Date(selectedNode.createTime).toLocaleDateString()}
                      </Typography>
                    </Grid>
                  </Grid>

                  <Box display="flex" gap={2} mb={3}>
                    <Chip
                      label={selectedNode.status}
                      color={getStatusColor(selectedNode.status)}
                      size="small"
                    />
                    <Chip
                      label={selectedNode.stage}
                      variant="outlined"
                      size="small"
                    />
                    <Typography variant="body2" color="textSecondary">
                      Creator: {selectedNode.creator}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      Updated: {new Date(selectedNode.updateTime).toLocaleDateString()}
                    </Typography>
                  </Box>

                  <Divider sx={{ mb: 2 }} />

                  {/* Items and Documents Tabs */}
                  <Tabs value={detailsTab} onChange={(e, newValue) => setDetailsTab(newValue)} sx={{ mb: 2 }}>
                    <Tab
                      label={
                        <Box display="flex" alignItems="center" gap={1}>
                          <TreeIcon />
                          Child Parts ({selectedNode.children?.length || 0})
                        </Box>
                      }
                    />
                    <Tab
                      label={
                        <Box display="flex" alignItems="center" gap={1}>
                          <DocumentIcon />
                          Documents
                        </Box>
                      }
                    />
                  </Tabs>

                  {/* Tab Content */}
                  {detailsTab === 0 && (
                    // Child Parts Tab
                    selectedNode.children && selectedNode.children.length > 0 ? (
                      <TableContainer component={Paper} variant="outlined">
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              <TableCell>Part ID</TableCell>
                              <TableCell>Title</TableCell>
                              <TableCell>Description</TableCell>
                              <TableCell>Quantity</TableCell>
                              <TableCell>Status</TableCell>
                              <TableCell>Actions</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {selectedNode.children.map((child) => (
                              <TableRow key={child.id}>
                                <TableCell>{child.id}</TableCell>
                                <TableCell>{child.title || child.description}</TableCell>
                                <TableCell>{child.description}</TableCell>
                                <TableCell>{child.quantity || 1}</TableCell>
                                <TableCell>
                                  <Chip
                                    label={child.status}
                                    color={getStatusColor(child.status)}
                                    size="small"
                                  />
                                </TableCell>
                                <TableCell>
                                  <IconButton
                                    size="small"
                                    onClick={() => handleRemoveChildPart(child.id)}
                                    title="Remove"
                                    color="error"
                                  >
                                    <DeleteIcon fontSize="small" />
                                  </IconButton>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    ) : (
                      <Box
                        sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          justifyContent: 'center',
                          height: '200px',
                          color: 'text.secondary'
                        }}
                      >
                        <TreeIcon sx={{ fontSize: 48, mb: 1, opacity: 0.3 }} />
                        <Typography variant="body2" textAlign="center">
                          No child parts defined for this part.
                        </Typography>
                        <Button
                          variant="outlined"
                          startIcon={<AddIcon />}
                          onClick={() => handleAddChildPart(selectedNode)}
                          sx={{ mt: 2 }}
                        >
                          Add Child Part
                        </Button>
                      </Box>
                    )
                  )}

                  {detailsTab === 1 && (
                    // Documents Tab
                    getRelatedDocuments(selectedNode.id).length > 0 ? (
                      <List sx={{ maxHeight: 400, overflow: 'auto' }}>
                        {getRelatedDocuments(selectedNode.id).map((document) => (
                          <ListItem key={document.id} disablePadding>
                            <ListItemButton
                              sx={{ mb: 1, border: 1, borderColor: 'divider', borderRadius: 1 }}
                              onClick={() => handleDocumentClick(document)}
                            >
                              <ListItemIcon>
                                <DocumentIcon color="primary" />
                              </ListItemIcon>
                              <ListItemText
                                primary={
                                  <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                                    {document.title}
                                  </Typography>
                                }
                                secondary={
                                  <Box>
                                    <Typography variant="caption" color="textSecondary">
                                      {document.id} • v{document.revision}.{document.version}
                                    </Typography>
                                    <br />
                                    <Box display="flex" gap={0.5} mt={0.5}>
                                      <Chip
                                        label={document.status}
                                        color={getStatusColor(document.status)}
                                        size="small"
                                        sx={{ height: 16, fontSize: '0.65rem' }}
                                      />
                                      <Chip
                                        label={document.stage}
                                        variant="outlined"
                                        size="small"
                                        sx={{ height: 16, fontSize: '0.65rem' }}
                                      />
                                    </Box>
                                    <Typography variant="caption" color="textSecondary" display="block">
                                      Created: {new Date(document.createTime).toLocaleDateString()}
                                    </Typography>
                                  </Box>
                                }
                              />
                            </ListItemButton>
                          </ListItem>
                        ))}
                      </List>
                    ) : (
                      <Box
                        sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          justifyContent: 'center',
                          height: '200px',
                          color: 'text.secondary'
                        }}
                      >
                        <DocumentIcon sx={{ fontSize: 48, mb: 1, opacity: 0.3 }} />
                        <Typography variant="body2" textAlign="center">
                          No documents found for this BOM.
                        </Typography>
                      </Box>
                    )
                  )}
                </CardContent>
              </Card>
            ) : (
              <Box
                sx={{
                  height: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'text.secondary'
                }}
              >
                <Box textAlign="center">
                  <TreeIcon sx={{ fontSize: 64, mb: 2, opacity: 0.3 }} />
                  <Typography variant="h6">Select a BOM from the tree</Typography>
                  <Typography variant="body2">Choose an item from the hierarchy to view details</Typography>
                </Box>
              </Box>
            )}
          </Box>
        </Grid>
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {filteredBOMs.map((bom) => (
            <Grid item xs={12} md={6} lg={4} key={bom.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold' }}>
                      {bom.description}
                    </Typography>
                    <IconButton size="small">
                      <MoreVertIcon />
                    </IconButton>
                  </Box>
                  <Typography variant="body2" color="textSecondary" gutterBottom>
                    {bom.id} • {bom.documentId}
                  </Typography>
                  <Box display="flex" gap={1} mb={2} flexWrap="wrap">
                    <Chip
                      label={bom.status}
                      color={getStatusColor(bom.status)}
                      size="small"
                    />
                    <Chip
                      label={bom.stage}
                      color="primary"
                      size="small"
                    />
                  </Box>
                  <Typography variant="body2" color="textSecondary" paragraph>
                    Child Parts: {bom.children?.length || 0}
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2" color="textSecondary">
                      Created by: {bom.creator}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      {new Date(bom.createTime).toLocaleDateString()}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Create BOM Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New BOM</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            {selectedParentBOM && (
              <Box sx={{ mb: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
                <Typography variant="body2" color="info.dark">
                  <strong>Creating child BOM under:</strong> {selectedParentBOM.description} ({selectedParentBOM.id})
                </Typography>
              </Box>
            )}

            {!selectedParentBOM && (
              <FormControl fullWidth variant="outlined" margin="normal">
                <InputLabel>Parent BOM (Optional)</InputLabel>
                <Select
                  value={newBOM.parentBOMId || ''}
                  onChange={(e) => {
                    const parentId = e.target.value;
                    const parent = parentId ? getAllBOMs(boms).find(b => b.id === parentId) : null;
                    setSelectedParentBOM(parent);
                    setNewBOM({...newBOM, parentBOMId: parentId || null});
                  }}
                  label="Parent BOM (Optional)"
                >
                  <MenuItem value="">No Parent (Root Level)</MenuItem>
                  {getAllBOMs(boms).map((bom) => (
                    <MenuItem key={bom.id} value={bom.id}>
                      {'  '.repeat(bom.level)}{bom.description} ({bom.id})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
            <TextField
              fullWidth
              label="Title"
              variant="outlined"
              value={newBOM.title}
              onChange={(e) => setNewBOM({...newBOM, title: e.target.value})}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Description"
              variant="outlined"
              value={newBOM.description}
              onChange={(e) => setNewBOM({...newBOM, description: e.target.value})}
              margin="normal"
              multiline
              rows={2}
            />
            <TextField
              fullWidth
              label="Creator"
              variant="outlined"
              value={newBOM.creator}
              onChange={(e) => setNewBOM({...newBOM, creator: e.target.value})}
              margin="normal"
              required
            />
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Level</InputLabel>
              <Select
                value={newBOM.level}
                onChange={(e) => setNewBOM({...newBOM, level: e.target.value})}
                label="Level"
              >
                <MenuItem value="ASSEMBLY">Assembly</MenuItem>
                <MenuItem value="PART">Part</MenuItem>
                <MenuItem value="COMPONENT">Component</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Stage</InputLabel>
              <Select
                value={newBOM.stage}
                onChange={(e) => setNewBOM({...newBOM, stage: e.target.value})}
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
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                value={newBOM.status}
                onChange={(e) => setNewBOM({...newBOM, status: e.target.value})}
                label="Status"
              >
                <MenuItem value="IN_WORK">In Work</MenuItem>
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="IN_REVIEW">In Review</MenuItem>
                <MenuItem value="IN_TECHNICAL_REVIEW">In Technical Review</MenuItem>
                <MenuItem value="RELEASED">Released</MenuItem>
                <MenuItem value="APPROVED">Approved</MenuItem>
                <MenuItem value="OBSOLETE">Obsolete</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateBOM}>Create</Button>
        </DialogActions>
      </Dialog>

      {/* Filter Dialog */}
      <Dialog open={filterDialogOpen} onClose={() => setFilterDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Filter BOMs</DialogTitle>
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
                <MenuItem value="OBSOLETE">Obsolete</MenuItem>
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

      {/* Context Menu */}
      <Menu
        open={contextMenu !== null}
        onClose={handleContextMenuClose}
        anchorReference="anchorPosition"
        anchorPosition={
          contextMenu !== null
            ? { top: contextMenu.mouseY, left: contextMenu.mouseX }
            : undefined
        }
      >
        <MenuItem onClick={() => handleCreateChildBOM(contextMenu?.node)}>
          <ListItemIcon>
            <TreeIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Add Child Part (New)</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleAddChildPart(contextMenu?.node)}>
          <ListItemIcon>
            <AddIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Add Child Part (Existing)</ListItemText>
        </MenuItem>
      </Menu>

      {/* Add Child Parts Dialog */}
      <Dialog open={childPartsDialogOpen} onClose={() => setChildPartsDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Child Part to {selectedNode?.description}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Select Child Part</InputLabel>
              <Select
                value={selectedChildPart}
                onChange={(e) => setSelectedChildPart(e.target.value)}
                label="Select Child Part"
              >
                <MenuItem value="">
                  <em>Select a part</em>
                </MenuItem>
                {availableParts.map((part) => (
                  <MenuItem key={part.id} value={part.id}>
                    {part.title} ({part.id}) - {part.status}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Quantity"
              variant="outlined"
              type="number"
              value={childPartQuantity}
              onChange={(e) => setChildPartQuantity(parseInt(e.target.value) || 1)}
              margin="normal"
              inputProps={{ min: 1 }}
            />
            <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
              Select an existing part to add as a child component of this part.
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setChildPartsDialogOpen(false)} disabled={loading}>Cancel</Button>
          <Button variant="contained" onClick={handleAddChildPartSubmit} disabled={loading}>
            {loading ? <CircularProgress size={24} /> : 'Add Child Part'}
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
          Document Details
        </DialogTitle>
        <DialogContent>
          {selectedDocument && (
            <Box>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Typography variant="h6">{selectedDocument.title}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    ID: {selectedDocument.masterId || selectedDocument.id}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">Status</Typography>
                  <Chip
                    label={selectedDocument.status}
                    color={getStatusColor(selectedDocument.status)}
                    size="small"
                  />
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">Stage</Typography>
                  <Chip
                    label={selectedDocument.stage}
                    variant="outlined"
                    size="small"
                  />
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">Version</Typography>
                  <Typography variant="body1">v{selectedDocument.revision}.{selectedDocument.version}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">Creator</Typography>
                  <Typography variant="body1">{selectedDocument.creator}</Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="body2" color="textSecondary">Created</Typography>
                  <Typography variant="body1">
                    {new Date(selectedDocument.createTime).toLocaleString()}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDocumentDetailsOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Edit BOM Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Part</DialogTitle>
        <DialogContent>
          {editingBOM && (
            <Box sx={{ pt: 2 }}>
              <TextField
                fullWidth
                label="Title"
                variant="outlined"
                value={editingBOM.title}
                onChange={(e) => setEditingBOM({...editingBOM, title: e.target.value})}
                margin="normal"
                required
              />
              <TextField
                fullWidth
                label="Description"
                variant="outlined"
                value={editingBOM.description || ''}
                onChange={(e) => setEditingBOM({...editingBOM, description: e.target.value})}
                margin="normal"
                multiline
                rows={2}
              />
              <TextField
                fullWidth
                label="Creator"
                variant="outlined"
                value={editingBOM.creator}
                onChange={(e) => setEditingBOM({...editingBOM, creator: e.target.value})}
                margin="normal"
                required
              />
              <FormControl fullWidth variant="outlined" margin="normal">
                <InputLabel>Level</InputLabel>
                <Select
                  value={editingBOM.level}
                  onChange={(e) => setEditingBOM({...editingBOM, level: e.target.value})}
                  label="Level"
                >
                  <MenuItem value="ASSEMBLY">Assembly</MenuItem>
                  <MenuItem value="PART">Part</MenuItem>
                  <MenuItem value="COMPONENT">Component</MenuItem>
                </Select>
              </FormControl>
              <FormControl fullWidth variant="outlined" margin="normal">
                <InputLabel>Stage</InputLabel>
                <Select
                  value={editingBOM.stage}
                  onChange={(e) => setEditingBOM({...editingBOM, stage: e.target.value})}
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
              <FormControl fullWidth variant="outlined" margin="normal">
                <InputLabel>Status</InputLabel>
                <Select
                  value={editingBOM.status}
                  onChange={(e) => setEditingBOM({...editingBOM, status: e.target.value})}
                  label="Status"
                >
                  <MenuItem value="IN_WORK">In Work</MenuItem>
                  <MenuItem value="DRAFT">Draft</MenuItem>
                  <MenuItem value="IN_REVIEW">In Review</MenuItem>
                  <MenuItem value="IN_TECHNICAL_REVIEW">In Technical Review</MenuItem>
                  <MenuItem value="RELEASED">Released</MenuItem>
                  <MenuItem value="APPROVED">Approved</MenuItem>
                  <MenuItem value="OBSOLETE">Obsolete</MenuItem>
                </Select>
              </FormControl>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)} disabled={loading}>Cancel</Button>
          <Button variant="contained" onClick={handleUpdateBOM} disabled={loading}>
            {loading ? <CircularProgress size={24} /> : 'Update'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this part: <strong>{deletingBOM?.title || deletingBOM?.description}</strong>?
            <br /><br />
            This action cannot be undone. All child relationships will also be removed.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={loading}>Cancel</Button>
          <Button variant="contained" color="error" onClick={handleConfirmDelete} disabled={loading}>
            {loading ? <CircularProgress size={24} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={6000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={() => setSnackbarOpen(false)} severity={error ? 'error' : 'success'} sx={{ width: '100%' }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>

      {/* Loading overlay */}
      {loading && (
        <Box
          sx={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(0, 0, 0, 0.3)',
            zIndex: 9999
          }}
        >
          <CircularProgress />
        </Box>
      )}
    </Box>
  );
}
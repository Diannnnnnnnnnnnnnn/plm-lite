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
  ToggleButtonGroup
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

const mockBOMHierarchy = [
  {
    id: 'BOM-001',
    documentId: 'DOC-001',
    description: 'Electric Motor Assembly',
    creator: 'John Doe',
    stage: 'PRODUCTION',
    status: 'ACTIVE',
    createTime: '2024-01-15T08:00:00',
    updateTime: '2024-01-15T14:30:00',
    items: [
      { id: '1', partNumber: 'MOTOR-001', description: 'AC Motor 1HP', quantity: 1.0, unit: 'EA', reference: 'REF-001' },
      { id: '2', partNumber: 'BOLT-M6-20', description: 'M6x20 Hex Bolt', quantity: 8.0, unit: 'EA', reference: 'REF-002' },
      { id: '3', partNumber: 'GASKET-001', description: 'Motor Housing Gasket', quantity: 1.0, unit: 'EA', reference: 'REF-003' }
    ],
    children: [
      {
        id: 'BOM-001-1',
        documentId: 'DOC-002',
        description: 'Motor Housing',
        creator: 'John Doe',
        stage: 'PRODUCTION',
        status: 'ACTIVE',
        createTime: '2024-01-15T08:00:00',
        updateTime: '2024-01-15T14:30:00',
        items: [
          { id: '7', partNumber: 'HOUSING-001', description: 'Aluminum Housing', quantity: 1.0, unit: 'EA', reference: 'REF-007' },
          { id: '8', partNumber: 'BEARING-001', description: 'Ball Bearing', quantity: 2.0, unit: 'EA', reference: 'REF-008' }
        ],
        children: [
          {
            id: 'BOM-001-1-1',
            documentId: 'DOC-003',
            description: 'Housing Cover',
            creator: 'John Doe',
            stage: 'PRODUCTION',
            status: 'ACTIVE',
            createTime: '2024-01-15T08:00:00',
            updateTime: '2024-01-15T14:30:00',
            items: [
              { id: '9', partNumber: 'COVER-001', description: 'Aluminum Cover', quantity: 1.0, unit: 'EA', reference: 'REF-009' }
            ],
            children: []
          }
        ]
      },
      {
        id: 'BOM-001-2',
        documentId: 'DOC-004',
        description: 'Electrical Components',
        creator: 'Jane Smith',
        stage: 'PRODUCTION',
        status: 'ACTIVE',
        createTime: '2024-01-15T08:00:00',
        updateTime: '2024-01-15T14:30:00',
        items: [
          { id: '10', partNumber: 'WIRE-001', description: 'Copper Wire 12AWG', quantity: 10.0, unit: 'M', reference: 'REF-010' },
          { id: '11', partNumber: 'CONNECTOR-001', description: 'Terminal Connector', quantity: 6.0, unit: 'EA', reference: 'REF-011' }
        ],
        children: []
      }
    ]
  },
  {
    id: 'BOM-002',
    documentId: 'DOC-005',
    description: 'Control Panel Assembly',
    creator: 'Jane Smith',
    stage: 'DESIGN',
    status: 'DRAFT',
    createTime: '2024-01-14T08:00:00',
    updateTime: '2024-01-14T14:30:00',
    items: [
      { id: '4', partNumber: 'PANEL-001', description: 'Control Panel Housing', quantity: 1.0, unit: 'EA', reference: 'REF-004' },
      { id: '5', partNumber: 'SWITCH-001', description: 'Toggle Switch', quantity: 3.0, unit: 'EA', reference: 'REF-005' },
      { id: '6', partNumber: 'LED-RED', description: 'Red LED Indicator', quantity: 2.0, unit: 'EA', reference: 'REF-006' }
    ],
    children: [
      {
        id: 'BOM-002-1',
        documentId: 'DOC-006',
        description: 'Control Electronics',
        creator: 'Jane Smith',
        stage: 'DESIGN',
        status: 'DRAFT',
        createTime: '2024-01-14T08:00:00',
        updateTime: '2024-01-14T14:30:00',
        items: [
          { id: '12', partNumber: 'PCB-001', description: 'Control PCB', quantity: 1.0, unit: 'EA', reference: 'REF-012' },
          { id: '13', partNumber: 'IC-001', description: 'Microcontroller', quantity: 1.0, unit: 'EA', reference: 'REF-013' }
        ],
        children: []
      }
    ]
  }
];

const getStatusColor = (status) => {
  switch (status) {
    case 'ACTIVE': return 'success';
    case 'DRAFT': return 'warning';
    case 'OBSOLETE': return 'error';
    default: return 'default';
  }
};

// Mock related documents data
const relatedDocuments = {
  'BOM-001': [
    {
      id: 'DOC-001',
      title: 'Motor Assembly Specifications.pdf',
      status: 'APPROVED',
      stage: 'PRODUCTION',
      revision: 2,
      version: 1,
      createTime: '2024-01-15T08:00:00',
      creator: 'John Doe'
    },
    {
      id: 'DOC-003',
      title: 'Assembly Instructions.pdf',
      status: 'ACTIVE',
      stage: 'PRODUCTION',
      revision: 1,
      version: 3,
      createTime: '2024-01-14T10:30:00',
      creator: 'Jane Smith'
    },
    {
      id: 'DOC-005',
      title: 'Quality Control Checklist.docx',
      status: 'DRAFT',
      stage: 'DEVELOPMENT',
      revision: 1,
      version: 0,
      createTime: '2024-01-16T14:15:00',
      creator: 'Mike Johnson'
    }
  ],
  'BOM-002': [
    {
      id: 'DOC-002',
      title: 'Control Panel Design.dwg',
      status: 'DRAFT',
      stage: 'DESIGN',
      revision: 1,
      version: 2,
      createTime: '2024-01-13T09:20:00',
      creator: 'Jane Smith'
    },
    {
      id: 'DOC-004',
      title: 'Electrical Schematic.pdf',
      status: 'IN_REVIEW',
      stage: 'DESIGN',
      revision: 1,
      version: 1,
      createTime: '2024-01-15T16:45:00',
      creator: 'Bob Wilson'
    }
  ],
  'BOM-001-1': [
    {
      id: 'DOC-006',
      title: 'Housing Material Specs.pdf',
      status: 'APPROVED',
      stage: 'PRODUCTION',
      revision: 1,
      version: 0,
      createTime: '2024-01-12T11:30:00',
      creator: 'John Doe'
    }
  ],
  'BOM-001-1-1': [
    {
      id: 'DOC-007',
      title: 'Cover Manufacturing Guide.pdf',
      status: 'ACTIVE',
      stage: 'PRODUCTION',
      revision: 1,
      version: 1,
      createTime: '2024-01-11T13:20:00',
      creator: 'Sarah Miller'
    }
  ]
};

export default function BOMManager() {
  const [boms, setBOMs] = useState(mockBOMHierarchy);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('All');
  const [filterStage, setFilterStage] = useState('All');
  const [currentTab, setCurrentTab] = useState(0);
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [filteredBOMs, setFilteredBOMs] = useState(mockBOMHierarchy);
  const [detailsTab, setDetailsTab] = useState(0);
  const [contextMenu, setContextMenu] = useState(null);
  const [selectedParentBOM, setSelectedParentBOM] = useState(null);
  const [itemsDialogOpen, setItemsDialogOpen] = useState(false);
  const [newItem, setNewItem] = useState({
    partNumber: '',
    description: '',
    quantity: 1,
    unit: 'EA',
    reference: ''
  });

  // Function to get related documents for a BOM
  const getRelatedDocuments = (bomId) => {
    return relatedDocuments[bomId] || [];
  };

  // Helper function to recursively add child BOM
  const addChildBOM = (bomList, parentId, newBOM) => {
    return bomList.map(bom => {
      if (bom.id === parentId) {
        return {
          ...bom,
          children: [...(bom.children || []), newBOM]
        };
      } else if (bom.children && bom.children.length > 0) {
        return {
          ...bom,
          children: addChildBOM(bom.children, parentId, newBOM)
        };
      }
      return bom;
    });
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
    documentId: '',
    description: '',
    creator: 'Current User',
    stage: 'DESIGN',
    status: 'DRAFT',
    parentBOMId: null
  });

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
      parentBOMId: parentNode.id
    });
    setCreateDialogOpen(true);
    handleContextMenuClose();
  };

  const handleAddItems = (bomNode) => {
    setSelectedNode(bomNode);
    setItemsDialogOpen(true);
    handleContextMenuClose();
  };

  const handleCreateBOM = () => {
    const newId = newBOM.parentBOMId ?
      `${newBOM.parentBOMId}-${Date.now().toString().slice(-3)}` :
      `BOM-${Date.now().toString().slice(-3)}`;

    const bomToAdd = {
      ...newBOM,
      id: newId,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      items: [],
      children: []
    };

    if (newBOM.parentBOMId) {
      // Add as child to parent BOM
      const updatedBOMs = addChildBOM(boms, newBOM.parentBOMId, bomToAdd);
      setBOMs(updatedBOMs);
    } else {
      // Add as root level BOM
      setBOMs([...boms, bomToAdd]);
    }

    setNewBOM({
      documentId: '',
      description: '',
      creator: 'Current User',
      stage: 'DESIGN',
      status: 'DRAFT',
      parentBOMId: null
    });
    setSelectedParentBOM(null);
    setCreateDialogOpen(false);
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
                    <Typography variant="h5">{selectedNode.id}</Typography>
                    <Box display="flex" gap={1}>
                      <IconButton
                        size="small"
                        onClick={() => handleAddItems(selectedNode)}
                        title="Add Items"
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
                      <IconButton size="small">
                        <EditIcon />
                      </IconButton>
                      <IconButton size="small">
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  </Box>

                  <Typography variant="h6" color="textSecondary" gutterBottom>
                    {selectedNode.description}
                  </Typography>

                  <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid item xs={6} sm={3}>
                      <Typography variant="body2" color="textSecondary">BOM ID</Typography>
                      <Typography variant="body1">{selectedNode.id}</Typography>
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <Typography variant="body2" color="textSecondary">Document ID</Typography>
                      <Typography variant="body1">{selectedNode.documentId}</Typography>
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <Typography variant="body2" color="textSecondary">Stage</Typography>
                      <Typography variant="body1">{selectedNode.stage}</Typography>
                    </Grid>
                    <Grid item xs={6} sm={3}>
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
                          Items
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
                    // Items Tab
                    selectedNode.items && selectedNode.items.length > 0 ? (
                      <TableContainer component={Paper} variant="outlined">
                        <Table size="small">
                          <TableHead>
                            <TableRow>
                              <TableCell>Part Number</TableCell>
                              <TableCell>Description</TableCell>
                              <TableCell>Quantity</TableCell>
                              <TableCell>Unit</TableCell>
                              <TableCell>Reference</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {selectedNode.items.map((item) => (
                              <TableRow key={item.id}>
                                <TableCell>{item.partNumber}</TableCell>
                                <TableCell>{item.description}</TableCell>
                                <TableCell>{item.quantity}</TableCell>
                                <TableCell>{item.unit}</TableCell>
                                <TableCell>{item.reference}</TableCell>
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
                          No items defined for this BOM.
                        </Typography>
                      </Box>
                    )
                  )}

                  {detailsTab === 1 && (
                    // Documents Tab
                    getRelatedDocuments(selectedNode.id).length > 0 ? (
                      <List>
                        {getRelatedDocuments(selectedNode.id).map((document) => (
                          <ListItem key={document.id} disablePadding>
                            <ListItemButton sx={{ mb: 1, border: 1, borderColor: 'divider', borderRadius: 1 }}>
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
                    Items: {bom.items?.length || 0}
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
              label="Document ID"
              variant="outlined"
              value={newBOM.documentId}
              onChange={(e) => setNewBOM({...newBOM, documentId: e.target.value})}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Description"
              variant="outlined"
              value={newBOM.description}
              onChange={(e) => setNewBOM({...newBOM, description: e.target.value})}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Creator"
              variant="outlined"
              value={newBOM.creator}
              onChange={(e) => setNewBOM({...newBOM, creator: e.target.value})}
              margin="normal"
            />
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Stage</InputLabel>
              <Select
                value={newBOM.stage}
                onChange={(e) => setNewBOM({...newBOM, stage: e.target.value})}
                label="Stage"
              >
                <MenuItem value="DESIGN">Design</MenuItem>
                <MenuItem value="DEVELOPMENT">Development</MenuItem>
                <MenuItem value="PRODUCTION">Production</MenuItem>
                <MenuItem value="OBSOLETE">Obsolete</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                value={newBOM.status}
                onChange={(e) => setNewBOM({...newBOM, status: e.target.value})}
                label="Status"
              >
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="ACTIVE">Active</MenuItem>
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
          <ListItemText>Add Child BOM</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleAddItems(contextMenu?.node)}>
          <ListItemIcon>
            <AddIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Add Items</ListItemText>
        </MenuItem>
      </Menu>

      {/* Add Items Dialog */}
      <Dialog open={itemsDialogOpen} onClose={() => setItemsDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Add Items to {selectedNode?.description}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Part Number"
                  variant="outlined"
                  value={newItem.partNumber}
                  onChange={(e) => setNewItem({...newItem, partNumber: e.target.value})}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Reference"
                  variant="outlined"
                  value={newItem.reference}
                  onChange={(e) => setNewItem({...newItem, reference: e.target.value})}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  variant="outlined"
                  value={newItem.description}
                  onChange={(e) => setNewItem({...newItem, description: e.target.value})}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Quantity"
                  variant="outlined"
                  type="number"
                  value={newItem.quantity}
                  onChange={(e) => setNewItem({...newItem, quantity: parseFloat(e.target.value) || 1})}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <FormControl fullWidth variant="outlined" margin="normal">
                  <InputLabel>Unit</InputLabel>
                  <Select
                    value={newItem.unit}
                    onChange={(e) => setNewItem({...newItem, unit: e.target.value})}
                    label="Unit"
                  >
                    <MenuItem value="EA">Each (EA)</MenuItem>
                    <MenuItem value="M">Meters (M)</MenuItem>
                    <MenuItem value="KG">Kilograms (KG)</MenuItem>
                    <MenuItem value="L">Liters (L)</MenuItem>
                    <MenuItem value="SET">Set</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setItemsDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => {
            // Add item logic here
            console.log('Adding item:', newItem);
            setNewItem({
              partNumber: '',
              description: '',
              quantity: 1,
              unit: 'EA',
              reference: ''
            });
            setItemsDialogOpen(false);
          }}>Add Item</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
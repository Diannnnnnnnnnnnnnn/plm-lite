import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  TextField,
  Typography,
  CircularProgress,
  Card,
  CardContent,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  InputAdornment,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Paper,
  Tabs,
  Tab,
  Badge,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableRow,
  TableCell
} from '@mui/material';
import {
  Search as SearchIcon,
  Close as CloseIcon,
  Description as DocumentIcon,
  AccountTree as BOMIcon,
  Assignment as TaskIcon,
  ChangeHistory as ChangeIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  ExpandMore as ExpandMoreIcon,
  Category as PartIcon
} from '@mui/icons-material';

const getItemIcon = (type) => {
  switch (type) {
    case 'document': return <DocumentIcon color="primary" />;
    case 'task': return <TaskIcon color="secondary" />;
    case 'part': return <PartIcon color="info" />;
    case 'change': return <ChangeIcon color="warning" />;
    default: return <SearchIcon />;
  }
};

const getStatusColor = (status) => {
  switch (status?.toLowerCase()) {
    case 'active':
    case 'approved':
    case 'completed': return 'success';
    case 'draft':
    case 'todo':
    case 'in_progress': return 'warning';
    case 'in_review': return 'info';
    case 'rejected':
    case 'obsolete': return 'error';
    default: return 'default';
  }
};

const SEARCH_API_URL = 'http://localhost:8091/api/v1/search';

export default function GlobalSearch() {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [currentTab, setCurrentTab] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState(null);
  const [error, setError] = useState(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Fetch search results from API
  useEffect(() => {
    if (debouncedSearchTerm) {
      setIsSearching(true);
      setError(null);
      
      const url = `${SEARCH_API_URL}?q=${encodeURIComponent(debouncedSearchTerm)}`;
      
      fetch(url)
        .then(response => {
          if (!response.ok) {
            throw new Error('Search service unavailable');
          }
          return response.json();
        })
        .then(data => {
          setSearchResults(data);
          setIsSearching(false);
        })
        .catch(err => {
          console.error('Search error:', err);
          setError('Failed to fetch search results. Please try again.');
          setIsSearching(false);
        });
    } else {
      setSearchResults(null);
    }
  }, [debouncedSearchTerm]);

  // Combine all data sources from API results
  const allData = useMemo(() => {
    if (!searchResults) return [];
    
    // Map API results to frontend format (convert type to lowercase)
    const documents = (searchResults.documents || []).map(doc => ({
      ...doc,
      type: doc.type?.toLowerCase() || 'document'
    }));
    
    const tasks = (searchResults.tasks || []).map(task => ({
      ...task,
      type: task.type?.toLowerCase() || 'task'
    }));
    
    const parts = (searchResults.parts || []).map(part => ({
      ...part,
      type: part.type?.toLowerCase() || 'part'
    }));
    
    const changes = (searchResults.changes || []).map(change => ({
      ...change,
      type: change.type?.toLowerCase() || 'change'
    }));
    
    return [...documents, ...tasks, ...parts, ...changes];
  }, [searchResults]);

  // Filter data (filtering already done by API, but add client-side filters)
  const filteredResults = useMemo(() => {
    if (!debouncedSearchTerm) return [];

    let filtered = allData;

    // Filter by category
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(item => item.type === selectedCategory);
    }

    // Filter by status
    if (selectedStatus !== 'all') {
      filtered = filtered.filter(item =>
        item.status?.toLowerCase() === selectedStatus ||
        item.taskStatus?.toLowerCase() === selectedStatus
      );
    }

    return filtered;
  }, [allData, debouncedSearchTerm, selectedCategory, selectedStatus]);

  // Group results by type
  const groupedResults = useMemo(() => {
    const groups = {
      document: [],
      task: [],
      part: [],
      change: []
    };

    filteredResults.forEach(item => {
      if (groups[item.type]) {
        groups[item.type].push(item);
      }
    });

    return groups;
  }, [filteredResults]);

  const totalResults = filteredResults.length;

  const handleClearSearch = () => {
    setSearchTerm('');
    setDebouncedSearchTerm('');
  };

  const handleClearFilters = () => {
    setSelectedCategory('all');
    setSelectedStatus('all');
  };

  const handleItemClick = (item) => {
    setSelectedItem(item);
    setDetailDialogOpen(true);
  };

  const handleCloseDetail = () => {
    setDetailDialogOpen(false);
    setSelectedItem(null);
  };

  const renderResultItem = (item) => (
    <ListItem 
      key={item.id} 
      sx={{ 
        py: 1, 
        px: 0,
        cursor: 'pointer',
        '&:hover': {
          backgroundColor: 'action.hover'
        }
      }}
      onClick={() => handleItemClick(item)}
    >
      <ListItemIcon sx={{ minWidth: 40 }}>
        {getItemIcon(item.type)}
      </ListItemIcon>
      <ListItemText
        primary={
          <Box>
            <Typography variant="subtitle2" component="div">
              {item.title || item.taskName || item.description}
            </Typography>
            <Typography variant="caption" color="textSecondary">
              {item.id}{(item.creator || item.assignee) && ` • ${item.creator || item.assignee}`}
            </Typography>
          </Box>
        }
        secondary={
          <Box sx={{ mt: 0.5 }}>
            <Box display="flex" gap={0.5} flexWrap="wrap">
              {(item.status || item.taskStatus) && (
                <Chip
                  label={item.status || item.taskStatus}
                  color={getStatusColor(item.status || item.taskStatus)}
                  size="small"
                  sx={{ height: 20, fontSize: '0.7rem' }}
                />
              )}
              {item.stage && (
                <Chip
                  label={item.stage}
                  variant="outlined"
                  size="small"
                  sx={{ height: 20, fontSize: '0.7rem' }}
                />
              )}
              {item.priority && (
                <Chip
                  label={`Priority: ${item.priority}`}
                  variant="outlined"
                  size="small"
                  sx={{ height: 20, fontSize: '0.7rem' }}
                />
              )}
            </Box>
            <Typography variant="body2" color="textSecondary" sx={{ mt: 0.5, fontSize: '0.8rem' }}>
              {item.taskDescription || item.changeReason || item.description}
            </Typography>
            {item.type === 'bom' && item.items && item.items.length > 0 && (
              <Box sx={{ mt: 1, pl: 2, borderLeft: '2px solid #e0e0e0' }}>
                <Typography variant="caption" color="textSecondary" sx={{ fontWeight: 'bold' }}>
                  Parts ({item.items.length}):
                </Typography>
                {item.items.slice(0, 5).map((bomItem, idx) => (
                  <Typography key={idx} variant="caption" display="block" color="textSecondary" sx={{ fontSize: '0.75rem' }}>
                    • {bomItem.partTitle || bomItem.partId} 
                    {bomItem.quantity && ` (${bomItem.quantity}${bomItem.unit ? ' ' + bomItem.unit : ''})`}
                  </Typography>
                ))}
                {item.items.length > 5 && (
                  <Typography variant="caption" color="textSecondary" sx={{ fontSize: '0.7rem', fontStyle: 'italic' }}>
                    ...and {item.items.length - 5} more
                  </Typography>
                )}
              </Box>
            )}
          </Box>
        }
      />
    </ListItem>
  );

  const tabLabels = [
    { label: 'All', count: totalResults },
    { label: 'Documents', count: groupedResults.document.length },
    { label: 'Tasks', count: groupedResults.task.length },
    { label: 'Parts', count: groupedResults.part.length },
    { label: 'Changes', count: groupedResults.change.length }
  ];

  return (
    <Box sx={{ height: 'calc(100vh - 100px)', display: 'flex', flexDirection: 'column' }}>
      {/* Search Input */}
      <Box sx={{ mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Search across all sections..."
          variant="outlined"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color="action" />
              </InputAdornment>
            ),
            endAdornment: searchTerm && (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={handleClearSearch}
                  edge="end"
                >
                  <CloseIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            )
          }}
        />
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Category</InputLabel>
              <Select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                label="Category"
              >
                <MenuItem value="all">All Categories</MenuItem>
                <MenuItem value="document">Documents</MenuItem>
                <MenuItem value="task">Tasks</MenuItem>
                <MenuItem value="part">Parts</MenuItem>
                <MenuItem value="change">Changes</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Status</InputLabel>
              <Select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                label="Status"
              >
                <MenuItem value="all">All Statuses</MenuItem>
                <MenuItem value="active">Active</MenuItem>
                <MenuItem value="draft">Draft</MenuItem>
                <MenuItem value="approved">Approved</MenuItem>
                <MenuItem value="in_progress">In Progress</MenuItem>
                <MenuItem value="completed">Completed</MenuItem>
                <MenuItem value="todo">To Do</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <Box display="flex" gap={1}>
              <IconButton
                size="small"
                onClick={handleClearFilters}
                title="Clear Filters"
              >
                <ClearIcon />
              </IconButton>
              {totalResults > 0 && (
                <Typography variant="body2" sx={{ alignSelf: 'center', color: 'text.secondary' }}>
                  {totalResults} result{totalResults !== 1 ? 's' : ''}
                </Typography>
              )}
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Loading State */}
      {isSearching && (
        <Box display="flex" justifyContent="center" py={2}>
          <CircularProgress size={24} />
        </Box>
      )}

      {/* Error State */}
      {error && (
        <Paper sx={{ p: 3, mb: 2, bgcolor: 'error.light', color: 'error.contrastText' }}>
          <Typography variant="body1">
            ⚠️ {error}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            Make sure the Search Service is running on port 8091.
          </Typography>
        </Paper>
      )}

      {/* Results */}
      <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
        {debouncedSearchTerm && !isSearching && !error && (
          <>
            {totalResults > 0 ? (
              <>
                {/* Result Tabs */}
                <Tabs
                  value={currentTab}
                  onChange={(e, newValue) => setCurrentTab(newValue)}
                  variant="scrollable"
                  scrollButtons="auto"
                  sx={{ mb: 2 }}
                >
                  {tabLabels.map((tab, index) => (
                    <Tab
                      key={index}
                      label={
                        <Badge badgeContent={tab.count} color="primary" max={999}>
                          {tab.label}
                        </Badge>
                      }
                    />
                  ))}
                </Tabs>

                {/* Results Display */}
                <Box sx={{ height: 'calc(100vh - 400px)', overflow: 'auto' }}>
                  {currentTab === 0 && (
                    // All Results - Grouped by Type
                    <Box>
                      {Object.entries(groupedResults).map(([type, items]) => (
                        items.length > 0 && (
                          <Accordion key={type} defaultExpanded>
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                              <Box display="flex" alignItems="center" gap={1}>
                                {getItemIcon(type)}
                                <Typography variant="h6" sx={{ textTransform: 'capitalize' }}>
                                  {type}s ({items.length})
                                </Typography>
                              </Box>
                            </AccordionSummary>
                            <AccordionDetails sx={{ pt: 0 }}>
                              <List>
                                {items.map(renderResultItem)}
                              </List>
                            </AccordionDetails>
                          </Accordion>
                        )
                      ))}
                    </Box>
                  )}

                  {currentTab === 1 && (
                    <List>{groupedResults.document.map(renderResultItem)}</List>
                  )}

                  {currentTab === 2 && (
                    <List>{groupedResults.task.map(renderResultItem)}</List>
                  )}

                  {currentTab === 3 && (
                    <List>{groupedResults.part.map(renderResultItem)}</List>
                  )}

                  {currentTab === 4 && (
                    <List>{groupedResults.change.map(renderResultItem)}</List>
                  )}
                </Box>
              </>
            ) : (
              // No Results
              <Paper sx={{ p: 4, textAlign: 'center' }}>
                <SearchIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="textSecondary">
                  No results found
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Try adjusting your search terms or filters
                </Typography>
              </Paper>
            )}
          </>
        )}

        {!debouncedSearchTerm && !isSearching && (
          // Welcome State
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <SearchIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" color="textSecondary" gutterBottom>
              Global Search
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Search across documents, tasks, parts, and changes all in one place
            </Typography>
          </Paper>
        )}
      </Box>

      {/* Detail Dialog */}
      <Dialog
        open={detailDialogOpen}
        onClose={handleCloseDetail}
        maxWidth="md"
        fullWidth
      >
        {selectedItem && (
          <>
            <DialogTitle>
              <Box display="flex" alignItems="center" gap={1}>
                {getItemIcon(selectedItem.type)}
                <Typography variant="h6">
                  {selectedItem.title || selectedItem.taskName || selectedItem.description}
                </Typography>
              </Box>
            </DialogTitle>
            <DialogContent>
              <Table size="small">
                <TableBody>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold', width: '30%' }}>
                      ID
                    </TableCell>
                    <TableCell>{selectedItem.id}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                      Type
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={selectedItem.type.toUpperCase()} 
                        size="small" 
                        color="primary"
                      />
                    </TableCell>
                  </TableRow>
                  {selectedItem.description && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Description
                      </TableCell>
                      <TableCell>{selectedItem.description}</TableCell>
                    </TableRow>
                  )}
                  {(selectedItem.status || selectedItem.taskStatus) && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Status
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={selectedItem.status || selectedItem.taskStatus} 
                          size="small"
                          color={getStatusColor(selectedItem.status || selectedItem.taskStatus)}
                        />
                      </TableCell>
                    </TableRow>
                  )}
                  {selectedItem.stage && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Stage
                      </TableCell>
                      <TableCell>{selectedItem.stage}</TableCell>
                    </TableRow>
                  )}
                  {(selectedItem.creator || selectedItem.assignee) && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        {selectedItem.type === 'task' ? 'Assignee' : 'Creator'}
                      </TableCell>
                      <TableCell>{selectedItem.creator || selectedItem.assignee}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.level && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Level
                      </TableCell>
                      <TableCell>{selectedItem.level}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.documentNumber && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Document Number
                      </TableCell>
                      <TableCell>{selectedItem.documentNumber}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.category && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Category
                      </TableCell>
                      <TableCell>{selectedItem.category}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.changeClass && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Change Class
                      </TableCell>
                      <TableCell>{selectedItem.changeClass}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.changeReason && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Change Reason
                      </TableCell>
                      <TableCell>{selectedItem.changeReason}</TableCell>
                    </TableRow>
                  )}
                  {selectedItem.score !== undefined && (
                    <TableRow>
                      <TableCell component="th" sx={{ fontWeight: 'bold' }}>
                        Search Score
                      </TableCell>
                      <TableCell>{selectedItem.score.toFixed(4)}</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseDetail}>Close</Button>
              <Button 
                variant="contained" 
                onClick={() => {
                  // Navigate to detail page based on type
                  const routes = {
                    'document': `/documents/${selectedItem.id}`,
                    'task': `/tasks/${selectedItem.id}`,
                    'part': `/bom/${selectedItem.id}`,
                    'change': `/changes/${selectedItem.id}`
                  };
                  const route = routes[selectedItem.type];
                  if (route) {
                    window.location.href = route;
                  }
                }}
              >
                View Full Details
              </Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  );
}
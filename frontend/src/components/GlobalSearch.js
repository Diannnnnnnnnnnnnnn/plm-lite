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
  AccordionDetails
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
  ExpandMore as ExpandMoreIcon
} from '@mui/icons-material';

// Mock data - in a real app, this would come from your backend
const mockData = {
  documents: [
    {
      id: 'DOC-001',
      title: 'Product Specification v2.1.pdf',
      type: 'document',
      stage: 'PRODUCTION',
      status: 'APPROVED',
      creator: 'John Doe',
      createTime: '2024-01-15T08:00:00',
      description: 'Detailed product specifications for motor assembly'
    },
    {
      id: 'DOC-002',
      title: 'Technical Drawing TD-001.dwg',
      type: 'document',
      stage: 'DESIGN',
      status: 'DRAFT',
      creator: 'Jane Smith',
      createTime: '2024-01-16T10:30:00',
      description: 'CAD drawing for motor housing component'
    }
  ],
  tasks: [
    {
      id: 'TASK-001',
      taskName: 'Design Review for Motor Assembly',
      type: 'task',
      taskDescription: 'Complete technical review of the motor assembly design documents',
      taskStatus: 'IN_PROGRESS',
      priority: 3,
      assignedTo: 'John Doe',
      dueDate: '2024-01-20T10:00:00'
    },
    {
      id: 'TASK-002',
      taskName: 'BOM Validation',
      type: 'task',
      taskDescription: 'Validate bill of materials for accuracy and cost optimization',
      taskStatus: 'TODO',
      priority: 2,
      assignedTo: 'Sarah Wilson',
      dueDate: '2024-01-25T15:00:00'
    }
  ],
  boms: [
    {
      id: 'BOM-001',
      description: 'Electric Motor Assembly',
      type: 'bom',
      stage: 'PRODUCTION',
      status: 'ACTIVE',
      creator: 'John Doe',
      createTime: '2024-01-15T08:00:00',
      itemCount: 12
    },
    {
      id: 'BOM-002',
      description: 'Control Panel Assembly',
      type: 'bom',
      stage: 'DESIGN',
      status: 'DRAFT',
      creator: 'Jane Smith',
      createTime: '2024-01-14T08:00:00',
      itemCount: 8
    }
  ],
  changes: [
    {
      id: 'CHG-001',
      title: 'Motor Assembly Design Update',
      type: 'change',
      stage: 'DESIGN',
      changeClass: 'Major',
      status: 'DRAFT',
      creator: 'John Doe',
      createTime: '2024-01-15T08:00:00',
      changeReason: 'Performance improvement based on customer feedback'
    },
    {
      id: 'CHG-002',
      title: 'PCB Layout Modification',
      type: 'change',
      stage: 'DEVELOPMENT',
      changeClass: 'Minor',
      status: 'IN_REVIEW',
      creator: 'Jane Smith',
      createTime: '2024-01-14T09:30:00',
      changeReason: 'Component obsolescence replacement'
    }
  ]
};

const getItemIcon = (type) => {
  switch (type) {
    case 'document': return <DocumentIcon color="primary" />;
    case 'task': return <TaskIcon color="secondary" />;
    case 'bom': return <BOMIcon color="success" />;
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

export default function GlobalSearch() {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [currentTab, setCurrentTab] = useState(0);
  const [isSearching, setIsSearching] = useState(false);

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      if (searchTerm) {
        setIsSearching(true);
        // Simulate API delay
        setTimeout(() => setIsSearching(false), 300);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Combine all data sources
  const allData = useMemo(() => {
    return [
      ...mockData.documents,
      ...mockData.tasks,
      ...mockData.boms,
      ...mockData.changes
    ];
  }, []);

  // Filter and search data
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

    // Search across all relevant fields
    const searchLower = debouncedSearchTerm.toLowerCase();
    filtered = filtered.filter(item => {
      const searchFields = [
        item.title,
        item.taskName,
        item.description,
        item.taskDescription,
        item.creator,
        item.assignedTo,
        item.id,
        item.stage,
        item.status,
        item.taskStatus,
        item.changeReason,
        item.changeClass
      ].filter(Boolean);

      return searchFields.some(field =>
        field.toLowerCase().includes(searchLower)
      );
    });

    return filtered;
  }, [allData, debouncedSearchTerm, selectedCategory, selectedStatus]);

  // Group results by type
  const groupedResults = useMemo(() => {
    const groups = {
      document: [],
      task: [],
      bom: [],
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

  const renderResultItem = (item) => (
    <ListItem key={item.id} sx={{ py: 1, px: 0 }}>
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
              {item.id} • {item.creator || item.assignedTo}
            </Typography>
          </Box>
        }
        secondary={
          <Box sx={{ mt: 0.5 }}>
            <Box display="flex" gap={0.5} flexWrap="wrap">
              <Chip
                label={item.status || item.taskStatus}
                color={getStatusColor(item.status || item.taskStatus)}
                size="small"
                sx={{ height: 20, fontSize: '0.7rem' }}
              />
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
          </Box>
        }
      />
    </ListItem>
  );

  const tabLabels = [
    { label: 'All', count: totalResults },
    { label: 'Documents', count: groupedResults.document.length },
    { label: 'Tasks', count: groupedResults.task.length },
    { label: 'BOMs', count: groupedResults.bom.length },
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
                <MenuItem value="bom">BOMs</MenuItem>
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

      {/* Results */}
      <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
        {debouncedSearchTerm && !isSearching && (
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
                    <List>{groupedResults.bom.map(renderResultItem)}</List>
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
              Search across documents, tasks, BOMs, and changes all in one place
            </Typography>
          </Paper>
        )}
      </Box>
    </Box>
  );
}
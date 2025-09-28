import React, { useState, useEffect } from 'react';
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
  Avatar,
  AvatarGroup,
  Tooltip,
  Fab,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  LinearProgress,
  ToggleButton,
  ToggleButtonGroup
} from '@mui/material';
import {
  Add as AddIcon,
  MoreVert as MoreVertIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
  CalendarToday as CalendarIcon,
  Flag as PriorityIcon,
  AttachFile as AttachFileIcon,
  Comment as CommentIcon,
  Schedule as ScheduleIcon,
  CheckCircle as CheckCircleIcon,
  PlayArrow as PlayArrowIcon,
  Pause as PauseIcon,
  FilterList as FilterIcon,
  Search as SearchIcon,
  Close as CloseIcon,
  ViewModule as GridViewIcon,
  ViewKanban as KanbanViewIcon,
  Assignment as TaskIcon
} from '@mui/icons-material';

const mockTasks = [
  {
    id: '1',
    taskName: 'Design Review for Motor Assembly',
    taskDescription: 'Complete technical review of the motor assembly design documents and provide feedback',
    taskStatus: 'IN_PROGRESS',
    priority: 3,
    assignedTo: 'John Doe',
    assignedBy: 'Manager',
    dueDate: '2024-01-20T10:00:00',
    createdAt: '2024-01-15T08:00:00',
    updatedAt: '2024-01-16T14:30:00',
    taskType: 'REVIEW',
    parentTaskId: null,
    workflowId: 'WF-001',
    contextType: 'DESIGN',
    contextId: 'DESIGN-001'
  },
  {
    id: '2',
    taskName: 'BOM Validation',
    taskDescription: 'Validate bill of materials for accuracy and cost optimization',
    taskStatus: 'TODO',
    priority: 2,
    assignedTo: 'Sarah Wilson',
    assignedBy: 'Manager',
    dueDate: '2024-01-25T15:00:00',
    createdAt: '2024-01-16T09:00:00',
    updatedAt: '2024-01-16T09:00:00',
    taskType: 'VALIDATION',
    parentTaskId: null,
    workflowId: 'WF-002',
    contextType: 'BOM',
    contextId: 'BOM-001'
  },
  {
    id: '3',
    taskName: 'Documentation Update',
    taskDescription: 'Update technical documentation for the new product release',
    taskStatus: 'COMPLETED',
    priority: 1,
    assignedTo: 'Mike Johnson',
    assignedBy: 'Manager',
    dueDate: '2024-01-18T17:00:00',
    createdAt: '2024-01-10T10:00:00',
    updatedAt: '2024-01-18T16:45:00',
    taskType: 'DOCUMENTATION',
    parentTaskId: null,
    workflowId: 'WF-003',
    contextType: 'DOCUMENTATION',
    contextId: 'DOC-001'
  }
];

const statusColors = {
  'TODO': 'default',
  'IN_PROGRESS': 'primary',
  'COMPLETED': 'success',
  'ON_HOLD': 'warning',
  'CANCELLED': 'error'
};

const priorityColors = {
  1: 'success',
  2: 'warning',
  3: 'error'
};

const priorityLabels = {
  1: 'Low',
  2: 'Medium',
  3: 'High'
};

const TaskCard = ({ task, onEdit, onDelete, onStatusChange, onClick }) => {
  const [anchorEl, setAnchorEl] = useState(null);

  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const getInitials = (name) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  return (
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
      onClick={() => onClick && onClick(task)}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold', fontSize: '1.1rem' }}>
            {task.taskName}
          </Typography>
          <IconButton
            size="small"
            onClick={(e) => {
              e.stopPropagation();
              handleMenuClick(e);
            }}
          >
            <MoreVertIcon />
          </IconButton>
        </Box>

        <Typography variant="body2" color="textSecondary" paragraph>
          {task.taskDescription}
        </Typography>

        <Box display="flex" gap={1} mb={2}>
          <Chip
            label={task.taskStatus.replace('_', ' ')}
            color={statusColors[task.taskStatus]}
            size="small"
          />
          <Chip
            label={priorityLabels[task.priority]}
            color={priorityColors[task.priority]}
            size="small"
            icon={<PriorityIcon />}
          />
        </Box>

        <Box display="flex" flexWrap="wrap" gap={0.5} mb={2}>
          <Chip label={task.taskType} variant="outlined" size="small" />
          {task.contextType && (
            <Chip label={task.contextType} variant="outlined" size="small" />
          )}
        </Box>

        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              {getInitials(task.assignedTo)}
            </Avatar>
            <Typography variant="body2">{task.assignedTo}</Typography>
          </Box>

          <Typography variant="body2" color="textSecondary">
            By: {task.assignedBy}
          </Typography>
        </Box>

        <Box mb={2}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
            <Typography variant="body2" color="textSecondary">Status Progress</Typography>
            <Typography variant="body2" color="textSecondary">
              {task.taskStatus === 'COMPLETED' ? '100%' :
               task.taskStatus === 'IN_PROGRESS' ? '50%' : '0%'}
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={task.taskStatus === 'COMPLETED' ? 100 :
                   task.taskStatus === 'IN_PROGRESS' ? 50 : 0}
          />
        </Box>

        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" gap={2}>
            <Box display="flex" alignItems="center" gap={0.5}>
              <ScheduleIcon fontSize="small" color="action" />
              <Typography variant="body2" color="textSecondary">
                Created: {new Date(task.createdAt).toLocaleDateString()}
              </Typography>
            </Box>
          </Box>

          <Box display="flex" alignItems="center" gap={0.5}>
            <CalendarIcon fontSize="small" color="action" />
            <Typography variant="body2" color="textSecondary">
              {new Date(task.dueDate).toLocaleDateString()}
            </Typography>
          </Box>
        </Box>
      </CardContent>

      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose}>
        <MenuItem onClick={() => { onEdit(task); handleMenuClose(); }}>
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Edit
        </MenuItem>
        <MenuItem onClick={() => { onStatusChange(task); handleMenuClose(); }}>
          <ScheduleIcon fontSize="small" sx={{ mr: 1 }} />
          Change Status
        </MenuItem>
        <MenuItem onClick={() => { onDelete(task.id); handleMenuClose(); }}>
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>
    </Card>
  );
};

export default function TaskManager() {
  const [tasks, setTasks] = useState(mockTasks);
  const [filteredTasks, setFilteredTasks] = useState(mockTasks);
  const [currentTab, setCurrentTab] = useState(0);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);
  const [filterStatus, setFilterStatus] = useState('All');
  const [filterPriority, setFilterPriority] = useState('All');
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [filterDialogOpen, setFilterDialogOpen] = useState(false);
  const [taskDetailsOpen, setTaskDetailsOpen] = useState(false);
  const [selectedTaskForDetails, setSelectedTaskForDetails] = useState(null);

  // Search highlighting function
  const highlightSearchTerm = (text, term) => {
    if (!term || !text) return text;
    const regex = new RegExp(`(${term})`, 'gi');
    return text.replace(regex, '<mark style="background-color: #ffeb3b; padding: 0;">$1</mark>');
  };

  // Debounce search term
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const [newTask, setNewTask] = useState({
    taskName: '',
    taskDescription: '',
    priority: 2,
    assignedTo: '',
    dueDate: '',
    taskType: 'GENERAL',
    contextType: '',
    contextId: ''
  });

  useEffect(() => {
    let filtered = tasks;

    // Filter by status
    if (filterStatus !== 'All') {
      filtered = filtered.filter(task => task.taskStatus === filterStatus);
    }

    // Filter by priority
    if (filterPriority !== 'All') {
      filtered = filtered.filter(task => task.priority.toString() === filterPriority);
    }

    // Filter by search term (using debounced term)
    if (debouncedSearchTerm) {
      const searchLower = debouncedSearchTerm.toLowerCase();
      filtered = filtered.filter(task =>
        task.taskName.toLowerCase().includes(searchLower) ||
        task.taskDescription.toLowerCase().includes(searchLower) ||
        task.assignedTo.toLowerCase().includes(searchLower) ||
        task.taskType.toLowerCase().includes(searchLower) ||
        task.contextType?.toLowerCase().includes(searchLower)
      );
    }

    setFilteredTasks(filtered);
  }, [tasks, filterStatus, filterPriority, debouncedSearchTerm]);

  const handleTabChange = (event, newValue) => {
    setCurrentTab(newValue);
  };

  const handleCreateTask = () => {
    const task = {
      id: (Math.max(...tasks.map(t => parseInt(t.id))) + 1).toString(),
      ...newTask,
      taskStatus: 'TODO',
      assignedBy: 'Current User',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      parentTaskId: null,
      workflowId: null
    };

    setTasks([...tasks, task]);
    setCreateDialogOpen(false);
    setNewTask({
      taskName: '',
      taskDescription: '',
      priority: 2,
      assignedTo: '',
      dueDate: '',
      taskType: 'GENERAL',
      contextType: '',
      contextId: ''
    });
  };

  const getInitials = (name) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  const handleTaskClick = (task) => {
    setSelectedTaskForDetails(task);
    setTaskDetailsOpen(true);
  };

  const handleEditTask = (task) => {
    setSelectedTask(task);
    setEditDialogOpen(true);
  };

  const handleDeleteTask = (taskId) => {
    setTasks(tasks.filter(task => task.id !== taskId));
  };

  const handleStatusChange = (task) => {
    // Implementation for status change dialog
    console.log('Change status for task:', task.id);
  };

  const getTasksByStatus = (status) => {
    return filteredTasks.filter(task => task.taskStatus === status);
  };

  const renderTaskGrid = (taskList) => (
    <Grid container spacing={3}>
      {taskList.map((task) => (
        <Grid item xs={12} md={6} lg={4} key={task.id}>
          <TaskCard
            task={task}
            onEdit={handleEditTask}
            onDelete={handleDeleteTask}
            onStatusChange={handleStatusChange}
            onClick={handleTaskClick}
          />
        </Grid>
      ))}
    </Grid>
  );

  const renderKanbanBoard = () => {
    const statuses = ['TODO', 'IN_PROGRESS', 'COMPLETED'];
    const statusLabels = {
      'TODO': 'To Do',
      'IN_PROGRESS': 'In Progress',
      'COMPLETED': 'Completed'
    };

    return (
      <Grid container spacing={2}>
        {statuses.map((status) => (
          <Grid item xs={12} md={4} key={status}>
            <Paper sx={{ p: 2, minHeight: '600px', bgcolor: 'grey.50' }}>
              <Typography variant="h6" gutterBottom>
                {statusLabels[status]} ({getTasksByStatus(status).length})
              </Typography>
              <Box display="flex" flexDirection="column" gap={2}>
                {getTasksByStatus(status).map((task) => (
                  <TaskCard
                    key={task.id}
                    task={task}
                    onEdit={handleEditTask}
                    onDelete={handleDeleteTask}
                    onStatusChange={handleStatusChange}
                    onClick={handleTaskClick}
                  />
                ))}
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
    );
  };

  return (
    <Box>
      {/* Search and Filters */}
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} md={4}>
          <Box sx={{ display: 'flex', gap: 1, height: '40px', alignItems: 'center' }}>
            <TextField
              fullWidth
              placeholder="Search tasks..."
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
              <ToggleButton value={0} aria-label="grid view">
                <Tooltip title="Grid View">
                  <GridViewIcon />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value={1} aria-label="kanban view">
                <Tooltip title="Kanban Board">
                  <KanbanViewIcon />
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
            New Task
          </Button>
        </Grid>
      </Grid>

      {/* Task Display */}
      {currentTab === 0 && renderTaskGrid(filteredTasks)}
      {currentTab === 1 && renderKanbanBoard()}

      {/* Create Task Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Create New Task</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Task Name"
                variant="outlined"
                value={newTask.taskName}
                onChange={(e) => setNewTask({...newTask, taskName: e.target.value})}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                variant="outlined"
                multiline
                rows={3}
                value={newTask.taskDescription}
                onChange={(e) => setNewTask({...newTask, taskDescription: e.target.value})}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth variant="outlined">
                <InputLabel>Priority</InputLabel>
                <Select
                  value={newTask.priority}
                  onChange={(e) => setNewTask({...newTask, priority: e.target.value})}
                  label="Priority"
                >
                  <MenuItem value={1}>Low</MenuItem>
                  <MenuItem value={2}>Medium</MenuItem>
                  <MenuItem value={3}>High</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Assigned To"
                variant="outlined"
                value={newTask.assignedTo}
                onChange={(e) => setNewTask({...newTask, assignedTo: e.target.value})}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Due Date"
                variant="outlined"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={newTask.dueDate}
                onChange={(e) => setNewTask({...newTask, dueDate: e.target.value})}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth variant="outlined">
                <InputLabel>Task Type</InputLabel>
                <Select
                  value={newTask.taskType}
                  onChange={(e) => setNewTask({...newTask, taskType: e.target.value})}
                  label="Task Type"
                >
                  <MenuItem value="GENERAL">General</MenuItem>
                  <MenuItem value="REVIEW">Review</MenuItem>
                  <MenuItem value="VALIDATION">Validation</MenuItem>
                  <MenuItem value="DOCUMENTATION">Documentation</MenuItem>
                  <MenuItem value="TESTING">Testing</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateTask}>Create Task</Button>
        </DialogActions>
      </Dialog>

      {/* Filter Dialog */}
      <Dialog open={filterDialogOpen} onClose={() => setFilterDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Filter Tasks</DialogTitle>
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
                <MenuItem value="TODO">To Do</MenuItem>
                <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                <MenuItem value="COMPLETED">Completed</MenuItem>
                <MenuItem value="ON_HOLD">On Hold</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel>Priority</InputLabel>
              <Select
                value={filterPriority}
                onChange={(e) => setFilterPriority(e.target.value)}
                label="Priority"
              >
                <MenuItem value="All">All Priorities</MenuItem>
                <MenuItem value="1">Low</MenuItem>
                <MenuItem value="2">Medium</MenuItem>
                <MenuItem value="3">High</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setFilterStatus('All');
            setFilterPriority('All');
          }}>
            Clear Filters
          </Button>
          <Button onClick={() => setFilterDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setFilterDialogOpen(false)}>
            Apply Filters
          </Button>
        </DialogActions>
      </Dialog>

      {/* Task Details Dialog */}
      <Dialog
        open={taskDetailsOpen}
        onClose={() => setTaskDetailsOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box display="flex" alignItems="center" gap={1}>
              <TaskIcon color="primary" />
              <Typography variant="h5" component="div">
                Task Details
              </Typography>
            </Box>
            <IconButton
              onClick={() => setTaskDetailsOpen(false)}
              size="small"
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          {selectedTaskForDetails && (
            <Box sx={{ pt: 1 }}>
              {/* Task Header */}
              <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="h6" component="div" sx={{ mb: 1, fontWeight: 'bold' }}>
                  {selectedTaskForDetails.taskName}
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Task ID: {selectedTaskForDetails.id}
                </Typography>
                <Box display="flex" gap={1} flexWrap="wrap">
                  <Chip
                    label={selectedTaskForDetails.taskStatus}
                    color={selectedTaskForDetails.taskStatus === 'COMPLETED' ? 'success' :
                           selectedTaskForDetails.taskStatus === 'IN_PROGRESS' ? 'warning' : 'default'}
                    size="small"
                  />
                  <Chip
                    label={`Priority: ${priorityLabels[selectedTaskForDetails.priority]}`}
                    color={selectedTaskForDetails.priority === 3 ? 'error' :
                           selectedTaskForDetails.priority === 2 ? 'warning' : 'default'}
                    size="small"
                  />
                </Box>
              </Box>

              {/* Task Information Grid */}
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
                          Task ID
                        </Typography>
                        <Typography variant="body1">
                          {selectedTaskForDetails.id}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Task Name
                        </Typography>
                        <Typography variant="body1">
                          {selectedTaskForDetails.taskName}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Assigned To
                        </Typography>
                        <Box display="flex" alignItems="center" gap={1}>
                          <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem' }}>
                            {selectedTaskForDetails.assignedTo.split(' ').map(n => n[0]).join('').toUpperCase()}
                          </Avatar>
                          <Typography variant="body1">
                            {selectedTaskForDetails.assignedTo}
                          </Typography>
                        </Box>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* Timeline & Priority */}
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, height: '100%' }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Timeline & Priority
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Created Date
                        </Typography>
                        <Typography variant="body1">
                          {new Date(selectedTaskForDetails.createdAt).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Due Date
                        </Typography>
                        <Typography variant="body1">
                          {new Date(selectedTaskForDetails.dueDate).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Priority Level
                        </Typography>
                        <Chip
                          label={priorityLabels[selectedTaskForDetails.priority]}
                          color={selectedTaskForDetails.priority === 3 ? 'error' :
                                 selectedTaskForDetails.priority === 2 ? 'warning' : 'default'}
                          size="small"
                        />
                      </Box>
                    </Box>
                  </Paper>
                </Grid>

                {/* Task Description */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Task Description
                    </Typography>
                    <Typography variant="body1" sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                      {selectedTaskForDetails.taskDescription}
                    </Typography>
                  </Paper>
                </Grid>

                {/* Progress & Status */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom color="primary">
                      Progress & Status
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="subtitle1">Current Status</Typography>
                        <Chip
                          label={selectedTaskForDetails.taskStatus}
                          color={selectedTaskForDetails.taskStatus === 'COMPLETED' ? 'success' :
                                 selectedTaskForDetails.taskStatus === 'IN_PROGRESS' ? 'warning' : 'default'}
                        />
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                          Progress
                        </Typography>
                        <LinearProgress
                          variant="determinate"
                          value={selectedTaskForDetails.taskStatus === 'COMPLETED' ? 100 :
                                 selectedTaskForDetails.taskStatus === 'IN_PROGRESS' ? 50 : 0}
                          sx={{ height: 8, borderRadius: 1 }}
                        />
                        <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5, display: 'block' }}>
                          {selectedTaskForDetails.taskStatus === 'COMPLETED' ? '100% Complete' :
                           selectedTaskForDetails.taskStatus === 'IN_PROGRESS' ? '50% In Progress' : '0% Not Started'}
                        </Typography>
                      </Box>
                    </Box>
                  </Paper>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            onClick={() => {
              handleEditTask(selectedTaskForDetails);
              setTaskDetailsOpen(false);
            }}
          >
            Edit Task
          </Button>
          <Button onClick={() => setTaskDetailsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
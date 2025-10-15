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
  Assignment as TaskIcon,
  Check as ApproveIcon,
  Close as DeclineIcon
} from '@mui/icons-material';
import taskService from '../../services/taskService';
import documentService from '../../services/documentService';
import changeService from '../../services/changeService';

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

const TaskCard = ({ task, onEdit, onDelete, onStatusChange, onClick, onDragStart, draggable = false }) => {
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
      draggable={draggable}
      onDragStart={(e) => onDragStart && onDragStart(e, task)}
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        cursor: draggable ? 'grab' : 'pointer',
        '&:hover': {
          boxShadow: 4,
          transform: 'translateY(-2px)',
          transition: 'all 0.2s ease-in-out'
        },
        '&:active': draggable ? {
          cursor: 'grabbing'
        } : {}
      }}
      onClick={() => onClick && onClick(task)}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold', fontSize: '1.1rem' }}>
            {task.name}
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
          {task.description}
        </Typography>

        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              U
            </Avatar>
            <Typography variant="body2">User ID: {task.userId}</Typography>
          </Box>
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
  const [tasks, setTasks] = useState([]);
  const [filteredTasks, setFilteredTasks] = useState([]);
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
  const [loading, setLoading] = useState(true);
  const [kanbanGroupBy, setKanbanGroupBy] = useState('status'); // 'status' or 'type'

  // Fetch tasks from API on component mount
  useEffect(() => {
    const fetchTasks = async () => {
      try {
        setLoading(true);
        const response = await taskService.getAllTasks();
        console.log('Loaded tasks from API:', response);
        setTasks(response);
      } catch (error) {
        console.error('Failed to load tasks:', error);
        // Fallback to empty array on error
        setTasks([]);
      } finally {
        setLoading(false);
      }
    };

    fetchTasks();
  }, []);

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

  const handleStatusChange = async (task, newStatus) => {
    try {
      await taskService.updateTaskStatus(task.id, newStatus);
      // Refresh tasks
      const response = await taskService.getAllTasks();
      setTasks(response);
    } catch (error) {
      console.error('Error changing task status:', error);
      alert('Failed to update task status: ' + error.message);
    }
  };

  const handleDragStart = (e, task) => {
    e.dataTransfer.setData('taskId', task.id.toString());
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = async (e, newStatus) => {
    e.preventDefault();
    const taskId = e.dataTransfer.getData('taskId');
    const task = tasks.find(t => t.id.toString() === taskId);

    if (task && task.taskStatus !== newStatus) {
      await handleStatusChange(task, newStatus);
    }
  };

  const extractDocumentIdFromTask = (task) => {
    // Try to extract document ID from task name which has format: "Review Document: masterId version [documentId]"
    let match = task.name.match(/\[([^\]]+)\]/);
    if (match) {
      return match[1];
    }

    // Fallback: try to extract from description which has format: "... Document ID: xxx"
    if (task.description) {
      match = task.description.match(/Document ID:\s*([a-f0-9-]+)/i);
      if (match) {
        return match[1];
      }
    }

    return null;
  };

  const extractChangeIdFromTask = (task) => {
    // Try to extract change ID from description which has format: "Please review change {changeId} - ..."
    if (task.description) {
      const match = task.description.match(/review change\s+([a-f0-9-]+)/i);
      if (match) {
        return match[1];
      }
    }
    return null;
  };

  const isChangeReviewTask = (task) => {
    return task.name && task.name.startsWith('Review Change:');
  };

  const handleApproveReview = async () => {
    try {
      // Check if this is a change review task or document review task
      if (isChangeReviewTask(selectedTaskForDetails)) {
        // Handle change approval
        const changeId = extractChangeIdFromTask(selectedTaskForDetails);
        if (!changeId) {
          alert('Cannot find change ID in task');
          return;
        }

        await changeService.approveChange(changeId);

        // Mark task as completed
        await taskService.updateTaskStatus(selectedTaskForDetails.id, 'COMPLETED');

        alert('Change approved successfully!');
        setTaskDetailsOpen(false);

        // Refresh tasks
        const response = await taskService.getAllTasks();
        setTasks(response);
      } else {
        // Handle document approval
        const documentId = extractDocumentIdFromTask(selectedTaskForDetails);
        if (!documentId) {
          alert('Cannot find document ID in task');
          return;
        }

        await documentService.completeReview(documentId, true, 'Current User', 'Approved');

        // Mark task as completed
        await taskService.updateTaskStatus(selectedTaskForDetails.id, 'COMPLETED');

        alert('Document approved successfully! Status changed to RELEASED.');
        setTaskDetailsOpen(false);

        // Refresh tasks
        const response = await taskService.getAllTasks();
        setTasks(response);
      }
    } catch (error) {
      console.error('Error approving review:', error);
      alert('Failed to approve: ' + error.message);
    }
  };

  const handleDeclineReview = async () => {
    try {
      // Check if this is a change review task or document review task
      if (isChangeReviewTask(selectedTaskForDetails)) {
        // For changes, we don't have a reject API yet, so just mark task as completed
        await taskService.updateTaskStatus(selectedTaskForDetails.id, 'COMPLETED');
        alert('Change review task marked as completed (rejection not yet implemented)');
        setTaskDetailsOpen(false);

        // Refresh tasks
        const response = await taskService.getAllTasks();
        setTasks(response);
        return;
      }

      const documentId = extractDocumentIdFromTask(selectedTaskForDetails);
      if (!documentId) {
        alert('Cannot find document ID in task');
        return;
      }

      await documentService.completeReview(documentId, false, 'Current User', 'Declined');

      // Mark task as completed even when declined
      await taskService.updateTaskStatus(selectedTaskForDetails.id, 'COMPLETED');

      alert('Document declined. Status remains IN_WORK.');
      setTaskDetailsOpen(false);

      // Refresh tasks
      const response = await taskService.getAllTasks();
      setTasks(response);
    } catch (error) {
      console.error('Error declining document:', error);
      alert('Failed to decline document: ' + error.message);
    }
  };

  const getTasksByStatus = (status) => {
    return filteredTasks.filter(task => task.taskStatus === status);
  };

  const getTasksByType = (type) => {
    return filteredTasks.filter(task => {
      // Extract task type from task name if taskType field doesn't exist
      if (task.taskType) {
        return task.taskType === type;
      }
      // Fallback: check task name for keywords
      const taskName = task.name || task.taskName || '';
      return taskName.toLowerCase().includes(type.toLowerCase());
    });
  };

  const getUniqueTaskTypes = () => {
    const types = new Set();
    filteredTasks.forEach(task => {
      if (task.taskType) {
        types.add(task.taskType);
      } else {
        // Extract type from task name
        const taskName = task.name || task.taskName || '';
        if (taskName.toLowerCase().includes('review')) {
          types.add('REVIEW');
        } else if (taskName.toLowerCase().includes('validation')) {
          types.add('VALIDATION');
        } else if (taskName.toLowerCase().includes('approval')) {
          types.add('APPROVAL');
        } else {
          types.add('GENERAL');
        }
      }
    });
    return Array.from(types);
  };

  const renderTaskGrid = (taskList) => (
    <Box sx={{ maxHeight: 'calc(100vh - 300px)', overflowY: 'auto' }}>
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
    </Box>
  );

  const renderKanbanBoard = () => {
    if (kanbanGroupBy === 'type') {
      // Group by task type
      const taskTypes = getUniqueTaskTypes();
      const typeLabels = {
        'REVIEW': 'Review Tasks',
        'VALIDATION': 'Validation Tasks',
        'APPROVAL': 'Approval Tasks',
        'DOCUMENTATION': 'Documentation',
        'GENERAL': 'General Tasks'
      };
      const typeColors = {
        'REVIEW': 'primary.light',
        'VALIDATION': 'secondary.light',
        'APPROVAL': 'success.light',
        'DOCUMENTATION': 'info.light',
        'GENERAL': 'grey.400'
      };

      return (
        <Grid container spacing={2}>
          {taskTypes.map((type) => (
            <Grid item xs={12} md={taskTypes.length > 2 ? 4 : 6} key={type}>
              <Paper
                sx={{
                  p: 2,
                  height: 'calc(100vh - 300px)',
                  bgcolor: 'grey.50',
                  display: 'flex',
                  flexDirection: 'column',
                  border: 2,
                  borderColor: typeColors[type] || 'grey.400'
                }}
              >
                <Box sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  mb: 2,
                  p: 1,
                  bgcolor: typeColors[type] || 'grey.400',
                  borderRadius: 1
                }}>
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    {typeLabels[type] || type}
                  </Typography>
                  <Chip
                    label={getTasksByType(type).length}
                    size="small"
                    sx={{ bgcolor: 'white', fontWeight: 'bold' }}
                  />
                </Box>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, overflowY: 'auto', flex: 1, pr: 1 }}>
                  {getTasksByType(type).length === 0 ? (
                    <Box sx={{
                      textAlign: 'center',
                      py: 4,
                      color: 'text.secondary',
                      border: '2px dashed',
                      borderColor: 'grey.300',
                      borderRadius: 1
                    }}>
                      <TaskIcon sx={{ fontSize: 48, opacity: 0.3, mb: 1 }} />
                      <Typography variant="body2">
                        No {(typeLabels[type] || type).toLowerCase()}
                      </Typography>
                    </Box>
                  ) : (
                    getTasksByType(type).map((task) => (
                      <TaskCard
                        key={task.id}
                        task={task}
                        onEdit={handleEditTask}
                        onDelete={handleDeleteTask}
                        onStatusChange={handleStatusChange}
                        onClick={handleTaskClick}
                      />
                    ))
                  )}
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      );
    }

    // Group by status (default)
    const statuses = ['TODO', 'IN_PROGRESS', 'COMPLETED'];
    const statusLabels = {
      'TODO': 'To Do',
      'IN_PROGRESS': 'In Progress',
      'COMPLETED': 'Completed'
    };
    const statusColors = {
      'TODO': 'info.light',
      'IN_PROGRESS': 'warning.light',
      'COMPLETED': 'success.light'
    };

    return (
      <Grid container spacing={2} sx={{ height: 'calc(100vh - 200px)' }}>
        {statuses.map((status) => (
          <Grid item xs={12} md={4} key={status} sx={{ height: '100%', display: 'flex' }}>
            <Paper
              onDragOver={handleDragOver}
              onDrop={(e) => handleDrop(e, status)}
              sx={{
                p: 2,
                height: '100%',
                width: '100%',
                bgcolor: 'grey.50',
                display: 'flex',
                flexDirection: 'column',
                border: 2,
                borderColor: statusColors[status]
              }}
            >
              <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                mb: 2,
                p: 1,
                bgcolor: statusColors[status],
                borderRadius: 1
              }}>
                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                  {statusLabels[status]}
                </Typography>
                <Chip
                  label={getTasksByStatus(status).length}
                  size="small"
                  sx={{ bgcolor: 'white', fontWeight: 'bold' }}
                />
              </Box>
              <Box
                sx={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 2,
                  overflowY: 'auto',
                  flex: 1,
                  pr: 1,
                  minHeight: 0
                }}
                onDragOver={handleDragOver}
                onDrop={(e) => handleDrop(e, status)}
              >
                {getTasksByStatus(status).length === 0 ? (
                  <Box sx={{
                    textAlign: 'center',
                    py: 4,
                    color: 'text.secondary',
                    border: '2px dashed',
                    borderColor: 'grey.300',
                    borderRadius: 1
                  }}>
                    <TaskIcon sx={{ fontSize: 48, opacity: 0.3, mb: 1 }} />
                    <Typography variant="body2">
                      No {statusLabels[status].toLowerCase()} tasks
                    </Typography>
                  </Box>
                ) : (
                  getTasksByStatus(status).map((task) => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      onEdit={handleEditTask}
                      onDelete={handleDeleteTask}
                      onStatusChange={handleStatusChange}
                      onClick={handleTaskClick}
                      draggable={true}
                      onDragStart={handleDragStart}
                    />
                  ))
                )}
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
                  {selectedTaskForDetails.name}
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Task ID: {selectedTaskForDetails.id}
                </Typography>
              </Box>

              {/* Task Information */}
              <Grid container spacing={3}>
                {/* Basic Information */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
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
                          {selectedTaskForDetails.name}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Assigned To User ID
                        </Typography>
                        <Typography variant="body1">
                          {selectedTaskForDetails.userId}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="subtitle2" color="textSecondary">
                          Task Status
                        </Typography>
                        <Chip
                          label={selectedTaskForDetails.taskStatus || 'TODO'}
                          color={
                            selectedTaskForDetails.taskStatus === 'COMPLETED' ? 'success' :
                            selectedTaskForDetails.taskStatus === 'IN_PROGRESS' ? 'warning' :
                            'default'
                          }
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
                      {selectedTaskForDetails.description}
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {selectedTaskForDetails && selectedTaskForDetails.taskStatus !== 'COMPLETED' && (
            <>
              <Button
                variant="contained"
                color="success"
                startIcon={<ApproveIcon />}
                onClick={handleApproveReview}
              >
                Approve
              </Button>
              <Button
                variant="contained"
                color="error"
                startIcon={<DeclineIcon />}
                onClick={handleDeclineReview}
              >
                Decline
              </Button>
            </>
          )}
          <Button onClick={() => setTaskDetailsOpen(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
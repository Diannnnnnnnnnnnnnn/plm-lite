import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  Avatar,
  AvatarGroup,
  Button,
  IconButton,
  Divider,
  TextField,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  Paper,
  Grid,
  LinearProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip
} from '@mui/material';
import {
  Edit as EditIcon,
  AttachFile as AttachFileIcon,
  Download as DownloadIcon,
  Comment as CommentIcon,
  Person as PersonIcon,
  AccessTime as TimeIcon,
  Flag as PriorityIcon,
  CheckCircle as CheckCircleIcon,
  Schedule as ScheduleIcon,
  Send as SendIcon,
  Close as CloseIcon
} from '@mui/icons-material';

const TaskDetails = ({ task, open, onClose, onUpdate }) => {
  const [editMode, setEditMode] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [editedTask, setEditedTask] = useState(task || {});

  const mockComments = [
    {
      id: 1,
      author: 'John Doe',
      avatar: 'JD',
      content: 'Started working on the design review. Will have initial feedback by tomorrow.',
      timestamp: '2024-01-17 14:30',
      attachments: []
    },
    {
      id: 2,
      author: 'Jane Smith',
      avatar: 'JS',
      content: 'Added the latest CAD files for review. Please check the tolerances on components 5-8.',
      timestamp: '2024-01-17 16:45',
      attachments: ['CAD_Assembly_v2.dwg', 'Tolerance_Report.pdf']
    },
    {
      id: 3,
      author: 'Mike Johnson',
      avatar: 'MJ',
      content: 'Found some issues with the material specifications. Creating a detailed report.',
      timestamp: '2024-01-18 09:15',
      attachments: []
    }
  ];

  const mockAttachments = [
    { id: 1, name: 'Motor_Assembly_Drawing.pdf', size: '2.4 MB', type: 'pdf' },
    { id: 2, name: 'Design_Specifications.docx', size: '1.8 MB', type: 'doc' },
    { id: 3, name: 'Material_List.xlsx', size: '456 KB', type: 'excel' }
  ];

  const statusColors = {
    'Todo': 'default',
    'In Progress': 'primary',
    'Completed': 'success',
    'On Hold': 'warning',
    'Cancelled': 'error'
  };

  const priorityColors = {
    'Low': 'success',
    'Medium': 'warning',
    'High': 'error'
  };

  if (!task) return null;

  const handleSave = () => {
    onUpdate(editedTask);
    setEditMode(false);
  };

  const handleAddComment = () => {
    if (newComment.trim()) {
      // Add comment logic here
      setNewComment('');
    }
  };

  const getFileIcon = (type) => {
    return <AttachFileIcon color="action" />;
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h5">{task.title}</Typography>
          <Box>
            <IconButton onClick={() => setEditMode(!editMode)}>
              <EditIcon />
            </IconButton>
            <IconButton onClick={onClose}>
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Grid container spacing={3}>
          {/* Main Content */}
          <Grid item xs={12} md={8}>
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>Description</Typography>
                {editMode ? (
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    value={editedTask.description}
                    onChange={(e) => setEditedTask({...editedTask, description: e.target.value})}
                  />
                ) : (
                  <Typography variant="body1" paragraph>
                    {task.description}
                  </Typography>
                )}

                <Box mt={3}>
                  <Typography variant="h6" gutterBottom>Progress</Typography>
                  <Box display="flex" alignItems="center" gap={2}>
                    <LinearProgress
                      variant="determinate"
                      value={task.progress}
                      sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
                    />
                    <Typography variant="body2" color="textSecondary">
                      {task.progress}%
                    </Typography>
                  </Box>
                </Box>

                <Box mt={3}>
                  <Typography variant="h6" gutterBottom>Tags</Typography>
                  <Box display="flex" flexWrap="wrap" gap={1}>
                    {task.tags.map((tag, index) => (
                      <Chip key={index} label={tag} variant="outlined" size="small" />
                    ))}
                  </Box>
                </Box>
              </CardContent>
            </Card>

            {/* Attachments */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Attachments ({mockAttachments.length})
                </Typography>
                <List>
                  {mockAttachments.map((file) => (
                    <ListItem key={file.id}>
                      <ListItemAvatar>
                        <Avatar>
                          {getFileIcon(file.type)}
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={file.name}
                        secondary={file.size}
                      />
                      <ListItemSecondaryAction>
                        <IconButton>
                          <DownloadIcon />
                        </IconButton>
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>
              </CardContent>
            </Card>

            {/* Comments */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Comments ({mockComments.length})
                </Typography>

                {/* Add Comment */}
                <Box display="flex" gap={2} mb={3}>
                  <TextField
                    fullWidth
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    multiline
                    rows={2}
                  />
                  <Button
                    variant="contained"
                    onClick={handleAddComment}
                    disabled={!newComment.trim()}
                    startIcon={<SendIcon />}
                  >
                    Send
                  </Button>
                </Box>

                <Divider sx={{ mb: 2 }} />

                {/* Comments List */}
                <List>
                  {mockComments.map((comment, index) => (
                    <Box key={comment.id}>
                      <ListItem alignItems="flex-start">
                        <ListItemAvatar>
                          <Avatar>{comment.avatar}</Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={
                            <Box display="flex" justifyContent="space-between" alignItems="center">
                              <Typography variant="subtitle2">{comment.author}</Typography>
                              <Typography variant="caption" color="textSecondary">
                                {comment.timestamp}
                              </Typography>
                            </Box>
                          }
                          secondary={
                            <Box>
                              <Typography variant="body2" sx={{ mt: 1 }}>
                                {comment.content}
                              </Typography>
                              {comment.attachments.length > 0 && (
                                <Box mt={1}>
                                  {comment.attachments.map((attachment, idx) => (
                                    <Chip
                                      key={idx}
                                      label={attachment}
                                      size="small"
                                      icon={<AttachFileIcon />}
                                      sx={{ mr: 1, mt: 0.5 }}
                                    />
                                  ))}
                                </Box>
                              )}
                            </Box>
                          }
                        />
                      </ListItem>
                      {index < mockComments.length - 1 && <Divider />}
                    </Box>
                  ))}
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Sidebar */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 2, mb: 2 }}>
              <Typography variant="h6" gutterBottom>Task Details</Typography>

              {/* Status */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Status</Typography>
                {editMode ? (
                  <FormControl fullWidth size="small">
                    <Select
                      value={editedTask.status}
                      onChange={(e) => setEditedTask({...editedTask, status: e.target.value})}
                    >
                      <MenuItem value="Todo">Todo</MenuItem>
                      <MenuItem value="In Progress">In Progress</MenuItem>
                      <MenuItem value="Completed">Completed</MenuItem>
                      <MenuItem value="On Hold">On Hold</MenuItem>
                    </Select>
                  </FormControl>
                ) : (
                  <Chip
                    label={task.status}
                    color={statusColors[task.status]}
                    size="small"
                  />
                )}
              </Box>

              {/* Priority */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Priority</Typography>
                {editMode ? (
                  <FormControl fullWidth size="small">
                    <Select
                      value={editedTask.priority}
                      onChange={(e) => setEditedTask({...editedTask, priority: e.target.value})}
                    >
                      <MenuItem value="Low">Low</MenuItem>
                      <MenuItem value="Medium">Medium</MenuItem>
                      <MenuItem value="High">High</MenuItem>
                    </Select>
                  </FormControl>
                ) : (
                  <Chip
                    label={task.priority}
                    color={priorityColors[task.priority]}
                    size="small"
                    icon={<PriorityIcon />}
                  />
                )}
              </Box>

              {/* Assignee */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Assignee</Typography>
                <Box display="flex" alignItems="center" gap={1}>
                  <Avatar sx={{ width: 32, height: 32 }}>{task.assignee.avatar}</Avatar>
                  <Typography variant="body2">{task.assignee.name}</Typography>
                </Box>
              </Box>

              {/* Collaborators */}
              {task.collaborators.length > 0 && (
                <Box mb={2}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>Collaborators</Typography>
                  <AvatarGroup max={4}>
                    {task.collaborators.map((collaborator, index) => (
                      <Tooltip key={index} title={collaborator.name}>
                        <Avatar sx={{ width: 32, height: 32 }}>{collaborator.avatar}</Avatar>
                      </Tooltip>
                    ))}
                  </AvatarGroup>
                </Box>
              )}

              {/* Due Date */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Due Date</Typography>
                {editMode ? (
                  <TextField
                    fullWidth
                    size="small"
                    type="date"
                    value={editedTask.dueDate}
                    onChange={(e) => setEditedTask({...editedTask, dueDate: e.target.value})}
                  />
                ) : (
                  <Typography variant="body2">{task.dueDate}</Typography>
                )}
              </Box>

              {/* Created Date */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Created</Typography>
                <Typography variant="body2">{task.createdDate}</Typography>
              </Box>

              {/* Category */}
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary" gutterBottom>Category</Typography>
                <Typography variant="body2">{task.category}</Typography>
              </Box>
            </Paper>

            {editMode && (
              <Box display="flex" gap={1}>
                <Button variant="contained" onClick={handleSave} fullWidth>
                  Save Changes
                </Button>
                <Button variant="outlined" onClick={() => setEditMode(false)} fullWidth>
                  Cancel
                </Button>
              </Box>
            )}
          </Grid>
        </Grid>
      </DialogContent>
    </Dialog>
  );
};

export default TaskDetails;
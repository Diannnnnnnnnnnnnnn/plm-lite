import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  Chip,
  Avatar,
  Alert,
  CircularProgress
} from '@mui/material';
import {
  ThumbUp as ApproveIcon,
  ThumbDown as RejectIcon,
  Description as DocumentIcon
} from '@mui/icons-material';
import taskService from '../../services/taskService';
import documentService from '../../services/documentService';

export default function ReviewTasks({ userId }) {
  const [reviewTasks, setReviewTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTask, setSelectedTask] = useState(null);
  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [reviewAction, setReviewAction] = useState(null); // 'APPROVED' or 'REJECTED'
  const [reviewComment, setReviewComment] = useState('');
  const [documentDetails, setDocumentDetails] = useState({});

  useEffect(() => {
    loadReviewTasks();
  }, [userId]);

  const loadReviewTasks = async () => {
    try {
      setLoading(true);
      const tasks = await taskService.getReviewTasks(userId);
      setReviewTasks(tasks);

      // Load document details for each task
      const docDetailsMap = {};
      for (const task of tasks) {
        if (task.contextType === 'DOCUMENT' && task.contextId) {
          try {
            const doc = await documentService.getDocumentById(task.contextId);
            docDetailsMap[task.contextId] = doc;
          } catch (err) {
            console.error(`Failed to load document ${task.contextId}:`, err);
          }
        }
      }
      setDocumentDetails(docDetailsMap);
      setError(null);
    } catch (err) {
      setError('Failed to load review tasks');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleReviewClick = (task, action) => {
    setSelectedTask(task);
    setReviewAction(action);
    setReviewDialogOpen(true);
  };

  const handleSubmitReview = async () => {
    if (!selectedTask || !reviewAction) return;

    try {
      // Step 1: Add signoff record (for tracking)
      await taskService.addTaskSignoff(
        selectedTask.id,
        userId,
        reviewAction,
        reviewComment
      );

      // Step 2: Update task status to COMPLETED with approved flag
      // This triggers the workflow to proceed!
      const isApproved = reviewAction === 'APPROVED';
      await taskService.updateTaskStatus(
        selectedTask.id,
        'COMPLETED',
        isApproved,  // ← KEY: Pass approval decision to workflow
        reviewComment
      );

      setReviewDialogOpen(false);
      setReviewComment('');
      setSelectedTask(null);
      setReviewAction(null);

      // Reload tasks
      await loadReviewTasks();
    } catch (err) {
      console.error('Failed to submit review:', err);
      setError('Failed to submit review. Please try again.');
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        {error}
      </Alert>
    );
  }

  if (reviewTasks.length === 0) {
    return (
      <Box textAlign="center" py={8}>
        <DocumentIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
        <Typography variant="h6" color="textSecondary">
          No pending reviews
        </Typography>
        <Typography variant="body2" color="textSecondary">
          You have no documents waiting for your review
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>
        Pending Reviews ({reviewTasks.length})
      </Typography>

      <Grid container spacing={3}>
        {reviewTasks.map((task) => {
          const document = documentDetails[task.contextId];

          return (
            <Grid item xs={12} md={6} key={task.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        {task.taskName}
                      </Typography>
                      <Typography variant="body2" color="textSecondary" paragraph>
                        {task.taskDescription}
                      </Typography>
                    </Box>
                    <Chip
                      label={task.taskStatus}
                      color={task.taskStatus === 'PENDING' ? 'warning' : 'default'}
                      size="small"
                    />
                  </Box>

                  {document && (
                    <Box
                      sx={{
                        p: 2,
                        mb: 2,
                        bgcolor: 'grey.50',
                        borderRadius: 1,
                        border: '1px solid',
                        borderColor: 'grey.200'
                      }}
                    >
                      <Typography variant="subtitle2" color="primary" gutterBottom>
                        Document Details
                      </Typography>
                      <Typography variant="body2">
                        <strong>Title:</strong> {document.title}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Version:</strong> {document.fullVersion}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Status:</strong> {document.status}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Creator:</strong> {document.creator}
                      </Typography>
                    </Box>
                  )}

                  <Box display="flex" alignItems="center" gap={1} mb={2}>
                    <Avatar sx={{ width: 28, height: 28, fontSize: '0.875rem' }}>
                      {task.assignedBy?.charAt(0)}
                    </Avatar>
                    <Typography variant="body2" color="textSecondary">
                      Requested by: {task.assignedBy}
                    </Typography>
                  </Box>

                  <Typography variant="body2" color="textSecondary" mb={2}>
                    Due: {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date'}
                  </Typography>

                  <Box display="flex" gap={1}>
                    <Button
                      variant="contained"
                      color="success"
                      size="small"
                      startIcon={<ApproveIcon />}
                      onClick={() => handleReviewClick(task, 'APPROVED')}
                      fullWidth
                    >
                      Approve
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      size="small"
                      startIcon={<RejectIcon />}
                      onClick={() => handleReviewClick(task, 'REJECTED')}
                      fullWidth
                    >
                      Reject
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* Review Dialog */}
      <Dialog
        open={reviewDialogOpen}
        onClose={() => setReviewDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {reviewAction === 'APPROVED' ? 'Approve Review' : 'Reject Review'}
        </DialogTitle>
        <DialogContent>
          {selectedTask && (
            <Box sx={{ pt: 2 }}>
              <Typography variant="body1" gutterBottom>
                <strong>Task:</strong> {selectedTask.taskName}
              </Typography>
              <Typography variant="body2" color="textSecondary" paragraph>
                {selectedTask.taskDescription}
              </Typography>

              <TextField
                fullWidth
                label="Comments"
                multiline
                rows={4}
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                placeholder="Enter your review comments here..."
                sx={{ mt: 2 }}
              />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReviewDialogOpen(false)}>
            Cancel
          </Button>
          <Button
            variant="contained"
            color={reviewAction === 'APPROVED' ? 'success' : 'error'}
            onClick={handleSubmitReview}
          >
            Submit {reviewAction === 'APPROVED' ? 'Approval' : 'Rejection'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

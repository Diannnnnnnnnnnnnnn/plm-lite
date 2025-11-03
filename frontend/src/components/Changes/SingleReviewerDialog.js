import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Box,
  Typography
} from '@mui/material';
import userService from '../../services/userService';

export default function SingleReviewerDialog({ open, onClose, onSubmit }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedReviewer, setSelectedReviewer] = useState('');

  useEffect(() => {
    if (open) {
      loadUsers();
    }
  }, [open]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const userData = await userService.getAllUsers();
      setUsers(userData || []);
    } catch (error) {
      console.error('Error loading users:', error);
      setError('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = () => {
    if (!selectedReviewer) {
      setError('Please select a reviewer');
      return;
    }

    // Return as array with single reviewer (for consistency with backend API)
    onSubmit([selectedReviewer]);
    handleClose();
  };

  const handleClose = () => {
    setSelectedReviewer('');
    setError(null);
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Typography variant="h6">Select Reviewer for Change</Typography>
        <Typography variant="body2" color="textSecondary">
          Choose one person to review this change request
        </Typography>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          
          {loading ? (
            <Box display="flex" justifyContent="center" py={3}>
              <CircularProgress />
            </Box>
          ) : (
            <FormControl fullWidth>
              <InputLabel>Reviewer *</InputLabel>
              <Select
                value={selectedReviewer}
                onChange={(e) => setSelectedReviewer(e.target.value)}
                label="Reviewer *"
                required
              >
                <MenuItem value="">
                  <em>Select a reviewer</em>
                </MenuItem>
                {users.map((user) => (
                  <MenuItem key={user.id} value={user.id.toString()}>
                    {user.username} ({user.email})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          <Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
            <Typography variant="body2" color="info.dark">
              <strong>Single-Stage Review:</strong> Changes only require one reviewer 
              to approve or reject. The selected reviewer will receive a task to review 
              this change request.
            </Typography>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button 
          variant="contained" 
          onClick={handleSubmit}
          disabled={!selectedReviewer || loading}
        >
          Submit for Review
        </Button>
      </DialogActions>
    </Dialog>
  );
}





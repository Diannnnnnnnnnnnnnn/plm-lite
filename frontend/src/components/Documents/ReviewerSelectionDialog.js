import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  Checkbox,
  ListItemIcon,
  TextField,
  CircularProgress,
  Alert
} from '@mui/material';
import userService from '../../services/userService';

const ReviewerSelectionDialog = ({ open, onClose, onSubmit, documentId }) => {
  const [users, setUsers] = useState([]);
  const [selectedReviewers, setSelectedReviewers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (open) {
      loadUsers();
    }
  }, [open]);

  const loadUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const allUsers = await userService.getAllUsers();
      setUsers(allUsers);
    } catch (err) {
      console.error('Error loading users:', err);
      setError('Failed to load users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleReviewer = (userId) => {
    setSelectedReviewers(prev => {
      if (prev.includes(userId)) {
        return prev.filter(id => id !== userId);
      } else {
        return [...prev, userId];
      }
    });
  };

  const handleSubmit = () => {
    if (selectedReviewers.length === 0) {
      setError('Please select at least one reviewer');
      return;
    }
    onSubmit(selectedReviewers);
    handleClose();
  };

  const handleClose = () => {
    setSelectedReviewers([]);
    setSearchTerm('');
    setError(null);
    onClose();
  };

  const filteredUsers = users.filter(user =>
    user.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Select Reviewers</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <TextField
          fullWidth
          placeholder="Search users..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ mb: 2 }}
          disabled={loading}
        />

        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '20px' }}>
            <CircularProgress />
          </div>
        ) : (
          <List sx={{ maxHeight: 400, overflow: 'auto' }}>
            {filteredUsers.length === 0 ? (
              <ListItem>
                <ListItemText primary="No users found" />
              </ListItem>
            ) : (
              filteredUsers.map((user) => (
                <ListItem
                  key={user.id}
                  button
                  onClick={() => handleToggleReviewer(user.id.toString())}
                >
                  <ListItemIcon>
                    <Checkbox
                      edge="start"
                      checked={selectedReviewers.includes(user.id.toString())}
                      tabIndex={-1}
                      disableRipple
                    />
                  </ListItemIcon>
                  <ListItemText
                    primary={user.name || user.username}
                    secondary={user.email || `User ID: ${user.id}`}
                  />
                </ListItem>
              ))
            )}
          </List>
        )}

        {selectedReviewers.length > 0 && (
          <Alert severity="info" sx={{ mt: 2 }}>
            {selectedReviewers.length} reviewer(s) selected
          </Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || selectedReviewers.length === 0}
        >
          Submit for Review
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ReviewerSelectionDialog;

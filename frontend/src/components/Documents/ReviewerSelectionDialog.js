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
  
  // NEW: Two-stage review support
  const [initialReviewer, setInitialReviewer] = useState(null);
  const [technicalReviewer, setTechnicalReviewer] = useState(null);

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

  // NEW: Select reviewer for specific stage
  const handleSelectInitialReviewer = (userId) => {
    setInitialReviewer(userId);
    if (technicalReviewer === userId) {
      setTechnicalReviewer(null); // Can't be both reviewers
    }
  };

  const handleSelectTechnicalReviewer = (userId) => {
    setTechnicalReviewer(userId);
    if (initialReviewer === userId) {
      setInitialReviewer(null); // Can't be both reviewers
    }
  };

  const handleSubmit = () => {
    // Two-stage review mode: both reviewers must be selected
    if (!initialReviewer || !technicalReviewer) {
      setError('Please select both Initial Reviewer and Technical Reviewer');
      return;
    }
    
    // Submit with two-stage review format
    onSubmit({
      initialReviewer: initialReviewer,
      technicalReviewer: technicalReviewer,
      twoStageReview: true
    });
    handleClose();
  };

  const handleClose = () => {
    setSelectedReviewers([]);
    setInitialReviewer(null);
    setTechnicalReviewer(null);
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
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>Select Reviewers - Two-Stage Review</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Alert severity="info" sx={{ mb: 2 }}>
          This document will go through a two-stage review process. Select one reviewer for each stage.
        </Alert>

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
          <div style={{ display: 'flex', gap: '16px' }}>
            {/* Initial Reviewer Column */}
            <div style={{ flex: 1 }}>
              <h3 style={{ margin: '0 0 8px 0', fontSize: '1rem', fontWeight: 600 }}>
                1️⃣ Initial Reviewer
              </h3>
              {initialReviewer && (
                <Alert severity="success" sx={{ mb: 1 }}>
                  Selected: {users.find(u => u.id.toString() === initialReviewer)?.name || initialReviewer}
                </Alert>
              )}
              <List sx={{ maxHeight: 300, overflow: 'auto', border: '1px solid #ddd', borderRadius: 1 }}>
                {filteredUsers.length === 0 ? (
                  <ListItem>
                    <ListItemText primary="No users found" />
                  </ListItem>
                ) : (
                  filteredUsers.map((user) => (
                    <ListItem
                      key={`initial-${user.id}`}
                      button
                      onClick={() => handleSelectInitialReviewer(user.id.toString())}
                      selected={initialReviewer === user.id.toString()}
                      sx={{
                        backgroundColor: initialReviewer === user.id.toString() ? 'primary.light' : 'inherit',
                        '&.Mui-selected': {
                          backgroundColor: 'primary.light',
                          '&:hover': {
                            backgroundColor: 'primary.main',
                          }
                        }
                      }}
                    >
                      <ListItemIcon>
                        <Checkbox
                          edge="start"
                          checked={initialReviewer === user.id.toString()}
                          tabIndex={-1}
                          disableRipple
                          disabled={technicalReviewer === user.id.toString()}
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
            </div>

            {/* Technical Reviewer Column */}
            <div style={{ flex: 1 }}>
              <h3 style={{ margin: '0 0 8px 0', fontSize: '1rem', fontWeight: 600 }}>
                2️⃣ Technical Reviewer
              </h3>
              {technicalReviewer && (
                <Alert severity="success" sx={{ mb: 1 }}>
                  Selected: {users.find(u => u.id.toString() === technicalReviewer)?.name || technicalReviewer}
                </Alert>
              )}
              <List sx={{ maxHeight: 300, overflow: 'auto', border: '1px solid #ddd', borderRadius: 1 }}>
                {filteredUsers.length === 0 ? (
                  <ListItem>
                    <ListItemText primary="No users found" />
                  </ListItem>
                ) : (
                  filteredUsers.map((user) => (
                    <ListItem
                      key={`technical-${user.id}`}
                      button
                      onClick={() => handleSelectTechnicalReviewer(user.id.toString())}
                      selected={technicalReviewer === user.id.toString()}
                      sx={{
                        backgroundColor: technicalReviewer === user.id.toString() ? 'secondary.light' : 'inherit',
                        '&.Mui-selected': {
                          backgroundColor: 'secondary.light',
                          '&:hover': {
                            backgroundColor: 'secondary.main',
                          }
                        }
                      }}
                    >
                      <ListItemIcon>
                        <Checkbox
                          edge="start"
                          checked={technicalReviewer === user.id.toString()}
                          tabIndex={-1}
                          disableRipple
                          disabled={initialReviewer === user.id.toString()}
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
            </div>
          </div>
        )}

        {initialReviewer && technicalReviewer && (
          <Alert severity="success" sx={{ mt: 2 }}>
            ✓ Review Path: {users.find(u => u.id.toString() === initialReviewer)?.name} (Initial) → {users.find(u => u.id.toString() === technicalReviewer)?.name} (Technical) → Approval
          </Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading || !initialReviewer || !technicalReviewer}
        >
          Submit for Two-Stage Review
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ReviewerSelectionDialog;

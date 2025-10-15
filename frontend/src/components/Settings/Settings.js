import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  Divider,
  Switch,
  FormControlLabel,
  Alert,
  Tab,
  Tabs,
  Avatar,
  IconButton
} from '@mui/material';
import {
  PhotoCamera as PhotoCameraIcon,
  Save as SaveIcon
} from '@mui/icons-material';

function TabPanel({ children, value, index }) {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

export default function Settings() {
  const [tabValue, setTabValue] = useState(0);
  const [saveMessage, setSaveMessage] = useState('');

  // Load user from localStorage
  const getStoredUser = () => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch (error) {
        console.error('Error parsing stored user:', error);
      }
    }
    return null;
  };

  const storedUser = getStoredUser();

  // Profile Settings
  const [profile, setProfile] = useState({
    name: storedUser?.name || (storedUser?.username ? storedUser.username.charAt(0).toUpperCase() + storedUser.username.slice(1) : ''),
    email: storedUser?.email || (storedUser?.username ? `${storedUser.username}@plm-lite.local` : ''),
    phone: storedUser?.phone || '',
    department: storedUser?.department || '',
    role: storedUser?.roles?.[0] || 'User',
    avatar: storedUser?.avatar || ''
  });

  // System Preferences
  const [preferences, setPreferences] = useState({
    emailNotifications: true,
    taskReminders: true,
    documentAlerts: true,
    weeklyReports: false,
    darkMode: false,
    language: 'English'
  });

  // Security Settings
  const [security, setSecurity] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setSaveMessage('');
  };

  const handleProfileChange = (field) => (event) => {
    setProfile({ ...profile, [field]: event.target.value });
  };

  const handlePreferenceToggle = (field) => (event) => {
    setPreferences({ ...preferences, [field]: event.target.checked });
  };

  const handleSecurityChange = (field) => (event) => {
    setSecurity({ ...security, [field]: event.target.value });
  };

  const handleSaveProfile = () => {
    try {
      // Get current user from localStorage
      const currentUser = getStoredUser();
      if (!currentUser) {
        setSaveMessage('Error: No user logged in');
        return;
      }

      // Update user object with new profile data
      const updatedUser = {
        ...currentUser,
        name: profile.name,
        email: profile.email,
        phone: profile.phone,
        department: profile.department,
        roles: [profile.role.toUpperCase()],
        avatar: profile.avatar
      };

      // Save back to localStorage
      localStorage.setItem('user', JSON.stringify(updatedUser));

      // TODO: Also sync with user service API when available
      console.log('Saving profile to user service:', updatedUser);

      setSaveMessage('Profile settings saved successfully! Please refresh to see changes in the header.');
      setTimeout(() => setSaveMessage(''), 5000);

      // Trigger a storage event to update other components
      window.dispatchEvent(new Event('storage'));
    } catch (error) {
      console.error('Error saving profile:', error);
      setSaveMessage('Error saving profile settings');
      setTimeout(() => setSaveMessage(''), 3000);
    }
  };

  const handleSavePreferences = () => {
    // TODO: Save to local storage or user preferences API
    console.log('Saving preferences:', preferences);
    localStorage.setItem('userPreferences', JSON.stringify(preferences));
    setSaveMessage('Preferences saved successfully!');
    setTimeout(() => setSaveMessage(''), 3000);
  };

  const handleChangePassword = () => {
    if (!security.currentPassword || !security.newPassword || !security.confirmPassword) {
      setSaveMessage('Please fill in all password fields');
      return;
    }

    if (security.newPassword !== security.confirmPassword) {
      setSaveMessage('New passwords do not match');
      return;
    }

    if (security.newPassword.length < 8) {
      setSaveMessage('Password must be at least 8 characters long');
      return;
    }

    // TODO: Connect to auth service API
    console.log('Changing password');
    setSaveMessage('Password changed successfully!');
    setSecurity({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    setTimeout(() => setSaveMessage(''), 3000);
  };

  const getUserInitials = (name) => {
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Settings
      </Typography>

      {saveMessage && (
        <Alert severity={saveMessage.includes('successfully') ? 'success' : 'error'} sx={{ mb: 2 }}>
          {saveMessage}
        </Alert>
      )}

      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange}>
            <Tab label="Profile" />
            <Tab label="Preferences" />
            <Tab label="Security" />
          </Tabs>
        </Box>

        {/* Profile Tab */}
        <TabPanel value={tabValue} index={0}>
          <CardContent>
            <Grid container spacing={3}>
              {/* Avatar Section */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Avatar
                    sx={{
                      width: 80,
                      height: 80,
                      bgcolor: 'primary.main',
                      fontSize: '2rem'
                    }}
                    src={profile.avatar}
                  >
                    {!profile.avatar && getUserInitials(profile.name)}
                  </Avatar>
                  <Box>
                    <Typography variant="h6">{profile.name}</Typography>
                    <Typography variant="body2" color="textSecondary" gutterBottom>
                      {profile.role}
                    </Typography>
                    <Button
                      variant="outlined"
                      size="small"
                      startIcon={<PhotoCameraIcon />}
                      component="label"
                    >
                      Change Photo
                      <input type="file" hidden accept="image/*" />
                    </Button>
                  </Box>
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Divider />
              </Grid>

              {/* Profile Fields */}
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Full Name"
                  value={profile.name}
                  onChange={handleProfileChange('name')}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Email"
                  type="email"
                  value={profile.email}
                  onChange={handleProfileChange('email')}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Phone"
                  value={profile.phone}
                  onChange={handleProfileChange('phone')}
                  placeholder="+1 (555) 123-4567"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Department"
                  value={profile.department}
                  onChange={handleProfileChange('department')}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Role"
                  value={profile.role}
                  onChange={handleProfileChange('role')}
                />
              </Grid>

              <Grid item xs={12}>
                <Button
                  variant="contained"
                  startIcon={<SaveIcon />}
                  onClick={handleSaveProfile}
                >
                  Save Profile
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </TabPanel>

        {/* Preferences Tab */}
        <TabPanel value={tabValue} index={1}>
          <CardContent>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Notifications
                </Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={preferences.emailNotifications}
                      onChange={handlePreferenceToggle('emailNotifications')}
                    />
                  }
                  label="Email Notifications"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mb: 2 }}>
                  Receive email notifications for important updates
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={preferences.taskReminders}
                      onChange={handlePreferenceToggle('taskReminders')}
                    />
                  }
                  label="Task Reminders"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mb: 2 }}>
                  Get reminded about pending tasks and deadlines
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={preferences.documentAlerts}
                      onChange={handlePreferenceToggle('documentAlerts')}
                    />
                  }
                  label="Document Alerts"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mb: 2 }}>
                  Receive alerts when documents require review
                </Typography>

                <FormControlLabel
                  control={
                    <Switch
                      checked={preferences.weeklyReports}
                      onChange={handlePreferenceToggle('weeklyReports')}
                    />
                  }
                  label="Weekly Reports"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mb: 2 }}>
                  Receive weekly activity summary reports
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <Divider />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Appearance
                </Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={preferences.darkMode}
                      onChange={handlePreferenceToggle('darkMode')}
                      disabled
                    />
                  }
                  label="Dark Mode (Coming Soon)"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mb: 2 }}>
                  Switch to dark theme for better visibility in low light
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <Button
                  variant="contained"
                  startIcon={<SaveIcon />}
                  onClick={handleSavePreferences}
                >
                  Save Preferences
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </TabPanel>

        {/* Security Tab */}
        <TabPanel value={tabValue} index={2}>
          <CardContent>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Change Password
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Ensure your password is at least 8 characters long and includes a mix of letters, numbers, and symbols.
                </Typography>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  type="password"
                  label="Current Password"
                  value={security.currentPassword}
                  onChange={handleSecurityChange('currentPassword')}
                />
              </Grid>
              <Grid item xs={12} md={6}></Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  type="password"
                  label="New Password"
                  value={security.newPassword}
                  onChange={handleSecurityChange('newPassword')}
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  type="password"
                  label="Confirm New Password"
                  value={security.confirmPassword}
                  onChange={handleSecurityChange('confirmPassword')}
                />
              </Grid>

              <Grid item xs={12}>
                <Button
                  variant="contained"
                  onClick={handleChangePassword}
                  color="primary"
                >
                  Change Password
                </Button>
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Two-Factor Authentication
                </Typography>
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Add an extra layer of security to your account (Coming Soon)
                </Typography>
                <Button variant="outlined" disabled>
                  Enable 2FA
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </TabPanel>
      </Card>
    </Box>
  );
}

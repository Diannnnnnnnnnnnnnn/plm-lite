import React, { useState } from 'react';
import {
  Box,
  Container,
  TextField,
  Button,
  Typography,
  Paper,
  Link,
  Alert,
  CircularProgress
} from '@mui/material';
import authService from '../../services/authService';

export default function Auth({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Authenticate with the backend service via API Gateway
      await authService.login(username, password);
      onLogin();
    } catch (err) {
      // If backend is not available or auth fails, provide demo access as fallback
      let demoUser = null;
      if (username === 'demo' && password === 'demo') {
        demoUser = { id: 1, username: 'demo', roles: ['USER'] };
      } else if (username === 'guodian' && password === 'password') {
        demoUser = { id: 2, username: 'guodian', roles: ['REVIEWER'] };
      } else if (username === 'labubu' && password === 'password') {
        demoUser = { id: 3, username: 'labubu', roles: ['EDITOR'] };
      } else if (username === 'vivi' && password === 'password') {
        demoUser = { id: 4, username: 'vivi', roles: ['APPROVER'] };
      }

      if (demoUser) {
        localStorage.setItem('user', JSON.stringify(demoUser));
        localStorage.setItem('jwt_token', 'demo_token');
        onLogin();
      } else {
        setError('Invalid username or password. Try demo/demo, guodian/password, labubu/password, or vivi/password for testing.');
        console.error('Login error:', err);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = () => {
    const demoUser = { id: 1, username: 'demo', roles: ['USER'] };
    localStorage.setItem('user', JSON.stringify(demoUser));
    onLogin();
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f5f5f5'
      }}
    >
      <Container maxWidth="sm">
        <Paper elevation={3} sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h3" component="h1" gutterBottom>
              PLM Lite
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Please sign in to continue
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Sign In'}
            </Button>
            <Button
              fullWidth
              variant="outlined"
              onClick={handleDemoLogin}
              sx={{ mb: 2 }}
              disabled={loading}
            >
              Demo Login
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Demo credentials: demo/demo, guodian/password (REVIEWER), labubu/password (EDITOR), vivi/password (APPROVER)
              </Typography>
              <Link href="#" variant="body2">
                Forgot password?
              </Link>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
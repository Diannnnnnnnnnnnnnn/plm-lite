import axios from 'axios';

// Use NGINX as the entry point (port 8111)
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8111';

console.log('[API Client] Base URL:', API_BASE_URL);

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000, // 60 seconds - increased to handle graph sync operations
});

// Request interceptor - Add JWT token
apiClient.interceptors.request.use(
  (config) => {
    // Don't add Authorization header to login/register endpoints
    const isAuthEndpoint = config.url && (
      config.url.includes('/auth/login') || 
      config.url.includes('/auth/register')
    );
    
    if (!isAuthEndpoint) {
      const token = localStorage.getItem('jwt_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log(`[API] Adding token to request: ${token.substring(0, 20)}...`);
      }
    } else {
      // Explicitly remove Authorization header for auth endpoints
      // Also check if it was set by default or previous interceptor
      delete config.headers.Authorization;
      delete config.headers.authorization; // lowercase variant
      // Double-check localStorage is not being read
      const token = localStorage.getItem('jwt_token');
      if (token) {
        console.warn(`[API] WARNING: Token exists in localStorage but should not be sent to auth endpoint: ${config.url}`);
      }
      console.log(`[API] Skipping token for auth endpoint: ${config.url}`);
      console.log(`[API] Authorization header after removal:`, config.headers.Authorization);
    }
    
    console.log(`[API] ${config.method.toUpperCase()} ${config.url}`);
    console.log(`[API] Headers:`, config.headers);
    return config;
  },
  (error) => {
    console.error('[API] Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - Handle 401 errors
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Don't handle 401 errors for login/register endpoints - let the component handle it
    const isAuthEndpoint = error.config && error.config.url && (
      error.config.url.includes('/auth/login') || 
      error.config.url.includes('/auth/register')
    );
    
    if (error.response && error.response.status === 401 && !isAuthEndpoint) {
      console.log('[API] Unauthorized - redirecting to login');
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user');
      // Only redirect if not already on login page
      if (window.location.pathname !== '/') {
        window.location.href = '/';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;




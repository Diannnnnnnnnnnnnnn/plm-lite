import apiClient from '../utils/apiClient';

class AuthService {
  async login(username, password) {
    try {
      // Clear any existing token before attempting login
      // Do this synchronously and wait a bit to ensure it's cleared
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user');
      sessionStorage.removeItem('jwt_token');
      sessionStorage.removeItem('user');
      
      // Small delay to ensure storage is cleared
      await new Promise(resolve => setTimeout(resolve, 50));
      
      console.log('[AuthService] Starting login for:', username);
      console.log('[AuthService] Token cleared, localStorage token:', localStorage.getItem('jwt_token'));
      
      // Make login request (interceptor will ensure no Authorization header is sent)
      const response = await apiClient.post('/auth/login', {
        username,
        password,
      });

      const { token } = response.data;
      
      // Store JWT token
      localStorage.setItem('jwt_token', token);
      
      // Decode JWT and create user object
      const payload = this.parseJwt(token);
      
      // Map JWT claims to user object format
      const userInfo = {
        id: payload.uid || payload.sub,
        username: payload.sub, // JWT subject is the username
        roles: payload.role ? [payload.role] : (payload.roles || [])
      };
      
      localStorage.setItem('user', JSON.stringify(userInfo));
      
      return userInfo;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  logout() {
    const token = localStorage.getItem('jwt_token');
    
    // Call backend logout endpoint with token (for blacklisting)
    // The interceptor will automatically add the Authorization header
    if (token) {
      try {
        apiClient.post('/auth/logout').catch(() => {
          // Ignore errors - we'll clear localStorage anyway
        });
      } catch (error) {
        // Ignore errors - we'll clear localStorage anyway
      }
    }
    
    // Remove tokens from localStorage
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (error) {
        console.error('Error parsing user data:', error);
        return null;
      }
    }
    return null;
  }

  getToken() {
    return localStorage.getItem('jwt_token');
  }

  isAuthenticated() {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded = this.parseJwt(token);
      const currentTime = Date.now() / 1000;
      return decoded.exp > currentTime;
    } catch (error) {
      return false;
    }
  }

  // Simple JWT parser (for demo - in production use jwt-decode library)
  parseJwt(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error parsing JWT:', error);
      return null;
    }
  }
}

const authServiceInstance = new AuthService();
export default authServiceInstance;


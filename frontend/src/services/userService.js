import apiClient from '../utils/apiClient';

class UserService {
  constructor() {
    this.api = apiClient;
  }

  async getAllUsers() {
    try {
      const response = await this.api.get('/api/users');
      return response.data;
    } catch (error) {
      console.error('Error fetching users:', error);
      throw error;
    }
  }

  async getUserById(id) {
    try {
      const response = await this.api.get(`/api/users/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching user ${id}:`, error);
      throw error;
    }
  }

  async createUser(userData) {
    try {
      const response = await this.api.post('/api/users', userData);
      return response.data;
    } catch (error) {
      console.error('Error creating user:', error);
      throw error;
    }
  }

  async updateUser(id, userData) {
    try {
      const response = await this.api.put(`/api/users/${id}`, userData);
      return response.data;
    } catch (error) {
      console.error(`Error updating user ${id}:`, error);
      throw error;
    }
  }

  async deleteUser(id) {
    try {
      await this.api.delete(`/api/users/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting user ${id}:`, error);
      throw error;
    }
  }

  async validateUser(username, password) {
    try {
      const response = await this.api.post('/api/users/validate', null, {
        params: { username, password }
      });
      return response.data;
    } catch (error) {
      console.error('Error validating user:', error);
      throw error;
    }
  }
}

const userServiceInstance = new UserService();
export default userServiceInstance;
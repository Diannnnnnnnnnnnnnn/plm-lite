import axios from 'axios';

const API_BASE_URL = 'http://localhost:8083';

class UserService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllUsers() {
    try {
      const response = await this.api.get('/users');
      return response.data;
    } catch (error) {
      console.error('Error fetching users:', error);
      throw error;
    }
  }

  async getUserById(id) {
    try {
      const response = await this.api.get(`/users/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching user ${id}:`, error);
      throw error;
    }
  }

  async createUser(userData) {
    try {
      const response = await this.api.post('/users', userData);
      return response.data;
    } catch (error) {
      console.error('Error creating user:', error);
      throw error;
    }
  }

  async updateUser(id, userData) {
    try {
      const response = await this.api.put(`/users/${id}`, userData);
      return response.data;
    } catch (error) {
      console.error(`Error updating user ${id}:`, error);
      throw error;
    }
  }

  async deleteUser(id) {
    try {
      await this.api.delete(`/users/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting user ${id}:`, error);
      throw error;
    }
  }

  async validateUser(username, password) {
    try {
      const response = await this.api.post('/users/validate', null, {
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
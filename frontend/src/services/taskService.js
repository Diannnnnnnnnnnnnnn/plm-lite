import axios from 'axios';

const API_BASE_URL = 'http://localhost:8082';

class TaskService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllTasks() {
    try {
      const response = await this.api.get('/tasks');
      console.log('getAllTasks response:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching tasks:', error);
      throw error;
    }
  }

  async getTaskById(id) {
    try {
      const response = await this.api.get(`/tasks/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching task ${id}:`, error);
      throw error;
    }
  }

  async createTask(taskData) {
    try {
      const response = await this.api.post('/tasks', taskData);
      return response.data;
    } catch (error) {
      console.error('Error creating task:', error);
      throw error;
    }
  }

  async updateTask(id, taskData) {
    try {
      const response = await this.api.put(`/tasks/${id}`, taskData);
      return response.data;
    } catch (error) {
      console.error(`Error updating task ${id}:`, error);
      throw error;
    }
  }

  async deleteTask(id) {
    try {
      await this.api.delete(`/tasks/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting task ${id}:`, error);
      throw error;
    }
  }

  async getTasksByStatus(status) {
    try {
      const response = await this.api.get(`/tasks?status=${status}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching tasks by status ${status}:`, error);
      throw error;
    }
  }

  async getTasksByAssignee(assignee) {
    try {
      const response = await this.api.get(`/tasks?assignedTo=${assignee}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching tasks for assignee ${assignee}:`, error);
      throw error;
    }
  }

  async getReviewTasks(userId) {
    try {
      const response = await this.api.get(`/api/tasks/review-tasks/${userId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching review tasks for user ${userId}:`, error);
      throw error;
    }
  }

  async getTasksByDocument(documentId) {
    try {
      const response = await this.api.get(`/api/tasks/by-document/${documentId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching tasks for document ${documentId}:`, error);
      throw error;
    }
  }

  async addTaskSignoff(taskId, userId, action, comments) {
    try {
      const response = await this.api.post(`/api/tasks/${taskId}/signoff`, {
        userId,
        action,
        comments
      });
      return response.data;
    } catch (error) {
      console.error(`Error adding signoff to task ${taskId}:`, error);
      throw error;
    }
  }

  async updateTaskStatus(taskId, status, approved = null, comments = null) {
    try {
      const payload = { status };
      
      // Add approved parameter if provided (for workflow integration)
      if (approved !== null) {
        payload.approved = approved ? 'true' : 'false';
      }
      
      // Add comments if provided
      if (comments) {
        payload.comments = comments;
      }
      
      console.log(`📤 Updating task ${taskId} status:`, payload);
      
      const response = await this.api.put(`/tasks/${taskId}/status`, payload);
      return response.data;
    } catch (error) {
      console.error(`Error updating task ${taskId} status:`, error);
      throw error;
    }
  }
}

const taskServiceInstance = new TaskService();
export default taskServiceInstance;
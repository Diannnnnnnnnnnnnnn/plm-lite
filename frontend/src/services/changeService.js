import axios from 'axios';

const API_BASE_URL = 'http://localhost:8084/api';

class ChangeService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllChanges() {
    try {
      const response = await this.api.get('/changes');
      return response.data;
    } catch (error) {
      console.error('Error fetching changes:', error);
      throw error;
    }
  }

  async getChangeById(id) {
    try {
      const response = await this.api.get(`/changes/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching change ${id}:`, error);
      throw error;
    }
  }

  async createChange(changeData) {
    try {
      const response = await this.api.post('/changes', changeData);
      return response.data;
    } catch (error) {
      console.error('Error creating change:', error);
      throw error;
    }
  }

  async submitForReview(changeId, reviewData) {
    try {
      const response = await this.api.put(`/changes/${changeId}/submit-review`, reviewData);
      return response.data;
    } catch (error) {
      console.error(`Error submitting change ${changeId} for review:`, error);
      throw error;
    }
  }

  async approveChange(changeId) {
    try {
      const response = await this.api.put(`/changes/${changeId}/approve`);
      return response.data;
    } catch (error) {
      console.error(`Error approving change ${changeId}:`, error);
      throw error;
    }
  }

  async getChangesByStatus(status) {
    try {
      const response = await this.api.get(`/changes/status/${status}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching changes by status ${status}:`, error);
      throw error;
    }
  }

  async getChangesByCreator(creator) {
    try {
      const response = await this.api.get(`/changes/creator/${creator}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching changes by creator ${creator}:`, error);
      throw error;
    }
  }

  async searchChanges(keyword) {
    try {
      const response = await this.api.get(`/changes/search?keyword=${encodeURIComponent(keyword)}`);
      return response.data;
    } catch (error) {
      console.error(`Error searching changes with keyword ${keyword}:`, error);
      throw error;
    }
  }

  async searchChangesElastic(query) {
    try {
      const response = await this.api.get(`/changes/search/elastic?query=${encodeURIComponent(query)}`);
      return response.data;
    } catch (error) {
      console.error(`Error performing elastic search with query ${query}:`, error);
      throw error;
    }
  }

  // Helper method to get changes with advanced filtering
  async getFilteredChanges(filters = {}) {
    try {
      let changes = await this.getAllChanges();

      if (filters.status && filters.status !== 'All') {
        changes = changes.filter(change => change.status === filters.status);
      }

      if (filters.stage && filters.stage !== 'All') {
        changes = changes.filter(change => change.stage === filters.stage);
      }

      if (filters.changeClass && filters.changeClass !== 'All') {
        changes = changes.filter(change => change.changeClass === filters.changeClass);
      }

      if (filters.creator) {
        changes = changes.filter(change =>
          change.creator.toLowerCase().includes(filters.creator.toLowerCase())
        );
      }

      if (filters.product) {
        changes = changes.filter(change =>
          change.product.toLowerCase().includes(filters.product.toLowerCase())
        );
      }

      if (filters.searchTerm) {
        const term = filters.searchTerm.toLowerCase();
        changes = changes.filter(change =>
          change.title.toLowerCase().includes(term) ||
          change.product.toLowerCase().includes(term) ||
          change.creator.toLowerCase().includes(term) ||
          change.changeReason.toLowerCase().includes(term) ||
          change.id.toLowerCase().includes(term)
        );
      }

      return changes;
    } catch (error) {
      console.error('Error filtering changes:', error);
      throw error;
    }
  }

  // Helper method to get change statistics
  async getChangeStatistics() {
    try {
      const changes = await this.getAllChanges();

      const stats = {
        total: changes.length,
        byStatus: {},
        byStage: {},
        byClass: {},
        recent: changes.filter(change => {
          const createdDate = new Date(change.createTime);
          const weekAgo = new Date();
          weekAgo.setDate(weekAgo.getDate() - 7);
          return createdDate >= weekAgo;
        }).length
      };

      // Calculate distribution by status
      changes.forEach(change => {
        stats.byStatus[change.status] = (stats.byStatus[change.status] || 0) + 1;
        stats.byStage[change.stage] = (stats.byStage[change.stage] || 0) + 1;
        stats.byClass[change.changeClass] = (stats.byClass[change.changeClass] || 0) + 1;
      });

      return stats;
    } catch (error) {
      console.error('Error calculating change statistics:', error);
      throw error;
    }
  }
}

const changeServiceInstance = new ChangeService();
export default changeServiceInstance;
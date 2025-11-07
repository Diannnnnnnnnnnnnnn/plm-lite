import apiClient from '../utils/apiClient';

class ChangeService {
  constructor() {
    this.api = apiClient;
  }

  async getAllChanges() {
    try {
      const response = await this.api.get('/api/changes');
      return response.data;
    } catch (error) {
      console.error('Error fetching changes:', error);
      throw error;
    }
  }

  async getChangeById(id) {
    try {
      const response = await this.api.get(`/api/changes/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching change ${id}:`, error);
      throw error;
    }
  }

  async createChange(changeData) {
    try {
      const response = await this.api.post('/api/changes', changeData);
      return response.data;
    } catch (error) {
      console.error('Error creating change:', error);
      throw error;
    }
  }

  async updateChange(id, changeData) {
    try {
      const response = await this.api.put(`/api/changes/${id}`, changeData);
      return response.data;
    } catch (error) {
      console.error(`Error updating change ${id}:`, error);
      throw error;
    }
  }

  async deleteChange(id) {
    try {
      await this.api.delete(`/api/changes/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting change ${id}:`, error);
      throw error;
    }
  }

  async getChangesByStatus(status) {
    try {
      const response = await this.api.get(`/api/changes?status=${status}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching changes by status ${status}:`, error);
      throw error;
    }
  }

  async submitChangeForApproval(id) {
    try {
      const response = await this.api.post(`/api/changes/${id}/submit`);
      return response.data;
    } catch (error) {
      console.error(`Error submitting change ${id}:`, error);
      throw error;
    }
  }

  async approveChange(id, approverId) {
    try {
      const response = await this.api.post(`/api/changes/${id}/approve`, {
        approverId,
      });
      return response.data;
    } catch (error) {
      console.error(`Error approving change ${id}:`, error);
      throw error;
    }
  }

  async rejectChange(id, approverId, reason) {
    try {
      const response = await this.api.post(`/api/changes/${id}/reject`, {
        approverId,
        reason,
      });
      return response.data;
    } catch (error) {
      console.error(`Error rejecting change ${id}:`, error);
      throw error;
    }
  }

  async implementChange(id) {
    try {
      const response = await this.api.post(`/api/changes/${id}/implement`);
      return response.data;
    } catch (error) {
      console.error(`Error implementing change ${id}:`, error);
      throw error;
    }
  }

  async getChangeHistory(id) {
    try {
      const response = await this.api.get(`/api/changes/${id}/history`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching change history ${id}:`, error);
      throw error;
    }
  }

  async getImpactedItems(id) {
    try {
      const response = await this.api.get(`/api/changes/${id}/impact`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching impacted items for change ${id}:`, error);
      throw error;
    }
  }

  async addImpactedDocument(changeId, documentId) {
    try {
      const response = await this.api.post(`/api/changes/${changeId}/documents/${documentId}`);
      return response.data;
    } catch (error) {
      console.error(`Error adding document to change ${changeId}:`, error);
      throw error;
    }
  }

  async removeImpactedDocument(changeId, documentId) {
    try {
      await this.api.delete(`/api/changes/${changeId}/documents/${documentId}`);
      return true;
    } catch (error) {
      console.error(`Error removing document from change ${changeId}:`, error);
      throw error;
    }
  }

  async addImpactedPart(changeId, partId) {
    try {
      const response = await this.api.post(`/api/changes/${changeId}/parts/${partId}`);
      return response.data;
    } catch (error) {
      console.error(`Error adding part to change ${changeId}:`, error);
      throw error;
    }
  }

  async removeImpactedPart(changeId, partId) {
    try {
      await this.api.delete(`/api/changes/${changeId}/parts/${partId}`);
      return true;
    } catch (error) {
      console.error(`Error removing part from change ${changeId}:`, error);
      throw error;
    }
  }

  async searchChanges(searchParams) {
    try {
      const response = await this.api.post('/api/changes/search', searchParams);
      return response.data;
    } catch (error) {
      console.error('Error searching changes:', error);
      throw error;
    }
  }
}

const changeServiceInstance = new ChangeService();
export default changeServiceInstance;

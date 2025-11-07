import apiClient from '../utils/apiClient';

class PartService {
  constructor() {
    this.api = apiClient;
  }

  async getAllParts() {
    try {
      const response = await this.api.get('/api/boms/parts');
      return response.data;
    } catch (error) {
      console.error('Error fetching parts:', error);
      throw error;
    }
  }

  async getPartById(id) {
    try {
      const response = await this.api.get(`/api/boms/parts/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching part ${id}:`, error);
      throw error;
    }
  }

  async createPart(partData) {
    try {
      const response = await this.api.post('/api/boms/parts', partData);
      return response.data;
    } catch (error) {
      console.error('Error creating part:', error);
      throw error;
    }
  }

  async updatePart(id, partData) {
    try {
      const response = await this.api.put(`/api/boms/parts/${id}`, partData);
      return response.data;
    } catch (error) {
      console.error(`Error updating part ${id}:`, error);
      throw error;
    }
  }

  async deletePart(id) {
    try {
      await this.api.delete(`/api/boms/parts/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting part ${id}:`, error);
      throw error;
    }
  }

  async getPartHierarchy(id) {
    try {
      const response = await this.api.get(`/api/boms/parts/${id}/bom-hierarchy`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching part hierarchy ${id}:`, error);
      throw error;
    }
  }

  async addPartUsage(parentPartId, childPartId, quantity) {
    try {
      const response = await this.api.post('/api/boms/parts/usage', {
        parentPartId,
        childPartId,
        quantity,
      });
      return response.data;
    } catch (error) {
      console.error('Error adding part usage:', error);
      throw error;
    }
  }

  async removePartUsage(parentPartId, childPartId) {
    try {
      await this.api.delete(`/api/boms/parts/${parentPartId}/usage/${childPartId}`);
      return true;
    } catch (error) {
      console.error('Error removing part usage:', error);
      throw error;
    }
  }

  async getChildParts(parentPartId) {
    try {
      const response = await this.api.get(`/api/boms/parts/${parentPartId}/children`);
      return response.data;
    } catch (error) {
      console.error('Error getting child parts:', error);
      throw error;
    }
  }

  async searchParts(searchParams) {
    try {
      const response = await this.api.post('/api/boms/parts/search', searchParams);
      return response.data;
    } catch (error) {
      console.error('Error searching parts:', error);
      throw error;
    }
  }

  async getPartsByDocument(documentId) {
    try {
      const response = await this.api.get(`/api/boms/parts/by-document/${documentId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching parts for document ${documentId}:`, error);
      throw error;
    }
  }

  async linkPartToDocument(partId, documentId) {
    try {
      const response = await this.api.post(`/api/boms/parts/${partId}/documents/${documentId}`);
      return response.data;
    } catch (error) {
      console.error(`Error linking part ${partId} to document ${documentId}:`, error);
      throw error;
    }
  }

  async unlinkPartFromDocument(partId, documentId) {
    try {
      await this.api.delete(`/api/boms/parts/${partId}/documents/${documentId}`);
      return true;
    } catch (error) {
      console.error(`Error unlinking part ${partId} from document ${documentId}:`, error);
      throw error;
    }
  }

  async getPartWhereUsed(partId) {
    try {
      const response = await this.api.get(`/api/boms/parts/${partId}/where-used`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching where-used for part ${partId}:`, error);
      throw error;
    }
  }
}

const partServiceInstance = new PartService();
export default partServiceInstance;

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8089/api/v1';

class PartService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllParts() {
    try {
      const response = await this.api.get('/parts');
      return response.data;
    } catch (error) {
      console.error('Error fetching parts:', error);
      throw error;
    }
  }

  async getPartById(id) {
    try {
      const response = await this.api.get(`/parts/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching part ${id}:`, error);
      throw error;
    }
  }

  async createPart(partData) {
    try {
      const response = await this.api.post('/parts', partData);
      return response.data;
    } catch (error) {
      console.error('Error creating part:', error);
      throw error;
    }
  }

  async updatePart(id, partData) {
    try {
      const response = await this.api.put(`/parts/${id}`, partData);
      return response.data;
    } catch (error) {
      console.error(`Error updating part ${id}:`, error);
      throw error;
    }
  }

  async deletePart(id) {
    try {
      await this.api.delete(`/parts/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting part ${id}:`, error);
      throw error;
    }
  }

  async getPartHierarchy(id) {
    try {
      const response = await this.api.get(`/parts/${id}/bom-hierarchy`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching part hierarchy ${id}:`, error);
      throw error;
    }
  }

  async addPartUsage(parentPartId, childPartId, quantity) {
    try {
      const response = await this.api.post('/parts/usage', {
        parentPartId,
        childPartId,
        quantity
      });
      return response.data;
    } catch (error) {
      console.error('Error adding part usage:', error);
      throw error;
    }
  }

  async removePartUsage(parentPartId, childPartId) {
    try {
      await this.api.delete(`/parts/${parentPartId}/usage/${childPartId}`);
      return true;
    } catch (error) {
      console.error('Error removing part usage:', error);
      throw error;
    }
  }

  async getChildParts(parentPartId) {
    try {
      const response = await this.api.get(`/parts/${parentPartId}/children`);
      return response.data;
    } catch (error) {
      console.error('Error getting child parts:', error);
      throw error;
    }
  }

  async getParentParts(childPartId) {
    try {
      const response = await this.api.get(`/parts/${childPartId}/parents`);
      return response.data;
    } catch (error) {
      console.error('Error getting parent parts:', error);
      throw error;
    }
  }

  async linkPartToDocument(linkData) {
    try {
      const response = await this.api.post('/parts/document-link', linkData);
      return response.data;
    } catch (error) {
      console.error('Error linking part to document:', error);
      throw error;
    }
  }

  async unlinkPartFromDocument(partId, documentId) {
    try {
      await this.api.delete(`/parts/${partId}/document/${documentId}`);
      return true;
    } catch (error) {
      console.error('Error unlinking part from document:', error);
      throw error;
    }
  }

  async getDocumentsForPart(partId) {
    try {
      const response = await this.api.get(`/parts/${partId}/documents`);
      return response.data;
    } catch (error) {
      console.error('Error getting documents for part:', error);
      throw error;
    }
  }

  async getPartsForDocument(documentId) {
    try {
      const response = await this.api.get(`/parts/document/${documentId}/parts`);
      return response.data;
    } catch (error) {
      console.error('Error getting parts for document:', error);
      throw error;
    }
  }
}

const partServiceInstance = new PartService();
export default partServiceInstance;




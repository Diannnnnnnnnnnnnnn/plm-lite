import apiClient from '../utils/apiClient';

class BomService {
  constructor() {
    this.api = apiClient;
  }

  // Legacy BOM methods now redirect to Part methods
  async getAllBoms() {
    return this.getAllParts();
  }

  async getBomById(id) {
    return this.getPartById(id);
  }

  async createBom(bomData) {
    // Convert BOM data to Part data format
    const partData = {
      title: bomData.description,
      description: bomData.description,
      stage: bomData.stage,
      level: 'ASSEMBLY',
      creator: bomData.creator
    };
    return this.createPart(partData);
  }

  async updateBom(id, bomData) {
    const partData = {
      title: bomData.description,
      description: bomData.description,
      stage: bomData.stage
    };
    return this.updatePart(id, partData);
  }

  async deleteBom(id) {
    return this.deletePart(id);
  }

  async getBomHierarchy(id) {
    return this.getPartHierarchy(id);
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

  async getPartById(id) {
    try {
      const response = await this.api.get(`/api/boms/parts/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching part ${id}:`, error);
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

  // Legacy method redirected to part usage
  async addPartToBom(bomId, partUsageData) {
    return this.addPartUsage(bomId, partUsageData.childPartId, partUsageData.quantity);
  }
}

const bomServiceInstance = new BomService();
export default bomServiceInstance;
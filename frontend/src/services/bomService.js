import axios from 'axios';

const API_BASE_URL = 'http://localhost:8089';

class BomService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllBoms() {
    try {
      const response = await this.api.get('/boms');
      return response.data;
    } catch (error) {
      console.error('Error fetching BOMs:', error);
      throw error;
    }
  }

  async getBomById(id) {
    try {
      const response = await this.api.get(`/boms/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching BOM ${id}:`, error);
      throw error;
    }
  }

  async createBom(bomData) {
    try {
      const response = await this.api.post('/boms', bomData);
      return response.data;
    } catch (error) {
      console.error('Error creating BOM:', error);
      throw error;
    }
  }

  async updateBom(id, bomData) {
    try {
      const response = await this.api.put(`/boms/${id}`, bomData);
      return response.data;
    } catch (error) {
      console.error(`Error updating BOM ${id}:`, error);
      throw error;
    }
  }

  async deleteBom(id) {
    try {
      await this.api.delete(`/boms/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting BOM ${id}:`, error);
      throw error;
    }
  }

  async getBomHierarchy(id) {
    try {
      const response = await this.api.get(`/boms/${id}/hierarchy`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching BOM hierarchy ${id}:`, error);
      throw error;
    }
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

  async createPart(partData) {
    try {
      const response = await this.api.post('/parts', partData);
      return response.data;
    } catch (error) {
      console.error('Error creating part:', error);
      throw error;
    }
  }

  async addPartToBom(bomId, partUsageData) {
    try {
      const response = await this.api.post(`/boms/${bomId}/parts`, partUsageData);
      return response.data;
    } catch (error) {
      console.error(`Error adding part to BOM ${bomId}:`, error);
      throw error;
    }
  }
}

const bomServiceInstance = new BomService();
export default bomServiceInstance;
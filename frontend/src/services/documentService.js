import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api/v1';

class DocumentService {
  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  async getAllDocuments() {
    try {
      const response = await this.api.get('/documents');
      return response.data;
    } catch (error) {
      console.error('Error fetching documents:', error);
      throw error;
    }
  }

  async getDocumentById(id) {
    try {
      const response = await this.api.get(`/documents/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching document ${id}:`, error);
      throw error;
    }
  }

  async createDocument(documentData) {
    try {
      const response = await this.api.post('/documents', documentData);
      return response.data;
    } catch (error) {
      console.error('Error creating document:', error);
      throw error;
    }
  }

  async updateDocument(id, documentData) {
    try {
      const response = await this.api.put(`/documents/${id}`, documentData);
      return response.data;
    } catch (error) {
      console.error(`Error updating document ${id}:`, error);
      throw error;
    }
  }

  async deleteDocument(id) {
    try {
      await this.api.delete(`/documents/${id}`);
      return true;
    } catch (error) {
      console.error(`Error deleting document ${id}:`, error);
      throw error;
    }
  }

  async uploadDocument(documentData, file, user = 'Current User') {
    try {
      // Step 1: Create the document first
      // Map frontend data to backend expected format
      // Convert frontend stage values to backend enum values
      const stageMapping = {
        'DESIGN': 'CONCEPTUAL_DESIGN',
        'DEVELOPMENT': 'PRELIMINARY_DESIGN',
        'REVIEW': 'DETAILED_DESIGN',
        'PRODUCTION': 'MANUFACTURING',
        'CONCEPTUAL_DESIGN': 'CONCEPTUAL_DESIGN',
        'PRELIMINARY_DESIGN': 'PRELIMINARY_DESIGN',
        'DETAILED_DESIGN': 'DETAILED_DESIGN',
        'MANUFACTURING': 'MANUFACTURING',
        'IN_SERVICE': 'IN_SERVICE',
        'RETIRED': 'RETIRED'
      };

      const backendDocumentData = {
        title: documentData.title,
        creator: user,
        stage: stageMapping[documentData.stage] || 'CONCEPTUAL_DESIGN',
        category: documentData.description || 'General',
        masterId: documentData.documentNumber || null
      };

      const createResponse = await this.api.post('/documents', backendDocumentData);
      const document = createResponse.data;

      // Step 2: Upload the file to the created document
      const formData = new FormData();
      formData.append('file', file);
      formData.append('user', user);

      const uploadResponse = await this.api.post(`/documents/${document.id}/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      // Return both document info and upload result
      return {
        document: document,
        fileKey: uploadResponse.data
      };
    } catch (error) {
      console.error('Error uploading document:', error);
      throw error;
    }
  }

  async downloadDocument(id) {
    try {
      const response = await this.api.get(`/documents/${id}/download`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      console.error(`Error downloading document ${id}:`, error);
      throw error;
    }
  }

  async getDocumentHistory(id) {
    try {
      const response = await this.api.get(`/documents/${id}/history`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching document history ${id}:`, error);
      throw error;
    }
  }

  async submitForReview(id, reviewData) {
    try {
      const response = await this.api.post(`/documents/${id}/submit-review`, reviewData);
      return response.data;
    } catch (error) {
      console.error(`Error submitting document ${id} for review:`, error);
      throw error;
    }
  }

  async approveDocument(id, approvalData) {
    try {
      const response = await this.api.post(`/documents/${id}/approve`, approvalData);
      return response.data;
    } catch (error) {
      console.error(`Error approving document ${id}:`, error);
      throw error;
    }
  }

  async rejectDocument(id, rejectionData) {
    try {
      const response = await this.api.post(`/documents/${id}/reject`, rejectionData);
      return response.data;
    } catch (error) {
      console.error(`Error rejecting document ${id}:`, error);
      throw error;
    }
  }

  async searchDocuments(searchParams) {
    try {
      const response = await this.api.post('/documents/search', searchParams);
      return response.data;
    } catch (error) {
      console.error('Error searching documents:', error);
      throw error;
    }
  }
}

const documentServiceInstance = new DocumentService();
export default documentServiceInstance;
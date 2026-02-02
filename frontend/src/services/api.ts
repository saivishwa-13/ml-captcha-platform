import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

// Create base API client
const createApiClient = (apiKey?: string): AxiosInstance => {
  const client = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Add API key to requests
  client.interceptors.request.use((config) => {
    const key = apiKey || localStorage.getItem('apiKey');
    if (key) {
      config.headers['X-API-Key'] = key;
    }
    return config;
  });

  return client;
};

// Default client (uses localStorage API key)
const apiClient = createApiClient();

export default apiClient;
export { createApiClient };

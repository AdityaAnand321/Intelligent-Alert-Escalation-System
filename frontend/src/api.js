import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create axios instance with default config
const api = axios.create({
    baseURL: `${API_BASE_URL}/api`,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add token to requests
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Auth API
export const authAPI = {
    register: (username, password) => 
        api.post('/auth/register', { username, password }),
    
    login: (username, password) => 
        api.post('/auth/login', { username, password }),
    
    getAllUsers: async () => {
        const response = await api.get('/auth/users');
        return {
            ...response,
            data: Array.isArray(response.data.users) ? response.data.users : []
        };
    },
    
    deleteUser: (userId) => 
        api.delete(`/auth/users/${userId}`)
};

// Alerts API
export const alertsAPI = {
    createAlert: (sourceType, severity, driverId, metadata) =>
        api.post('/alerts', { sourceType, severity, driverId, metadata }),
    
    getAllAlerts: () =>
        api.get('/alerts'),
    
    getAlertById: (alertId) =>
        api.get(`/alerts/${alertId}`),
    
    resolveAlert: (alertId) =>
        api.patch(`/alerts/${alertId}/resolve`),
    
    markComplianceRenewed: (driverId) =>
        api.post('/alerts/compliance-renewed', { driverId })
};

// Dashboard API
export const dashboardAPI = {
    getSummary: () =>
        api.get('/dashboard/summary'),
    
    getTopOffenders: (limit = 10) =>
        api.get(`/dashboard/top-offenders?limit=${limit}`),
    
    getRecentEvents: (limit = 20) =>
        api.get(`/dashboard/events?limit=${limit}`),
    
    getRecentAutoClosed: (hours = 24) =>
        api.get(`/dashboard/auto-closed?hours=${hours}`),
    
    getTrend: (days = 7) =>
        api.get(`/dashboard/trend?days=${days}`),
    
    getActiveRules: () =>
        api.get('/dashboard/config/rules')
};

export default api;

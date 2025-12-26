/**
 * API Service - Centralized HTTP client for backend communication
 * Planification Personnelle Intelligente
 */

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Generic fetch wrapper with error handling
 */
async function request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };
    
    // Add auth token if available
    const token = Auth.getToken();
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(url, config);
        
        // Handle HTTP errors
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new ApiError(
                errorData.message || `HTTP Error: ${response.status}`,
                response.status,
                errorData
            );
        }
        
        // Return JSON or empty object
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        return {};
        
    } catch (error) {
        if (error instanceof ApiError) {
            throw error;
        }
        // Network or other errors
        throw new ApiError('Erreur de connexion au serveur', 0, { originalError: error.message });
    }
}

/**
 * Custom API Error class
 */
class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

/**
 * API Service object with all endpoints
 */
const Api = {
    // ========== UTILISATEURS ==========
    utilisateurs: {
        /**
         * Register a new user
         */
        register: (data) => request('/utilisateurs/register', {
            method: 'POST',
            body: JSON.stringify(data)
        }),
        
        /**
         * Login user
         */
        login: (email, motdepasse) => request('/utilisateurs/login', {
            method: 'POST',
            body: JSON.stringify({ email, motdepasse })
        }),
        
        /**
         * Get user profile
         */
        getProfile: (id) => request(`/utilisateurs/${id}`),
        
        /**
         * Update user profile
         */
        updateProfile: (id, data) => request(`/utilisateurs/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        }),
        
        /**
         * Get all users (admin)
         */
        getAll: () => request('/utilisateurs')
    },
    
    // ========== ACTIVITES ==========
    activites: {
        /**
         * Get all activities for a user
         */
        getByUser: (userId) => request(`/activites/utilisateur/${userId}`),
        
        /**
         * Get activity by ID
         */
        getById: (id) => request(`/activites/${id}`),
        
        /**
         * Create new activity
         */
        create: (data) => request('/activites', {
            method: 'POST',
            body: JSON.stringify(data)
        }),
        
        /**
         * Update activity
         */
        update: (id, data) => request(`/activites/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        }),
        
        /**
         * Delete activity
         */
        delete: (id) => request(`/activites/${id}`, {
            method: 'DELETE'
        }),
        
        /**
         * Get activities by type
         */
        getByType: (userId, type) => request(`/activites/utilisateur/${userId}/type/${type}`),
        
        /**
         * Get activities in period
         */
        getByPeriod: (userId, dateDebut, dateFin) => 
            request(`/activites/utilisateur/${userId}/periode?debut=${dateDebut}&fin=${dateFin}`),
        
        /**
         * Search activities
         */
        search: (userId, query) => request(`/activites/utilisateur/${userId}/recherche?q=${encodeURIComponent(query)}`)
    },
    
    // ========== CONTRAINTES ==========
    contraintes: {
        /**
         * Get all constraints for a user
         */
        getByUser: (userId) => request(`/contraintes/utilisateur/${userId}`),
        
        /**
         * Get constraint by ID
         */
        getById: (id) => request(`/contraintes/${id}`),
        
        /**
         * Create new constraint
         */
        create: (data) => request('/contraintes', {
            method: 'POST',
            body: JSON.stringify(data)
        }),
        
        /**
         * Update constraint
         */
        update: (id, data) => request(`/contraintes/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        }),
        
        /**
         * Delete constraint
         */
        delete: (id) => request(`/contraintes/${id}`, {
            method: 'DELETE'
        }),
        
        /**
         * Toggle constraint status
         */
        toggleStatus: (id) => request(`/contraintes/${id}/toggle`, {
            method: 'PATCH'
        }),
        
        /**
         * Get active constraints
         */
        getActive: (userId) => request(`/contraintes/utilisateur/${userId}/actives`)
    },
    
    // ========== CONFLITS ==========
    conflits: {
        /**
         * Get all conflicts for a user
         */
        getByUser: (userId) => request(`/conflits/utilisateur/${userId}`),
        
        /**
         * Get unresolved conflicts
         */
        getUnresolved: (userId) => request(`/conflits/utilisateur/${userId}/non-resolus`),
        
        /**
         * Mark conflict as resolved
         */
        resolve: (id) => request(`/conflits/${id}/resoudre`, {
            method: 'PATCH'
        }),
        
        /**
         * Detect conflicts for user
         */
        detect: (userId) => request(`/conflits/utilisateur/${userId}/detecter`, {
            method: 'POST'
        }),
        
        /**
         * Get conflict statistics
         */
        getStats: (userId) => request(`/conflits/utilisateur/${userId}/statistiques`)
    },
    
    // ========== STATISTIQUES ==========
    statistiques: {
        /**
         * Get dashboard stats
         */
        getDashboard: (userId) => request(`/statistiques/utilisateur/${userId}/dashboard`),
        
        /**
         * Get activity distribution
         */
        getDistribution: (userId) => request(`/statistiques/utilisateur/${userId}/distribution`)
    }
};

// Export for usage
window.Api = Api;
window.ApiError = ApiError;

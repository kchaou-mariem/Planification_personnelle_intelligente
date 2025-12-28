/**
 * API Statistiques
 */
const statistiques = {
    getRapport: (userId) => request(`${API_BASE_URL}/statistiques/utilisateur/${userId}/rapport`)
};
/**
 * API Service - Client HTTP centralisé avec optimisation
 * Planification Personnelle Intelligente
 */

const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8083/api';

/**
 * Fonction générique pour les requêtes HTTP
 */
async function request(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur ${response.status}: ${errorText || response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erreur API:', error);
        throw error;
    }
}

/**
 * API Utilisateurs
 */
const utilisateurs = {
    login: (email, motdepasse) => 
        request(`${API_BASE_URL}/utilisateurs/login`, {
            method: 'POST',
            body: JSON.stringify({ email, motdepasse })
        }),

    register: (nom, prenom, email, motdepasse) => 
        request(`${API_BASE_URL}/utilisateurs/register`, {
            method: 'POST',
            body: JSON.stringify({ nom, prenom, email, motdepasse })
        }),

    getById: (id) => 
        request(`${API_BASE_URL}/utilisateurs/${id}`),

    update: (id, data) => 
        request(`${API_BASE_URL}/utilisateurs/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        }),

    delete: (id) => 
        request(`${API_BASE_URL}/utilisateurs/${id}`, {
            method: 'DELETE'
        })
};

/**
 * API Activités avec optimisation
 */
const activites = {
    getAll: (userId) => 
        request(`${API_BASE_URL}/activites/utilisateur/${userId}`),
    
    // Alias pour compatibilité
    getByUser: (userId) => 
        request(`${API_BASE_URL}/activites/utilisateur/${userId}`),

    getById: (id) => 
        request(`${API_BASE_URL}/activites/${id}`),

    create: (activite) => 
        request(`${API_BASE_URL}/activites`, {
            method: 'POST',
            body: JSON.stringify(activite)
        }),

    update: (id, activite) => 
        request(`${API_BASE_URL}/activites/${id}`, {
            method: 'PUT',
            body: JSON.stringify(activite)
        }),

    delete: (id) => 
        request(`${API_BASE_URL}/activites/${id}`, {
            method: 'DELETE'
        }),

    // ✅ NOUVEAUX ENDPOINTS D'OPTIMISATION
    getScore: (userId) => 
        request(`${API_BASE_URL}/activites/utilisateur/${userId}/score`),

    valider: (userId) => 
        request(`${API_BASE_URL}/activites/utilisateur/${userId}/valider`),

    optimiser: (userId, iterations = 1000) => 
        request(`${API_BASE_URL}/activites/utilisateur/${userId}/optimiser`, {
            method: 'POST',
            body: JSON.stringify({ iterations })
        })
};

/**
 * API Contraintes
 */
const contraintes = {
    getAll: (userId) => 
        request(`${API_BASE_URL}/contraintes/utilisateur/${userId}`),
    
    // Alias pour compatibilité
    getByUser: (userId) => 
        request(`${API_BASE_URL}/contraintes/utilisateur/${userId}`),

    getById: (id) => 
        request(`${API_BASE_URL}/contraintes/${id}`),

    create: (contrainte) => 
        request(`${API_BASE_URL}/contraintes`, {
            method: 'POST',
            body: JSON.stringify(contrainte)
        }),

    update: (id, contrainte) => 
        request(`${API_BASE_URL}/contraintes/${id}`, {
            method: 'PUT',
            body: JSON.stringify(contrainte)
        }),
    
    toggleStatus: (id) => 
        request(`${API_BASE_URL}/contraintes/${id}/toggle`, {
            method: 'PUT'
        }),

    delete: (id) => 
        request(`${API_BASE_URL}/contraintes/${id}`, {
            method: 'DELETE'
        })
};

/**
 * API Conflits
 */
const conflits = {
    getAll: (userId) => 
        request(`${API_BASE_URL}/conflits/utilisateur/${userId}`),

    getNonResolus: (userId) => 
        request(`${API_BASE_URL}/conflits/utilisateur/${userId}/non-resolus`),
    
    // Alias pour compatibilité
    getByUser: (userId) => 
        request(`${API_BASE_URL}/conflits/utilisateur/${userId}`),
    
    getUnresolved: (userId) => 
        request(`${API_BASE_URL}/conflits/utilisateur/${userId}/non-resolus`),

    getById: (id) => 
        request(`${API_BASE_URL}/conflits/${id}`),
    
    getStats: (userId) => request(`${API_BASE_URL}/conflits/utilisateur/${userId}/statistiques`),

    resoudre: (id) => 
        request(`${API_BASE_URL}/conflits/${id}/resoudre`, {
            method: 'PUT'
        }),
    
    // Alias pour compatibilité
    resolve: (id) => 
        request(`${API_BASE_URL}/conflits/${id}/resoudre`, {
            method: 'PUT'
        }),
    
    detect: (userId) => 
        request(`${API_BASE_URL}/conflits/detect/${userId}`, {
            method: 'POST'
        }),

    delete: (id) => 
        request(`${API_BASE_URL}/conflits/${id}`, {
            method: 'DELETE'
        })
};

/**
 * Export de l'API
 */
const Api = {
    utilisateurs,
    activites,
    contraintes,
    conflits,
    statistiques
};

// Rendre disponible globalement
window.Api = Api;
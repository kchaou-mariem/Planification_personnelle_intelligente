/**
 * Authentication Module - Session and token management
 * Planification Personnelle Intelligente
 */

const Auth = {
    // Storage keys
    TOKEN_KEY: 'auth_token',
    USER_KEY: 'auth_user',

    /**
     * Login user and store session
     */
    async login(email, motdepasse) {
        try {
            const response = await Api.utilisateurs.login(email, motdepasse);

            if (response.succes && response.utilisateur) {
                this.setUser(response.utilisateur);
                if (response.token) {
                    this.setToken(response.token);
                }
                return { success: true, user: response.utilisateur };
            }

            return { success: false, message: response.message || 'Identifiants incorrects' };
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, message: error.message || 'Erreur de connexion' };
        }
    },

    /**
     * Register new user
     */
    async register(userData) {
        try {
            const response = await Api.utilisateurs.register(userData);

            if (response.succes && response.utilisateur) {
                this.setUser(response.utilisateur);
                if (response.token) {
                    this.setToken(response.token);
                }
                return { success: true, user: response.utilisateur };
            }

            return { success: false, message: response.message || 'Erreur lors de l\'inscription' };
        } catch (error) {
            console.error('Register error:', error);
            return { success: false, message: error.message || 'Erreur lors de l\'inscription' };
        }
    },

    /**
     * Logout user and clear session
     */
    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = 'login.html';
    },

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return this.getUser() !== null;
    },

    /**
     * Get stored token
     */
    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    /**
     * Set auth token
     */
    setToken(token) {
        localStorage.setItem(this.TOKEN_KEY, token);
    },

    /**
     * Get stored user
     */
    getUser() {
        const userStr = localStorage.getItem(this.USER_KEY);
        if (!userStr) return null;

        try {
            return JSON.parse(userStr);
        } catch {
            return null;
        }
    },

    /**
     * Set user data
     */
    setUser(user) {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    /**
     * Update user data
     */
    updateUser(updates) {
        const user = this.getUser();
        if (user) {
            Object.assign(user, updates);
            this.setUser(user);
        }
    },

    /**
     * Get user ID
     */
    getUserId() {
        const user = this.getUser();
        return user ? user.id : null;
    },

    /**
     * Get user full name
     */
    getUserFullName() {
        const user = this.getUser();
        if (!user) return 'Utilisateur';
        return `${user.prenom || ''} ${user.nom || ''}`.trim() || 'Utilisateur';
    },

    /**
     * Get user initials for avatar
     */
    getUserInitials() {
        const user = this.getUser();
        if (!user) return 'U';
        const prenom = user.prenom || '';
        const nom = user.nom || '';
        return `${prenom.charAt(0)}${nom.charAt(0)}`.toUpperCase() || 'U';
    },

    /**
     * Require authentication - redirect if not logged in
     */
    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'login.html';
            return false;
        }
        return true;
    },

    /**
     * Redirect if already authenticated (for login/register pages)
     */
    redirectIfAuthenticated(destination = 'dashboard.html') {
        if (this.isAuthenticated()) {
            window.location.href = destination;
            return true;
        }
        return false;
    }
};

// Export for usage
window.Auth = Auth;

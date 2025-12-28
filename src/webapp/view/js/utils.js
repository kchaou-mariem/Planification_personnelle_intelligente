/**
 * Utility Functions
 * Planification Personnelle Intelligente
 */

const Utils = {
    // ========== DATE FORMATTING ==========

    /**
     * Format date to French locale
     */
    formatDate(date, options = {}) {
        const d = date instanceof Date ? date : new Date(date);
        const defaultOptions = {
            day: '2-digit',
            month: 'long',
            year: 'numeric',
            ...options
        };
        return d.toLocaleDateString('fr-FR', defaultOptions);
    },

    /**
     * Format time
     */
    formatTime(date) {
        const d = date instanceof Date ? date : new Date(date);
        return d.toLocaleTimeString('fr-FR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    

    /**
     * Format datetime
     */
    formatDateTime(date) {
        return `${this.formatDate(date)} à ${this.formatTime(date)}`;
    },

    /**
     * Format relative time (e.g., "il y a 2 heures")
     */
    formatRelativeTime(date) {
        const d = date instanceof Date ? date : new Date(date);
        const now = new Date();
        const diffMs = now - d;
        const diffSec = Math.floor(diffMs / 1000);
        const diffMin = Math.floor(diffSec / 60);
        const diffHour = Math.floor(diffMin / 60);
        const diffDay = Math.floor(diffHour / 24);

        if (diffSec < 60) return 'À l\'instant';
        if (diffMin < 60) return `Il y a ${diffMin} min`;
        if (diffHour < 24) return `Il y a ${diffHour}h`;
        if (diffDay < 7) return `Il y a ${diffDay}j`;
        return this.formatDate(d, { day: '2-digit', month: 'short' });
    },

    /**
     * Format date for input[type="datetime-local"]
     */
    formatForInput(date) {
        const d = date instanceof Date ? date : new Date(date);
        return d.toISOString().slice(0, 16);
    },

    // ========== ACTIVITY TYPES ==========

    activityTypes: {
        Sport: { label: 'Sport', color: '#22c55e', icon: '🏃' },
        Etude: { label: 'Étude', color: '#3b82f6', icon: '📚' },
        Loisirs: { label: 'Loisirs', color: '#f59e0b', icon: '🎮' },
        Repos: { label: 'Repos', color: '#8b5cf6', icon: '😴' },
        Travail: { label: 'Travail', color: '#ef4444', icon: '💼' }
    },

    /**
     * Get activity type info
     */
    getActivityType(type) {
        return this.activityTypes[type] || { label: type, color: '#6b7280', icon: '📌' };
    },

    // ========== CONSTRAINT TYPES ==========

    constraintTypes: {
        Sommeil: { label: 'Sommeil', icon: '🌙' },
        Travail: { label: 'Travail', icon: '💼' },
        RDV: { label: 'RDV', icon: '📅' },
        Repos: { label: 'Repos', icon: '🛋️' },
        Cours: { label: 'Cours', icon: '🎓' }
    },

    /**
     * Get constraint type info
     */
    getConstraintType(type) {
        return this.constraintTypes[type] || { label: type, icon: '📋' };
    },

    // ========== CONFLICT TYPES ==========

    conflictTypes: {
        CHEVAUCHEMENT_DES_ACTIVITES: { label: 'Chevauchement', severity: 'high', icon: '⚠️' },
        VIOLATION_DE_CONTRAINTE: { label: 'Violation de contrainte', severity: 'high', icon: '🚫' },
        FATIGUE_EXCESSIVE: { label: 'Fatigue excessive', severity: 'medium', icon: '😓' },
        DEADLINE: { label: 'Deadline proche', severity: 'high', icon: '⏰' },
        EQUILIBRE_FAIBLE: { label: 'Équilibre faible', severity: 'low', icon: '⚖️' },
        REPOS_INSUFFISANT: { label: 'Repos insuffisant', severity: 'medium', icon: '😴' }
    },

    /**
     * Get conflict type info
     */
    getConflictType(type) {
        return this.conflictTypes[type] || { label: type, severity: 'medium', icon: '❓' };
    },

    // ========== DOM UTILITIES ==========

    /**
     * Create element with attributes and children
     */
    createElement(tag, attributes = {}, children = []) {
        const el = document.createElement(tag);

        Object.entries(attributes).forEach(([key, value]) => {
            if (key === 'className') {
                el.className = value;
            } else if (key === 'innerHTML') {
                el.innerHTML = value;
            } else if (key === 'textContent') {
                el.textContent = value;
            } else if (key.startsWith('on')) {
                el.addEventListener(key.slice(2).toLowerCase(), value);
            } else {
                el.setAttribute(key, value);
            }
        });

        children.forEach(child => {
            if (typeof child === 'string') {
                el.appendChild(document.createTextNode(child));
            } else if (child instanceof Node) {
                el.appendChild(child);
            }
        });

        return el;
    },

    /**
     * Show/hide element
     */
    toggle(element, show) {
        if (typeof element === 'string') {
            element = document.querySelector(element);
        }
        if (element) {
            element.classList.toggle('hidden', !show);
        }
    },

    /**
     * Show loading overlay
     */
    showLoading() {
        let overlay = document.getElementById('loading-overlay');
        if (!overlay) {
            overlay = this.createElement('div', {
                id: 'loading-overlay',
                className: 'loading-overlay'
            }, [
                this.createElement('div', { className: 'spinner' })
            ]);
            document.body.appendChild(overlay);
        }
        overlay.classList.remove('hidden');
    },

    /**
     * Hide loading overlay
     */
    hideLoading() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.add('hidden');
        }
    },

    // ========== NOTIFICATIONS ==========

    /**
     * Show toast notification
     */
    toast(message, type = 'info', duration = 4000) {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = this.createElement('div', {
                id: 'toast-container',
                style: 'position:fixed;top:20px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:10px;'
            });
            document.body.appendChild(container);
        }

        const icons = {
            success: '✓',
            danger: '✕',
            warning: '⚠',
            info: 'ℹ'
        };

        const toast = this.createElement('div', {
            className: `alert alert-${type} animate-slideUp`,
            style: 'min-width:300px;box-shadow:var(--shadow-lg);'
        }, [
            this.createElement('span', { textContent: icons[type] || 'ℹ' }),
            this.createElement('span', { textContent: message })
        ]);

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },

    // ========== FORM UTILITIES ==========

    /**
     * Get form data as object
     */
    getFormData(form) {
        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });
        return data;
    },

    /**
     * Validate email format
     */
    isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    },

    /**
     * Show form error
     */
    showFormError(input, message) {
        input.classList.add('error');
        let errorEl = input.parentElement.querySelector('.form-error');
        if (!errorEl) {
            errorEl = this.createElement('div', { className: 'form-error' });
            input.parentElement.appendChild(errorEl);
        }
        errorEl.textContent = message;
    },

    /**
     * Clear form errors
     */
    clearFormErrors(form) {
        form.querySelectorAll('.error').forEach(el => el.classList.remove('error'));
        form.querySelectorAll('.form-error').forEach(el => el.remove());
    },

    // ========== THEME ==========

    /**
     * Toggle dark/light theme
     */
    toggleTheme() {
        const html = document.documentElement;
        const current = html.getAttribute('data-theme');
        const newTheme = current === 'dark' ? 'light' : 'dark';
        html.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
    },

    /**
     * Initialize theme from storage
     */
    initTheme() {
        const saved = localStorage.getItem('theme');
        if (saved) {
            document.documentElement.setAttribute('data-theme', saved);
        } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
            document.documentElement.setAttribute('data-theme', 'dark');
        }
    },

    // ========== MODAL UTILITIES ==========

    /**
     * Open modal
     */
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    },

    /**
     * Close modal
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    },

    /**
     * Close modal on overlay click
     */
    initModalClose() {
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    overlay.classList.remove('active');
                    document.body.style.overflow = '';
                }
            });
        });

        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', () => {
                const modal = btn.closest('.modal-overlay');
                if (modal) {
                    modal.classList.remove('active');
                    document.body.style.overflow = '';
                }
            });
        });
    },

    // ========== PRIORITY HELPERS ==========

    /**
     * Get priority label and color
     */
    getPriority(level) {
        if (level >= 8) return { label: 'Haute', color: 'danger' };
        if (level >= 5) return { label: 'Moyenne', color: 'warning' };
        return { label: 'Basse', color: 'success' };
    }
};

// Initialize theme on load
document.addEventListener('DOMContentLoaded', () => {
    Utils.initTheme();
    Utils.initModalClose();
});

// Export for usage
window.Utils = Utils;

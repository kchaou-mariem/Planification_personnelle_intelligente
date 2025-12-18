package service;

import entities.Conflit;

import java.util.List;

/**
 * Service métier pour la gestion et la résolution automatique des conflits.
 */
public interface ConflitService {

	/**
	 * Détecte les chevauchements d'activités pour un utilisateur et enregistre des conflits.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Liste des conflits détectés (persistés)
	 */
	List<Conflit> detecterChevauchementsUtilisateur(Long idUtilisateur);

	/**
	 * Marquer manuellement un conflit comme résolu.
	 * @param idConflit ID du conflit
	 * @return true si marqué, sinon false
	 */
	boolean marquerConflitCommeResolu(Long idConflit);

	// ========== CONSULTATION ET RECHERCHE ==========

	/**
	 * Récupérer tous les conflits d'un utilisateur (résolus et non résolus).
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Liste de tous les conflits
	 */
	List<Conflit> getTousLesConflitsUtilisateur(Long idUtilisateur);

	/**
	 * Récupérer uniquement les conflits non résolus d'un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Liste des conflits non résolus
	 */
	List<Conflit> getConflitsNonResolusUtilisateur(Long idUtilisateur);

	/**
	 * Récupérer les conflits critiques d'un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Liste des conflits critiques (non résolus et prioritaires)
	 */
	List<Conflit> getConflitsCritiquesUtilisateur(Long idUtilisateur);

	/**
	 * Récupérer les conflits par type pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @param type Type de conflit
	 * @return Liste des conflits du type spécifié
	 */
	List<Conflit> getConflitsParTypeUtilisateur(Long idUtilisateur, entities.TypeConflit type);

	/**
	 * Récupérer les conflits résolus d'un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Liste des conflits résolus
	 */
	List<Conflit> getConflitsResolusUtilisateur(Long idUtilisateur);

	/**
	 * Récupérer les conflits d'un utilisateur dans une période.
	 * @param idUtilisateur ID de l'utilisateur
	 * @param dateDebut Date de début
	 * @param dateFin Date de fin
	 * @return Liste des conflits dans la période
	 */
	List<Conflit> getConflitsParPeriodeUtilisateur(Long idUtilisateur, java.time.LocalDateTime dateDebut, java.time.LocalDateTime dateFin);

	/**
	 * Rechercher des conflits par mot-clé dans les activités liées.
	 * @param idUtilisateur ID de l'utilisateur
	 * @param motCle Mot-clé à rechercher
	 * @return Liste des conflits correspondants
	 */
	List<Conflit> rechercherConflitsParMotCle(Long idUtilisateur, String motCle);

	/**
	 * Récupérer les activités impliquées dans un conflit.
	 * @param idConflit ID du conflit
	 * @return Liste des activités liées au conflit
	 */
	List<entities.Activite> getActivitesImpliqueesDansConflit(Long idConflit);

	// ========== STATISTIQUES ==========

	/**
	 * Obtenir le nombre total de conflits pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Nombre de conflits
	 */
	int compterConflitsUtilisateur(Long idUtilisateur);

	/**
	 * Obtenir le nombre de conflits non résolus pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Nombre de conflits non résolus
	 */
	int compterConflitsNonResolusUtilisateur(Long idUtilisateur);

	/**
	 * Calculer le taux de résolution des conflits pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Pourcentage de conflits résolus (0-100)
	 */
	double getTauxResolutionUtilisateur(Long idUtilisateur);

	/**
	 * Obtenir les statistiques par type de conflit pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Map avec type de conflit et nombre d'occurrences
	 */
	java.util.Map<entities.TypeConflit, Integer> getStatistiquesParTypeUtilisateur(Long idUtilisateur);

	/**
	 * Obtenir le nombre de conflits par type pour un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @param type Type de conflit
	 * @return Nombre de conflits de ce type
	 */
	int compterConflitsParTypeUtilisateur(Long idUtilisateur, entities.TypeConflit type);

	// ========== GESTION AVANCÉE ==========

	/**
	 * Supprimer un conflit et ses liens.
	 * @param idConflit ID du conflit
	 * @return true si supprimé avec succès
	 */
	boolean supprimerConflit(Long idConflit);

	/**
	 * Supprimer tous les conflits résolus d'un utilisateur.
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Nombre de conflits supprimés
	 */
	int nettoyerConflitsResolusUtilisateur(Long idUtilisateur);

	/**
	 * Marquer plusieurs conflits comme résolus en une seule opération.
	 * @param idsConflits Liste des IDs des conflits
	 * @return Nombre de conflits marqués comme résolus
	 */
	int marquerPlusieursConflitsCommeResolus(java.util.List<Long> idsConflits);

	/**
	 * Supprimer les anciens conflits résolus avant une date donnée.
	 * @param idUtilisateur ID de l'utilisateur
	 * @param dateAvant Date limite
	 * @return Nombre de conflits supprimés
	 */
	int supprimerConflitsResolusAvant(Long idUtilisateur, java.time.LocalDateTime dateAvant);
}

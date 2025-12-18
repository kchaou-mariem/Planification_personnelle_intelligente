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
	 * Tente de résoudre automatiquement les chevauchements pour un utilisateur
	 * en réorganisant les activités selon une stratégie simple (priorité/durée).
	 * @param idUtilisateur ID de l'utilisateur
	 * @return Nombre de conflits résolus automatiquement
	 */
	int resoudreChevauchementsUtilisateur(Long idUtilisateur);

	/**
	 * Marquer manuellement un conflit comme résolu.
	 * @param idConflit ID du conflit
	 * @return true si marqué, sinon false
	 */
	boolean marquerConflitCommeResolu(Long idConflit);
}

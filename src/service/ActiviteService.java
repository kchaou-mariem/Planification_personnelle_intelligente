 package service;

import entities.Activite;
import entities.TypeActivite;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface Service pour la gestion métier des activités
 * Encapsule la logique métier et valide les opérations
 */
public interface ActiviteService {
    
    // ========== OPÉRATIONS CRUD AVEC VALIDATION ==========
    
    /**
     * Créer une nouvelle activité avec validation
     * @param activite L'activité à créer
     * @return L'ID de l'activité créée, ou -1 en cas d'échec
     */
    Long creerActivite(Activite activite);
    
    /**
     * Mettre à jour une activité avec validation
     * @param activite L'activité à mettre à jour
     * @return true si la mise à jour a réussi
     */
    boolean mettreAJourActivite(Activite activite);
    
    /**
     * Supprimer une activité
     * @param idActivite L'ID de l'activité
     * @return true si la suppression a réussi
     */
    boolean supprimerActivite(Long idActivite);
    
    /**
     * Récupérer une activité
     * @param idActivite L'ID de l'activité
     * @return Optional contenant l'activité
     */
    Optional<Activite> obtenirActivite(Long idActivite);
    
    /**
     * Récupérer toutes les activités
     * @return Liste de toutes les activités
     */
    List<Activite> obtenirToutesLesActivites();
    
    // ========== RECHERCHE ET FILTRAGE ==========
    
    /**
     * Récupérer les activités d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesUtilisateur(Long idUtilisateur);
    
    /**
     * Récupérer les activités d'un type spécifique
     * @param type Le type d'activité
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesParType(TypeActivite type);
    
    /**
     * Récupérer les activités non complétées d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des activités non complétées
     */
    List<Activite> obtenirActivitesNonCompletees(Long idUtilisateur);
    
    /**
     * Récupérer les activités complétées d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des activités complétées
     */
    List<Activite> obtenirActivitesCompletees(Long idUtilisateur);
    
    /**
     * Récupérer les activités dans une plage horaire
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesDansLaPeriode(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Récupérer les activités d'un utilisateur dans une plage horaire
     * @param idUtilisateur L'ID de l'utilisateur
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesUtilisateurDansLaPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Rechercher les activités par mot-clé
     * @param motCle Le mot-clé
     * @return Liste des activités correspondantes
     */
    List<Activite> rechercherActivites(String motCle);
    
    /**
     * Rechercher les activités d'un utilisateur par mot-clé
     * @param idUtilisateur L'ID de l'utilisateur
     * @param motCle Le mot-clé
     * @return Liste des activités correspondantes
     */
    List<Activite> rechercherActivitesUtilisateur(Long idUtilisateur, String motCle);
    
    // ========== OPÉRATIONS MÉTIER ==========
    
    /**
     * Marquer une activité comme complétée
     * @param idActivite L'ID de l'activité
     * @return true si l'opération a réussi
     */
    boolean completerActivite(Long idActivite);
    
    /**
     * Marquer une activité comme non complétée
     * @param idActivite L'ID de l'activité
     * @return true si l'opération a réussi
     */
    boolean decompleterActivite(Long idActivite);
    
    /**
     * Vérifier s'il y a conflit de chevauchement avec d'autres activités
     * @param idUtilisateur L'ID de l'utilisateur
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return true s'il y a chevauchement
     */
    boolean verifierChevauchement(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Récupérer les activités qui chevauchent pour un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return Liste des activités qui chevauchent
     */
    List<Activite> obtenirActivitesChevauchantes(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Vérifier si les horaires d'une activité sont valides
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return true si les horaires sont valides
     */
    boolean validerHoraires(LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Vérifier si la durée d'une activité correspond aux horaires
     * @param duree La durée en minutes
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return true si la durée correspond
     */
    boolean validerDuree(int duree, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Vérifier la priorité (doit être entre 1 et 10)
     * @param priorite La priorité
     * @return true si la priorité est valide
     */
    boolean validerPriorite(int priorite);
    
    // ========== STATISTIQUES ==========
    
    /**
     * Obtenir le nombre total d'activités
     * @return Nombre total d'activités
     */
    int obtenirNombreTotalActivites();
    
    /**
     * Obtenir le nombre d'activités d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Nombre d'activités
     */
    int obtenirNombreActivitesUtilisateur(Long idUtilisateur);
    
    /**
     * Obtenir le nombre d'activités complétées
     * @return Nombre d'activités complétées
     */
    int obtenirNombreActivitesCompletees();
    
    /**
     * Obtenir le nombre d'activités non complétées
     * @return Nombre d'activités non complétées
     */
    int obtenirNombreActivitesNonCompletees();
    
    /**
     * Obtenir le taux de complétude des activités
     * @return Pourcentage de complétude (0-100)
     */
    double obtenirTauxCompletion();
    
    /**
     * Obtenir le taux de complétude pour un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Pourcentage de complétude (0-100)
     */
    double obtenirTauxCompletionUtilisateur(Long idUtilisateur);
    
    /**
     * Obtenir le nombre total de minutes d'activités
     * @return Durée totale en minutes
     */
    int obtenirDureeTotalActivites();
    
    /**
     * Obtenir le nombre total de minutes pour un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Durée totale en minutes
     */
    int obtenirDureeTotalActivitesUtilisateur(Long idUtilisateur);
    
    /**
     * Obtenir les activités avec deadline proche
     * @param joursAvance Nombre de jours d'avance
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesDeadlineProche(int joursAvance);
    
    /**
     * Obtenir les activités haute priorité non complétées
     * @return Liste des activités
     */
    List<Activite> obtenirActivitesHautePriorite();
    
    /**
     * Obtenir les activités récentes
     * @param nombre Nombre d'activités à récupérer
     * @return Liste des activités récentes
     */
    List<Activite> obtenirActivitesRecentes(int nombre);
}

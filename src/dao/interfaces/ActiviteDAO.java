package dao.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import entities.Activite;
import entities.TypeActivite;

/**
 * Interface DAO pour la gestion des activités dans la base de données
 * Définit les opérations CRUD et les méthodes métier pour les activités
 */
public interface ActiviteDAO {
    
 
    
    /**
     * Ajouter une nouvelle activité dans la base de données
     * @param activite L'activité à ajouter
     * @return L'ID de l'activité créée, ou -1 en cas d'échec
     */
    Long ajouter(Activite activite);
    
    /**
     * Modifier une activité existante
     * @param activite L'activité avec les nouvelles valeurs
     * @return true si la modification a réussi, false sinon
     */
    boolean modifier(Activite activite);
    
    /**
     * Supprimer une activité par son ID
     * @param idActivite L'ID de l'activité à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean supprimer(Long idActivite);
    
    /**
     * Récupérer une activité par son ID
     * @param idActivite L'ID de l'activité recherchée
     * @return Optional contenant l'activité si elle existe, Optional vide sinon
     */
    Optional<Activite> getById(Long idActivite);
    
    /**
     * Récupérer toutes les activités
     * @return Liste de toutes les activités
     */
    List<Activite> getAll();
    
   
    
    /**
     * Récupérer les activités d'un utilisateur spécifique
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des activités de l'utilisateur
     */
    List<Activite> getByUtilisateur(Long idUtilisateur);
    
    /**
     * Récupérer les activités par type
     * @param type Le type d'activité recherché
     * @return Liste des activités du type spécifié
     */
    List<Activite> getByType(TypeActivite type);
    
    /**
     * Récupérer les activités par type et utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @param type Le type d'activité
     * @return Liste des activités correspondantes
     */
    List<Activite> getByTypeAndUtilisateur(Long idUtilisateur, TypeActivite type);
    
    /**
     * Récupérer les activités non complétées
     * @return Liste des activités non complétées
     */
    List<Activite> getActivitesNonCompletees();
    
    /**
     * Récupérer les activités complétées
     * @return Liste des activités complétées
     */
    List<Activite> getActivitesCompletees();
    
    /**
     * Récupérer les activités non complétées d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des activités non complétées
     */
    List<Activite> getActivitesNonCompleteesByUtilisateur(Long idUtilisateur);
    
    /**
     * Récupérer les activités dans une période donnée
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des activités dans la période
     */
    List<Activite> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Récupérer les activités d'un utilisateur dans une période donnée
     * @param idUtilisateur L'ID de l'utilisateur
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des activités correspondantes
     */
    List<Activite> getByUtilisateurAndPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Récupérer les activités arrivant à deadline bientôt
     * @param joursAvance Nombre de jours d'avance pour la recherche
     * @return Liste des activités avec deadline proche
     */
    List<Activite> getActivitesAvecDeadlineProche(int joursAvance);
    
    /**
     * Récupérer les activités par priorité
     * @param priorite Le niveau de priorité
     * @return Liste des activités avec cette priorité
     */
    List<Activite> getByPriorite(int priorite);
    
    /**
     * Rechercher les activités par mot-clé (titre ou description)
     * @param motCle Le mot-clé de recherche
     * @return Liste des activités correspondantes
     */
    List<Activite> rechercherParMotCle(String motCle);
    
    /**
     * Rechercher les activités d'un utilisateur par mot-clé
     * @param idUtilisateur L'ID de l'utilisateur
     * @param motCle Le mot-clé de recherche
     * @return Liste des activités correspondantes
     */
    List<Activite> rechercherParMotCleUtilisateur(Long idUtilisateur, String motCle);
    
    // ========== OPÉRATIONS MÉTIER ==========
    
    /**
     * Marquer une activité comme complétée
     * @param idActivite L'ID de l'activité
     * @return true si l'opération a réussi, false sinon
     */
    boolean marquerCommeCompletee(Long idActivite);
    
    /**
     * Marquer plusieurs activités comme complétées
     * @param idsActivites Liste des IDs des activités
     * @return Nombre d'activités marquées comme complétées
     */
    int marquerPlusieursCommeCompletees(List<Long> idsActivites);
    
    /**
     * Marquer une activité comme non complétée
     * @param idActivite L'ID de l'activité
     * @return true si l'opération a réussi, false sinon
     */
    boolean marquerCommeNonCompletee(Long idActivite);
    
    /**
     * Vérifier s'il y a chevauchement avec d'autres activités
     * @param idActivite L'ID de l'activité
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return true s'il y a chevauchement, false sinon
     */
    boolean hasChevauchement(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Récupérer les activités qui chevauchent avec une période donnée
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return Liste des activités qui chevauchent
     */
    List<Activite> getActivitesChevauchantes(LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    /**
     * Récupérer les activités qui chevauchent pour un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @param horaireDebut Horaire de début
     * @param horaireFin Horaire de fin
     * @return Liste des activités qui chevauchent
     */
    List<Activite> getActivitesChevauchantesUtilisateur(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    // ========== STATISTIQUES ==========
    
    /**
     * Compter le nombre total d'activités
     * @return Nombre total d'activités
     */
    int compterToutesLesActivites();
    
    /**
     * Compter le nombre d'activités d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Nombre d'activités
     */
    int compterActivitesUtilisateur(Long idUtilisateur);
    
    /**
     * Compter le nombre d'activités complétées
     * @return Nombre d'activités complétées
     */
    int compterActivitesCompletees();
    
    /**
     * Compter le nombre d'activités non complétées
     * @return Nombre d'activités non complétées
     */
    int compterActivitesNonCompletees();
    
    /**
     * Compter le nombre d'activités d'un type spécifique
     * @param type Le type d'activité
     * @return Nombre d'activités
     */
    int compterParType(TypeActivite type);
    
    /**
     * Calculer la durée totale des activités
     * @return Durée totale en minutes
     */
    int calculerDureeTotalActivites();
    
    /**
     * Calculer la durée totale des activités d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Durée totale en minutes
     */
    int calculerDureeTotalActivitesUtilisateur(Long idUtilisateur);
    
    /**
     * Récupérer les activités récentes
     * @param limite Nombre d'activités à récupérer
     * @return Liste des activités récentes
     */
    List<Activite> getActivitesRecentes(int limite);
    
    /**
     * Récupérer les activités avec la priorité la plus élevée
     * @return Liste des activités haute priorité
     */
    List<Activite> getActivitesHautePriorite();
}

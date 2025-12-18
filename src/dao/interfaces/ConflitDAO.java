package dao.interfaces;

import entities.Conflit;
import entities.TypeConflit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO pour la gestion des conflits dans la base de données
 * Définit les opérations CRUD et les méthodes métier pour les conflits
 */
public interface ConflitDAO {
    
    // ========== OPÉRATIONS CRUD DE BASE ==========
    
    /**
     * Ajouter un nouveau conflit dans la base de données
     * @param conflit Le conflit à ajouter
     * @return L'ID du conflit créé, ou -1 en cas d'échec
     */
    Long ajouter(Conflit conflit);
    
    /**
     * Modifier un conflit existant
     * @param conflit Le conflit avec les nouvelles valeurs
     * @return true si la modification a réussi, false sinon
     */
    boolean modifier(Conflit conflit);
    
    /**
     * Supprimer un conflit par son ID
     * @param idConflit L'ID du conflit à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean supprimer(Long idConflit);
    
    /**
     * Récupérer un conflit par son ID
     * @param idConflit L'ID du conflit recherché
     * @return Optional contenant le conflit s'il existe, Optional vide sinon
     */
    Optional<Conflit> getById(Long idConflit);
    //Un ID peut être valide mais absent
    //Optional : évite les null --> evite null pointer exception
    //avec optional on gere les retours null 
    /*public void afficherConflit(Long id) {
        dao.getById(id)
           .ifPresentOrElse(
               conflit -> System.out.println(conflit.getTitre()),
               () -> System.out.println("Non trouvé")
           );
    }
    */
    
    /**
     * Récupérer tous les conflits
     * @return Liste de tous les conflits
     */
    List<Conflit> getAll();
    
    // ========== RECHERCHE ET FILTRAGE ==========
    
    /**
     * Récupérer les conflits par type
     * @param type Le type de conflit recherché
     * @return Liste des conflits du type spécifié
     */
    List<Conflit> getByType(TypeConflit type);
    
    /**
     * Récupérer les conflits non résolus
     * @return Liste des conflits non résolus
     */
    List<Conflit> getConflitsNonResolus();
    
    /**
     * Récupérer les conflits résolus
     * @return Liste des conflits résolus
     */
    List<Conflit> getConflitsResolus();
    
    /**
     * Récupérer les conflits détectés dans une période donnée
     * @param dateDebut Date de début de la période
     * @param dateFin Date de fin de la période
     * @return Liste des conflits détectés dans la période
     */
    List<Conflit> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin);
    
    /**
     * Récupérer les conflits liés à une activité spécifique
     * @param idActivite L'ID de l'activité
     * @return Liste des conflits liés à cette activité
     */
    List<Conflit> getByActivite(Long idActivite);
    
    /**
     * Récupérer les conflits liés à un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Liste des conflits de l'utilisateur
     */
    List<Conflit> getByUtilisateur(Long idUtilisateur);
    
    // ========== OPÉRATIONS MÉTIER ==========
    
    /**
     * Marquer un conflit comme résolu
     * @param idConflit L'ID du conflit à marquer comme résolu
     * @return true si l'opération a réussi, false sinon
     */
    boolean marquerCommeResolu(Long idConflit);
    
    /**
     * Marquer plusieurs conflits comme résolus
     * @param idsConflits Liste des IDs des conflits à marquer comme résolus
     * @return Nombre de conflits marqués comme résolus
     */
    int marquerPlusieursCommeResolus(List<Long> idsConflits);
    
    /**
     * Détecter les conflits de chevauchement pour une activité
     * @param idActivite L'ID de l'activité à vérifier
     * @param horaireDebut Horaire de début de l'activité
     * @param horaireFin Horaire de fin de l'activité
     * @return Liste des conflits détectés
     */
    List<Conflit> detecterChevauchements(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin);
    
    // ========== STATISTIQUES ==========
    
    /**
     * Compter le nombre total de conflits
     * @return Nombre total de conflits
     */
    int compterTousLesConflits();
    
    /**
     * Compter les conflits non résolus
     * @return Nombre de conflits non résolus
     */
    int compterConflitsNonResolus();
    
    /**
     * Compter les conflits par type
     * @param type Le type de conflit
     * @return Nombre de conflits de ce type
     */
    int compterParType(TypeConflit type);
    
    /**
     * Compter les conflits d'un utilisateur
     * @param idUtilisateur L'ID de l'utilisateur
     * @return Nombre de conflits de l'utilisateur
     */
    int compterParUtilisateur(Long idUtilisateur);
    
    /**
     * Obtenir le taux de résolution des conflits
     * @return Pourcentage de conflits résolus (0-100)
     */
    double getTauxResolution();
    
    /**
     * Obtenir les statistiques des conflits par type
     * @return Map avec le type de conflit en clé et le nombre en valeur
     */
    java.util.Map<TypeConflit, Integer> getStatistiquesParType();
    
    // ========== OPÉRATIONS DE MAINTENANCE ==========
    
    /**
     * Supprimer les conflits résolus avant une certaine date
     * @param dateAvant Date limite (les conflits résolus avant seront supprimés)
     * @return Nombre de conflits supprimés
     */
    int supprimerConflitsResolusAvant(LocalDateTime dateAvant);
    
    /**
     * Archiver les anciens conflits
     * @param dateAvant Date limite pour l'archivage
     * @return Nombre de conflits archivés
     */
    int archiverConflitsAvant(LocalDateTime dateAvant);
    
    /**
     * Rechercher les conflits par mot-clé (recherche dans les descriptions liées)
     * @param motCle Le mot-clé à rechercher
     * @return Liste des conflits correspondants
     */
    List<Conflit> rechercherParMotCle(String motCle);
    
    /**
     * Obtenir les conflits les plus récents
     * @param limite Nombre maximum de conflits à récupérer
     * @return Liste des conflits les plus récents
     */
    List<Conflit> getConflitsRecents(int limite);
    
    /**
     * Obtenir les conflits critiques (non résolus et de haute priorité)
     * @return Liste des conflits critiques
     */
    List<Conflit> getConflitsCritiques();
}

package service;

import entities.Contrainte;
import entities.StatutContrainte;
import entities.TypeContrainte;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface Service pour la gestion des contraintes.
 * Elle définit les opérations métier liées aux contraintes.
 */
public interface ContrainteService {
    
    /**
     * Ajoute une nouvelle contrainte
     */
    Long ajouterContrainte(Contrainte contrainte);
    
    /**
     * Modifie une contrainte existante
     */
    boolean modifierContrainte(Contrainte contrainte);
    
    /**
     * Supprime une contrainte
     */
    boolean supprimerContrainte(Long idContrainte);
    
    /**
     * Récupère une contrainte par son ID
     */
    Optional<Contrainte> getContrainteById(Long idContrainte);
    
    /**
     * Récupère toutes les contraintes
     */
    List<Contrainte> getToutesLesContraintes();
    
    /**
     * Récupère les contraintes actives
     */
    List<Contrainte> getContraintesActives();
    
    /**
     * Récupère les contraintes désactivées
     */
    List<Contrainte> getContraintesDesactives();
    
    /**
     * Récupère les contraintes par statut
     */
    List<Contrainte> getContraintesByStatut(StatutContrainte statut);
    
    /**
     * Récupère les contraintes par période horaire
     */
    List<Contrainte> getContraintesByPeriode(LocalTime heureDebut, LocalTime heureFin);
    
    /**
     * Récupère les contraintes répétitives
     */
    List<Contrainte> getContraintesRepetitives();
    
    /**
     * Récupère les contraintes non répétitives
     */
    List<Contrainte> getContraintesNonRepetitives();
    
    /**
     * Récupère les contraintes par type
     */
    List<Contrainte> getContraintesByType(TypeContrainte type);
    
    /**
     * Active une contrainte
     */
    boolean activerContrainte(Long idContrainte);
    
    /**
     * Désactive une contrainte
     */
    boolean desactiverContrainte(Long idContrainte);
    
    /**
     * Change le statut d'une contrainte
     */
    boolean changerStatutContrainte(Long idContrainte, StatutContrainte newStatut);
    
    /**
     * Compte le nombre total de contraintes
     */
    int compterToutesLesContraintes();
    
    /**
     * Compte les contraintes par statut
     */
    int compterContraintesByStatut(StatutContrainte statut);
    
    /**
     * Compte les contraintes actives
     */
    int compterContraintesActives();
    
    /**
     * Compte les contraintes désactivées
     */
    int compterContraintesDesactives();
    
    /**
     * Vérifie si une contrainte est en conflit avec une plage horaire
     */
    boolean estEnConflit(Contrainte contrainte, LocalTime heureDebut, LocalTime heureFin);
}

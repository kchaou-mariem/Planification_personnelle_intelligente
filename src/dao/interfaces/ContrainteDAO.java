package dao.interfaces;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import entities.Contrainte;
import entities.StatutContrainte;
import entities.TypeContrainte;

/**
 * Interface DAO pour la table `contrainte`.
 */
public interface ContrainteDAO {

    int ajouter(Contrainte contrainte);
    boolean modifier(Contrainte contrainte);
    boolean supprimer(int idContrainte);
    Optional<Contrainte> getById(int idContrainte);
    List<Contrainte> getAll();

    // Requêtes utilitaires
    //List<Contrainte> getByActivite(Long idActivite);
    //List<Contrainte> getByUtilisateur(Long idUtilisateur);

    List<Contrainte> getByPeriode(LocalTime heureDebut, LocalTime heureFin);

    List<Contrainte> getRepetitives();
    List<Contrainte> getNonRepetitives();

    int compterToutesLesContraintes();
    //int compterParUtilisateur(Long idUtilisateur);

    // Méthodes pour gérer le statut des contraintes
    List<Contrainte> getContraintesByStatut(StatutContrainte statut);
    List<Contrainte> getContraintesActives();
    List<Contrainte> getContraintesDesactives();
    int compterContraintesByStatut(StatutContrainte statut);

}
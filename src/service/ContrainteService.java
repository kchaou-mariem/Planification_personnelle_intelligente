package service;

import java.time.LocalTime;
import java.util.List;

import entities.Contrainte;
import entities.StatutContrainte;
import entities.TypeContrainte;

public interface ContrainteService {

	// Méthodes appelées par le Controller
	Contrainte getById(int id);

	List<Contrainte> getAll();

	List<Contrainte> getByUtilisateur(int utilisateurId);

	List<Contrainte> getContraintesActives(int utilisateurId);

	boolean ajouter(Contrainte contrainte);

	boolean modifier(Contrainte contrainte);

	boolean supprimer(int id);

	boolean toggleStatut(int id);

	// Méthodes supplémentaires (garder les existantes avec userId)
	List<Contrainte> getContraintesDesactives(int utilisateurId);

	List<Contrainte> getContraintesByStatut(int utilisateurId, StatutContrainte statut);

	List<Contrainte> getContraintesByPeriode(int utilisateurId, LocalTime heureDebut, LocalTime heureFin);

	List<Contrainte> getContraintesRepetitives(int utilisateurId);

	List<Contrainte> getContraintesNonRepetitives(int utilisateurId);

	List<Contrainte> getContraintesByType(int utilisateurId, TypeContrainte type);

	boolean activerContrainte(int idContrainte, int utilisateurId);

	boolean desactiverContrainte(int idContrainte, int utilisateurId);

	boolean changerStatutContrainte(int idContrainte, int utilisateurId, StatutContrainte newStatut);

	int compterContraintesByStatut(int utilisateurId, StatutContrainte statut);

	int compterContraintesActives(int utilisateurId);

	int compterContraintesDesactives(int utilisateurId);

	boolean estEnConflit(Contrainte contrainte, LocalTime heureDebut, LocalTime heureFin);
}
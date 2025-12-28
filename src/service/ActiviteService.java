package service;

import java.time.LocalDateTime;
import java.util.List;

import entities.Activite;
import entities.Contrainte;
import entities.TypeActivite;

public interface ActiviteService {

        // ========== MÉTHODES DU CONTROLLER ==========
        Activite getById(Long id);

        List<Activite> getAll();

        List<Activite> getByUtilisateur(Long utilisateurId);

        List<Activite> getByType(Long utilisateurId, TypeActivite type);

        boolean ajouter(Activite activite);

        boolean modifier(Activite activite);

        boolean supprimer(Long id);

        // ========== MÉTHODES AVANCÉES AVEC userId ==========
        Long creerActivite(Activite activite);

        boolean mettreAJourActivite(Activite activite);

        boolean supprimerActivite(Long idActivite, Long utilisateurId);

        List<Activite> obtenirActivitesUtilisateurDansLaPeriode(Long utilisateurId, LocalDateTime dateDebut,
                        LocalDateTime dateFin);

        List<Activite> rechercherActivitesUtilisateur(Long utilisateurId, String motCle);

        boolean verifierChevauchement(Long utilisateurId, LocalDateTime horaireDebut, LocalDateTime horaireFin);

        List<Activite> obtenirActivitesChevauchantes(Long utilisateurId, LocalDateTime horaireDebut,
                        LocalDateTime horaireFin);

        boolean validerHoraires(LocalDateTime horaireDebut, LocalDateTime horaireFin);

        boolean validerPriorite(int priorite);

        int obtenirNombreActivitesUtilisateur(Long utilisateurId);

        List<Activite> obtenirActivitesDeadlineProche(Long utilisateurId, int joursAvance);

        List<Activite> obtenirActivitesHautePriorite(Long utilisateurId);

        List<Activite> obtenirActivitesRecentes(Long utilisateurId, int nombre);

        /**
         * Valider un planning complet en vérifiant qu'il n'y a pas de chevauchements
         * entre activités et qu'aucune contrainte n'est violée
         * 
         * @param activites   Liste des activités à valider
         * @param contraintes Liste des contraintes actives à respecter
         * @return true si le planning est valide, false sinon
         */
        boolean planningValide(List<Activite> activites, List<Contrainte> contraintes);

        /**
         * Calculer le score de qualité d'un planning basé sur plusieurs critères:
         * - Priorité des activités
         * - Respect des deadlines
         * - Équilibre travail/repos/sport
         * - Horaires appropriés
         * 
         * @param activites Liste des activités du planning
         * @return Score du planning (plus élevé = meilleur)
         */
        double calculerScorePlanning(List<Activite> activites);

        /**
         * ✅ MÉTHODE PUBLIQUE
         * Calculer le score d'un planning en incluant des pénalités pour les conflits.
         * Cette méthode est utilisée pour l'affichage et l'évaluation réelle du
         * planning.
         * 
         * @param activites   La liste des activités du planning
         * @param contraintes La liste des contraintes à respecter
         * @return Le score du planning (avec pénalités pour conflits)
         */
        double calculerScoreAvecConflits(List<Activite> activites, List<Contrainte> contraintes);

        /**
         * Créer une mutation d'un planning en décalant aléatoirement une activité de
         * ±30 minutes.
         * Utile pour les algorithmes d'optimisation (algorithmes génétiques, recherche
         * locale).
         * 
         * @param activites Liste des activités de base
         * @return Nouvelle liste avec une activité décalée
         */
        List<Activite> muterPlanning(List<Activite> activites);

        /**
         * Optimiser un planning en utilisant un algorithme de recherche locale.
         * Génère des mutations successives et conserve les améliorations.
         * 
         * @param activites   Liste des activités à optimiser
         * @param contraintes Liste des contraintes à respecter
         * @param iterations  Nombre d'itérations de l'algorithme
         * @return Planning optimisé
         */
        List<Activite> optimiserPlanning(List<Activite> activites, List<Contrainte> contraintes, int iterations);
}
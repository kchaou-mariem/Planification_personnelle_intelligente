package service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service pour la consultation des statistiques utilisateur.
 * Calcule :
 * - Temps par type d'activité
 * - Équilibre travail/repos
 * - Score de fatigue estimé
 * - Autres métriques de productivité
 */
public interface StatistiqueService {

    // ========== TEMPS PAR TYPE D'ACTIVITÉ ==========

    /**
     * Calcule le temps total passé par type d'activité pour un utilisateur.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Map avec le type d'activité et le nombre d'heures
     */
    Map<String, Double> getTempsParTypeActivite(Long idUtilisateur);

    /**
     * Calcule le temps total passé par type d'activité pour une période donnée.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @param dateDebut     Date de début
     * @param dateFin       Date de fin
     * @return Map avec le type d'activité et le nombre d'heures
     */
    Map<String, Double> getTempsParTypeActivitePeriode(Long idUtilisateur, LocalDateTime dateDebut,
            LocalDateTime dateFin);

    /**
     * Calcule le pourcentage de temps par type d'activité.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Map avec le type d'activité et le pourcentage (0-100)
     */
    Map<String, Double> getPourcentageParTypeActivite(Long idUtilisateur);

    // ========== ÉQUILIBRE TRAVAIL/REPOS ==========

    /**
     * Calcule le ratio travail/repos pour un utilisateur.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Ratio (heures de travail / heures de repos)
     */
    double getRatioTravailRepos(Long idUtilisateur);

    /**
     * Calcule le ratio travail/repos pour une période donnée.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @param dateDebut     Date de début
     * @param dateFin       Date de fin
     * @return Ratio (heures de travail / heures de repos)
     */
    double getRatioTravailReposPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin);

    /**
     * Évalue le niveau d'équilibre travail/repos.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Niveau (Excellent, Bon, Moyen, Faible, Critique)
     */
    String getNiveauEquilibre(Long idUtilisateur);

    /**
     * Calcule les heures de travail totales.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Nombre d'heures de travail
     */
    double getHeuresTravail(Long idUtilisateur);

    /**
     * Calcule les heures de repos totales.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Nombre d'heures de repos
     */
    double getHeuresRepos(Long idUtilisateur);

    // ========== SCORE DE FATIGUE ==========

    /**
     * Calcule le score de fatigue estimé (0-100).
     * Plus le score est élevé, plus la fatigue est importante.
     * Basé sur :
     * - Ratio travail/repos
     * - Nombre d'heures de travail consécutives
     * - Respect des contraintes de repos
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Score de fatigue (0-100)
     */
    double getScoreFatigue(Long idUtilisateur);

    /**
     * Évalue le niveau de fatigue.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Niveau (Faible, Modéré, Élevé, Critique)
     */
    String getNiveauFatigue(Long idUtilisateur);

    /**
     * Calcule le nombre d'heures de travail consécutives maximales.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Nombre d'heures maximales sans pause
     */
    double getHeuresTravailConsecutivesMax(Long idUtilisateur);

    // ========== STATISTIQUES GÉNÉRALES ==========

    /**
     * Calcule le nombre total d'activités planifiées.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Nombre d'activités
     */
    int getNombreActivites(Long idUtilisateur);

    /**
     * Calcule le nombre d'activités par jour en moyenne.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Moyenne d'activités par jour
     */
    double getMoyenneActivitesParJour(Long idUtilisateur);

    /**
     * Calcule le nombre d'heures planifiées totales.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Nombre d'heures total
     */
    double getHeuresPlannifieesTotal(Long idUtilisateur);

    /**
     * Calcule le nombre d'activités urgentes (deadline proche).
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @param joursAvant    Nombre de jours avant la deadline
     * @return Nombre d'activités urgentes
     */
    int getNombreActivitesUrgentes(Long idUtilisateur, int joursAvant);

    // ========== PRODUCTIVITÉ ==========

    /**
     * Calcule le score de productivité global (0-100).
     * Basé sur :
     * - Respect des deadlines
     * - Équilibre travail/repos
     * - Nombre de conflits résolus
     * - Score de fatigue
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Score de productivité (0-100)
     */
    double getScoreProductivite(Long idUtilisateur);

    /**
     * Calcule le taux de respect des deadlines.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Pourcentage (0-100)
     */
    double getTauxRespectDeadlines(Long idUtilisateur);

    // ========== TENDANCES ==========

    /**
     * Calcule l'évolution du nombre d'activités par semaine.
     * 
     * @param idUtilisateur  ID de l'utilisateur
     * @param nombreSemaines Nombre de semaines à analyser
     * @return Map avec le numéro de semaine et le nombre d'activités
     */
    Map<Integer, Integer> getTendanceActivitesParSemaine(Long idUtilisateur, int nombreSemaines);

    /**
     * Calcule l'évolution du score de fatigue par semaine.
     * 
     * @param idUtilisateur  ID de l'utilisateur
     * @param nombreSemaines Nombre de semaines à analyser
     * @return Map avec le numéro de semaine et le score de fatigue
     */
    Map<Integer, Double> getTendanceFatigueParSemaine(Long idUtilisateur, int nombreSemaines);

    // ========== RAPPORT COMPLET ==========

    /**
     * Génère un rapport statistique complet pour un utilisateur.
     * 
     * @param idUtilisateur ID de l'utilisateur
     * @return Objet contenant toutes les statistiques
     */
    RapportStatistique getRapportComplet(Long idUtilisateur);

    /**
     * Classe interne pour le rapport statistique complet.
     */
    class RapportStatistique {
        // Temps par type
        private Map<String, Double> tempsParType;
        private Map<String, Double> pourcentageParType;

        // Équilibre
        private double ratioTravailRepos;
        private String niveauEquilibre;
        private double heuresTravail;
        private double heuresRepos;

        // Fatigue
        private double scoreFatigue;
        private String niveauFatigue;
        private double heuresTravailConsecutivesMax;

        // Général
        private int nombreActivites;
        private double moyenneActivitesParJour;
        private double heuresPlannifieesTotal;
        private int nombreActivitesUrgentes;

        // Productivité
        private double scoreProductivite;
        private double tauxRespectDeadlines;

        // Conflits
        private int nombreConflitsTotal;
        private int nombreConflitsNonResolus;
        private double tauxResolutionConflits;

        // Constructeurs
        public RapportStatistique() {
        }

        // Getters et Setters
        public Map<String, Double> getTempsParType() {
            return tempsParType;
        }

        public void setTempsParType(Map<String, Double> tempsParType) {
            this.tempsParType = tempsParType;
        }

        public Map<String, Double> getPourcentageParType() {
            return pourcentageParType;
        }

        public void setPourcentageParType(Map<String, Double> pourcentageParType) {
            this.pourcentageParType = pourcentageParType;
        }

        public double getRatioTravailRepos() {
            return ratioTravailRepos;
        }

        public void setRatioTravailRepos(double ratioTravailRepos) {
            this.ratioTravailRepos = ratioTravailRepos;
        }

        public String getNiveauEquilibre() {
            return niveauEquilibre;
        }

        public void setNiveauEquilibre(String niveauEquilibre) {
            this.niveauEquilibre = niveauEquilibre;
        }

        public double getHeuresTravail() {
            return heuresTravail;
        }

        public void setHeuresTravail(double heuresTravail) {
            this.heuresTravail = heuresTravail;
        }

        public double getHeuresRepos() {
            return heuresRepos;
        }

        public void setHeuresRepos(double heuresRepos) {
            this.heuresRepos = heuresRepos;
        }

        public double getScoreFatigue() {
            return scoreFatigue;
        }

        public void setScoreFatigue(double scoreFatigue) {
            this.scoreFatigue = scoreFatigue;
        }

        public String getNiveauFatigue() {
            return niveauFatigue;
        }

        public void setNiveauFatigue(String niveauFatigue) {
            this.niveauFatigue = niveauFatigue;
        }

        public double getHeuresTravailConsecutivesMax() {
            return heuresTravailConsecutivesMax;
        }

        public void setHeuresTravailConsecutivesMax(double heuresTravailConsecutivesMax) {
            this.heuresTravailConsecutivesMax = heuresTravailConsecutivesMax;
        }

        public int getNombreActivites() {
            return nombreActivites;
        }

        public void setNombreActivites(int nombreActivites) {
            this.nombreActivites = nombreActivites;
        }

        public double getMoyenneActivitesParJour() {
            return moyenneActivitesParJour;
        }

        public void setMoyenneActivitesParJour(double moyenneActivitesParJour) {
            this.moyenneActivitesParJour = moyenneActivitesParJour;
        }

        public double getHeuresPlannifieesTotal() {
            return heuresPlannifieesTotal;
        }

        public void setHeuresPlannifieesTotal(double heuresPlannifieesTotal) {
            this.heuresPlannifieesTotal = heuresPlannifieesTotal;
        }

        public int getNombreActivitesUrgentes() {
            return nombreActivitesUrgentes;
        }

        public void setNombreActivitesUrgentes(int nombreActivitesUrgentes) {
            this.nombreActivitesUrgentes = nombreActivitesUrgentes;
        }

        public double getScoreProductivite() {
            return scoreProductivite;
        }

        public void setScoreProductivite(double scoreProductivite) {
            this.scoreProductivite = scoreProductivite;
        }

        public double getTauxRespectDeadlines() {
            return tauxRespectDeadlines;
        }

        public void setTauxRespectDeadlines(double tauxRespectDeadlines) {
            this.tauxRespectDeadlines = tauxRespectDeadlines;
        }

        public int getNombreConflitsTotal() {
            return nombreConflitsTotal;
        }

        public void setNombreConflitsTotal(int nombreConflitsTotal) {
            this.nombreConflitsTotal = nombreConflitsTotal;
        }

        public int getNombreConflitsNonResolus() {
            return nombreConflitsNonResolus;
        }

        public void setNombreConflitsNonResolus(int nombreConflitsNonResolus) {
            this.nombreConflitsNonResolus = nombreConflitsNonResolus;
        }

        public double getTauxResolutionConflits() {
            return tauxResolutionConflits;
        }

        public void setTauxResolutionConflits(double tauxResolutionConflits) {
            this.tauxResolutionConflits = tauxResolutionConflits;
        }
    }
}
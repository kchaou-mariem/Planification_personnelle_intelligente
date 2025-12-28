package service.impl;

import dao.impl.ActiviteDAOImpl;
import dao.impl.ConflitDAOImpl;
import dao.interfaces.ActiviteDAO;
import dao.interfaces.ConflitDAO;
import entities.Activite;
import entities.Conflit;
import entities.TypeActivite;
import service.ConflitService;
import service.StatistiqueService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service de statistiques.
 */
public class StatistiqueServiceImpl implements StatistiqueService {

    private final ActiviteDAO activiteDAO;
    private final ConflitDAO conflitDAO;
    private final ConflitService conflitService;

    public StatistiqueServiceImpl() {
        this.activiteDAO = new ActiviteDAOImpl();
        this.conflitDAO = new ConflitDAOImpl();
        this.conflitService = new ConflitServiceImpl();
    }

    // ========== TEMPS PAR TYPE D'ACTIVITÉ ==========

    @Override
    public Map<String, Double> getTempsParTypeActivite(Long idUtilisateur) {
        // Par défaut : statistiques de la semaine en cours
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par semaine en cours
        List<Activite> activitesSemaine = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .collect(Collectors.toList());

        return calculerTempsParType(activitesSemaine);
    }

    @Override
    public Map<String, Double> getTempsParTypeActivitePeriode(Long idUtilisateur, LocalDateTime dateDebut,
            LocalDateTime dateFin) {
        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par période
        List<Activite> activitesPeriode = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(dateDebut) && !a.getHoraireDebut().isAfter(dateFin))
                .collect(Collectors.toList());

        return calculerTempsParType(activitesPeriode);
    }

    @Override
    public Map<String, Double> getPourcentageParTypeActivite(Long idUtilisateur) {
        Map<String, Double> tempsParType = getTempsParTypeActivite(idUtilisateur); // Utilise déjà la semaine en cours
        double total = tempsParType.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total == 0) {
            return new HashMap<>();
        }

        Map<String, Double> pourcentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : tempsParType.entrySet()) {
            double pourcentage = Math.round((entry.getValue() / total) * 10000.0) / 100.0;
            pourcentages.put(entry.getKey(), pourcentage);
        }

        return pourcentages;
    }

    // ========== ÉQUILIBRE TRAVAIL/REPOS ==========

    @Override
    public double getRatioTravailRepos(Long idUtilisateur) {
        double heuresTravail = getHeuresTravail(idUtilisateur); // Utilise déjà la semaine en cours
        double heuresRepos = getHeuresRepos(idUtilisateur); // Utilise déjà la semaine en cours

        if (heuresRepos == 0) {
            // Si pas de repos : retourner 999 au lieu de Double.MAX_VALUE
            return heuresTravail > 0 ? 999.0 : 0.0;
        }

        return Math.round((heuresTravail / heuresRepos) * 100.0) / 100.0;
    }

    @Override
    public double getRatioTravailReposPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par période
        List<Activite> activitesPeriode = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(dateDebut) && !a.getHoraireDebut().isAfter(dateFin))
                .collect(Collectors.toList());

        double heuresTravail = calculerHeuresTravail(activitesPeriode);
        double heuresRepos = calculerHeuresRepos(activitesPeriode);

        if (heuresRepos == 0) {
            return heuresTravail > 0 ? 999.0 : 0.0;
        }

        return Math.round((heuresTravail / heuresRepos) * 100.0) / 100.0;
    }

    @Override
    public String getNiveauEquilibre(Long idUtilisateur) {
        double ratio = getRatioTravailRepos(idUtilisateur);

        // Cas spécial : pas de repos du tout
        if (ratio >= 999.0)
            return "Critique";

        // Ratio idéal : 2-3 (2-3h de travail pour 1h de repos)
        if (ratio >= 2.0 && ratio <= 3.0)
            return "Excellent";
        if (ratio >= 1.5 && ratio < 2.0)
            return "Bon";
        if (ratio >= 3.0 && ratio <= 4.0)
            return "Bon";
        if (ratio >= 1.0 && ratio < 1.5)
            return "Moyen";
        if (ratio > 4.0 && ratio <= 6.0)
            return "Moyen";
        if (ratio < 1.0)
            return "Faible";
        return "Critique";
    }

    @Override
    public double getHeuresTravail(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par semaine en cours
        List<Activite> activitesSemaine = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .collect(Collectors.toList());

        return calculerHeuresTravail(activitesSemaine);
    }

    @Override
    public double getHeuresRepos(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par semaine en cours
        List<Activite> activitesSemaine = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .collect(Collectors.toList());

        return calculerHeuresRepos(activitesSemaine);
    }

    // ========== SCORE DE FATIGUE ==========

    @Override
    public double getScoreFatigue(Long idUtilisateur) {
        double ratio = getRatioTravailRepos(idUtilisateur); // Semaine en cours
        double heuresConsecutives = getHeuresTravailConsecutivesMax(idUtilisateur); // Semaine en cours
        double heuresTravail = getHeuresTravail(idUtilisateur); // Semaine en cours

        double scoreFatigue = 0.0;

        // Facteur 1 : Ratio travail/repos (40% du score)
        if (ratio > 6.0)
            scoreFatigue += 40.0;
        else if (ratio > 4.0)
            scoreFatigue += 30.0;
        else if (ratio > 3.0)
            scoreFatigue += 15.0;
        else if (ratio < 1.5)
            scoreFatigue += 20.0;

        // Facteur 2 : Heures de travail consécutives (35% du score)
        if (heuresConsecutives > 8.0)
            scoreFatigue += 35.0;
        else if (heuresConsecutives > 6.0)
            scoreFatigue += 25.0;
        else if (heuresConsecutives > 4.0)
            scoreFatigue += 15.0;

        // Facteur 3 : Volume total de travail (25% du score)
        if (heuresTravail > 60.0)
            scoreFatigue += 25.0;
        else if (heuresTravail > 45.0)
            scoreFatigue += 18.0;
        else if (heuresTravail > 35.0)
            scoreFatigue += 10.0;

        return Math.min(100.0, Math.round(scoreFatigue * 100.0) / 100.0);
    }

    @Override
    public String getNiveauFatigue(Long idUtilisateur) {
        double score = getScoreFatigue(idUtilisateur);

        if (score < 25.0)
            return "Faible";
        if (score < 50.0)
            return "Modéré";
        if (score < 75.0)
            return "Élevé";
        return "Critique";
    }

    @Override
    public double getHeuresTravailConsecutivesMax(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer et trier les activités de travail de la semaine en cours
        List<Activite> activitesTravail = activites.stream()
                .filter(a -> a.getHoraireDebut() != null && a.getHoraireFin() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .filter(a -> a.getTypeActivite() == TypeActivite.Travail ||
                        a.getTypeActivite() == TypeActivite.Etude)
                .sorted(Comparator.comparing(Activite::getHoraireDebut))
                .collect(Collectors.toList());

        if (activitesTravail.isEmpty())
            return 0.0;

        double maxConsecutif = 0.0;
        double consecutifActuel = 0.0;
        LocalDateTime finPrecedent = null;

        for (Activite activite : activitesTravail) {
            double duree = Duration.between(activite.getHoraireDebut(), activite.getHoraireFin()).toMinutes() / 60.0;

            if (finPrecedent == null || Duration.between(finPrecedent, activite.getHoraireDebut()).toMinutes() <= 30) {
                // Activités consécutives (moins de 30 min d'écart)
                consecutifActuel += duree;
            } else {
                // Rupture dans la séquence
                maxConsecutif = Math.max(maxConsecutif, consecutifActuel);
                consecutifActuel = duree;
            }

            finPrecedent = activite.getHoraireFin();
        }

        maxConsecutif = Math.max(maxConsecutif, consecutifActuel);
        return Math.round(maxConsecutif * 100.0) / 100.0;
    }

    // ========== STATISTIQUES GÉNÉRALES ==========

    @Override
    public int getNombreActivites(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Compter uniquement les activités de la semaine en cours
        return (int) activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .count();
    }

    @Override
    public double getMoyenneActivitesParJour(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par semaine en cours
        List<Activite> activitesSemaine = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .collect(Collectors.toList());

        if (activitesSemaine.isEmpty())
            return 0.0;

        // Compter le nombre de jours écoulés depuis le début de la semaine
        long joursEcoules = ChronoUnit.DAYS.between(debutSemaine.toLocalDate(), maintenant.toLocalDate()) + 1;

        return Math.round((double) activitesSemaine.size() / joursEcoules * 100.0) / 100.0;
    }

    @Override
    public double getHeuresPlannifieesTotal(Long idUtilisateur) {
        LocalDateTime debutSemaine = getDebutSemaineEnCours();
        LocalDateTime maintenant = LocalDateTime.now();

        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        // Filtrer par semaine en cours
        List<Activite> activitesSemaine = activites.stream()
                .filter(a -> a.getHoraireDebut() != null)
                .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine) && !a.getHoraireDebut().isAfter(maintenant))
                .collect(Collectors.toList());

        return calculerDureeTotal(activitesSemaine);
    }

    @Override
    public int getNombreActivitesUrgentes(Long idUtilisateur, int joursAvant) {
        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);
        LocalDateTime limite = LocalDateTime.now().plusDays(joursAvant);

        return (int) activites.stream()
                .filter(a -> a.getDeadline() != null)
                .filter(a -> a.getDeadline().isBefore(limite))
                .filter(a -> a.getDeadline().isAfter(LocalDateTime.now()))
                .count();
    }

    // ========== PRODUCTIVITÉ ==========

    @Override
    public double getScoreProductivite(Long idUtilisateur) {
        double tauxRespectDeadlines = getTauxRespectDeadlines(idUtilisateur);
        double scoreFatigue = getScoreFatigue(idUtilisateur);
        String equilibre = getNiveauEquilibre(idUtilisateur);
        double tauxResolutionConflits = conflitService.getTauxResolutionUtilisateur(idUtilisateur);

        double score = 0.0;

        // Facteur 1 : Respect des deadlines (40%)
        score += tauxRespectDeadlines * 0.4;

        // Facteur 2 : Équilibre (25%)
        switch (equilibre) {
            case "Excellent":
                score += 25.0;
                break;
            case "Bon":
                score += 20.0;
                break;
            case "Moyen":
                score += 12.0;
                break;
            case "Faible":
                score += 5.0;
                break;
        }

        // Facteur 3 : Fatigue inversée (20%)
        score += (100.0 - scoreFatigue) * 0.2;

        // Facteur 4 : Résolution des conflits (15%)
        score += tauxResolutionConflits * 0.15;

        return Math.min(100.0, Math.round(score * 100.0) / 100.0);
    }

    @Override
    public double getTauxRespectDeadlines(Long idUtilisateur) {
        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);

        List<Activite> avecDeadline = activites.stream()
                .filter(a -> a.getDeadline() != null)
                .collect(Collectors.toList());

        if (avecDeadline.isEmpty())
            return 100.0;

        long respectees = avecDeadline.stream()
                .filter(a -> a.getHoraireFin() != null)
                .filter(a -> !a.getHoraireFin().isAfter(a.getDeadline()))
                .count();

        return Math.round((double) respectees / avecDeadline.size() * 10000.0) / 100.0;
    }

    // ========== TENDANCES ==========

    @Override
    public Map<Integer, Integer> getTendanceActivitesParSemaine(Long idUtilisateur, int nombreSemaines) {
        List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);
        LocalDateTime maintenant = LocalDateTime.now();
        Map<Integer, Integer> tendance = new LinkedHashMap<>();

        for (int i = nombreSemaines - 1; i >= 0; i--) {
            LocalDateTime debutSemaine = maintenant.minusWeeks(i).with(java.time.DayOfWeek.MONDAY)
                    .truncatedTo(ChronoUnit.DAYS);
            LocalDateTime finSemaine = debutSemaine.plusDays(7);

            int count = (int) activites.stream()
                    .filter(a -> a.getHoraireDebut() != null)
                    .filter(a -> !a.getHoraireDebut().isBefore(debutSemaine)
                            && a.getHoraireDebut().isBefore(finSemaine))
                    .count();

            tendance.put(nombreSemaines - i, count);
        }

        return tendance;
    }

    @Override
    public Map<Integer, Double> getTendanceFatigueParSemaine(Long idUtilisateur, int nombreSemaines) {
        // Implémentation simplifiée : score actuel pour toutes les semaines
        // Pour une vraie implémentation, il faudrait stocker l'historique
        Map<Integer, Double> tendance = new LinkedHashMap<>();
        double scoreFatigue = getScoreFatigue(idUtilisateur);

        for (int i = 1; i <= nombreSemaines; i++) {
            tendance.put(i, scoreFatigue);
        }

        return tendance;
    }

    // ========== RAPPORT COMPLET ==========

    @Override
    public RapportStatistique getRapportComplet(Long idUtilisateur) {
        RapportStatistique rapport = new RapportStatistique();

        // Temps par type
        rapport.setTempsParType(getTempsParTypeActivite(idUtilisateur));
        rapport.setPourcentageParType(getPourcentageParTypeActivite(idUtilisateur));

        // Équilibre
        rapport.setRatioTravailRepos(getRatioTravailRepos(idUtilisateur));
        rapport.setNiveauEquilibre(getNiveauEquilibre(idUtilisateur));
        rapport.setHeuresTravail(getHeuresTravail(idUtilisateur));
        rapport.setHeuresRepos(getHeuresRepos(idUtilisateur));

        // Fatigue
        rapport.setScoreFatigue(getScoreFatigue(idUtilisateur));
        rapport.setNiveauFatigue(getNiveauFatigue(idUtilisateur));
        rapport.setHeuresTravailConsecutivesMax(getHeuresTravailConsecutivesMax(idUtilisateur));

        // Général
        rapport.setNombreActivites(getNombreActivites(idUtilisateur));
        rapport.setMoyenneActivitesParJour(getMoyenneActivitesParJour(idUtilisateur));
        rapport.setHeuresPlannifieesTotal(getHeuresPlannifieesTotal(idUtilisateur));
        rapport.setNombreActivitesUrgentes(getNombreActivitesUrgentes(idUtilisateur, 7));

        // Productivité
        rapport.setScoreProductivite(getScoreProductivite(idUtilisateur));
        rapport.setTauxRespectDeadlines(getTauxRespectDeadlines(idUtilisateur));

        // Conflits
        rapport.setNombreConflitsTotal(conflitService.compterConflitsUtilisateur(idUtilisateur));
        rapport.setNombreConflitsNonResolus(conflitService.compterConflitsNonResolusUtilisateur(idUtilisateur));
        rapport.setTauxResolutionConflits(conflitService.getTauxResolutionUtilisateur(idUtilisateur));

        return rapport;
    }

    // ========== MÉTHODES UTILITAIRES PRIVÉES ==========

    /**
     * Retourne le début de la semaine en cours (lundi à 00:00)
     */
    private LocalDateTime getDebutSemaineEnCours() {
        LocalDateTime maintenant = LocalDateTime.now();
        return maintenant.with(java.time.DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
    }

    private Map<String, Double> calculerTempsParType(List<Activite> activites) {
        Map<String, Double> tempsParType = new HashMap<>();

        for (Activite activite : activites) {
            if (activite.getHoraireDebut() != null && activite.getHoraireFin() != null) {
                String type = activite.getTypeActivite().toString();
                double duree = Duration.between(activite.getHoraireDebut(), activite.getHoraireFin()).toMinutes()
                        / 60.0;
                tempsParType.put(type, tempsParType.getOrDefault(type, 0.0) + duree);
            }
        }

        // Arrondir à 2 décimales
        tempsParType.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);

        return tempsParType;
    }

    private double calculerHeuresTravail(List<Activite> activites) {
        // Considère Travail, Etude, Sport, Loisirs comme "activités actives"
        return activites.stream()
                .filter(a -> a.getHoraireDebut() != null && a.getHoraireFin() != null)
                .filter(a -> a.getTypeActivite() == TypeActivite.Travail ||
                        a.getTypeActivite() == TypeActivite.Etude ||
                        a.getTypeActivite() == TypeActivite.Sport ||
                        a.getTypeActivite() == TypeActivite.Loisirs)
                .mapToDouble(a -> Duration.between(a.getHoraireDebut(), a.getHoraireFin()).toMinutes() / 60.0)
                .sum();
    }

    private double calculerHeuresRepos(List<Activite> activites) {
        return activites.stream()
                .filter(a -> a.getHoraireDebut() != null && a.getHoraireFin() != null)
                .filter(a -> a.getTypeActivite() == TypeActivite.Repos)
                .mapToDouble(a -> Duration.between(a.getHoraireDebut(), a.getHoraireFin()).toMinutes() / 60.0)
                .sum();
    }

    private double calculerDureeTotal(List<Activite> activites) {
        double total = activites.stream()
                .filter(a -> a.getHoraireDebut() != null && a.getHoraireFin() != null)
                .mapToDouble(a -> Duration.between(a.getHoraireDebut(), a.getHoraireFin()).toMinutes() / 60.0)
                .sum();

        return Math.round(total * 100.0) / 100.0;
    }
}
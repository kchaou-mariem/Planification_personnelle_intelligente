package service.impl;

import dao.interfaces.ActiviteDAO;
import dao.interfaces.ConflitDAO;
import dao.interfaces.ContrainteDAO;
import entities.Activite;
import entities.Conflit;
import entities.Contrainte;
import entities.TypeActivite;
import entities.TypeConflit;
import dao.impl.ActiviteDAOImpl;
import dao.impl.ConflitDAOImpl;
import dao.impl.ContrainteDAOImpl;
import service.ActiviteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActiviteServiceImpl implements ActiviteService {

    private final ActiviteDAO activiteDAO;
    private final ConflitDAO conflitDAO;
    private final ContrainteDAO contrainteDAO;

    public ActiviteServiceImpl() {
        this.activiteDAO = new ActiviteDAOImpl();
        this.conflitDAO = new ConflitDAOImpl();
        this.contrainteDAO = new ContrainteDAOImpl();
    }

    public ActiviteServiceImpl(ActiviteDAO activiteDAO) {
        this.activiteDAO = activiteDAO;
        this.conflitDAO = new ConflitDAOImpl();
        this.contrainteDAO = new ContrainteDAOImpl();
    }

    // ========== MÉTHODES DU CONTROLLER ==========

    @Override
    public Activite getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return activiteDAO.getById(id).orElse(null);
    }

    @Override
    public List<Activite> getAll() {
        return activiteDAO.getAll();
    }

    @Override
    public List<Activite> getByUtilisateur(Long utilisateurId) {
        if (utilisateurId == null || utilisateurId <= 0) {
            return List.of();
        }
        return activiteDAO.getByUtilisateur(utilisateurId);
    }

    @Override
    public List<Activite> getByType(Long utilisateurId, TypeActivite type) {
        if (utilisateurId == null || utilisateurId <= 0 || type == null) {
            return List.of();
        }
        return activiteDAO.getByTypeAndUtilisateur(utilisateurId, type);
    }

    @Override
    public boolean ajouter(Activite activite) {
        if (activite == null) {
            System.err.println("Erreur: activité null");
            return false;
        }

        if (activite.getTitre() == null || activite.getTitre().trim().isEmpty()) {
            System.err.println("Erreur: titre requis");
            return false;
        }

        if (activite.getTypeActivite() == null) {
            System.err.println("Erreur: type requis");
            return false;
        }

        if (!validerHoraires(activite.getHoraireDebut(), activite.getHoraireFin())) {
            return false;
        }

        if (activite.getPriorite() != null && !validerPriorite(activite.getPriorite())) {
            return false;
        }

        if (activite.getDeadline() == null) {
            System.err.println("Erreur: deadline requis");
            return false;
        }

        if (activite.getIdUtilisateur() == null || activite.getIdUtilisateur() <= 0) {
            System.err.println("Erreur: ID utilisateur invalide");
            return false;
        }

        Long id = creerActivite(activite);
        return id != null && id > 0;
    }

    @Override
    public boolean modifier(Activite activite) {
        return mettreAJourActivite(activite);
    }

    @Override
    public boolean supprimer(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return activiteDAO.supprimer(id);
    }

    // ========== MÉTHODES AVANCÉES AVEC userId ==========

    @Override
    public Long creerActivite(Activite activite) {
        if (!validerActivite(activite)) {
            return -1L;
        }

        // Créer l'activité dans la base de données
        Long idActivite = activiteDAO.ajouter(activite);

        if (idActivite != null && idActivite > 0) {
            activite.setIdActivite(idActivite);

            // Détecter et créer automatiquement les conflits
            List<Activite> activitesChevauchantes = activiteDAO.getActivitesChevauchantesUtilisateur(
                    activite.getIdUtilisateur(),
                    activite.getHoraireDebut(),
                    activite.getHoraireFin());

            creerConflitsSiNecessaire(activite, activitesChevauchantes);

            return idActivite;
        }

        return -1L;
    }

    @Override
    public boolean mettreAJourActivite(Activite activite) {
        if (!validerActivite(activite)) {
            return false;
        }

        // Modifier l'activité dans la base de données
        boolean succes = activiteDAO.modifier(activite);

        if (succes) {
            // Détecter et créer automatiquement les conflits
            List<Activite> activitesChevauchantes = activiteDAO.getActivitesChevauchantesUtilisateur(
                    activite.getIdUtilisateur(),
                    activite.getHoraireDebut(),
                    activite.getHoraireFin());

            creerConflitsSiNecessaire(activite, activitesChevauchantes);
        }

        return succes;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Modifier une activité SANS créer automatiquement de
     * conflits.
     * Utilisée pendant l'optimisation pour éviter de recréer des conflits à chaque
     * modification.
     * 
     * @param activite L'activité à modifier
     * @return true si la modification a réussi
     */
    public boolean modifierSansDetectionConflits(Activite activite) {
        if (!validerActivite(activite)) {
            return false;
        }

        // Modifier directement sans détecter les conflits
        return activiteDAO.modifier(activite);
    }

    @Override
    public boolean supprimerActivite(Long idActivite, Long utilisateurId) {
        if (idActivite == null || idActivite <= 0 || utilisateurId == null || utilisateurId <= 0) {
            return false;
        }

        Optional<Activite> activiteOpt = activiteDAO.getById(idActivite);
        if (activiteOpt.isEmpty() || !activiteOpt.get().getIdUtilisateur().equals(utilisateurId)) {
            return false;
        }

        return activiteDAO.supprimer(idActivite);
    }

    @Override
    public List<Activite> obtenirActivitesUtilisateurDansLaPeriode(Long utilisateurId, LocalDateTime dateDebut,
            LocalDateTime dateFin) {
        if (utilisateurId == null || utilisateurId <= 0 || dateDebut == null || dateFin == null
                || dateDebut.isAfter(dateFin)) {
            return List.of();
        }
        return activiteDAO.getByUtilisateurAndPeriode(utilisateurId, dateDebut, dateFin);
    }

    @Override
    public List<Activite> rechercherActivitesUtilisateur(Long utilisateurId, String motCle) {
        if (utilisateurId == null || utilisateurId <= 0 || motCle == null || motCle.trim().isEmpty()) {
            return List.of();
        }
        return activiteDAO.rechercherParMotCleUtilisateur(utilisateurId, motCle.trim());
    }

    @Override
    public boolean verifierChevauchement(Long utilisateurId, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        if (utilisateurId == null || utilisateurId <= 0 || horaireDebut == null || horaireFin == null) {
            return false;
        }

        if (!validerHoraires(horaireDebut, horaireFin)) {
            return false;
        }

        List<Activite> chevauchantes = activiteDAO.getActivitesChevauchantesUtilisateur(
                utilisateurId,
                horaireDebut,
                horaireFin);

        return !chevauchantes.isEmpty();
    }

    @Override
    public List<Activite> obtenirActivitesChevauchantes(Long utilisateurId, LocalDateTime horaireDebut,
            LocalDateTime horaireFin) {
        if (utilisateurId == null || utilisateurId <= 0 || horaireDebut == null || horaireFin == null) {
            return List.of();
        }

        if (!validerHoraires(horaireDebut, horaireFin)) {
            return List.of();
        }

        return activiteDAO.getActivitesChevauchantesUtilisateur(utilisateurId, horaireDebut, horaireFin);
    }

    @Override
    public boolean validerHoraires(LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        if (horaireDebut == null || horaireFin == null) {
            System.err.println("Erreur : Les horaires ne peuvent pas être null");
            return false;
        }

        if (horaireDebut.isAfter(horaireFin)) {
            System.err.println("Erreur : L'horaire de début doit être avant l'horaire de fin");
            return false;
        }

        if (horaireDebut.isEqual(horaireFin)) {
            System.err.println("Erreur : Les horaires de début et fin ne peuvent pas être identiques");
            return false;
        }

        return true;
    }

    @Override
    public boolean validerPriorite(int priorite) {
        if (priorite < 1 || priorite > 10) {
            System.err.println("Erreur : La priorité doit être entre 1 et 10");
            return false;
        }
        return true;
    }

    @Override
    public int obtenirNombreActivitesUtilisateur(Long utilisateurId) {
        if (utilisateurId == null || utilisateurId <= 0) {
            return 0;
        }
        return activiteDAO.compterActivitesUtilisateur(utilisateurId);
    }

    @Override
    public List<Activite> obtenirActivitesDeadlineProche(Long utilisateurId, int joursAvance) {
        if (utilisateurId == null || utilisateurId <= 0 || joursAvance < 0) {
            return List.of();
        }
        return activiteDAO.getActivitesAvecDeadlineProche(joursAvance).stream()
                .filter(a -> a.getIdUtilisateur().equals(utilisateurId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Activite> obtenirActivitesHautePriorite(Long utilisateurId) {
        if (utilisateurId == null || utilisateurId <= 0) {
            return List.of();
        }
        return activiteDAO.getActivitesHautePriorite().stream()
                .filter(a -> a.getIdUtilisateur().equals(utilisateurId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Activite> obtenirActivitesRecentes(Long utilisateurId, int nombre) {
        if (utilisateurId == null || utilisateurId <= 0 || nombre <= 0) {
            return List.of();
        }
        return activiteDAO.getActivitesRecentes(nombre).stream()
                .filter(a -> a.getIdUtilisateur().equals(utilisateurId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean planningValide(List<Activite> activites, List<Contrainte> contraintes) {
        if (activites == null || contraintes == null) {
            return true; // Planning vide est considéré valide
        }

        // Vérifier les chevauchements entre activités
        for (int i = 0; i < activites.size(); i++) {
            for (int j = i + 1; j < activites.size(); j++) {
                if (activitesChevauchent(activites.get(i), activites.get(j))) {
                    return false;
                }
            }
        }

        // Vérifier les violations de contraintes
        for (Activite activite : activites) {
            for (Contrainte contrainte : contraintes) {
                if (estEnConflitAvecContrainte(
                        contrainte,
                        activite.getHoraireDebut().toLocalTime(),
                        activite.getHoraireFin().toLocalTime(),
                        activite.getHoraireDebut())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public double calculerScorePlanning(List<Activite> activites) {
        if (activites == null || activites.isEmpty()) {
            return 0.0;
        }

        double score = 0;

        // ========== 1. SCORE DE BASE : Priorités (max 200 points) ==========
        double scorePriorites = 0;
        int nombreActivites = activites.size();

        for (Activite a : activites) {
            if (a.getPriorite() != null) {
                scorePriorites += a.getPriorite() * 5;
            }
        }

        scorePriorites = Math.min(200, scorePriorites);
        score += scorePriorites;

        // ========== 2. RESPECT DES DEADLINES (max 100 points) ==========
        int avecDeadline = 0;
        int respectees = 0;

        for (Activite a : activites) {
            if (a.getDeadline() != null && a.getHoraireFin() != null) {
                avecDeadline++;
                if (!a.getHoraireFin().isAfter(a.getDeadline())) {
                    respectees++;
                }
            }
        }

        if (avecDeadline > 0) {
            double tauxRespect = (double) respectees / avecDeadline;
            score += tauxRespect * 100;
        } else {
            score += 50;
        }

        // ========== 3. ÉQUILIBRE TRAVAIL/REPOS (max 100 points) ==========
        double travail = 0, repos = 0;

        for (Activite a : activites) {
            if (a.getHoraireDebut() != null && a.getHoraireFin() != null) {
                long dureeMinutes = java.time.Duration.between(
                        a.getHoraireDebut(),
                        a.getHoraireFin()).toMinutes();

                if (a.getTypeActivite() != null) {
                    switch (a.getTypeActivite()) {
                        case Travail:
                        case Etude:
                        case Sport:
                        case Loisirs:
                            travail += dureeMinutes;
                            break;
                        case Repos:
                            repos += dureeMinutes;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        double heuresTravail = travail / 60.0;
        double heuresRepos = repos / 60.0;

        if (heuresRepos > 0) {
            double ratio = heuresTravail / heuresRepos;

            if (ratio >= 2.0 && ratio <= 3.0) {
                score += 100;
            } else if (ratio >= 1.5 && ratio <= 4.0) {
                score += 75;
            } else if (ratio >= 1.0 && ratio <= 6.0) {
                score += 50;
            } else {
                score += 20;
            }
        } else if (heuresTravail > 0) {
            score += 0;
        } else {
            score += 50;
        }

        // ========== 4. HORAIRES APPROPRIÉS (max 50 points) ==========
        int activitesBienPlacees = 0;

        for (Activite a : activites) {
            if (a.getHoraireDebut() != null) {
                int heure = a.getHoraireDebut().getHour();

                if (heure >= 7 && heure < 22) {
                    activitesBienPlacees++;
                }
            }
        }

        if (nombreActivites > 0) {
            double pourcentageBienPlacees = (double) activitesBienPlacees / nombreActivites;
            score += pourcentageBienPlacees * 50;
        }

        // ========== 5. COMPACITÉ (max 50 points) ==========
        if (nombreActivites >= 2) {
            List<Activite> triees = new ArrayList<>(activites);
            triees.sort((a1, a2) -> {
                if (a1.getHoraireDebut() == null)
                    return 1;
                if (a2.getHoraireDebut() == null)
                    return -1;
                return a1.getHoraireDebut().compareTo(a2.getHoraireDebut());
            });

            int espacesCompacts = 0;

            for (int i = 0; i < triees.size() - 1; i++) {
                Activite actuelle = triees.get(i);
                Activite suivante = triees.get(i + 1);

                if (actuelle.getHoraireFin() != null && suivante.getHoraireDebut() != null) {
                    long ecartMinutes = java.time.Duration.between(
                            actuelle.getHoraireFin(),
                            suivante.getHoraireDebut()).toMinutes();

                    if (ecartMinutes >= 0 && ecartMinutes <= 120) {
                        espacesCompacts++;
                    }
                }
            }

            double pourcentageCompact = (double) espacesCompacts / (nombreActivites - 1);
            score += pourcentageCompact * 50;
        }

        return Math.round(score * 100.0) / 100.0;
    }

    @Override
    public double calculerScoreAvecConflits(List<Activite> activites, List<Contrainte> contraintes) {
        double score = calculerScorePlanning(activites);

        if (activites == null || contraintes == null) {
            return score;
        }

        // ✅ CALCUL BASÉ SUR LES VRAIS CHEVAUCHEMENTS (pas les conflits en base)
        // Pénalité pour les chevauchements entre activités (-100 par chevauchement)
        for (int i = 0; i < activites.size(); i++) {
            for (int j = i + 1; j < activites.size(); j++) {
                if (activitesChevauchent(activites.get(i), activites.get(j))) {
                    score -= 100;
                }
            }
        }

        // Pénalité pour les violations de contraintes (-80 par violation)
        for (Activite activite : activites) {
            for (Contrainte contrainte : contraintes) {
                if (estEnConflitAvecContrainte(
                        contrainte,
                        activite.getHoraireDebut().toLocalTime(),
                        activite.getHoraireFin().toLocalTime(),
                        activite.getHoraireDebut())) {
                    score -= 80;
                }
            }
        }

        return score;
    }

    @Override
    public List<Activite> muterPlanning(List<Activite> activites) {
        if (activites == null || activites.isEmpty()) {
            return new ArrayList<>();
        }

        List<Activite> copie = new ArrayList<>();
        for (Activite a : activites) {
            Activite clone = new Activite();
            clone.setIdActivite(a.getIdActivite());
            clone.setTitre(a.getTitre());
            clone.setTypeActivite(a.getTypeActivite());
            clone.setPriorite(a.getPriorite());
            clone.setHoraireDebut(a.getHoraireDebut());
            clone.setHoraireFin(a.getHoraireFin());
            clone.setDeadline(a.getDeadline());
            clone.setDescription(a.getDescription());
            clone.setIdUtilisateur(a.getIdUtilisateur());
            copie.add(clone);
        }

        java.util.Random random = new java.util.Random();
        Activite activiteAMuter = copie.get(random.nextInt(copie.size()));

        int[] decalagesPossibles = { -240, -120, -60, -30, 30, 60, 120, 240 };
        int decalage = decalagesPossibles[random.nextInt(decalagesPossibles.length)];

        if (activiteAMuter.getHoraireDebut() != null && activiteAMuter.getHoraireFin() != null) {
            long dureeMinutes = java.time.Duration.between(
                    activiteAMuter.getHoraireDebut(),
                    activiteAMuter.getHoraireFin()).toMinutes();

            LocalDateTime nouveauDebut = activiteAMuter.getHoraireDebut().plusMinutes(decalage);
            LocalDateTime nouvelleFin = nouveauDebut.plusMinutes(dureeMinutes);

            if (nouveauDebut.getHour() >= 6 && nouvelleFin.getHour() <= 23) {
                activiteAMuter.setHoraireDebut(nouveauDebut);
                activiteAMuter.setHoraireFin(nouvelleFin);
            }
        }

        return copie;
    }

    @Override
    public List<Activite> optimiserPlanning(List<Activite> activites, List<Contrainte> contraintes, int iterations) {
        if (activites == null || activites.isEmpty() || contraintes == null || iterations <= 0) {
            System.out.println("⚠️ Paramètres invalides pour l'optimisation");
            return activites;
        }

        System.out.println("🚀 Début optimisation : " + activites.size() + " activités, " +
                contraintes.size() + " contraintes, " + iterations + " itérations");

        List<Activite> meilleur = copierPlanning(activites);
        double meilleurScore = calculerScoreAvecConflits(meilleur, contraintes);

        List<Activite> courant = copierPlanning(activites);
        double scoreCourant = meilleurScore;

        System.out.println("📊 Score initial: " + meilleurScore);
        System.out.println("✅ Planning initial valide: " + planningValide(meilleur, contraintes));

        double temperature = 1000.0;
        double tauxRefroidissement = 0.995;

        int ameliorations = 0;
        int acceptations = 0;
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < iterations; i++) {
            List<Activite> voisin = muterPlanning(courant);
            double scoreVoisin = calculerScoreAvecConflits(voisin, contraintes);

            double delta = scoreVoisin - scoreCourant;

            boolean accepter = false;

            if (delta > 0) {
                accepter = true;
            } else {
                double probabilite = Math.exp(delta / temperature);
                if (random.nextDouble() < probabilite) {
                    accepter = true;
                }
            }

            if (accepter) {
                courant = voisin;
                scoreCourant = scoreVoisin;
                acceptations++;

                if (scoreVoisin > meilleurScore) {
                    meilleur = voisin;
                    meilleurScore = scoreVoisin;
                    ameliorations++;

                    if (ameliorations % 10 == 0) {
                        System.out.println("✨ Amélioration #" + ameliorations +
                                " - Score: " + String.format("%.2f", meilleurScore) +
                                " - Valide: " + planningValide(meilleur, contraintes));
                    }
                }
            }

            temperature *= tauxRefroidissement;

            if ((i + 1) % 100 == 0) {
                System.out.println("🔄 Itération " + (i + 1) + "/" + iterations +
                        " - T: " + String.format("%.2f", temperature) +
                        " - Score: " + String.format("%.2f", scoreCourant) +
                        " - Acceptations: " + acceptations);
            }
        }

        System.out.println("✅ Optimisation terminée!");
        System.out.println("📊 Score final: " + meilleurScore + " (initial: " +
                calculerScoreAvecConflits(activites, contraintes) + ")");
        System.out.println("🎯 Améliorations: " + ameliorations);
        System.out.println("✓ Planning final valide: " + planningValide(meilleur, contraintes));

        int conflitsChevauchement = compterChevauchements(meilleur);
        int conflitsContraintes = compterViolationsContraintes(meilleur, contraintes);
        System.out.println("⚠️ Chevauchements restants: " + conflitsChevauchement);
        System.out.println("⚠️ Violations de contraintes restantes: " + conflitsContraintes);

        return meilleur;
    }

    // ========== MÉTHODES PRIVÉES ==========

    private void creerConflitsSiNecessaire(Activite activite, List<Activite> activitesChevauchantes) {
        if (activite == null) {
            return;
        }

        // 1. Vérifier les violations de contraintes
        List<Contrainte> contraintesUtilisateur = contrainteDAO.getContraintesActivesByUtilisateur(
                activite.getIdUtilisateur().intValue());

        LocalTime heureDebut = activite.getHoraireDebut().toLocalTime();
        LocalTime heureFin = activite.getHoraireFin().toLocalTime();

        for (Contrainte contrainte : contraintesUtilisateur) {
            if (estEnConflitAvecContrainte(contrainte, heureDebut, heureFin, activite.getHoraireDebut())) {
                Conflit conflit = new Conflit(
                        null,
                        LocalDateTime.now(),
                        TypeConflit.VIOLATION_DE_CONTRAINTE,
                        false);
                Long idConflit = conflitDAO.ajouter(conflit);
                if (idConflit != null && idConflit > 0) {
                    conflitDAO.lierActiviteAuConflit(idConflit, activite.getIdActivite());
                    System.out.println("⚠️ Conflit créé: Violation de contrainte '" + contrainte.getTitre() + "'");
                }
            }
        }

        // 2. Créer des conflits pour les chevauchements d'activités
        if (activitesChevauchantes != null && !activitesChevauchantes.isEmpty()) {
            for (Activite autre : activitesChevauchantes) {
                if (!autre.getIdActivite().equals(activite.getIdActivite())) {
                    if (!conflitDejaExistePourActivites(activite.getIdActivite(), autre.getIdActivite())) {
                        Conflit conflit = new Conflit(
                                null,
                                LocalDateTime.now(),
                                TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES,
                                false);
                        Long idConflit = conflitDAO.ajouter(conflit);
                        if (idConflit != null && idConflit > 0) {
                            conflitDAO.lierActiviteAuConflit(idConflit, activite.getIdActivite());
                            conflitDAO.lierActiviteAuConflit(idConflit, autre.getIdActivite());
                            System.out.println("⚠️ Conflit créé: Chevauchement entre '" + activite.getTitre() +
                                    "' et '" + autre.getTitre() + "'");
                        }
                    }
                }
            }
        }
    }

    private boolean activitesChevauchent(Activite a1, Activite a2) {
        if (a1 == null || a2 == null) {
            return false;
        }

        if (a1.getHoraireDebut() == null || a1.getHoraireFin() == null ||
                a2.getHoraireDebut() == null || a2.getHoraireFin() == null) {
            return false;
        }

        return a1.getHoraireDebut().isBefore(a2.getHoraireFin()) &&
                a1.getHoraireFin().isAfter(a2.getHoraireDebut());
    }

    private boolean estEnConflitAvecContrainte(Contrainte contrainte, LocalTime heureDebut,
            LocalTime heureFin, LocalDateTime dateActivite) {
        if (contrainte == null || heureDebut == null || heureFin == null || dateActivite == null) {
            return false;
        }

        LocalTime debContrainte = contrainte.getDateHeureDeb();
        LocalTime finContrainte = contrainte.getDateHeureFin();

        if (debContrainte == null || finContrainte == null) {
            return false;
        }

        LocalDate dateAct = dateActivite.toLocalDate();
        java.time.DayOfWeek jour = dateActivite.getDayOfWeek();

        if (contrainte.isRepetitif()) {
            if (contrainte.getJoursSemaine() == null || !contrainte.getJoursSemaine().contains(jour)) {
                return false;
            }
        } else {
            if (contrainte.getDatesSpecifiques() == null || !contrainte.getDatesSpecifiques().contains(dateAct)) {
                return false;
            }
        }

        return heureDebut.isBefore(finContrainte) && heureFin.isAfter(debContrainte);
    }

    private boolean conflitDejaExistePourActivites(Long idActivite1, Long idActivite2) {
        List<Conflit> conflits1 = conflitDAO.getByActivite(idActivite1);
        for (Conflit conflit : conflits1) {
            List<Long> activitesLiees = conflitDAO.getActivitesLieesAuConflit(conflit.getidConflit());
            if (activitesLiees.contains(idActivite2) && !conflit.isResolu()) {
                return true;
            }
        }
        return false;
    }

    private boolean validerActivite(Activite activite) {
        if (activite == null) {
            System.err.println("Erreur : L'activité ne peut pas être null");
            return false;
        }

        if (activite.getTitre() == null || activite.getTitre().trim().isEmpty()) {
            System.err.println("Erreur : Le titre de l'activité ne peut pas être vide");
            return false;
        }

        if (activite.getTypeActivite() == null) {
            System.err.println("Erreur : Le type d'activité doit être spécifié");
            return false;
        }

        if (!validerHoraires(activite.getHoraireDebut(), activite.getHoraireFin())) {
            return false;
        }

        if (activite.getPriorite() != null && !validerPriorite(activite.getPriorite())) {
            return false;
        }

        if (activite.getDeadline() == null) {
            System.err.println("Erreur : La deadline ne peut pas être null");
            return false;
        }

        if (activite.getIdUtilisateur() == null || activite.getIdUtilisateur() <= 0) {
            System.err.println("Erreur : L'ID utilisateur doit être valide");
            return false;
        }

        return true;
    }

    private int compterChevauchements(List<Activite> activites) {
        int count = 0;
        for (int i = 0; i < activites.size(); i++) {
            for (int j = i + 1; j < activites.size(); j++) {
                if (activitesChevauchent(activites.get(i), activites.get(j))) {
                    count++;
                }
            }
        }
        return count;
    }

    private int compterViolationsContraintes(List<Activite> activites, List<Contrainte> contraintes) {
        if (contraintes == null)
            return 0;

        int count = 0;
        for (Activite activite : activites) {
            if (activite.getHoraireDebut() == null || activite.getHoraireFin() == null) {
                continue;
            }
            for (Contrainte contrainte : contraintes) {
                if (estEnConflitAvecContrainte(
                        contrainte,
                        activite.getHoraireDebut().toLocalTime(),
                        activite.getHoraireFin().toLocalTime(),
                        activite.getHoraireDebut())) {
                    count++;
                }
            }
        }
        return count;
    }

    private List<Activite> copierPlanning(List<Activite> activites) {
        List<Activite> copie = new ArrayList<>();
        for (Activite a : activites) {
            Activite clone = new Activite();
            clone.setIdActivite(a.getIdActivite());
            clone.setTitre(a.getTitre());
            clone.setTypeActivite(a.getTypeActivite());
            clone.setPriorite(a.getPriorite());
            clone.setHoraireDebut(a.getHoraireDebut());
            clone.setHoraireFin(a.getHoraireFin());
            clone.setDeadline(a.getDeadline());
            clone.setDescription(a.getDescription());
            clone.setIdUtilisateur(a.getIdUtilisateur());
            copie.add(clone);
        }
        return copie;
    }
}
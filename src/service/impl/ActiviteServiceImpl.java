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
        double travail = 0, repos = 0, sport = 0;

        for (Activite a : activites) {
            // Bonus basé sur la priorité
            if (a.getPriorite() != null) {
                score += a.getPriorite() * 10;
            }

            // Pénalité si la deadline n'est pas respectée
            if (a.getDeadline() != null && a.getHoraireFin() != null && 
                a.getHoraireFin().isAfter(a.getDeadline())) {
                score -= 30;
            }

            // Calculer la durée en minutes
            if (a.getHoraireDebut() != null && a.getHoraireFin() != null) {
                long dureeMinutes = java.time.Duration.between(
                    a.getHoraireDebut(), 
                    a.getHoraireFin()
                ).toMinutes();

                // Accumuler les durées par type d'activité
                if (a.getTypeActivite() != null) {
                    switch (a.getTypeActivite()) {
                        case Travail:
                            travail += dureeMinutes;
                            break;
                        case Repos:
                            repos += dureeMinutes;
                            break;
                        case Sport:
                            sport += dureeMinutes;
                            break;
                        default:
                            break;
                    }
                }

                // Pénalité pour les activités en dehors des heures normales (22h-7h)
                if (a.getHoraireDebut() != null) {
                    int heure = a.getHoraireDebut().getHour();
                    if (heure >= 22 || heure < 7) {
                        score -= 15;
                    }
                }
            }
        }

        // Calculer le score de fatigue (plus de travail et sport = plus de fatigue)
        double fatigue = travail - repos + 0.5 * sport;
        score -= fatigue / 60;

        return score;
    }

    /**
     * Calculer le score d'un planning en incluant des pénalités pour les conflits.
     * Cela permet à l'algorithme d'optimisation d'explorer les plannings invalides
     * et de converger progressivement vers des solutions valides.
     * 
     * @param activites La liste des activités du planning
     * @param contraintes La liste des contraintes à respecter
     * @return Le score du planning (avec pénalités pour conflits)
     */
    private double calculerScoreAvecConflits(List<Activite> activites, List<Contrainte> contraintes) {
        // Commencer avec le score de base
        double score = calculerScorePlanning(activites);
        
        if (activites == null || contraintes == null) {
            return score;
        }
        
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

        // Créer une copie profonde de la liste
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

        // Sélectionner une activité aléatoire
        java.util.Random random = new java.util.Random();
        Activite activiteAMuter = copie.get(random.nextInt(copie.size()));

        // Décalage aléatoire : -120, -60, -30, +30, +60 ou +120 minutes
        int[] decalagesPossibles = {-120, -60, -30, 30, 60, 120};
        int decalage = decalagesPossibles[random.nextInt(decalagesPossibles.length)];

        // Appliquer la mutation
        if (activiteAMuter.getHoraireDebut() != null) {
            activiteAMuter.setHoraireDebut(
                activiteAMuter.getHoraireDebut().plusMinutes(decalage)
            );
        }
        
        if (activiteAMuter.getHoraireFin() != null) {
            activiteAMuter.setHoraireFin(
                activiteAMuter.getHoraireFin().plusMinutes(decalage)
            );
        }

        return copie;
    }

    @Override
    public List<Activite> optimiserPlanning(List<Activite> activites, List<Contrainte> contraintes, int iterations) {
        if (activites == null || contraintes == null || iterations <= 0) {
            return activites;
        }

        // Planning initial avec pénalités pour conflits
        List<Activite> meilleur = activites;
        double meilleurScore = calculerScoreAvecConflits(meilleur, contraintes);
        
        int ameliorations = 0;
        int tentativesReussies = 0;

        // Itérations d'optimisation
        for (int i = 0; i < iterations; i++) {
            // Générer une variante par mutation
            List<Activite> candidat = muterPlanning(meilleur);

            // Calculer le score avec pénalités de conflits
            double scoreCandidat = calculerScoreAvecConflits(candidat, contraintes);
            
            // Conserver si meilleur (même invalide si score supérieur)
            if (scoreCandidat > meilleurScore) {
                meilleur = candidat;
                meilleurScore = scoreCandidat;
                ameliorations++;
                
                // Vérifier si on a atteint un planning valide
                if (planningValide(candidat, contraintes)) {
                    tentativesReussies++;
                }
            }
        }
        
        System.out.println("Debug: " + ameliorations + " améliorations trouvées sur " + iterations + " itérations");
        if (tentativesReussies > 0) {
            System.out.println("Debug: " + tentativesReussies + " plannings valides trouvés");
        }

        return meilleur;
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Créer automatiquement des conflits pour les violations de contraintes et chevauchements
     * @param activite L'activité qui viole les contraintes
     * @param activitesChevauchantes Liste des activités qui se chevauchent
     */
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
                // Créer un conflit de violation de contrainte
                Conflit conflit = new Conflit(
                    null,
                    LocalDateTime.now(),
                    TypeConflit.VIOLATION_DE_CONTRAINTE,
                    false
                );
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
                    // Vérifier si un conflit n'existe pas déjà entre ces deux activités
                    if (!conflitDejaExistePourActivites(activite.getIdActivite(), autre.getIdActivite())) {
                        Conflit conflit = new Conflit(
                            null,
                            LocalDateTime.now(),
                            TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES,
                            false
                        );
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

    /**
     * Vérifier si deux activités se chevauchent temporellement
     */
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

    /**
     * Vérifier si une activité viole une contrainte
     * Logique inspirée de la méthode violeContrainte
     */
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

        // Extraire la date et le jour de la semaine de l'activité
        LocalDate dateAct = dateActivite.toLocalDate();
        java.time.DayOfWeek jour = dateActivite.getDayOfWeek();

        // Si la contrainte est répétitive, vérifier que le jour correspond
        if (contrainte.isRepetitif()) {
            if (contrainte.getJoursSemaine() == null || !contrainte.getJoursSemaine().contains(jour)) {
                return false; // Le jour ne correspond pas, pas de violation
            }
        } else {
            // Si la contrainte n'est pas répétitive, vérifier que la date correspond
            if (contrainte.getDatesSpecifiques() == null || !contrainte.getDatesSpecifiques().contains(dateAct)) {
                return false; // La date ne correspond pas, pas de violation
            }
        }

        // Vérifier le chevauchement temporel (l'activité viole la contrainte si les horaires se chevauchent)
        return heureDebut.isBefore(finContrainte) && heureFin.isAfter(debContrainte);
    }

    /**
     * Vérifier si un conflit existe déjà entre deux activités
     */
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
}
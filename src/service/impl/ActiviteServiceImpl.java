package service.impl;

import dao.interfaces.ActiviteDAO;
import entities.Activite;
import entities.TypeActivite;
import dao.impl.ActiviteDAOImpl;
import service.ActiviteService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du Service métier pour les activités
 * Applique la logique métier et valide les opérations avant d'accéder à la DAO
 */
public class ActiviteServiceImpl implements ActiviteService {
    
    private final ActiviteDAO activiteDAO;
    
    public ActiviteServiceImpl() {
        this.activiteDAO = new ActiviteDAOImpl();
    }
    
    public ActiviteServiceImpl(ActiviteDAO activiteDAO) {
        this.activiteDAO = activiteDAO;
    }
    
    // ========== OPÉRATIONS CRUD AVEC VALIDATION ==========
    
    @Override
    public Long creerActivite(Activite activite) {
        // Validation des données
        if (!validerActivite(activite)) {
            return -1L;
        }
        
        // Vérification du chevauchement
        if (verifierChevauchement(activite.getIdUtilisateur(), 
                                 activite.getHoraireDebut(), 
                                 activite.getHoraireFin())) {
            System.err.println("Erreur : Chevauchement détecté avec une autre activité");
            return -1L;
        }
        
        return activiteDAO.ajouter(activite);
    }
    
    @Override
    public boolean mettreAJourActivite(Activite activite) {
        // Validation
        if (!validerActivite(activite)) {
            return false;
        }
        
        // Vérification du chevauchement (en excluant l'activité courante)
        List<Activite> chevauchantes = activiteDAO.getActivitesChevauchantesUtilisateur(
            activite.getIdUtilisateur(),
            activite.getHoraireDebut(),
            activite.getHoraireFin()
        );
        
        // S'il existe des chevauchements autres que l'activité elle-même
        if (chevauchantes.stream().anyMatch(a -> !a.getIdActivite().equals(activite.getIdActivite()))) {
            System.err.println("Erreur : Chevauchement détecté avec une autre activité");
            return false;
        }
        
        return activiteDAO.modifier(activite);
    }
    
    @Override
    public boolean supprimerActivite(Long idActivite) {
        if (idActivite == null || idActivite <= 0) {
            System.err.println("Erreur : ID d'activité invalide");
            return false;
        }
        
        return activiteDAO.supprimer(idActivite);
    }
    
    @Override
    public Optional<Activite> obtenirActivite(Long idActivite) {
        if (idActivite == null || idActivite <= 0) {
            return Optional.empty();
        }
        
        return activiteDAO.getById(idActivite);
    }
    
    @Override
    public List<Activite> obtenirToutesLesActivites() {
        return activiteDAO.getAll();
    }
    
    // ========== RECHERCHE ET FILTRAGE ==========
    
    @Override
    public List<Activite> obtenirActivitesUtilisateur(Long idUtilisateur) {
        if (idUtilisateur == null || idUtilisateur <= 0) {
            return List.of();
        }
        
        return activiteDAO.getByUtilisateur(idUtilisateur);
    }
    
    @Override
    public List<Activite> obtenirActivitesParType(TypeActivite type) {
        if (type == null) {
            return List.of();
        }
        
        return activiteDAO.getByType(type);
    }
    
    @Override
    public List<Activite> obtenirActivitesDansLaPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (dateDebut == null || dateFin == null || dateDebut.isAfter(dateFin)) {
            return List.of();
        }
        
        return activiteDAO.getByPeriode(dateDebut, dateFin);
    }
    
    @Override
    public List<Activite> obtenirActivitesUtilisateurDansLaPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin) {
        if (idUtilisateur == null || idUtilisateur <= 0 || dateDebut == null || dateFin == null || dateDebut.isAfter(dateFin)) {
            return List.of();
        }
        
        return activiteDAO.getByUtilisateurAndPeriode(idUtilisateur, dateDebut, dateFin);
    }
    
    @Override
    public List<Activite> rechercherActivites(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            return List.of();
        }
        
        return activiteDAO.rechercherParMotCle(motCle.trim());
    }
    
    @Override
    public List<Activite> rechercherActivitesUtilisateur(Long idUtilisateur, String motCle) {
        if (idUtilisateur == null || idUtilisateur <= 0 || motCle == null || motCle.trim().isEmpty()) {
            return List.of();
        }
        
        return activiteDAO.rechercherParMotCleUtilisateur(idUtilisateur, motCle.trim());
    }
    
    // ========== OPÉRATIONS MÉTIER ==========
    
    @Override
    public boolean verifierChevauchement(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        if (idUtilisateur == null || idUtilisateur <= 0 || horaireDebut == null || horaireFin == null) {
            return false;
        }
        
        if (!validerHoraires(horaireDebut, horaireFin)) {
            return false;
        }
        
        List<Activite> chevauchantes = activiteDAO.getActivitesChevauchantesUtilisateur(
            idUtilisateur,
            horaireDebut,
            horaireFin
        );
        
        return !chevauchantes.isEmpty();
    }
    
    @Override
    public List<Activite> obtenirActivitesChevauchantes(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        if (idUtilisateur == null || idUtilisateur <= 0 || horaireDebut == null || horaireFin == null) {
            return List.of();
        }
        
        if (!validerHoraires(horaireDebut, horaireFin)) {
            return List.of();
        }
        
        return activiteDAO.getActivitesChevauchantesUtilisateur(idUtilisateur, horaireDebut, horaireFin);
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
    
    // ========== STATISTIQUES ==========
    
    @Override
    public int obtenirNombreTotalActivites() {
        return activiteDAO.compterToutesLesActivites();
    }
    
    @Override
    public int obtenirNombreActivitesUtilisateur(Long idUtilisateur) {
        if (idUtilisateur == null || idUtilisateur <= 0) {
            return 0;
        }
        
        return activiteDAO.compterActivitesUtilisateur(idUtilisateur);
    }
    
    @Override
    public double obtenirTauxCompletion() {
        // Méthode non applicable sans champ completee
        return 0.0;
    }
    
    @Override
    public double obtenirTauxCompletionUtilisateur(Long idUtilisateur) {
        // Méthode non applicable sans champ completee
        return 0.0;
    }
    
    @Override
    public List<Activite> obtenirActivitesDeadlineProche(int joursAvance) {
        if (joursAvance < 0) {
            return List.of();
        }
        
        return activiteDAO.getActivitesAvecDeadlineProche(joursAvance);
    }
    
    @Override
    public List<Activite> obtenirActivitesHautePriorite() {
        return activiteDAO.getActivitesHautePriorite();
    }
    
    @Override
    public List<Activite> obtenirActivitesRecentes(int nombre) {
        if (nombre <= 0) {
            return List.of();
        }
        
        return activiteDAO.getActivitesRecentes(nombre);
    }
    
    // ========== MÉTHODES PRIVÉES DE VALIDATION ==========
    
    /**
     * Valider une activité complètement
     */
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

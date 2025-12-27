package service.impl;

import dao.impl.ContrainteDAOImpl;
import dao.interfaces.ContrainteDAO;
import entities.Contrainte;
import entities.StatutContrainte;
import entities.TypeContrainte;
import service.ContrainteService;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContrainteServiceImpl implements ContrainteService {

    private ContrainteDAO contrainteDAO;

    public ContrainteServiceImpl() {
        this.contrainteDAO = new ContrainteDAOImpl();
    }

    public ContrainteServiceImpl(ContrainteDAO contrainteDAO) {
        this.contrainteDAO = contrainteDAO;
    }

    @Override
    public Contrainte getById(int id) {
        if (id <= 0) {
            return null;
        }
        return contrainteDAO.getById(id).orElse(null);
    }

    @Override
    public List<Contrainte> getAll() {
        return contrainteDAO.getAll();
    }

    @Override
    public List<Contrainte> getByUtilisateur(int utilisateurId) {
        if (utilisateurId <= 0) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId);
    }

    @Override
    public List<Contrainte> getContraintesActives(int utilisateurId) {
        if (utilisateurId <= 0) {
            return List.of();
        }
        return contrainteDAO.getContraintesActivesByUtilisateur(utilisateurId);
    }

    @Override
    public boolean ajouter(Contrainte contrainte) {
        if (contrainte == null) {
            System.err.println("Erreur: contrainte null");
            return false;
        }

        if (contrainte.getTitre() == null || contrainte.getTitre().trim().isEmpty()) {
            System.err.println("Erreur: titre requis");
            return false;
        }

        if (contrainte.getType() == null) {
            System.err.println("Erreur: type requis");
            return false;
        }

        if (contrainte.getDateHeureDeb() == null || contrainte.getDateHeureFin() == null) {
            System.err.println("Erreur: horaires requis");
            return false;
        }

        if (contrainte.getDateHeureDeb().isAfter(contrainte.getDateHeureFin())) {
            System.err.println("Erreur: heure début > heure fin");
            return false;
        }

        if (!contrainte.isRepetitif() && contrainte.getJoursSemaine() != null
                && !contrainte.getJoursSemaine().isEmpty()) {
            System.err.println("Erreur: jours semaine non vide pour non répétitif");
            return false;
        }

        if (contrainte.isRepetitif()
                && (contrainte.getJoursSemaine() == null || contrainte.getJoursSemaine().isEmpty())) {
            System.err.println("Erreur: jours semaine requis pour répétitif");
            return false;
        }

        if (contrainte.getUtilisateurId() <= 0) {
            System.err.println("Erreur: ID utilisateur invalide");
            return false;
        }

        if (contrainte.getStatut() == null) {
            contrainte.setStatut(StatutContrainte.ACTIVE);
        }

        // ⚠️ CORRECTION IMPORTANTE : Récupérer l'ID généré par la base
        int idGenere = contrainteDAO.ajouter(contrainte);
        if (idGenere > 0) {
            contrainte.setId(idGenere);  // ⚠️ METTRE À JOUR L'ID DANS L'OBJET
            return true;
        }
        return false;
    }

    @Override
    public boolean modifier(Contrainte contrainte) {
        if (contrainte == null || contrainte.getId() <= 0) {
            System.err.println("Erreur: contrainte sans ID");
            return false;
        }

        if (contrainte.getDateHeureDeb() != null && contrainte.getDateHeureFin() != null) {
            if (contrainte.getDateHeureDeb().isAfter(contrainte.getDateHeureFin())) {
                System.err.println("Erreur: heure début > heure fin");
                return false;
            }
        }

        if (!contrainte.isRepetitif() && contrainte.getJoursSemaine() != null
                && !contrainte.getJoursSemaine().isEmpty()) {
            System.err.println("Erreur: jours semaine non vide pour non répétitif");
            return false;
        }

        if (contrainte.isRepetitif()
                && (contrainte.getJoursSemaine() == null || contrainte.getJoursSemaine().isEmpty())) {
            System.err.println("Erreur: jours semaine requis pour répétitif");
            return false;
        }

        return contrainteDAO.modifier(contrainte);
    }

    @Override
    public boolean supprimer(int id) {
        if (id <= 0) {
            System.err.println("Erreur: ID invalide");
            return false;
        }
        return contrainteDAO.supprimer(id);
    }

    @Override
    public boolean toggleStatut(int id) {
        Contrainte contrainte = getById(id);
        if (contrainte == null) {
            System.err.println("Contrainte avec ID " + id + " non trouvée");
            return false;
        }
        
        if (contrainte.getStatut() == StatutContrainte.ACTIVE) {
            contrainte.setStatut(StatutContrainte.DESACTIVE);
        } else {
            contrainte.setStatut(StatutContrainte.ACTIVE);
        }
        
        return modifier(contrainte);
    }

    
    // Méthodes supplémentaires avec userId
    @Override
    public List<Contrainte> getContraintesDesactives(int utilisateurId) {
        if (utilisateurId <= 0) {
            return List.of();
        }
        return getContraintesByStatut(utilisateurId, StatutContrainte.DESACTIVE);
    }

    @Override
    public List<Contrainte> getContraintesByStatut(int utilisateurId, StatutContrainte statut) {
        if (utilisateurId <= 0 || statut == null) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId).stream()
                .filter(c -> c.getStatut() == statut)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contrainte> getContraintesByPeriode(int utilisateurId, LocalTime heureDebut, LocalTime heureFin) {
        if (utilisateurId <= 0 || heureDebut == null || heureFin == null || heureDebut.isAfter(heureFin)) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId).stream()
                .filter(c -> c.getDateHeureDeb() != null && c.getDateHeureFin() != null)
                .filter(c -> !(c.getDateHeureFin().compareTo(heureDebut) <= 0 ||
                        c.getDateHeureDeb().compareTo(heureFin) >= 0))
                .collect(Collectors.toList());
    }

    @Override
    public List<Contrainte> getContraintesRepetitives(int utilisateurId) {
        if (utilisateurId <= 0) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId).stream()
                .filter(Contrainte::isRepetitif)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contrainte> getContraintesNonRepetitives(int utilisateurId) {
        if (utilisateurId <= 0) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId).stream()
                .filter(c -> !c.isRepetitif())
                .collect(Collectors.toList());
    }

    @Override
    public List<Contrainte> getContraintesByType(int utilisateurId, TypeContrainte type) {
        if (utilisateurId <= 0 || type == null) {
            return List.of();
        }
        return contrainteDAO.getAllByUtilisateur(utilisateurId).stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public boolean activerContrainte(int idContrainte, int utilisateurId) {
        return changerStatutContrainte(idContrainte, utilisateurId, StatutContrainte.ACTIVE);
    }

    @Override
    public boolean desactiverContrainte(int idContrainte, int utilisateurId) {
        return changerStatutContrainte(idContrainte, utilisateurId, StatutContrainte.DESACTIVE);
    }

    @Override
    public boolean changerStatutContrainte(int idContrainte, int utilisateurId, StatutContrainte newStatut) {
        if (newStatut == null || idContrainte <= 0 || utilisateurId <= 0) {
            return false;
        }

        Optional<Contrainte> contrainteOpt = contrainteDAO.getByIdAndUtilisateur(idContrainte, utilisateurId);
        if (contrainteOpt.isEmpty()) {
            return false;
        }

        Contrainte contrainte = contrainteOpt.get();
        contrainte.setStatut(newStatut);
        return contrainteDAO.modifier(contrainte);
    }

    @Override
    public int compterContraintesByStatut(int utilisateurId, StatutContrainte statut) {
        if (utilisateurId <= 0 || statut == null) {
            return 0;
        }
        return getContraintesByStatut(utilisateurId, statut).size();
    }

    @Override
    public int compterContraintesActives(int utilisateurId) {
        return compterContraintesByStatut(utilisateurId, StatutContrainte.ACTIVE);
    }

    @Override
    public int compterContraintesDesactives(int utilisateurId) {
        return compterContraintesByStatut(utilisateurId, StatutContrainte.DESACTIVE);
    }

    @Override
    public boolean estEnConflit(Contrainte contrainte, LocalTime heureDebut, LocalTime heureFin) {
        if (contrainte == null || heureDebut == null || heureFin == null) {
            return false;
        }

        if (heureDebut.isAfter(heureFin)) {
            return false;
        }

        LocalTime debContrainte = contrainte.getDateHeureDeb();
        LocalTime finContrainte = contrainte.getDateHeureFin();

        if (debContrainte == null || finContrainte == null) {
            return false;
        }

        return !(finContrainte.compareTo(heureDebut) <= 0 || debContrainte.compareTo(heureFin) >= 0);
    }
}
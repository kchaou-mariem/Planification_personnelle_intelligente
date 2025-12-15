package service.impl;

import dao.impl.ContrainteDAOImpl;
import dao.interfaces.ContrainteDAO;
import Entities.Contrainte;
import Entities.StatutContrainte;
import Entities.TypeContrainte;
import service.ContrainteService;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service ContrainteService.
 * Gère la logique métier liée aux contraintes.
 */
public class ContrainteServiceImpl implements ContrainteService {
    
    private ContrainteDAO contrainteDAO;
    
    public ContrainteServiceImpl() {
        this.contrainteDAO = new ContrainteDAOImpl();
    }
    
    public ContrainteServiceImpl(ContrainteDAO contrainteDAO) {
        this.contrainteDAO = contrainteDAO;
    }
    
    @Override
    public Long ajouterContrainte(Contrainte contrainte) {
        if (contrainte == null) {
            System.err.println("Erreur: impossible d'ajouter une contrainte null");
            return -1L;
        }
        
        if (contrainte.getTitre() == null || contrainte.getTitre().trim().isEmpty()) {
            System.err.println("Erreur: le titre de la contrainte est requis");
            return -1L;
        }
        
        if (contrainte.getType() == null) {
            System.err.println("Erreur: le type de contrainte est requis");
            return -1L;
        }
        
        if (contrainte.getDateHeureDeb() == null || contrainte.getDateHeureFin() == null) {
            System.err.println("Erreur: les horaires de début et fin sont requis");
            return -1L;
        }
        
        if (contrainte.getDateHeureDeb().isAfter(contrainte.getDateHeureFin())) {
            System.err.println("Erreur: l'heure de début doit être antérieure à l'heure de fin");
            return -1L;
        }
        
        // Par défaut, une nouvelle contrainte est ACTIVE
        if (contrainte.getStatut() == null) {
            contrainte.setStatut(StatutContrainte.ACTIVE);
        }
        
        return contrainteDAO.ajouter(contrainte);
    }
    
    @Override
    public boolean modifierContrainte(Contrainte contrainte) {
        if (contrainte == null || contrainte.getId() == null) {
            System.err.println("Erreur: impossible de modifier une contrainte sans ID");
            return false;
        }
        
        if (contrainte.getDateHeureDeb() != null && contrainte.getDateHeureFin() != null) {
            if (contrainte.getDateHeureDeb().isAfter(contrainte.getDateHeureFin())) {
                System.err.println("Erreur: l'heure de début doit être antérieure à l'heure de fin");
                return false;
            }
        }
        
        return contrainteDAO.modifier(contrainte);
    }
    
    @Override
    public boolean supprimerContrainte(Long idContrainte) {
        if (idContrainte == null || idContrainte <= 0) {
            System.err.println("Erreur: ID invalide");
            return false;
        }
        
        return contrainteDAO.supprimer(idContrainte);
    }
    
    @Override
    public Optional<Contrainte> getContrainteById(Long idContrainte) {
        if (idContrainte == null || idContrainte <= 0) {
            return Optional.empty();
        }
        
        return contrainteDAO.getById(idContrainte);
    }
    
    @Override
    public List<Contrainte> getToutesLesContraintes() {
        return contrainteDAO.getAll();
    }
    
    @Override
    public List<Contrainte> getContraintesActives() {
        return contrainteDAO.getContraintesActives();
    }
    
    @Override
    public List<Contrainte> getContraintesDesactives() {
        return contrainteDAO.getContraintesDesactives();
    }
    
    @Override
    public List<Contrainte> getContraintesByStatut(StatutContrainte statut) {
        if (statut == null) {
            return List.of();
        }
        
        return contrainteDAO.getContraintesByStatut(statut);
    }
    
    @Override
    public List<Contrainte> getContraintesByPeriode(LocalTime heureDebut, LocalTime heureFin) {
        if (heureDebut == null || heureFin == null || heureDebut.isAfter(heureFin)) {
            System.err.println("Erreur: plage horaire invalide");
            return List.of();
        }
        
        return contrainteDAO.getByPeriode(heureDebut, heureFin);
    }
    
    @Override
    public List<Contrainte> getContraintesRepetitives() {
        return contrainteDAO.getRepetitives();
    }
    
    @Override
    public List<Contrainte> getContraintesNonRepetitives() {
        return contrainteDAO.getNonRepetitives();
    }
    
    @Override
    public List<Contrainte> getContraintesByType(TypeContrainte type) {
        if (type == null) {
            return List.of();
        }
        
        return contrainteDAO.getAll().stream()
            .filter(c -> c.getType() == type)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean activerContrainte(Long idContrainte) {
        Optional<Contrainte> contrainteOpt = getContrainteById(idContrainte);
        
        if (contrainteOpt.isEmpty()) {
            System.err.println("Contrainte introuvable avec l'ID: " + idContrainte);
            return false;
        }
        
        Contrainte contrainte = contrainteOpt.get();
        if (contrainte.getStatut() == StatutContrainte.ACTIVE) {
            System.out.println("Info: la contrainte est déjà active");
            return true;
        }
        
        contrainte.setStatut(StatutContrainte.ACTIVE);
        return modifierContrainte(contrainte);
    }
    
    @Override
    public boolean desactiverContrainte(Long idContrainte) {
        Optional<Contrainte> contrainteOpt = getContrainteById(idContrainte);
        
        if (contrainteOpt.isEmpty()) {
            System.err.println("Contrainte introuvable avec l'ID: " + idContrainte);
            return false;
        }
        
        Contrainte contrainte = contrainteOpt.get();
        if (contrainte.getStatut() == StatutContrainte.DESACTIVE) {
            System.out.println("Info: la contrainte est déjà inactive");
            return true;
        }
        
        contrainte.setStatut(StatutContrainte.DESACTIVE);
        return modifierContrainte(contrainte);
    }
    
    @Override
    public boolean changerStatutContrainte(Long idContrainte, StatutContrainte newStatut) {
        if (newStatut == null) {
            System.err.println("Erreur: le nouveau statut ne peut pas être null");
            return false;
        }
        
        Optional<Contrainte> contrainteOpt = getContrainteById(idContrainte);
        
        if (contrainteOpt.isEmpty()) {
            System.err.println("Contrainte introuvable avec l'ID: " + idContrainte);
            return false;
        }
        
        Contrainte contrainte = contrainteOpt.get();
        contrainte.setStatut(newStatut);
        return modifierContrainte(contrainte);
    }
    
    @Override
    public int compterToutesLesContraintes() {
        return contrainteDAO.compterToutesLesContraintes();
    }
    
    @Override
    public int compterContraintesByStatut(StatutContrainte statut) {
        if (statut == null) {
            return 0;
        }
        
        return contrainteDAO.compterContraintesByStatut(statut);
    }
    
    @Override
    public int compterContraintesActives() {
        return compterContraintesByStatut(StatutContrainte.ACTIVE);
    }
    
    @Override
    public int compterContraintesDesactives() {
        return compterContraintesByStatut(StatutContrainte.DESACTIVE);
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
        
        // Il y a conflit si les plages horaires se chevauchent
        // Pas de conflit si: fin de contrainte <= debut de plage OU debut de contrainte >= fin de plage
        return !(finContrainte.compareTo(heureDebut) <= 0 || debContrainte.compareTo(heureFin) >= 0);
    }
}

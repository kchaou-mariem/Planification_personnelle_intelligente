package test;

import Entities.*;
import service.impl.ContrainteServiceImpl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Classe de test pour le service ContrainteService.
 */
public class TestContrainteService {
    
    private static ContrainteServiceImpl contrainteService = new ContrainteServiceImpl();
    private static int testContrainteId = 0;
    
    public static void main(String[] args) {
        System.out.println("========== TESTS CONTRAINTE SERVICE ==========\n");
        
        testAjouterContrainte();
        testGetContrainteById();
        testModifierContrainte();
        testActiverDesactiverContrainte();
        testGetContraintesActives();
        testGetContraintesDesactives();
        testCompterContraintes();
        testEstEnConflit();
        testGetContraintesByType();
        testGetContraintesRepetitives();
       // testSupprimerContrainte();
        
        System.out.println("\n========== TESTS TERMINÉS ==========");
    }
    
    // ==================== TESTS CONTRAINTE SERVICE ====================
    
    private static void testAjouterContrainte() {
        System.out.println(">>> TEST AJOUTER CONTRAINTE");
        Contrainte c = new Contrainte(
            "Repas midi",
            TypeContrainte.Repos,
            LocalTime.of(12, 0),
            LocalTime.of(13, 30),
            true,
            Arrays.asList(LocalDate.of(2025, 12, 25), LocalDate.of(2025, 1, 1)),
            Arrays.asList(DayOfWeek.MONDAY),
            1
        );
        c.setStatut(StatutContrainte.ACTIVE);
        
        testContrainteId = contrainteService.ajouterContrainte(c);
        if (testContrainteId > 0) {
            System.out.println("✓ Contrainte insérée avec id=" + testContrainteId);
        } else {
            System.out.println("✗ Erreur lors de l'insertion");
        }
        System.out.println();
    }
    
    private static void testGetContrainteById() {
        System.out.println(">>> TEST GET CONTRAINTE BY ID");
        if (testContrainteId >0) {
            Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
            if (opt.isPresent()) {
                System.out.println("✓ Contrainte trouvée: " + opt.get().getTitre());
                System.out.println("  Statut: " + opt.get().getStatut());
            } else {
                System.out.println("✗ Contrainte non trouvée");
            }
        }
        System.out.println();
    }
    
    private static void testModifierContrainte() {
        System.out.println(">>> TEST MODIFIER CONTRAINTE");
        if (testContrainteId >0) {
            Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
            if (opt.isPresent()) {
                Contrainte c = opt.get();
                c.setTitre("Repas modifié");
                c.setDateHeureDeb(LocalTime.of(12, 30));
                
                boolean success = contrainteService.modifierContrainte(c);
                if (success) {
                    System.out.println("✓ Contrainte modifiée avec succès");
                } else {
                    System.out.println("✗ Erreur lors de la modification");
                }
            }
        }
        System.out.println();
    }
    
    private static void testActiverDesactiverContrainte() {
        System.out.println(">>> TEST ACTIVER/DESACTIVER CONTRAINTE");
        if (testContrainteId >0) {
            // Désactiver
            boolean success = contrainteService.desactiverContrainte(testContrainteId);
            if (success) {
                System.out.println("✓ Contrainte désactivée");
                Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
                if (opt.isPresent() && opt.get().getStatut() == StatutContrainte.DESACTIVE) {
                    System.out.println("  ✓ Statut vérifié: DESACTIVE");
                }
            }
            
            // Activer
            success = contrainteService.activerContrainte(testContrainteId);
            if (success) {
                System.out.println("✓ Contrainte activée");
                Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
                if (opt.isPresent() && opt.get().getStatut() == StatutContrainte.ACTIVE) {
                    System.out.println("  ✓ Statut vérifié: ACTIVE");
                }
            }
        }
        System.out.println();
    }
    
    private static void testGetContraintesActives() {
        System.out.println(">>> TEST GET CONTRAINTES ACTIVES");
        List<Contrainte> actives = contrainteService.getContraintesActives();
        System.out.println("✓ Nombre de contraintes actives: " + actives.size());
        actives.forEach(c -> System.out.println("  - " + c.getTitre()));
        System.out.println();
    }
    
    private static void testGetContraintesDesactives() {
        System.out.println(">>> TEST GET CONTRAINTES DESACTIVES");
        List<Contrainte> desactives = contrainteService.getContraintesDesactives();
        System.out.println("✓ Nombre de contraintes désactivées: " + desactives.size());
        desactives.forEach(c -> System.out.println("  - " + c.getTitre()));
        System.out.println();
    }
    
    private static void testCompterContraintes() {
        System.out.println(">>> TEST COMPTER CONTRAINTES");
        int total = contrainteService.compterToutesLesContraintes();
        int actives = contrainteService.compterContraintesActives();
        int desactives = contrainteService.compterContraintesDesactives();
        
        System.out.println("✓ Total: " + total);
        System.out.println("✓ Actives: " + actives);
        System.out.println("✓ Désactivées: " + desactives);
        System.out.println();
    }
    
    private static void testEstEnConflit() {
        System.out.println(">>> TEST EST EN CONFLIT");
        if (testContrainteId >0) {
            Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
            if (opt.isPresent()) {
                Contrainte c = opt.get();
                
                // Conflit: plage chevauchant
                boolean conflit1 = contrainteService.estEnConflit(c, LocalTime.of(12, 15), LocalTime.of(13, 0));
                System.out.println("✓ Conflit [12:15-13:00] avec [" + c.getDateHeureDeb() + "-" + c.getDateHeureFin() + "]: " + conflit1);
                
                // Pas de conflit: plage antérieure
                boolean conflit2 = contrainteService.estEnConflit(c, LocalTime.of(11, 0), LocalTime.of(12, 0));
                System.out.println("✓ Conflit [11:00-12:00] avec [" + c.getDateHeureDeb() + "-" + c.getDateHeureFin() + "]: " + conflit2);
                
                // Pas de conflit: plage postérieure
                boolean conflit3 = contrainteService.estEnConflit(c, LocalTime.of(13, 30), LocalTime.of(14, 30));
                System.out.println("✓ Conflit [13:30-14:30] avec [" + c.getDateHeureDeb() + "-" + c.getDateHeureFin() + "]: " + conflit3);
            }
        }
        System.out.println();
    }
    
    private static void testGetContraintesByType() {
        System.out.println(">>> TEST GET CONTRAINTES BY TYPE");
        List<Contrainte> reposContraintes = contrainteService.getContraintesByType(TypeContrainte.Repos);
        System.out.println("✓ Contraintes de type Repos: " + reposContraintes.size());
        reposContraintes.forEach(c -> System.out.println("  - " + c.getTitre()));
        System.out.println();
    }
    
    private static void testGetContraintesRepetitives() {
        System.out.println(">>> TEST GET CONTRAINTES REPETITIVES");
        List<Contrainte> repetitives = contrainteService.getContraintesRepetitives();
        System.out.println("✓ Contraintes répétitives: " + repetitives.size());
        repetitives.forEach(c -> System.out.println("  - " + c.getTitre()));
        System.out.println();
    }
    
    private static void testSupprimerContrainte() {
        System.out.println(">>> TEST SUPPRIMER CONTRAINTE");
        if (testContrainteId >0) {
            boolean success = contrainteService.supprimerContrainte(testContrainteId);
            if (success) {
                System.out.println("✓ Contrainte supprimée avec succès (ID: " + testContrainteId + ")");
                Optional<Contrainte> opt = contrainteService.getContrainteById(testContrainteId);
                if (opt.isEmpty()) {
                    System.out.println("✓ Confirmé: la contrainte n'existe plus");
                } else {
                    System.out.println("✗ La contrainte existe toujours!");
                }
            } else {
                System.out.println("✗ Erreur lors de la suppression");
            }
        }
        System.out.println();
    }
}

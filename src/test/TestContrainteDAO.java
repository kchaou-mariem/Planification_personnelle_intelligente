package test;

import dao.impl.ContrainteDAOImpl;
import entities.Contrainte;
import entities.StatutContrainte;
import entities.TypeContrainte;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestContrainteDAO {
    
    private static ContrainteDAOImpl dao = new ContrainteDAOImpl();
    private static int testId = 0;
    
    public static void main(String[] args) {
        System.out.println("========== TEST CONTRAINTE DAO ==========\n");
        
        testAjouter();
        testGetById();
        testModifier();
        testGetContraintesByStatut();
        testGetContraintesActives();
        testGetContraintesDesactives();
        testCompterContraintesByStatut();
        testGetAll();
        testGetRepetitives();
        testGetNonRepetitives();
        testCompterToutesLesContraintes();
        testSupprimer();
        
        System.out.println("\n========== TESTS TERMINÉS ==========");
    }
    
    private static void testAjouter() {
        System.out.println(">>> TEST AJOUTER");
        Contrainte c = new Contrainte(
            "Pause déjeuner",
            TypeContrainte.Sommeil,
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            true,
            Arrays.asList(LocalDate.of(2025, 12, 25)),
            Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            1
        );
        c.setUtilisateurId(1);
        c.setStatut(StatutContrainte.ACTIVE);
        
        testId = dao.ajouter(c);
        if (testId > 0) {
            System.out.println("✓ Contrainte insérée avec id=" + testId);
        } else {
            System.out.println("✗ Erreur lors de l'insertion");
        }
        System.out.println();
    }
    
    private static void testGetById() {
        System.out.println(">>> TEST GET BY ID");
        if (testId > 0) {
            dao.getById(testId).ifPresentOrElse(
                contr -> System.out.println("✓ Contrainte trouvée: " + contr),
                () -> System.out.println("✗ Contrainte non trouvée")
            );
        }
        System.out.println();
    }
    
    private static void testModifier() {
        System.out.println(">>> TEST MODIFIER");
        if (testId > 0) {
            dao.getById(testId).ifPresent(c -> {
                c.setTitre("Pause déjeuner modifiée");
                c.setStatut(StatutContrainte.DESACTIVE);
                c.setDateHeureDeb(LocalTime.of(12, 30));
                
                boolean success = dao.modifier(c);
                if (success) {
                    System.out.println("✓ Contrainte modifiée avec succès");
                    dao.getById(testId).ifPresent(updated -> {
                        System.out.println("  Nouvelle valeur: " + updated);
                    });
                } else {
                    System.out.println("✗ Erreur lors de la modification");
                }
            });
        }
        System.out.println();
    }
    
    private static void testGetContraintesByStatut() {
        System.out.println(">>> TEST GET CONTRAINTES BY STATUT");
        List<Contrainte> actives = dao.getContraintesByStatut(StatutContrainte.ACTIVE);
        System.out.println("✓ Contraintes ACTIVE: " + actives.size());
        actives.forEach(c -> System.out.println("  - " + c.getTitre() + " (" + c.getStatut() + ")"));
        
        List<Contrainte> desactives = dao.getContraintesByStatut(StatutContrainte.DESACTIVE);
        System.out.println("✓ Contraintes DESACTIVE: " + desactives.size());
        desactives.forEach(c -> System.out.println("  - " + c.getTitre() + " (" + c.getStatut() + ")"));
        System.out.println();
    }
    
    private static void testGetContraintesActives() {
        System.out.println(">>> TEST GET CONTRAINTES ACTIVES");
        List<Contrainte> actives = dao.getContraintesActives();
        System.out.println("✓ Nombre de contraintes actives: " + actives.size());
        actives.forEach(c -> System.out.println("  - " + c.getTitre() + " (" + c.getId() + ")"));
        System.out.println();
    }
    
    private static void testGetContraintesDesactives() {
        System.out.println(">>> TEST GET CONTRAINTES DESACTIVES");
        List<Contrainte> desactives = dao.getContraintesDesactives();
        System.out.println("✓ Nombre de contraintes désactivées: " + desactives.size());
        desactives.forEach(c -> System.out.println("  - " + c.getTitre() + " (" + c.getId() + ")"));
        System.out.println();
    }
    
    private static void testCompterContraintesByStatut() {
        System.out.println(">>> TEST COMPTER CONTRAINTES BY STATUT");
        int activeCount = dao.compterContraintesByStatut(StatutContrainte.ACTIVE);
        int desactiveCount = dao.compterContraintesByStatut(StatutContrainte.DESACTIVE);
        System.out.println("✓ Total ACTIVE: " + activeCount);
        System.out.println("✓ Total DESACTIVE: " + desactiveCount);
        System.out.println();
    }
    
    private static void testGetAll() {
        System.out.println(">>> TEST GET ALL");
        List<Contrainte> all = dao.getAll();
        System.out.println("✓ Nombre total de contraintes: " + all.size());
        all.forEach(c -> System.out.println("  - " + c.getTitre() + " (ID: " + c.getId() + ", Statut: " + c.getStatut() + ")"));
        System.out.println();
    }
    
    private static void testGetRepetitives() {
        System.out.println(">>> TEST GET REPETITIVES");
        List<Contrainte> rep = dao.getRepetitives();
        System.out.println("✓ Nombre de contraintes répétitives: " + rep.size());
        rep.forEach(c -> System.out.println("  - " + c.getTitre() + " (Jours: " + c.getJoursSemaine() + ")"));
        System.out.println();
    }
    
    private static void testGetNonRepetitives() {
        System.out.println(">>> TEST GET NON REPETITIVES");
        List<Contrainte> nonRep = dao.getNonRepetitives();
        System.out.println("✓ Nombre de contraintes non répétitives: " + nonRep.size());
        nonRep.forEach(c -> System.out.println("  - " + c.getTitre()));
        System.out.println();
    }
    
    private static void testCompterToutesLesContraintes() {
        System.out.println(">>> TEST COMPTER TOUTES LES CONTRAINTES");
        int total = dao.compterToutesLesContraintes();
        System.out.println("✓ Total des contraintes: " + total);
        System.out.println();
    }
    
    private static void testSupprimer() {
        System.out.println(">>> TEST SUPPRIMER");
        if (testId > 0) {
            boolean success = dao.supprimer(testId);
            if (success) {
                System.out.println("✓ Contrainte supprimée avec succès (ID: " + testId + ")");
                dao.getById(testId).ifPresentOrElse(
                    c -> System.out.println("✗ La contrainte existe toujours!"),
                    () -> System.out.println("✓ Confirmé: la contrainte n'existe plus")
                );
            } else {
                System.out.println("✗ Erreur lors de la suppression");
            }
        }
        System.out.println();
    }
}

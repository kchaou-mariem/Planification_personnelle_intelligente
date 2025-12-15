package test;

import Entities.Activite;
import Entities.TypeActivite;
import service.ActiviteService;
import service.impl.ActiviteServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests de validation et de cas limites pour le Service Activite
 */
public class TestActiviteService {
    
    private static ActiviteService activiteService;
    private static final Long TEST_USER_ID = 1L;
    
    public static void main(String[] args) {
        activiteService = new ActiviteServiceImpl();
        
        System.out.println("=== TESTS SERVICE ACTIVITE (VALIDATION) ===\n");
        
        testValidationHoraires();
        testValidationDuree();
        testValidationPriorite();
        testValidationActiviteComplete();
        testCasLimites();
        testStatistiquesUtilisateur();
        testActivitesDeadlineProche();
        testActivitesHautePriorite();
        
        System.out.println("\n=== TESTS VALIDATION TERMINÉS ===");
    }
    
    /**
     * Test validation des horaires
     */
    private static void testValidationHoraires() {
        System.out.println("Test 1 : Validation des horaires");
        System.out.println("-".repeat(50));
        
        // Cas valide
        LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 11, 0);
        
        if (activiteService.validerHoraires(debut, fin)) {
            System.out.println("✓ Horaires valides acceptés");
        } else {
            System.out.println("✗ Horaires valides rejetés");
        }
        
        // Cas invalide : début > fin
        LocalDateTime debutInvalide = LocalDateTime.of(2024, 12, 20, 11, 0);
        LocalDateTime finInvalide = LocalDateTime.of(2024, 12, 20, 10, 0);
        
        if (!activiteService.validerHoraires(debutInvalide, finInvalide)) {
            System.out.println("✓ Horaires invalides (début > fin) rejetés");
        } else {
            System.out.println("✗ Horaires invalides acceptés");
        }
        
        // Cas invalide : début = fin
        if (!activiteService.validerHoraires(debut, debut)) {
            System.out.println("✓ Horaires identiques rejetés");
        } else {
            System.out.println("✗ Horaires identiques acceptés");
        }
        
        System.out.println();
    }
    
    /**
     * Test validation de la durée
     */
    private static void testValidationDuree() {
        System.out.println("Test 2 : Validation de la durée");
        System.out.println("-".repeat(50));
        
        LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 11, 30);  // 90 minutes
        
        // Cas valide
        if (activiteService.validerDuree(90, debut, fin)) {
            System.out.println("✓ Durée correcte acceptée");
        } else {
            System.out.println("✗ Durée correcte rejetée");
        }
        
        // Cas invalide : durée incorrecte
        if (!activiteService.validerDuree(60, debut, fin)) {
            System.out.println("✓ Durée incorrecte rejetée");
        } else {
            System.out.println("✗ Durée incorrecte acceptée");
        }
        
        // Cas invalide : durée négative
        if (!activiteService.validerDuree(-30, debut, fin)) {
            System.out.println("✓ Durée négative rejetée");
        } else {
            System.out.println("✗ Durée négative acceptée");
        }
        
        System.out.println();
    }
    
    /**
     * Test validation de la priorité
     */
    private static void testValidationPriorite() {
        System.out.println("Test 3 : Validation de la priorité");
        System.out.println("-".repeat(50));
        
        // Cas valides
        for (int p : new int[]{1, 5, 10}) {
            if (activiteService.validerPriorite(p)) {
                System.out.println("✓ Priorité " + p + " acceptée");
            } else {
                System.out.println("✗ Priorité " + p + " rejetée");
            }
        }
        
        // Cas invalides
        for (int p : new int[]{0, -1, 11, 15}) {
            if (!activiteService.validerPriorite(p)) {
                System.out.println("✓ Priorité " + p + " rejetée");
            } else {
                System.out.println("✗ Priorité " + p + " acceptée");
            }
        }
        
        System.out.println();
    }
    
    /**
     * Test validation d'une activité complète
     */
    private static void testValidationActiviteComplete() {
        System.out.println("Test 4 : Validation d'une activité complète");
        System.out.println("-".repeat(50));
        
        LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 12, 0);
        LocalDateTime deadline = LocalDateTime.of(2024, 12, 21, 17, 0);
        
        // Activité valide
        Activite activiteValide = new Activite(
            "Cours",
            "Cours de Java",
            TypeActivite.Etude,
            120,
            7,
            deadline,
            debut,
            fin,
            TEST_USER_ID
        );
        
        Long idCreated = activiteService.creerActivite(activiteValide);
        if (idCreated > 0) {
            System.out.println("✓ Activité valide créée");
        } else {
            System.out.println("✗ Activité valide rejetée");
        }
        
        // Activité invalide (titre vide)
        Activite activiteInvalide = new Activite(
            "",
            "Description",
            TypeActivite.Travail,
            60,
            5,
            deadline,
            debut,
            LocalDateTime.of(2024, 12, 20, 11, 0),
            TEST_USER_ID
        );
        
        Long idInvalide = activiteService.creerActivite(activiteInvalide);
        if (idInvalide < 0) {
            System.out.println("✓ Activité invalide (titre vide) rejetée");
        } else {
            System.out.println("✗ Activité invalide acceptée");
        }
        
        System.out.println();
    }
    
    /**
     * Test cas limites
     */
    private static void testCasLimites() {
        System.out.println("Test 5 : Cas limites");
        System.out.println("-".repeat(50));
        
        // Test avec ID utilisateur null
        List<Activite> activites = activiteService.obtenirActivitesUtilisateur(null);
        if (activites.isEmpty()) {
            System.out.println("✓ Requête avec ID null retourne une liste vide");
        }
        
        // Test avec ID invalide
        activites = activiteService.obtenirActivitesUtilisateur(-1L);
        if (activites.isEmpty()) {
            System.out.println("✓ Requête avec ID invalide retourne une liste vide");
        }
        
        // Test recherche avec mot-clé vide
        activites = activiteService.rechercherActivites("");
        if (activites.isEmpty()) {
            System.out.println("✓ Recherche avec mot-clé vide retourne une liste vide");
        }
        
        // Test avec nombre d'activités invalide
        activites = activiteService.obtenirActivitesRecentes(-5);
        if (activites.isEmpty()) {
            System.out.println("✓ Requête avec nombre invalide retourne une liste vide");
        }
        
        System.out.println();
    }
    
    /**
     * Test statistiques par utilisateur
     */
    private static void testStatistiquesUtilisateur() {
        System.out.println("Test 6 : Statistiques par utilisateur");
        System.out.println("-".repeat(50));
        
        try {
            int nombre = activiteService.obtenirNombreActivitesUtilisateur(TEST_USER_ID);
            double taux = activiteService.obtenirTauxCompletionUtilisateur(TEST_USER_ID);
            int duree = activiteService.obtenirDureeTotalActivitesUtilisateur(TEST_USER_ID);
            
            System.out.println("Utilisateur " + TEST_USER_ID + ":");
            System.out.println("  Nombre d'activités: " + nombre);
            System.out.printf("  Taux de complétude: %.2f%%\n", taux);
            System.out.println("  Durée totale: " + duree + " minutes");
            System.out.println("✓ Statistiques calculées");
        } catch (Exception e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test activités avec deadline proche
     */
    private static void testActivitesDeadlineProche() {
        System.out.println("Test 7 : Activités avec deadline proche");
        System.out.println("-".repeat(50));
        
        try {
            List<Activite> activites = activiteService.obtenirActivitesDeadlineProche(7);
            
            System.out.println("Activités avec deadline dans les 7 prochains jours: " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println("  " + (i + 1) + ". " + a.getTitre() + 
                                 " (deadline: " + a.getDeadline() + ")");
            }
            System.out.println("✓ Activités deadline trouvées");
        } catch (Exception e) {
            System.out.println("⚠ Erreur ou aucune activité: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test activités haute priorité
     */
    private static void testActivitesHautePriorite() {
        System.out.println("Test 8 : Activités haute priorité");
        System.out.println("-".repeat(50));
        
        try {
            List<Activite> activites = activiteService.obtenirActivitesHautePriorite();
            
            System.out.println("Activités haute priorité (≥8): " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println("  " + (i + 1) + ". " + a.getTitre() + 
                                 " (priorité: " + a.getPriorite() + ")");
            }
            System.out.println("✓ Activités haute priorité trouvées");
        } catch (Exception e) {
            System.out.println("⚠ Erreur ou aucune activité: " + e.getMessage());
        }
        
        System.out.println();
    }
}

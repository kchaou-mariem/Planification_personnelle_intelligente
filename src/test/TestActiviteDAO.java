package test;

import dao.interfaces.ActiviteDAO;
import entities.Activite;
import entities.TypeActivite;
import dao.impl.ActiviteDAOImpl;
import service.ActiviteService;
import service.impl.ActiviteServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Tests pour la classe Activite et ses opérations DAO/Service
 * Contient des tests unitaires pour valider les fonctionnalités
 */
public class TestActiviteDAO {
    
    private static ActiviteDAO activiteDAO;
    private static ActiviteService activiteService;
    private static final Long TEST_USER_ID = 1L;
    
    public static void main(String[] args) {
        // Initialisation
        activiteDAO = new ActiviteDAOImpl();
        activiteService = new ActiviteServiceImpl(activiteDAO);
        
        System.out.println("=== TESTS ACTIVITE DAO ===\n");
        
        // Tests
        testAjouterActivite();
        testObtenirActivite();
        testModifierActivite();
        testObtenirActivitesUtilisateur();
        testObtenirActivitesParType();
        testVerifierChevauchement();
        testMarquerCommeCompletee();
        testRechercheParMotCle();
        testStatistiques();
        testSupprimerActivite();
        
        System.out.println("\n=== TESTS TERMINÉS ===");
    }
    
    /**
     * Test : Ajouter une activité
     */
    private static void testAjouterActivite() {
        System.out.println("Test 1 : Ajouter une activité");
        System.out.println("-".repeat(50));
        
        try {
            LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 10, 0);
            LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 11, 30);
            LocalDateTime deadline = LocalDateTime.of(2024, 12, 21, 17, 0);
            
            Activite activite = new Activite(
                "Réunion importante",
                "Réunion avec l'équipe de projet",
                TypeActivite.Travail,
                90,  // 1h30
                8,   // priorité haute
                deadline,
                debut,
                fin,
                TEST_USER_ID
            );
            
            Long idActivite = activiteService.creerActivite(activite);
            
            if (idActivite > 0) {
                System.out.println("✓ Activité ajoutée avec succès. ID: " + idActivite);
            } else {
                System.out.println("✗ Erreur lors de l'ajout");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Obtenir une activité par ID
     */
    private static void testObtenirActivite() {
        System.out.println("Test 2 : Obtenir une activité par ID");
        System.out.println("-".repeat(50));
        
        try {
            Optional<Activite> activite = activiteService.obtenirActivite(1L);
            
            if (activite.isPresent()) {
                System.out.println("✓ Activité trouvée: " + activite.get().getTitre());
                System.out.println("  Type: " + activite.get().getType());
                System.out.println("  Priorité: " + activite.get().getPriorite());
            } else {
                System.out.println("✗ Activité non trouvée");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Modifier une activité
     */
    private static void testModifierActivite() {
        System.out.println("Test 3 : Modifier une activité");
        System.out.println("-".repeat(50));
        
        try {
            Optional<Activite> activiteOpt = activiteService.obtenirActivite(1L);
            
            if (activiteOpt.isPresent()) {
                Activite activite = activiteOpt.get();
                
                // Modification
                LocalDateTime nouvelleFin = activite.getHoraireDebut().plusHours(2);
                activite.setHoraireFin(nouvelleFin);
                activite.setDuree(120);  // 2 heures
                activite.setPriorite(9);
                
                if (activiteService.mettreAJourActivite(activite)) {
                    System.out.println("✓ Activité modifiée avec succès");
                } else {
                    System.out.println("✗ Erreur lors de la modification");
                }
            } else {
                System.out.println("✗ Activité non trouvée");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Obtenir les activités d'un utilisateur
     */
    private static void testObtenirActivitesUtilisateur() {
        System.out.println("Test 4 : Obtenir les activités d'un utilisateur");
        System.out.println("-".repeat(50));
        
        try {
            List<Activite> activites = activiteService.obtenirActivitesUtilisateur(TEST_USER_ID);
            
            System.out.println("Nombre d'activités: " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println((i + 1) + ". " + a.getTitre() + " (" + a.getType() + ")");
            }
            
            if (!activites.isEmpty()) {
                System.out.println("✓ Activités trouvées");
            } else {
                System.out.println("⚠ Aucune activité trouvée");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Obtenir les activités par type
     */
    private static void testObtenirActivitesParType() {
        System.out.println("Test 5 : Obtenir les activités par type");
        System.out.println("-".repeat(50));
        
        try {
            List<Activite> activites = activiteService.obtenirActivitesParType(TypeActivite.Travail);
            
            System.out.println("Activités de type TRAVAIL: " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println((i + 1) + ". " + a.getTitre());
            }
            
            if (!activites.isEmpty()) {
                System.out.println("✓ Activités trouvées");
            } else {
                System.out.println("⚠ Aucune activité de type TRAVAIL");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Vérifier le chevauchement
     */
    private static void testVerifierChevauchement() {
        System.out.println("Test 6 : Vérifier le chevauchement");
        System.out.println("-".repeat(50));
        
        try {
            LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 10, 30);
            LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 11, 0);
            
            boolean chevauchement = activiteService.verifierChevauchement(TEST_USER_ID, debut, fin);
            
            if (chevauchement) {
                System.out.println("✓ Chevauchement détecté");
                
                List<Activite> activites = activiteService.obtenirActivitesChevauchantes(TEST_USER_ID, debut, fin);
                System.out.println("Activités chevauchantes: " + activites.size());
                for (Activite a : activites) {
                    System.out.println("  - " + a.getTitre());
                }
            } else {
                System.out.println("⚠ Pas de chevauchement détecté");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Marquer une activité comme complétée
     */
    private static void testMarquerCommeCompletee() {
        System.out.println("Test 7 : Marquer une activité comme complétée");
        System.out.println("-".repeat(50));
        
        try {
            // D'abord obtenir une activité
            Optional<Activite> activiteOpt = activiteService.obtenirActivite(1L);
            
            if (activiteOpt.isPresent()) {
                Activite activite = activiteOpt.get();
                
                if (activiteService.completerActivite(activite.getIdActivite())) {
                    System.out.println("✓ Activité marquée comme complétée");
                } else {
                    System.out.println("✗ Erreur lors du marquage");
                }
                
                // Vérifier l'état
                Optional<Activite> activiteModifiee = activiteService.obtenirActivite(1L);
                if (activiteModifiee.isPresent() && activiteModifiee.get().isCompletee()) {
                    System.out.println("✓ Vérification: L'activité est bien complétée");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Recherche par mot-clé
     */
    private static void testRechercheParMotCle() {
        System.out.println("Test 8 : Recherche par mot-clé");
        System.out.println("-".repeat(50));
        
        try {
            List<Activite> activites = activiteService.rechercherActivitesUtilisateur(TEST_USER_ID, "Réunion");
            
            System.out.println("Résultats pour 'Réunion': " + activites.size());
            for (Activite a : activites) {
                System.out.println("  - " + a.getTitre() + " (" + a.getDescription() + ")");
            }
            
            if (!activites.isEmpty()) {
                System.out.println("✓ Résultats trouvés");
            } else {
                System.out.println("⚠ Aucun résultat");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Statistiques
     */
    private static void testStatistiques() {
        System.out.println("Test 9 : Statistiques");
        System.out.println("-".repeat(50));
        
        try {
            int total = activiteService.obtenirNombreTotalActivites();
            int completees = activiteService.obtenirNombreActivitesCompletees();
            int nonCompletees = activiteService.obtenirNombreActivitesNonCompletees();
            double taux = activiteService.obtenirTauxCompletion();
            int dureeTotal = activiteService.obtenirDureeTotalActivites();
            
            System.out.println("Total d'activités: " + total);
            System.out.println("Complétées: " + completees);
            System.out.println("Non complétées: " + nonCompletees);
            System.out.printf("Taux de complétude: %.2f%%\n", taux);
            System.out.println("Durée totale: " + dureeTotal + " minutes (" + (dureeTotal / 60) + "h)");
            
            System.out.println("✓ Statistiques calculées");
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * Test : Supprimer une activité
     */
    private static void testSupprimerActivite() {
        System.out.println("Test 10 : Supprimer une activité");
        System.out.println("-".repeat(50));
        
        try {
            // Créer une activité de test à supprimer
            LocalDateTime debut = LocalDateTime.of(2024, 12, 25, 14, 0);
            LocalDateTime fin = LocalDateTime.of(2024, 12, 25, 15, 0);
            LocalDateTime deadline = LocalDateTime.of(2024, 12, 26, 17, 0);
            
            Activite activiteTest = new Activite(
                "Activité à supprimer",
                "Test suppression",
                TypeActivite.Loisirs,
                60,
                3,
                deadline,
                debut,
                fin,
                TEST_USER_ID
            );
            
            Long idActivite = activiteService.creerActivite(activiteTest);
            
            if (idActivite > 0) {
                System.out.println("Activité créée avec ID: " + idActivite);
                
                if (activiteService.supprimerActivite(idActivite)) {
                    System.out.println("✓ Activité supprimée avec succès");
                    
                    // Vérifier qu'elle n'existe plus
                    Optional<Activite> activiteDeleted = activiteService.obtenirActivite(idActivite);
                    if (activiteDeleted.isEmpty()) {
                        System.out.println("✓ Vérification: L'activité n'existe plus");
                    }
                } else {
                    System.out.println("✗ Erreur lors de la suppression");
                }
            } else {
                System.out.println("✗ Erreur lors de la création");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
}

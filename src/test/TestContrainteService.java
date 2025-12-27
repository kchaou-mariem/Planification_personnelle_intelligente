package test;

import service.impl.ContrainteServiceImpl;
import entities.Contrainte;
import entities.TypeContrainte;
import entities.StatutContrainte;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestContrainteService {
    
    private static ContrainteServiceImpl contrainteService = new ContrainteServiceImpl();
    private static int testContrainteId = 0;
    private static final int TEST_USER_ID = 1;
    
    public static void main(String[] args) {
        System.out.println("========== TESTS CONTRAINTE SERVICE ==========\n");
        
        // Test simple et propre
        testSimple();
        
        System.out.println("\n========== TESTS TERMINÉS ==========");
    }
    
    private static void testSimple() {
        System.out.println(">>> TEST SIMPLE ET COMPLET");
        
        // 1. Créer une contrainte
        Contrainte c = new Contrainte();
        c.setTitre("Test contrainte");
        c.setType(TypeContrainte.Travail);
        c.setDateHeureDeb(LocalTime.of(9, 0));
        c.setDateHeureFin(LocalTime.of(10, 0));
        c.setRepetitif(false);
        c.setDatesSpecifiques(Arrays.asList(LocalDate.now()));
        c.setJoursSemaine(Arrays.asList());
        c.setUtilisateurId(TEST_USER_ID);
        
        System.out.println("1. Ajout de contrainte...");
        boolean ajoutOk = contrainteService.ajouter(c);
        System.out.println("   Résultat: " + ajoutOk);
        System.out.println("   ID dans l'objet après ajout: " + c.getId());
        
        if (ajoutOk && c.getId() > 0) {
            testContrainteId = c.getId();
            
            // 2. Récupérer par ID
            System.out.println("\n2. Récupération par ID=" + testContrainteId + "...");
            Contrainte recuperee = contrainteService.getById(testContrainteId);
            if (recuperee != null) {
                System.out.println("   ✓ Trouvée: " + recuperee.getTitre());
            } else {
                System.out.println("   ✗ Non trouvée");
            }
            
            // 3. Lister par utilisateur
            System.out.println("\n3. Liste par utilisateur...");
            List<Contrainte> contraintes = contrainteService.getByUtilisateur(TEST_USER_ID);
            System.out.println("   Nombre: " + contraintes.size());
            
            // 4. Toggle statut
            System.out.println("\n4. Toggle statut...");
            boolean toggleOk = contrainteService.toggleStatut(testContrainteId);
            System.out.println("   Résultat: " + toggleOk);
            
            if (toggleOk) {
                Contrainte apresToggle = contrainteService.getById(testContrainteId);
                System.out.println("   Nouveau statut: " + (apresToggle != null ? apresToggle.getStatut() : "null"));
            }
            
            // 5. Supprimer
           /* System.out.println("\n5. Suppression...");
            boolean suppressionOk = contrainteService.supprimer(testContrainteId);
            System.out.println("   Résultat: " + suppressionOk);
            */
            // 6. Vérifier suppression
            System.out.println("\n6. Vérification suppression...");
            Contrainte supprimee = contrainteService.getById(testContrainteId);
            System.out.println("   Contrainte existe encore: " + (supprimee != null));
        }
    }
}
package test;

import service.impl.ConflitServiceImpl;
import service.ConflitService;
import entities.Conflit;
import dao.impl.ConflitDAOImpl;
import dao.impl.ActiviteDAOImpl;
import entities.Activite;
import entities.TypeActivite;

import java.time.LocalDateTime;
import java.util.List;

public class TestConflitService {
    public static void main(String[] args) {
        ConflitService service = new ConflitServiceImpl();
        ConflitDAOImpl conflitDAO = new ConflitDAOImpl();
        ActiviteDAOImpl activiteDAO = new ActiviteDAOImpl();

        Long idUtilisateur = 1L; // Adapter selon vos données

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║      TEST COMPLET DU SERVICE DE RÉSOLUTION DE CONFLITS      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Créer des activités de test qui se chevauchent
        System.out.println("📝 Préparation: Création d'activités de test chevauchantes...");
        LocalDateTime maintenant = LocalDateTime.now();
        
        Activite act1 = new Activite(
            "Réunion importante",
            "Réunion d'équipe",
            TypeActivite.Travail,
            60,
            5, // Priorité élevée
            maintenant.plusDays(1),
            maintenant.plusHours(1),
            maintenant.plusHours(2),
            idUtilisateur
        );
        
        Activite act2 = new Activite(
            "Pause déjeuner",
            "Déjeuner",
            TypeActivite.Repos,
            60,
            2, // Priorité plus faible
            maintenant.plusDays(1),
            maintenant.plusHours(1).plusMinutes(30), // Chevauche avec act1
            maintenant.plusHours(2).plusMinutes(30),
            idUtilisateur
        );
        
        Long id1 = activiteDAO.ajouter(act1);
        Long id2 = activiteDAO.ajouter(act2);
        act1.setIdActivite(id1);
        act2.setIdActivite(id2);
        
        System.out.println("✓ Activité 1 ajoutée (ID: " + id1 + ", Priorité: " + act1.getPriorite() + ")");
        System.out.println("✓ Activité 2 ajoutée (ID: " + id2 + ", Priorité: " + act2.getPriorite() + ")");
        System.out.println("  → Chevauchement: " + act1.getHoraireDebut() + " - " + act1.getHoraireFin());
        System.out.println("  → Chevauchement: " + act2.getHoraireDebut() + " - " + act2.getHoraireFin() + "\n");

        // Test 1: Détection des chevauchements
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: Détection des chevauchements");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        List<Conflit> detects = service.detecterChevauchementsUtilisateur(idUtilisateur);
        System.out.println("✅ Conflits détectés et enregistrés: " + detects.size());
        
        for (Conflit c : detects) {
            System.out.println("\n   Conflit ID: " + c.getidConflit());
            System.out.println("   Type: " + c.getType());
            System.out.println("   Détecté à: " + c.getHoraireDetection());
            System.out.println("   Résolu: " + (c.isResolu() ? "✓" : "✗"));
            
            // Afficher les activités liées
            List<Long> activitesLiees = conflitDAO.getActivitesLieesAuConflit(c.getidConflit());
            System.out.println("   Activités impliquées: " + activitesLiees.size());
            for (Long idAct : activitesLiees) {
                activiteDAO.getById(idAct).ifPresent(a -> 
                    System.out.println("     - " + a.getTitre() + " (Priorité: " + a.getPriorite() + ")")
                );
            }
        }

        // Test 2: Résolution automatique
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: Résolution automatique des chevauchements");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        int resolus = service.resoudreChevauchementsUtilisateur(idUtilisateur);
        System.out.println("✅ Conflits résolus automatiquement: " + resolus);
        
        // Vérifier les horaires après résolution
        System.out.println("\n   Horaires après résolution:");
        activiteDAO.getById(id1).ifPresent(a -> 
            System.out.println("   - " + a.getTitre() + ": " + a.getHoraireDebut() + " → " + a.getHoraireFin())
        );
        activiteDAO.getById(id2).ifPresent(a -> 
            System.out.println("   - " + a.getTitre() + ": " + a.getHoraireDebut() + " → " + a.getHoraireFin())
        );

        // Test 3: Vérification du statut des conflits
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: Vérification du statut des conflits");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        for (Conflit c : detects) {
            conflitDAO.getById(c.getidConflit()).ifPresent(conflit -> {
                System.out.println("   Conflit ID " + conflit.getidConflit() + ": " + 
                    (conflit.isResolu() ? "✓ RÉSOLU" : "✗ NON RÉSOLU"));
            });
        }

        // Test 4: Marquage manuel
        if (!detects.isEmpty()) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("TEST 4: Marquage manuel d'un conflit");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            Long idConflit = detects.get(0).getidConflit();
            boolean ok = service.marquerConflitCommeResolu(idConflit);
            System.out.println("   Marquage manuel du conflit ID " + idConflit + ": " + (ok ? "✓" : "✗"));
        }

        // Nettoyage
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("NETTOYAGE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        activiteDAO.supprimer(id1);
        activiteDAO.supprimer(id2);
        for (Conflit c : detects) {
            conflitDAO.supprimerLiensConflit(c.getidConflit());
            conflitDAO.supprimer(c.getidConflit());
        }
        System.out.println("✓ Données de test nettoyées\n");
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TESTS TERMINÉS                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}

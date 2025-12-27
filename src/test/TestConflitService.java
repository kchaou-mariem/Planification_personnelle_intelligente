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
        System.out.println("║      TEST COMPLET DU SERVICE DE GESTION DE CONFLITS         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Créer des activités de test qui se chevauchent
        System.out.println("📝 Préparation: Création d'activités de test chevauchantes...");
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Utiliser le constructeur SANS ID (8 paramètres) :
        // Activite(String titre, TypeActivite typeActivite, String description, 
        //         Integer priorite, LocalDateTime deadline, 
        //         LocalDateTime horaireDebut, LocalDateTime horaireFin, Long idUtilisateur)
        
        Activite act1 = new Activite(
            "Réunion importante",           // titre
            TypeActivite.Travail,          // typeActivite
            "Réunion d'équipe",            // description
            5,                             // priorite (Integer)
            maintenant.plusDays(2),        // deadline (après-demain)
            maintenant.plusDays(1).plusHours(1),  // horaireDebut (demain 1h)
            maintenant.plusDays(1).plusHours(2),  // horaireFin (demain 2h)
            idUtilisateur                  // idUtilisateur
        );
        
        Activite act2 = new Activite(
            "Pause déjeuner",              // titre
            TypeActivite.Repos,            // typeActivite
            "Déjeuner",                    // description
            2,                             // priorite
            maintenant.plusDays(2),        // deadline (après-demain)
            maintenant.plusDays(1).plusHours(1).plusMinutes(30),  // horaireDebut (chevauche avec act1)
            maintenant.plusDays(1).plusHours(2).plusMinutes(30),  // horaireFin
            idUtilisateur                  // idUtilisateur
        );
        
        long id1 = activiteDAO.ajouter(act1);
        long id2 = activiteDAO.ajouter(act2);
        act1.setIdActivite(id1);
        act2.setIdActivite(id2);
        
        System.out.println("✓ Activité 1 ajoutée (ID: " + id1 + ")");
        System.out.println("  Titre: " + act1.getTitre());
        System.out.println("  Type: " + act1.getTypeActivite());
        System.out.println("  Priorité: " + act1.getPriorite());
        System.out.println("  Horaire: " + act1.getHoraireDebut() + " → " + act1.getHoraireFin());
        
        System.out.println("\n✓ Activité 2 ajoutée (ID: " + id2 + ")");
        System.out.println("  Titre: " + act2.getTitre());
        System.out.println("  Type: " + act2.getTypeActivite());
        System.out.println("  Priorité: " + act2.getPriorite());
        System.out.println("  Horaire: " + act2.getHoraireDebut() + " → " + act2.getHoraireFin());
        
        // Vérifier le chevauchement
        System.out.println("\n📊 Vérification de chevauchement:");
        boolean chevauchement = act1.getHoraireDebut().isBefore(act2.getHoraireFin()) &&
                               act1.getHoraireFin().isAfter(act2.getHoraireDebut());
        System.out.println("  Chevauchement détecté: " + (chevauchement ? "✓ OUI" : "✗ NON") + "\n");

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
                    System.out.println("     - " + a.getTitre() + 
                                     " (Priorité: " + a.getPriorite() + 
                                     ", Type: " + a.getTypeActivite() + ")")
                );
            }
        }

        // Test 2: Marquage manuel
        if (!detects.isEmpty()) {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("TEST 2: Marquage manuel d'un conflit");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            Long idConflit = detects.get(0).getidConflit();
            boolean ok = service.marquerConflitCommeResolu(idConflit);
            System.out.println("   Marquage manuel du conflit ID " + idConflit + ": " + (ok ? "✓" : "✗"));
        }

        // Test 3: Vérification du statut des conflits après marquage
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: Vérification du statut des conflits");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        for (Conflit c : detects) {
            conflitDAO.getById(c.getidConflit()).ifPresent(conflit -> {
                System.out.println("   Conflit ID " + conflit.getidConflit() + ": " + 
                    (conflit.isResolu() ? "✓ RÉSOLU" : "✗ NON RÉSOLU"));
            });
        }

        // Test 4: Statistiques
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: Statistiques des conflits");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        int totalConflits = service.compterConflitsUtilisateur(idUtilisateur);
        int nonResolus = service.compterConflitsNonResolusUtilisateur(idUtilisateur);
        double tauxResolution = service.getTauxResolutionUtilisateur(idUtilisateur);
        
        System.out.println("   Total conflits: " + totalConflits);
        System.out.println("   Conflits non résolus: " + nonResolus);
        System.out.println("   Taux de résolution: " + tauxResolution + "%");

        // Test 5: Récupération des conflits non résolus
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 5: Liste des conflits non résolus");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        List<Conflit> conflitsNonResolus = service.getConflitsNonResolusUtilisateur(idUtilisateur);
        System.out.println("   Nombre de conflits non résolus: " + conflitsNonResolus.size());
        for (Conflit c : conflitsNonResolus) {
            System.out.println("   - Conflit ID " + c.getidConflit() + 
                             " (" + c.getType() + 
                             ") détecté à " + c.getHoraireDetection());
        }

        // Nettoyage
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("NETTOYAGE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        System.out.println("Nettoyage des données de test...");
        
        // Supprimer d'abord les liens conflit-activité, puis les conflits, puis les activités
   /*     for (Conflit c : detects) {
            System.out.println("  Suppression des liens pour conflit ID " + c.getidConflit());
            conflitDAO.supprimerLiensConflit(c.getidConflit());
            
            System.out.println("  Suppression du conflit ID " + c.getidConflit());
            conflitDAO.supprimer(c.getidConflit());
        }
        
        System.out.println("  Suppression de l'activité ID " + id1);
        activiteDAO.supprimer(id1);
        
        System.out.println("  Suppression de l'activité ID " + id2);
        activiteDAO.supprimer(id2);
        
        System.out.println("✓ Données de test nettoyées\n");
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TESTS TERMINÉS                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
   */ }
}
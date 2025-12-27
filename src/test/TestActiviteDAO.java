package test;

import dao.impl.ActiviteDAOImpl;
import dao.interfaces.ActiviteDAO;
import entities.Activite;
import entities.TypeActivite;
import service.ActiviteService;
import service.impl.ActiviteServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TestActiviteDAO {

    private static ActiviteDAO activiteDAO;
    private static ActiviteService activiteService;
    private static final Long TEST_USER_ID = 1L;
    private static Long testActiviteId = 0L;

    public static void main(String[] args) {
        // Initialisation
        activiteDAO = new ActiviteDAOImpl();
        activiteService = new ActiviteServiceImpl(activiteDAO);

        System.out.println("=== TESTS ACTIVITE DAO ===\n");

        // Tests
        testAjouter();
        testGetById();
        testGetByUtilisateur();
        testGetByTypeAndUtilisateur();
        testGetByPeriode();
        testModifier();
        testGetActivitesChevauchantes();
        testRecherche();
        testStatistiques();
       // testSupprimer();

        System.out.println("\n=== TESTS TERMINÉS ===");
    }

    private static void testAjouter() {
        System.out.println("Test 1 : Ajouter une activité");
        System.out.println("-".repeat(50));

        try {
            LocalDateTime debut = LocalDateTime.now().plusHours(1);
            LocalDateTime fin = debut.plusHours(2);
            LocalDateTime deadline = LocalDateTime.now().plusDays(1);

            Activite activite = new Activite();
            activite.setTitre("Réunion importante");
            activite.setDescription("Réunion avec l'équipe de projet");
            activite.setTypeActivite(TypeActivite.Travail);
            activite.setPriorite(8);
            activite.setDeadline(deadline);
            activite.setHoraireDebut(debut);
            activite.setHoraireFin(fin);
            activite.setIdUtilisateur(TEST_USER_ID);

            Long id = activiteDAO.ajouter(activite);

            if (id > 0) {
                testActiviteId = id;
                System.out.println("✓ Activité ajoutée. ID: " + id);
            } else {
                System.out.println("✗ Erreur lors de l'ajout");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testGetById() {
        System.out.println("Test 2 : Obtenir une activité par ID");
        System.out.println("-".repeat(50));

        if (testActiviteId > 0) {
            try {
                Optional<Activite> activite = activiteDAO.getById(testActiviteId);

                if (activite.isPresent()) {
                    System.out.println("✓ Activité trouvée: " + activite.get().getTitre());
                    System.out.println("  Type: " + activite.get().getTypeActivite());
                    System.out.println("  Priorité: " + activite.get().getPriorite());
                } else {
                    System.out.println("✗ Activité non trouvée");
                }
            } catch (Exception e) {
                System.out.println("✗ Exception: " + e.getMessage());
            }
        }
        System.out.println();
    }

    private static void testGetByUtilisateur() {
        System.out.println("Test 3 : Obtenir les activités d'un utilisateur");
        System.out.println("-".repeat(50));

        try {
            List<Activite> activites = activiteDAO.getByUtilisateur(TEST_USER_ID);

            System.out.println("✓ Activités pour utilisateur " + TEST_USER_ID + ": " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println((i + 1) + ". " + a.getTitre() + " (" + a.getTypeActivite() + ")");
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testGetByTypeAndUtilisateur() {
        System.out.println("Test 4 : Obtenir les activités par type et utilisateur");
        System.out.println("-".repeat(50));

        try {
            List<Activite> activites = activiteDAO.getByTypeAndUtilisateur(TEST_USER_ID, TypeActivite.Travail);

            System.out.println("✓ Activités TRAVAIL: " + activites.size());
            for (int i = 0; i < Math.min(activites.size(), 3); i++) {
                Activite a = activites.get(i);
                System.out.println((i + 1) + ". " + a.getTitre());
            }
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testGetByPeriode() {
        System.out.println("Test 5 : Obtenir les activités par période");
        System.out.println("-".repeat(50));

        try {
            LocalDateTime debut = LocalDateTime.now().minusDays(1);
            LocalDateTime fin = LocalDateTime.now().plusDays(2);

            List<Activite> activites = activiteDAO.getByPeriode(debut, fin);

            System.out.println("✓ Activités dans la période: " + activites.size());
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testModifier() {
        System.out.println("Test 6 : Modifier une activité");
        System.out.println("-".repeat(50));

        if (testActiviteId > 0) {
            try {
                Optional<Activite> activiteOpt = activiteDAO.getById(testActiviteId);

                if (activiteOpt.isPresent()) {
                    Activite activite = activiteOpt.get();
                    activite.setTitre("Réunion modifiée");
                    activite.setPriorite(9);

                    boolean success = activiteDAO.modifier(activite);
                    if (success) {
                        System.out.println("✓ Activité modifiée");
                    } else {
                        System.out.println("✗ Erreur lors de la modification");
                    }
                }
            } catch (Exception e) {
                System.out.println("✗ Exception: " + e.getMessage());
            }
        }
        System.out.println();
    }

    private static void testGetActivitesChevauchantes() {
        System.out.println("Test 7 : Obtenir les activités chevauchantes");
        System.out.println("-".repeat(50));

        try {
            LocalDateTime debut = LocalDateTime.now().plusHours(1).plusMinutes(30);
            LocalDateTime fin = debut.plusHours(1);

            List<Activite> activites = activiteDAO.getActivitesChevauchantesUtilisateur(
                    TEST_USER_ID, debut, fin);

            System.out.println("✓ Activités chevauchantes: " + activites.size());
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testRecherche() {
        System.out.println("Test 8 : Recherche par mot-clé");
        System.out.println("-".repeat(50));

        try {
            List<Activite> activites = activiteDAO.rechercherParMotCleUtilisateur(TEST_USER_ID, "réunion");

            System.out.println("✓ Résultats pour 'réunion': " + activites.size());
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testStatistiques() {
        System.out.println("Test 9 : Statistiques");
        System.out.println("-".repeat(50));

        try {
            int total = activiteDAO.compterToutesLesActivites();
            int parUtilisateur = activiteDAO.compterActivitesUtilisateur(TEST_USER_ID);
            int parType = activiteDAO.compterParType(TypeActivite.Travail);

            System.out.println("✓ Total d'activités: " + total);
            System.out.println("✓ Activités utilisateur " + TEST_USER_ID + ": " + parUtilisateur);
            System.out.println("✓ Activités TRAVAIL: " + parType);
        } catch (Exception e) {
            System.out.println("✗ Exception: " + e.getMessage());
        }
        System.out.println();
    }
/*
    private static void testSupprimer() {
        System.out.println("Test 10 : Supprimer une activité");
        System.out.println("-".repeat(50));

        if (testActiviteId > 0) {
            try {
                boolean success = activiteDAO.supprimer(testActiviteId);
                if (success) {
                    System.out.println("✓ Activité supprimée");
                } else {
                    System.out.println("✗ Erreur lors de la suppression");
                }
            } catch (Exception e) {
                System.out.println("✗ Exception: " + e.getMessage());
            }
        }
        System.out.println();
    }
    */
}
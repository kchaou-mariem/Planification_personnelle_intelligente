package test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dao.impl.ConflitDAOImpl;
import entities.Conflit;
import entities.TypeConflit;

/**
 * Classe de test pour démontrer l'utilisation du ConflitDAO
 * Teste toutes les catégories de méthodes disponibles
 */
public class TestConflitDAO {

    public static void main(String[] args) {
        System.out.println("=== TEST ConflitDAO - Démonstration des Méthodes ===\n");
        
        ConflitDAOImpl conflitDAOImpl = new ConflitDAOImpl();
        
        // Tests des différentes catégories
        testOperationsCRUD(conflitDAOImpl);
        testRechercheEtFiltrage(conflitDAOImpl);
        testOperationsMetier(conflitDAOImpl);
        testStatistiques(conflitDAOImpl);
        testMaintenance(conflitDAOImpl);
        
        System.out.println("\n=== FIN DES TESTS ===");
    }
    
    /**
     * Test des opérations CRUD de base
     */
    private static void testOperationsCRUD(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("1. TEST DES OPÉRATIONS CRUD");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // 1.1 Ajouter un conflit
            System.out.println("1.1 Ajout d'un conflit:");
            Conflit nouveauConflit = new Conflit(
                null,
                LocalDateTime.now(),
                TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES,
                false
            );
            Long idConflit = conflitDAOImpl.ajouter(nouveauConflit);
            if (idConflit > 0) {
                System.out.println("✅ Conflit ajouté avec ID: " + idConflit);
            } else {
                System.out.println("❌ Échec de l'ajout");
            }
            
            // 1.2 Récupérer par ID
            System.out.println("\n1.2 Récupération par ID:");
            Optional<Conflit> conflitRecupere = conflitDAOImpl.getById(idConflit);
            if (conflitRecupere.isPresent()) {
                System.out.println("✅ Conflit récupéré: " + conflitRecupere.get());
            } else {
                System.out.println("❌ Conflit non trouvé");
            }
            
            // 1.3 Modifier le conflit
            System.out.println("\n1.3 Modification:");
            if (conflitRecupere.isPresent()) {
                Conflit conflit = conflitRecupere.get();
                conflit.marquerCommeResolu();
                boolean modifie = conflitDAOImpl.modifier(conflit);
                System.out.println(modifie ? "✅ Conflit modifié" : "❌ Échec de modification");
            }
            
            // 1.4 Récupérer tous les conflits
            System.out.println("\n1.4 Récupération de tous les conflits:");
            List<Conflit> tousLesConflits = conflitDAOImpl.getAll();
            System.out.println("✅ Nombre total de conflits: " + tousLesConflits.size());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test CRUD: " + e.getMessage());
        }
    }
    
    /**
     * Test des méthodes de recherche et filtrage
     */
    private static void testRechercheEtFiltrage(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("2. TEST DE RECHERCHE ET FILTRAGE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // 2.1 Recherche par type
            System.out.println("2.1 Conflits par type (CHEVAUCHEMENT):");
            List<Conflit> conflitsChevauchement = conflitDAOImpl.getByType(TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES);
            System.out.println("✅ Nombre: " + conflitsChevauchement.size());
            
            // 2.2 Conflits non résolus
            System.out.println("\n2.2 Conflits non résolus:");
            List<Conflit> conflitsNonResolus = conflitDAOImpl.getConflitsNonResolus();
            System.out.println("✅ Nombre: " + conflitsNonResolus.size());
            if (!conflitsNonResolus.isEmpty()) {
                System.out.println("   Premier: " + conflitsNonResolus.get(0));
            }
            
            // 2.3 Conflits résolus
            System.out.println("\n2.3 Conflits résolus:");
            List<Conflit> conflitsResolus = conflitDAOImpl.getConflitsResolus();
            System.out.println("✅ Nombre: " + conflitsResolus.size());
            
            // 2.4 Conflits par période
            System.out.println("\n2.4 Conflits de la dernière semaine:");
            LocalDateTime debutSemaine = LocalDateTime.now().minusDays(7);
            LocalDateTime finSemaine = LocalDateTime.now();
            List<Conflit> conflitsSemaine = conflitDAOImpl.getByPeriode(debutSemaine, finSemaine);
            System.out.println("✅ Nombre: " + conflitsSemaine.size());
            
            // 2.5 Conflits récents
            System.out.println("\n2.5 Les 5 conflits les plus récents:");
            List<Conflit> conflitsRecents = conflitDAOImpl.getConflitsRecents(5);
            System.out.println("✅ Récupérés: " + conflitsRecents.size());
            conflitsRecents.forEach(c -> System.out.println("   - " + c.getType() + " à " + c.getHoraireDetection()));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test de recherche: " + e.getMessage());
        }
    }
    
    /**
     * Test des opérations métier
     */
    private static void testOperationsMetier(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("3. TEST DES OPÉRATIONS MÉTIER");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // 3.1 Marquer comme résolu
            System.out.println("3.1 Marquer un conflit comme résolu:");
            List<Conflit> conflitsNonResolus = conflitDAOImpl.getConflitsNonResolus();
            if (!conflitsNonResolus.isEmpty()) {
                Long idConflit = conflitsNonResolus.get(0).getidConflit();
                boolean resolu = conflitDAOImpl.marquerCommeResolu(idConflit);
                System.out.println(resolu ? "✅ Conflit marqué comme résolu" : "❌ Échec");
            } else {
                System.out.println("ℹ️ Aucun conflit non résolu disponible");
            }
            
            // 3.2 Marquer plusieurs comme résolus
            System.out.println("\n3.2 Marquer plusieurs conflits comme résolus:");
            conflitsNonResolus = conflitDAOImpl.getConflitsNonResolus();
            if (conflitsNonResolus.size() >= 2) {
                List<Long> ids = Arrays.asList(
                    conflitsNonResolus.get(0).getidConflit(),
                    conflitsNonResolus.get(1).getidConflit()
                );
                int resolus = conflitDAOImpl.marquerPlusieursCommeResolus(ids);
                System.out.println("✅ " + resolus + " conflits marqués comme résolus");
            } else {
                System.out.println("ℹ️ Pas assez de conflits non résolus");
            }
            
            // 3.3 Conflits critiques
            System.out.println("\n3.4 Récupération des conflits critiques:");
            List<Conflit> conflitsCritiques = conflitDAOImpl.getConflitsCritiques();
            System.out.println("✅ Nombre de conflits critiques: " + conflitsCritiques.size());
            if (!conflitsCritiques.isEmpty()) {
                System.out.println("   Types critiques trouvés:");
                conflitsCritiques.forEach(c -> System.out.println("   - " + c.getType()));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test métier: " + e.getMessage());
        }
    }
    
    /**
     * Test des méthodes statistiques
     */
    private static void testStatistiques(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("4. TEST DES STATISTIQUES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // 4.1 Comptages de base
            System.out.println("4.1 Comptages:");
            int total = conflitDAOImpl.compterTousLesConflits();
            int nonResolus = conflitDAOImpl.compterConflitsNonResolus();
            System.out.println("✅ Total de conflits: " + total);
            System.out.println("✅ Conflits non résolus: " + nonResolus);
            System.out.println("✅ Conflits résolus: " + (total - nonResolus));
            
            // 4.2 Taux de résolution
            System.out.println("\n4.2 Taux de résolution:");
            double tauxResolution = conflitDAOImpl.getTauxResolution();
            System.out.printf("✅ Taux de résolution: %.2f%%\n", tauxResolution);
            
            // Affichage visuel du taux
            int barreLength = (int) (tauxResolution / 2); // 50 caractères max
            System.out.print("   [");
            for (int i = 0; i < 50; i++) {
                System.out.print(i < barreLength ? "█" : "░");
            }
            System.out.println("]");
            
            // 4.3 Comptage par type
            System.out.println("\n4.3 Comptage par type de conflit:");
            for (TypeConflit type : TypeConflit.values()) {
                int nombre = conflitDAOImpl.compterParType(type);
                if (nombre > 0) {
                    System.out.printf("   - %-30s: %d\n", type.name(), nombre);
                }
            }
            
            // 4.4 Statistiques détaillées par type
            System.out.println("\n4.4 Statistiques détaillées:");
            Map<TypeConflit, Integer> stats = conflitDAOImpl.getStatistiquesParType();
            if (!stats.isEmpty()) {
                System.out.println("✅ Répartition des conflits:");
                stats.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> {
                        double pourcentage = (entry.getValue() * 100.0) / total;
                        System.out.printf("   - %-30s: %d (%.1f%%)\n", 
                            entry.getKey().name(), entry.getValue(), pourcentage);
                    });
            } else {
                System.out.println("ℹ️ Aucune statistique disponible");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test statistiques: " + e.getMessage());
        }
    }
    
    /**
     * Test des opérations de maintenance
     */
    private static void testMaintenance(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("5. TEST DE MAINTENANCE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            // 5.1 Recherche par mot-clé
            System.out.println("5.1 Recherche par mot-clé:");
            List<Conflit> resultatsRecherche = conflitDAOImpl.rechercherParMotCle("test");
            System.out.println("✅ Résultats trouvés: " + resultatsRecherche.size());
            
            // 5.2 Suppression des anciens conflits résolus (simulation)
            System.out.println("\n5.2 Simulation de nettoyage (anciennes données):");
            LocalDateTime dateLimit = LocalDateTime.now().minusMonths(6);
            int supprimes = conflitDAOImpl.supprimerConflitsResolusAvant(dateLimit);
            System.out.println("✅ Conflits nettoyés (> 6 mois): " + supprimes);
            
            // 5.3 Information sur l'archivage
            System.out.println("\n5.3 Information sur l'archivage:");
            System.out.println("ℹ️ L'archivage nécessite une table 'conflit_archive'");
            System.out.println("   Cette opération déplace les anciens conflits vers l'archive");
            
            // 5.4 Recommandations de maintenance
            System.out.println("\n5.4 Recommandations de maintenance:");
            int totalConflits = conflitDAOImpl.compterTousLesConflits();
            if (totalConflits > 1000) {
                System.out.println("⚠️  Base volumineuse (" + totalConflits + " conflits)");
                System.out.println("   → Recommandation: Archiver les conflits de plus de 3 mois");
            } else {
                System.out.println("✅ Base de données en bon état (" + totalConflits + " conflits)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test maintenance: " + e.getMessage());
        }
    }
    
    /**
     * Afficher un résumé final
     */
    private static void afficherResume(ConflitDAOImpl conflitDAOImpl) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("RÉSUMÉ GLOBAL");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        try {
            int total = conflitDAOImpl.compterTousLesConflits();
            int actifs = conflitDAOImpl.compterConflitsNonResolus();
            double taux = conflitDAOImpl.getTauxResolution();
            
            System.out.println("📊 Statistiques globales:");
            System.out.println("   • Total de conflits: " + total);
            System.out.println("   • Conflits actifs: " + actifs);
            System.out.println("   • Conflits résolus: " + (total - actifs));
            System.out.printf("   • Taux de résolution: %.2f%%\n", taux);
            
            List<Conflit> critiques = conflitDAOImpl.getConflitsCritiques();
            if (!critiques.isEmpty()) {
                System.out.println("\n⚠️  Conflits critiques à traiter: " + critiques.size());
            } else {
                System.out.println("\n✅ Aucun conflit critique");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du résumé: " + e.getMessage());
        }
    }
}


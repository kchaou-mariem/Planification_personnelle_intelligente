import dao.impl.ActiviteDAOImpl;
import dao.impl.ContrainteDAOImpl;
import dao.impl.ConflitDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import dao.interfaces.ActiviteDAO;
import dao.interfaces.ConflitDAO;
import dao.interfaces.ContrainteDAO;
import dao.interfaces.UtilisateurDAOinterface;
import entities.*;
import service.ActiviteService;
import service.impl.ActiviteServiceImpl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principale pour tester le système de planification intelligente
 */
public class Main {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SYSTÈME DE PLANIFICATION INTELLIGENTE");
        System.out.println("========================================\n");
        
        // Initialisation des services
        ActiviteService activiteService = new ActiviteServiceImpl();
        ActiviteDAO activiteDAO = new ActiviteDAOImpl();
        ContrainteDAO contrainteDAO = new ContrainteDAOImpl();
        ConflitDAO conflitDAO = new ConflitDAOImpl();
        UtilisateurDAOinterface utilisateurDAO = new UtilisateurDAOImpl();
        
        // Créer ou récupérer un utilisateur
        Long userId = creerOuRecupererUtilisateur(utilisateurDAO);
        
        // Nettoyer les anciennes données
        System.out.println("🧹 Nettoyage des anciennes données...\n");
        nettoyerDonneesUtilisateur(userId, activiteDAO, contrainteDAO, conflitDAO);
        
        // 1. CRÉER DES CONTRAINTES
        System.out.println("📋 ÉTAPE 1: Création des contraintes horaires");
        System.out.println("──────────────────────────────────────────\n");
        List<Contrainte> contraintes = creerContraintes(userId, contrainteDAO);
        afficherContraintes(contraintes);
        
        // 2. CRÉER DES ACTIVITÉS (avec conflits intentionnels)
        System.out.println("\n📅 ÉTAPE 2: Création des activités");
        System.out.println("──────────────────────────────────────────\n");
        List<Activite> activites = creerActivites(userId, activiteService);
        afficherActivites(activites);
        
        // 3. VÉRIFIER LES CONFLITS
        System.out.println("\n⚠️  ÉTAPE 3: Détection des conflits");
        System.out.println("──────────────────────────────────────────\n");
        List<Conflit> conflits = conflitDAO.getByUtilisateur(userId);
        afficherConflits(conflits, conflitDAO, activiteDAO);
        
        // 4. CALCULER LE SCORE INITIAL
        System.out.println("\n📊 ÉTAPE 4: Évaluation du planning");
        System.out.println("──────────────────────────────────────────\n");
        double scoreInitial = activiteService.calculerScorePlanning(activites);
        System.out.println("Score initial du planning: " + String.format("%.2f", scoreInitial));
        analyserScore(scoreInitial);
        
        // 5. OPTIMISER LE PLANNING
        System.out.println("\n🚀 ÉTAPE 5: Optimisation du planning");
        System.out.println("──────────────────────────────────────────\n");
        System.out.println("Lancement de l'algorithme d'optimisation (5000 itérations)...");
        
        List<Activite> planningOptimise = activiteService.optimiserPlanning(activites, contraintes, 5000);
        double scoreOptimise = activiteService.calculerScorePlanning(planningOptimise);
        
        System.out.println("\n✅ Optimisation terminée !\n");
        System.out.println("Score initial:   " + String.format("%.2f", scoreInitial));
        System.out.println("Score optimisé:  " + String.format("%.2f", scoreOptimise));
        System.out.println("Amélioration:    " + String.format("%.2f", scoreOptimise - scoreInitial) + 
                         " points (" + String.format("%.1f%%", ((scoreOptimise - scoreInitial) / Math.abs(scoreInitial) * 100)) + ")");
        
        // 5b. SAUVEGARDER LE PLANNING OPTIMISÉ EN BASE DE DONNÉES
        System.out.println("\n💾 Sauvegarde du planning optimisé en base de données...");
        for (Activite activiteOptimisee : planningOptimise) {
            activiteService.mettreAJourActivite(activiteOptimisee);
        }
        System.out.println("✅ Planning sauvegardé !");
        
        // 5c. RECHARGER DEPUIS LA BD POUR VÉRIFIER
        System.out.println("\n🔄 Rechargement des activités depuis la base de données...");
        List<Activite> activitesDepuisBD = activiteDAO.getByUtilisateur(userId);
        System.out.println("✅ " + activitesDepuisBD.size() + " activités rechargées depuis la BD");
        
        // 6. AFFICHER LE PLANNING OPTIMISÉ (DEPUIS LA BD)
        System.out.println("\n📅 ÉTAPE 6: Planning optimisé (rechargé depuis BD)");
        System.out.println("──────────────────────────────────────────\n");
        afficherActivites(activitesDepuisBD);
        
        // 7. VALIDATION FINALE
        System.out.println("\n✓ ÉTAPE 7: Validation du planning optimisé");
        System.out.println("──────────────────────────────────────────\n");
        boolean estValide = activiteService.planningValide(activitesDepuisBD, contraintes);
        System.out.println("Le planning optimisé est " + (estValide ? "✅ VALIDE" : "❌ INVALIDE"));
        
        System.out.println("\n========================================");
        System.out.println("   TEST TERMINÉ AVEC SUCCÈS ! 🎉");
        System.out.println("========================================\n");
    }
    
    private static Long creerOuRecupererUtilisateur(UtilisateurDAOinterface utilisateurDAO) {
        // Utiliser l'utilisateur avec l'ID 1 (doit exister en base)
        System.out.println("👤 Utilisation de l'utilisateur ID=1\n");
        return 1L;
    }
    
    private static void nettoyerDonneesUtilisateur(Long userId, ActiviteDAO activiteDAO, 
                                                     ContrainteDAO contrainteDAO, ConflitDAO conflitDAO) {
        // 1. Supprimer d'abord tous les conflits (et leurs liens conflit_activite)
        List<Conflit> anciensConflits = conflitDAO.getByUtilisateur(userId);
        for (Conflit c : anciensConflits) {
            conflitDAO.supprimer(c.getidConflit());
        }
        
        // 2. Maintenant on peut supprimer les activités (plus de liens conflit_activite)
        List<Activite> anciennes = activiteDAO.getByUtilisateur(userId);
        for (Activite a : anciennes) {
            activiteDAO.supprimer(a.getIdActivite());
        }
        
        // 3. Supprimer les anciennes contraintes
        List<Contrainte> anciennesContraintes = contrainteDAO.getAllByUtilisateur(userId.intValue());
        for (Contrainte c : anciennesContraintes) {
            contrainteDAO.supprimer(c.getId());
        }
    }
    
    private static List<Contrainte> creerContraintes(Long userId, ContrainteDAO contrainteDAO) {
        List<Contrainte> contraintes = new ArrayList<>();
        
        // Contrainte 1: Sommeil (22h - 7h) tous les jours
        Contrainte sommeil = new Contrainte();
        sommeil.setTitre("Sommeil");
        sommeil.setType(TypeContrainte.Sommeil);
        sommeil.setDateHeureDeb(LocalTime.of(22, 0));
        sommeil.setDateHeureFin(LocalTime.of(23, 59));
        sommeil.setRepetitif(true);
        sommeil.setJoursSemaine(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        sommeil.setUtilisateurId(userId.intValue());
        sommeil.setStatut(StatutContrainte.ACTIVE);
        int id1 = contrainteDAO.ajouter(sommeil);
        if (id1 > 0) {
            sommeil.setId(id1);
            contraintes.add(sommeil);
        }
        
        // Contrainte 2: Repos déjeuner (12h - 13h) en semaine
        Contrainte repos = new Contrainte();
        repos.setTitre("Pause déjeuner");
        repos.setType(TypeContrainte.Repos);
        repos.setDateHeureDeb(LocalTime.of(12, 0));
        repos.setDateHeureFin(LocalTime.of(13, 0));
        repos.setRepetitif(true);
        repos.setJoursSemaine(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                      DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        repos.setUtilisateurId(userId.intValue());
        repos.setStatut(StatutContrainte.ACTIVE);
        int id2 = contrainteDAO.ajouter(repos);
        if (id2 > 0) {
            repos.setId(id2);
            contraintes.add(repos);
        }
        
        return contraintes;
    }
    
    private static List<Activite> creerActivites(Long userId, ActiviteService activiteService) {
        List<Activite> activites = new ArrayList<>();
        LocalDateTime aujourdhui = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0);
        
        // Activité 1: Réunion (priorité haute) - 9h-11h
        Activite reunion = new Activite();
        reunion.setTitre("Réunion d'équipe");
        reunion.setTypeActivite(TypeActivite.Travail);
        reunion.setPriorite(9);
        reunion.setHoraireDebut(aujourdhui);
        reunion.setHoraireFin(aujourdhui.plusHours(2));
        reunion.setDeadline(aujourdhui.plusDays(1));
        reunion.setIdUtilisateur(userId);
        Long id1 = activiteService.creerActivite(reunion);
        if (id1 > 0) {
            reunion.setIdActivite(id1);
            activites.add(reunion);
        }
        
        // Activité 2: Formation (priorité moyenne) - 10h-12h (CONFLIT avec réunion!)
        Activite formation = new Activite();
        formation.setTitre("Formation Java");
        formation.setTypeActivite(TypeActivite.Travail);
        formation.setPriorite(6);
        formation.setHoraireDebut(aujourdhui.plusHours(1));
        formation.setHoraireFin(aujourdhui.plusHours(3));
        formation.setDeadline(aujourdhui.plusDays(2));
        formation.setIdUtilisateur(userId);
        Long id2 = activiteService.creerActivite(formation);
        if (id2 > 0) {
            formation.setIdActivite(id2);
            activites.add(formation);
        }
        
        // Activité 3: Sport (priorité moyenne) - 14h-15h
        Activite sport = new Activite();
        sport.setTitre("Séance de sport");
        sport.setTypeActivite(TypeActivite.Sport);
        sport.setPriorite(5);
        sport.setHoraireDebut(aujourdhui.plusHours(5));
        sport.setHoraireFin(aujourdhui.plusHours(6));
        sport.setDeadline(aujourdhui.plusDays(1));
        sport.setIdUtilisateur(userId);
        Long id3 = activiteService.creerActivite(sport);
        if (id3 > 0) {
            sport.setIdActivite(id3);
            activites.add(sport);
        }
        
        // Activité 4: Repos (priorité haute) - 16h-17h
        Activite repos = new Activite();
        repos.setTitre("Temps de repos");
        repos.setTypeActivite(TypeActivite.Repos);
        repos.setPriorite(8);
        repos.setHoraireDebut(aujourdhui.plusHours(7));
        repos.setHoraireFin(aujourdhui.plusHours(8));
        repos.setDeadline(aujourdhui.plusDays(1));
        repos.setIdUtilisateur(userId);
        Long id4 = activiteService.creerActivite(repos);
        if (id4 > 0) {
            repos.setIdActivite(id4);
            activites.add(repos);
        }
        
        // Activité 5: Travail tardif (CONFLIT avec contrainte sommeil!) - 22h30-23h30
        Activite travailTardif = new Activite();
        travailTardif.setTitre("Travail urgent");
        travailTardif.setTypeActivite(TypeActivite.Travail);
        travailTardif.setPriorite(7);
        travailTardif.setHoraireDebut(aujourdhui.plusHours(13).plusMinutes(30));
        travailTardif.setHoraireFin(aujourdhui.plusHours(14).plusMinutes(30));
        travailTardif.setDeadline(aujourdhui.plusDays(1));
        travailTardif.setIdUtilisateur(userId);
        Long id5 = activiteService.creerActivite(travailTardif);
        if (id5 > 0) {
            travailTardif.setIdActivite(id5);
            activites.add(travailTardif);
        }
        
        return activites;
    }
    
    private static void afficherContraintes(List<Contrainte> contraintes) {
        for (Contrainte c : contraintes) {
            System.out.println("✓ " + c.getTitre() + " (" + c.getType() + ")");
            System.out.println("  Horaire: " + c.getDateHeureDeb() + " - " + c.getDateHeureFin());
            if (c.isRepetitif() && c.getJoursSemaine() != null) {
                System.out.println("  Jours: " + c.getJoursSemaine());
            }
        }
    }
    
    private static void afficherActivites(List<Activite> activites) {
        for (Activite a : activites) {
            String icone = switch (a.getTypeActivite()) {
                case Travail -> "💼";
                case Sport -> "🏃";
                case Repos -> "😴";
                default -> "📌";
            };
            
            System.out.println(icone + " " + a.getTitre());
            System.out.println("  Type: " + a.getTypeActivite() + " | Priorité: " + a.getPriorite() + "/10");
            System.out.println("  Horaire: " + a.getHoraireDebut().format(formatter) + " - " + 
                             a.getHoraireFin().format(formatter));
            System.out.println("  Deadline: " + a.getDeadline().format(formatter));
            System.out.println();
        }
    }
    
    private static void afficherConflits(List<Conflit> conflits, ConflitDAO conflitDAO, ActiviteDAO activiteDAO) {
        if (conflits.isEmpty()) {
            System.out.println("✅ Aucun conflit détecté !");
            return;
        }
        
        System.out.println("⚠️  " + conflits.size() + " conflit(s) détecté(s) :\n");
        
        for (Conflit c : conflits) {
            System.out.println("  • Type: " + c.getType());
            System.out.println("    Date détection: " + c.getHoraireDetection().format(formatter));
            System.out.println("    Statut: " + (c.isResolu() ? "Résolu" : "Non résolu"));
            
            List<Long> activitesLiees = conflitDAO.getActivitesLieesAuConflit(c.getidConflit());
            if (!activitesLiees.isEmpty()) {
                System.out.println("    Activités concernées:");
                for (Long idAct : activitesLiees) {
                    activiteDAO.getById(idAct).ifPresent(act -> 
                        System.out.println("      - " + act.getTitre())
                    );
                }
            }
            System.out.println();
        }
    }
    
    private static void analyserScore(double score) {
        if (score > 100) {
            System.out.println("➜ Excellent planning ! 🌟");
        } else if (score > 50) {
            System.out.println("➜ Planning correct, améliorations possibles 👍");
        } else if (score > 0) {
            System.out.println("➜ Planning médiocre, optimisation recommandée ⚠️");
        } else {
            System.out.println("➜ Planning problématique, optimisation nécessaire ❌");
        }
    }
}

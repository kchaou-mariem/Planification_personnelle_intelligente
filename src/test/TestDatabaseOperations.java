package test;

import java.sql.Connection;
import java.sql.SQLException;


import config.Connect;

/**
 * Classe de test avancée pour tester les opérations sur la base de données
 * avec les entités du projet Personal Planner
 */
public class TestDatabaseOperations {

    public static void main(String[] args) {
        System.out.println("=== Test des opérations sur la base de données ===");
        
        // Test de connexion de base
        if (!testBasicConnection()) {
            System.out.println("❌ Connexion échouée - Arrêt des tests");
            return;
        }
        
        
    }
    
    /**
     * Test de connexion basique
     */
    private static boolean testBasicConnection() {
        System.out.println("\n1. Test de connexion:");
        System.out.println("   URL: jdbc:mysql://localhost:3306/personal_planner");
        System.out.println("   USER: root");
        
        try {
            Connection conn = Connect.getConnection();
			if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion active");
                conn.close();
                return true;
            } else {
                System.out.println("❌ Connexion inactive");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL: " + e.getMessage());
            System.out.println("   Code erreur: " + e.getErrorCode());
            System.out.println("   SQL State: " + e.getSQLState());
            
            // Messages d'aide selon l'erreur
            if (e.getMessage().contains("Communications link failure")) {
                System.out.println("\n💡 Solution: MySQL n'est pas démarré ou n'écoute pas sur le port 3306");
                System.out.println("   Vérifiez: Get-Service | Where-Object {$_.Name -like '*mysql*'}");
            } else if (e.getMessage().contains("Unknown database")) {
                System.out.println("\n💡 Solution: La base 'personal_planner' n'existe pas");
                System.out.println("   Exécutez: CREATE DATABASE personal_planner;");
            } else if (e.getMessage().contains("Access denied")) {
                System.out.println("\n💡 Solution: Mauvais user/password");
                System.out.println("   Vérifiez vos credentials MySQL");
            } else if (e.getMessage().contains("Driver")) {
                System.out.println("\n💡 Solution: Driver MySQL manquant");
                System.out.println("   Téléchargez mysql-connector-j.jar et ajoutez-le au classpath");
            }
            
            return false;
        }
    }

}

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
        try {
            Connection conn = Connect.getConnection();
			if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion active");
                return true;
            } else {
                System.out.println("❌ Connexion inactive");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur de connexion: " + e.getMessage());
            return false;
        }
    }

}
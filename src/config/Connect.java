package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
	private static final String URL = "jdbc:mysql://localhost:3306/personal_planner";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        // Retourne directement la connexion
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }	
}

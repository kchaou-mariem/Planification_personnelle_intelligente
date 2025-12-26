package dao.impl;

import dao.interfaces.UtilisateurDAOinterface;
import entities.Utilisateur;
import config.Connect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAOImpl implements UtilisateurDAOinterface {
    
    @Override
    public boolean inserer(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, age, genre, poste, mot_de_passe_hash, salt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setInt(4, utilisateur.getAge());
            stmt.setString(5, utilisateur.getGenre());
            stmt.setString(6, utilisateur.getPoste());
            stmt.setString(7, utilisateur.getMotdepasse());
            
            if (utilisateur.getMotdepasse() != null) {
                utilisateur.setMotdepasse(utilisateur.getMotdepasse());
                stmt.setString(7, utilisateur.getMotdepasse());
                stmt.setString(8, getSalt(utilisateur));
            } else {
                stmt.setString(7, null);
                stmt.setString(8, null);
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur inséré avec succès");
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion: " + e.getMessage());
            return false;
        }
    }
    @Override
    public boolean modifier(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, age=?, genre=?, poste=?, mot_de_passe_hash=?, salt=? WHERE id_utilisateur=?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getPrenom());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setInt(4, utilisateur.getAge());
            stmt.setString(5, utilisateur.getGenre());
            stmt.setString(6, utilisateur.getPoste());
            stmt.setString(7, utilisateur.getMotdepasse());
            stmt.setString(8, getSalt(utilisateur));
            stmt.setInt(9, utilisateur.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur modifié avec succès");
                return true;
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'ID: " + utilisateur.getId());
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur=?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur supprimé avec succès (ID: " + id + ")");
                return true;
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'ID: " + id);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur=?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'ID: " + id);
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Utilisateur> getAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
            
            System.out.println(utilisateurs.size() + " utilisateur(s) récupéré(s)");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les utilisateurs: " + e.getMessage());
        }
        
        return utilisateurs;
    }
    
    @Override
    public Utilisateur getByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email=?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            } else {
                System.out.println("Aucun utilisateur trouvé avec l'email: " + email);
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par email: " + e.getMessage());
            return null;
        }
    }
    /*Au lieu de réecrire ce code dans grtbyid(), getall(), getbyemail() on l'écrit une seule fois ici */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur(
            rs.getInt("id_utilisateur"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getInt("age"),
            rs.getString("genre"),
            rs.getString("poste"),
            rs.getString("mot_de_passe_hash")
        );
        // Récupérer le salt depuis la base de données
        utilisateur.setSalt(rs.getString("salt"));
        return utilisateur;
    }
    private String getSalt(Utilisateur utilisateur) {
        return utilisateur.getSalt();
    }
}  

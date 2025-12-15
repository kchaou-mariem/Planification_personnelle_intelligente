package dao.impl;

import Entities.Conflit;
import Entities.TypeConflit;
import config.Connect;
import dao.interfaces.ConflitDAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implémentation DAO pour la gestion des conflits
 * Gère toutes les opérations de base de données pour les conflits
 */
public class ConflitDAOImpl implements ConflitDAO {

    // ========== OPÉRATIONS CRUD DE BASE ==========

    @Override
    public Long ajouter(Conflit conflit) {
        String sql = "INSERT INTO conflit (horaire_detection, type_conflit, resolu) VALUES (?, ?, ?)";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(conflit.getHoraireDetection()));
            stmt.setString(2, convertirTypeConflitJava(conflit.getType()));
            stmt.setInt(3, conflit.isResolu() ? 1 : 0); 
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du conflit: " + e.getMessage());
            e.printStackTrace();
        }
        return -1L;
    }

    @Override
    public boolean modifier(Conflit conflit) {
        String sql = "UPDATE conflit SET horaire_detection = ?, type_conflit = ?, resolu = ? WHERE id_conflit = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(conflit.getHoraireDetection()));
            stmt.setString(2, convertirTypeConflitJava(conflit.getType()));
            stmt.setInt(3, conflit.isResolu() ? 1 : 0); // boolean vers tinyint(1)
            stmt.setLong(4, conflit.getidConflit());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du conflit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(Long idConflit) {
        String sql = "DELETE FROM conflit WHERE id_conflit = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idConflit);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du conflit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Conflit> getById(Long idConflit) {
        String sql = "SELECT * FROM conflit WHERE id_conflit = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idConflit);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du conflit: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Conflit> getAll() {
        String sql = "SELECT * FROM conflit ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les conflits: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    // ========== RECHERCHE ET FILTRAGE ==========

    @Override
    public List<Conflit> getByType(TypeConflit type) {
        String sql = "SELECT * FROM conflit WHERE type_conflit = ? ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, convertirTypeConflitJava(type));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits par type: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getConflitsNonResolus() {
        String sql = "SELECT * FROM conflit WHERE resolu = 0 ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits non résolus: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getConflitsResolus() {
        String sql = "SELECT * FROM conflit WHERE resolu = 1 ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits résolus: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT * FROM conflit WHERE horaire_detection BETWEEN ? AND ? ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, dateDebut);
            stmt.setObject(2, dateFin);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits par période: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getByActivite(Long idActivite) {
        // Note: Cette requête nécessite une table de liaison conflit_activite ou un champ id_activite dans conflit
        String sql = "SELECT c.* FROM conflit c " +
                    "INNER JOIN conflit_activite ca ON c.id_conflit = ca.id_conflit " +
                    "WHERE ca.id_activite = ? ORDER BY c.horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idActivite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits par activité: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getByUtilisateur(Long idUtilisateur) {
        // Requête qui joint avec les activités de l'utilisateur
        String sql = "SELECT DISTINCT c.* FROM conflit c " +
                    "INNER JOIN conflit_activite ca ON c.id_conflit = ca.id_conflit " +
                    "INNER JOIN activite a ON ca.id_activite = a.id_activite " +
                    "WHERE a.id_utilisateur = ? ORDER BY c.horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits par utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    // ========== OPÉRATIONS MÉTIER ==========

    @Override
    public boolean marquerCommeResolu(Long idConflit) {
        String sql = "UPDATE conflit SET resolu = 1 WHERE id_conflit = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idConflit);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage du conflit comme résolu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int marquerPlusieursCommeResolus(List<Long> idsConflits) {
        if (idsConflits == null || idsConflits.isEmpty()) {
            return 0;
        }
        
        String placeholders = String.join(",", Collections.nCopies(idsConflits.size(), "?"));
        String sql = "UPDATE conflit SET resolu = 1 WHERE id_conflit IN (" + placeholders + ")";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < idsConflits.size(); i++) {
                stmt.setLong(i + 1, idsConflits.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de plusieurs conflits comme résolus: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Conflit> detecterChevauchements(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        String sql = "SELECT c.* FROM conflit c " +
                    "INNER JOIN conflit_activite ca ON c.id_conflit = ca.id_conflit " +
                    "WHERE ca.id_activite = ? AND c.type_conflit = ? " +
                    "AND c.horaire_detection BETWEEN ? AND ?";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idActivite);
            stmt.setString(2, convertirTypeConflitJava(TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES));
            stmt.setTimestamp(3, Timestamp.valueOf(horaireDebut));
            stmt.setTimestamp(4, Timestamp.valueOf(horaireFin));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la détection des chevauchements: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    // ========== STATISTIQUES ==========

    @Override
    public int compterTousLesConflits() {
        String sql = "SELECT COUNT(*) FROM conflit";
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des conflits: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int compterConflitsNonResolus() {
        String sql = "SELECT COUNT(*) FROM conflit WHERE resolu = 0";
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des conflits non résolus: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int compterParType(TypeConflit type) {
        String sql = "SELECT COUNT(*) FROM conflit WHERE type_conflit = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, convertirTypeConflitJava(type));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des conflits par type: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int compterParUtilisateur(Long idUtilisateur) {
        String sql = "SELECT COUNT(DISTINCT c.id_conflit) FROM conflit c " +
                    "INNER JOIN conflit_activite ca ON c.id_conflit = ca.id_conflit " +
                    "INNER JOIN activite a ON ca.id_activite = a.id_activite " +
                    "WHERE a.id_utilisateur = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des conflits par utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getTauxResolution() {
        String sql = "SELECT " +
                    "COUNT(CASE WHEN resolu = 1 THEN 1 END) * 100.0 / COUNT(*) as taux " +
                    "FROM conflit";
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("taux");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du taux de résolution: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public Map<TypeConflit, Integer> getStatistiquesParType() {
        String sql = "SELECT type_conflit, COUNT(*) as nombre FROM conflit GROUP BY type_conflit";
        Map<TypeConflit, Integer> stats = new HashMap<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String typeBD = rs.getString("type_conflit");
                TypeConflit type = convertirTypeConflitBD(typeBD);
                int nombre = rs.getInt("nombre");
                stats.put(type, nombre);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques par type: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    // ========== OPÉRATIONS DE MAINTENANCE ==========

    @Override
    public int supprimerConflitsResolusAvant(LocalDateTime dateAvant) {
        String sql = "DELETE FROM conflit WHERE resolu = 1 AND horaire_detection < ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dateAvant));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des conflits résolus: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int archiverConflitsAvant(LocalDateTime dateAvant) {
        // Cette méthode nécessiterait une table d'archivage
        String sqlInsert = "INSERT INTO conflit_archive SELECT * FROM conflit WHERE horaire_detection < ?";
        String sqlDelete = "DELETE FROM conflit WHERE horaire_detection < ?";
        
        try (Connection conn = Connect.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);
                 PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                
                stmtInsert.setObject(1, dateAvant);
                stmtInsert.executeUpdate();
                
                stmtDelete.setObject(1, dateAvant);
                int archived = stmtDelete.executeUpdate();
                
                conn.commit();
                return archived;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'archivage des conflits: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Conflit> rechercherParMotCle(String motCle) {
        // Recherche dans les activités liées
        String sql = "SELECT DISTINCT c.* FROM conflit c " +
                    "INNER JOIN conflit_activite ca ON c.id_conflit = ca.id_conflit " +
                    "INNER JOIN activite a ON ca.id_activite = a.id_activite " +
                    "WHERE a.titre LIKE ? OR a.description LIKE ? " +
                    "ORDER BY c.horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + motCle + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par mot-clé: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getConflitsRecents(int limite) {
        String sql = "SELECT * FROM conflit ORDER BY horaire_detection DESC LIMIT ?";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits récents: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    @Override
    public List<Conflit> getConflitsCritiques() {
        // Conflits non résolus de types critiques
        String sql = "SELECT * FROM conflit " +
                    "WHERE resolu = 0 " +
                    "AND type_conflit IN (?, ?, ?) " +
                    "ORDER BY horaire_detection DESC";
        List<Conflit> conflits = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, convertirTypeConflitJava(TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES));
            stmt.setString(2, convertirTypeConflitJava(TypeConflit.DEADLINE));
            stmt.setString(3, convertirTypeConflitJava(TypeConflit.VIOLATION_DE_CONTRAINTE));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                conflits.add(mapResultSetToConflit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conflits critiques: " + e.getMessage());
            e.printStackTrace();
        }
        return conflits;
    }

    // ========== MÉTHODE UTILITAIRE ==========

    /**
     * Mapper un ResultSet vers un objet Conflit
     * Convertit les valeurs d'enum de la BD (français avec espaces) vers les constantes Java
     */
    private Conflit mapResultSetToConflit(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_conflit");
        LocalDateTime horaireDetection = rs.getTimestamp("horaire_detection").toLocalDateTime();
        String typeConflit = rs.getString("type_conflit");
        boolean resolu = rs.getInt("resolu") == 1; // tinyint(1) converti en boolean
        
        // Convertir le type de conflit de la BD vers l'enum Java
        TypeConflit type = convertirTypeConflitBD(typeConflit);
        
        return new Conflit(id, horaireDetection, type, resolu);
    }
    
    /**
     * Convertit les valeurs d'enum de la BD (français) vers les constantes Java (anglais)
     */
    private TypeConflit convertirTypeConflitBD(String typeBD) {
        switch (typeBD) {
            case "Chevauchement des activités":
                return TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES;
            case "Violation de contrainte":
                return TypeConflit.VIOLATION_DE_CONTRAINTE;
            case "Fatigue Excessive":
                return TypeConflit.FATIGUE_EXCESSIVE;
            case "Deadline":
                return TypeConflit.DEADLINE;
            case "Équilibre Faible":
                return TypeConflit.EQUILIBRE_FAIBLE;
            case "Repos Insuffisant":
                return TypeConflit.REPOS_INSUFFISANT;
            default:
                throw new IllegalArgumentException("Type de conflit inconnu: " + typeBD);
        }
    }
    
    /**
     * Convertit les constantes Java vers les valeurs d'enum de la BD (français)
     */
    private String convertirTypeConflitJava(TypeConflit type) {
        switch (type) {
            case CHEVAUCHEMENT_DES_ACTIVITES:
                return "Chevauchement des activités";
            case VIOLATION_DE_CONTRAINTE:
                return "Violation de contrainte";
            case FATIGUE_EXCESSIVE:
                return "Fatigue Excessive";
            case DEADLINE:
                return "Deadline";
            case EQUILIBRE_FAIBLE:
                return "Équilibre Faible";
            case REPOS_INSUFFISANT:
                return "Repos Insuffisant";
            default:
                throw new IllegalArgumentException("Type de conflit inconnu: " + type);
        }
    }
}

package dao.impl;

import config.Connect;
import dao.interfaces.ActiviteDAO;
import entities.Activite;
import entities.TypeActivite;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implémentation DAO pour la gestion des activités
 * Gère toutes les opérations de base de données pour les activités
 */
public class ActiviteDAOImpl implements ActiviteDAO {

    // ========== OPÉRATIONS CRUD DE BASE ==========

    @Override
    public Long ajouter(Activite activite) {
        String sql = "INSERT INTO activite (titre, type_activite, description, priorite, deadline, " +
                    "horaire_debut, horaire_fin, id_utilisateur) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, activite.getTitre());
            stmt.setString(2, convertirTypeActiviteJava(activite.getTypeActivite()));
            stmt.setString(3, activite.getDescription());
            if (activite.getPriorite() != null) {
                stmt.setInt(4, activite.getPriorite());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setTimestamp(5, activite.getDeadline() != null ? Timestamp.valueOf(activite.getDeadline()) : null);
            stmt.setTimestamp(6, activite.getHoraireDebut() != null ? Timestamp.valueOf(activite.getHoraireDebut()) : null);
            stmt.setTimestamp(7, activite.getHoraireFin() != null ? Timestamp.valueOf(activite.getHoraireFin()) : null);
            stmt.setLong(8, activite.getIdUtilisateur());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'activité: " + e.getMessage());
            e.printStackTrace();
        }
        return -1L;
    }

    @Override
    public boolean modifier(Activite activite) {
        String sql = "UPDATE activite SET titre = ?, type_activite = ?, description = ?, priorite = ?, " +
                    "deadline = ?, horaire_debut = ?, horaire_fin = ? " +
                    "WHERE id_activite = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, activite.getTitre());
            stmt.setString(2, convertirTypeActiviteJava(activite.getTypeActivite()));
            stmt.setString(3, activite.getDescription());
            if (activite.getPriorite() != null) {
                stmt.setInt(4, activite.getPriorite());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setTimestamp(5, activite.getDeadline() != null ? Timestamp.valueOf(activite.getDeadline()) : null);
            stmt.setTimestamp(6, activite.getHoraireDebut() != null ? Timestamp.valueOf(activite.getHoraireDebut()) : null);
            stmt.setTimestamp(7, activite.getHoraireFin() != null ? Timestamp.valueOf(activite.getHoraireFin()) : null);
            stmt.setLong(8, activite.getIdActivite());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'activité: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean supprimer(Long idActivite) {
        String sql = "DELETE FROM activite WHERE id_activite = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idActivite);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'activité: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Activite> getById(Long idActivite) {
        String sql = "SELECT * FROM activite WHERE id_activite = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idActivite);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'activité: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Activite> getAll() {
        String sql = "SELECT * FROM activite ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de toutes les activités: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    // ========== RECHERCHE ET FILTRAGE ==========

    @Override
    public List<Activite> getByUtilisateur(Long idUtilisateur) {
        String sql = "SELECT * FROM activite WHERE id_utilisateur = ? ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getByType(TypeActivite type) {
        String sql = "SELECT * FROM activite WHERE type_activite = ? ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, convertirTypeActiviteJava(type));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités par type: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getByTypeAndUtilisateur(Long idUtilisateur, TypeActivite type) {
        String sql = "SELECT * FROM activite WHERE id_utilisateur = ? AND type_activite = ? " +
                    "ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            stmt.setString(2, convertirTypeActiviteJava(type));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT * FROM activite WHERE horaire_debut BETWEEN ? AND ? ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités par période: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getByUtilisateurAndPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT * FROM activite WHERE id_utilisateur = ? AND horaire_debut BETWEEN ? AND ? " +
                    "ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            stmt.setTimestamp(2, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(3, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getActivitesAvecDeadlineProche(int joursAvance) {
        String sql = "SELECT * FROM activite WHERE deadline BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? DAY) " +
                    "ORDER BY deadline ASC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, joursAvance);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités avec deadline proche: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getByPriorite(int priorite) {
        String sql = "SELECT * FROM activite WHERE priorite = ? ORDER BY deadline ASC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, priorite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités par priorité: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> rechercherParMotCle(String motCle) {
        String sql = "SELECT * FROM activite WHERE titre LIKE ? OR description LIKE ? ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + motCle + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par mot-clé: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> rechercherParMotCleUtilisateur(Long idUtilisateur, String motCle) {
        String sql = "SELECT * FROM activite WHERE id_utilisateur = ? AND (titre LIKE ? OR description LIKE ?) " +
                    "ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + motCle + "%";
            stmt.setLong(1, idUtilisateur);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par mot-clé: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    // ========== OPÉRATIONS MÉTIER ==========

    @Override
    public boolean hasChevauchement(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        String sql = "SELECT COUNT(*) FROM activite WHERE id_activite != ? AND " +
                    "horaire_debut < ? AND horaire_fin > ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idActivite);
            stmt.setTimestamp(2, Timestamp.valueOf(horaireFin));
            stmt.setTimestamp(3, Timestamp.valueOf(horaireDebut));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du chevauchement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Activite> getActivitesChevauchantes(LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        String sql = "SELECT * FROM activite WHERE horaire_debut < ? AND horaire_fin > ? " +
                    "ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(horaireFin));
            stmt.setTimestamp(2, Timestamp.valueOf(horaireDebut));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités chevauchantes: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getActivitesChevauchantesUtilisateur(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin) {
        String sql = "SELECT * FROM activite WHERE id_utilisateur = ? AND horaire_debut < ? AND horaire_fin > ? " +
                    "ORDER BY horaire_debut DESC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            stmt.setTimestamp(2, Timestamp.valueOf(horaireFin));
            stmt.setTimestamp(3, Timestamp.valueOf(horaireDebut));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités chevauchantes: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    // ========== STATISTIQUES ==========

    @Override
    public int compterToutesLesActivites() {
        String sql = "SELECT COUNT(*) FROM activite";
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des activités: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int compterActivitesUtilisateur(Long idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM activite WHERE id_utilisateur = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des activités: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int compterParType(TypeActivite type) {
        String sql = "SELECT COUNT(*) FROM activite WHERE type_activite = ?";
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, convertirTypeActiviteJava(type));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des activités par type: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Activite> getActivitesRecentes(int limite) {
        String sql = "SELECT * FROM activite ORDER BY horaire_debut DESC LIMIT ?";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités récentes: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    @Override
    public List<Activite> getActivitesHautePriorite() {
        String sql = "SELECT * FROM activite WHERE priorite >= 8 ORDER BY priorite DESC, deadline ASC";
        List<Activite> activites = new ArrayList<>();
        
        try (Connection conn = Connect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                activites.add(mapResultSetToActivite(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des activités haute priorité: " + e.getMessage());
            e.printStackTrace();
        }
        return activites;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Mapper un ResultSet vers un objet Activite
     */
    private Activite mapResultSetToActivite(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_activite");
        String titre = rs.getString("titre");
        String typeActivite = rs.getString("type_activite");
        String description = rs.getString("description");
        Integer priorite = (Integer) rs.getObject("priorite");
        Timestamp deadlineTs = rs.getTimestamp("deadline");
        LocalDateTime deadline = deadlineTs != null ? deadlineTs.toLocalDateTime() : null;
        Timestamp horaireDebutTs = rs.getTimestamp("horaire_debut");
        LocalDateTime horaireDebut = horaireDebutTs != null ? horaireDebutTs.toLocalDateTime() : null;
        Timestamp horaireFinTs = rs.getTimestamp("horaire_fin");
        LocalDateTime horaireFin = horaireFinTs != null ? horaireFinTs.toLocalDateTime() : null;
        Long idUtilisateur = rs.getLong("id_utilisateur");
        Timestamp dateCreationTs = rs.getTimestamp("date_creation");
        LocalDateTime dateCreation = dateCreationTs != null ? dateCreationTs.toLocalDateTime() : null;
        
        // Convertir le type d'activité
        TypeActivite type = convertirTypeActiviteBD(typeActivite);
        
        return new Activite(id, titre, type, description, priorite, deadline, 
                           horaireDebut, horaireFin, idUtilisateur, dateCreation);
    }
    
    /**
     * Convertit les valeurs d'enum de la BD vers les constantes Java
     */
    private TypeActivite convertirTypeActiviteBD(String typeBD) {
        switch (typeBD) {
            case "Sport":
                return TypeActivite.Sport;
            case "Étude":
            case "Etude":
                return TypeActivite.Etude;
            case "Loisirs":
                return TypeActivite.Loisirs;
            case "Repos":
                return TypeActivite.Repos;
            case "Travail":
                return TypeActivite.Travail;
            default:
                throw new IllegalArgumentException("Type d'activité inconnu: " + typeBD);
        }
    }
    
    /**
     * Convertit les constantes Java vers les valeurs d'enum de la BD
     */
    private String convertirTypeActiviteJava(TypeActivite type) {
        switch (type) {
            case Sport:
                return "Sport";
            case Etude:
                return "Étude";
            case Loisirs:
                return "Loisirs";
            case Repos:
                return "Repos";
            case Travail:
                return "Travail";
            default:
                throw new IllegalArgumentException("Type d'activité inconnu: " + type);
        }
    }
}

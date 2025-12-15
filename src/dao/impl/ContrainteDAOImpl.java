package dao.impl;

import config.Connect;
import dao.interfaces.ContrainteDAO;
import Entities.Contrainte;
import Entities.StatutContrainte;
import Entities.TypeContrainte;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContrainteDAOImpl implements ContrainteDAO {

    @Override
    public Long ajouter(Contrainte contrainte) {
        String sql = "INSERT INTO contrainte (titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contrainte.getTitre());
            ps.setString(2, contrainte.getType() != null ? contrainte.getType().name() : null);
            LocalTime hd = contrainte.getDateHeureDeb();
            LocalTime hf = contrainte.getDateHeureFin();
            if (hd != null) ps.setTime(3, Time.valueOf(hd)); else ps.setNull(3, Types.TIME);
            if (hf != null) ps.setTime(4, Time.valueOf(hf)); else ps.setNull(4, Types.TIME);
            ps.setInt(5, contrainte.isRepetitif() ? 1 : 0);

            ps.setString(6, datesToJson(contrainte.getDatesSpecifiques()));
            ps.setString(7, joursToJson(contrainte.getJoursSemaine()));
            ps.setString(8, contrainte.getStatut() != null ? contrainte.getStatut().name() : "ACTIVE");

            int aff = ps.executeUpdate();
            if (aff > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        long id = gk.getLong(1);
                        return id;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1L;
    }

    @Override
    public boolean modifier(Contrainte contrainte) {
        String sql = "UPDATE contrainte SET titre = ?, type_contrainte = ?, heure_debut = ?, heure_fin = ?, repetitif = ?, dates_specifiques = ?, jours = ?, statut = ? WHERE id_contrainte = ?";
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contrainte.getTitre());
            ps.setString(2, contrainte.getType() != null ? contrainte.getType().name() : null);
            LocalTime hd = contrainte.getDateHeureDeb();
            LocalTime hf = contrainte.getDateHeureFin();
            if (hd != null) ps.setTime(3, Time.valueOf(hd)); else ps.setNull(3, Types.TIME);
            if (hf != null) ps.setTime(4, Time.valueOf(hf)); else ps.setNull(4, Types.TIME);
            ps.setInt(5, contrainte.isRepetitif() ? 1 : 0);

            ps.setString(6, datesToJson(contrainte.getDatesSpecifiques()));
            ps.setString(7, joursToJson(contrainte.getJoursSemaine()));
            ps.setString(8, contrainte.getStatut() != null ? contrainte.getStatut().name() : "ACTIVE");

            ps.setLong(9, contrainte.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean supprimer(Long idContrainte) {
        String sql = "DELETE FROM contrainte WHERE id_contrainte = ?";
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrainte);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public Optional<Contrainte> getById(Long idContrainte) {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte WHERE id_contrainte = ?";
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrainte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrainte c = mapRow(rs);
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Contrainte> getAll() {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte ORDER BY id_contrainte DESC";
        List<Contrainte> list = new ArrayList<>();
        try (Connection conn = Connect.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Contrainte c = mapRow(rs);
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Contrainte> getByPeriode(LocalTime heureDebut, LocalTime heureFin) {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte WHERE heure_debut >= ? AND heure_fin <= ?";
        List<Contrainte> list = new ArrayList<>();
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(heureDebut));
            ps.setTime(2, Time.valueOf(heureFin));
            try (ResultSet rs = ps.executeQuery()) { 
                while (rs.next()) {
                    Contrainte c = mapRow(rs);
                    list.add(c);
                } 
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Contrainte> getRepetitives() {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte WHERE repetitif = 1";
        List<Contrainte> list = new ArrayList<>();
        try (Connection conn = Connect.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Contrainte c = mapRow(rs);
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Contrainte> getNonRepetitives() {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte WHERE repetitif = 0";
        List<Contrainte> list = new ArrayList<>();
        try (Connection conn = Connect.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Contrainte c = mapRow(rs);
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public int compterToutesLesContraintes() {
        String sql = "SELECT COUNT(*) FROM contrainte";
        try (Connection conn = Connect.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public List<Contrainte> getContraintesByStatut(StatutContrainte statut) {
        String sql = "SELECT id_contrainte, titre, type_contrainte, heure_debut, heure_fin, repetitif, dates_specifiques, jours, statut FROM contrainte WHERE statut = ? ORDER BY id_contrainte DESC";
        List<Contrainte> list = new ArrayList<>();
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut != null ? statut.name() : "ACTIVE");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contrainte c = mapRow(rs);
                    list.add(c);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Contrainte> getContraintesActives() {
        return getContraintesByStatut(StatutContrainte.ACTIVE);
    }

    @Override
    public List<Contrainte> getContraintesDesactives() {
        return getContraintesByStatut(StatutContrainte.DESACTIVE);
    }

    @Override
    public int compterContraintesByStatut(StatutContrainte statut) {
        String sql = "SELECT COUNT(*) FROM contrainte WHERE statut = ?";
        try (Connection conn = Connect.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut != null ? statut.name() : "ACTIVE");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ---------- Helpers ----------
    private Contrainte mapRow(ResultSet rs) throws SQLException {
        Contrainte c = new Contrainte();
        c.setId(rs.getLong("id_contrainte"));
        c.setTitre(rs.getString("titre"));
        String type = rs.getString("type_contrainte");
        if (type != null && !type.trim().isEmpty()) {
            try { 
                c.setType(TypeContrainte.valueOf(type)); 
            } catch (IllegalArgumentException e) { 
                System.err.println("Invalid TypeContrainte value: " + type);
            }
        }
        Time tDeb = rs.getTime("heure_debut");
        Time tFin = rs.getTime("heure_fin");
        if (tDeb != null) c.setDateHeureDeb(tDeb.toLocalTime());
        if (tFin != null) c.setDateHeureFin(tFin.toLocalTime());
        c.setRepetitif(rs.getBoolean("repetitif"));

        String datesJson = rs.getString("dates_specifiques");
        c.setDatesSpecifiques(parseDatesFromJson(datesJson));
        String joursJson = rs.getString("jours");
        c.setJoursSemaine(parseJoursFromJson(joursJson));
        
        String statut = rs.getString("statut");
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                c.setStatut(StatutContrainte.valueOf(statut));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid StatutContrainte value: " + statut);
                c.setStatut(StatutContrainte.ACTIVE); // default
            }
        }

        return c;
    }

    // ---------- JSON helpers (dates_specifiques / jours) ----------
    private String datesToJson(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) return null;
        return "[" + dates.stream().map(d -> "\"" + d.toString() + "\"").collect(Collectors.joining(",")) + "]";
    }

    private List<LocalDate> parseDatesFromJson(String json) {
        List<LocalDate> res = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return res;
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String val = m.group(1);
            try { res.add(LocalDate.parse(val)); } catch (Exception e) { /* ignore invalid */ }
        }
        return res;
    }

    private String joursToJson(List<java.time.DayOfWeek> jours) {
        if (jours == null || jours.isEmpty()) return null;
        return "[" + jours.stream().map(j -> "\"" + j.name() + "\"").collect(Collectors.joining(",")) + "]";
    }

    private List<java.time.DayOfWeek> parseJoursFromJson(String json) {
        List<java.time.DayOfWeek> res = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return res;
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String val = m.group(1);
            try { res.add(java.time.DayOfWeek.valueOf(val)); } catch (Exception e) { /* ignore invalid */ }
        }
        return res;
    }
}

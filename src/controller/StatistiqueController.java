package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import service.StatistiqueService;
import service.impl.StatistiqueServiceImpl;
import util.GsonConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controller REST pour les statistiques utilisateur.
 */
@WebServlet(urlPatterns = { "/api/statistiques/*" })
public class StatistiqueController extends HttpServlet {

    private final StatistiqueService statistiqueService;
    private final Gson gson;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StatistiqueController() {
        this.statistiqueService = new StatistiqueServiceImpl();
        this.gson = GsonConfig.createGson();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            System.out.println("📊 StatistiqueController - PathInfo: " + pathInfo);
            
            if (pathInfo == null) {
                sendError(resp, out, 400, "PathInfo null");
                return;
            }

            // Supprimer le / initial et découper
            String path = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            String[] parts = path.split("/");
            
            System.out.println("📊 Parts: " + String.join(", ", parts));
            System.out.println("📊 Parts length: " + parts.length);

            // Format attendu: utilisateur/{userId} ou utilisateur/{userId}/{endpoint}
            if (parts.length < 2 || !"utilisateur".equals(parts[0])) {
                sendError(resp, out, 400, "Format attendu: /utilisateur/{userId}/[endpoint]");
                return;
            }

            Long userId = Long.parseLong(parts[1]);
            System.out.println("📊 User ID: " + userId);

            // Si pas d'endpoint spécifique, retourner stats de base
            if (parts.length == 2) {
                JsonObject stats = new JsonObject();
                stats.addProperty("nombreActivites", statistiqueService.getNombreActivites(userId));
                stats.addProperty("heuresTotal", statistiqueService.getHeuresPlannifieesTotal(userId));
                stats.addProperty("moyenneParJour", statistiqueService.getMoyenneActivitesParJour(userId));
                out.print(gson.toJson(stats));
                return;
            }

            String endpoint = parts[2];
            System.out.println("📊 Endpoint: " + endpoint);

            switch (endpoint) {
                case "rapport":
                    System.out.println("📊 Génération rapport complet pour user " + userId);
                    StatistiqueService.RapportStatistique rapport = statistiqueService.getRapportComplet(userId);
                    String json = gson.toJson(rapport);
                    System.out.println("📊 Rapport JSON length: " + json.length());
                    out.print(json);
                    break;

                case "temps-par-type":
                    Map<String, Double> tempsParType;
                    
                    String dateDebut = req.getParameter("dateDebut");
                    String dateFin = req.getParameter("dateFin");
                    
                    if (dateDebut != null && dateFin != null) {
                        LocalDateTime debut = LocalDateTime.parse(dateDebut, formatter);
                        LocalDateTime fin = LocalDateTime.parse(dateFin, formatter);
                        tempsParType = statistiqueService.getTempsParTypeActivitePeriode(userId, debut, fin);
                    } else {
                        tempsParType = statistiqueService.getTempsParTypeActivite(userId);
                    }
                    
                    JsonObject resultTemps = new JsonObject();
                    resultTemps.addProperty("succes", true);
                    resultTemps.add("tempsParType", gson.toJsonTree(tempsParType));
                    resultTemps.add("pourcentages", gson.toJsonTree(statistiqueService.getPourcentageParTypeActivite(userId)));
                    out.print(gson.toJson(resultTemps));
                    break;

                case "equilibre":
                    JsonObject resultEquilibre = new JsonObject();
                    resultEquilibre.addProperty("succes", true);
                    resultEquilibre.addProperty("ratioTravailRepos", statistiqueService.getRatioTravailRepos(userId));
                    resultEquilibre.addProperty("niveauEquilibre", statistiqueService.getNiveauEquilibre(userId));
                    resultEquilibre.addProperty("heuresTravail", statistiqueService.getHeuresTravail(userId));
                    resultEquilibre.addProperty("heuresRepos", statistiqueService.getHeuresRepos(userId));
                    out.print(gson.toJson(resultEquilibre));
                    break;

                case "fatigue":
                    JsonObject resultFatigue = new JsonObject();
                    resultFatigue.addProperty("succes", true);
                    resultFatigue.addProperty("scoreFatigue", statistiqueService.getScoreFatigue(userId));
                    resultFatigue.addProperty("niveauFatigue", statistiqueService.getNiveauFatigue(userId));
                    resultFatigue.addProperty("heuresTravailConsecutivesMax", statistiqueService.getHeuresTravailConsecutivesMax(userId));
                    out.print(gson.toJson(resultFatigue));
                    break;

                case "productivite":
                    JsonObject resultProductivite = new JsonObject();
                    resultProductivite.addProperty("succes", true);
                    resultProductivite.addProperty("scoreProductivite", statistiqueService.getScoreProductivite(userId));
                    resultProductivite.addProperty("tauxRespectDeadlines", statistiqueService.getTauxRespectDeadlines(userId));
                    resultProductivite.addProperty("nombreActivitesUrgentes", statistiqueService.getNombreActivitesUrgentes(userId, 7));
                    out.print(gson.toJson(resultProductivite));
                    break;

                case "tendances":
                    int nombreSemaines = 4;
                    String semaines = req.getParameter("semaines");
                    if (semaines != null) {
                        nombreSemaines = Integer.parseInt(semaines);
                    }
                    
                    JsonObject resultTendances = new JsonObject();
                    resultTendances.addProperty("succes", true);
                    resultTendances.add("activitesParSemaine", 
                        gson.toJsonTree(statistiqueService.getTendanceActivitesParSemaine(userId, nombreSemaines)));
                    resultTendances.add("fatigueParSemaine", 
                        gson.toJsonTree(statistiqueService.getTendanceFatigueParSemaine(userId, nombreSemaines)));
                    out.print(gson.toJson(resultTendances));
                    break;

                default:
                    sendError(resp, out, 404, "Endpoint non trouvé: " + endpoint);
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Erreur format nombre: " + e.getMessage());
            sendError(resp, out, 400, "Format de paramètre invalide");

        } catch (Exception e) {
            System.err.println("❌ Erreur dans StatistiqueController.doGet:");
            e.printStackTrace();
            sendError(resp, out, 500, "Erreur serveur: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, PrintWriter out, int status, String message) {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("succes", false);
        error.addProperty("message", message);
        out.print(gson.toJson(error));
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
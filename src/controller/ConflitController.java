package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entities.Conflit;
import entities.TypeConflit;
import service.ConflitService;
import service.impl.ConflitServiceImpl;
import util.GsonConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour la gestion des conflits
 * Endpoints:
 * - GET /api/conflits/utilisateur/{userId} - Liste des conflits d'un
 * utilisateur
 * - GET /api/conflits/utilisateur/{userId}/non-resolus - Conflits non résolus
 * - GET /api/conflits/utilisateur/{userId}/statistiques - Statistiques des
 * conflits
 * - POST /api/conflits/utilisateur/{userId}/detecter - Détecter les conflits
 * - PATCH /api/conflits/{id}/resoudre - Marquer un conflit comme résolu
 * - PUT /api/conflits/{id}/resoudre - Marquer un conflit comme résolu
 * (alternative)
 */
@WebServlet(urlPatterns = { "/api/conflits/*" })
public class ConflitController extends HttpServlet {

    private final ConflitService conflitService;
    private final Gson gson;

    public ConflitController() {
        this.conflitService = new ConflitServiceImpl();
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
            if (pathInfo != null && pathInfo.startsWith("/utilisateur/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);

                    if (parts.length >= 4) {
                        String action = parts[3];

                        if ("non-resolus".equals(action)) {
                            // GET /api/conflits/utilisateur/{userId}/non-resolus
                            List<Conflit> conflits = conflitService.getConflitsNonResolusUtilisateur(userId);
                            System.out.println("👤 Conflits non résolus pour user " + userId + ": " + conflits.size());
                            out.print(gson.toJson(conflits));

                        } else if ("statistiques".equals(action)) {
                            // GET /api/conflits/utilisateur/{userId}/statistiques
                            JsonObject stats = new JsonObject();
                            stats.addProperty("total", conflitService.compterConflitsUtilisateur(userId));
                            stats.addProperty("nonResolus",
                                    conflitService.compterConflitsNonResolusUtilisateur(userId));
                            stats.addProperty("tauxResolution", conflitService.getTauxResolutionUtilisateur(userId));

                            Map<TypeConflit, Integer> parType = conflitService
                                    .getStatistiquesParTypeUtilisateur(userId);
                            stats.add("parType", gson.toJsonTree(parType));

                            out.print(gson.toJson(stats));

                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            JsonObject error = new JsonObject();
                            error.addProperty("message", "Endpoint non trouvé");
                            out.print(gson.toJson(error));
                        }
                    } else {
                        // GET /api/conflits/utilisateur/{userId}
                        List<Conflit> conflits = conflitService.getTousLesConflitsUtilisateur(userId);
                        System.out.println("👤 Tous les conflits pour user " + userId + ": " + conflits.size());
                        out.print(gson.toJson(conflits));
                    }
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("message", "ID utilisateur requis");
                out.print(gson.toJson(error));
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ConflitController.doGet:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            if (pathInfo != null && pathInfo.contains("/detecter")) {
                // POST /api/conflits/utilisateur/{userId}/detecter
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);

                    System.out.println("🔍 Détection des conflits pour user " + userId);
                    List<Conflit> conflitsDetectes = conflitService.detecterChevauchementsUtilisateur(userId);

                    response.addProperty("succes", true);
                    response.addProperty("message", conflitsDetectes.size() + " conflit(s) détecté(s)");
                    response.add("conflits", gson.toJsonTree(conflitsDetectes));

                    System.out.println("✅ " + conflitsDetectes.size() + " conflit(s) détecté(s)");
                    out.print(gson.toJson(response));
                    return;
                }
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "Endpoint non trouvé");
            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ConflitController.doPost:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(response));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            if (pathInfo != null && pathInfo.contains("/resoudre")) {
                // PUT /api/conflits/{id}/resoudre
                String idStr = pathInfo.replace("/resoudre", "").substring(1);
                Long id = Long.parseLong(idStr);

                System.out.println("✅ Tentative de résolution du conflit ID: " + id);
                boolean success = conflitService.marquerConflitCommeResolu(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Conflit marqué comme résolu");
                    System.out.println("✅ Conflit " + id + " résolu avec succès");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Conflit non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    System.out.println("❌ Conflit " + id + " non trouvé");
                }

                out.print(gson.toJson(response));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "Endpoint non trouvé");
            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ConflitController.doPut:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(response));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            if (pathInfo != null && pathInfo.contains("/resoudre")) {
                // PATCH /api/conflits/{id}/resoudre
                String idStr = pathInfo.replace("/resoudre", "").substring(1);
                Long id = Long.parseLong(idStr);

                System.out.println("✅ Tentative de résolution du conflit ID: " + id);
                boolean success = conflitService.marquerConflitCommeResolu(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Conflit marqué comme résolu");
                    System.out.println("✅ Conflit " + id + " résolu avec succès");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Conflit non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    System.out.println("❌ Conflit " + id + " non trouvé");
                }

                out.print(gson.toJson(response));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "Endpoint non trouvé");
            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ConflitController.doPatch:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(response));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // DELETE /api/conflits/{id}
                Long id = Long.parseLong(pathInfo.substring(1));

                boolean success = conflitService.supprimerConflit(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Conflit supprimé");
                    System.out.println("✅ Conflit " + id + " supprimé");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Conflit non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ConflitController.doDelete:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur");
            out.print(gson.toJson(response));
        }
    }

    // ========== UTILITY METHODS ==========

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entities.Conflit;
import entities.TypeConflit;
import service.ConflitService;
import service.impl.ConflitServiceImpl;

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
 * Note: Les conflits sont détectés AUTOMATIQUEMENT par le système
 * Endpoints:
 * - GET /api/conflits/utilisateur/{id} - Liste des conflits d'un utilisateur
 * - GET /api/conflits/utilisateur/{id}/non-resolus - Conflits non résolus
 * - POST /api/conflits/utilisateur/{id}/detecter - Lancer la détection automatique
 * - PATCH /api/conflits/{id}/resoudre - Marquer un conflit comme résolu
 * - GET /api/conflits/utilisateur/{id}/statistiques - Statistiques des conflits
 */
@WebServlet(urlPatterns = {"/api/conflits/*"})
public class ConflitController extends HttpServlet {
    
    private final ConflitService conflitService;
    private final Gson gson;
    
    public ConflitController() {
        this.conflitService = new ConflitServiceImpl();
        this.gson = new Gson();
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
                            // GET /api/conflits/utilisateur/{id}/non-resolus
                            List<Conflit> conflits = conflitService.getConflitsNonResolusUtilisateur(userId);
                            out.print(gson.toJson(conflits));
                            
                        } else if ("statistiques".equals(action)) {
                            // GET /api/conflits/utilisateur/{id}/statistiques
                            JsonObject stats = new JsonObject();
                            stats.addProperty("total", conflitService.compterConflitsUtilisateur(userId));
                            stats.addProperty("nonResolus", conflitService.compterConflitsNonResolusUtilisateur(userId));
                            stats.addProperty("tauxResolution", conflitService.getTauxResolutionUtilisateur(userId));
                            
                            Map<TypeConflit, Integer> parType = conflitService.getStatistiquesParTypeUtilisateur(userId);
                            stats.add("parType", gson.toJsonTree(parType));
                            
                            out.print(gson.toJson(stats));
                            
                        } else {
                            // Unknown action
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            JsonObject error = new JsonObject();
                            error.addProperty("message", "Endpoint non trouvé");
                            out.print(gson.toJson(error));
                        }
                    } else {
                        // GET /api/conflits/utilisateur/{id} - Tous les conflits
                        List<Conflit> conflits = conflitService.getTousLesConflitsUtilisateur(userId);
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
            // POST /api/conflits/utilisateur/{id}/detecter - Détection automatique des conflits
            if (pathInfo != null && pathInfo.contains("/detecter")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);
                    
                    // Lancer la détection automatique des chevauchements
                    List<Conflit> conflitsDetectes = conflitService.detecterChevauchementsUtilisateur(userId);
                    
                    response.addProperty("succes", true);
                    response.addProperty("message", conflitsDetectes.size() + " conflit(s) détecté(s)");
                    response.add("conflits", gson.toJsonTree(conflitsDetectes));
                    
                    out.print(gson.toJson(response));
                    return;
                }
            }
            
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "Endpoint non trouvé");
            out.print(gson.toJson(response));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(response));
        }
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle PATCH method
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
            // PATCH /api/conflits/{id}/resoudre - Marquer comme résolu
            if (pathInfo != null && pathInfo.contains("/resoudre")) {
                String idStr = pathInfo.replace("/resoudre", "").substring(1);
                Long id = Long.parseLong(idStr);
                
                boolean success = conflitService.marquerConflitCommeResolu(id);
                
                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Conflit marqué comme résolu");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Conflit non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                
                out.print(gson.toJson(response));
                return;
            }
            
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "Endpoint non trouvé");
            out.print(gson.toJson(response));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(response));
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}

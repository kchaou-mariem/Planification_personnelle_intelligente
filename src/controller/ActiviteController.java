package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entities.Activite;
import entities.Conflit;
import entities.Contrainte;
import entities.TypeActivite;
import service.ActiviteService;
import service.ConflitService;
import service.ContrainteService;
import service.impl.ActiviteServiceImpl;
import service.impl.ConflitServiceImpl;
import service.impl.ContrainteServiceImpl;
import util.GsonConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller REST pour la gestion des activités
 * Endpoints:
 * - GET /api/activites/utilisateur/{id} - Liste des activités d'un utilisateur
 * - GET /api/activites/{id} - Détail d'une activité
 * - GET /api/activites/utilisateur/{id}/score - Score du planning (avec
 * conflits)
 * - GET /api/activites/utilisateur/{id}/valider - Validation du planning
 * - POST /api/activites - Créer une activité
 * - POST /api/activites/utilisateur/{id}/optimiser - Optimiser le planning
 * - PUT /api/activites/{id} - Modifier une activité
 * - DELETE /api/activites/{id} - Supprimer une activité
 */
@WebServlet(urlPatterns = { "/api/activites/*" })
public class ActiviteController extends HttpServlet {

    private final ActiviteService activiteService;
    private final ContrainteService contrainteService;
    private final Gson gson;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ActiviteController() {
        this.activiteService = new ActiviteServiceImpl();
        this.contrainteService = new ContrainteServiceImpl();
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
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/activites - Toutes les activités
                List<Activite> activites = activiteService.getAll();
                out.print(gson.toJson(activites));

            } else if (pathInfo.matches("/utilisateur/\\d+/score")) {
                // GET /api/activites/utilisateur/{userId}/score - Score du planning AVEC
                // conflits
                String[] parts = pathInfo.split("/");
                Long userId = Long.parseLong(parts[2]);

                List<Activite> activites = activiteService.getByUtilisateur(userId);
                List<Contrainte> contraintes = contrainteService.getByUtilisateur(userId.intValue());

                // ✅ UTILISER LA MÉTHODE AVEC CONFLITS
                double score = activiteService.calculerScoreAvecConflits(activites, contraintes);

                // Calculer aussi le planning valide
                boolean valide = activiteService.planningValide(activites, contraintes);

                JsonObject result = new JsonObject();
                result.addProperty("succes", true);
                result.addProperty("score", score);
                result.addProperty("valide", valide);
                result.addProperty("nombreActivites", activites.size());
                result.addProperty("nombreContraintes", contraintes.size());

                out.print(gson.toJson(result));

            } else if (pathInfo.matches("/utilisateur/\\d+/valider")) {
                // GET /api/activites/utilisateur/{userId}/valider - Valider le planning
                String[] parts = pathInfo.split("/");
                Long userId = Long.parseLong(parts[2]);

                List<Activite> activites = activiteService.getByUtilisateur(userId);
                List<Contrainte> contraintes = contrainteService.getByUtilisateur(userId.intValue());

                boolean valide = activiteService.planningValide(activites, contraintes);
                double score = activiteService.calculerScoreAvecConflits(activites, contraintes);

                JsonObject result = new JsonObject();
                result.addProperty("succes", true);
                result.addProperty("valide", valide);
                result.addProperty("score", score);
                result.addProperty("nombreActivites", activites.size());
                result.addProperty("nombreContraintes", contraintes.size());

                out.print(gson.toJson(result));

            } else if (pathInfo.startsWith("/utilisateur/")) {
                // GET /api/activites/utilisateur/{userId} ou /utilisateur/{userId}/type/{type}
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);

                    if (parts.length >= 5 && "type".equals(parts[3])) {
                        String typeStr = parts[4];
                        TypeActivite type = TypeActivite.valueOf(typeStr);
                        List<Activite> activites = activiteService.getByType(userId, type);
                        out.print(gson.toJson(activites));
                    } else {
                        List<Activite> activites = activiteService.getByUtilisateur(userId);
                        System.out.println("👤 Récupération activités pour user ID: " + userId);
                        System.out.println("✅ Trouvé " + activites.size() + " activités");
                        out.print(gson.toJson(activites));
                    }
                }

            } else {
                // GET /api/activites/{id}
                Long id = Long.parseLong(pathInfo.substring(1));
                Activite activite = activiteService.getById(id);

                if (activite != null) {
                    out.print(gson.toJson(activite));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject error = new JsonObject();
                    error.addProperty("message", "Activité non trouvée");
                    out.print(gson.toJson(error));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ActiviteController.doGet:");
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
            // POST /api/activites/utilisateur/{userId}/optimiser - Optimiser le planning
            if (pathInfo != null && pathInfo.matches("/utilisateur/\\d+/optimiser")) {
                String[] parts = pathInfo.split("/");
                Long userId = Long.parseLong(parts[2]);

                // Lire le nombre d'itérations
                String body = getRequestBody(req);
                int iterations = 1000;
                if (body != null && !body.isEmpty()) {
                    JsonObject json = gson.fromJson(body, JsonObject.class);
                    if (json.has("iterations")) {
                        iterations = json.get("iterations").getAsInt();
                    }
                }

                System.out.println("🔄 Début optimisation pour user " + userId + " avec " + iterations + " itérations");

                // ✅ ÉTAPE 1 : Supprimer TOUS les conflits existants avant l'optimisation
                ConflitService conflitService = new ConflitServiceImpl();
                List<Conflit> anciensConflits = conflitService.getTousLesConflitsUtilisateur(userId);
                int conflitsSupprimes = 0;
                for (Conflit conflit : anciensConflits) {
                    if (conflitService.supprimerConflit(conflit.getidConflit())) {
                        conflitsSupprimes++;
                    }
                }
                System.out.println("🗑️ " + conflitsSupprimes + " anciens conflits supprimés");

                // Récupérer les activités et contraintes
                List<Activite> activites = activiteService.getByUtilisateur(userId);
                List<Contrainte> contraintes = contrainteService.getByUtilisateur(userId.intValue());

                // Score avant optimisation
                double scoreAvant = activiteService.calculerScoreAvecConflits(activites, contraintes);
                boolean valideAvant = activiteService.planningValide(activites, contraintes);

                System.out.println("📊 Score avant: " + scoreAvant + " - Valide: " + valideAvant);

                // ✅ ÉTAPE 2 : Optimiser
                List<Activite> activitesOptimisees = activiteService.optimiserPlanning(
                        activites,
                        contraintes,
                        iterations);

                // Score après optimisation (AVANT la mise à jour en base)
                double scoreApres = activiteService.calculerScoreAvecConflits(activitesOptimisees, contraintes);
                boolean valideApres = activiteService.planningValide(activitesOptimisees, contraintes);

                System.out.println(
                        "📊 Score après optimisation (théorique): " + scoreApres + " - Valide: " + valideApres);

                // ✅ ÉTAPE 3 : Mettre à jour SANS détecter les conflits automatiquement
                int activitesMisesAJour = 0;
                ActiviteServiceImpl serviceImpl = (ActiviteServiceImpl) activiteService;

                for (Activite activite : activitesOptimisees) {
                    // ✅ Utiliser la méthode SANS détection de conflits
                    if (serviceImpl.modifierSansDetectionConflits(activite)) {
                        activitesMisesAJour++;
                    }
                }

                System.out.println("✅ " + activitesMisesAJour + " activités mises à jour (sans recréer de conflits)");

                // ✅ ÉTAPE 4 : Détecter manuellement les NOUVEAUX conflits (s'il en reste)
                // Si le planning optimisé n'est toujours pas valide, créer les conflits
                // nécessaires
                if (!valideApres) {
                    System.out.println("⚠️ Planning toujours invalide après optimisation, détection des conflits...");

                    // Utiliser la méthode normale pour AU MOINS une activité afin de détecter les
                    // conflits
                    if (!activitesOptimisees.isEmpty()) {
                        serviceImpl.mettreAJourActivite(activitesOptimisees.get(0));
                    }
                }

                // ✅ ÉTAPE 5 : Recompter les conflits restants
                List<Conflit> nouveauxConflits = conflitService.getTousLesConflitsUtilisateur(userId);
                int conflitsRestants = nouveauxConflits.size();

                System.out.println("📊 Conflits restants: " + conflitsRestants);

                // Réponse
                response.addProperty("succes", true);
                response.addProperty("message", "Planning optimisé avec succès");
                response.addProperty("iterations", iterations);
                response.addProperty("scoreAvant", scoreAvant);
                response.addProperty("scoreApres", scoreApres);
                response.addProperty("amelioration", scoreApres - scoreAvant);
                response.addProperty("valideAvant", valideAvant);
                response.addProperty("valideApres", valideApres);
                response.addProperty("activitesMisesAJour", activitesMisesAJour);
                response.addProperty("conflitsSupprimes", conflitsSupprimes);
                response.addProperty("conflitsRestants", conflitsRestants);
                response.add("activites", gson.toJsonTree(activitesOptimisees));

                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(gson.toJson(response));
                return;
            }

            // POST /api/activites - Créer une activité
            String body = getRequestBody(req);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            Activite activite = new Activite();
            activite.setTitre(getJsonString(json, "titre"));
            activite.setDescription(getJsonString(json, "description"));
            activite.setIdUtilisateur(json.get("idUtilisateur").getAsLong());

            // Type d'activité
            String typeStr = getJsonString(json, "typeActivite");
            if (typeStr != null) {
                activite.setTypeActivite(TypeActivite.valueOf(typeStr));
            }

            // Priorité
            if (json.has("priorite") && !json.get("priorite").isJsonNull()) {
                activite.setPriorite(json.get("priorite").getAsInt());
            }

            // Dates
            String debutStr = getJsonString(json, "horaireDebut");
            if (debutStr != null) {
                activite.setHoraireDebut(parseDateTime(debutStr));
            }

            String finStr = getJsonString(json, "horaireFin");
            if (finStr != null) {
                activite.setHoraireFin(parseDateTime(finStr));
            }

            String deadlineStr = getJsonString(json, "deadline");
            if (deadlineStr != null && !deadlineStr.isEmpty()) {
                activite.setDeadline(parseDateTime(deadlineStr));
            }

            boolean success = activiteService.ajouter(activite);

            if (success) {
                response.addProperty("succes", true);
                response.addProperty("message", "Activité créée");
                response.add("activite", gson.toJsonTree(activite));
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.addProperty("succes", false);
                response.addProperty("message", "Erreur lors de la création");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ActiviteController.doPost:");
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
            if (pathInfo != null && pathInfo.length() > 1) {
                Long id = Long.parseLong(pathInfo.substring(1));
                Activite existingActivite = activiteService.getById(id);

                if (existingActivite != null) {
                    String body = getRequestBody(req);
                    JsonObject json = gson.fromJson(body, JsonObject.class);

                    // Mise à jour des champs
                    if (json.has("titre"))
                        existingActivite.setTitre(getJsonString(json, "titre"));
                    if (json.has("description"))
                        existingActivite.setDescription(getJsonString(json, "description"));

                    if (json.has("typeActivite")) {
                        existingActivite.setTypeActivite(TypeActivite.valueOf(getJsonString(json, "typeActivite")));
                    }

                    if (json.has("priorite") && !json.get("priorite").isJsonNull()) {
                        existingActivite.setPriorite(json.get("priorite").getAsInt());
                    }

                    if (json.has("horaireDebut")) {
                        existingActivite.setHoraireDebut(parseDateTime(getJsonString(json, "horaireDebut")));
                    }
                    if (json.has("horaireFin")) {
                        existingActivite.setHoraireFin(parseDateTime(getJsonString(json, "horaireFin")));
                    }
                    if (json.has("deadline")) {
                        String dl = getJsonString(json, "deadline");
                        existingActivite.setDeadline(dl != null && !dl.isEmpty() ? parseDateTime(dl) : null);
                    }

                    boolean success = activiteService.modifier(existingActivite);

                    if (success) {
                        response.addProperty("succes", true);
                        response.addProperty("message", "Activité modifiée");
                        response.add("activite", gson.toJsonTree(existingActivite));
                    } else {
                        response.addProperty("succes", false);
                        response.addProperty("message", "Erreur lors de la modification");
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Activité non trouvée");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ActiviteController.doPut:");
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
            if (pathInfo != null && pathInfo.length() > 1) {
                Long id = Long.parseLong(pathInfo.substring(1));
                boolean success = activiteService.supprimer(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Activité supprimée");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Activité non trouvée");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ActiviteController.doDelete:");
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
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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

    private String getJsonString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isEmpty())
            return null;
        // Handle format from datetime-local input (yyyy-MM-ddTHH:mm)
        if (str.length() == 16) {
            str = str + ":00";
        }
        return LocalDateTime.parse(str, formatter);
    }
}
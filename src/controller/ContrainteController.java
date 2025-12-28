package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entities.Contrainte;
import entities.TypeContrainte;
import entities.StatutContrainte;
import service.ContrainteService;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = { "/api/contraintes/*" })
public class ContrainteController extends HttpServlet {

    private final ContrainteService contrainteService;
    private final Gson gson = GsonConfig.createGson(); // ✅ ICI

    public ContrainteController() {
        this.contrainteService = new ContrainteServiceImpl();
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
                List<Contrainte> contraintes = contrainteService.getAll();
                out.print(gson.toJson(contraintes));

            } else if (pathInfo.startsWith("/utilisateur/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    int userId = Integer.parseInt(parts[2]);

                    if (parts.length >= 4 && "actives".equals(parts[3])) {
                        List<Contrainte> contraintes = contrainteService.getContraintesActives(userId);
                        out.print(gson.toJson(contraintes));
                    } else {
                        List<Contrainte> contraintes = contrainteService.getByUtilisateur(userId);
                        out.print(gson.toJson(contraintes));
                    }
                }

            } else {
                int id = Integer.parseInt(pathInfo.substring(1));
                Contrainte contrainte = contrainteService.getById(id);

                if (contrainte != null) {
                    out.print(gson.toJson(contrainte));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject error = new JsonObject();
                    error.addProperty("message", "Contrainte non trouvée");
                    out.print(gson.toJson(error));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ContrainteController.doGet:");
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

        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            String body = getRequestBody(req);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            Contrainte contrainte = new Contrainte();
            contrainte.setTitre(getJsonString(json, "titre"));
            contrainte.setUtilisateurId(json.get("utilisateurId").getAsInt());

            String typeStr = getJsonString(json, "type");
            if (typeStr != null) {
                contrainte.setType(TypeContrainte.valueOf(typeStr));
            }

            String debutStr = getJsonString(json, "dateHeureDeb");
            if (debutStr != null) {
                contrainte.setDateHeureDeb(LocalTime.parse(debutStr));
            }

            String finStr = getJsonString(json, "dateHeureFin");
            if (finStr != null) {
                contrainte.setDateHeureFin(LocalTime.parse(finStr));
            }

            if (json.has("repetitif")) {
                contrainte.setRepetitif(json.get("repetitif").getAsBoolean());
            }

            if (json.has("joursSemaine") && json.get("joursSemaine").isJsonArray()) {
                List<DayOfWeek> jours = new ArrayList<>();
                json.get("joursSemaine").getAsJsonArray().forEach(j -> {
                    jours.add(DayOfWeek.valueOf(j.getAsString()));
                });
                contrainte.setJoursSemaine(jours);
            }

            contrainte.setStatut(StatutContrainte.ACTIVE);

            boolean success = contrainteService.ajouter(contrainte);

            if (success) {
                response.addProperty("succes", true);
                response.addProperty("message", "Contrainte créée");
                response.add("contrainte", gson.toJsonTree(contrainte));
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.addProperty("succes", false);
                response.addProperty("message", "Erreur lors de la création");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ContrainteController.doPost:");
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
                int id = Integer.parseInt(pathInfo.substring(1));
                Contrainte existing = contrainteService.getById(id);

                if (existing != null) {
                    String body = getRequestBody(req);
                    JsonObject json = gson.fromJson(body, JsonObject.class);

                    if (json.has("titre"))
                        existing.setTitre(getJsonString(json, "titre"));
                    if (json.has("type"))
                        existing.setType(TypeContrainte.valueOf(getJsonString(json, "type")));
                    if (json.has("dateHeureDeb"))
                        existing.setDateHeureDeb(LocalTime.parse(getJsonString(json, "dateHeureDeb")));
                    if (json.has("dateHeureFin"))
                        existing.setDateHeureFin(LocalTime.parse(getJsonString(json, "dateHeureFin")));
                    if (json.has("repetitif"))
                        existing.setRepetitif(json.get("repetitif").getAsBoolean());

                    boolean success = contrainteService.modifier(existing);

                    if (success) {
                        response.addProperty("succes", true);
                        response.addProperty("message", "Contrainte modifiée");
                    } else {
                        response.addProperty("succes", false);
                        response.addProperty("message", "Erreur lors de la modification");
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Contrainte non trouvée");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ContrainteController.doPut:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur");
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
                int id = Integer.parseInt(pathInfo.substring(1));
                boolean success = contrainteService.supprimer(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Contrainte supprimée");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Contrainte non trouvée");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ContrainteController.doDelete:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur");
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
            if (pathInfo != null && pathInfo.contains("/toggle")) {
                String idStr = pathInfo.replace("/toggle", "").substring(1);
                int id = Integer.parseInt(idStr);

                boolean success = contrainteService.toggleStatut(id);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Statut modifié");
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Erreur");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans ContrainteController.doPatch:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur");
            out.print(gson.toJson(response));
        }
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
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
}
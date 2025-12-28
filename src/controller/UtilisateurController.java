package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entities.Utilisateur;
import service.UtilisateurService;
import service.impl.UtilisateurServiceImpl;
import util.GsonConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = { "/api/utilisateurs/*" })
public class UtilisateurController extends HttpServlet {

    private final UtilisateurService utilisateurService;
    private final Gson gson = GsonConfig.createGson(); // ✅ ICI

    public UtilisateurController() {
        this.utilisateurService = new UtilisateurServiceImpl();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            String body = getRequestBody(req);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            JsonObject response = new JsonObject();

            if ("/register".equals(pathInfo)) {
                // Inscription
                Utilisateur user = new Utilisateur();
                user.setNom(getJsonString(json, "nom"));
                user.setPrenom(getJsonString(json, "prenom"));
                user.setEmail(getJsonString(json, "email"));
                user.set_mot_de_passe(getJsonString(json, "motdepasse"));

                if (json.has("age") && !json.get("age").isJsonNull()) {
                    user.setAge(json.get("age").getAsInt());
                }
                user.setGenre(getJsonString(json, "genre"));
                user.setPoste(getJsonString(json, "poste"));

                boolean success = utilisateurService.creerUtilisateur(user);

                if (success) {
                    response.addProperty("succes", true);
                    response.addProperty("message", "Inscription réussie");
                    response.add("utilisateur", userToJson(user));
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Erreur lors de l'inscription");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }

            } else if ("/login".equals(pathInfo)) {
                // Connexion
                String email = getJsonString(json, "email");
                String motdepasse = getJsonString(json, "motdepasse");

                System.out.println("🔐 Tentative de connexion: " + email);

                Utilisateur user = utilisateurService.authentifier(email, motdepasse);

                if (user != null) {
                    System.out.println("✅ Authentification réussie pour: " + email);
                    response.addProperty("succes", true);
                    response.addProperty("message", "Connexion réussie");
                    response.add("utilisateur", userToJson(user));
                } else {
                    System.out.println("❌ Échec authentification pour: " + email);
                    response.addProperty("succes", false);
                    response.addProperty("message", "Email ou mot de passe incorrect");
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }

            } else {
                response.addProperty("succes", false);
                response.addProperty("message", "Endpoint non trouvé");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans UtilisateurController.doPost:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("succes", false);
            error.addProperty("message", "Erreur serveur: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                // GET /api/utilisateurs/{id}
                int id = Integer.parseInt(pathInfo.substring(1));
                Utilisateur user = utilisateurService.getUtilisateurById(id);

                if (user != null) {
                    response.addProperty("succes", true);
                    response.add("utilisateur", userToJson(user));
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Utilisateur non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                // GET /api/utilisateurs - Liste tous les utilisateurs
                response.addProperty("succes", true);
                response.add("utilisateurs", gson.toJsonTree(utilisateurService.getAllUtilisateurs()));
            }

            out.print(gson.toJson(response));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.addProperty("succes", false);
            response.addProperty("message", "ID invalide");
            out.print(gson.toJson(response));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans UtilisateurController.doGet:");
            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.addProperty("succes", false);
            response.addProperty("message", "Erreur serveur");
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
                Utilisateur existingUser = utilisateurService.getUtilisateurById(id);

                if (existingUser != null) {
                    String body = getRequestBody(req);
                    JsonObject json = gson.fromJson(body, JsonObject.class);

                    // Mise à jour des champs
                    if (json.has("nom"))
                        existingUser.setNom(getJsonString(json, "nom"));
                    if (json.has("prenom"))
                        existingUser.setPrenom(getJsonString(json, "prenom"));
                    if (json.has("age") && !json.get("age").isJsonNull()) {
                        existingUser.setAge(json.get("age").getAsInt());
                    }
                    if (json.has("genre"))
                        existingUser.setGenre(getJsonString(json, "genre"));
                    if (json.has("poste"))
                        existingUser.setPoste(getJsonString(json, "poste"));

                    boolean success = utilisateurService.modifierUtilisateur(existingUser);

                    if (success) {
                        response.addProperty("succes", true);
                        response.addProperty("message", "Profil mis à jour");
                        response.add("utilisateur", userToJson(existingUser));
                    } else {
                        response.addProperty("succes", false);
                        response.addProperty("message", "Erreur lors de la mise à jour");
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    response.addProperty("succes", false);
                    response.addProperty("message", "Utilisateur non trouvé");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }

            out.print(gson.toJson(response));

        } catch (Exception e) {
            System.err.println("❌ Erreur dans UtilisateurController.doPut:");
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

    private JsonObject userToJson(Utilisateur user) {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getId());
        json.addProperty("nom", user.getNom());
        json.addProperty("prenom", user.getPrenom());
        json.addProperty("email", user.getEmail());
        json.addProperty("age", user.getAge());
        json.addProperty("genre", user.getGenre());
        json.addProperty("poste", user.getPoste());
        return json;
    }
}
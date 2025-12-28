import com.sun.net.httpserver.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import controller.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.Method;

/**
 * Serveur HTTP Embarqué qui charge vos vrais Controllers
 * Replace UltraSimpleServer.java
 */
public class EmbeddedServer {

    private static final int PORT = 8083;
    private static final String WEBAPP_DIR = "src/webapp/view";
    private static final Gson gson = new Gson();

    // Map des controllers
    private static final Map<String, HttpServlet> controllers = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Initialiser les controllers
        initControllers();

        // Créer le serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Route API
        server.createContext("/api/", EmbeddedServer::handleApi);

        // Route fichiers statiques
        server.createContext("/", EmbeddedServer::handleStatic);

        server.start();

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  🚀 Serveur démarré avec succès !                        ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║  📡 API Backend:  http://localhost:" + PORT + "/api/              ║");
        System.out.println("║  🌐 Frontend:     http://localhost:" + PORT + "/                  ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║  Endpoints disponibles:                                   ║");
        System.out.println("║  • POST   /api/utilisateurs/register                     ║");
        System.out.println("║  • POST   /api/utilisateurs/login                        ║");
        System.out.println("║  • GET    /api/activites/utilisateur/{id}                ║");
        System.out.println("║  • POST   /api/activites                                 ║");
        System.out.println("║  • GET    /api/contraintes/utilisateur/{id}              ║");
        System.out.println("║  • GET    /api/conflits/utilisateur/{id}                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    /**
     * Initialise tous les controllers
     */
    private static void initControllers() {
        controllers.put("utilisateurs", new UtilisateurController());
        controllers.put("activites", new ActiviteController());
        controllers.put("contraintes", new ContrainteController());
        controllers.put("conflits", new ConflitController());
        controllers.put("statistiques", new StatistiqueController()); 
        System.out.println("✅ Controllers chargés: " + controllers.size());
    }

    /**
     * Gère les requêtes API
     */
    private static void handleApi(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Log
        System.out.println("📥 " + method + " " + path);

        // CORS
        setCorsHeaders(exchange);

        // OPTIONS preflight
        if ("OPTIONS".equals(method)) {
            sendResponse(exchange, 200, "");
            return;
        }

        try {
            // Extraire le nom du controller et le pathInfo
            String apiPath = path.substring(4); // enlever "/api"

            // Trouver le controller approprié
            HttpServlet controller = findController(apiPath);

            if (controller != null) {
                // Lire le body AVANT de créer les wrappers
                String requestBody = readRequestBody(exchange);

                // Créer des wrappers Servlet avec le pathInfo correct
                HttpExchangeRequest req = new HttpExchangeRequest(exchange, apiPath, requestBody);
                HttpExchangeResponse resp = new HttpExchangeResponse(exchange);

                // Appeler la méthode via réflexion pour contourner protected
                invokeServletMethod(controller, method, req, resp);

                // IMPORTANT: Envoyer la réponse après traitement
                resp.flushResponse();

            } else {
                sendJsonError(exchange, 404, "Endpoint non trouvé: " + path);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(exchange, 500, "Erreur serveur: " + e.getMessage());
        }
    }

    /**
     * Lit le body de la requête
     */
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(isr)) {

            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    /**
     * Invoque la méthode du servlet via réflexion
     */
    private static void invokeServletMethod(HttpServlet servlet, String httpMethod,
            HttpExchangeRequest req, HttpExchangeResponse resp) {
        try {
            Method method = null;

            switch (httpMethod) {
                case "GET":
                    method = HttpServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case "POST":
                    method = HttpServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case "PUT":
                    method = HttpServlet.class.getDeclaredMethod("doPut", HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case "DELETE":
                    method = HttpServlet.class.getDeclaredMethod("doDelete", HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
            }

            if (method != null) {
                method.setAccessible(true);
                method.invoke(servlet, req, resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'invocation du servlet", e);
        }
    }

    /**
     * Trouve le controller correspondant au path
     * Corrigé pour gérer correctement /api/activites (sans pathInfo)
     */
    private static HttpServlet findController(String apiPath) {
        // Enlever le "/" initial si présent
        if (apiPath.startsWith("/")) {
            apiPath = apiPath.substring(1);
        }

        // Extraire le nom du controller (première partie du path)
        String controllerName = apiPath.split("/")[0];

        System.out.println("🔍 Recherche controller pour: '" + controllerName + "'");

        return controllers.get(controllerName);
    }

    /**
     * Gère les fichiers statiques (HTML, CSS, JS)
     */
    private static void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(WEBAPP_DIR + path);

        if (file.exists() && !file.isDirectory()) {
            String contentType = getContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            System.out.println("📄 Served: " + path);
        } else {
            String response = "404 - File not found: " + path;
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /**
     * Détermine le Content-Type selon l'extension
     */
    private static String getContentType(String path) {
        if (path.endsWith(".html"))
            return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))
            return "text/css";
        if (path.endsWith(".js"))
            return "application/javascript";
        if (path.endsWith(".json"))
            return "application/json";
        if (path.endsWith(".png"))
            return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
            return "image/jpeg";
        if (path.endsWith(".gif"))
            return "image/gif";
        if (path.endsWith(".svg"))
            return "image/svg+xml";
        return "text/plain";
    }

    /**
     * Configure les headers CORS
     */
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * Envoie une réponse simple
     */
    private static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Envoie une erreur JSON
     */
    private static void sendJsonError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        JsonObject error = new JsonObject();
        error.addProperty("succes", false);
        error.addProperty("message", message);
        sendResponse(exchange, code, gson.toJson(error));
    }
}

/**
 * Wrapper HttpServletRequest pour HttpExchange
 */
class HttpExchangeRequest implements HttpServletRequest {
    private final HttpExchange exchange;
    private final String apiPath;
    private final String requestBody;
    private BufferedReader reader;

    public HttpExchangeRequest(HttpExchange exchange, String apiPath, String requestBody) {
        this.exchange = exchange;
        this.apiPath = apiPath;
        this.requestBody = requestBody;
    }

    @Override
    public String getPathInfo() {
        // Enlever le "/" initial si présent
        String path = apiPath.startsWith("/") ? apiPath.substring(1) : apiPath;

        // Extraire le pathInfo après le nom du controller
        // Ex: "activites" -> null
        // Ex: "activites/123" -> "/123"
        // Ex: "activites/utilisateur/5" -> "/utilisateur/5"
        String[] parts = path.split("/", 2);

        if (parts.length < 2 || parts[1].isEmpty()) {
            return null;
        }

        return "/" + parts[1];
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new StringReader(requestBody));
        }
        return reader;
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getParameter(String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(name)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    // Méthodes minimales requises
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        return -1;
    }

    @Override
    public String getHeader(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(exchange.getRequestHeaders().get(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(exchange.getRequestHeaders().keySet());
    }

    @Override
    public int getIntHeader(String name) {
        return -1;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return exchange.getRequestURI().getQuery();
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return exchange.getRequestURI().getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer("http://localhost" + exchange.getRequestURI());
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) {
        return false;
    }

    @Override
    public void login(String username, String password) {
    }

    @Override
    public void logout() {
    }

    @Override
    public Collection<Part> getParts() {
        return null;
    }

    @Override
    public Part getPart(String name) {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String env) {
    }

    @Override
    public int getContentLength() {
        return requestBody.length();
    }

    @Override
    public long getContentLengthLong() {
        return requestBody.length();
    }

    @Override
    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() {
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return new HashMap<>();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String[] getParameterValues(String name) {
        return null;
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "localhost";
    }

    @Override
    public int getServerPort() {
        return 8083;
    }

    @Override
    public String getRemoteAddr() {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {
        return exchange.getRemoteAddress().getHostName();
    }

    @Override
    public void setAttribute(String name, Object o) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(Arrays.asList(Locale.getDefault()));
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return exchange.getRemoteAddress().getPort();
    }

    @Override
    public String getLocalName() {
        return "localhost";
    }

    @Override
    public String getLocalAddr() {
        return "127.0.0.1";
    }

    @Override
    public int getLocalPort() {
        return 8083;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }
}

/**
 * Wrapper HttpServletResponse pour HttpExchange
 */
class HttpExchangeResponse implements HttpServletResponse {
    private final HttpExchange exchange;
    private final ByteArrayOutputStream outputStream;
    private PrintWriter writer;
    private int statusCode = 200;

    public HttpExchangeResponse(HttpExchange exchange) {
        this.exchange = exchange;
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void setStatus(int sc) {
        this.statusCode = sc;
    }

    @Override
    public void setHeader(String name, String value) {
        exchange.getResponseHeaders().set(name, value);
    }

    @Override
    public void setContentType(String type) {
        exchange.getResponseHeaders().set("Content-Type", type);
    }

    @Override
    public PrintWriter getWriter() {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8),
                    true);
        }
        return writer;
    }

    /**
     * Flush and send the response
     */
    public void flushResponse() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            byte[] bytes = outputStream.toByteArray();
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes minimales requises
    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public boolean containsHeader(String name) {
        return exchange.getResponseHeaders().containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    @Override
    public void sendError(int sc, String msg) {
        statusCode = sc;
    }

    @Override
    public void sendError(int sc) {
        statusCode = sc;
    }

    @Override
    public void sendRedirect(String location) {
    }

    @Override
    public void setDateHeader(String name, long date) {
    }

    @Override
    public void addDateHeader(String name, long date) {
    }

    @Override
    public void addHeader(String name, String value) {
        exchange.getResponseHeaders().add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
    }

    @Override
    public void addIntHeader(String name, int value) {
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.statusCode = sc;
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getHeader(String name) {
        return exchange.getResponseHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return exchange.getResponseHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return exchange.getResponseHeaders().keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return exchange.getResponseHeaders().getFirst("Content-Type");
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {
    }

    @Override
    public void setContentLength(int len) {
    }

    @Override
    public void setContentLengthLong(long len) {
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public int getBufferSize() {
        return 8192;
    }

    @Override
    public void flushBuffer() {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLocale(Locale loc) {
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
}
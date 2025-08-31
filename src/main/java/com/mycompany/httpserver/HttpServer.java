package com.mycompany.httpserver;

import com.mycompany.microspingboot.anotations.GetMapping;
import com.mycompany.microspingboot.anotations.RequestParam;
import com.mycompany.microspingboot.anotations.RestController;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {

    //public static Map<String, Method> services = new HashMap();
    public static Map<String, Handler> services = new HashMap<>();
    private static String staticRoot = null;
    private static final Path PUBLIC_DIR = Paths.get("src", "main", "resources", "webroot").toAbsolutePath();
    
    public static class Handler {
        final Object instance;
        final Method method;

        Handler(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
 
    public static void loadServices(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException { 
        if (args != null && args.length > 0) {
            registerController(args[0]);
        } else {
            autoScanAndRegister("com.mycompany.microspingboot.examples");
        }
    }
    
    private static void registerController(String fqcn) {
        try {
            Class c = Class.forName(fqcn);
            if (c.isAnnotationPresent(RestController.class)) {
                Object instance = c.getDeclaredConstructor().newInstance();
                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String mapping = m.getAnnotation(GetMapping.class).value();
                        m.setAccessible(true);
                        services.put(mapping, new Handler(instance, m));
                    }
                }
            }
        } catch (Exception ex) {
            System.getLogger(HttpServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    private static void autoScanAndRegister(String basePkg) {
        try {
            String path = basePkg.replace('.', '/');
            var cl = Thread.currentThread().getContextClassLoader();
            var resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                var dir = new java.io.File(url.toURI());
                var files = dir.listFiles((d, name) -> name.endsWith(".class"));
                if (files == null) {
                    continue;
                }
                for (var f : files) {
                    String cls = f.getName().substring(0, f.getName().length() - 6);
                    registerController(basePkg + "." + cls);
                }
            }
        } catch (Exception ignore) {
        }
    }

    public static void runServer(String[] args) throws IOException, URISyntaxException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        loadServices(args);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;

        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream   rawOut = clientSocket.getOutputStream();
            PrintWriter    out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String         inputLine, outputLine = null;;
            String         path = null;
            boolean        firstline = true;
            URI            requri = null;

            while ((inputLine = in.readLine()) != null) {
                if (firstline) {
                    requri = new URI(inputLine.split(" ")[1]);
                    System.out.println("Path: " + requri.getPath());
                    firstline = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            if (requri.getPath().startsWith("/app")) {
                outputLine = invokeService(requri);
            } else {
                if (!serveStatic(PUBLIC_DIR, requri.getPath(), rawOut))
                    writeText(rawOut, 404, "text/plain; charset=utf-8", "Not Found");
                //Leo del disco
                //outputLine = defaultResponse();
            }
            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String helloService(URI requesturi) {
        //Encabezado con content-type adaptado para retornar un JSON        
        String response = "HTTP/1.1 200 OK\n\r"
                + "content-type: application/json\n\r"
                + "\n\r";
        //Extrae el valor de "name" desde el query.
        String name = requesturi.getQuery().split("=")[1]; //name=jhon

        //Crea la respuesta completa                
        response = response + "{\"mensaje\": \"Hola " + name + "\"}";
        return response;
    }


    private static String invokeService(URI requri) throws InvocationTargetException {
        String header = "HTTP/1.1 200 OK\n\r"
                + "content-type: text/html\n\r"
                + "\n\r";
        try {
            HttpRequest req = new HttpRequest(requri);
            HttpResponse res = new HttpResponse();
            String servicePath = requri.getPath().substring(4);
            Handler h = services.get(servicePath);
            if (h == null) {
                return header + "404: ruta no encontrada";
            }

            Method m = h.method;
            var params = m.getParameters();
            Object[] args = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                var p = params[i];
                var ann = p.getAnnotations();
                String val = null;
                for (var a : ann) {
                    if (a instanceof RequestParam rp) {
                        String key = rp.value();
                        String qv = req.getValue(key);
                        val = (qv == null || qv.isEmpty()) ? rp.defaultValue() : qv;
                    }
                }
                args[i] = val;
            }

            Object ret = m.invoke(h.instance, args);
            return header + (ret == null ? "" : ret.toString());

        }catch (IllegalAccessException ex){
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null,ex);
        }catch (InvocationTargetException ex)
        { 
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null,ex);
        }
        return header + "Error";
    }
    
    private static boolean serveStatic(Path publicDir, String path, OutputStream out) {
        try {
            Path p = publicDir.resolve("." + path).normalize();
            if (!p.startsWith(publicDir) || !java.nio.file.Files.exists(p) || java.nio.file.Files.isDirectory(p)) {
                return false;
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(p);
            String ct = guessCT(p.getFileName().toString());

            var pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8), false);
            pw.print("HTTP/1.1 200 OK\r\n");
            pw.printf("Content-Type: %s\r\n", ct);
            pw.printf("Content-Length: %d\r\n", bytes.length);
            pw.print("\r\n");
            pw.flush();

            out.write(bytes);
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void writeText(OutputStream out, int status, String ct, String body) throws IOException {
        byte[] b = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8), false);
        pw.printf("HTTP/1.1 %d OK\r\n", status);
        pw.printf("Content-Type: %s\r\n", ct);
        pw.printf("Content-Length: %d\r\n", b.length);
        pw.print("\r\n");
        pw.flush();
        out.write(b);
        out.flush();
    }

    private static String inferContentType(String body) {
        String t = body.trim();
        return t.startsWith("<") ? "text/html; charset=utf-8" : "text/plain; charset=utf-8";
    }


    private static String guessCT(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) {
            return "text/html; charset=utf-8";
        }
        if (n.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (n.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (n.endsWith(".png")) {
            return "image/png";
        }
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (n.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }

    
    public static void staticfiles(String localFilesPath) 
    {
        if (localFilesPath.startsWith("/"))
        {
            staticRoot = localFilesPath;
        }
        else
        {
            staticRoot = ("/" + localFilesPath);
        }
    }
    
    public static void start(String[] args) throws IOException, URISyntaxException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        runServer(args);
    }

    public static String defaultResponse() {
        return "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Form Example</title>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Form with GET</h1>\n"
                + "<form action=\"/hello\">\n"
                + "<label for=\"name\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n"
                + "</form>\n"
                + "<div id=\"getrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadGetMsg() {\n"
                + "let nameVar = document.getElementById(\"name\").value;\n"
                + "const xhttp = new XMLHttpRequest();\n"
                + "xhttp.onload = function() {\n"
                + "document.getElementById(\"getrespmsg\").innerHTML =\n"
                + "this.responseText;\n"
                + "}\n"
                + "xhttp.open(\"GET\", \"/app/greeting?name=\"+nameVar);\n"
                + "xhttp.send();\n"
                + "}\n"
                + "</script>\n"
                + " \n"
                + "<h1>Form with POST</h1>\n"
                + "<form action=\"/hellopost\">\n"
                + "<label for=\"postname\">Name:</label><br>\n"
                + "<input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n"
                + "<input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n"
                + "</form>\n"
                + " \n"
                + "<div id=\"postrespmsg\"></div>\n"
                + " \n"
                + "<script>\n"
                + "function loadPostMsg(name){\n"
                + "let url = \"/hellopost?name=\" + name.value;\n"
                + " \n"
                + "fetch (url, {method: 'POST'})\n"
                + ".then(x => x.text())\n"
                + ".then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n"
                + "}\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
    }

}

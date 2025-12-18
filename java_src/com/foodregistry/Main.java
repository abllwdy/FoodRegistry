package com.foodregistry;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Restaurant restaurant;

    public static void main(String[] args) throws IOException {
        restaurant = new Restaurant();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        server.createContext("/", new RootHandler());
        server.createContext("/style.css", new StyleHandler());
        server.createContext("/action", new ActionHandler());
        
        server.setExecutor(null);
        System.out.println("Server started on port 8000");
        server.start();
    }

    static class StyleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            byte[] response = Files.readAllBytes(Paths.get("style.css"));
            t.getResponseHeaders().set("Content-Type", "text/css");
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String template = Files.readString(Paths.get("index.html"), StandardCharsets.UTF_8);
                
                String menu = restaurant.getMenuDisplay();
                String order = restaurant.getCurrentOrderDisplay();
                
                String response = template.replace("{{MENU}}", menu)
                                        .replace("{{ORDER}}", order)
                                        .replace("{{MESSAGE}}", "");

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
                String error = "Error: " + e.getMessage();
                t.sendResponseHeaders(500, error.length());
                t.getResponseBody().write(error.getBytes());
                t.getResponseBody().close();
            }
        }
    }

    static class ActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                StringBuilder sb = new StringBuilder();
                int i;
                while ((i = t.getRequestBody().read()) != -1) {
                    sb.append((char) i);
                }
                String formData = sb.toString();
                Map<String, String> params = parseFormData(formData);
                
                String message = "";
                String action = params.get("action");
                
                if ("add".equals(action)) {
                    String codeStr = params.get("code");
                    String qtyStr = params.get("quantity");
                    if (codeStr != null && !codeStr.isEmpty() && qtyStr != null && !qtyStr.isEmpty()) {
                        try {
                            char code = codeStr.charAt(0);
                            int qty = Integer.parseInt(qtyStr);
                            restaurant.processItemCode(code, qty);
                        } catch (Exception e) {
                            message = "Invalid input.";
                        }
                    }
                } else if ("clear".equals(action)) {
                    restaurant.clearOrder();
                    message = "Order cleared.";
                } else if ("checkout".equals(action)) {
                    String amtStr = params.get("amount");
                    if (amtStr != null && !amtStr.isEmpty()) {
                        try {
                            float amount = Float.parseFloat(amtStr);
                            message = restaurant.processPayment(amount);
                        } catch (Exception e) {
                            message = "Invalid amount.";
                        }
                    }
                } else if ("report".equals(action)) {
                     message = restaurant.generateDailyReport();
                }

                String template = Files.readString(Paths.get("index.html"), StandardCharsets.UTF_8);
                String menu = restaurant.getMenuDisplay();
                String order = restaurant.getCurrentOrderDisplay();
                
                String response = template.replace("{{MENU}}", menu)
                                        .replace("{{ORDER}}", order)
                                        .replace("{{MESSAGE}}", message);

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
        
        private Map<String, String> parseFormData(String formData) {
            Map<String, String> map = new HashMap<>();
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        map.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
                    } catch (Exception e) {}
                }
            }
            return map;
        }
    }
}

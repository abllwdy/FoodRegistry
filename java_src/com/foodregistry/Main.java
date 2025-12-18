package com.foodregistry;

import com.foodregistry.security.Permission;
import com.foodregistry.security.Role;
import com.foodregistry.security.UnauthorizedException;
import com.foodregistry.security.User;
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
import java.util.List;

public class Main {
    private static Restaurant restaurant;
    private static String loginMessage = "";

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

    private static String getLoginPage() {
        try {
            String content = Files.readString(Paths.get("login.html"), StandardCharsets.UTF_8);
            return content.replace("{{LOGIN_MESSAGE}}", loginMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error loading login page";
        }
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
                // Check authentication
                User currentUser = restaurant.getAuthService().getCurrentUser();
                if (currentUser == null) {
                     String response = getLoginPage();
                     byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                     t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                     t.sendResponseHeaders(200, bytes.length);
                     OutputStream os = t.getResponseBody();
                     os.write(bytes);
                     os.close();
                     return;
                }

                String template = Files.readString(Paths.get("index.html"), StandardCharsets.UTF_8);
                
                String menu = restaurant.getMenuDisplay();
                String order = restaurant.getCurrentOrderDisplay();
                
                // Build User Info
                String userInfo = "<div style='position: absolute; top: 10px; right: 10px; text-align: right;'>" +
                                  "<span>Logged in as: <strong>" + currentUser.getUsername() + " (" + currentUser.getRole() + ")</strong></span><br>" +
                                  "<form action='/action' method='post' style='display: inline;'>" +
                                  "<input type='hidden' name='action' value='logout'>" +
                                  "<button type='submit' style='padding: 5px 10px; background: #95a5a6; border: none; color: white; border-radius: 3px; cursor: pointer; font-size: 0.8em; margin-top: 5px;'>Logout</button>" +
                                  "</form></div>";

                // Build Manager Controls
                String managerControls = "";
                if (restaurant.getAuthService().hasPermission(Permission.MODIFY_MENU)) {
                     managerControls += "<div class='panel system-box' style='margin-top: 20px; border-top: 2px solid #eee; padding-top: 20px;'>" +
                                        "<h3>Manager Controls</h3>" +
                                        "<form action='/action' method='post' style='margin-bottom: 10px;'>" +
                                        "<h4>Modify Menu Price</h4>" +
                                        "<input type='text' name='code' placeholder='Code' required style='width: 50px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='number' name='price' step='0.01' placeholder='Price' required style='width: 80px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='hidden' name='action' value='modify_price'>" +
                                        "<button type='submit' class='btn' style='background: #e67e22;'>Update</button>" +
                                        "</form>" +
                                        "<form action='/action' method='post' style='margin-bottom: 10px;'>" +
                                        "<h4>Process Refund</h4>" +
                                        "<input type='number' name='order_number' placeholder='Order #' required style='width: 80px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='hidden' name='action' value='refund'>" +
                                        "<button type='submit' class='btn' style='background: #c0392b;'>Refund</button>" +
                                        "</form>" +
                                        "<form action='/action' method='post'>" +
                                        "<h4>Audit Logs</h4>" +
                                        "<input type='hidden' name='action' value='view_log'>" +
                                        "<button type='submit' class='btn' style='background: #34495e;'>View Logs</button>" +
                                        "</form>" +
                                        "</div>";
                }
                
                String response = template.replace("{{MENU}}", menu)
                                        .replace("{{ORDER}}", order)
                                        .replace("{{MESSAGE}}", "")
                                        .replace("{{USER_INFO}}", userInfo)
                                        .replace("{{MANAGER_CONTROLS}}", managerControls);

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
                
                try {
                    if ("login".equals(action)) {
                        String u = params.get("username");
                        String p = params.get("password");
                        if (restaurant.getAuthService().login(u, p)) {
                            loginMessage = "";
                            // Redirect to home to avoid resubmission issues or just render home
                        } else {
                            loginMessage = "Invalid credentials!";
                        }
                    } else if ("logout".equals(action)) {
                        restaurant.getAuthService().logout();
                        loginMessage = "Logged out successfully.";
                    } else if ("add".equals(action)) {
                        String codeStr = params.get("code");
                        String qtyStr = params.get("quantity");
                        if (codeStr != null && !codeStr.isEmpty() && qtyStr != null && !qtyStr.isEmpty()) {
                            try {
                                char code = codeStr.charAt(0);
                                int qty = Integer.parseInt(qtyStr);
                                restaurant.processItemCode(code, qty);
                            } catch (NumberFormatException e) {
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
                            } catch (NumberFormatException e) {
                                message = "Invalid amount.";
                            }
                        }
                    } else if ("report".equals(action)) {
                         message = restaurant.generateDailyReport();
                    } else if ("modify_price".equals(action)) {
                        String codeStr = params.get("code");
                        String priceStr = params.get("price");
                        if (codeStr != null && !codeStr.isEmpty() && priceStr != null && !priceStr.isEmpty()) {
                            try {
                                char code = codeStr.charAt(0);
                                float price = Float.parseFloat(priceStr);
                                restaurant.modifyMenuPrice(code, price);
                                message = "Price updated for " + code;
                            } catch (NumberFormatException e) {
                                message = "Invalid input.";
                            }
                        }
                    } else if ("refund".equals(action)) {
                        String ordStr = params.get("order_number");
                        if (ordStr != null && !ordStr.isEmpty()) {
                            try {
                                int orderNum = Integer.parseInt(ordStr);
                                message = restaurant.processRefund(orderNum);
                            } catch (NumberFormatException e) {
                                message = "Invalid order number.";
                            }
                        }
                    } else if ("view_log".equals(action)) {
                        if (restaurant.getAuthService().hasPermission(Permission.VIEW_HISTORICAL_REPORT)) { // Using existing permission or create new one?
                             // Re-using VIEW_HISTORICAL_REPORT as generic "view logs" or just MANAGER check
                             // The requirement said "AuditLog... records user actions".
                             // Let's just assume manager can view it.
                             if (restaurant.getAuthService().getCurrentUser().getRole() == Role.MANAGER) {
                                 StringBuilder logHtml = new StringBuilder("<h3>System Logs</h3><ul style='text-align:left;'>");
                                 for(String log : restaurant.getAuditLog().viewLog()) {
                                     logHtml.append("<li>").append(log).append("</li>");
                                 }
                                 logHtml.append("</ul>");
                                 message = logHtml.toString();
                             } else {
                                 throw new UnauthorizedException("Only Manager can view logs.");
                             }
                        } else {
                             throw new UnauthorizedException("Access Denied.");
                        }
                    }
                } catch (UnauthorizedException e) {
                    message = "⛔ " + e.getMessage();
                } catch (Exception e) {
                    message = "Error: " + e.getMessage();
                }

                // If logged out or not logged in, return login page
                User currentUser = restaurant.getAuthService().getCurrentUser();
                if (currentUser == null) {
                    String response = getLoginPage();
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    t.sendResponseHeaders(200, bytes.length);
                    OutputStream os = t.getResponseBody();
                    os.write(bytes);
                    os.close();
                    return;
                }

                String template = Files.readString(Paths.get("index.html"), StandardCharsets.UTF_8);
                String menu = restaurant.getMenuDisplay();
                String order = restaurant.getCurrentOrderDisplay();
                
                // Build User Info
                String userInfo = "<div style='position: absolute; top: 10px; right: 10px; text-align: right;'>" +
                                  "<span>Logged in as: <strong>" + currentUser.getUsername() + " (" + currentUser.getRole() + ")</strong></span><br>" +
                                  "<form action='/action' method='post' style='display: inline;'>" +
                                  "<input type='hidden' name='action' value='logout'>" +
                                  "<button type='submit' style='padding: 5px 10px; background: #95a5a6; border: none; color: white; border-radius: 3px; cursor: pointer; font-size: 0.8em; margin-top: 5px;'>Logout</button>" +
                                  "</form></div>";

                // Build Manager Controls
                String managerControls = "";
                if (restaurant.getAuthService().hasPermission(Permission.MODIFY_MENU)) {
                     managerControls += "<div class='panel system-box' style='margin-top: 20px; border-top: 2px solid #eee; padding-top: 20px;'>" +
                                        "<h3>Manager Controls</h3>" +
                                        "<form action='/action' method='post' style='margin-bottom: 10px;'>" +
                                        "<h4>Modify Menu Price</h4>" +
                                        "<input type='text' name='code' placeholder='Code' required style='width: 50px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='number' name='price' step='0.01' placeholder='Price' required style='width: 80px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='hidden' name='action' value='modify_price'>" +
                                        "<button type='submit' class='btn' style='background: #e67e22;'>Update</button>" +
                                        "</form>" +
                                        "<form action='/action' method='post' style='margin-bottom: 10px;'>" +
                                        "<h4>Process Refund</h4>" +
                                        "<input type='number' name='order_number' placeholder='Order #' required style='width: 80px; margin-right: 5px; padding: 5px;'>" +
                                        "<input type='hidden' name='action' value='refund'>" +
                                        "<button type='submit' class='btn' style='background: #c0392b;'>Refund</button>" +
                                        "</form>" +
                                        "<form action='/action' method='post'>" +
                                        "<h4>Audit Logs</h4>" +
                                        "<input type='hidden' name='action' value='view_log'>" +
                                        "<button type='submit' class='btn' style='background: #34495e;'>View Logs</button>" +
                                        "</form>" +
                                        "</div>";
                }
                
                String response = template.replace("{{MENU}}", menu)
                                        .replace("{{ORDER}}", order)
                                        .replace("{{MESSAGE}}", message)
                                        .replace("{{USER_INFO}}", userInfo)
                                        .replace("{{MANAGER_CONTROLS}}", managerControls);

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

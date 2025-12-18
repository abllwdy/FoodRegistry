package com.foodregistry;

import com.foodregistry.security.AuditLog;
import com.foodregistry.security.AuthenticationService;
import com.foodregistry.security.Permission;
import com.foodregistry.security.UnauthorizedException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Scanner;

public class Restaurant implements IRestaurant {
    private List<MenuItem> menu;
    private Order currentOrder;
    private Order dailySales;
    private List<Order> orderHistory;
    private int customerCount;
    private Receipt receiptPrinter;
    private AuthenticationService authService;
    private AuditLog auditLog;
    private static final String MENU_FILE = "menu.txt";

    public Restaurant() {
        menu = new ArrayList<>();
        loadMenu();

        currentOrder = new Order(menu.size());
        dailySales = new Order(menu.size());
        orderHistory = new ArrayList<>();
        customerCount = 0;
        receiptPrinter = new Receipt();
        authService = new AuthenticationService();
        auditLog = new AuditLog();
    }

    private void loadMenu() {
        File file = new File(MENU_FILE);
        if (!file.exists()) {
            createDefaultMenu();
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    char code = parts[0].charAt(0);
                    String name = parts[1];
                    float price = Float.parseFloat(parts[2]);
                    menu.add(new MenuItem(code, name, price));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            createDefaultMenu();
        }
    }

    private void createDefaultMenu() {
        menu.add(new MenuItem('N', "Nasi Lemak", 9.00f));
        menu.add(new MenuItem('C', "Chicken Rice", 8.00f));
        menu.add(new MenuItem('M', "Masala Dosa", 6.00f));
        menu.add(new MenuItem('H', "Hamburger", 5.00f));
        menu.add(new MenuItem('F', "Fish and Chips", 12.00f));
        saveMenu();
    }

    public void saveMenu() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MENU_FILE))) {
            for (MenuItem item : menu) {
                writer.println(item.getCode() + "," + item.getName() + "," + item.getPrice());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AuthenticationService getAuthService() {
        return authService;
    }

    @Override
    public AuditLog getAuditLog() {
        return auditLog;
    }

    @Override
    public void processItemCode(char code, int quantity) throws UnauthorizedException {
        if (!authService.hasPermission(Permission.PROCESS_ORDER)) {
            throw new UnauthorizedException("Access Denied: PROCESS_ORDER requires CASHIER or MANAGER privileges");
        }
        int index = findMenuItemByCode(code);
        if (index != -1) {
            currentOrder.addItem(index, quantity);
            currentOrder.calculateTotals(menu);
        }
    }

    private int findMenuItemByCode(char code) {
        for (int i = 0; i < menu.size(); i++) {
            if (Character.toUpperCase(menu.get(i).getCode()) == Character.toUpperCase(code)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String processPayment(float amount) throws UnauthorizedException {
        if (!authService.hasPermission(Permission.PROCESS_ORDER)) {
            throw new UnauthorizedException("Access Denied: PROCESS_ORDER requires CASHIER or MANAGER privileges");
        }

        if (amount < currentOrder.getTotal()) {
            return "Insufficient payment! Need RM " + String.format("%.2f", currentOrder.getTotal());
        }

        customerCount++;
        receiptPrinter.saveToFile(currentOrder, menu, amount, customerCount);
        
        // Add to history
        orderHistory.add(new Order(currentOrder));
        
        // Add to daily sales
        for (int i = 0; i < menu.size(); i++) {
            dailySales.addItem(i, currentOrder.getQuantity(i));
        }
        dailySales.calculateTotals(menu);

        auditLog.record(authService.getCurrentUser(), 
            String.format("Processed Order #%d - RM %.2f", customerCount, currentOrder.getTotal()));

        String receipt = generateReceiptString(amount);
        currentOrder.clear();
        return receipt;
    }

    @Override
    public void modifyMenuPrice(char code, float newPrice) throws UnauthorizedException {
        if (!authService.hasPermission(Permission.MODIFY_MENU)) {
            throw new UnauthorizedException("Access Denied: MODIFY_MENU requires MANAGER privileges");
        }
        int index = findMenuItemByCode(code);
        if (index != -1) {
            float oldPrice = menu.get(index).getPrice();
            menu.get(index).setPrice(newPrice);
            saveMenu();
            auditLog.record(authService.getCurrentUser(), 
                String.format("Modified Menu Item %c: RM %.2f -> RM %.2f", code, oldPrice, newPrice));
        }
    }

    @Override
    public String processRefund(int orderNumber) throws UnauthorizedException {
        if (!authService.hasPermission(Permission.PROCESS_REFUND)) {
            throw new UnauthorizedException("Access Denied: PROCESS_REFUND requires MANAGER privileges");
        }
        
        // Adjust index (1-based to 0-based)
        int index = orderNumber - 1;
        if (index < 0 || index >= orderHistory.size()) {
            return "Order not found.";
        }
        
        Order targetOrder = orderHistory.get(index);
        
        // Reverse sales from daily sales
        for (int i = 0; i < menu.size(); i++) {
            dailySales.addItem(i, -targetOrder.getQuantity(i)); // Subtract quantities
        }
        dailySales.calculateTotals(menu);
        
        // Remove from history? Or just keep it?
        // To keep history integrity, we might want to just mark it or leave it but the requirement is simple.
        // I'll remove it from the list to reflect "Refunded" status in report simply.
        orderHistory.remove(index);
        
        // Re-adjust customerCount? No, that messes up receipt numbers.
        // We just remove it from history and daily sales.
        
        auditLog.record(authService.getCurrentUser(), 
            String.format("Refunded Order #%d - RM %.2f", orderNumber, targetOrder.getTotal()));
            
        return "Refund processed for Order #" + orderNumber;
    }

    private String generateReceiptString(float amountPaid) {
        StringBuilder sb = new StringBuilder();
        float change = amountPaid - currentOrder.getTotal();
        sb.append("<h3>Receipt Order: #").append(customerCount).append("</h3>");
        sb.append("<div class='receipt'>");
        sb.append("Items in bill:<br>");
        for (int i = 0; i < menu.size(); i++) {
            if (currentOrder.getQuantity(i) > 0) {
                sb.append(String.format("(%c) [RM %.2f] %s x %d = RM %.2f<br>",
                    menu.get(i).getCode(), menu.get(i).getPrice(), menu.get(i).getName(),
                    currentOrder.getQuantity(i), menu.get(i).getPrice() * currentOrder.getQuantity(i)));
            }
        }
        sb.append("<hr>");
        sb.append(String.format("Subtotal: RM %.2f<br>", currentOrder.getSubtotal()));
        sb.append(String.format("SST charge 10%%: RM %.2f<br>", currentOrder.getSST()));
        sb.append(String.format("Grand total: RM %.2f<br>", currentOrder.getTotal()));
        sb.append("<hr>");
        sb.append(String.format("Amount received: RM %.2f<br>", amountPaid));
        if (change > 0) {
            sb.append(String.format("Change given: RM %.2f<br>", change));
        }
        sb.append("<hr>Thank you and have a nice day ahead!</div>");
        return sb.toString();
    }

    @Override
    public void clearOrder() {
        currentOrder.clear();
    }

    @Override
    public String generateDailyReport() throws UnauthorizedException {
        if (!authService.hasPermission(Permission.VIEW_DAILY_REPORT)) {
            throw new UnauthorizedException("Access Denied: VIEW_DAILY_REPORT requires privileges");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>DAILY MEAL SALES REPORT</h3>");
        sb.append("<div class='report'>");
        
        // Transaction History
        sb.append("<h4>Transaction History</h4>");
        if (orderHistory.isEmpty()) {
            sb.append("<p>No orders processed yet.</p>");
        } else {
            for (int i = 0; i < orderHistory.size(); i++) {
                Order ord = orderHistory.get(i);
                sb.append("<div class='mini-bill' style='margin-bottom: 10px; padding-bottom: 5px; border-bottom: 1px dashed #ccc;'>");
                sb.append("<strong>Order #").append(i + 1).append("</strong><br>");
                for (int j = 0; j < menu.size(); j++) {
                    if (ord.getQuantity(j) > 0) {
                        sb.append(String.format("- %s x %d<br>", menu.get(j).getName(), ord.getQuantity(j)));
                    }
                }
                sb.append(String.format("Total: RM %.2f<br>", ord.getTotal()));
                sb.append("</div>");
            }
        }
        
        sb.append("<h4>Daily Summary</h4>");
        sb.append("Orders billed: ").append(customerCount).append("<br>");
        sb.append("Items sold today:<br>");
        
        for (int i = 0; i < menu.size(); i++) {
            if (dailySales.getQuantity(i) > 0) {
                sb.append(String.format("(%c) [RM %.2f] %s x %d = RM %.2f<br>",
                    menu.get(i).getCode(), menu.get(i).getPrice(), menu.get(i).getName(),
                    dailySales.getQuantity(i), menu.get(i).getPrice() * dailySales.getQuantity(i)));
            }
        }
        sb.append("<hr>");
        sb.append(String.format("Subtotal: RM %.2f<br>", dailySales.getSubtotal()));
        sb.append(String.format("SST charge 10%%: RM %.2f<br>", dailySales.getSST()));
        sb.append(String.format("Grand total: RM %.2f<br>", dailySales.getTotal()));
        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public String getMenuDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='menu-grid'>");
        
        for (MenuItem item : menu) {
            sb.append("<div class='menu-card'>");
            
            // Header: Code and Price
            sb.append("<div class='card-header'>");
            sb.append("<div class='item-code'>").append(item.getCode()).append("</div>");
            sb.append("<div class='item-price'>RM ").append(String.format("%.2f", item.getPrice())).append("</div>");
            sb.append("</div>");
            
            // Item Name
            sb.append("<div class='item-name'>").append(item.getName()).append("</div>");
            
            // Controls Form
            sb.append("<form action='/action' method='post' class='item-controls'>");
            sb.append("<input type='hidden' name='action' value='add'>");
            sb.append("<input type='hidden' name='code' value='").append(item.getCode()).append("'>");
            
            sb.append("<div class='qty-wrapper'>");
            sb.append("<button type='button' class='btn-qty minus' onclick='updateQty(this, -1)'>-</button>");
            sb.append("<input type='number' name='quantity' value='0' min='1' class='qty-input' readonly>");
            sb.append("<button type='button' class='btn-qty plus' onclick='updateQty(this, 1)'>+</button>");
            sb.append("</div>");
            
            sb.append("<button type='submit' class='btn-add'>Add to Cart</button>");
            sb.append("</form>");
            
            sb.append("</div>"); // end card
        }
        
        sb.append("</div>");
        return sb.toString();
    }

    @Override
    public String getCurrentOrderDisplay() {
        if (currentOrder.isEmpty()) return "<p>Nothing.</p>";
        
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='order-list'>");
        for (int i = 0; i < menu.size(); i++) {
            if (currentOrder.getQuantity(i) > 0) {
                sb.append(String.format("<div>(%c) %s x %d = RM %.2f</div>",
                    menu.get(i).getCode(), menu.get(i).getName(), 
                    currentOrder.getQuantity(i), 
                    menu.get(i).getPrice() * currentOrder.getQuantity(i)));
            }
        }
        sb.append("<hr>");
        sb.append(String.format("<div><strong>Amount to be paid: RM %.2f</strong></div>", currentOrder.getTotal()));
        sb.append("</div>");
        return sb.toString();
    }
}

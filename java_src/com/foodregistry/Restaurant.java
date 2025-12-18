package com.foodregistry;

import java.util.ArrayList;
import java.util.List;

public class Restaurant implements IRestaurant {
    private List<MenuItem> menu;
    private Order currentOrder;
    private Order dailySales;
    private List<Order> orderHistory;
    private int customerCount;
    private Receipt receiptPrinter;

    public Restaurant() {
        menu = new ArrayList<>();
        menu.add(new MenuItem('N', "Nasi Lemak", 9.00f));
        menu.add(new MenuItem('C', "Chicken Rice", 8.00f));
        menu.add(new MenuItem('M', "Masala Dosa", 6.00f));
        menu.add(new MenuItem('H', "Hamburger", 5.00f));
        menu.add(new MenuItem('F', "Fish and Chips", 12.00f));

        currentOrder = new Order(menu.size());
        dailySales = new Order(menu.size());
        orderHistory = new ArrayList<>();
        customerCount = 0;
        receiptPrinter = new Receipt();
    }

    @Override
    public void processItemCode(char code, int quantity) {
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
    public String processPayment(float amount) {
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

        String receipt = generateReceiptString(amount);
        currentOrder.clear();
        return receipt;
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
    public String generateDailyReport() {
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

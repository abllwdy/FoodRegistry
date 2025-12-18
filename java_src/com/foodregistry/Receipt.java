package com.foodregistry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Receipt {
    
    public void saveToFile(Order order, List<MenuItem> menu, float amountPaid, int orderNumber) {
        try (PrintWriter outFile = new PrintWriter(new FileWriter("ReceiptNo" + orderNumber + ".txt"))) {
            printHeaderToFile(outFile);
            outFile.println("Order: #" + orderNumber);
            outFile.println();
            outFile.println("Items in bill:");

            for (int i = 0; i < menu.size(); i++) {
                if (order.getQuantity(i) > 0) {
                    outFile.printf("(%c) [RM %.2f] %s × %d = RM %.2f%n",
                            menu.get(i).getCode(),
                            menu.get(i).getPrice(),
                            menu.get(i).getName(),
                            order.getQuantity(i),
                            menu.get(i).getPrice() * order.getQuantity(i));
                }
            }

            float change = amountPaid - order.getTotal();
            printSeparatorToFile(outFile, 36);
            outFile.printf(" Subtotal: RM %.2f%n", order.getSubtotal());
            outFile.printf(" SST charge 10%%: RM %.2f%n", order.getSST());
            outFile.printf(" Grand total: RM %.2f%n", order.getTotal());
            printSeparatorToFile(outFile, 36);
            outFile.printf("Amount received: RM %.2f%n", amountPaid);
            if (change > 0) {
                outFile.printf(" Change given: RM %.2f%n", change);
            }
            printSeparatorToFile(outFile, 36);
            outFile.println("Thank you and have a nice day ahead!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printSeparatorToFile(PrintWriter outFile, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append("=");
        outFile.println(sb.toString());
    }

    private void printHeaderToFile(PrintWriter outFile) {
        String header = "= * === * === * === * === * ==";
        String title = "H & S Restaurant Meal Billing System";
        outFile.println(header);
        outFile.println(title);
        outFile.println(header);
        outFile.println();
    }
}

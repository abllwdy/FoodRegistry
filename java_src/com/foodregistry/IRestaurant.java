package com.foodregistry;

public interface IRestaurant {
    void processItemCode(char code, int quantity);
    String processPayment(float amount);
    void clearOrder();
    String generateDailyReport();
    String getMenuDisplay();
    String getCurrentOrderDisplay();
}

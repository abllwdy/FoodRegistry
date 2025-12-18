package com.foodregistry;

import com.foodregistry.security.AuditLog;
import com.foodregistry.security.AuthenticationService;
import com.foodregistry.security.UnauthorizedException;

public interface IRestaurant {
    void processItemCode(char code, int quantity) throws UnauthorizedException;
    String processPayment(float amount) throws UnauthorizedException;
    void clearOrder();
    String generateDailyReport() throws UnauthorizedException;
    String getMenuDisplay();
    String getCurrentOrderDisplay();
    
    // New security methods
    void modifyMenuPrice(char code, float newPrice) throws UnauthorizedException;
    String processRefund(int orderNumber) throws UnauthorizedException;
    
    AuthenticationService getAuthService();
    AuditLog getAuditLog();
}

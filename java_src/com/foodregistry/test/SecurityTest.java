package com.foodregistry.test;

import com.foodregistry.*;
import com.foodregistry.security.*;

public class SecurityTest {
    public static void main(String[] args) {
        System.out.println("Running Security Tests...");
        testLogin();
        testPermissions();
        testAccessControl();
        System.out.println("Tests Completed.");
    }

    static void testLogin() {
        System.out.println("\n[Test 1] Login Functionality");
        AuthenticationService auth = new AuthenticationService();
        
        // Test valid login
        if (auth.login("cashier01", "cash123")) {
             System.out.println("PASS: cashier01 login successful");
        } else {
             System.out.println("FAIL: cashier01 login failed");
        }
        
        // Test invalid login
        if (!auth.login("cashier01", "wrongpass")) {
             System.out.println("PASS: Invalid password rejected");
        } else {
             System.out.println("FAIL: Invalid password accepted");
        }
    }

    static void testPermissions() {
        System.out.println("\n[Test 2] Role Permissions");
        AuthenticationService auth = new AuthenticationService();
        
        auth.login("cashier01", "cash123");
        if (auth.hasPermission(Permission.PROCESS_ORDER) && !auth.hasPermission(Permission.MODIFY_MENU)) {
            System.out.println("PASS: Cashier has correct permissions");
        } else {
            System.out.println("FAIL: Cashier permissions incorrect");
        }

        auth.login("manager01", "mgr123");
        if (auth.hasPermission(Permission.MODIFY_MENU) && auth.hasPermission(Permission.MANAGE_USERS)) {
            System.out.println("PASS: Manager has correct permissions");
        } else {
            System.out.println("FAIL: Manager permissions incorrect");
        }
    }

    static void testAccessControl() {
        System.out.println("\n[Test 3] Access Control Integration");
        Restaurant restaurant = new Restaurant();
        AuthenticationService auth = restaurant.getAuthService();
        
        // Login as Cashier
        auth.login("cashier01", "cash123");
        
        try {
            restaurant.modifyMenuPrice('N', 100.0f);
            System.out.println("FAIL: Cashier was able to modify menu price");
        } catch (UnauthorizedException e) {
            System.out.println("PASS: Cashier blocked from modifying menu: " + e.getMessage());
        }

        // Login as Manager
        auth.login("manager01", "mgr123");
        try {
            restaurant.modifyMenuPrice('N', 10.0f);
            System.out.println("PASS: Manager modified menu price successfully");
        } catch (UnauthorizedException e) {
            System.out.println("FAIL: Manager blocked from modifying menu: " + e.getMessage());
        }
    }
}

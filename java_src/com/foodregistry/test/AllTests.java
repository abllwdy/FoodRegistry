package com.foodregistry.test;

import com.foodregistry.*;
import com.foodregistry.security.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class AllTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   Running Comprehensive Unit Test Suite  ");
        System.out.println("   Target: FoodRegistry System            ");
        System.out.println("==========================================");

        try {
            // 1. Data Models
            testMenuItem();
            
            // 2. Core Logic
            testOrderCalculationLogic();
            
            // 3. Security
            testAuthentication();
            
            // 4. Restaurant Operations (The main business logic)
            testRestaurantAccessControl();
            testOrderProcessing();
            testPaymentProcessing();
            testOrderClearing(); // New
            
            // 5. Reporting & Auditing
            testDailyReportGeneration(); // New
            testAuditLogRecording(); // New
            
            // 6. Manager Operations
            testManagerPrivileges();
            
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR IN TEST SUITE: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==========================================");
        System.out.println("   Test Summary");
        System.out.println("==========================================");
        System.out.println("PASSED: " + testsPassed);
        System.out.println("FAILED: " + testsFailed);
        
        if (testsFailed == 0) {
            System.out.println("\n[SUCCESS] ALL FUNCTIONS VERIFIED.");
        } else {
            System.out.println("\n[FAILURE] ERRORS DETECTED.");
        }
    }

    // --- Helper Assertions ---

    private static void assertEqual(Object expected, Object actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("PASS: " + testName);
            testsPassed++;
        } else {
            System.out.println("FAIL: " + testName + " [Expected: " + expected + ", Actual: " + actual + "]");
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            testsPassed++;
        } else {
            System.out.println("FAIL: " + testName);
            testsFailed++;
        }
    }

    // --- Test Modules ---

    private static void testMenuItem() {
        System.out.println("\n[Module: MenuItem Data Integrity]");
        MenuItem item = new MenuItem('T', "Test Item", 10.00f);
        
        assertEqual('T', item.getCode(), "Get Code");
        assertEqual("Test Item", item.getName(), "Get Name");
        assertEqual(10.00f, item.getPrice(), "Get Price");
        
        item.setPrice(15.50f);
        assertEqual(15.50f, item.getPrice(), "Set Price");
    }

    private static void testOrderCalculationLogic() {
        System.out.println("\n[Module: Order Calculation Logic]");
        List<MenuItem> menu = new ArrayList<>();
        menu.add(new MenuItem('A', "Item A", 10.00f));
        menu.add(new MenuItem('B', "Item B", 20.00f));
        
        Order order = new Order(2);
        
        // Scenario 1: Empty Order
        order.calculateTotals(menu);
        assertEqual(0.00f, order.getTotal(), "Empty Order Total is 0.00");
        
        // Scenario 2: Single Item Calculation
        order.addItem(0, 1); // 1 * 10.00
        order.calculateTotals(menu);
        assertEqual(10.00f, order.getSubtotal(), "Subtotal (1 item)");
        assertEqual(1.00f, order.getSST(), "SST (10% of 10.00)");
        assertEqual(11.00f, order.getTotal(), "Total (10.00 + 1.00)");
        
        // Scenario 3: Multiple Items Calculation
        order.addItem(1, 2); // 2 * 20.00 = 40.00. Total Subtotal = 50.00
        order.calculateTotals(menu);
        assertEqual(50.00f, order.getSubtotal(), "Subtotal (Multiple items)");
        assertEqual(5.00f, order.getSST(), "SST (10% of 50.00)");
        assertEqual(55.00f, order.getTotal(), "Total (50.00 + 5.00)");
        
        // Scenario 4: Negative Handling
        order.addItem(0, -100); // Should go to 0
        assertEqual(0, order.getQuantity(0), "Negative Add Resets to 0");
    }

    

    private static void testAuthentication() {
        System.out.println("\n[Module: Authentication Service]");
        AuthenticationService auth = new AuthenticationService();
        
        // 1. Invalid Login
        assertTrue(!auth.login("invalid", "invalid"), "Reject Invalid Credentials");
        
        // 2. Valid Login
        assertTrue(auth.login("cashier01", "cash123"), "Accept Valid Cashier Credentials");
        assertEqual("cashier01", auth.getCurrentUser().getUsername(), "Current User Set");
        assertEqual(Role.CASHIER, auth.getCurrentUser().getRole(), "Role Correctly Loaded");
        
        // 3. Logout
        auth.logout();
        assertTrue(auth.getCurrentUser() == null, "Logout Clears Session");
    }

    private static void testRestaurantAccessControl() {
        System.out.println("\n[Module: Restaurant Access Control]");
        Restaurant res = new Restaurant();
        
        // Ensure no one is logged in initially (new instance)
        // Try to process order without login
        boolean exceptionThrown = false;
        try {
            res.processItemCode('N', 1);
        } catch (UnauthorizedException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "Block Unauthenticated Order Processing");
        
        // Try to generate report without login
        exceptionThrown = false;
        try {
            res.generateDailyReport();
        } catch (UnauthorizedException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "Block Unauthenticated Report View");
    }

    private static void testOrderProcessing() {
        System.out.println("\n[Module: Order Processing Workflow]");
        Restaurant res = new Restaurant();
        res.getAuthService().login("cashier01", "cash123");
        
        try {
            // Add Item
            res.processItemCode('N', 1); // Assuming 'N' exists
            String display = res.getCurrentOrderDisplay();
            assertTrue(display.contains("Nasi Lemak"), "Item 'N' appears in Order Display");
            
            // Add Another Item
            res.processItemCode('C', 2); // Assuming 'C' exists
            display = res.getCurrentOrderDisplay();
            assertTrue(display.contains("Chicken Rice"), "Item 'C' appears in Order Display");
            
        } catch (Exception e) {
            System.out.println("FAIL: Exception in Order Processing: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testPaymentProcessing() {
        System.out.println("\n[Module: Payment Processing]");
        Restaurant res = new Restaurant();
        res.getAuthService().login("cashier01", "cash123");
        
        try {
            res.processItemCode('H', 1); // Hamburger 5.00
            // Subtotal 5.00, SST 0.50, Total 5.50
            
            // Insufficient Payment
            String result = res.processPayment(2.00f);
            assertTrue(result.contains("Insufficient"), "Detect Insufficient Payment");
            
            // Sufficient Payment
            result = res.processPayment(10.00f);
            assertTrue(result.contains("Change given"), "Calculate Change");
            assertTrue(result.contains("Receipt Order"), "Generate Receipt");
            
        } catch (Exception e) {
            System.out.println("FAIL: Exception in Payment: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void testOrderClearing() {
        System.out.println("\n[Module: Clear Order Function]");
        Restaurant res = new Restaurant();
        res.getAuthService().login("cashier01", "cash123");
        
        try {
            res.processItemCode('N', 5);
            String display = res.getCurrentOrderDisplay();
            assertTrue(!display.contains("Nothing"), "Order has items before clear");
            
            res.clearOrder();
            
            display = res.getCurrentOrderDisplay();
            assertTrue(display.contains("Nothing"), "Order is empty after clear");
            
        } catch (Exception e) {
            System.out.println("FAIL: Exception in Clear Order: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testDailyReportGeneration() {
        System.out.println("\n[Module: Daily Report Generation]");
        Restaurant res = new Restaurant();
        
        // 1. Try as Cashier (Should Fail or be restricted depending on requirements, here restricted)
        res.getAuthService().login("cashier01", "cash123");
        boolean exceptionThrown = false;
        try {
            res.generateDailyReport();
        } catch (UnauthorizedException e) {
            exceptionThrown = true;
        }
        // NOTE: In our AuthenticationService/Role, Cashier usually doesn't have VIEW_DAILY_REPORT
        // Let's verify that expectation.
        assertTrue(exceptionThrown, "Cashier Denied Report Access");
        
        // 2. Try as Manager
        res.getAuthService().logout();
        res.getAuthService().login("manager01", "mgr123");
        
        try {
            // Generate some sales first to make report interesting
            res.processItemCode('N', 1);
            res.processPayment(50.00f);
            
            String report = res.generateDailyReport();
            assertTrue(report.contains("DAILY MEAL SALES REPORT"), "Report Header Present");
            assertTrue(report.contains("Grand total"), "Financial Summary Present");
            assertTrue(report.contains("Nasi Lemak"), "Sold Item Listed");
            
        } catch (Exception e) {
            System.out.println("FAIL: Exception in Report Generation: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void testAuditLogRecording() {
        System.out.println("\n[Module: Audit Log System]");
        Restaurant res = new Restaurant();
        res.getAuthService().login("manager01", "mgr123");
        
        try {
            // Perform an auditable action
            res.modifyMenuPrice('M', 8.50f);
            
            // Check logs
            List<String> logs = res.getAuditLog().viewLog();
            boolean found = false;
            for(String log : logs) {
                if(log.contains("Modified Menu Item M")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Audit Log Recorded Price Change");
            
        } catch (Exception e) {
            System.out.println("FAIL: Exception in Audit Log: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testManagerPrivileges() {
        System.out.println("\n[Module: Manager Specific Operations]");
        Restaurant res = new Restaurant();
        
        // 1. Modify Price
        res.getAuthService().login("manager01", "mgr123");
        try {
            res.modifyMenuPrice('N', 20.00f);
            String menu = res.getMenuDisplay();
            assertTrue(menu.contains("20.00"), "Price Modification Applied");
            
            // Reset for sanity
            res.modifyMenuPrice('N', 9.00f);
        } catch (Exception e) {
            System.out.println("FAIL: Modify Price Error: " + e.getMessage());
            testsFailed++;
        }
        
        // 2. Refund
        try {
            // Need a transaction first
            res.processItemCode('F', 1);
            res.processPayment(50.00f);
            
            String refundResult = res.processRefund(1); // Refund 1st order
            assertTrue(refundResult.contains("Refund processed"), "Refund Execution");
            
        } catch (Exception e) {
             System.out.println("FAIL: Refund Error: " + e.getMessage());
             testsFailed++;
        }
    }
}

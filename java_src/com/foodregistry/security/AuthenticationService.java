package com.foodregistry.security;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.Scanner;

/**
 * Handles user authentication and permission checking.
 */
public class AuthenticationService {
    private Map<String, User> users;
    private User currentUser;
    private static final String USERS_FILE = "users.txt";

    public AuthenticationService() {
        users = new HashMap<>();
        loadUsers();
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            createDefaultUsers();
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String username = parts[0];
                    String passwordHash = parts[1];
                    try {
                        Role role = Role.valueOf(parts[2]);
                        String employeeId = parts[3];
                        users.put(username, new User(username, passwordHash, role, employeeId));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid role for user " + username);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultUsers();
        }
    }

    private void createDefaultUsers() {
        // cashier01 / cash123
        addUser("cashier01", hashPassword("cash123"), Role.CASHIER, "EMP001");
        // manager01 / mgr123
        addUser("manager01", hashPassword("mgr123"), Role.MANAGER, "MGR001");
        saveUsers();
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.println(user.getUsername() + "," + 
                             user.getPasswordHash() + "," + 
                             user.getRole() + "," + 
                             user.getEmployeeId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addUser(String username, String passwordHash, Role role, String employeeId) {
        users.put(username, new User(username, passwordHash, role, employeeId));
    }

    /**
     * Hashes a password using SHA-256.
     * @param plainText The plain text password.
     * @return The hashed password in hex format.
     */
    public String hashPassword(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Verifies user credentials and logs them in.
     * @param username The username.
     * @param password The plain text password.
     * @return true if login successful, false otherwise.
     */
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     * @return The current User object, or null if not logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if the current user has the specified permission.
     * @param permission The permission to check.
     * @return true if authorized, false otherwise.
     */
    public boolean hasPermission(Permission permission) {
        if (currentUser == null) return false;
        return currentUser.getRole().hasPermission(permission);
    }
}

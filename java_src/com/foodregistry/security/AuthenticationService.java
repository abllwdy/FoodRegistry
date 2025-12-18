package com.foodregistry.security;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication and permission checking.
 */
public class AuthenticationService {
    private Map<String, User> users;
    private User currentUser;

    public AuthenticationService() {
        users = new HashMap<>();
        // Add default users
        // cashier01 / cash123
        addUser("cashier01", hashPassword("cash123"), Role.CASHIER, "EMP001");
        // manager01 / mgr123
        addUser("manager01", hashPassword("mgr123"), Role.MANAGER, "MGR001");
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

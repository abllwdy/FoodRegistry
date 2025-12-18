package com.foodregistry.security;

/**
 * Represents a system user.
 */
public class User {
    private String username;
    private String passwordHash;
    private Role role;
    private String employeeId;

    public User(String username, String passwordHash, Role role, String employeeId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}

package com.foodregistry.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Records and retrieves user actions.
 */
public class AuditLog {
    private List<String> logs;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {
        this.logs = new ArrayList<>();
    }

    /**
     * Records a user action.
     * @param user The user performing the action.
     * @param action The description of the action.
     */
    public void record(User user, String action) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("%s - [%s] %s", timestamp, user.getUsername(), action);
        logs.add(logEntry);
        System.out.println("AUDIT: " + logEntry); // Also print to console
    }

    /**
     * Retrieves all log entries.
     * @return List of log strings.
     */
    public List<String> viewLog() {
        return new ArrayList<>(logs);
    }
}

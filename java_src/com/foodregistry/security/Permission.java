package com.foodregistry.security;

/**
 * Defines the available permissions in the system.
 */
public enum Permission {
    PROCESS_ORDER,          // Both roles
    VIEW_DAILY_REPORT,      // Both roles (current day only)
    MODIFY_MENU,           // Manager only
    PROCESS_REFUND,        // Manager only
    MANAGE_USERS,          // Manager only
    VIEW_HISTORICAL_REPORT // Manager only
}

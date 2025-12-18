package com.foodregistry.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the user roles and their associated permissions.
 */
public enum Role {
    CASHIER(EnumSet.of(Permission.PROCESS_ORDER, Permission.VIEW_DAILY_REPORT)),
    
    MANAGER(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}

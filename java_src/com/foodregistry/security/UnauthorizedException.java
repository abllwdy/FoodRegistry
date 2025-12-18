package com.foodregistry.security;

/**
 * Custom exception for unauthorized access attempts.
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}

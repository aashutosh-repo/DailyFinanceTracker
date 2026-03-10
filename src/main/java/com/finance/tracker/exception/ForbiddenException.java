package com.finance.tracker.exception;

/**
 * Forbidden Exception
 * Thrown when user tries to access a resource they don't have permission for
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

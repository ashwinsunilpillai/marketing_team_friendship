package com.marketing.exception;

/**
 * EmptySegmentException
 * Thrown when a segment contains no customers.
 */
public class EmptySegmentException extends Exception {
    public EmptySegmentException(String message) {
        super(message);
    }
    
    public EmptySegmentException(String message, Throwable cause) {
        super(message, cause);
    }
}

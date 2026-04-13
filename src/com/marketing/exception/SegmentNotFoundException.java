package com.marketing.exception;

/**
 * SegmentNotFoundException
 * Thrown when a segment is requested but not found in the database.
 */
public class SegmentNotFoundException extends Exception {
    public SegmentNotFoundException(String message) {
        super(message);
    }
    
    public SegmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

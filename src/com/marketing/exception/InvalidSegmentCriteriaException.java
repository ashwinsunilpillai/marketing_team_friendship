package com.marketing.exception;

/**
 * InvalidSegmentCriteriaException
 * Thrown when segment criteria is invalid or malformed.
 */
public class InvalidSegmentCriteriaException extends Exception {
    public InvalidSegmentCriteriaException(String message) {
        super(message);
    }
    
    public InvalidSegmentCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }
}

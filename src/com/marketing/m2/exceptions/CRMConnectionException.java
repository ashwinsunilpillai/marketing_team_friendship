package com.marketing.m2.exceptions;

/**
 * Category: MAJOR
 * Default message: Unable to connect to CRM system
 * Handling: Retry connection, use cached data temporarily
 */
public class CRMConnectionException extends Exception {
    public CRMConnectionException(String message) {
        super(message);
    }
}

package com.marketing.m2.exceptions;

/**
 * Category: MINOR
 * Default message: Customer data not found in CRM
 * Handling: Skip customer and continue processing
 */
public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}

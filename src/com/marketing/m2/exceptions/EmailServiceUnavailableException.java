package com.marketing.m2.exceptions;

public class EmailServiceUnavailableException extends Exception {
    public EmailServiceUnavailableException(String message) {
        super(message);
    }
}

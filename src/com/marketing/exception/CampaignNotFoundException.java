package com.marketing.exception;

/**
 * CampaignNotFoundException
 * Thrown when a campaign is requested but not found in the database.
 */
public class CampaignNotFoundException extends Exception {
    public CampaignNotFoundException(String message) {
        super(message);
    }
    
    public CampaignNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.marketing.exception;

/**
 * CampaignCreationException
 * Thrown when a campaign cannot be created due to invalid data or database errors.
 */
public class CampaignCreationException extends Exception {
    public CampaignCreationException(String message) {
        super(message);
    }
    
    public CampaignCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

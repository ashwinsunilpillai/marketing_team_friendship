package com.marketing.exception;

/**
 * CampaignStateException
 * Thrown when an invalid state transition is attempted on a campaign.
 */
public class CampaignStateException extends Exception {
    public CampaignStateException(String message) {
        super(message);
    }
    
    public CampaignStateException(String message, Throwable cause) {
        super(message, cause);
    }
}

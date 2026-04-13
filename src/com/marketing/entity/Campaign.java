package com.marketing.entity;

import java.time.LocalDate;

/**
 * Campaign Entity Class
 * Represents a marketing campaign in the system.
 * Maps directly to the campaigns table in the database.
 * GRASP: Information Expert (knows its own data)
 */
public class Campaign {
    private int campaignId;
    private String campaignName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double budget;
    private String status; // ACTIVE, PAUSED, COMPLETED
    private int segmentId;
    private String description;
    private int impressions;
    private int clicks;
    private int conversions;
    
    /**
     * Default constructor
     */
    public Campaign() {
    }
    
    /**
     * Constructor with essential fields
     */
    public Campaign(String campaignName, LocalDate startDate, LocalDate endDate, double budget, String status, int segmentId) {
        this.campaignName = campaignName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = status;
        this.segmentId = segmentId;
        this.impressions = 0;
        this.clicks = 0;
        this.conversions = 0;
    }
    
    /**
     * Full constructor
     */
    public Campaign(int campaignId, String campaignName, LocalDate startDate, LocalDate endDate, 
                   double budget, String status, int segmentId, String description) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = status;
        this.segmentId = segmentId;
        this.description = description;
        this.impressions = 0;
        this.clicks = 0;
        this.conversions = 0;
    }
    
    // Getters and Setters
    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }
    
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
    
    public int getConversions() { return conversions; }
    public void setConversions(int conversions) { this.conversions = conversions; }
    
    @Override
    public String toString() {
        return "Campaign{" +
                "campaignId=" + campaignId +
                ", campaignName='" + campaignName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", budget=" + budget +
                ", status='" + status + '\'' +
                ", segmentId=" + segmentId +
                '}';
    }
}

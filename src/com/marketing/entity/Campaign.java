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
    private String campaignTitle;
    private String campaignType; // EMAIL, ADS, SOCIAL_MEDIA, MULTI_CHANNEL
    private String targetVehicleSegment;
    private double campaignBudget;
    private String targetLeads; // JSON
    private LocalDate startDate;
    private LocalDate endDate;
    private double campaignRoi;
    private String campaignResults; // JSON
    
    // Legacy fields for backward compatibility
    private String campaignName;
    private double budget;
    private String status;
    private int segmentId;
    private String description;
    private int impressions;
    private int clicks;
    private int conversions;
    private int leadTarget;
    private int leadsGenerated;

    /**
     * Default constructor
     */
    public Campaign() {
    }

    /**
     * Constructor with essential fields (new schema)
     */
    public Campaign(String campaignTitle, String campaignType, LocalDate startDate, LocalDate endDate, 
                   double campaignBudget) {
        this.campaignTitle = campaignTitle;
        this.campaignType = campaignType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.campaignBudget = campaignBudget;
        this.campaignRoi = 0.0;
    }

    /**
     * Full constructor (new schema)
     */
    public Campaign(int campaignId, String campaignTitle, String campaignType, String targetVehicleSegment,
                   double campaignBudget, String targetLeads, LocalDate startDate, LocalDate endDate, 
                   double campaignRoi, String campaignResults) {
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.campaignType = campaignType;
        this.targetVehicleSegment = targetVehicleSegment;
        this.campaignBudget = campaignBudget;
        this.targetLeads = targetLeads;
        this.startDate = startDate;
        this.endDate = endDate;
        this.campaignRoi = campaignRoi;
        this.campaignResults = campaignResults;
    }

    // New schema getters/setters
    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    
    public String getCampaignTitle() { return campaignTitle; }
    public void setCampaignTitle(String campaignTitle) { this.campaignTitle = campaignTitle; }
    
    public String getCampaignType() { return campaignType != null ? campaignType : "EMAIL"; }
    public void setCampaignType(String campaignType) { this.campaignType = campaignType; }
    
    public String getTargetVehicleSegment() { return targetVehicleSegment; }
    public void setTargetVehicleSegment(String targetVehicleSegment) { this.targetVehicleSegment = targetVehicleSegment; }
    
    public double getCampaignBudget() { return campaignBudget; }
    public void setCampaignBudget(double campaignBudget) { this.campaignBudget = campaignBudget; }
    
    public String getTargetLeads() { return targetLeads; }
    public void setTargetLeads(String targetLeads) { this.targetLeads = targetLeads; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public double getCampaignRoi() { return campaignRoi; }
    public void setCampaignRoi(double campaignRoi) { this.campaignRoi = campaignRoi; }
    
    public String getCampaignResults() { return campaignResults; }
    public void setCampaignResults(String campaignResults) { this.campaignResults = campaignResults; }
    
    // Legacy backward compatibility getters/setters
    public String getCampaignName() { return campaignTitle != null ? campaignTitle : campaignName; }
    public void setCampaignName(String campaignName) { 
        this.campaignName = campaignName;
        if (this.campaignTitle == null) this.campaignTitle = campaignName;
    }
    
    public double getBudget() { return campaignBudget; }
    public void setBudget(double budget) { 
        this.budget = budget;
        this.campaignBudget = budget;
    }
    
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
    
    public int getLeadTarget() { return leadTarget; }
    public void setLeadTarget(int leadTarget) { this.leadTarget = leadTarget; }
    
    public int getLeadsGenerated() { return leadsGenerated; }
    public void setLeadsGenerated(int leadsGenerated) { this.leadsGenerated = leadsGenerated; }

    @Override
    public String toString() {
        return "Campaign{" +
                "campaignId=" + campaignId +
                ", campaignTitle='" + campaignTitle + '\'' +
                ", campaignType='" + campaignType + '\'' +
                ", campaignBudget=" + campaignBudget +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", campaignRoi=" + campaignRoi +
                '}';
    }
}

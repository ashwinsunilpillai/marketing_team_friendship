package com.marketing.m3.analytics;

public class CampaignAnalyticsItem {
    private final int campaignId;
    private final String campaignName;
    private final int impressions;
    private final int clicks;
    private final int conversions;
    private final double budget;
    private final double ctr;
    private final double roi;
    private final String campaignType;
    private final String status;

    public CampaignAnalyticsItem(int campaignId, String campaignName, int impressions, int clicks,
            int conversions, double budget, double ctr, double roi) {
        this(campaignId, campaignName, impressions, clicks, conversions, budget, ctr, roi, "EMAIL", "ACTIVE");
    }

    public CampaignAnalyticsItem(int campaignId, String campaignName, int impressions, int clicks,
            int conversions, double budget, double ctr, double roi, String campaignType, String status) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.impressions = impressions;
        this.clicks = clicks;
        this.conversions = conversions;
        this.budget = budget;
        this.ctr = ctr;
        this.roi = roi;
        this.campaignType = campaignType;
        this.status = status;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public int getImpressions() {
        return impressions;
    }

    public int getClicks() {
        return clicks;
    }

    public int getConversions() {
        return conversions;
    }

    public double getBudget() {
        return budget;
    }

    public double getCtr() {
        return ctr;
    }

    public double getRoi() {
        return roi;
    }

    public String getCampaignType() {
        return campaignType;
    }

    public String getStatus() {
        return status;
    }
}

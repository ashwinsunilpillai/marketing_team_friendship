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

    public CampaignAnalyticsItem(int campaignId, String campaignName, int impressions, int clicks,
                                 int conversions, double budget, double ctr, double roi) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.impressions = impressions;
        this.clicks = clicks;
        this.conversions = conversions;
        this.budget = budget;
        this.ctr = ctr;
        this.roi = roi;
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
}

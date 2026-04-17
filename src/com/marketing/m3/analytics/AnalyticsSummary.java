package com.marketing.m3.analytics;

import java.util.Collections;
import java.util.List;

public class AnalyticsSummary {
    private final int campaignCount;
    private final int totalImpressions;
    private final int totalClicks;
    private final int totalConversions;
    private final double totalBudget;
    private final double ctr;
    private final double roi;
    private final List<CampaignAnalyticsItem> campaignItems;

    public AnalyticsSummary(int campaignCount, int totalImpressions, int totalClicks, int totalConversions,
                            double totalBudget, double ctr, double roi,
                            List<CampaignAnalyticsItem> campaignItems) {
        this.campaignCount = campaignCount;
        this.totalImpressions = totalImpressions;
        this.totalClicks = totalClicks;
        this.totalConversions = totalConversions;
        this.totalBudget = totalBudget;
        this.ctr = ctr;
        this.roi = roi;
        this.campaignItems = campaignItems;
    }

    public int getCampaignCount() {
        return campaignCount;
    }

    public int getTotalImpressions() {
        return totalImpressions;
    }

    public int getTotalClicks() {
        return totalClicks;
    }

    public int getTotalConversions() {
        return totalConversions;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public double getCtr() {
        return ctr;
    }

    public double getRoi() {
        return roi;
    }

    public List<CampaignAnalyticsItem> getCampaignItems() {
        return Collections.unmodifiableList(campaignItems);
    }
}

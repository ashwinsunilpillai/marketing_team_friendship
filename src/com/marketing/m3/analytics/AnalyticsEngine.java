package com.marketing.m3.analytics;

import com.marketing.entity.Campaign;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsEngine {
    private static final double ESTIMATED_REVENUE_PER_CONVERSION = 120.0;

    private final List<AnalyticsObserver> observers = new ArrayList<>();
    private AnalyticsSummary latestSummary = new AnalyticsSummary(0, 0, 0, 0, 0.0, 0.0, 0.0, new ArrayList<>());

    public void attach(AnalyticsObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(AnalyticsObserver observer) {
        observers.remove(observer);
    }

    public AnalyticsSummary getLatestSummary() {
        return latestSummary;
    }

    public void refresh(List<Campaign> campaigns) {
        latestSummary = calculateSummary(campaigns);
        notifyObservers();
    }

    private void notifyObservers() {
        for (AnalyticsObserver observer : observers) {
            observer.onAnalyticsUpdated(latestSummary);
        }
    }

    private AnalyticsSummary calculateSummary(List<Campaign> campaigns) {
        if (campaigns == null) {
            campaigns = new ArrayList<>();
        }

        int totalImpressions = 0;
        int totalClicks = 0;
        int totalConversions = 0;
        double totalBudget = 0.0;
        List<CampaignAnalyticsItem> items = new ArrayList<>();

        for (Campaign campaign : campaigns) {
            int impressions = Math.max(0, campaign.getImpressions());
            int clicks = Math.max(0, campaign.getClicks());
            int conversions = Math.max(0, campaign.getConversions());
            double budget = Math.max(0.0, campaign.getBudget());

            double ctr = impressions > 0 ? (clicks * 100.0) / impressions : 0.0;
            double estimatedRevenue = conversions * ESTIMATED_REVENUE_PER_CONVERSION;
            double roi = budget > 0 ? ((estimatedRevenue - budget) * 100.0) / budget : 0.0;

            items.add(new CampaignAnalyticsItem(
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    impressions,
                    clicks,
                    conversions,
                    budget,
                    ctr,
                    roi,
                    campaign.getCampaignType(),
                    campaign.getStatus()));

            totalImpressions += impressions;
            totalClicks += clicks;
            totalConversions += conversions;
            totalBudget += budget;
        }

        double overallCtr = totalImpressions > 0 ? (totalClicks * 100.0) / totalImpressions : 0.0;
        double overallRevenue = totalConversions * ESTIMATED_REVENUE_PER_CONVERSION;
        double overallRoi = totalBudget > 0 ? ((overallRevenue - totalBudget) * 100.0) / totalBudget : 0.0;

        return new AnalyticsSummary(
                campaigns.size(),
                totalImpressions,
                totalClicks,
                totalConversions,
                totalBudget,
                overallCtr,
                overallRoi,
                items);
    }
}

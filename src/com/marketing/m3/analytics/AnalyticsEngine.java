package com.marketing.m3.analytics;

import com.marketing.entity.Campaign;
import com.marketing.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsEngine {
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

        Map<Integer, MetricsAggregate> metricsByCampaign = fetchMetricsAggregates();

        int totalImpressions = 0;
        int totalClicks = 0;
        int totalConversions = 0;
        double totalBudget = 0.0;
        double totalRevenue = 0.0;
        List<CampaignAnalyticsItem> items = new ArrayList<>();

        for (Campaign campaign : campaigns) {
            MetricsAggregate agg = metricsByCampaign.get(campaign.getCampaignId());

            int impressions = Math.max(0, agg != null ? agg.impressions : campaign.getImpressions());
            int clicks = Math.max(0, agg != null ? agg.clicks : campaign.getClicks());
            int conversions = Math.max(0, agg != null ? agg.conversions : campaign.getConversions());
            double revenue = Math.max(0.0, agg != null ? agg.revenue : 0.0);

            double budget = campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget();
            budget = Math.max(0.0, budget);

            double ctr = impressions > 0 ? (clicks * 100.0) / impressions : 0.0;
            // ROI from persisted campaign metrics revenue.
            double roi = budget > 0 ? ((revenue - budget) / budget) * 100.0 : 0.0;

            items.add(new CampaignAnalyticsItem(
                    campaign.getCampaignId(),
                    campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName(),
                    impressions,
                    clicks,
                    conversions,
                    budget,
                    ctr,
                    roi,
                    campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL",
                    campaign.getStatus() != null ? campaign.getStatus() : "ACTIVE"));

            totalImpressions += impressions;
            totalClicks += clicks;
            totalConversions += conversions;
            totalBudget += budget;
            totalRevenue += revenue;
        }

        double overallCtr = totalImpressions > 0 ? (totalClicks * 100.0) / totalImpressions : 0.0;
        double overallRoi = totalBudget > 0 ? ((totalRevenue - totalBudget) / totalBudget) * 100.0 : 0.0;

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

    private Map<Integer, MetricsAggregate> fetchMetricsAggregates() {
        Map<Integer, MetricsAggregate> data = new HashMap<>();
        String sql = "SELECT campaign_id, SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, " +
                "SUM(conversions) AS total_conversions, SUM(revenue_generated) AS total_revenue " +
                "FROM campaign_metrics GROUP BY campaign_id";

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int campaignId = rs.getInt("campaign_id");
                data.put(campaignId, new MetricsAggregate(
                        rs.getInt("total_impressions"),
                        rs.getInt("total_clicks"),
                        rs.getInt("total_conversions"),
                        rs.getDouble("total_revenue")));
            }
        } catch (SQLException ignore) {
            // Keep dashboard functional even if metrics table is unavailable.
        }

        return data;
    }

    private static class MetricsAggregate {
        final int impressions;
        final int clicks;
        final int conversions;
        final double revenue;

        MetricsAggregate(int impressions, int clicks, int conversions, double revenue) {
            this.impressions = impressions;
            this.clicks = clicks;
            this.conversions = conversions;
            this.revenue = revenue;
        }
    }
}

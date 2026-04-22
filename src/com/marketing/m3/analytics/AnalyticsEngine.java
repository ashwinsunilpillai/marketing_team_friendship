package com.marketing.m3.analytics;

import com.marketing.entity.Campaign;
import com.marketing.util.DBUtil;

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
            int conversionsFromMetrics = Math.max(0, agg != null ? agg.conversions : 0);
            int conversionsFromCampaign = Math.max(0, campaign.getLeadsGenerated());
            int conversions = Math.max(conversionsFromMetrics, conversionsFromCampaign);
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
        try {
            Object marketingSubsystem = DBUtil.getInstance().getMarketingSubsystem();
            if (marketingSubsystem == null) {
                return data;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, "campaign_metrics", new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    int campaignId = asInt(row.get("campaign_id"), 0);
                    if (campaignId <= 0) {
                        continue;
                    }

                    MetricsAggregate existing = data.get(campaignId);
                    if (existing == null) {
                        existing = new MetricsAggregate(0, 0, 0, 0.0);
                        data.put(campaignId, existing);
                    }

                    existing.impressions += asInt(row.get("impressions"), 0);
                    existing.clicks += asInt(row.get("clicks"), 0);
                    existing.conversions += asInt(row.get("conversions"), 0);
                    existing.revenue += asDouble(row.get("revenue_generated"), 0.0);
                }
            }
        } catch (Exception ignore) {
            // Keep dashboard functional even if metrics table is unavailable.
        }

        return data;
    }

    private static class MetricsAggregate {
        int impressions;
        int clicks;
        int conversions;
        double revenue;

        MetricsAggregate(int impressions, int clicks, int conversions, double revenue) {
            this.impressions = impressions;
            this.clicks = clicks;
            this.conversions = conversions;
            this.revenue = revenue;
        }
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }
}

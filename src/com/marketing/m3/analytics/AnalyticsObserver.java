package com.marketing.m3.analytics;

public interface AnalyticsObserver {
    void onAnalyticsUpdated(AnalyticsSummary summary);
}

package com.marketing.m3.reporting;

import com.marketing.m3.analytics.AnalyticsSummary;
import com.marketing.m3.analytics.CampaignAnalyticsItem;

import java.text.DecimalFormat;

public class ReportBuilder {
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private final Report.MutableReportSections sections = new Report.MutableReportSections();
    private String title = "Analytics Report";

    public ReportBuilder withTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        return this;
    }

    public ReportBuilder withSummary(AnalyticsSummary summary) {
        StringBuilder content = new StringBuilder();
        content.append("Campaigns: ").append(summary.getCampaignCount()).append('\n');
        content.append("Impressions: ").append(summary.getTotalImpressions()).append('\n');
        content.append("Clicks: ").append(summary.getTotalClicks()).append('\n');
        content.append("Conversions: ").append(summary.getTotalConversions()).append('\n');
        content.append("CTR: ").append(decimalFormat.format(summary.getCtr())).append("%\n");
        content.append("ROI: ").append(decimalFormat.format(summary.getRoi())).append("%");
        sections.add("Executive Summary", content.toString());
        return this;
    }

    public ReportBuilder withKpiTargets(double targetCtr, double targetRoi, AnalyticsSummary summary) {
        StringBuilder content = new StringBuilder();
        content.append("Target CTR: ").append(decimalFormat.format(targetCtr)).append("%\n");
        content.append("Actual CTR: ").append(decimalFormat.format(summary.getCtr())).append("%\n");
        content.append("CTR Status: ").append(summary.getCtr() >= targetCtr ? "On Track" : "Below Target").append("\n\n");
        content.append("Target ROI: ").append(decimalFormat.format(targetRoi)).append("%\n");
        content.append("Actual ROI: ").append(decimalFormat.format(summary.getRoi())).append("%\n");
        content.append("ROI Status: ").append(summary.getRoi() >= targetRoi ? "On Track" : "Below Target");
        sections.add("KPI Targets vs Actuals", content.toString());
        return this;
    }

    public ReportBuilder withCampaignBreakdown(AnalyticsSummary summary) {
        StringBuilder content = new StringBuilder();
        for (CampaignAnalyticsItem item : summary.getCampaignItems()) {
            content.append("#")
                    .append(item.getCampaignId())
                    .append(" - ")
                    .append(item.getCampaignName())
                    .append(": CTR ")
                    .append(decimalFormat.format(item.getCtr()))
                    .append("%, ROI ")
                    .append(decimalFormat.format(item.getRoi()))
                    .append("%")
                    .append('\n');
        }
        if (summary.getCampaignItems().isEmpty()) {
            content.append("No campaign analytics available.");
        }
        sections.add("Campaign Breakdown", content.toString().trim());
        return this;
    }

    public Report build() {
        if (sections.toList().isEmpty()) {
            throw new IllegalStateException("Report must contain at least one section");
        }
        return new Report(title, sections.toList());
    }
}
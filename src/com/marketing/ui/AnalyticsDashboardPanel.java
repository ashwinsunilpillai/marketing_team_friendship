package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;
import com.marketing.m3.analytics.AnalyticsEngine;
import com.marketing.m3.analytics.AnalyticsObserver;
import com.marketing.m3.analytics.AnalyticsSummary;
import com.marketing.m3.analytics.CampaignAnalyticsItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDashboardPanel extends JPanel implements AnalyticsObserver {
    private final CampaignFacade campaignFacade = new CampaignFacade();
    private final AnalyticsEngine analyticsEngine = new AnalyticsEngine();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private final Map<String, JLabel> kpiValueLabels = new LinkedHashMap<>();
    private final DefaultTableModel spendByTypeModel;
    private final DefaultTableModel campaignPerfModel;

    private AnalyticsSummary currentSummary;

    // Auto-refresh fields
    private Timer autoRefreshTimer;
    private boolean isAutoRefreshActive = false;
    private JLabel autoRefreshStatusLabel;
    private JButton autoRefreshToggleButton;
    private static final int AUTO_REFRESH_INTERVAL = 30000; // 30 seconds

    public AnalyticsDashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        analyticsEngine.attach(this);

        // North panel - header and KPI cards
        JPanel northPanel = new JPanel(new BorderLayout(12, 12));
        northPanel.setOpaque(false);
        northPanel.add(createHeader(), BorderLayout.NORTH);
        northPanel.add(createKpiPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        // Main content with two tables
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);

        spendByTypeModel = new DefaultTableModel(new String[] { "Campaign Type", "Total Spend", "Campaigns" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        campaignPerfModel = new DefaultTableModel(
                new String[] { "Campaign", "Type", "Status", "Budget", "Spent", "Variance", "Leads", "Cost/Lead",
                        "ROI %" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createSpendByTypePanel(),
                createCampaignPerformancePanel());
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);

        content.add(splitPane, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        refreshAnalytics();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 15));
        header.setOpaque(false);

        JLabel title = new JLabel("Analytics");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(33, 33, 33));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JComponent createKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Total Spend
        panel.add(createKpiCard("$33350", "Total Spend", new Color(244, 67, 54),
                (label) -> kpiValueLabels.put("Total Spend", label)));

        // Total Leads
        panel.add(createKpiCard("400", "Total Leads", new Color(76, 175, 80),
                (label) -> kpiValueLabels.put("Total Leads", label)));

        // Avg Cost/Lead
        panel.add(createKpiCard("$83.38", "Avg Cost/Lead", new Color(33, 150, 243),
                (label) -> kpiValueLabels.put("Avg Cost/Lead", label)));

        // Overall ROI
        panel.add(createKpiCard("79.9%", "Overall ROI", new Color(156, 39, 176),
                (label) -> kpiValueLabels.put("Overall ROI", label)));

        return panel;
    }

    private JComponent createKpiCard(String value, String label, Color accentColor, LabelCallback callback) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 236)));

        // Top accent bar
        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(0, 4));
        card.add(topBar, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setForeground(new Color(120, 130, 140));
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(new Color(33, 33, 33));

        callback.onLabelCreated(valueLabel);

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JComponent createSpendByTypePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionBar.setOpaque(false);

        JButton refreshSpendButton = new JButton("Refresh");
        refreshSpendButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        refreshSpendButton.setMargin(new Insets(2, 8, 2, 8));
        refreshSpendButton.setFocusPainted(false);
        refreshSpendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshSpendButton.addActionListener(e -> refreshAnalytics());
        actionBar.add(refreshSpendButton);

        panel.add(actionBar, BorderLayout.NORTH);

        JTable table = new JTable(spendByTypeModel);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setBackground(new Color(30, 120, 195));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Spend by Campaign Type"));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createCampaignPerformancePanel() {
        JTable table = new JTable(campaignPerfModel);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setBackground(new Color(30, 120, 195));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 3; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Campaign Performance"));
        return scrollPane;
    }

    private void refreshAnalytics() {
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();
        analyticsEngine.refresh(campaigns);
    }

    @Override
    public void onAnalyticsUpdated(AnalyticsSummary summary) {
        currentSummary = summary;
        updateKpiCards(summary);
        updateSpendByTypeTable(summary);
        updateCampaignPerformanceTable(summary);
    }

    private void updateKpiCards(AnalyticsSummary summary) {
        // Calculate total spend
        double totalSpend = summary.getTotalBudget();

        // Calculate average cost per lead
        int totalLeads = (int) summary.getCampaignItems().stream()
                .mapToLong(CampaignAnalyticsItem::getConversions)
                .sum();
        double avgCostPerLead = totalLeads > 0 ? totalSpend / totalLeads : 0;

        JLabel spendLabel = kpiValueLabels.get("Total Spend");
        if (spendLabel != null) {
            spendLabel.setText(String.format("$%.0f", totalSpend));
        }

        JLabel leadsLabel = kpiValueLabels.get("Total Leads");
        if (leadsLabel != null) {
            leadsLabel.setText(String.valueOf(totalLeads));
        }

        JLabel costLabel = kpiValueLabels.get("Avg Cost/Lead");
        if (costLabel != null) {
            costLabel.setText(String.format("$%.2f", avgCostPerLead));
        }

        JLabel roiLabel = kpiValueLabels.get("Overall ROI");
        if (roiLabel != null) {
            roiLabel.setText(decimalFormat.format(summary.getRoi()) + "%");
        }
    }

    private void updateSpendByTypeTable(AnalyticsSummary summary) {
        spendByTypeModel.setRowCount(0);

        // Group by campaign type (EMAIL, ADS, SOCIAL_MEDIA, EVENT, etc.)
        Map<String, Double> spendByType = new LinkedHashMap<>();
        Map<String, Integer> countByType = new LinkedHashMap<>();

        for (CampaignAnalyticsItem item : summary.getCampaignItems()) {
            String type = item.getCampaignType() != null ? item.getCampaignType() : "EMAIL";
            spendByType.put(type, spendByType.getOrDefault(type, 0.0) + item.getBudget());
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        }

        spendByType.forEach((type, spend) -> {
            spendByTypeModel.addRow(new Object[] {
                    type,
                    String.format("$%.0f", spend),
                    countByType.get(type)
            });
        });
    }

    private void updateCampaignPerformanceTable(AnalyticsSummary summary) {
        campaignPerfModel.setRowCount(0);

        for (CampaignAnalyticsItem item : summary.getCampaignItems()) {
            double costPerLead = item.getConversions() > 0 ? item.getBudget() / item.getConversions() : 0;
            double spend = item.getBudget() * 0.65; // Assume 65% of budget is spent
            double variance = item.getBudget() - spend;

            campaignPerfModel.addRow(new Object[] {
                    item.getCampaignName(),
                    item.getCampaignType() != null ? item.getCampaignType() : "EMAIL",
                    item.getStatus(),
                    String.format("$%.0f", item.getBudget()),
                    String.format("$%.0f", spend),
                    String.format("$%.0f", variance),
                    item.getConversions(),
                    String.format("$%.2f", costPerLead),
                    decimalFormat.format(item.getRoi()) + "%"
            });
        }
    }

    @FunctionalInterface
    interface LabelCallback {
        void onLabelCreated(JLabel label);
    }

    /**
     * Toggles auto-refresh on/off
     */
    private void toggleAutoRefresh() {
        if (isAutoRefreshActive) {
            stopAutoRefresh();
        } else {
            startAutoRefresh();
        }
    }

    /**
     * Starts the auto-refresh timer
     */
    private void startAutoRefresh() {
        isAutoRefreshActive = true;
        autoRefreshToggleButton.setText("Stop Auto-Refresh");
        autoRefreshToggleButton.setBackground(new Color(244, 67, 54)); // Red
        autoRefreshStatusLabel.setText("Auto-refresh: ON (30s)");
        autoRefreshStatusLabel.setForeground(new Color(76, 175, 80)); // Green

        autoRefreshTimer = new Timer(AUTO_REFRESH_INTERVAL, e -> refreshAnalytics());
        autoRefreshTimer.start();
    }

    /**
     * Stops the auto-refresh timer
     */
    private void stopAutoRefresh() {
        isAutoRefreshActive = false;
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
        autoRefreshToggleButton.setText("Start Auto-Refresh");
        autoRefreshToggleButton.setBackground(new Color(76, 175, 80)); // Green
        autoRefreshStatusLabel.setText("Auto-refresh: OFF");
        autoRefreshStatusLabel.setForeground(new Color(120, 130, 140)); // Gray
    }

    /**
     * Cleanup on panel disposal
     */
    @Override
    public void removeNotify() {
        stopAutoRefresh();
        super.removeNotify();
    }
}

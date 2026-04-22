package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;
import com.marketing.util.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.*;

/**
 * AnalyticsPanel - Displays comprehensive analytics and performance metrics
 */
public class AnalyticsPanel extends JPanel {
    private CampaignFacade campaignFacade;
    private DBUtil dbUtil;
    private JLabel totalSpendLabel;
    private JLabel totalLeadsLabel;
    private JLabel avgCostPerLeadLabel;
    private JLabel overallROILabel;
    private JTable spendByTypeTable;
    private JTable performanceTable;
    private DefaultTableModel spendByTypeModel;
    private DefaultTableModel performanceModel;

    public AnalyticsPanel() {
        this.campaignFacade = new CampaignFacade();
        this.dbUtil = DBUtil.getInstance();
        initializeUI();
        loadAnalytics();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        // Header and Metrics Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Content Panel
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Spend by Campaign Type Tab
        JPanel spendPanel = createSpendByTypePanel();
        tabbedPane.addTab("Spend by Campaign Type", spendPanel);

        // Campaign Performance Tab
        JPanel performancePanel = createPerformancePanel();
        tabbedPane.addTab("Campaign Performance", performancePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Metrics cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel card1 = createMetricCard("$33350", "Total Spend", new Color(244, 67, 54),
                (label) -> totalSpendLabel = label);
        metricsPanel.add(card1);

        JPanel card2 = createMetricCard("400", "Total Leads", new Color(76, 175, 80),
                (label) -> totalLeadsLabel = label);
        metricsPanel.add(card2);

        JPanel card3 = createMetricCard("$83.38", "Avg Cost/Lead", new Color(33, 150, 243),
                (label) -> avgCostPerLeadLabel = label);
        metricsPanel.add(card3);

        JPanel card4 = createMetricCard("79.9%", "Overall ROI", new Color(156, 39, 176),
                (label) -> overallROILabel = label);
        metricsPanel.add(card4);

        panel.add(metricsPanel, BorderLayout.CENTER);

        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshButton.setBackground(new Color(63, 81, 181));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadAnalytics());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMetricCard(String value, String label, Color accentColor,
            java.util.function.Consumer<JLabel> labelCapture) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 130, 145));

        JPanel leftBorder = new JPanel();
        leftBorder.setBackground(accentColor);
        leftBorder.setPreferredSize(new Dimension(4, 0));

        card.add(leftBorder, BorderLayout.WEST);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        labelCapture.accept(valueLabel);
        return card;
    }

    private JPanel createSpendByTypePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "Campaign Type", "Total Spend", "Campaigns" };
        spendByTypeModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        spendByTypeTable = new JTable(spendByTypeModel);
        spendByTypeTable.setRowHeight(32);
        spendByTypeTable.setBackground(Color.WHITE);

        // Style header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(63, 81, 181));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < spendByTypeTable.getColumnCount(); i++) {
            spendByTypeTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Alternate row colors
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row % 2 == 0) {
                    c.setBackground(new Color(245, 247, 250));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < spendByTypeTable.getColumnCount(); i++) {
            spendByTypeTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(spendByTypeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "Campaign", "Type", "Status", "Budget", "Spent", "Variance", "Leads", "Cost/Lead",
                "ROI %" };
        performanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        performanceTable = new JTable(performanceModel);
        performanceTable.setRowHeight(32);
        performanceTable.setBackground(Color.WHITE);

        // Style header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(63, 81, 181));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < performanceTable.getColumnCount(); i++) {
            performanceTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Status renderer
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel label = (JLabel) c;

                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "ACTIVE":
                            label.setBackground(new Color(200, 230, 201));
                            label.setForeground(new Color(27, 94, 32));
                            break;
                        case "COMPLETED":
                            label.setBackground(new Color(225, 190, 231));
                            label.setForeground(new Color(74, 20, 140));
                            break;
                        case "PLANNED":
                            label.setBackground(new Color(255, 243, 224));
                            label.setForeground(new Color(230, 124, 115));
                            break;
                        case "PAUSED":
                            label.setBackground(new Color(255, 235, 205));
                            label.setForeground(new Color(255, 152, 0));
                            break;
                        default:
                            label.setBackground(Color.WHITE);
                    }
                }

                return label;
            }
        };
        performanceTable.getColumnModel().getColumn(2).setCellRenderer(statusRenderer);

        // ROI % renderer (color coding)
        DefaultTableCellRenderer roiRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel label = (JLabel) c;

                if (value != null) {
                    try {
                        double roi = Double.parseDouble(value.toString().replaceAll("%", ""));
                        if (roi >= 50) {
                            label.setForeground(new Color(76, 175, 80)); // Green
                        } else if (roi >= 25) {
                            label.setForeground(new Color(255, 152, 0)); // Orange
                        } else {
                            label.setForeground(new Color(244, 67, 54)); // Red
                        }
                    } catch (Exception e) {
                        label.setForeground(Color.BLACK);
                    }
                }

                return label;
            }
        };
        performanceTable.getColumnModel().getColumn(8).setCellRenderer(roiRenderer);

        JScrollPane scrollPane = new JScrollPane(performanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadAnalytics() {
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();
        
        // Fetch metrics from campaign_metrics table
        Map<Integer, CampaignMetrics> metricsMap = fetchCampaignMetrics();

        double totalSpend = 0;
        int totalLeads = 0;
        double totalBudget = 0;
        double totalRevenue = 0;

        Map<String, Double> spendByType = new LinkedHashMap<>();
        Map<String, Integer> campaignCountByType = new LinkedHashMap<>();
        Map<String, Double> revenueByType = new LinkedHashMap<>();

        for (Campaign campaign : campaigns) {
            double budget = campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget();
            CampaignMetrics metrics = metricsMap.getOrDefault(campaign.getCampaignId(), new CampaignMetrics(0, 0, 0, 0));
            
            double spend = metrics.revenue > 0 ? metrics.revenue : budget * 0.65;
            int leads = metrics.conversions > 0 ? metrics.conversions : 0;
            
            totalSpend += spend;
            totalLeads += leads;
            totalBudget += budget;
            totalRevenue += metrics.revenue;

            // Group by type
            String type = campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL";
            spendByType.put(type, spendByType.getOrDefault(type, 0.0) + spend);
            campaignCountByType.put(type, campaignCountByType.getOrDefault(type, 0) + 1);
            revenueByType.put(type, revenueByType.getOrDefault(type, 0.0) + metrics.revenue);
        }

        // Calculate metrics
        double avgCostPerLead = totalLeads > 0 ? totalSpend / totalLeads : 0;
        // ROI: if no conversions across all campaigns, show 0% (pending data)
        // Otherwise calculate based on spend vs budget
        double overallROI;
        if (totalLeads == 0 && totalRevenue == 0) {
            // No metrics data collected yet across all campaigns
            overallROI = 0.0;
        } else if (totalBudget > 0) {
            // Has metrics data - calculate ROI
            overallROI = ((totalSpend - totalBudget) / totalBudget) * 100;
        } else {
            overallROI = 0.0;
        }

        // Update metric labels
        if (totalSpendLabel != null)
            totalSpendLabel.setText("$" + String.format("%.0f", totalSpend));
        if (totalLeadsLabel != null)
            totalLeadsLabel.setText(String.valueOf(totalLeads));
        if (avgCostPerLeadLabel != null)
            avgCostPerLeadLabel.setText(totalLeads > 0 ? "$" + String.format("%.2f", avgCostPerLead) : "$0.00");
        if (overallROILabel != null)
            overallROILabel.setText(String.format("%.1f%%", overallROI));

        // Load Spend by Campaign Type table
        spendByTypeModel.setRowCount(0);
        for (Map.Entry<String, Double> entry : spendByType.entrySet()) {
            Object[] row = {
                    entry.getKey(),
                    "$" + String.format("%.0f", entry.getValue()),
                    campaignCountByType.get(entry.getKey())
            };
            spendByTypeModel.addRow(row);
        }

        // Load Campaign Performance table
        performanceModel.setRowCount(0);
        for (Campaign campaign : campaigns) {
            double budget = campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget();
            CampaignMetrics metrics = metricsMap.getOrDefault(campaign.getCampaignId(), new CampaignMetrics(0, 0, 0, 0));
            
            double spend = metrics.revenue > 0 ? metrics.revenue : budget * 0.65;
            double variance = budget - spend;
            int leads = metrics.conversions;
            double costPerLead = leads > 0 ? spend / leads : 0;
            
            // ROI calculation: if no metrics data yet (conversions = 0), show 0% (pending)
            // Otherwise show actual ROI
            double roiPercent;
            if (metrics.conversions == 0 && metrics.revenue == 0) {
                // No metrics data collected yet - show 0% as "pending data"
                roiPercent = 0.0;
            } else {
                // Has metrics - calculate ROI
                roiPercent = budget > 0 ? ((spend - budget) / budget) * 100 : 0;
            }

            Object[] row = {
                    campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName(),
                    campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL",
                    campaign.getStatus() != null ? campaign.getStatus() : "ACTIVE",
                    "$" + String.format("%.0f", budget),
                    "$" + String.format("%.0f", spend),
                    "$" + String.format("%.0f", variance),
                    leads,
                    leads > 0 ? "$" + String.format("%.2f", costPerLead) : "$0.00",
                    String.format("%.1f%%", roiPercent)
            };
            performanceModel.addRow(row);
        }
    }
    
    /**
     * Fetch metrics from campaign_metrics table and aggregate by campaign_id
     */
    private Map<Integer, CampaignMetrics> fetchCampaignMetrics() {
        Map<Integer, CampaignMetrics> metricsMap = new HashMap<>();
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                return metricsMap;
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

                    CampaignMetrics current = metricsMap.getOrDefault(campaignId, new CampaignMetrics(0, 0, 0, 0));
                    current.impressions += asInt(row.get("impressions"), 0);
                    current.clicks += asInt(row.get("clicks"), 0);
                    current.conversions += asInt(row.get("conversions"), 0);
                    current.revenue += asDouble(row.get("revenue_generated"), 0.0);
                    metricsMap.put(campaignId, current);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching campaign metrics: " + e.getMessage());
        }
        
        return metricsMap;
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
    
    /**
     * Inner class to hold aggregated campaign metrics
     */
    private static class CampaignMetrics {
        int impressions;
        int clicks;
        int conversions;
        double revenue;
        
        CampaignMetrics(int impressions, int clicks, int conversions, double revenue) {
            this.impressions = impressions;
            this.clicks = clicks;
            this.conversions = conversions;
            this.revenue = revenue;
        }
    }
}

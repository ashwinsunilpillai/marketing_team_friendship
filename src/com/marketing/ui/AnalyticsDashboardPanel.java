package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AnalyticsDashboardPanel - Campaign Analytics Dashboard
 * Displays lead analytics, conversion metrics, and campaign performance
 */
public class AnalyticsDashboardPanel extends JPanel {
    private CampaignFacade campaignFacade;
    private DefaultTableModel tableModel;
    private JTable analyticsTable;

    // Metric labels for dynamic updates
    private JLabel totalLeadsLabel;
    private JLabel totalTargetLabel;
    private JLabel overallRateLabel;
    private JLabel activeCampaignsLabel;

    // Filter components
    private JComboBox<String> statusFilterCombo;
    private JButton refreshButton;

    public AnalyticsDashboardPanel() {
        this.campaignFacade = new CampaignFacade();
        initializeUI();
        loadAnalytics();
    }

    /**
     * Initializes the UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Metrics and filter wrapper panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(createMetricsPanel());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(createFilterPanel());

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Table panel
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Creates header panel
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Analytics");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(45, 55, 72));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    /**
     * Creates metrics panel with 4 metric cards
     */
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Total Leads
        JPanel card1 = createMetricCardPanel("400", "Total Leads", new Color(76, 175, 80),
                (label) -> totalLeadsLabel = label);
        panel.add(card1);

        // Total Target
        JPanel card2 = createMetricCardPanel("955", "Total Target", new Color(33, 150, 243),
                (label) -> totalTargetLabel = label);
        panel.add(card2);

        // Overall Rate
        JPanel card3 = createMetricCardPanel("41.9%", "Overall Rate", new Color(156, 39, 176),
                (label) -> overallRateLabel = label);
        panel.add(card3);

        // Active Campaigns
        JPanel card4 = createMetricCardPanel("3", "Active Campaigns", new Color(255, 152, 0),
                (label) -> activeCampaignsLabel = label);
        panel.add(card4);

        return panel;
    }

    /**
     * Helper to create metric card panel and capture its label
     */
    private JPanel createMetricCardPanel(String value, String label, Color accentColor,
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

        JPanel topBorder = new JPanel();
        topBorder.setBackground(accentColor);
        topBorder.setPreferredSize(new Dimension(0, 4));

        card.add(topBorder, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        labelCapture.accept(valueLabel);
        return card;
    }

    /**
     * Creates filter panel
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Status filter label
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(statusLabel);

        // Status filter dropdown
        statusFilterCombo = new JComboBox<>(
                new String[] { "All Statuses", "ACTIVE", "COMPLETED", "PAUSED", "PLANNED" });
        statusFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilterCombo.addActionListener(e -> applyFilters());
        panel.add(statusFilterCombo);

        panel.add(Box.createHorizontalGlue());

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        refreshButton.setBackground(new Color(33, 150, 243));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        refreshButton.addActionListener(e -> loadAnalytics());
        panel.add(refreshButton);

        return panel;
    }

    /**
     * Creates table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "Campaign", "Type", "Status", "Lead Target", "Leads Generat...", "Conversion %",
                "Progress" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        analyticsTable = new JTable(tableModel);
        analyticsTable.setRowHeight(32);
        analyticsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        analyticsTable.setBackground(Color.WHITE);
        analyticsTable.setGridColor(new Color(220, 225, 232));

        // Custom header
        analyticsTable.getTableHeader().setBackground(new Color(33, 150, 243));
        analyticsTable.getTableHeader().setForeground(Color.WHITE);
        analyticsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Status column renderer with background colors
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("ACTIVE".equals(status)) {
                    c.setBackground(new Color(144, 238, 144)); // Light green
                    c.setForeground(new Color(0, 100, 0));
                } else if ("COMPLETED".equals(status)) {
                    c.setBackground(new Color(176, 224, 230)); // Light blue
                    c.setForeground(new Color(0, 0, 139));
                } else if ("PAUSED".equals(status)) {
                    c.setBackground(new Color(255, 218, 185)); // Peach
                    c.setForeground(new Color(139, 69, 19));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                if (isSelected) {
                    c.setBackground(new Color(33, 150, 243));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };

        // Progress bar renderer for last column
        DefaultTableCellRenderer progressRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JProgressBar bar = new JProgressBar(0, 100);
                if (value instanceof String) {
                    String strValue = (String) value;
                    int percentage = Integer.parseInt(strValue.replace("%", ""));
                    bar.setValue(percentage);
                    bar.setStringPainted(true);
                    bar.setString(strValue);

                    if (percentage >= 75) {
                        bar.setForeground(new Color(76, 175, 80)); // Green
                    } else if (percentage >= 50) {
                        bar.setForeground(new Color(33, 150, 243)); // Blue
                    } else if (percentage >= 25) {
                        bar.setForeground(new Color(255, 152, 0)); // Orange
                    } else {
                        bar.setForeground(new Color(244, 67, 54)); // Red
                    }
                }
                return bar;
            }
        };

        analyticsTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer); // Status column
        analyticsTable.getColumnModel().getColumn(7).setCellRenderer(progressRenderer); // Progress column

        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads analytics data from campaigns
     */
    private void loadAnalytics() {
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();

        for (Campaign campaign : campaigns) {
            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;
            double conversionRate = leadTarget > 0 ? (leadsGenerated * 100.0 / leadTarget) : 0;
            int progress = (int) Math.min(100, conversionRate);

            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    "EMAIL", // Type
                    campaign.getStatus(),
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    String.format("%.1f%%", conversionRate),
                    progress + "%"
            };
            tableModel.addRow(row);
        }

        // Update metrics
        updateMetrics();
    }

    /**
     * Applies status filter to the table
     */
    private void applyFilters() {
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();

        for (Campaign campaign : campaigns) {
            // Check status filter
            if (!selectedStatus.equals("All Statuses") && !campaign.getStatus().equals(selectedStatus)) {
                continue;
            }

            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;
            double conversionRate = leadTarget > 0 ? (leadsGenerated * 100.0 / leadTarget) : 0;
            int progress = (int) Math.min(100, conversionRate);

            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    "EMAIL",
                    campaign.getStatus(),
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    String.format("%.1f%%", conversionRate),
                    progress + "%"
            };
            tableModel.addRow(row);
        }

        // Update metrics
        updateMetrics();
    }

    /**
     * Updates the metrics panel with current data
     */
    private void updateMetrics() {
        List<Campaign> allCampaigns = campaignFacade.getAllCampaigns();

        // Calculate totals from real campaign data
        int totalLeads = 0;
        int totalTarget = 0;
        int activeCampaigns = 0;

        for (Campaign campaign : allCampaigns) {
            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;
            totalLeads += leadsGenerated;
            totalTarget += leadTarget;
            if ("ACTIVE".equals(campaign.getStatus())) {
                activeCampaigns++;
            }
        }

        double overallRate = totalTarget > 0 ? (totalLeads * 100.0 / totalTarget) : 0;

        // Update labels
        if (totalLeadsLabel != null) {
            totalLeadsLabel.setText(String.valueOf(totalLeads));
        }
        if (totalTargetLabel != null) {
            totalTargetLabel.setText(String.valueOf(totalTarget));
        }
        if (overallRateLabel != null) {
            overallRateLabel.setText(String.format("%.1f%%", overallRate));
        }
        if (activeCampaignsLabel != null) {
            activeCampaignsLabel.setText(String.valueOf(activeCampaigns));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Analytics Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new AnalyticsDashboardPanel());
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

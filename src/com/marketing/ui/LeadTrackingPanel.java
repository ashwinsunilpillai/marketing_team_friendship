package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * LeadTrackingPanel - Displays lead tracking metrics and performance
 */
public class LeadTrackingPanel extends JPanel {
    private CampaignFacade campaignFacade;
    private JTable leadTable;
    private DefaultTableModel tableModel;
    private JLabel totalLeadsLabel;
    private JLabel totalTargetLabel;
    private JLabel overallRateLabel;
    private JLabel activeCampaignsLabel;
    private int selectedCampaignId = -1;

    public LeadTrackingPanel() {
        this.campaignFacade = new CampaignFacade();
        initializeUI();
        loadLeadData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        // Metrics panel
        JPanel metricsPanel = createMetricsPanel();
        add(metricsPanel, BorderLayout.NORTH);

        // Filter and table panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Filter panel
        contentPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // Table panel
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel card1 = createMetricCard("400", "Total Leads", new Color(76, 175, 80),
                (label) -> totalLeadsLabel = label);
        panel.add(card1);

        JPanel card2 = createMetricCard("955", "Total Target", new Color(33, 150, 243),
                (label) -> totalTargetLabel = label);
        panel.add(card2);

        JPanel card3 = createMetricCard("41.9%", "Overall Rate", new Color(255, 152, 0),
                (label) -> overallRateLabel = label);
        panel.add(card3);

        JPanel card4 = createMetricCard("3", "Active Campaigns", new Color(156, 39, 176),
                (label) -> activeCampaignsLabel = label);
        panel.add(card4);

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

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[] { "All Statuses", "ACTIVE", "PAUSED", "COMPLETED", "PLANNED" });
        statusCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusCombo.setPreferredSize(new Dimension(140, 25));
        panel.add(statusLabel);
        panel.add(statusCombo);

        panel.add(Box.createHorizontalGlue());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        refreshButton.setBackground(new Color(200, 200, 200));
        refreshButton.setForeground(new Color(45, 55, 72));
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        refreshButton.addActionListener(e -> loadLeadData());
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "Campaign", "Type", "Status", "Lead Target", "Leads Generated", "Conversion %",
                "Progress" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leadTable = new JTable(tableModel);
        leadTable.setRowHeight(36);
        leadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leadTable.setBackground(Color.WHITE);
        leadTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leadTable.getSelectedRow() >= 0) {
                selectedCampaignId = (Integer) tableModel.getValueAt(leadTable.getSelectedRow(), 0);
            }
        });

        // Style header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(63, 81, 181));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < leadTable.getColumnCount(); i++) {
            leadTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
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
                    }
                }
                return label;
            }
        };
        leadTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);

        // Progress bar renderer
        DefaultTableCellRenderer progressRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.WHITE);

                if (value != null) {
                    try {
                        double percentage = Double.parseDouble(value.toString().replaceAll("%", ""));

                        JProgressBar progressBar = new JProgressBar(0, 100);
                        progressBar.setValue((int) percentage);
                        progressBar.setStringPainted(true);
                        progressBar.setString(String.format("%.0f%%", percentage));
                        progressBar.setFont(new Font("SansSerif", Font.BOLD, 10));

                        // Color based on percentage
                        if (percentage >= 75) {
                            progressBar.setForeground(new Color(76, 175, 80)); // Green
                        } else if (percentage >= 50) {
                            progressBar.setForeground(new Color(33, 150, 243)); // Blue
                        } else if (percentage >= 25) {
                            progressBar.setForeground(new Color(255, 152, 0)); // Orange/Yellow
                        } else {
                            progressBar.setForeground(new Color(244, 67, 54)); // Red
                        }

                        panel.add(progressBar, BorderLayout.CENTER);
                    } catch (Exception e) {
                        JLabel label = new JLabel(value.toString());
                        panel.add(label, BorderLayout.CENTER);
                    }
                }
                return panel;
            }
        };
        leadTable.getColumnModel().getColumn(7).setCellRenderer(progressRenderer);

        JScrollPane scrollPane = new JScrollPane(leadTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadLeadData() {
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();

        int totalLeads = 0;
        int totalTarget = 0;
        int activeCampaigns = 0;

        for (Campaign campaign : campaigns) {
            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;

            totalLeads += leadsGenerated;
            totalTarget += leadTarget;

            if ("ACTIVE".equals(campaign.getStatus())) {
                activeCampaigns++;
            }

            double conversionRate = leadTarget > 0 ? (double) leadsGenerated / leadTarget * 100 : 0;

            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    "EMAIL",
                    campaign.getStatus(),
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    String.format("%.1f%%", conversionRate),
                    String.format("%.0f%%", conversionRate)
            };
            tableModel.addRow(row);
        }

        // Update metrics
        double overallRate = totalTarget > 0 ? (double) totalLeads / totalTarget * 100 : 0;

        if (totalLeadsLabel != null)
            totalLeadsLabel.setText(String.valueOf(totalLeads));
        if (totalTargetLabel != null)
            totalTargetLabel.setText(String.valueOf(totalTarget));
        if (overallRateLabel != null)
            overallRateLabel.setText(String.format("%.1f%%", overallRate));
        if (activeCampaignsLabel != null)
            activeCampaignsLabel.setText(String.valueOf(activeCampaigns));
    }
}

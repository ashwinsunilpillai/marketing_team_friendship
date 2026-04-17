package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;
import com.marketing.m3.analytics.AnalyticsEngine;
import com.marketing.m3.analytics.AnalyticsObserver;
import com.marketing.m3.analytics.AnalyticsSummary;
import com.marketing.m3.analytics.CampaignAnalyticsItem;
import com.marketing.m3.reporting.Report;
import com.marketing.m3.reporting.ReportBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private final DefaultTableModel tableModel;
    private final JTextField reportNameField = new JTextField("Weekly Performance Report");
    private final JTextField ctrTargetField = new JTextField("2.5");
    private final JTextField roiTargetField = new JTextField("15");
    private final JTextArea reportArea = new JTextArea();
    private final MetricsChartPanel chartPanel = new MetricsChartPanel();

    private AnalyticsSummary currentSummary;

    public AnalyticsDashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 247, 250));

        analyticsEngine.attach(this);

        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.add(createKpiWidgets(), BorderLayout.NORTH);

        String[] columns = {"Campaign", "Impressions", "Clicks", "Conversions", "CTR %", "ROI %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JSplitPane dataSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCampaignTablePanel(),
                createChartPanel());
        dataSplit.setResizeWeight(0.62);
        dataSplit.setBorder(null);

        content.add(dataSplit, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(createReportPanel(), BorderLayout.SOUTH);

        refreshAnalytics();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("Analytics Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Live campaign KPIs, trends, and report generation");
        subtitle.setForeground(new Color(90, 98, 110));

        JPanel text = new JPanel(new BorderLayout());
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.SOUTH);

        JButton refreshButton = new JButton("Refresh Metrics");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshAnalytics());

        header.add(text, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JComponent createKpiWidgets() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        kpiPanel.setOpaque(false);

        kpiPanel.add(createKpiWidget("Campaigns", "0"));
        kpiPanel.add(createKpiWidget("Impressions", "0"));
        kpiPanel.add(createKpiWidget("Clicks", "0"));
        kpiPanel.add(createKpiWidget("Conversions", "0"));
        kpiPanel.add(createKpiWidget("CTR", "0.00%"));
        kpiPanel.add(createKpiWidget("ROI", "0.00%"));

        return kpiPanel;
    }

    private JComponent createKpiWidget(String name, String initialValue) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel(name);
        title.setForeground(new Color(90, 98, 110));

        JLabel value = new JLabel(initialValue);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 16f));
        kpiValueLabels.put(name, value);

        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JComponent createCampaignTablePanel() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Campaign Analytics"));
        return scrollPane;
    }

    private JComponent createChartPanel() {
        chartPanel.setBorder(BorderFactory.createTitledBorder("KPI Snapshot"));
        return chartPanel;
    }

    private JComponent createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Report Generator"));

        JPanel controls = new JPanel(new GridLayout(2, 4, 8, 8));
        controls.setOpaque(false);

        controls.add(new JLabel("Report Name"));
        controls.add(reportNameField);
        controls.add(new JLabel("CTR Target %"));
        controls.add(ctrTargetField);
        controls.add(new JLabel("ROI Target %"));
        controls.add(roiTargetField);

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        controls.add(generateButton);

        JButton clearButton = new JButton("Clear Report");
        clearButton.addActionListener(e -> reportArea.setText(""));
        controls.add(clearButton);

        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setRows(8);
        reportArea.setBackground(new Color(251, 252, 253));

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }

    private void refreshAnalytics() {
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();
        analyticsEngine.refresh(campaigns);
    }

    @Override
    public void onAnalyticsUpdated(AnalyticsSummary summary) {
        currentSummary = summary;
        updateKpiCards(summary);
        updateTable(summary);
        chartPanel.setSummary(summary);
    }

    private void updateKpiCards(AnalyticsSummary summary) {
        kpiValueLabels.get("Campaigns").setText(String.valueOf(summary.getCampaignCount()));
        kpiValueLabels.get("Impressions").setText(String.valueOf(summary.getTotalImpressions()));
        kpiValueLabels.get("Clicks").setText(String.valueOf(summary.getTotalClicks()));
        kpiValueLabels.get("Conversions").setText(String.valueOf(summary.getTotalConversions()));
        kpiValueLabels.get("CTR").setText(decimalFormat.format(summary.getCtr()) + "%");
        kpiValueLabels.get("ROI").setText(decimalFormat.format(summary.getRoi()) + "%");
    }

    private void updateTable(AnalyticsSummary summary) {
        tableModel.setRowCount(0);
        for (CampaignAnalyticsItem item : summary.getCampaignItems()) {
            tableModel.addRow(new Object[]{
                    item.getCampaignName(),
                    item.getImpressions(),
                    item.getClicks(),
                    item.getConversions(),
                    decimalFormat.format(item.getCtr()),
                    decimalFormat.format(item.getRoi())
            });
        }
    }

    private void generateReport() {
        if (currentSummary == null) {
            JOptionPane.showMessageDialog(this, "Refresh metrics before generating a report.",
                    "Analytics Dashboard", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double ctrTarget = Double.parseDouble(ctrTargetField.getText().trim());
            double roiTarget = Double.parseDouble(roiTargetField.getText().trim());

            Report report = new ReportBuilder()
                    .withTitle(reportNameField.getText())
                    .withSummary(currentSummary)
                    .withKpiTargets(ctrTarget, roiTarget, currentSummary)
                    .withCampaignBreakdown(currentSummary)
                    .build();

            reportArea.setText(report.renderText());
            reportArea.setCaretPosition(0);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "CTR and ROI targets must be valid numbers.",
                    "Analytics Dashboard", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Analytics Dashboard", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class MetricsChartPanel extends JPanel {
        private AnalyticsSummary summary;

        MetricsChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(360, 300));
        }

        void setSummary(AnalyticsSummary summary) {
            this.summary = summary;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (summary == null) {
                g2.setColor(new Color(120, 128, 140));
                g2.drawString("No metrics yet. Click Refresh Metrics.", 20, height / 2);
                g2.dispose();
                return;
            }

            double[] values = {
                    summary.getCtr(),
                    Math.max(summary.getRoi(), 0.0),
                    summary.getCampaignCount()
            };
            String[] labels = {"CTR", "ROI", "Campaigns"};
            Color[] colors = {
                    new Color(14, 116, 144),
                    new Color(14, 165, 233),
                    new Color(56, 189, 248)
            };

            double max = 1.0;
            for (double value : values) {
                max = Math.max(max, value);
            }

            int chartTop = 30;
            int chartBottom = height - 40;
            int chartHeight = chartBottom - chartTop;
            int barWidth = (width - 90) / values.length;

            g2.setColor(new Color(104, 112, 125));
            g2.drawLine(35, chartBottom, width - 20, chartBottom);

            for (int i = 0; i < values.length; i++) {
                int x = 45 + i * barWidth;
                int h = (int) ((values[i] / max) * (chartHeight - 12));
                int y = chartBottom - h;

                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y, barWidth - 20, h, 10, 10);

                g2.setColor(new Color(70, 78, 92));
                g2.drawString(labels[i], x, chartBottom + 16);
                g2.drawString(new DecimalFormat("0.##").format(values[i]), x, y - 6);
            }

            g2.dispose();
        }
    }
}

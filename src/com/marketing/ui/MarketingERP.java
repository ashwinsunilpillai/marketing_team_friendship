package com.marketing.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * MarketingERP - Main Application Frame
 * Entry point for the Marketing ERP subsystem.
 */
public class MarketingERP extends JFrame {
    
    /**
     * Constructor
     */
    public MarketingERP() {
        applyTheme();
        setTitle("Marketing ERP Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(14, 14, 14, 14));
        rootPanel.setBackground(new Color(245, 247, 250));

        rootPanel.add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Campaigns", new CampaignManagerPanel());
        tabs.addTab("Engagement Hub", new Member2Panel());
        tabs.addTab("Analytics Dashboard", new AnalyticsDashboardPanel());

        rootPanel.add(tabs, BorderLayout.CENTER);
        add(rootPanel);
    }

    private void applyTheme() {
        UIManager.put("Panel.background", new Color(245, 247, 250));
        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 13));
        UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("TextArea.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 13));
        UIManager.put("TabbedPane.font", new Font("SansSerif", Font.BOLD, 13));
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 2, 10, 2));

        JLabel title = new JLabel("Marketing ERP Suite");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel("Campaign operations and customer engagement tools");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));
        subtitle.setForeground(new Color(90, 98, 110));

        JPanel textBlock = new JPanel(new BorderLayout());
        textBlock.setOpaque(false);
        textBlock.add(title, BorderLayout.NORTH);
        textBlock.add(subtitle, BorderLayout.SOUTH);

        header.add(textBlock, BorderLayout.WEST);
        return header;
    }
    
    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarketingERP frame = new MarketingERP();
            frame.setVisible(true);
        });
    }
}

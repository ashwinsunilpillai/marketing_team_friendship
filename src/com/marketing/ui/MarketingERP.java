package com.marketing.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MarketingERP - Main Application Frame
 * Entry point for the Marketing ERP subsystem with sidebar navigation.
 */
public class MarketingERP extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;

    /**
     * Constructor
     */
    public MarketingERP() {
        applyTheme();
        setTitle("Enterprise Resource Planning System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(new Color(245, 247, 250));

        // Create content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250));

        // Add all menu panels to the CardLayout
        contentPanel.add(new PlaceholderPanel("Sales Management"), "Sales Management");
        contentPanel.add(new PlaceholderPanel("Supply Chain"), "Supply Chain");
        contentPanel.add(new PlaceholderPanel("Manufacturing"), "Manufacturing");
        contentPanel.add(new PlaceholderPanel("Financial Management"), "Financial Management");
        contentPanel.add(new PlaceholderPanel("Accounting"), "Accounting");
        contentPanel.add(new PlaceholderPanel("HR Management"), "HR Management");
        contentPanel.add(new PlaceholderPanel("Project Management"), "Project Management");
        contentPanel.add(new PlaceholderPanel("Reporting"), "Reporting");
        contentPanel.add(new PlaceholderPanel("Data Analytics"), "Data Analytics");
        contentPanel.add(new PlaceholderPanel("Business Intelligence"), "Business Intelligence");

        // Add the Marketing panel with actual content
        JTabbedPane marketingTabs = new JTabbedPane();
        marketingTabs.addTab("Campaigns", new CampaignManagerPanel());
        marketingTabs.addTab("Analytics", new Member2Panel());
        marketingTabs.addTab("Lead Tracking", new AnalyticsDashboardPanel());
        contentPanel.add(marketingTabs, "Marketing");

        contentPanel.add(new PlaceholderPanel("Automation"), "Automation");
        contentPanel.add(new PlaceholderPanel("Integration"), "Integration");

        // Create sidebar with listener
        sidebarPanel = new SidebarPanel(marketingTabs);
        sidebarPanel.setMenuClickListener(this::handleMenuClick);

        rootPanel.add(sidebarPanel, BorderLayout.WEST);
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        add(rootPanel);

        // Show Marketing panel by default
        cardLayout.show(contentPanel, "Marketing");
    }

    /**
     * Handles menu item clicks from sidebar
     */
    private void handleMenuClick(String menuItemName) {
        cardLayout.show(contentPanel, menuItemName);
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

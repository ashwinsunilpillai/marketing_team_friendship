package com.marketing.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * MarketingERP - Main Application Frame with Sidebar Navigation
 * Entry point for the Marketing ERP subsystem.
 */
public class MarketingERP extends JFrame {
    private JPanel contentPanel;
    private JLabel currentModuleLabel;

    /**
     * Constructor
     */
    public MarketingERP() {
        applyTheme();
        setTitle("Enterprise Resource Planning System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1400, 800));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 247, 250));

        // Create sidebar and main area
        SidebarPanel sidebar = new SidebarPanel(null);
        JPanel mainArea = createMainArea();

        // Setup menu click listener
        sidebar.setMenuClickListener(menuItemName -> handleMenuClick(menuItemName));

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(mainArea, BorderLayout.CENTER);

        add(rootPanel);
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        header.setPreferredSize(new Dimension(0, 80));

        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setBackground(Color.WHITE);
        headerContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        currentModuleLabel = new JLabel("Marketing");
        currentModuleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        textPanel.add(currentModuleLabel, BorderLayout.NORTH);

        headerContent.add(textPanel, BorderLayout.WEST);

        JButton userBtn = new JButton("User");
        userBtn.setBackground(new Color(240, 240, 240));
        userBtn.setFocusPainted(false);
        headerContent.add(userBtn, BorderLayout.EAST);

        header.add(headerContent);
        mainArea.add(header, BorderLayout.NORTH);

        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainArea.add(contentPanel, BorderLayout.CENTER);

        // Show the marketing workspace by default
        showMarketing();

        return mainArea;
    }

    private void handleMenuClick(String menuItemName) {
        if (menuItemName.equals("Marketing")) {
            showMarketing();
        } else {
            showPlaceholder(menuItemName);
        }
    }

    private void showMarketing() {
        currentModuleLabel.setText("Marketing");
        contentPanel.removeAll();

        JTabbedPane marketingTabs = new JTabbedPane();
        marketingTabs.setBackground(Color.WHITE);
        marketingTabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        marketingTabs.addTab("Campaigns", new CampaignManagerPanel());
        marketingTabs.addTab("Lead Tracking", new LeadTrackingPanel());
        marketingTabs.addTab("Analytics", new AnalyticsDashboardPanel());

        // Refresh Lead Tracking panel when its tab is selected so it shows latest statuses
        marketingTabs.addChangeListener(e -> {
            Component comp = marketingTabs.getSelectedComponent();
            if (comp instanceof LeadTrackingPanel) {
                ((LeadTrackingPanel) comp).refreshData();
            }
        });

        contentPanel.add(marketingTabs, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPlaceholder(String moduleName) {
        currentModuleLabel.setText(moduleName);
        contentPanel.removeAll();

        JPanel placeholder = new JPanel(new GridLayout(1, 1));
        placeholder.setBackground(Color.WHITE);
        placeholder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(moduleName),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel label = new JLabel(moduleName + " features coming soon...");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(new Color(150, 150, 150));
        label.setHorizontalAlignment(JLabel.CENTER);
        placeholder.add(label);

        contentPanel.add(placeholder, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
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
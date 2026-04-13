package com.marketing.ui;

import javax.swing.*;

/**
 * MarketingERP - Main Application Frame
 * Entry point for the Marketing ERP subsystem.
 */
public class MarketingERP extends JFrame {
    
    /**
     * Constructor
     */
    public MarketingERP() {
        setTitle("Marketing ERP - Member 1 (Foundation Layer)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Add Campaign Manager Panel
        CampaignManagerPanel campaignPanel = new CampaignManagerPanel();
        mainPanel.add(campaignPanel);
        
        add(mainPanel);
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

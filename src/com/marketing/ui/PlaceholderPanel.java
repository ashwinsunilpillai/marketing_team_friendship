package com.marketing.ui;

import javax.swing.*;
import java.awt.*;

/**
 * PlaceholderPanel - A blank placeholder panel for sidebar menu items
 * Displays the section title with a clean, empty canvas
 */
public class PlaceholderPanel extends JPanel {
    private final String title;

    public PlaceholderPanel(String title) {
        this.title = title;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(45, 55, 72));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        add(headerPanel, BorderLayout.NORTH);

        // Center content area (empty)
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(245, 247, 250));
        add(contentPanel, BorderLayout.CENTER);
    }
}

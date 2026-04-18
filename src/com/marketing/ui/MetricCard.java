package com.marketing.ui;

import javax.swing.*;
import java.awt.*;

/**
 * MetricCard - A card component for displaying key metrics
 */
public class MetricCard extends JPanel {
    private final String value;
    private final String label;
    private final Color accentColor;

    public MetricCard(String value, String label, Color accentColor) {
        this.value = value;
        this.label = label;
        this.accentColor = accentColor;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Value label
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        // Label text
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelText.setForeground(new Color(130, 140, 155));

        // Accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(4, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel, BorderLayout.NORTH);
        centerPanel.add(labelText, BorderLayout.SOUTH);

        add(accentBar, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }
}

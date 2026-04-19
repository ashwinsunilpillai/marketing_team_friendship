package com.marketing.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * SidebarPanel - Navigation sidebar for the ERP system
 * Displays menu items with a modern dark theme
 */
public class SidebarPanel extends JPanel {
    private final JTabbedPane tabbedPane;
    private MenuItemClickListener menuClickListener;
    private JPanel currentHighlightedItem;

    public interface MenuItemClickListener {
        void onMenuItemClicked(String menuItemName);
    }

    public SidebarPanel(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        initializeUI();
    }

    public void setMenuClickListener(MenuItemClickListener listener) {
        this.menuClickListener = listener;
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 55, 72));
        setPreferredSize(new Dimension(200, 0));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 38, 54));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("ERP System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(150, 160, 175));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(versionLabel, BorderLayout.SOUTH);
        headerPanel.add(textPanel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(45, 55, 72));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] items = {
                "Sales Management",
                "Supply Chain",
                "Manufacturing",
                "Financial Management",
                "Accounting",
                "HR Management",
                "Project Management",
                "Reporting",
                "Data Analytics",
                "Business Intelligence",
                "Marketing",
                "Automation",
                "Integration"
        };

        for (String item : items) {
            menuPanel.add(createMenuItem(item));
        }

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMenuItem(String label) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel itemLabel = new JLabel(label);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        itemLabel.setForeground(new Color(180, 190, 205));

        itemPanel.add(itemLabel, BorderLayout.CENTER);

        // Highlight Marketing item by default
        if (label.equals("Marketing")) {
            itemPanel.setBackground(new Color(63, 81, 181));
            itemLabel.setForeground(Color.WHITE);
            itemLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            itemPanel.setOpaque(true);
            currentHighlightedItem = itemPanel;
        }

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Reset previous highlight
                if (currentHighlightedItem != null && !currentHighlightedItem.equals(itemPanel)) {
                    currentHighlightedItem.setOpaque(false);
                    currentHighlightedItem.setBackground(new Color(45, 55, 72));
                    for (Component comp : currentHighlightedItem.getComponents()) {
                        if (comp instanceof JLabel) {
                            ((JLabel) comp).setForeground(new Color(180, 190, 205));
                            ((JLabel) comp).setFont(new Font("SansSerif", Font.PLAIN, 13));
                        }
                    }
                }

                // Highlight current item
                itemPanel.setBackground(new Color(63, 81, 181));
                itemPanel.setOpaque(true);
                itemLabel.setForeground(Color.WHITE);
                itemLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
                currentHighlightedItem = itemPanel;

                // Notify listener
                if (menuClickListener != null) {
                    menuClickListener.onMenuItemClicked(label);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!itemPanel.equals(currentHighlightedItem)) {
                    itemPanel.setBackground(new Color(60, 75, 110));
                    itemPanel.setOpaque(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!itemPanel.equals(currentHighlightedItem)) {
                    itemPanel.setBackground(new Color(45, 55, 72));
                    itemPanel.setOpaque(false);
                }
            }
        });

        return itemPanel;
    }
}

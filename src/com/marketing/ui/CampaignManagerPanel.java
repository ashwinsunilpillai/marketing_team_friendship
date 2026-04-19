package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.exception.*;
import com.marketing.facade.CampaignFacade;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * CampaignManagerPanel - Swing UI Panel with Modern Dashboard
 * Provides a professional interface for managing campaigns with metrics and
 * status visualization
 */
public class CampaignManagerPanel extends JPanel {
    private CampaignFacade campaignFacade;

    // UI Components
    private JTable campaignTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton startButton;
    private JButton pauseButton;
    private JButton endButton;
    private JButton refreshButton;
    private int selectedCampaignId = -1;

    // Metric components for dynamic updates
    private JLabel totalCampaignsLabel;
    private JLabel activeCampaignsLabel;
    private JLabel totalBudgetLabel;
    private JLabel totalSpendLabel;

    /**
     * Constructor
     */
    public CampaignManagerPanel() {
        this.campaignFacade = new CampaignFacade();
        initializeUI();
        loadCampaigns();
    }

    /**
     * Initializes the UI
     */
    private void initializeUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        // Header panel with title
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        JPanel campaignsPanel = new JPanel(new BorderLayout(10, 10));
        campaignsPanel.setOpaque(false);
        campaignsPanel.add(createMetricsPanel(), BorderLayout.NORTH);

        JPanel campaignsContent = new JPanel(new BorderLayout(10, 10));
        campaignsContent.setOpaque(false);
        JPanel filterActionsPanel = new JPanel();
        filterActionsPanel.setLayout(new BoxLayout(filterActionsPanel, BoxLayout.Y_AXIS));
        filterActionsPanel.setOpaque(false);
        filterActionsPanel.add(createFilterPanel());
        filterActionsPanel.add(Box.createVerticalStrut(8));
        filterActionsPanel.add(createActionsMenuPanel());
        campaignsContent.add(filterActionsPanel, BorderLayout.NORTH);
        campaignsContent.add(createTablePanel(), BorderLayout.CENTER);

        campaignsPanel.add(campaignsContent, BorderLayout.CENTER);

        add(campaignsPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the header panel
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Marketing");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(45, 55, 72));

        header.add(title, BorderLayout.WEST);

        // Right-side user info
        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        rightPanel.setOpaque(false);

        JLabel userLabel = new JLabel("User");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLabel.setForeground(new Color(90, 98, 110));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(255, 100, 100));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutButton.setFont(new Font("SansSerif", Font.PLAIN, 11));

        rightPanel.add(userLabel, BorderLayout.WEST);
        rightPanel.add(logoutButton, BorderLayout.EAST);

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Creates the metrics panel
     */
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Create metric cards with labels we can update
        JPanel card1 = createMetricCardPanel("7", "Total Campaigns", new Color(63, 81, 181),
                (label) -> totalCampaignsLabel = label);
        panel.add(card1);

        JPanel card2 = createMetricCardPanel("3", "Active", new Color(76, 175, 80),
                (label) -> activeCampaignsLabel = label);
        panel.add(card2);

        JPanel card3 = createMetricCardPanel("$51000", "Total Budget", new Color(33, 150, 243),
                (label) -> totalBudgetLabel = label);
        panel.add(card3);

        JPanel card4 = createMetricCardPanel("$33350", "Total Spend", new Color(156, 39, 176),
                (label) -> totalSpendLabel = label);
        panel.add(card4);

        return panel;
    }

    /**
     * Helper to create metric card and capture its label
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

        JPanel leftBorder = new JPanel();
        leftBorder.setBackground(accentColor);
        leftBorder.setPreferredSize(new Dimension(4, 0));

        card.add(leftBorder, BorderLayout.WEST);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);

        labelCapture.accept(valueLabel);
        return card;
    }

    /**
     * Creates the filter panel
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(searchLabel);
        panel.add(searchField);

        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilterCombo = new JComboBox<>(
                new String[] { "All Statuses", "ACTIVE", "PAUSED", "COMPLETED", "PLANNED" });
        statusFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilterCombo.setPreferredSize(new Dimension(140, 25));
        panel.add(statusLabel);
        panel.add(statusFilterCombo);

        // Type filter
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        typeFilterCombo = new JComboBox<>(
                new String[] { "All Types", "EMAIL", "SOCIAL_MEDIA", "ADS", "CONTENT", "EVENT" });
        typeFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        typeFilterCombo.setPreferredSize(new Dimension(140, 25));
        panel.add(typeLabel);
        panel.add(typeFilterCombo);

        panel.add(Box.createHorizontalGlue());

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        searchButton.setBackground(new Color(63, 81, 181));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        searchButton.addActionListener(e -> applyFilters());
        panel.add(searchButton);

        // Add listeners for real-time filtering
        statusFilterCombo.addActionListener(e -> applyFilters());
        typeFilterCombo.addActionListener(e -> applyFilters());

        return panel;
    }

    /**
     * Creates the actions menu panel
     */
    private JPanel createActionsMenuPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Actions label
        JLabel actionsLabel = new JLabel("Actions:");
        actionsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        actionsLabel.setForeground(new Color(45, 55, 72));
        panel.add(actionsLabel);

        // Create buttons
        addButton = createStyledButton("Add Campaign", new Color(63, 81, 181), Color.WHITE);
        addButton.addActionListener(e -> handleAddCampaign());
        panel.add(addButton);

        editButton = createStyledButton("Edit", new Color(33, 150, 243), Color.WHITE);
        editButton.addActionListener(e -> handleEdit());
        panel.add(editButton);

        deleteButton = createStyledButton("Delete", new Color(244, 67, 54), Color.WHITE);
        deleteButton.addActionListener(e -> handleDelete());
        panel.add(deleteButton);

        startButton = createStyledButton("Start", new Color(76, 175, 80), Color.WHITE);
        startButton.addActionListener(e -> handleStatusChange("ACTIVE"));
        panel.add(startButton);

        pauseButton = createStyledButton("Pause", new Color(255, 152, 0), Color.WHITE);
        pauseButton.addActionListener(e -> handleStatusChange("PAUSED"));
        panel.add(pauseButton);

        endButton = createStyledButton("End", new Color(156, 39, 176), Color.WHITE);
        endButton.addActionListener(e -> handleStatusChange("COMPLETED"));
        panel.add(endButton);

        panel.add(Box.createHorizontalGlue());

        refreshButton = createStyledButton("Refresh", new Color(200, 200, 200), new Color(45, 55, 72));
        refreshButton.addActionListener(e -> loadCampaigns());
        panel.add(refreshButton);

        return panel;
    }

    /**
     * Creates the table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "Name", "Type", "Status", "Start Date", "End Date", "Budget", "Spent", "Lead Target",
                "Lead Generated",
                "Channel" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        campaignTable = new JTable(tableModel);
        campaignTable.setRowHeight(32);
        campaignTable.setRowSelectionAllowed(true);
        campaignTable.setColumnSelectionAllowed(false);
        campaignTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        campaignTable.setBackground(Color.WHITE);
        campaignTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && campaignTable.getSelectedRow() >= 0) {
                int viewRow = campaignTable.getSelectedRow();
                int modelRow = campaignTable.convertRowIndexToModel(viewRow);
                Object idObj = tableModel.getValueAt(modelRow, 0);
                if (idObj instanceof Number) {
                    selectedCampaignId = ((Number) idObj).intValue();
                } else {
                    try {
                        selectedCampaignId = Integer.parseInt(idObj.toString());
                    } catch (Exception ex) {
                        selectedCampaignId = -1;
                    }
                }
            }
        });

        // Style table header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(245, 247, 250));
        headerRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerRenderer.setForeground(new Color(45, 55, 72));

        for (int i = 0; i < campaignTable.getColumnCount(); i++) {
            campaignTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Status cell renderer for colors
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
                        default:
                            label.setBackground(Color.WHITE);
                    }
                }

                return label;
            }
        };

        campaignTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer); // Status column

        JScrollPane scrollPane = new JScrollPane(campaignTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a styled button
     */
    private JButton createStyledButton(String label, Color background, Color foreground) {
        JButton button = new JButton(label);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Loads all campaigns into the table
     */
    private void loadCampaigns() {
        // clear selection because we'll reload the table
        selectedCampaignId = -1;
        if (campaignTable != null) {
            campaignTable.clearSelection();
        }
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();

        for (Campaign campaign : campaigns) {
            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;

            double conversionRate = leadTarget > 0 ? (double) leadsGenerated / leadTarget * 100 : 0;

            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    campaign.getCampaignType(),
                    campaign.getStatus(),
                    campaign.getStartDate(),
                    campaign.getEndDate(),
                    "$" + campaign.getBudget(),
                    "$3200", // Spent would come from real data
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    "Email"
            };
            tableModel.addRow(row);
        }

        // Update metrics after loading campaigns
        updateMetrics();
    }

    /**
     * Applies search and filter criteria to the table
     */
    private void applyFilters() {
        // clear selection when applying filters
        selectedCampaignId = -1;
        if (campaignTable != null) {
            campaignTable.clearSelection();
        }
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();

        // Get filter values
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        String selectedType = (String) typeFilterCombo.getSelectedItem();

        // Filter campaigns based on criteria
        for (Campaign campaign : campaigns) {
            // Check search filter (search in campaign name)
            if (!searchText.isEmpty() && !campaign.getCampaignName().toLowerCase().contains(searchText)) {
                continue;
            }

            // Check status filter
            if (!selectedStatus.equals("All Statuses") && !campaign.getStatus().equals(selectedStatus)) {
                continue;
            }

            // Check type filter (use actual campaign type from database)
            if (!selectedType.equals("All Types")) {
                String campaignType = campaign.getCampaignType();
                if (!campaignType.equals(selectedType)) {
                    continue;
                }
            }

            // Add row if it passes all filters
            int leadTarget = campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100;
            int leadsGenerated = campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 45;
            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    campaign.getCampaignType(),
                    campaign.getStatus(),
                    campaign.getStartDate(),
                    campaign.getEndDate(),
                    "$" + campaign.getBudget(),
                    "$3200", // Spent would come from real data
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    "Email"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Handles adding a campaign
     */
    private void handleAddCampaign() {
        // Create a new dialog for campaign creation
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Campaign", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 700);
        dialog.setLocationRelativeTo(this);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Fields panel with GridLayout
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        fieldsPanel.setBackground(Color.WHITE);

        // Campaign Name
        fieldsPanel.add(createLabel("Campaign Name:*"));
        JTextField nameField = new JTextField();
        fieldsPanel.add(nameField);

        // Type
        fieldsPanel.add(createLabel("Type:"));
        JComboBox<String> typeCombo = new JComboBox<>(
                new String[] { "EMAIL", "SOCIAL_MEDIA", "ADS", "CONTENT", "EVENT" });
        typeCombo.setSelectedItem("EMAIL");
        fieldsPanel.add(typeCombo);

        // Status
        fieldsPanel.add(createLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[] { "PLANNED", "ACTIVE", "PAUSED", "COMPLETED" });
        statusCombo.setSelectedItem("PLANNED");
        fieldsPanel.add(statusCombo);

        // Start Date
        fieldsPanel.add(createLabel("Start Date:"));
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        fieldsPanel.add(startDateSpinner);

        // End Date
        fieldsPanel.add(createLabel("End Date:"));
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        fieldsPanel.add(endDateSpinner);

        // Budget
        fieldsPanel.add(createLabel("Budget:"));
        JTextField budgetField = new JTextField("0.00");
        fieldsPanel.add(budgetField);

        // Target Audience
        fieldsPanel.add(createLabel("Target Audience:"));
        JTextField audienceField = new JTextField();
        fieldsPanel.add(audienceField);

        // Lead Target
        fieldsPanel.add(createLabel("Lead Target:"));
        JTextField leadTargetField = new JTextField("0");
        fieldsPanel.add(leadTargetField);

        // Channel
        fieldsPanel.add(createLabel("Channel:"));
        JTextField channelField = new JTextField();
        fieldsPanel.add(channelField);

        // Description
        fieldsPanel.add(createLabel("Description:"));
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        fieldsPanel.add(new JScrollPane(descriptionArea));

        // Notes
        fieldsPanel.add(createLabel("Notes:"));
        JTextArea notesArea = new JTextArea(2, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        fieldsPanel.add(new JScrollPane(notesArea));

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(new Color(45, 55, 72));
        cancelButton.setOpaque(true);
        cancelButton.setContentAreaFilled(true);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(33, 150, 243));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setContentAreaFilled(true);
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Campaign name is required", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create campaign object
                Campaign campaign = new Campaign();
                campaign.setCampaignName(name);
                campaign.setStatus((String) statusCombo.getSelectedItem());
                campaign.setCampaignType((String) typeCombo.getSelectedItem());
                campaign.setBudget(Double.parseDouble(budgetField.getText()));
                campaign.setStartDate(LocalDate.now());
                campaign.setEndDate(LocalDate.now().plusMonths(1));
                campaign.setSegmentId(1); // Default segment
                campaign.setDescription(descriptionArea.getText());

                // Set lead target
                try {
                    int leadTarget = Integer.parseInt(leadTargetField.getText().trim());
                    campaign.setLeadTarget(leadTarget > 0 ? leadTarget : 100);
                } catch (NumberFormatException ex) {
                    campaign.setLeadTarget(100); // Default to 100 if invalid
                }

                // Set leads generated (default to 45)
                campaign.setLeadsGenerated(45);

                // Save campaign
                if (campaignFacade.createCampaign(campaign)) {
                    JOptionPane.showMessageDialog(dialog, "Campaign created successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCampaigns();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create campaign", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid budget amount", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            } catch (CampaignCreationException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Updates the metrics panel with current data from database
     */
    private void updateMetrics() {
        List<Campaign> allCampaigns = campaignFacade.getAllCampaigns();

        // Count total campaigns
        int totalCampaigns = allCampaigns.size();

        // Count active campaigns
        int activeCampaigns = 0;
        double totalBudget = 0;
        double totalSpend = 0;

        for (Campaign campaign : allCampaigns) {
            if ("ACTIVE".equals(campaign.getStatus())) {
                activeCampaigns++;
            }
            totalBudget += campaign.getBudget();
            // Note: In a real system, you'd have actual spend data from the database
            // For now, we'll use a fixed value per campaign
            totalSpend += 3200;
        }

        // Update labels
        if (totalCampaignsLabel != null) {
            totalCampaignsLabel.setText(String.valueOf(totalCampaigns));
        }
        if (activeCampaignsLabel != null) {
            activeCampaignsLabel.setText(String.valueOf(activeCampaigns));
        }
        if (totalBudgetLabel != null) {
            totalBudgetLabel.setText("$" + String.format("%.0f", totalBudget));
        }
        if (totalSpendLabel != null) {
            totalSpendLabel.setText("$" + String.format("%.0f", totalSpend));
        }
    }

    /**
     * Helper method to create styled labels
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(45, 55, 72));
        return label;
    }

    /**
     * Handles editing selected campaign
     */
    private void handleEdit() {
        if (selectedCampaignId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a campaign to edit", "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Campaign campaign;
        try {
            campaign = campaignFacade.getCampaignById(selectedCampaignId);
        } catch (com.marketing.exception.CampaignNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Campaign not found: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Campaign", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 760);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        fieldsPanel.setBackground(Color.WHITE);

        fieldsPanel.add(createLabel("Campaign Name:*"));
        JTextField nameField = new JTextField(campaign.getCampaignName());
        fieldsPanel.add(nameField);

        fieldsPanel.add(createLabel("Type:"));
        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "EMAIL", "SOCIAL_MEDIA", "ADS", "CONTENT", "EVENT" });
        typeCombo.setSelectedItem(campaign.getCampaignType());
        fieldsPanel.add(typeCombo);

        fieldsPanel.add(createLabel("Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[] { "PLANNED", "ACTIVE", "PAUSED", "COMPLETED" });
        statusCombo.setSelectedItem(campaign.getStatus());
        fieldsPanel.add(statusCombo);

        fieldsPanel.add(createLabel("Start Date:"));
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        if (campaign.getStartDate() != null) {
            startDateSpinner.setValue(java.sql.Date.valueOf(campaign.getStartDate()));
        }
        fieldsPanel.add(startDateSpinner);

        fieldsPanel.add(createLabel("End Date:"));
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        if (campaign.getEndDate() != null) {
            endDateSpinner.setValue(java.sql.Date.valueOf(campaign.getEndDate()));
        }
        fieldsPanel.add(endDateSpinner);

        fieldsPanel.add(createLabel("Budget:"));
        JTextField budgetField = new JTextField(String.valueOf(campaign.getBudget()));
        fieldsPanel.add(budgetField);

        fieldsPanel.add(createLabel("Target Audience:"));
        JTextField audienceField = new JTextField();
        fieldsPanel.add(audienceField);

        fieldsPanel.add(createLabel("Lead Target:"));
        JTextField leadTargetField = new JTextField(String.valueOf(campaign.getLeadTarget()));
        fieldsPanel.add(leadTargetField);

        fieldsPanel.add(createLabel("Channel:"));
        JTextField channelField = new JTextField(campaign.getCampaignType());
        fieldsPanel.add(channelField);

        fieldsPanel.add(createLabel("Description:"));
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setText(campaign.getDescription());
        fieldsPanel.add(new JScrollPane(descriptionArea));

        fieldsPanel.add(createLabel("Notes:"));
        JTextArea notesArea = new JTextArea(2, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        fieldsPanel.add(new JScrollPane(notesArea));

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(new Color(45, 55, 72));
        cancelButton.setOpaque(true);
        cancelButton.setContentAreaFilled(true);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(33, 150, 243));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setContentAreaFilled(true);
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Campaign name is required", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                campaign.setCampaignName(name);
                campaign.setCampaignType((String) typeCombo.getSelectedItem());
                campaign.setStatus((String) statusCombo.getSelectedItem());

                // Dates conversion
                try {
                    java.util.Date sd = (java.util.Date) startDateSpinner.getValue();
                    campaign.setStartDate(new java.sql.Date(sd.getTime()).toLocalDate());
                } catch (Exception ex) {
                    // ignore, keep existing
                }
                try {
                    java.util.Date ed = (java.util.Date) endDateSpinner.getValue();
                    campaign.setEndDate(new java.sql.Date(ed.getTime()).toLocalDate());
                } catch (Exception ex) {
                    // ignore, keep existing
                }

                try {
                    campaign.setBudget(Double.parseDouble(budgetField.getText().trim()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid budget amount", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int lt = Integer.parseInt(leadTargetField.getText().trim());
                    campaign.setLeadTarget(lt > 0 ? lt : campaign.getLeadTarget());
                } catch (NumberFormatException ex) {
                    // ignore
                }

                campaign.setDescription(descriptionArea.getText());

                // Persist
                try {
                    campaignFacade.updateCampaign(campaign);
                    JOptionPane.showMessageDialog(dialog, "Campaign updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCampaigns();
                } catch (com.marketing.exception.CampaignNotFoundException cnf) {
                    JOptionPane.showMessageDialog(dialog, "Campaign not found: " + cnf.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (com.marketing.exception.CampaignStateException cse) {
                    JOptionPane.showMessageDialog(dialog, "Error updating campaign: " + cse.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Unexpected error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Handles campaign deletion
     */
    private void handleDelete() {
        if (selectedCampaignId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a campaign to delete", "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this campaign?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (campaignFacade.deleteCampaign(selectedCampaignId)) {
                    JOptionPane.showMessageDialog(this, "Campaign deleted successfully", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadCampaigns();
                }
            } catch (CampaignNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Campaign not found: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles status changes
     */
    private void handleStatusChange(String newStatus) {
        if (selectedCampaignId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a campaign", "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean ok = campaignFacade.changeCampaignStatus(selectedCampaignId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Campaign status changed to " + newStatus, "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadCampaigns();
                // refresh other related panels (lead tracking, analytics) in the UI
                refreshSiblingPanels();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change campaign status", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (com.marketing.exception.CampaignStateException | com.marketing.exception.CampaignNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSiblingPanels() {
        Window root = SwingUtilities.getWindowAncestor(this);
        if (root == null) return;
        java.util.List<Component> found = new java.util.ArrayList<>();
        findComponents(root, found);
        for (Component c : found) {
            if (c instanceof LeadTrackingPanel) {
                try { ((LeadTrackingPanel) c).refreshData(); } catch (Exception ignore) {}
            } else if (c instanceof AnalyticsDashboardPanel) {
                try { ((AnalyticsDashboardPanel) c).refreshAnalyticsNow(); } catch (Exception ignore) {}
            }
        }
    }

    private void findComponents(Component parent, java.util.List<Component> out) {
        if (parent == null) return;
        out.add(parent);
        if (parent instanceof Container) {
            for (Component child : ((Container) parent).getComponents()) {
                findComponents(child, out);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Campaign Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new CampaignManagerPanel());
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.exception.*;
import com.marketing.facade.CampaignFacade;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * CampaignManagerPanel - Swing UI Panel
 * Provides a user interface for creating, viewing, updating, and deleting campaigns.
 * GRASP: Controller (handles all campaign UI requests)
 * SOLID: SRP (single responsibility - manage campaign UI)
 */
public class CampaignManagerPanel extends JPanel {
    private CampaignFacade campaignFacade;
    
    // UI Components
    private JTable campaignTable;
    private DefaultTableModel tableModel;
    private JTextField campaignNameField;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JTextField budgetField;
    private JTextField segmentIdField;
    private JTextField descriptionField;
    private JComboBox<String> statusCombo;
    private JButton createButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private int selectedCampaignId = -1;
    
    /**
     * Constructor
     */
    public CampaignManagerPanel() {
        this.campaignFacade = new CampaignFacade();
        
        initializeUI();
        loadCampaigns();
    }
    
    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Campaign Manager"));
        
        // Top panel - Input fields
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the input panel for campaign details
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Campaign Details"));
        
        // Row 1
        panel.add(new JLabel("Campaign Name:"));
        campaignNameField = new JTextField();
        panel.add(campaignNameField);
        
        panel.add(new JLabel("Budget:"));
        budgetField = new JTextField();
        panel.add(budgetField);
        
        // Row 2
        panel.add(new JLabel("Start Date:"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        panel.add(startDateSpinner);
        
        panel.add(new JLabel("End Date:"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        panel.add(endDateSpinner);
        
        // Row 3
        panel.add(new JLabel("Status:"));
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "PAUSED", "COMPLETED"});
        panel.add(statusCombo);
        
        panel.add(new JLabel("Segment ID:"));
        segmentIdField = new JTextField();
        panel.add(segmentIdField);
        
        // Row 4
        panel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        panel.add(descriptionField);
        
        return panel;
    }
    
    /**
     * Creates the table panel for displaying campaigns
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Campaigns"));
        
        // Create table with columns
        String[] columns = {"ID", "Name", "Start Date", "End Date", "Budget", "Status", "Segment ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        campaignTable = new JTable(tableModel);
        campaignTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        campaignTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(campaignTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        createButton = new JButton("Create");
        createButton.addActionListener(e -> handleCreate());
        panel.add(createButton);
        
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> handleUpdate());
        panel.add(updateButton);
        
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> handleDelete());
        panel.add(deleteButton);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCampaigns());
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * Populates the input fields with selected campaign data
     */
    private void populateFieldsFromSelection() {
        int selectedRow = campaignTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedCampaignId = (Integer) tableModel.getValueAt(selectedRow, 0);
            try {
                Campaign campaign = campaignFacade.getCampaignById(selectedCampaignId);
                campaignNameField.setText(campaign.getCampaignName());
                budgetField.setText(String.valueOf(campaign.getBudget()));
                statusCombo.setSelectedItem(campaign.getStatus());
                segmentIdField.setText(String.valueOf(campaign.getSegmentId()));
                descriptionField.setText(campaign.getDescription() != null ? campaign.getDescription() : "");
            } catch (CampaignNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Campaign not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Handles campaign creation
     */
    private void handleCreate() {
        try {
            String name = campaignNameField.getText();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Campaign name cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Campaign campaign = new Campaign();
            campaign.setCampaignName(name);
            campaign.setBudget(Double.parseDouble(budgetField.getText()));
            campaign.setStatus((String) statusCombo.getSelectedItem());
            campaign.setSegmentId(Integer.parseInt(segmentIdField.getText()));
            campaign.setDescription(descriptionField.getText());
            campaign.setStartDate(LocalDate.now());
            campaign.setEndDate(LocalDate.now().plusMonths(1));
            
            if (campaignFacade.createCampaign(campaign)) {
                JOptionPane.showMessageDialog(this, "Campaign created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadCampaigns();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (CampaignCreationException ex) {
            JOptionPane.showMessageDialog(this, "Failed to create campaign: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles campaign update
     */
    private void handleUpdate() {
        if (selectedCampaignId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a campaign to update", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Campaign campaign = new Campaign();
            campaign.setCampaignId(selectedCampaignId);
            campaign.setCampaignName(campaignNameField.getText());
            campaign.setBudget(Double.parseDouble(budgetField.getText()));
            campaign.setStatus((String) statusCombo.getSelectedItem());
            campaign.setSegmentId(Integer.parseInt(segmentIdField.getText()));
            campaign.setDescription(descriptionField.getText());
            campaign.setStartDate(LocalDate.now());
            campaign.setEndDate(LocalDate.now().plusMonths(1));
            
            if (campaignFacade.updateCampaign(campaign)) {
                JOptionPane.showMessageDialog(this, "Campaign updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadCampaigns();
            }
        } catch (CampaignNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Campaign not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles campaign deletion
     */
    private void handleDelete() {
        if (selectedCampaignId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a campaign to delete", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this campaign?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (campaignFacade.deleteCampaign(selectedCampaignId)) {
                    JOptionPane.showMessageDialog(this, "Campaign deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadCampaigns();
                }
            } catch (CampaignNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Campaign not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Loads all campaigns into the table
     */
    private void loadCampaigns() {
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();
        
        for (Campaign campaign : campaigns) {
            Object[] row = {
                campaign.getCampaignId(),
                campaign.getCampaignName(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getBudget(),
                campaign.getStatus(),
                campaign.getSegmentId()
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Clears all input fields
     */
    private void clearFields() {
        campaignNameField.setText("");
        budgetField.setText("");
        statusCombo.setSelectedIndex(0);
        segmentIdField.setText("");
        descriptionField.setText("");
        selectedCampaignId = -1;
        campaignTable.clearSelection();
    }
}

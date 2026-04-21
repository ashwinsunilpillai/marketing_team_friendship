package com.marketing.ui;

import com.marketing.entity.Campaign;
import com.marketing.facade.CampaignFacade;
import com.marketing.facade.LeadFacade;
import com.marketing.entity.Lead;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * LeadTrackingPanel - Displays lead tracking metrics and performance
 */
public class LeadTrackingPanel extends JPanel {
    private CampaignFacade campaignFacade;
    private JTable leadTable;
    private DefaultTableModel tableModel;
    private JLabel totalLeadsLabel;
    private JLabel totalTargetLabel;
    private JLabel overallRateLabel;
    private JLabel activeCampaignsLabel;
    private int selectedCampaignId = -1;
    private final LeadFacade leadFacade = new LeadFacade();
    private JComboBox<String> statusFilterCombo;

    public LeadTrackingPanel() {
        this.campaignFacade = new CampaignFacade();
        initializeUI();
        loadLeadData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(new Color(245, 247, 250));

        // Metrics panel
        JPanel metricsPanel = createMetricsPanel();
        add(metricsPanel, BorderLayout.NORTH);

        // Filter and table panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Filter panel
        contentPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // Table panel
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel card1 = createMetricCard("—", "Total Leads", new Color(76, 175, 80),
                (label) -> totalLeadsLabel = label);
        panel.add(card1);

        JPanel card2 = createMetricCard("—", "Total Target", new Color(33, 150, 243),
                (label) -> totalTargetLabel = label);
        panel.add(card2);

        JPanel card3 = createMetricCard("—", "Overall Rate", new Color(255, 152, 0),
                (label) -> overallRateLabel = label);
        panel.add(card3);

        JPanel card4 = createMetricCard("—", "Active Campaigns", new Color(156, 39, 176),
                (label) -> activeCampaignsLabel = label);
        panel.add(card4);

        return panel;
    }

    private JPanel createMetricCard(String value, String label, Color accentColor,
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

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilterCombo = new JComboBox<>(new String[] { "All Statuses", "ACTIVE", "PAUSED", "COMPLETED", "PLANNED" });
        statusFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilterCombo.setPreferredSize(new Dimension(140, 25));
        panel.add(statusLabel);
        panel.add(statusFilterCombo);

        // Apply filter immediately when status changes
        statusFilterCombo.addActionListener(e -> loadLeadData());

        panel.add(Box.createHorizontalGlue());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        refreshButton.setBackground(new Color(200, 200, 200));
        refreshButton.setForeground(new Color(45, 55, 72));
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        refreshButton.addActionListener(e -> loadLeadData());
        panel.add(refreshButton);

        JButton viewLeadsButton = new JButton("View Leads");
        viewLeadsButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        viewLeadsButton.setBackground(new Color(36, 99, 235));
        viewLeadsButton.setForeground(Color.WHITE);
        viewLeadsButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        viewLeadsButton.addActionListener(e -> {
            if (selectedCampaignId > 0) {
                showLeadListDialog(selectedCampaignId);
            } else {
                JOptionPane.showMessageDialog(this, "Select a campaign to view its leads.", "Lead Listing", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panel.add(viewLeadsButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "Campaign", "Type", "Status", "Lead Target", "Leads Generated", "Conversion %",
                "Progress" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leadTable = new JTable(tableModel);
        leadTable.setRowHeight(36);
        leadTable.setRowSelectionAllowed(true);
        leadTable.setColumnSelectionAllowed(false);
        leadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leadTable.setBackground(Color.WHITE);
        leadTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leadTable.getSelectedRow() >= 0) {
                int viewRow = leadTable.getSelectedRow();
                int modelRow = leadTable.convertRowIndexToModel(viewRow);
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

        // Style header
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(63, 81, 181));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < leadTable.getColumnCount(); i++) {
            leadTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Status renderer
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
                    }
                }
                return label;
            }
        };
        leadTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);

        // Progress bar renderer
        DefaultTableCellRenderer progressRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.WHITE);

                if (value != null) {
                    try {
                        double percentage = Double.parseDouble(value.toString().replaceAll("%", ""));

                        JProgressBar progressBar = new JProgressBar(0, 100);
                        progressBar.setValue((int) percentage);
                        progressBar.setStringPainted(true);
                        progressBar.setString(String.format("%.0f%%", percentage));
                        progressBar.setFont(new Font("SansSerif", Font.BOLD, 10));

                        // Color based on percentage
                        if (percentage >= 75) {
                            progressBar.setForeground(new Color(76, 175, 80)); // Green
                        } else if (percentage >= 50) {
                            progressBar.setForeground(new Color(33, 150, 243)); // Blue
                        } else if (percentage >= 25) {
                            progressBar.setForeground(new Color(255, 152, 0)); // Orange/Yellow
                        } else {
                            progressBar.setForeground(new Color(244, 67, 54)); // Red
                        }

                        panel.add(progressBar, BorderLayout.CENTER);
                    } catch (Exception e) {
                        JLabel label = new JLabel(value.toString());
                        panel.add(label, BorderLayout.CENTER);
                    }
                }
                return panel;
            }
        };
        leadTable.getColumnModel().getColumn(7).setCellRenderer(progressRenderer);

        JScrollPane scrollPane = new JScrollPane(leadTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 232), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadLeadData() {
        // clear selection because we'll reload the table
        selectedCampaignId = -1;
        if (leadTable != null) {
            leadTable.clearSelection();
        }
        tableModel.setRowCount(0);
        List<Campaign> campaigns = campaignFacade.getAllCampaigns();

        int totalLeads = 0;
        int totalTarget = 0;
        int activeCampaigns = 0;

        String selectedStatus = null;
        if (statusFilterCombo != null) selectedStatus = (String) statusFilterCombo.getSelectedItem();

        for (Campaign campaign : campaigns) {
            // apply status filter if any
            if (selectedStatus != null && !"All Statuses".equals(selectedStatus)) {
                if (!selectedStatus.equals(campaign.getStatus())) continue;
            }

            int leadTarget = campaign.getLeadTarget();
            int leadsGenerated = campaign.getLeadsGenerated();

            totalLeads += leadsGenerated;
            totalTarget += leadTarget;

            if ("ACTIVE".equals(campaign.getStatus())) {
                activeCampaigns++;
            }

            double conversionRate = leadTarget > 0 ? (double) leadsGenerated / leadTarget * 100 : 0;

            Object[] row = {
                    campaign.getCampaignId(),
                    campaign.getCampaignName(),
                    campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL",
                    campaign.getStatus(),
                    String.valueOf(leadTarget),
                    String.valueOf(leadsGenerated),
                    String.format("%.1f%%", conversionRate),
                    String.format("%.0f%%", conversionRate)
            };
            tableModel.addRow(row);
        }

        // Update metrics
        double overallRate = totalTarget > 0 ? (double) totalLeads / totalTarget * 100 : 0;

        if (totalLeadsLabel != null)
            totalLeadsLabel.setText(String.valueOf(totalLeads));
        if (totalTargetLabel != null)
            totalTargetLabel.setText(String.valueOf(totalTarget));
        if (overallRateLabel != null)
            overallRateLabel.setText(String.format("%.1f%%", overallRate));
        if (activeCampaignsLabel != null)
            activeCampaignsLabel.setText(String.valueOf(activeCampaigns));
    }

    private void showLeadListDialog(int campaignId) {
        List<Lead> leads = leadFacade.getLeadsByCampaign(campaignId);
        if (leads == null || leads.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No leads found for the selected campaign.", "Leads", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String title = "Leads for Campaign " + campaignId;
        try {
            Campaign c = campaignFacade.getCampaignById(campaignId);
            if (c != null && c.getCampaignName() != null && !c.getCampaignName().isEmpty()) {
                title = "Leads for: " + c.getCampaignName() + " (" + campaignId + ")";
            }
        } catch (Exception ignore) {}

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(900, 520);
        dialog.setLocationRelativeTo(this);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel leftSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Filter:");
        JTextField searchField = new JTextField(28);
        JButton searchBtn = new JButton("Search");
        leftSearch.add(searchLabel);
        leftSearch.add(searchField);
        leftSearch.add(searchBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addLeadBtn = new JButton("Add Lead");
        JButton qualifyBtn = new JButton("Mark Qualified");
        JButton convertBtn = new JButton("Convert + Revenue");
        JButton exportBtn = new JButton("Export CSV");
        JButton copyBtn = new JButton("Copy Details");
        rightActions.add(addLeadBtn);
        rightActions.add(qualifyBtn);
        rightActions.add(convertBtn);
        rightActions.add(exportBtn);
        rightActions.add(copyBtn);

        top.add(leftSearch, BorderLayout.WEST);
        top.add(rightActions, BorderLayout.EAST);

        String[] cols = { "ID", "Name", "Email", "State", "Created At", "Updated At" };
        DefaultTableModel leadModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable leadsTable = new JTable(leadModel);
        leadsTable.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(leadsTable);

        java.util.List<Lead> filtered = new java.util.ArrayList<>(leads);
        final int pageSize = 10;
        final int[] page = new int[] { 1 };
        final int[] totalPages = new int[] { Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize)) };
        final JLabel pageLabel = new JLabel("Page " + page[0] + " of " + totalPages[0]);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Runnable loadPage = () -> {
            leadModel.setRowCount(0);
            int start = (page[0] - 1) * pageSize;
            int end = Math.min(start + pageSize, filtered.size());
            for (int i = start; i < end; i++) {
                Lead l = filtered.get(i);
                leadModel.addRow(new Object[] {
                        l.getLeadId(),
                        l.getName(),
                        l.getEmail(),
                        l.getState(),
                        l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "",
                        l.getUpdatedAt() != null ? l.getUpdatedAt().format(fmt) : ""
                });
            }
        };

        Runnable refreshLists = () -> {
            leads.clear();
            leads.addAll(leadFacade.getLeadsByCampaign(campaignId));
            String q = searchField.getText().trim().toLowerCase();
            filtered.clear();
            if (q.isEmpty()) {
                filtered.addAll(leads);
            } else {
                for (Lead l : leads) {
                    String name = l.getName() != null ? l.getName().toLowerCase() : "";
                    String email = l.getEmail() != null ? l.getEmail().toLowerCase() : "";
                    String state = l.getState() != null ? l.getState().toLowerCase() : "";
                    if (name.contains(q) || email.contains(q) || state.contains(q)) {
                        filtered.add(l);
                    }
                }
            }
            page[0] = 1;
            totalPages[0] = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
            loadPage.run();
            pageLabel.setText("Page " + page[0] + " of " + totalPages[0]);
            refreshData();
            refreshSiblingPanels();
        };

        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        prev.addActionListener(e -> {
            if (page[0] > 1) { page[0]--; loadPage.run(); pageLabel.setText("Page " + page[0] + " of " + totalPages[0]); }
        });
        next.addActionListener(e -> {
            if (page[0] < totalPages[0]) { page[0]++; loadPage.run(); pageLabel.setText("Page " + page[0] + " of " + totalPages[0]); }
        });

        // Detail panel
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(251, 252, 253));
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setPreferredSize(new Dimension(860, 120));

        leadsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && leadsTable.getSelectedRow() >= 0) {
                int viewRow = leadsTable.getSelectedRow();
                int modelRow = leadsTable.convertRowIndexToModel(viewRow);
                int index = (page[0] - 1) * pageSize + modelRow;
                if (index >= 0 && index < filtered.size()) {
                    Lead l = filtered.get(index);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Lead ID: ").append(l.getLeadId()).append('\n');
                    sb.append("Name: ").append(l.getName()).append('\n');
                    sb.append("Email: ").append(l.getEmail()).append('\n');
                    sb.append("State: ").append(l.getState()).append('\n');
                    sb.append("Created: ").append(l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "").append('\n');
                    sb.append("Updated: ").append(l.getUpdatedAt() != null ? l.getUpdatedAt().format(fmt) : "").append('\n');
                    detailArea.setText(sb.toString());
                }
            }
        });

        // Search
        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim().toLowerCase();
            filtered.clear();
            if (q.isEmpty()) { filtered.addAll(leads); }
            else {
                for (Lead l : leads) {
                    String name = l.getName() != null ? l.getName().toLowerCase() : "";
                    String email = l.getEmail() != null ? l.getEmail().toLowerCase() : "";
                    String state = l.getState() != null ? l.getState().toLowerCase() : "";
                    if (name.contains(q) || email.contains(q) || state.contains(q)) filtered.add(l);
                }
            }
            page[0] = 1;
            totalPages[0] = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
            loadPage.run();
            pageLabel.setText("Page " + page[0] + " of " + totalPages[0]);
        });

        addLeadBtn.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            Object[] msg = {
                    "Name:", nameField,
                    "Email:", emailField
            };
            int ok = JOptionPane.showConfirmDialog(dialog, msg, "Add Lead", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION) {
                String n = nameField.getText().trim();
                String em = emailField.getText().trim();
                if (n.isEmpty() || em.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and email are required.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Lead nl = new Lead();
                nl.setName(n);
                nl.setEmail(em);
                nl.setCampaignId(campaignId);
                nl.setState("New");
                if (leadFacade.createLead(nl)) {
                    refreshLists.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Could not create lead.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        qualifyBtn.addActionListener(e -> {
            int sr = leadsTable.getSelectedRow();
            if (sr < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a lead first.", "Lead State",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = leadsTable.convertRowIndexToModel(sr);
            int index = (page[0] - 1) * pageSize + modelRow;
            if (index < 0 || index >= filtered.size()) {
                return;
            }
            Lead l = filtered.get(index);
            if (leadFacade.transitionLeadState(l.getLeadId(), "Qualified")) {
                refreshLists.run();
            } else {
                JOptionPane.showMessageDialog(dialog, "Transition to Qualified is not allowed.", "Lead State",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        convertBtn.addActionListener(e -> {
            int sr = leadsTable.getSelectedRow();
            if (sr < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a lead first.", "Lead Conversion",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = leadsTable.convertRowIndexToModel(sr);
            int index = (page[0] - 1) * pageSize + modelRow;
            if (index < 0 || index >= filtered.size()) {
                return;
            }

            Lead l = filtered.get(index);
            String amount = JOptionPane.showInputDialog(dialog, "Closed revenue amount for this lead:", "0.00");
            if (amount == null) return;

            double revenue;
            try {
                revenue = Double.parseDouble(amount.trim());
                if (revenue < 0) {
                    JOptionPane.showMessageDialog(dialog, "Revenue cannot be negative.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid revenue amount.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (leadFacade.convertLeadWithRevenue(l.getLeadId(), revenue)) {
                refreshLists.run();
                JOptionPane.showMessageDialog(dialog,
                        "Lead converted. Revenue attributed to campaign analytics.",
                        "Converted",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Conversion failed. Ensure lead is New/Qualified and try again.",
                        "Lead Conversion",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Export
        exportBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("lead_id,name,email,state,created_at,updated_at\n");
            for (Lead l : filtered) {
                sb.append(l.getLeadId()).append(",\"").append(l.getName() != null ? l.getName().replace("\"","\"\"") : "").append("\",")
                  .append("\"").append(l.getEmail() != null ? l.getEmail().replace("\"","\"\"") : "").append("\",")
                  .append("\"").append(l.getState() != null ? l.getState().replace("\"","\"\"") : "").append("\",")
                  .append(l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "").append(",")
                  .append(l.getUpdatedAt() != null ? l.getUpdatedAt().format(fmt) : "").append('\n');
            }
            StringSelection sel = new StringSelection(sb.toString());
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(sel, null);
            JOptionPane.showMessageDialog(dialog, "Exported " + filtered.size() + " leads to clipboard as CSV.", "Export", JOptionPane.INFORMATION_MESSAGE);
        });

        // Copy details
        copyBtn.addActionListener(e -> {
            int sr = leadsTable.getSelectedRow();
            if (sr < 0) { JOptionPane.showMessageDialog(dialog, "Select a lead to copy details.", "Copy", JOptionPane.INFORMATION_MESSAGE); return; }
            int modelRow = leadsTable.convertRowIndexToModel(sr);
            int index = (page[0] - 1) * pageSize + modelRow;
            if (index >= 0 && index < filtered.size()) {
                Lead l = filtered.get(index);
                StringBuilder sb = new StringBuilder();
                sb.append("Lead ID: ").append(l.getLeadId()).append('\n');
                sb.append("Name: ").append(l.getName()).append('\n');
                sb.append("Email: ").append(l.getEmail()).append('\n');
                StringSelection sel = new StringSelection(sb.toString());
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                cb.setContents(sel, null);
                JOptionPane.showMessageDialog(dialog, "Copied lead details to clipboard.", "Copy", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Double-click detail
        leadsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = leadsTable.getSelectedRow();
                    if (r >= 0) {
                        int modelRow = leadsTable.convertRowIndexToModel(r);
                        int index = (page[0] - 1) * pageSize + modelRow;
                        if (index >= 0 && index < filtered.size()) {
                            Lead l = filtered.get(index);
                            JTextArea ta = new JTextArea();
                            ta.setEditable(false);
                            ta.setLineWrap(true);
                            ta.setWrapStyleWord(true);
                            ta.setText("Lead ID: " + l.getLeadId() + "\nName: " + l.getName() + "\nEmail: " + l.getEmail() + "\n\nState: " + l.getState());
                            JScrollPane sp = new JScrollPane(ta);
                            sp.setPreferredSize(new Dimension(700, 360));
                            JOptionPane.showMessageDialog(dialog, sp, "Lead Details", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pager.add(prev);
        pager.add(pageLabel);
        pager.add(next);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.add(detailScroll, BorderLayout.CENTER);
        bottom.add(pager, BorderLayout.SOUTH);
        content.add(bottom, BorderLayout.SOUTH);

        loadPage.run();

        dialog.add(content);
        dialog.setVisible(true);
    }

    /**
     * Public refresh method so parent containers (tabs) can request a data reload.
     */
    public void refreshData() {
        loadLeadData();
    }

    private void refreshSiblingPanels() {
        Window root = SwingUtilities.getWindowAncestor(this);
        if (root == null) return;
        java.util.List<Component> found = new java.util.ArrayList<>();
        findComponents(root, found);
        for (Component c : found) {
            if (c instanceof AnalyticsDashboardPanel) {
                try {
                    ((AnalyticsDashboardPanel) c).refreshAnalyticsNow();
                } catch (Exception ignore) {
                }
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
}

package com.marketing.ui;

import com.marketing.entity.Customer;
import com.marketing.m2.crm.CRMService;
import com.marketing.m2.email.BasicEmailTemplate;
import com.marketing.m2.email.EmailService;
import com.marketing.m2.email.EmailTemplate;
import com.marketing.entity.EmailTemplateRecord;
import com.marketing.entity.EmailRecord;
import com.marketing.facade.EmailTemplateFacade;
import com.marketing.facade.LeadFacade;
import com.marketing.facade.CRMLogFacade;
import com.marketing.entity.CRMSyncLog;
import com.marketing.m2.exceptions.EmailSendException;
import com.marketing.m2.exceptions.InvalidEmailTemplateException;
import com.marketing.m2.lead.Lead;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Engagement Hub panel.
 * Exposes the email, CRM, and lead flows defined in the team plan.
 */
public class Member2Panel extends JPanel {
    private final EmailService emailService = new EmailService();
    private final CRMService crmService = new CRMService();

    private final JTextArea activityLog = new JTextArea();
    private final JTextField recipientField = new JTextField();
    private final JTextField emailMessageField = new JTextField("Welcome to our campaign update.");
    private final JTextField templateNameField = new JTextField();
    private final JTextField templateSubjectField = new JTextField();
    private javax.swing.JComboBox<EmailTemplateRecord> templateCombo;
    private final JTextField crmCustomerIdField = new JTextField();
    private final JTextArea customerDetailsArea = new JTextArea();
    private final JTextField leadNameField = new JTextField();
    private final JLabel leadStateLabel = new JLabel("No lead created");
    private final JTextArea leadTimelineArea = new JTextArea();

    private Lead currentLead;
    private final EmailTemplateFacade templateFacade = new EmailTemplateFacade();
    private final LeadFacade leadFacade = new LeadFacade();
    private final CRMLogFacade crmLogFacade = new CRMLogFacade();

    public Member2Panel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 247, 250));

        JTabbedPane memberTabs = new JTabbedPane();
        memberTabs.addTab("Email Campaigns", createEmailPanel());
        memberTabs.addTab("Customer CRM", createCrmPanel());
        memberTabs.addTab("Lead Lifecycle", createLeadPanel());

        add(createPanelHeader(), BorderLayout.NORTH);
        add(memberTabs, BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);
    }

    private JComponent createPanelHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 2, 4, 2));

        JLabel title = new JLabel("Engagement Hub");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Email outreach, customer lookup, and lead progression");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitle.setForeground(new Color(90, 98, 110));

        JPanel textBlock = new JPanel(new BorderLayout());
        textBlock.setOpaque(false);
        textBlock.add(title, BorderLayout.NORTH);
        textBlock.add(subtitle, BorderLayout.SOUTH);

        header.add(textBlock, BorderLayout.WEST);
        return header;
    }

    private JPanel createEmailPanel() {
        JPanel panel = createCardPanel("Email Campaigns");

        GridBagConstraints constraints = baseConstraints();
        addFormRow(panel, constraints, 0, "Recipient Email", recipientField);
        addFormRow(panel, constraints, 1, "Message", emailMessageField);

        // Template management
        addFormRow(panel, constraints, 2, "Template Name", templateNameField);
        addFormRow(panel, constraints, 3, "Template Subject", templateSubjectField);

        templateCombo = new javax.swing.JComboBox<>();
        constraints.gridy = 4;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        JLabel pickLabel = new JLabel("Select Template:");
        panel.add(pickLabel, constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(templateCombo, constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton sendButton = createActionButton("Send Campaign Email");
        sendButton.addActionListener(e -> handleSendEmail());
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(sendButton, constraints);

        JButton saveTemplateBtn = createActionButton("Save Template");
        saveTemplateBtn.addActionListener(e -> handleSaveTemplate());
        constraints.gridx = 2;
        constraints.gridy = 3;
        panel.add(saveTemplateBtn, constraints);

        JButton viewEmailHistory = createActionButton("View Email History");
        viewEmailHistory.addActionListener(e -> showEmailHistoryDialog());
        constraints.gridx = 2;
        constraints.gridy = 2;
        panel.add(viewEmailHistory, constraints);

        loadTemplates();

        return panel;
    }

    private JPanel createCrmPanel() {
        JPanel panel = createCardPanel("Customer CRM");
        panel.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = baseConstraints();
        addFormRow(top, constraints, 0, "Customer ID", crmCustomerIdField);

        JButton fetchButton = createActionButton("Lookup Customer");
        fetchButton.addActionListener(e -> handleFetchCustomer());
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        top.add(fetchButton, constraints);

        JButton viewCrmLogBtn = createActionButton("View CRM Log");
        viewCrmLogBtn.addActionListener(e -> showCrmLogDialog());
        constraints.gridx = 2;
        constraints.gridy = 1;
        top.add(viewCrmLogBtn, constraints);

        customerDetailsArea.setEditable(false);
        customerDetailsArea.setLineWrap(true);
        customerDetailsArea.setWrapStyleWord(true);
        customerDetailsArea.setBackground(new Color(251, 252, 253));
        JScrollPane detailsScrollPane = new JScrollPane(customerDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Customer Profile"));

        panel.add(top, BorderLayout.NORTH);
        panel.add(detailsScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeadPanel() {
        JPanel panel = createCardPanel("Lead Lifecycle");

        GridBagConstraints constraints = baseConstraints();
        addFormRow(panel, constraints, 0, "Lead Name", leadNameField);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;

        JButton createButton = createActionButton("Create Lead");
        createButton.addActionListener(e -> handleCreateLead());
        panel.add(createButton, constraints);

        JButton progressButton = createActionButton("Advance Stage");
        progressButton.addActionListener(e -> handleProgressLead());
        constraints.gridx = 2;
        panel.add(progressButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        leadStateLabel.setFont(leadStateLabel.getFont().deriveFont(Font.BOLD));
        leadTimelineArea.setEditable(false);
        leadTimelineArea.setLineWrap(true);
        leadTimelineArea.setWrapStyleWord(true);
        leadTimelineArea.setBackground(new Color(251, 252, 253));
        JScrollPane timelineScrollPane = new JScrollPane(leadTimelineArea);
        timelineScrollPane.setBorder(BorderFactory.createTitledBorder("Lead Activity"));
        panel.add(leadStateLabel, constraints);
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1.0;
        panel.add(timelineScrollPane, constraints);

        return panel;
    }

    private JScrollPane createLogPanel() {
        activityLog.setEditable(false);
        activityLog.setLineWrap(true);
        activityLog.setWrapStyleWord(true);
        activityLog.setRows(6);
        activityLog.setBackground(new Color(251, 252, 253));
        JScrollPane scrollPane = new JScrollPane(activityLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Interaction Log"));
        return scrollPane;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                new EmptyBorder(6, 6, 6, 6)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(36, 99, 235));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        return constraints;
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int row, String labelText, JComponent field) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 13f));
        if (field instanceof JTextField textField) {
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 218, 230)),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }
        panel.add(field, constraints);
    }

    private void handleSendEmail() {
        String recipient = recipientField.getText().trim();
        String message = emailMessageField.getText().trim();

        if (recipient.isEmpty()) {
            showError("Recipient email is required.");
            return;
        }

        // Determine template/subject
        EmailTemplateRecord selected = (EmailTemplateRecord) (templateCombo.getSelectedItem());
        String subject = templateSubjectField.getText().trim();
        String body = message;
        int templateId = -1;
        if (selected != null) {
            templateId = selected.getTemplateId();
            if (selected.getSubject() != null && !selected.getSubject().isEmpty()) subject = selected.getSubject();
            body = selected.getBody() != null && !selected.getBody().isEmpty() ? selected.getBody() : body;
        }

        EmailRecord emailRecord = new EmailRecord();
        emailRecord.setTemplateId(templateId);
        emailRecord.setRecipient(recipient);
        emailRecord.setSubject(subject != null && !subject.isEmpty() ? subject : "Campaign Message");
        emailRecord.setBody(body);
        emailRecord.setStatus("PENDING");

        int emailId = templateFacade.createEmailRecord(emailRecord);

        try {
            EmailTemplate templateObj = new BasicEmailTemplate(body);
            emailService.sendEmail(recipient, templateObj);
            appendLog("Email sent to " + recipient);
            if (emailId > 0) templateFacade.updateEmailStatus(emailId, "SENT");
        } catch (InvalidEmailTemplateException | EmailSendException ex) {
            showError("Email failed: " + ex.getMessage());
            if (emailId > 0) templateFacade.updateEmailStatus(emailId, "FAILED");
        }
    }

    private void handleFetchCustomer() {
        String customerId = crmCustomerIdField.getText().trim();

        if (customerId.isEmpty()) {
            showError("Customer ID is required.");
            return;
        }

        Customer customer = crmService.getCustomer(customerId);
        if (customer == null) {
            customerDetailsArea.setText("No customer returned from CRM.");
            appendLog("CRM lookup returned no customer for ID " + customerId);
            return;
        }

        customerDetailsArea.setText(formatCustomer(customer));
        appendLog("Loaded CRM customer ID " + customer.getCustomerId());
    }

    private void handleCreateLead() {
        String leadName = leadNameField.getText().trim();
        if (leadName.isEmpty()) {
            showError("Lead name is required.");
            return;
        }

        // create in-memory lead for lifecycle actions
        currentLead = new Lead(leadName);
        leadStateLabel.setText("Current state: " + currentLead.getStateName());
        appendLog("Created lead: " + leadName + " in state " + currentLead.getStateName());
        leadTimelineArea.setText("Lead created: " + leadName + System.lineSeparator() +
            "Current state: " + currentLead.getStateName());

        // persist lead to database (simple mapping)
        try {
            com.marketing.entity.Lead newLead = new com.marketing.entity.Lead();
            newLead.setName(leadName);
            newLead.setState("New");
            newLead.setCampaignId(0);
            if (leadFacade.createLead(newLead)) {
                appendLog("Lead persisted with id " + newLead.getLeadId());
            } else {
                appendLog("Failed to persist lead to database");
            }
        } catch (Exception ex) {
            appendLog("Error persisting lead: " + ex.getMessage());
        }
    }

    private void handleProgressLead() {
        if (currentLead == null) {
            showError("Create a lead before progressing it.");
            return;
        }

        currentLead.progress();
        leadStateLabel.setText("Current state: " + currentLead.getStateName());
        appendLog("Lead " + currentLead.getName() + " moved to " + currentLead.getStateName());
        leadTimelineArea.append(System.lineSeparator() + "Advanced to: " + currentLead.getStateName());
    }

    private String formatCustomer(Customer customer) {
        StringBuilder builder = new StringBuilder();
        builder.append("Customer ID: ").append(customer.getCustomerId()).append('\n');
        builder.append("Name: ").append(customer.getFirstName()).append(' ').append(customer.getLastName()).append('\n');
        builder.append("Email: ").append(customer.getEmail()).append('\n');
        builder.append("Phone: ").append(customer.getPhone()).append('\n');
        builder.append("City: ").append(customer.getCity()).append('\n');
        builder.append("Age: ").append(customer.getAge()).append('\n');
        builder.append("Interest: ").append(customer.getInterest()).append('\n');
        builder.append("Status: ").append(customer.getStatus());
        return builder.toString();
    }

    private void appendLog(String message) {
        activityLog.append(message + System.lineSeparator());
        activityLog.setCaretPosition(activityLog.getDocument().getLength());
    }

    private void showError(String message) {
        appendLog(message);
        JOptionPane.showMessageDialog(this, message, "Engagement Hub", JOptionPane.WARNING_MESSAGE);
    }

    private void loadTemplates() {
        try {
            templateCombo.removeAllItems();
            java.util.List<EmailTemplateRecord> templates = templateFacade.getAllTemplates();
            for (EmailTemplateRecord t : templates) {
                templateCombo.addItem(t);
            }
        } catch (Exception ex) {
            appendLog("Error loading templates: " + ex.getMessage());
        }
    }

    private void handleSaveTemplate() {
        String name = templateNameField.getText().trim();
        String subject = templateSubjectField.getText().trim();
        String body = emailMessageField.getText().trim();

        if (name.isEmpty()) {
            showError("Template name is required.");
            return;
        }

        EmailTemplateRecord t = new EmailTemplateRecord();
        t.setName(name);
        t.setSubject(subject);
        t.setBody(body);

        if (templateFacade.createTemplate(t)) {
            appendLog("Saved template: " + name + " (id=" + t.getTemplateId() + ")");
            loadTemplates();
            templateCombo.setSelectedItem(t);
        } else {
            showError("Failed to save template.");
        }
    }

    private void showCrmLogDialog() {
        java.util.List<CRMSyncLog> logs = crmLogFacade.getRecentLogs(1000);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "CRM Sync Log", Dialog.ModalityType.APPLICATION_MODAL);
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
            JButton exportBtn = createActionButton("Export CSV");
            JButton copyBtn = createActionButton("Copy Details");
            rightActions.add(exportBtn);
            rightActions.add(copyBtn);

        top.add(leftSearch, BorderLayout.WEST);
        top.add(rightActions, BorderLayout.EAST);

        String[] cols = { "ID", "Source", "Details", "Created At" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);

        // In-memory filtered list and pagination
        java.util.List<CRMSyncLog> filtered = new java.util.ArrayList<>(logs);
        final int pageSize = 20;
        final int[] page = new int[] { 1 };
        final int[] totalPages = new int[] { Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize)) };

        Runnable loadPage = () -> {
            model.setRowCount(0);
            int start = (page[0] - 1) * pageSize;
            int end = Math.min(start + pageSize, filtered.size());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = start; i < end; i++) {
                CRMSyncLog l = filtered.get(i);
                model.addRow(new Object[] { l.getLogId(), l.getSource(), l.getDetails(), l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "" });
            }
        };

        loadPage.run();

        JScrollPane scroll = new JScrollPane(table);

        // Pager
        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        JLabel pageLabel = new JLabel("Page " + page[0] + " of " + totalPages[0]);
        prev.addActionListener(e -> {
            if (page[0] > 1) {
                page[0]--;
                loadPage.run();
                pageLabel.setText("Page " + page[0] + " of " + totalPages[0]);
            }
        });
        next.addActionListener(e -> {
            if (page[0] < totalPages[0]) {
                page[0]++;
                loadPage.run();
                pageLabel.setText("Page " + page[0] + " of " + totalPages[0]);
            }
        });
        pager.add(prev);
        pager.add(pageLabel);
        pager.add(next);

        // Search action
        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim().toLowerCase();
            filtered.clear();
            if (q.isEmpty()) {
                filtered.addAll(logs);
            } else {
                for (CRMSyncLog l : logs) {
                    String src = l.getSource() != null ? l.getSource().toLowerCase() : "";
                    String details = l.getDetails() != null ? l.getDetails().toLowerCase() : "";
                    if (src.contains(q) || details.contains(q)) filtered.add(l);
                }
            }
            page[0] = 1;
            totalPages[0] = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
            loadPage.run();
            pageLabel.setText("Page " + page[0] + " of " + totalPages[0]);
        });

        // Copy selected details
        copyBtn.addActionListener(e -> {
            int sr = table.getSelectedRow();
            if (sr < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a log row to copy details.", "Copy", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(sr);
            int index = (page[0] - 1) * pageSize + modelRow;
            if (index >= 0 && index < filtered.size()) {
                CRMSyncLog l = filtered.get(index);
                String text = "Log ID: " + l.getLogId() + "\nSource: " + l.getSource() + "\nDetails:\n" + l.getDetails();
                StringSelection sel = new StringSelection(text);
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                cb.setContents(sel, null);
                JOptionPane.showMessageDialog(dialog, "Copied details to clipboard.", "Copy", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Export filtered as CSV to clipboard
        exportBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("log_id,source,details,created_at\n");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (CRMSyncLog l : filtered) {
                String detailsEsc = l.getDetails() != null ? l.getDetails().replace("\"", "\"\"") : "";
                sb.append(l.getLogId()).append(",\"").append(l.getSource() != null ? l.getSource().replace("\"", "\"\"") : "").append("\",")
                  .append("\"").append(detailsEsc).append("\",")
                  .append(l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "").append('\n');
            }
            StringSelection sel = new StringSelection(sb.toString());
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(sel, null);
            JOptionPane.showMessageDialog(dialog, "Exported " + filtered.size() + " rows to clipboard as CSV.", "Export", JOptionPane.INFORMATION_MESSAGE);
        });

        // Double-click to view details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        int modelRow = table.convertRowIndexToModel(r);
                        int idx = (page[0] - 1) * pageSize + modelRow;
                        if (idx >= 0 && idx < filtered.size()) {
                            CRMSyncLog l = filtered.get(idx);
                            JTextArea ta = new JTextArea();
                            ta.setEditable(false);
                            ta.setLineWrap(true);
                            ta.setWrapStyleWord(true);
                            ta.setText("Log ID: " + l.getLogId() + "\nSource: " + l.getSource() + "\n\nDetails:\n" + l.getDetails());
                            JScrollPane sp = new JScrollPane(ta);
                            sp.setPreferredSize(new Dimension(700, 360));
                            JOptionPane.showMessageDialog(dialog, sp, "CRM Log Details", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(pager, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showEmailHistoryDialog() {
        java.util.List<EmailRecord> emails = templateFacade.getRecentEmails(200);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Email History", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(900, 520);
        dialog.setLocationRelativeTo(this);

        String[] cols = { "ID", "Recipient", "Subject", "Status", "Sent At", "Created At" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        for (EmailRecord e : emails) {
            model.addRow(new Object[] { e.getEmailId(), e.getRecipient(), e.getSubject(), e.getStatus(), e.getSentAt() != null ? e.getSentAt().toString() : "", e.getCreatedAt() != null ? e.getCreatedAt().toString() : "" });
        }

        JScrollPane scroll = new JScrollPane(table);

        // Resend button
        JButton resendBtn = createActionButton("Resend Selected");
        resendBtn.addActionListener(ae -> {
            int sr = table.getSelectedRow();
            if (sr < 0) {
                showError("Select an email to resend.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(sr);
            Object idObj = model.getValueAt(modelRow, 0);
            int emailId = -1;
            try { emailId = Integer.parseInt(String.valueOf(idObj)); } catch (Exception ex) { emailId = -1; }
            if (emailId <= 0) { showError("Invalid email selected."); return; }

            // find record
            EmailRecord rec = null;
            for (EmailRecord er : emails) if (er.getEmailId() == emailId) { rec = er; break; }
            if (rec == null) { showError("Email record not found."); return; }

            try {
                EmailTemplate tmpl = new BasicEmailTemplate(rec.getBody() != null ? rec.getBody() : "");
                emailService.sendEmail(rec.getRecipient(), tmpl);
                templateFacade.updateEmailStatus(emailId, "SENT");
                appendLog("Resent email id=" + emailId + " to " + rec.getRecipient());
                model.setValueAt("SENT", modelRow, 3);
            } catch (Exception ex) {
                templateFacade.updateEmailStatus(emailId, "FAILED");
                appendLog("Resend failed for email id=" + emailId + ": " + ex.getMessage());
                model.setValueAt("FAILED", modelRow, 3);
                showError("Resend failed: " + ex.getMessage());
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(resendBtn);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scroll, BorderLayout.CENTER);
        container.add(bottom, BorderLayout.SOUTH);

        dialog.add(container);
        dialog.setVisible(true);
    }
}
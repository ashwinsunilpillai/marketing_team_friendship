package com.marketing.ui;

import com.marketing.entity.Customer;
import com.marketing.m2.crm.CRMService;
import com.marketing.m2.email.BasicEmailTemplate;
import com.marketing.m2.email.EmailService;
import com.marketing.m2.email.EmailTemplate;
import com.marketing.m2.exceptions.EmailSendException;
import com.marketing.m2.exceptions.InvalidEmailTemplateException;
import com.marketing.m2.lead.Lead;

import javax.swing.*;
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
    private final JTextField crmCustomerIdField = new JTextField();
    private final JTextArea customerDetailsArea = new JTextArea();
    private final JTextField leadNameField = new JTextField();
    private final JLabel leadStateLabel = new JLabel("No lead created");
    private final JTextArea leadTimelineArea = new JTextArea();

    private Lead currentLead;

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

        JButton sendButton = createActionButton("Send Campaign Email");
        sendButton.addActionListener(e -> handleSendEmail());
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(sendButton, constraints);

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

        try {
            EmailTemplate template = new BasicEmailTemplate(message);
            emailService.sendEmail(recipient, template);
            appendLog("Email sent to " + recipient);
        } catch (InvalidEmailTemplateException | EmailSendException ex) {
            showError("Email failed: " + ex.getMessage());
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

        currentLead = new Lead(leadName);
        leadStateLabel.setText("Current state: " + currentLead.getStateName());
        appendLog("Created lead: " + leadName + " in state " + currentLead.getStateName());
        leadTimelineArea.setText("Lead created: " + leadName + System.lineSeparator() +
            "Current state: " + currentLead.getStateName());
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
}
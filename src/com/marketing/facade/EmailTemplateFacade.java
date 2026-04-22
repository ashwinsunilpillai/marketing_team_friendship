package com.marketing.facade;

import com.marketing.entity.EmailRecord;
import com.marketing.entity.EmailTemplateRecord;
import com.marketing.util.DBUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailTemplateFacade {
    private static final String TEMPLATES_TABLE = "email_templates";
    private static final String EMAILS_TABLE = "emails";
    private static final String EMAIL_ID_COLUMN = "email_id";
    private final DBUtil dbUtil = DBUtil.getInstance();

    public boolean createTemplate(EmailTemplateRecord tmpl) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return false;

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", tmpl.getName());
            payload.put("subject", tmpl.getSubject());
            payload.put("body", tmpl.getBody());

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TEMPLATES_TABLE, payload);
            return true;
        } catch (Exception e) {
            System.err.println("Error creating template: " + e.getMessage());
            return false;
        }
    }

    public List<EmailTemplateRecord> getAllTemplates() {
        List<EmailTemplateRecord> results = new ArrayList<>();
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return results;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TEMPLATES_TABLE, new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    EmailTemplateRecord t = new EmailTemplateRecord();
                    if (row.get("template_id") instanceof Number n) t.setTemplateId(n.intValue());
                    t.setName((String) row.get("name"));
                    t.setSubject((String) row.get("subject"));
                    t.setBody((String) row.get("body"));
                    results.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading templates: " + e.getMessage());
        }
        return results;
    }

    public int createEmailRecord(EmailRecord email) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return -1;

            Map<String, Object> payload = new HashMap<>();
            if (email.getTemplateId() > 0) payload.put("template_id", email.getTemplateId());
            payload.put("recipient", email.getRecipient());
            payload.put("subject", email.getSubject());
            payload.put("body", email.getBody());
            payload.put("status", email.getStatus() != null ? email.getStatus() : "PENDING");

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, EMAILS_TABLE, payload);
            return 1;
        } catch (Exception e) {
            System.err.println("Error creating email record: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateEmailStatus(int emailId, String status) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return false;

            Map<String, Object> payload = new HashMap<>();
            payload.put("status", status);
            payload.put("sent_at", LocalDateTime.now());

            marketingSubsystem.getClass()
                    .getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, EMAILS_TABLE, EMAIL_ID_COLUMN, emailId, payload);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating email status: " + e.getMessage());
            return false;
        }
    }

    public List<EmailRecord> getRecentEmails(int limit) {
        List<EmailRecord> results = new ArrayList<>();
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return results;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, EMAILS_TABLE, new HashMap<>());

            if (rows != null) {
                int count = 0;
                for (Map<String, Object> row : rows) {
                    if (count >= limit) break;
                    EmailRecord r = new EmailRecord();
                    if (row.get("email_id") instanceof Number n) r.setEmailId(n.intValue());
                    if (row.get("template_id") instanceof Number n) r.setTemplateId(n.intValue());
                    r.setRecipient((String) row.get("recipient"));
                    r.setSubject((String) row.get("subject"));
                    r.setBody((String) row.get("body"));
                    r.setStatus((String) row.get("status"));
                    results.add(r);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading recent emails: " + e.getMessage());
        }
        return results;
    }
}

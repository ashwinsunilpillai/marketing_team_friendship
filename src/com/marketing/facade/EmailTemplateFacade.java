package com.marketing.facade;

import com.marketing.entity.EmailRecord;
import com.marketing.entity.EmailTemplateRecord;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmailTemplateFacade {
    private final DBUtil dbUtil = DBUtil.getInstance();

    public boolean createTemplate(EmailTemplateRecord tmpl) {
        String sql = "INSERT INTO email_templates (name, subject, body) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tmpl.getName());
            pstmt.setString(2, tmpl.getSubject());
            pstmt.setString(3, tmpl.getBody());

            int affected = pstmt.executeUpdate();
            if (affected == 0) return false;

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) tmpl.setTemplateId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating template: " + e.getMessage());
            return false;
        }
    }

    public List<EmailTemplateRecord> getAllTemplates() {
        List<EmailTemplateRecord> results = new ArrayList<>();
        String sql = "SELECT * FROM email_templates ORDER BY created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                EmailTemplateRecord t = new EmailTemplateRecord();
                t.setTemplateId(rs.getInt("template_id"));
                t.setName(rs.getString("name"));
                t.setSubject(rs.getString("subject"));
                t.setBody(rs.getString("body"));
                results.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error loading templates: " + e.getMessage());
        }
        return results;
    }

    public int createEmailRecord(EmailRecord email) {
        String sql = "INSERT INTO emails (template_id, recipient, subject, body, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (email.getTemplateId() > 0) pstmt.setInt(1, email.getTemplateId()); else pstmt.setNull(1, Types.INTEGER);
            pstmt.setString(2, email.getRecipient());
            pstmt.setString(3, email.getSubject());
            pstmt.setString(4, email.getBody());
            pstmt.setString(5, email.getStatus() != null ? email.getStatus() : "PENDING");

            int affected = pstmt.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating email record: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateEmailStatus(int emailId, String status) {
        String sql = "UPDATE emails SET status = ?, sent_at = ? WHERE email_id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, emailId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating email status: " + e.getMessage());
            return false;
        }
    }

    public List<EmailRecord> getRecentEmails(int limit) {
        List<EmailRecord> results = new ArrayList<>();
        String sql = "SELECT * FROM emails ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = dbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EmailRecord r = new EmailRecord();
                    r.setEmailId(rs.getInt("email_id"));
                    r.setTemplateId(rs.getInt("template_id"));
                    r.setRecipient(rs.getString("recipient"));
                    r.setSubject(rs.getString("subject"));
                    r.setBody(rs.getString("body"));
                    r.setStatus(rs.getString("status"));
                    Timestamp sent = rs.getTimestamp("sent_at");
                    if (sent != null) r.setSentAt(sent.toLocalDateTime());
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) r.setCreatedAt(created.toLocalDateTime());
                    results.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading recent emails: " + e.getMessage());
        }
        return results;
    }
}

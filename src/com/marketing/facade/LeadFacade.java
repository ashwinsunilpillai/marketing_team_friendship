package com.marketing.facade;

import com.marketing.entity.Lead;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeadFacade {
    private final DBUtil dbUtil = DBUtil.getInstance();

    public boolean createLead(Lead lead) {
        String sql = "INSERT INTO leads (name, email, campaign_id, state) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, lead.getName());
            pstmt.setString(2, lead.getEmail());
            pstmt.setInt(3, lead.getCampaignId());
            pstmt.setString(4, lead.getState() != null ? lead.getState() : "New");

            int affected = pstmt.executeUpdate();
            if (affected == 0) return false;

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    lead.setLeadId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating lead: " + e.getMessage());
            // If leads table is missing, attempt to create it and retry once
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("doesn't exist") || msg.contains("unknown table") || msg.contains("no such table")) {
                String ddl = "CREATE TABLE IF NOT EXISTS marketing_erp.leads (lead_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
                try (Connection conn = dbUtil.getConnection(); Statement s = conn.createStatement()) {
                    s.executeUpdate(ddl);
                    // retry insert
                    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setString(1, lead.getName());
                        pstmt.setString(2, lead.getEmail());
                        pstmt.setInt(3, lead.getCampaignId());
                        pstmt.setString(4, lead.getState() != null ? lead.getState() : "New");
                        int affected = pstmt.executeUpdate();
                        if (affected == 0) return false;
                        try (ResultSet keys = pstmt.getGeneratedKeys()) {
                            if (keys.next()) lead.setLeadId(keys.getInt(1));
                        }
                        return true;
                    }
                } catch (SQLException ex) {
                    System.err.println("Retry creating leads table/insert failed: " + ex.getMessage());
                    return false;
                }
            }
            return false;
        }
    }

    public List<Lead> getLeadsByCampaign(int campaignId) {
        List<Lead> leads = new ArrayList<>();
        String sql = "SELECT * FROM leads WHERE campaign_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, campaignId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    leads.add(mapResultSetToLead(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading leads: " + e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("doesn't exist") || msg.contains("unknown table") || msg.contains("no such table")) {
                // Attempt to create the leads table and retry once
                String ddl = "CREATE TABLE IF NOT EXISTS leads (lead_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
                try (Connection conn2 = dbUtil.getConnection(); Statement s = conn2.createStatement()) {
                    s.executeUpdate(ddl);
                    // retry the select
                    try (PreparedStatement pstmt2 = conn2.prepareStatement(sql)) {
                        pstmt2.setInt(1, campaignId);
                        try (ResultSet rs2 = pstmt2.executeQuery()) {
                            while (rs2.next()) {
                                leads.add(mapResultSetToLead(rs2));
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.err.println("Retry loading leads failed: " + ex.getMessage());
                }
            }
        }
        return leads;
    }

    public boolean updateLeadState(int leadId, String newState) {
        String sql = "UPDATE leads SET state = ?, updated_at = CURRENT_TIMESTAMP WHERE lead_id = ?";
        try (Connection conn = dbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newState);
            pstmt.setInt(2, leadId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating lead state: " + e.getMessage());
            return false;
        }
    }

    private Lead mapResultSetToLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setLeadId(rs.getInt("lead_id"));
        lead.setName(rs.getString("name"));
        lead.setEmail(rs.getString("email"));
        lead.setCampaignId(rs.getInt("campaign_id"));
        lead.setState(rs.getString("state"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) lead.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) lead.setUpdatedAt(updated.toLocalDateTime());
        return lead;
    }
}

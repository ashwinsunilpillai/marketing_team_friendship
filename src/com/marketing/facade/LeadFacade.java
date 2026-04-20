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
                String ddl = "CREATE TABLE IF NOT EXISTS marketing_erp.leads (" +
                    "lead_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), " +
                    "source VARCHAR(100), expected_value DECIMAL(15,2) DEFAULT 0, " +
                    "closed_value DECIMAL(15,2) DEFAULT 0, converted_at TIMESTAMP NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
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
                String ddl = "CREATE TABLE IF NOT EXISTS leads (" +
                    "lead_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255), email VARCHAR(255), campaign_id INT, state VARCHAR(50), " +
                    "source VARCHAR(100), expected_value DECIMAL(15,2) DEFAULT 0, " +
                    "closed_value DECIMAL(15,2) DEFAULT 0, converted_at TIMESTAMP NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
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

    public boolean transitionLeadState(int leadId, String targetState) {
        String state = normalizeState(targetState);
        if (state == null) {
            return false;
        }

        String selectSql = "SELECT state FROM leads WHERE lead_id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, leadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String current = normalizeState(rs.getString("state"));
                if (!isValidTransition(current, state)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking lead transition: " + e.getMessage());
            return false;
        }

        return updateLeadState(leadId, state);
    }

    public boolean convertLeadWithRevenue(int leadId, double closedValue) {
        if (closedValue < 0) {
            return false;
        }

        String selectLead = "SELECT lead_id, campaign_id, state FROM leads WHERE lead_id = ?";
        String updateLead = "UPDATE leads SET state = 'Converted', closed_value = ?, converted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE lead_id = ?";
        String updateMetricsToday = "UPDATE campaign_metrics SET conversions = conversions + 1, revenue_generated = revenue_generated + ? WHERE campaign_id = ? AND metric_date = CURDATE()";
        String insertMetricsToday = "INSERT INTO campaign_metrics (campaign_id, metric_date, impressions, clicks, conversions, revenue_generated) VALUES (?, CURDATE(), 0, 0, 1, ?)";
        String syncLeadCount = "UPDATE campaigns SET leads_generated = (SELECT COUNT(*) FROM leads WHERE campaign_id = ?) WHERE campaign_id = ?";
        String updateCampaignRoi = "UPDATE campaigns SET campaign_roi = CASE WHEN COALESCE(campaign_budget,0) > 0 THEN ((COALESCE((SELECT SUM(revenue_generated) FROM campaign_metrics WHERE campaign_id = ?),0) - campaign_budget) / campaign_budget) * 100 ELSE 0 END WHERE campaign_id = ?";

        Connection conn = dbUtil.getConnection();
        if (conn == null) {
            return false;
        }

        try (Connection safeConn = conn) {
            safeConn.setAutoCommit(false);

            int campaignId;
            String currentState;
            try (PreparedStatement ps = safeConn.prepareStatement(selectLead)) {
                ps.setInt(1, leadId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        safeConn.rollback();
                        return false;
                    }
                    campaignId = rs.getInt("campaign_id");
                    currentState = normalizeState(rs.getString("state"));
                }
            }

            if (!isValidTransition(currentState, "Converted")) {
                safeConn.rollback();
                return false;
            }

            try (PreparedStatement ps = safeConn.prepareStatement(updateLead)) {
                ps.setDouble(1, closedValue);
                ps.setInt(2, leadId);
                if (ps.executeUpdate() == 0) {
                    safeConn.rollback();
                    return false;
                }
            }

            try (PreparedStatement ps = safeConn.prepareStatement(updateMetricsToday)) {
                ps.setDouble(1, closedValue);
                ps.setInt(2, campaignId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement ins = safeConn.prepareStatement(insertMetricsToday)) {
                        ins.setInt(1, campaignId);
                        ins.setDouble(2, closedValue);
                        ins.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = safeConn.prepareStatement(syncLeadCount)) {
                ps.setInt(1, campaignId);
                ps.setInt(2, campaignId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = safeConn.prepareStatement(updateCampaignRoi)) {
                ps.setInt(1, campaignId);
                ps.setInt(2, campaignId);
                ps.executeUpdate();
            }

            safeConn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error converting lead with revenue: " + e.getMessage());
            return false;
        }
    }

    private String normalizeState(String rawState) {
        if (rawState == null) return null;
        String s = rawState.trim().toLowerCase();
        if (s.equals("new")) return "New";
        if (s.equals("qualified")) return "Qualified";
        if (s.equals("converted")) return "Converted";
        return null;
    }

    private boolean isValidTransition(String from, String to) {
        if (from == null || to == null) return false;
        if (from.equals(to)) return true;
        if (from.equals("New") && (to.equals("Qualified") || to.equals("Converted"))) return true;
        if (from.equals("Qualified") && to.equals("Converted")) return true;
        return false;
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

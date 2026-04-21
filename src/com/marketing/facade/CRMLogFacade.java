package com.marketing.facade;

import com.marketing.entity.CRMSyncLog;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CRMLogFacade {
    private final DBUtil dbUtil = DBUtil.getInstance();

    public boolean createLog(String source, String details) {
        String sql = "INSERT INTO crm_sync_log (source, details) VALUES (?, ?)";
        try (Connection conn = dbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, source);
            pstmt.setString(2, details);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating CRM log: " + e.getMessage());
            return false;
        }
    }

    public List<CRMSyncLog> getRecentLogs(int limit) {
        List<CRMSyncLog> results = new ArrayList<>();
        String sql = "SELECT * FROM crm_sync_log ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = dbUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CRMSyncLog log = new CRMSyncLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setSource(rs.getString("source"));
                    log.setDetails(rs.getString("details"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
                    results.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading CRM logs: " + e.getMessage());
        }
        return results;
    }
}

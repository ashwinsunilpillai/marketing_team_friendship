package com.marketing.facade;

import com.marketing.entity.Campaign;
import com.marketing.exception.*;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CampaignFacade - Structural Facade Pattern
 * Provides a simplified interface to perform CRUD operations on campaigns.
 * Abstracts away the complexity of direct JDBC calls.
 * GRASP: Creator (creates Campaign objects), Information Expert (knows campaign
 * DB operations)
 * SOLID: SRP (single responsibility - manage campaign lifecycle)
 */
public class CampaignFacade {
    private DBUtil dbUtil;

    public CampaignFacade() {
        this.dbUtil = DBUtil.getInstance();
    }

    /**
     * Creates a new campaign in the database
     * 
     * @param campaign Campaign object to be created
     * @return true if successful, false otherwise
     * @throws CampaignCreationException if creation fails
     */
    public boolean createCampaign(Campaign campaign) throws CampaignCreationException {
        String title = campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName();
        if (campaign == null || title == null || title.isEmpty()) {
            throw new CampaignCreationException("Campaign title cannot be null or empty");
        }

        String newSql = "INSERT INTO campaigns (campaign_title, campaign_type, target_vehicle_segment, campaign_budget, target_leads, start_date, end_date, campaign_roi, campaign_results, status, lead_target, leads_generated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String oldSql = "INSERT INTO campaigns (campaign_name, start_date, end_date, budget, status, segment_id, description, lead_target, leads_generated, campaign_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = dbUtil.getConnection();
        if (conn == null) {
            throw new CampaignCreationException("No database connection available");
        }

        try (Connection safeConn = conn) {
            double budget = campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget();
            String type = campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL";
            String status = campaign.getStatus() != null ? campaign.getStatus() : "PLANNED";

            try (PreparedStatement pstmt = safeConn.prepareStatement(newSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, title);
                pstmt.setString(2, type);
                pstmt.setString(3, campaign.getTargetVehicleSegment());
                pstmt.setDouble(4, budget);
                pstmt.setString(5, campaign.getTargetLeads());
                if (campaign.getStartDate() != null) pstmt.setDate(6, java.sql.Date.valueOf(campaign.getStartDate())); else pstmt.setNull(6, java.sql.Types.DATE);
                if (campaign.getEndDate() != null) pstmt.setDate(7, java.sql.Date.valueOf(campaign.getEndDate())); else pstmt.setNull(7, java.sql.Types.DATE);
                pstmt.setDouble(8, campaign.getCampaignRoi());
                pstmt.setString(9, campaign.getCampaignResults());
                pstmt.setString(10, status);
                pstmt.setInt(11, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                pstmt.setInt(12, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new CampaignCreationException("Creating campaign failed, no rows affected.");
                }
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys != null && generatedKeys.next()) {
                        campaign.setCampaignId(generatedKeys.getInt(1));
                    }
                }
                return true;
            } catch (SQLException e) {
                // Fallback for legacy schema without campaign_title/campaign_budget.
                try (PreparedStatement legacy = safeConn.prepareStatement(oldSql, Statement.RETURN_GENERATED_KEYS)) {
                    legacy.setString(1, title);
                    if (campaign.getStartDate() != null) legacy.setDate(2, java.sql.Date.valueOf(campaign.getStartDate())); else legacy.setNull(2, java.sql.Types.DATE);
                    if (campaign.getEndDate() != null) legacy.setDate(3, java.sql.Date.valueOf(campaign.getEndDate())); else legacy.setNull(3, java.sql.Types.DATE);
                    legacy.setDouble(4, budget);
                    legacy.setString(5, status);
                    legacy.setInt(6, campaign.getSegmentId() > 0 ? campaign.getSegmentId() : 1);
                    legacy.setString(7, campaign.getDescription());
                    legacy.setInt(8, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                    legacy.setInt(9, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
                    legacy.setString(10, type);

                    int affectedRows = legacy.executeUpdate();
                    if (affectedRows == 0) {
                        throw new CampaignCreationException("Creating campaign failed, no rows affected.");
                    }
                    try (ResultSet generatedKeys = legacy.getGeneratedKeys()) {
                        if (generatedKeys != null && generatedKeys.next()) {
                            campaign.setCampaignId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                } catch (SQLException legacyEx) {
                    throw new CampaignCreationException("Failed to create campaign: " + legacyEx.getMessage(), legacyEx);
                }
            }
        } catch (SQLException e) {
            throw new CampaignCreationException("Failed to create campaign: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a campaign by ID
     * 
     * @param campaignId ID of the campaign to retrieve
     * @return Campaign object if found
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public Campaign getCampaignById(int campaignId) throws CampaignNotFoundException {
        String sql = "SELECT * FROM campaigns WHERE campaign_id = ?";

        try (Connection conn = dbUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, campaignId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCampaign(rs);
            } else {
                throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
            }

        } catch (SQLException e) {
            throw new CampaignNotFoundException("Error retrieving campaign: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all campaigns
     * 
     * @return List of all campaigns
     */
    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        String sql = "SELECT * FROM campaigns";

        Connection conn = dbUtil.getConnection();
        if (conn == null) {
            System.err.println("Skipping campaign load because no database connection is available.");
            return campaigns;
        }

        try (Connection safeConn = conn;
                Statement stmt = safeConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving campaigns: " + e.getMessage());
        }

        // Enrich campaigns with live lead counts from the leads table.
        for (Campaign c : campaigns) {
            try (Connection conn2 = dbUtil.getConnection();
                 PreparedStatement ps = conn2.prepareStatement(
                         "SELECT COUNT(*) AS total, SUM(CASE WHEN LOWER(state)='converted' THEN 1 ELSE 0 END) AS converted FROM leads WHERE campaign_id = ?")) {
                ps.setInt(1, c.getCampaignId());
                try (ResultSet rs2 = ps.executeQuery()) {
                    if (rs2.next()) {
                        c.setLeadsGenerated(rs2.getInt("total"));
                        c.setConversions(rs2.getInt("converted"));
                    }
                }
            } catch (SQLException ignore) {
                // best-effort enrichment; ignore
            }
        }

        return campaigns;
    }

    /**
     * Updates an existing campaign
     * 
     * @param campaign Campaign object with updated values
     * @return true if successful, false otherwise
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public boolean updateCampaign(Campaign campaign) throws CampaignNotFoundException, com.marketing.exception.CampaignStateException {
        if (campaign.getCampaignId() <= 0) {
            throw new CampaignNotFoundException("Invalid campaign ID");
        }

        String newSql = "UPDATE campaigns SET campaign_title = ?, start_date = ?, end_date = ?, campaign_budget = ?, status = ?, lead_target = ?, leads_generated = ?, campaign_type = ? WHERE campaign_id = ?";
        String oldSql = "UPDATE campaigns SET campaign_name = ?, start_date = ?, end_date = ?, budget = ?, status = ?, segment_id = ?, description = ?, lead_target = ?, leads_generated = ?, campaign_type = ? WHERE campaign_id = ?";

        Connection conn = dbUtil.getConnection();
        if (conn == null) {
            throw new com.marketing.exception.CampaignStateException("No database connection available");
        }
        try (Connection safeConn = conn) {
            String title = campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName();
            double budget = campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget();
            String status = campaign.getStatus() != null ? campaign.getStatus() : "PLANNED";
            String type = campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL";

            try (PreparedStatement pstmt = safeConn.prepareStatement(newSql)) {
                pstmt.setString(1, title);
                if (campaign.getStartDate() != null) pstmt.setDate(2, java.sql.Date.valueOf(campaign.getStartDate())); else pstmt.setNull(2, java.sql.Types.DATE);
                if (campaign.getEndDate() != null) pstmt.setDate(3, java.sql.Date.valueOf(campaign.getEndDate())); else pstmt.setNull(3, java.sql.Types.DATE);
                pstmt.setDouble(4, budget);
                pstmt.setString(5, status);
                pstmt.setInt(6, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                pstmt.setInt(7, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
                pstmt.setString(8, type);
                pstmt.setInt(9, campaign.getCampaignId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new CampaignNotFoundException("Campaign with ID " + campaign.getCampaignId() + " not found");
                }
                return true;
            } catch (SQLException e) {
                // Fallback for legacy schema without campaign_title/campaign_budget.
                try (PreparedStatement legacy = safeConn.prepareStatement(oldSql)) {
                    legacy.setString(1, title);
                    if (campaign.getStartDate() != null) legacy.setDate(2, java.sql.Date.valueOf(campaign.getStartDate())); else legacy.setNull(2, java.sql.Types.DATE);
                    if (campaign.getEndDate() != null) legacy.setDate(3, java.sql.Date.valueOf(campaign.getEndDate())); else legacy.setNull(3, java.sql.Types.DATE);
                    legacy.setDouble(4, budget);
                    legacy.setString(5, status);
                    legacy.setInt(6, campaign.getSegmentId() > 0 ? campaign.getSegmentId() : 1);
                    legacy.setString(7, campaign.getDescription());
                    legacy.setInt(8, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                    legacy.setInt(9, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
                    legacy.setString(10, type);
                    legacy.setInt(11, campaign.getCampaignId());

                    int affectedRows = legacy.executeUpdate();
                    if (affectedRows == 0) {
                        throw new CampaignNotFoundException("Campaign with ID " + campaign.getCampaignId() + " not found");
                    }
                    return true;
                } catch (SQLException legacyEx) {
                    throw new com.marketing.exception.CampaignStateException("Error updating campaign: " + legacyEx.getMessage(), legacyEx);
                }
            }
        } catch (SQLException e) {
            throw new com.marketing.exception.CampaignStateException("Error updating campaign: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a campaign by ID
     * 
     * @param campaignId ID of the campaign to delete
     * @return true if successful, false otherwise
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public boolean deleteCampaign(int campaignId) throws CampaignNotFoundException {
        String sql = "DELETE FROM campaigns WHERE campaign_id = ?";

        try (Connection conn = dbUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, campaignId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting campaign: " + e.getMessage());
            return false;
        }
    }

    /**
     * Changes the status of a campaign
     * 
     * @param campaignId ID of the campaign
     * @param newStatus  New status to set (ACTIVE, PAUSED, COMPLETED)
     * @return true if successful, false otherwise
     * @throws CampaignStateException    if state transition is invalid
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public boolean changeCampaignStatus(int campaignId, String newStatus)
            throws CampaignStateException, CampaignNotFoundException {
        if (!isValidStatus(newStatus)) {
            throw new CampaignStateException("Invalid status: " + newStatus);
        }
        Campaign campaign = getCampaignById(campaignId);

        String current = campaign.getStatus();

        // Disallow any changes on campaigns already completed
        if ("COMPLETED".equals(current) && !"COMPLETED".equals(newStatus)) {
            throw new CampaignStateException("Cannot change status of a completed campaign");
        }

        // Validate allowed transitions
        boolean allowed = false;
        switch (newStatus) {
            case "ACTIVE":
                // PLANNED -> ACTIVE, PAUSED -> ACTIVE allowed
                if ("PLANNED".equals(current) || "PAUSED".equals(current) || "ACTIVE".equals(current)) {
                    allowed = true;
                    if (campaign.getStartDate() == null) {
                        campaign.setStartDate(java.time.LocalDate.now());
                    }
                }
                break;
            case "PAUSED":
                // Can pause from ACTIVE
                if ("ACTIVE".equals(current) || "PAUSED".equals(current)) {
                    allowed = true;
                }
                break;
            case "COMPLETED":
                // Can complete from ACTIVE, PAUSED, PLANNED
                if (!"COMPLETED".equals(current)) {
                    allowed = true;
                    campaign.setEndDate(java.time.LocalDate.now());
                }
                break;
            case "PLANNED":
                // Allow setting back to PLANNED only if not already completed
                if (!"COMPLETED".equals(current)) {
                    allowed = true;
                }
                break;
            default:
                allowed = false;
        }

        if (!allowed) {
            throw new CampaignStateException("Transition from " + current + " to " + newStatus + " is not allowed");
        }

        // Persist only relevant fields to avoid null-pointer risks from full update
        String sql = "UPDATE campaigns SET status = ?, start_date = ?, end_date = ? WHERE campaign_id = ?";
        Connection conn = dbUtil.getConnection();
        if (conn == null) {
            throw new CampaignStateException("No database connection available");
        }

        try (Connection safeConn = conn; PreparedStatement pstmt = safeConn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            if (campaign.getStartDate() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(campaign.getStartDate()));
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }
            if (campaign.getEndDate() != null) {
                pstmt.setDate(3, java.sql.Date.valueOf(campaign.getEndDate()));
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }
            pstmt.setInt(4, campaignId);

            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
            }
            return true;
        } catch (SQLException e) {
            throw new CampaignStateException("Error updating campaign status: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if a status is valid
     * 
     * @param status Status to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("ACTIVE") || status.equals("PAUSED") || status.equals("COMPLETED") || status.equals("PLANNED"));
    }

    /**
     * Maps a ResultSet row to a Campaign object
     * 
     * @param rs ResultSet to map from
     * @return Campaign object
     * @throws SQLException if database access error occurs
     */
    private Campaign mapResultSetToCampaign(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(rs.getInt("campaign_id"));
        
        // Try new schema columns first, fall back to legacy columns
        try {
            campaign.setCampaignTitle(rs.getString("campaign_title"));
        } catch (SQLException e) {
            campaign.setCampaignTitle(rs.getString("campaign_name"));
        }
        
        try {
            campaign.setTargetVehicleSegment(rs.getString("target_vehicle_segment"));
        } catch (SQLException ignore) {
        }
        
        try {
            campaign.setCampaignBudget(rs.getDouble("campaign_budget"));
        } catch (SQLException e) {
            campaign.setCampaignBudget(rs.getDouble("budget"));
        }
        
        try {
            campaign.setTargetLeads(rs.getString("target_leads"));
        } catch (SQLException ignore) {
        }
        
        java.sql.Date sd = rs.getDate("start_date");
        if (sd != null) campaign.setStartDate(sd.toLocalDate());
        else campaign.setStartDate(null);
        
        java.sql.Date ed = rs.getDate("end_date");
        if (ed != null) campaign.setEndDate(ed.toLocalDate());
        else campaign.setEndDate(null);
        
        try {
            campaign.setCampaignRoi(rs.getDouble("campaign_roi"));
        } catch (SQLException ignore) {
        }
        
        try {
            campaign.setCampaignResults(rs.getString("campaign_results"));
        } catch (SQLException ignore) {
        }
        
        // Legacy field mapping
        try {
            campaign.setStatus(rs.getString("status"));
        } catch (SQLException ignore) {
        }
        try {
            campaign.setSegmentId(rs.getInt("segment_id"));
        } catch (SQLException ignore) {
        }
        try {
            campaign.setDescription(rs.getString("description"));
        } catch (SQLException ignore) {
        }
        try {
            campaign.setImpressions(rs.getInt("impressions"));
        } catch (SQLException ignore) {
            campaign.setImpressions(0);
        }
        try {
            campaign.setClicks(rs.getInt("clicks"));
        } catch (SQLException ignore) {
            campaign.setClicks(0);
        }
        try {
            campaign.setConversions(rs.getInt("conversions"));
        } catch (SQLException ignore) {
            campaign.setConversions(0);
        }
        try {
            campaign.setLeadTarget(rs.getInt("lead_target"));
        } catch (SQLException e) {
            campaign.setLeadTarget(0);
        }
        try {
            campaign.setLeadsGenerated(rs.getInt("leads_generated"));
        } catch (SQLException e) {
            campaign.setLeadsGenerated(0);
        }
        try {
            campaign.setCampaignType(rs.getString("campaign_type"));
        } catch (SQLException e) {
            campaign.setCampaignType("EMAIL");
        }
        
        return campaign;
    }
}

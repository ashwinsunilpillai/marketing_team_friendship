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
 * GRASP: Creator (creates Campaign objects), Information Expert (knows campaign DB operations)
 * SOLID: SRP (single responsibility - manage campaign lifecycle)
 */
public class CampaignFacade {
    private DBUtil dbUtil;
    
    public CampaignFacade() {
        this.dbUtil = DBUtil.getInstance();
    }
    
    /**
     * Creates a new campaign in the database
     * @param campaign Campaign object to be created
     * @return true if successful, false otherwise
     * @throws CampaignCreationException if creation fails
     */
    public boolean createCampaign(Campaign campaign) throws CampaignCreationException {
        if (campaign == null || campaign.getCampaignName() == null || campaign.getCampaignName().isEmpty()) {
            throw new CampaignCreationException("Campaign name cannot be null or empty");
        }
        
        String sql = "INSERT INTO campaigns (campaign_name, start_date, end_date, budget, status, segment_id, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, campaign.getCampaignName());
            pstmt.setDate(2, java.sql.Date.valueOf(campaign.getStartDate()));
            pstmt.setDate(3, java.sql.Date.valueOf(campaign.getEndDate()));
            pstmt.setDouble(4, campaign.getBudget());
            pstmt.setString(5, campaign.getStatus() != null ? campaign.getStatus() : "ACTIVE");
            pstmt.setInt(6, campaign.getSegmentId());
            pstmt.setString(7, campaign.getDescription());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new CampaignCreationException("Failed to create campaign: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves a campaign by ID
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
     * @return List of all campaigns
     */
    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        String sql = "SELECT * FROM campaigns";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving campaigns: " + e.getMessage());
        }
        
        return campaigns;
    }
    
    /**
     * Updates an existing campaign
     * @param campaign Campaign object with updated values
     * @return true if successful, false otherwise
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public boolean updateCampaign(Campaign campaign) throws CampaignNotFoundException {
        if (campaign.getCampaignId() <= 0) {
            throw new CampaignNotFoundException("Invalid campaign ID");
        }
        
        String sql = "UPDATE campaigns SET campaign_name = ?, start_date = ?, end_date = ?, budget = ?, status = ?, segment_id = ?, description = ? WHERE campaign_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, campaign.getCampaignName());
            pstmt.setDate(2, java.sql.Date.valueOf(campaign.getStartDate()));
            pstmt.setDate(3, java.sql.Date.valueOf(campaign.getEndDate()));
            pstmt.setDouble(4, campaign.getBudget());
            pstmt.setString(5, campaign.getStatus());
            pstmt.setInt(6, campaign.getSegmentId());
            pstmt.setString(7, campaign.getDescription());
            pstmt.setInt(8, campaign.getCampaignId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new CampaignNotFoundException("Campaign with ID " + campaign.getCampaignId() + " not found");
            }
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error updating campaign: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a campaign by ID
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
     * @param campaignId ID of the campaign
     * @param newStatus New status to set (ACTIVE, PAUSED, COMPLETED)
     * @return true if successful, false otherwise
     * @throws CampaignStateException if state transition is invalid
     * @throws CampaignNotFoundException if campaign does not exist
     */
    public boolean changeCampaignStatus(int campaignId, String newStatus) throws CampaignStateException, CampaignNotFoundException {
        if (!isValidStatus(newStatus)) {
            throw new CampaignStateException("Invalid status: " + newStatus);
        }
        
        Campaign campaign = getCampaignById(campaignId);
        campaign.setStatus(newStatus);
        
        try {
            return updateCampaign(campaign);
        } catch (CampaignNotFoundException e) {
            throw e;
        }
    }
    
    /**
     * Validates if a status is valid
     * @param status Status to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("ACTIVE") || status.equals("PAUSED") || status.equals("COMPLETED"));
    }
    
    /**
     * Maps a ResultSet row to a Campaign object
     * @param rs ResultSet to map from
     * @return Campaign object
     * @throws SQLException if database access error occurs
     */
    private Campaign mapResultSetToCampaign(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(rs.getInt("campaign_id"));
        campaign.setCampaignName(rs.getString("campaign_name"));
        campaign.setStartDate(rs.getDate("start_date").toLocalDate());
        campaign.setEndDate(rs.getDate("end_date").toLocalDate());
        campaign.setBudget(rs.getDouble("budget"));
        campaign.setStatus(rs.getString("status"));
        campaign.setSegmentId(rs.getInt("segment_id"));
        campaign.setDescription(rs.getString("description"));
        
        if (rs.findColumn("impressions") > 0) {
            campaign.setImpressions(rs.getInt("impressions"));
        }
        if (rs.findColumn("clicks") > 0) {
            campaign.setClicks(rs.getInt("clicks"));
        }
        if (rs.findColumn("conversions") > 0) {
            campaign.setConversions(rs.getInt("conversions"));
        }
        
        return campaign;
    }
}

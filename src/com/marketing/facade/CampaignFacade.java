package com.marketing.facade;

import com.marketing.entity.Campaign;
import com.marketing.exception.CampaignCreationException;
import com.marketing.exception.CampaignNotFoundException;
import com.marketing.exception.CampaignStateException;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignFacade {
    private static final String TABLE_NAME = "campaigns";
    private static final String ID_COLUMN = "campaign_id";
    private final DBUtil dbUtil;

    public CampaignFacade() {
        this.dbUtil = DBUtil.getInstance();
    }

    public boolean createCampaign(Campaign campaign) throws CampaignCreationException {
        if (campaign == null) {
            throw new CampaignCreationException("Campaign cannot be null");
        }
        String title = campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName();
        if (title == null || title.isBlank()) {
            throw new CampaignCreationException("Campaign title cannot be null or empty");
        }

        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                throw new CampaignCreationException("Database connection not available");
            }

            String sql = "INSERT INTO campaigns (campaign_title, campaign_type, target_vehicle_segment, " +
                    "campaign_budget, target_leads, lead_target, leads_generated, start_date, end_date, campaign_roi, campaign_results) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL");
                pstmt.setString(3, campaign.getTargetVehicleSegment());
                pstmt.setDouble(4, campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget());
                pstmt.setString(5, campaign.getTargetLeads());
                pstmt.setInt(6, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                pstmt.setInt(7, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
                pstmt.setDate(8, campaign.getStartDate() != null ? Date.valueOf(campaign.getStartDate()) : null);
                pstmt.setDate(9, campaign.getEndDate() != null ? Date.valueOf(campaign.getEndDate()) : null);
                pstmt.setDouble(10, campaign.getCampaignRoi());
                pstmt.setString(11, campaign.getCampaignResults());
                
                pstmt.executeUpdate();
                System.out.println("Campaign created successfully: " + title);
                return true;
            }
        } catch (CampaignCreationException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Failed to create campaign: " + e.getMessage());
            e.printStackTrace();
            throw new CampaignCreationException("Failed to create campaign: " + e.getMessage(), e);
        }
    }

    public Campaign getCampaignById(int campaignId) throws CampaignNotFoundException {
        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                throw new CampaignNotFoundException("Database connection not available");
            }

            String sql = "SELECT * FROM campaigns WHERE campaign_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, campaignId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    } else {
                        throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
                    }
                }
            }
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error retrieving campaign: " + e.getMessage());
            e.printStackTrace();
            throw new CampaignNotFoundException("Error retrieving campaign: " + e.getMessage(), e);
        }
    }

    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                System.err.println("Database connection not available");
                return campaigns;
            }

            String sql = "SELECT * FROM campaigns ORDER BY campaign_id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    campaigns.add(mapResultSetToEntity(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving campaigns: " + e.getMessage());
            e.printStackTrace();
        }
        return campaigns;
    }

    private Campaign mapResultSetToEntity(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(rs.getInt("campaign_id"));
        campaign.setCampaignTitle(rs.getString("campaign_title"));
        campaign.setCampaignName(rs.getString("campaign_title"));
        campaign.setCampaignType(rs.getString("campaign_type"));
        campaign.setTargetVehicleSegment(rs.getString("target_vehicle_segment"));
        campaign.setCampaignBudget(rs.getDouble("campaign_budget"));
        campaign.setBudget(rs.getDouble("campaign_budget"));
        campaign.setTargetLeads(rs.getString("target_leads"));
        
        // Read lead target and leads generated
        campaign.setLeadTarget(rs.getInt("lead_target"));
        campaign.setLeadsGenerated(rs.getInt("leads_generated"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            campaign.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            campaign.setEndDate(endDate.toLocalDate());
        }
        
        campaign.setCampaignRoi(rs.getDouble("campaign_roi"));
        campaign.setCampaignResults(rs.getString("campaign_results"));
        
        // Read status from database, default to ACTIVE if not found
        String status = rs.getString("status");
        campaign.setStatus(status != null ? status : "ACTIVE");
        
        return campaign;
    }

    public boolean updateCampaign(Campaign campaign) throws CampaignNotFoundException, CampaignStateException {
        if (campaign.getCampaignId() <= 0) {
            throw new CampaignNotFoundException("Invalid campaign ID");
        }

        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                throw new CampaignStateException("Database connection not available");
            }

            String sql = "UPDATE campaigns SET campaign_title=?, campaign_budget=?, campaign_type=?, " +
                    "lead_target=?, leads_generated=?, start_date=?, end_date=? WHERE campaign_id=?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName());
                pstmt.setDouble(2, campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget());
                pstmt.setString(3, campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL");
                pstmt.setInt(4, campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
                pstmt.setInt(5, campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
                pstmt.setDate(6, campaign.getStartDate() != null ? Date.valueOf(campaign.getStartDate()) : null);
                pstmt.setDate(7, campaign.getEndDate() != null ? Date.valueOf(campaign.getEndDate()) : null);
                pstmt.setInt(8, campaign.getCampaignId());
                
                pstmt.executeUpdate();
                System.out.println("Campaign updated successfully: ID " + campaign.getCampaignId());
                return true;
            }
        } catch (CampaignStateException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating campaign: " + e.getMessage());
            e.printStackTrace();
            throw new CampaignStateException("Error updating campaign: " + e.getMessage(), e);
        }
    }

    public boolean deleteCampaign(int campaignId) throws CampaignNotFoundException {
        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                throw new CampaignNotFoundException("Database connection not available");
            }

            String sql = "DELETE FROM campaigns WHERE campaign_id=?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, campaignId);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Campaign deleted successfully: ID " + campaignId);
                    return true;
                } else {
                    throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
                }
            }
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error deleting campaign: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeCampaignStatus(int campaignId, String newStatus)
            throws CampaignStateException, CampaignNotFoundException {
        if (!isValidStatus(newStatus)) {
            throw new CampaignStateException("Invalid status: " + newStatus);
        }

        Campaign campaign = getCampaignById(campaignId);
        String current = campaign.getStatus();

        if ("COMPLETED".equals(current) && !"COMPLETED".equals(newStatus)) {
            throw new CampaignStateException("Cannot change status of a completed campaign");
        }

        boolean allowed = switch (newStatus) {
            case "ACTIVE" -> "PLANNED".equals(current) || "PAUSED".equals(current) || "ACTIVE".equals(current);
            case "PAUSED" -> "ACTIVE".equals(current) || "PAUSED".equals(current);
            case "COMPLETED" -> !"COMPLETED".equals(current);
            case "PLANNED" -> !"COMPLETED".equals(current);
            default -> false;
        };

        if (!allowed) {
            throw new CampaignStateException("Transition from " + current + " to " + newStatus + " is not allowed");
        }

        if ("ACTIVE".equals(newStatus) && campaign.getStartDate() == null) {
            campaign.setStartDate(LocalDate.now());
        }
        if ("COMPLETED".equals(newStatus)) {
            campaign.setEndDate(LocalDate.now());
        }

        try {
            Connection conn = dbUtil.getConnection();
            if (conn == null) {
                throw new CampaignStateException("Database connection not available");
            }

            String sql = "UPDATE campaigns SET status=?, start_date=?, end_date=? WHERE campaign_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setDate(2, campaign.getStartDate() != null ? Date.valueOf(campaign.getStartDate()) : null);
                pstmt.setDate(3, campaign.getEndDate() != null ? Date.valueOf(campaign.getEndDate()) : null);
                pstmt.setInt(4, campaignId);
                
                pstmt.executeUpdate();
                System.out.println("Campaign status updated: ID " + campaignId + " -> " + newStatus);
                return true;
            }
        } catch (CampaignStateException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating campaign status: " + e.getMessage());
            e.printStackTrace();
            throw new CampaignStateException("Error updating campaign status: " + e.getMessage(), e);
        }
    }

    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "PAUSED".equals(status) || "COMPLETED".equals(status) || "PLANNED".equals(status);
    }
}

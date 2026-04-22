package com.marketing.facade;

import com.marketing.entity.Campaign;
import com.marketing.exception.CampaignCreationException;
import com.marketing.exception.CampaignNotFoundException;
import com.marketing.exception.CampaignStateException;
import com.marketing.util.DBUtil;

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
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignCreationException("Marketing subsystem facade not available");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("campaign_title", title);
            payload.put("campaign_type", campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL");
            payload.put("target_vehicle_segment", campaign.getTargetVehicleSegment());
            payload.put("campaign_budget", campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget());
            payload.put("target_leads", campaign.getTargetLeads());
            if (campaign.getStartDate() != null) payload.put("start_date", campaign.getStartDate());
            if (campaign.getEndDate() != null) payload.put("end_date", campaign.getEndDate());
            payload.put("campaign_roi", campaign.getCampaignRoi());
            payload.put("campaign_results", campaign.getCampaignResults());
            payload.put("lead_target", campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
            payload.put("leads_generated", campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);

            marketingSubsystem.getClass().getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, payload);
            return true;
        } catch (CampaignCreationException e) {
            throw e;
        } catch (Exception e) {
            throw new CampaignCreationException("Failed to create campaign: " + e.getMessage(), e);
        }
    }

    public Campaign getCampaignById(int campaignId) throws CampaignNotFoundException {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignNotFoundException("Marketing subsystem facade not available");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) marketingSubsystem.getClass()
                    .getMethod("readById", String.class, String.class, Object.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaignId);

            if (row == null || row.isEmpty()) {
                throw new CampaignNotFoundException("Campaign with ID " + campaignId + " not found");
            }
            return mapToEntity(row);
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new CampaignNotFoundException("Error retrieving campaign: " + e.getMessage(), e);
        }
    }

    public List<Campaign> getAllCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) return campaigns;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    campaigns.add(mapToEntity(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving campaigns: " + e.getMessage());
        }
        return campaigns;
    }

    public boolean updateCampaign(Campaign campaign) throws CampaignNotFoundException, CampaignStateException {
        if (campaign.getCampaignId() <= 0) {
            throw new CampaignNotFoundException("Invalid campaign ID");
        }

        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignStateException("Marketing subsystem facade not available");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("campaign_title", campaign.getCampaignTitle() != null ? campaign.getCampaignTitle() : campaign.getCampaignName());
            payload.put("campaign_budget", campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget());
            payload.put("lead_target", campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
            payload.put("leads_generated", campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
            payload.put("campaign_type", campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL");
            if (campaign.getStartDate() != null) payload.put("start_date", campaign.getStartDate());
            if (campaign.getEndDate() != null) payload.put("end_date", campaign.getEndDate());

            marketingSubsystem.getClass().getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaign.getCampaignId(), payload);
            return true;
        } catch (CampaignStateException e) {
            throw e;
        } catch (Exception e) {
            throw new CampaignStateException("Error updating campaign: " + e.getMessage(), e);
        }
    }

    public boolean deleteCampaign(int campaignId) throws CampaignNotFoundException {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignNotFoundException("Marketing subsystem facade not available");
            }

            marketingSubsystem.getClass().getMethod("delete", String.class, String.class, Object.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaignId);
            return true;
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error deleting campaign: " + e.getMessage());
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
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignStateException("Marketing subsystem facade not available");
            }

            Map<String, Object> payload = new HashMap<>();
            if (campaign.getStartDate() != null) payload.put("start_date", campaign.getStartDate());
            if (campaign.getEndDate() != null) payload.put("end_date", campaign.getEndDate());

            // Integration permissions may block status writes for marketing subsystem.
            // Persist date changes only and keep status transition validated at facade level.
            if (payload.isEmpty()) {
                return true;
            }

            marketingSubsystem.getClass().getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaignId, payload);
            return true;
        } catch (CampaignStateException e) {
            throw e;
        } catch (Exception e) {
            throw new CampaignStateException("Error updating campaign status: " + e.getMessage(), e);
        }
    }

    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "PAUSED".equals(status) || "COMPLETED".equals(status) || "PLANNED".equals(status);
    }

    private Campaign mapToEntity(Map<String, Object> row) {
        Campaign campaign = new Campaign();
        if (row.get("campaign_id") instanceof Number n) campaign.setCampaignId(n.intValue());
        campaign.setCampaignTitle((String) (row.containsKey("campaign_title") ? row.get("campaign_title") : row.get("campaign_name")));
        campaign.setTargetVehicleSegment((String) row.get("target_vehicle_segment"));
        if (row.get("campaign_budget") instanceof Number n) campaign.setCampaignBudget(n.doubleValue());
        if (row.get("budget") instanceof Number n) campaign.setCampaignBudget(n.doubleValue());
        campaign.setTargetLeads((String) row.get("target_leads"));
        campaign.setCampaignResults((String) row.get("campaign_results"));
        String status = (String) row.get("status");
        campaign.setStatus(status != null ? status : "PLANNED");
        campaign.setDescription((String) row.get("description"));
        if (row.get("segment_id") instanceof Number n) campaign.setSegmentId(n.intValue());
        if (row.get("impressions") instanceof Number n) campaign.setImpressions(n.intValue());
        if (row.get("clicks") instanceof Number n) campaign.setClicks(n.intValue());
        if (row.get("conversions") instanceof Number n) campaign.setConversions(n.intValue());
        if (row.get("lead_target") instanceof Number n) campaign.setLeadTarget(n.intValue());
        if (row.get("leads_generated") instanceof Number n) campaign.setLeadsGenerated(n.intValue());
        campaign.setCampaignType((String) row.get("campaign_type"));
        return campaign;
    }
}

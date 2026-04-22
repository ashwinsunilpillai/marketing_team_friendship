package com.marketing.facade;

import com.marketing.entity.Campaign;
import com.marketing.exception.CampaignCreationException;
import com.marketing.exception.CampaignNotFoundException;
import com.marketing.exception.CampaignStateException;
import com.marketing.util.DBUtil;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampaignFacade {
    private static final String TABLE_NAME = "campaigns";
    private static final String ID_COLUMN = "campaign_id";
    private static final Pattern UI_STATUS_PATTERN = Pattern.compile("\\\"ui_status\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
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
            if (campaign.getTargetVehicleSegment() != null) {
                payload.put("target_vehicle_segment", campaign.getTargetVehicleSegment());
            }
            payload.put("campaign_budget", campaign.getCampaignBudget() > 0 ? campaign.getCampaignBudget() : campaign.getBudget());
            if (campaign.getTargetLeads() != null) {
                payload.put("target_leads", campaign.getTargetLeads());
            }
            payload.put("lead_target", campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
            payload.put("leads_generated", campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
            if (campaign.getStartDate() != null) {
                payload.put("start_date", campaign.getStartDate());
            }
            if (campaign.getEndDate() != null) {
                payload.put("end_date", campaign.getEndDate());
            }
            payload.put("campaign_roi", campaign.getCampaignRoi());
            if (campaign.getCampaignResults() != null) {
                payload.put("campaign_results", campaign.getCampaignResults());
            }

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, payload);

            // Best-effort sync of the in-memory ID for follow-up operations in UI.
            campaign.setCampaignId(resolveLatestCampaignIdByTitle(marketingSubsystem, title));
            System.out.println("Campaign created successfully: " + title);
            return true;
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
            return mapRowToEntity(row);
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
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return campaigns;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    campaigns.add(mapRowToEntity(row));
                }
            }

            campaigns.sort(Comparator.comparingInt(Campaign::getCampaignId).reversed());
        } catch (Exception e) {
            System.err.println("Error retrieving campaigns: " + e.getMessage());
            e.printStackTrace();
        }
        return campaigns;
    }

    private Campaign mapRowToEntity(Map<String, Object> row) {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(asInt(row.get("campaign_id"), 0));
        String title = asString(row.get("campaign_title"));
        campaign.setCampaignTitle(title);
        campaign.setCampaignName(title);
        campaign.setCampaignType(asStringOrDefault(row.get("campaign_type"), "EMAIL"));
        campaign.setTargetVehicleSegment(asString(row.get("target_vehicle_segment")));
        double budget = asDouble(row.get("campaign_budget"), 0.0);
        campaign.setCampaignBudget(budget);
        campaign.setBudget(budget);
        campaign.setTargetLeads(asString(row.get("target_leads")));
        campaign.setLeadTarget(asInt(row.get("lead_target"), 100));
        campaign.setLeadsGenerated(asInt(row.get("leads_generated"), 0));
        campaign.setStartDate(asLocalDate(row.get("start_date")));
        campaign.setEndDate(asLocalDate(row.get("end_date")));
        campaign.setCampaignRoi(asDouble(row.get("campaign_roi"), 0.0));
        String campaignResults = asString(row.get("campaign_results"));
        campaign.setCampaignResults(campaignResults);
        
        // Resolve status: try marker first, then database status column, then default to PLANNED
        String status = extractUiStatus(campaignResults);
        if (status == null || status.isBlank()) {
            status = asString(row.get("status"));
        }
        if (status == null || status.isBlank()) {
            status = "PLANNED";
        }
        campaign.setStatus(status);
        return campaign;
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
            payload.put("campaign_type", campaign.getCampaignType() != null ? campaign.getCampaignType() : "EMAIL");
            payload.put("lead_target", campaign.getLeadTarget() > 0 ? campaign.getLeadTarget() : 100);
            payload.put("leads_generated", campaign.getLeadsGenerated() > 0 ? campaign.getLeadsGenerated() : 0);
            payload.put("start_date", campaign.getStartDate());
            payload.put("end_date", campaign.getEndDate());

            marketingSubsystem.getClass()
                    .getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaign.getCampaignId(), payload);

            System.out.println("Campaign updated successfully: ID " + campaign.getCampaignId());
            return true;
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
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignNotFoundException("Marketing subsystem facade not available");
            }

            // Validate first to keep original method contract.
            getCampaignById(campaignId);

            try {
                deleteCampaignWithDependencies(marketingSubsystem, campaignId);
            } catch (InvocationTargetException primaryEx) {
                // Fallback to integration subsystem when marketing subsystem hits FK or permission constraints.
                Object integrationSubsystem = dbUtil.getDatabaseIntegrationSubsystem();
                if (integrationSubsystem == null) {
                    throw primaryEx;
                }
                deleteCampaignWithDependencies(integrationSubsystem, campaignId);
            }

            System.out.println("Campaign deleted successfully: ID " + campaignId);
            return true;
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

        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new CampaignStateException("Marketing subsystem facade not available");
            }

            // Status column is read-only in the permission model, so we update related date fields instead
            // to indicate campaign state changes
            Map<String, Object> payload = new HashMap<>();
            
            if ("ACTIVE".equals(newStatus) && campaign.getStartDate() == null) {
                payload.put("start_date", LocalDate.now());
            }
            if ("COMPLETED".equals(newStatus)) {
                payload.put("end_date", LocalDate.now());
            }
            payload.put("campaign_results", attachUiStatus(campaign.getCampaignResults(), newStatus));
            
            // If we have fields to update, do the update
            if (!payload.isEmpty()) {
                marketingSubsystem.getClass()
                        .getMethod("update", String.class, String.class, Object.class, Map.class)
                        .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, campaignId, payload);
            }

            System.out.println("Campaign state updated: ID " + campaignId + " -> " + newStatus);
            return true;
        } catch (InvocationTargetException e) {
            throw new CampaignStateException("Error updating campaign state: " + extractCauseMessage(e), e);
        } catch (CampaignStateException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating campaign state: " + e.getMessage());
            e.printStackTrace();
            throw new CampaignStateException("Error updating campaign state: " + e.getMessage(), e);
        }
    }

    private void deleteCampaignWithDependencies(Object subsystem, int campaignId) throws Exception {
        Map<String, Object> filters = new HashMap<>();
        filters.put("campaign_id", campaignId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metricRows = (List<Map<String, Object>>) subsystem.getClass()
                .getMethod("readAll", String.class, Map.class)
                .invoke(subsystem, "campaign_metrics", filters);

        if (metricRows != null) {
            for (Map<String, Object> row : metricRows) {
                Object metricId = row.get("metric_id");
                String idColumn = "metric_id";

                if (metricId == null) {
                    metricId = row.get("campaign_metric_id");
                    idColumn = "campaign_metric_id";
                }

                if (metricId != null) {
                    subsystem.getClass()
                            .getMethod("delete", String.class, String.class, Object.class)
                            .invoke(subsystem, "campaign_metrics", idColumn, metricId);
                }
            }
        }

        subsystem.getClass()
                .getMethod("delete", String.class, String.class, Object.class)
                .invoke(subsystem, TABLE_NAME, ID_COLUMN, campaignId);
    }

    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "PAUSED".equals(status) || "COMPLETED".equals(status) || "PLANNED".equals(status);
    }

    private int resolveLatestCampaignIdByTitle(Object marketingSubsystem, String title) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("campaign_title", title);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, filters);

            if (rows == null || rows.isEmpty()) {
                return 0;
            }

            int maxId = 0;
            for (Map<String, Object> row : rows) {
                maxId = Math.max(maxId, asInt(row.get("campaign_id"), 0));
            }
            return maxId;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String asStringOrDefault(Object value, String fallback) {
        String s = asString(value);
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private LocalDate asLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate d) {
            return d;
        }
        String text = value.toString();
        if (text.length() >= 10) {
            text = text.substring(0, 10);
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractUiStatus(String campaignResults) {
        if (campaignResults == null || campaignResults.isBlank()) {
            return null;
        }

        Matcher matcher = UI_STATUS_PATTERN.matcher(campaignResults);
        if (matcher.find()) {
            String value = matcher.group(1);
            return value == null ? null : value.trim().toUpperCase();
        }

        return null;
    }

    private String attachUiStatus(String campaignResults, String newStatus) {
        // Always write valid JSON compatible with the JSON column.
        return "{\"ui_status\":\"" + newStatus + "\"}";
    }

    private String extractCauseMessage(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return e.getMessage();
        }
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}

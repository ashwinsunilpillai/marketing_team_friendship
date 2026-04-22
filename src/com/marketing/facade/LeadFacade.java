package com.marketing.facade;

import com.marketing.entity.Lead;
import com.marketing.util.DBUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeadFacade {
    private final DBUtil dbUtil = DBUtil.getInstance();
    private static final String TABLE_NAME = "leads";
    private static final String ID_COLUMN = "lead_id";

    public boolean createLead(Lead lead) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", lead.getName());
            payload.put("email", lead.getEmail());
            payload.put("campaign_id", lead.getCampaignId());
            payload.put("state", lead.getState() != null ? lead.getState() : "New");

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, payload);

            return true;
        } catch (Exception e) {
            System.err.println("Error creating lead: " + e.getMessage());
            return false;
        }
    }

    public List<Lead> getLeadsByCampaignId(int campaignId) {
        List<Lead> results = new ArrayList<>();
        
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return results;
            }

            Map<String, Object> filters = new HashMap<>();
            filters.put("campaign_id", campaignId);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, filters);

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapToEntity(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading leads by campaign: " + e.getMessage());
        }
        return results;
    }

    // Backward-compatible alias used by UI code.
    public List<Lead> getLeadsByCampaign(int campaignId) {
        return getLeadsByCampaignId(campaignId);
    }

    public List<Lead> getAllLeads() {
        List<Lead> results = new ArrayList<>();
        
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return results;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    results.add(mapToEntity(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading leads: " + e.getMessage());
        }
        return results;
    }

    public boolean updateLeadState(int leadId, String newState) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("state", newState);
            if ("Converted".equalsIgnoreCase(newState)) {
                payload.put("converted_at", LocalDateTime.now());
            }

            marketingSubsystem.getClass()
                    .getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, leadId, payload);

            return true;
        } catch (Exception e) {
            System.err.println("Error updating lead state: " + e.getMessage());
            return false;
        }
    }

    // Backward-compatible alias used by UI code.
    public boolean transitionLeadState(int leadId, String newState) {
        return updateLeadState(leadId, newState);
    }

    // Backward-compatible method used by UI code. Revenue is ignored because Lead entity
    // in this project currently does not model closed_value.
    public boolean convertLeadWithRevenue(int leadId, double revenue) {
        return updateLeadState(leadId, "Converted");
    }

    public boolean deleteLead(int leadId) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return false;
            }

            marketingSubsystem.getClass()
                    .getMethod("delete", String.class, String.class, Object.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, leadId);

            return true;
        } catch (Exception e) {
            System.err.println("Error deleting lead: " + e.getMessage());
            return false;
        }
    }

    private Lead mapToEntity(Map<String, Object> row) {
        Lead lead = new Lead();
        
        if (row.containsKey("lead_id")) {
            lead.setLeadId(((Number) row.get("lead_id")).intValue());
        }
        if (row.containsKey("name")) {
            lead.setName((String) row.get("name"));
        }
        if (row.containsKey("email")) {
            lead.setEmail((String) row.get("email"));
        }
        if (row.containsKey("campaign_id")) {
            lead.setCampaignId(((Number) row.get("campaign_id")).intValue());
        }
        if (row.containsKey("state")) {
            lead.setState((String) row.get("state"));
        }
        if (row.containsKey("created_at") && row.get("created_at") instanceof LocalDateTime) {
            lead.setCreatedAt((LocalDateTime) row.get("created_at"));
        }
        if (row.containsKey("updated_at") && row.get("updated_at") instanceof LocalDateTime) {
            lead.setUpdatedAt((LocalDateTime) row.get("updated_at"));
        }
        
        return lead;
    }
}

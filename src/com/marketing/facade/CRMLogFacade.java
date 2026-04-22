package com.marketing.facade;

import com.marketing.entity.CRMSyncLog;
import com.marketing.util.DBUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRMLogFacade {
    private final DBUtil dbUtil = DBUtil.getInstance();
    private static final String TABLE_NAME = "crm_sync_log";
    private static final String ID_COLUMN = "log_id";

    public boolean createLog(String source, String details) {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                System.err.println("Marketing subsystem facade not available");
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("source", source);
            payload.put("details", details);

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, payload);

            return true;
        } catch (Exception e) {
            System.err.println("Error creating CRM log: " + e.getMessage());
            return false;
        }
    }

    public List<CRMSyncLog> getRecentLogs(int limit) {
        List<CRMSyncLog> results = new ArrayList<>();
        
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
                int count = 0;
                for (Map<String, Object> row : rows) {
                    if (count >= limit) break;
                    
                    CRMSyncLog log = new CRMSyncLog();
                    if (row.containsKey("log_id")) {
                        log.setLogId(((Number) row.get("log_id")).intValue());
                    }
                    if (row.containsKey("source")) {
                        log.setSource((String) row.get("source"));
                    }
                    if (row.containsKey("details")) {
                        log.setDetails((String) row.get("details"));
                    }
                    if (row.containsKey("created_at") && row.get("created_at") != null) {
                        Object createdAt = row.get("created_at");
                        if (createdAt instanceof LocalDateTime) {
                            log.setCreatedAt((LocalDateTime) createdAt);
                        }
                    }
                    results.add(log);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CRM logs: " + e.getMessage());
        }
        return results;
    }
}

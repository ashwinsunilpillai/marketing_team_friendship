package com.marketing.facade;

import com.marketing.entity.Segment;
import com.marketing.exception.InvalidSegmentCriteriaException;
import com.marketing.exception.SegmentNotFoundException;
import com.marketing.util.DBUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentManager {
    private static final String TABLE_NAME = "segments";
    private static final String ID_COLUMN = "segment_id";
    private final DBUtil dbUtil;

    public SegmentManager() {
        this.dbUtil = DBUtil.getInstance();
    }

    public boolean createSegment(Segment segment) throws InvalidSegmentCriteriaException {
        if (segment == null || segment.getSegmentName() == null || segment.getSegmentName().isBlank()) {
            throw new InvalidSegmentCriteriaException("Segment cannot be null");
        }

        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new InvalidSegmentCriteriaException("Marketing subsystem facade not available");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("segment_name", segment.getSegmentName());
            payload.put("segment_type", segment.getSegmentType());
            payload.put("criteria", segment.getCriteria());
            payload.put("description", segment.getDescription());

            marketingSubsystem.getClass()
                    .getMethod("create", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, payload);
            return true;
        } catch (InvalidSegmentCriteriaException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidSegmentCriteriaException("Failed to create segment: " + e.getMessage(), e);
        }
    }

    public Segment getSegmentById(int segmentId) throws SegmentNotFoundException {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new SegmentNotFoundException("Marketing subsystem facade not available");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) marketingSubsystem.getClass()
                    .getMethod("readById", String.class, String.class, Object.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, segmentId);

            if (row == null || row.isEmpty()) {
                throw new SegmentNotFoundException("Segment with ID " + segmentId + " not found");
            }

            return mapToEntity(row);
        } catch (SegmentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new SegmentNotFoundException("Error retrieving segment: " + e.getMessage(), e);
        }
    }

    public List<Segment> getAllSegments() {
        List<Segment> segments = new ArrayList<>();
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                return segments;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) marketingSubsystem.getClass()
                    .getMethod("readAll", String.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, new HashMap<>());

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    segments.add(mapToEntity(row));
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving segments: " + e.getMessage());
        }
        return segments;
    }

    public boolean updateSegment(Segment segment) throws SegmentNotFoundException {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new SegmentNotFoundException("Marketing subsystem facade not available");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("segment_name", segment.getSegmentName());
            payload.put("segment_type", segment.getSegmentType());
            payload.put("criteria", segment.getCriteria());
            payload.put("description", segment.getDescription());

            marketingSubsystem.getClass()
                    .getMethod("update", String.class, String.class, Object.class, Map.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, segment.getSegmentId(), payload);
            return true;
        } catch (SegmentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating segment: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSegment(int segmentId) throws SegmentNotFoundException {
        try {
            Object marketingSubsystem = dbUtil.getMarketingSubsystem();
            if (marketingSubsystem == null) {
                throw new SegmentNotFoundException("Marketing subsystem facade not available");
            }

            marketingSubsystem.getClass()
                    .getMethod("delete", String.class, String.class, Object.class)
                    .invoke(marketingSubsystem, TABLE_NAME, ID_COLUMN, segmentId);
            return true;
        } catch (SegmentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error deleting segment: " + e.getMessage());
            return false;
        }
    }

    private Segment mapToEntity(Map<String, Object> row) {
        Segment segment = new Segment();
        if (row.get("segment_id") instanceof Number n) segment.setSegmentId(n.intValue());
        segment.setSegmentName((String) row.get("segment_name"));
        segment.setSegmentType((String) row.get("segment_type"));
        segment.setCriteria((String) row.get("criteria"));
        segment.setDescription((String) row.get("description"));
        if (row.get("customer_count") instanceof Number n) segment.setCustomerCount(n.intValue());
        return segment;
    }
}

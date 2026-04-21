package com.marketing.facade;

import com.marketing.entity.Segment;
import com.marketing.exception.*;
import com.marketing.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SegmentManager - Segment Management using Facade Pattern
 * Provides CRUD operations for customer segments.
 * GRASP: Creator (creates Segment objects), Information Expert (knows segment DB operations)
 */
public class SegmentManager {
    private DBUtil dbUtil;
    
    public SegmentManager() {
        this.dbUtil = DBUtil.getInstance();
    }
    
    /**
     * Creates a new segment
     * @param segment Segment object to create
     * @return true if successful
     * @throws InvalidSegmentCriteriaException if criteria is invalid
     */
    public boolean createSegment(Segment segment) throws InvalidSegmentCriteriaException {
        if (segment == null || segment.getSegmentName() == null) {
            throw new InvalidSegmentCriteriaException("Segment cannot be null");
        }
        
        // Use customer_segments table for new schema, fall back to segments for legacy
        String sql = "INSERT INTO customer_segments (segment_name, segment_description, criteria_definition) VALUES (?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, segment.getSegmentName());
            pstmt.setString(2, segment.getSegmentDescription() != null ? segment.getSegmentDescription() : segment.getDescription());
            pstmt.setString(3, segment.getCriteriaDefinition() != null ? segment.getCriteriaDefinition() : segment.getCriteria());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            // Try legacy table if customer_segments doesn't exist
            if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("no such table")) {
                try {
                    String legacySql = "INSERT INTO segments (segment_name, segment_type, criteria, description) VALUES (?, ?, ?, ?)";
                    try (Connection conn2 = dbUtil.getConnection();
                         PreparedStatement pstmt2 = conn2.prepareStatement(legacySql)) {
                        pstmt2.setString(1, segment.getSegmentName());
                        pstmt2.setString(2, segment.getSegmentType());
                        pstmt2.setString(3, segment.getCriteria());
                        pstmt2.setString(4, segment.getDescription());
                        return pstmt2.executeUpdate() > 0;
                    }
                } catch (SQLException e2) {
                    throw new InvalidSegmentCriteriaException("Failed to create segment: " + e2.getMessage(), e2);
                }
            }
            throw new InvalidSegmentCriteriaException("Failed to create segment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves a segment by ID
     * @param segmentId ID of the segment
     * @return Segment object
     * @throws SegmentNotFoundException if not found
     */
    public Segment getSegmentById(int segmentId) throws SegmentNotFoundException {
        String sql = "SELECT * FROM segments WHERE segment_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, segmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSegment(rs);
            } else {
                throw new SegmentNotFoundException("Segment with ID " + segmentId + " not found");
            }
            
        } catch (SQLException e) {
            throw new SegmentNotFoundException("Error retrieving segment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all segments
     * @return List of all segments
     */
    public List<Segment> getAllSegments() {
        List<Segment> segments = new ArrayList<>();
        String sql = "SELECT * FROM segments";
        
        try (Connection conn = dbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                segments.add(mapResultSetToSegment(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving segments: " + e.getMessage());
        }
        
        return segments;
    }
    
    /**
     * Updates a segment
     * @param segment Segment with updated values
     * @return true if successful
     * @throws SegmentNotFoundException if not found
     */
    public boolean updateSegment(Segment segment) throws SegmentNotFoundException {
        String sql = "UPDATE segments SET segment_name = ?, segment_type = ?, criteria = ?, description = ? WHERE segment_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, segment.getSegmentName());
            pstmt.setString(2, segment.getSegmentType());
            pstmt.setString(3, segment.getCriteria());
            pstmt.setString(4, segment.getDescription());
            pstmt.setInt(5, segment.getSegmentId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SegmentNotFoundException("Segment with ID " + segment.getSegmentId() + " not found");
            }
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error updating segment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a segment
     * @param segmentId ID of segment to delete
     * @return true if successful
     * @throws SegmentNotFoundException if not found
     */
    public boolean deleteSegment(int segmentId) throws SegmentNotFoundException {
        String sql = "DELETE FROM segments WHERE segment_id = ?";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, segmentId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SegmentNotFoundException("Segment with ID " + segmentId + " not found");
            }
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting segment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Maps a ResultSet row to a Segment object
     * @param rs ResultSet to map from
     * @return Segment object
     * @throws SQLException if database access error occurs
     */
    private Segment mapResultSetToSegment(ResultSet rs) throws SQLException {
        Segment segment = new Segment();
        segment.setSegmentId(rs.getInt("segment_id"));
        segment.setSegmentName(rs.getString("segment_name"));
        
        // Try new schema columns first
        try {
            segment.setSegmentDescription(rs.getString("segment_description"));
        } catch (SQLException ignore) {
        }
        try {
            segment.setCriteriaDefinition(rs.getString("criteria_definition"));
        } catch (SQLException ignore) {
        }
        
        // Legacy columns
        try {
            segment.setSegmentType(rs.getString("segment_type"));
        } catch (SQLException ignore) {
        }
        try {
            segment.setCriteria(rs.getString("criteria"));
        } catch (SQLException ignore) {
        }
        try {
            segment.setDescription(rs.getString("description"));
        } catch (SQLException ignore) {
        }
        
        try {
            segment.setCustomerCount(rs.getInt("customer_count"));
        } catch (SQLException e) {
            segment.setCustomerCount(0);
        }
        
        return segment;
    }
}

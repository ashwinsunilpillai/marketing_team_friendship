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
        
        String sql = "INSERT INTO segments (segment_name, segment_type, criteria, description) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, segment.getSegmentName());
            pstmt.setString(2, segment.getSegmentType());
            pstmt.setString(3, segment.getCriteria());
            pstmt.setString(4, segment.getDescription());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
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
        segment.setSegmentType(rs.getString("segment_type"));
        segment.setCriteria(rs.getString("criteria"));
        segment.setDescription(rs.getString("description"));
        
        try {
            segment.setCustomerCount(rs.getInt("customer_count"));
        } catch (SQLException e) {
            segment.setCustomerCount(0);
        }
        
        return segment;
    }
}

package com.marketing.entity;

/**
 * Segment Entity Class
 * Represents a customer segment based on segmentation criteria.
 * Maps directly to the segments table in the database.
 * GRASP: Information Expert (knows its own data)
 */
public class Segment {
    private int segmentId;
    private String segmentName;
    private String segmentType; // CITY, AGE_GROUP, INTEREST
    private String criteria; // The specific criteria value (e.g., "Bangalore", "25-35", "Sports")
    private int customerCount;
    private String description;
    
    /**
     * Default constructor
     */
    public Segment() {
    }
    
    /**
     * Constructor with essential fields
     */
    public Segment(String segmentName, String segmentType, String criteria) {
        this.segmentName = segmentName;
        this.segmentType = segmentType;
        this.criteria = criteria;
        this.customerCount = 0;
    }
    
    /**
     * Full constructor
     */
    public Segment(int segmentId, String segmentName, String segmentType, String criteria, int customerCount) {
        this.segmentId = segmentId;
        this.segmentName = segmentName;
        this.segmentType = segmentType;
        this.criteria = criteria;
        this.customerCount = customerCount;
    }
    
    // Getters and Setters
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }
    
    public String getSegmentName() { return segmentName; }
    public void setSegmentName(String segmentName) { this.segmentName = segmentName; }
    
    public String getSegmentType() { return segmentType; }
    public void setSegmentType(String segmentType) { this.segmentType = segmentType; }
    
    public String getCriteria() { return criteria; }
    public void setCriteria(String criteria) { this.criteria = criteria; }
    
    public int getCustomerCount() { return customerCount; }
    public void setCustomerCount(int customerCount) { this.customerCount = customerCount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() {
        return "Segment{" +
                "segmentId=" + segmentId +
                ", segmentName='" + segmentName + '\'' +
                ", segmentType='" + segmentType + '\'' +
                ", criteria='" + criteria + '\'' +
                ", customerCount=" + customerCount +
                '}';
    }
}

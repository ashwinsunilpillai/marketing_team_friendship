package com.marketing.entity;

/**
 * Segment Entity Class
 * Represents a customer segment based on segmentation criteria.
 * Maps to both the customer_segments table and the segments view.
 * GRASP: Information Expert (knows its own data)
 */
public class Segment {
    private int segmentId;
    private String segmentName;
    private String segmentDescription;
    private String criteriaDefinition;
    
    // Legacy fields for backward compatibility
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
     * Constructor with essential fields (new schema)
     */
    public Segment(String segmentName, String criteriaDefinition) {
        this.segmentName = segmentName;
        this.criteriaDefinition = criteriaDefinition;
    }
    
    /**
     * Full constructor (new schema)
     */
    public Segment(int segmentId, String segmentName, String segmentDescription, String criteriaDefinition) {
        this.segmentId = segmentId;
        this.segmentName = segmentName;
        this.segmentDescription = segmentDescription;
        this.criteriaDefinition = criteriaDefinition;
    }
    
    /**
     * Legacy constructor (backward compatibility)
     */
    public Segment(String segmentName, String segmentType, String criteria) {
        this.segmentName = segmentName;
        this.segmentType = segmentType;
        this.criteria = criteria;
        this.criteriaDefinition = criteria;
        this.customerCount = 0;
    }
    
    /**
     * Legacy full constructor
     */
    public Segment(int segmentId, String segmentName, String segmentType, String criteria, int customerCount) {
        this.segmentId = segmentId;
        this.segmentName = segmentName;
        this.segmentType = segmentType;
        this.criteria = criteria;
        this.criteriaDefinition = criteria;
        this.customerCount = customerCount;
    }
    
    // New schema getters/setters
    public int getSegmentId() { return segmentId; }
    public void setSegmentId(int segmentId) { this.segmentId = segmentId; }
    
    public String getSegmentName() { return segmentName; }
    public void setSegmentName(String segmentName) { this.segmentName = segmentName; }
    
    public String getSegmentDescription() { return segmentDescription; }
    public void setSegmentDescription(String segmentDescription) { this.segmentDescription = segmentDescription; }
    
    public String getCriteriaDefinition() { return criteriaDefinition; }
    public void setCriteriaDefinition(String criteriaDefinition) { this.criteriaDefinition = criteriaDefinition; }
    
    // Legacy backward compatibility getters/setters
    public String getSegmentType() { return segmentType; }
    public void setSegmentType(String segmentType) { this.segmentType = segmentType; }
    
    public String getCriteria() { return criteriaDefinition != null ? criteriaDefinition : criteria; }
    public void setCriteria(String criteria) { 
        this.criteria = criteria;
        if (this.criteriaDefinition == null) this.criteriaDefinition = criteria;
    }
    
    public int getCustomerCount() { return customerCount; }
    public void setCustomerCount(int customerCount) { this.customerCount = customerCount; }
    
    public String getDescription() { return segmentDescription != null ? segmentDescription : description; }
    public void setDescription(String description) { 
        this.description = description;
        if (this.segmentDescription == null) this.segmentDescription = description;
    }
    
    @Override
    public String toString() {
        return "Segment{" +
                "segmentId=" + segmentId +
                ", segmentName='" + segmentName + '\'' +
                ", criteriaDefinition='" + criteriaDefinition + '\'' +
                '}';
    }
}

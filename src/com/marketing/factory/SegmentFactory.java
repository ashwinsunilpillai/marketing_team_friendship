package com.marketing.factory;

import com.marketing.entity.Segment;
import com.marketing.exception.InvalidSegmentCriteriaException;

/**
 * SegmentFactory - Creational Factory Method Pattern
 * Creates different types of segments based on segmentation criteria.
 * GRASP: Creator (creates Segment objects), Polymorphism
 * SOLID: OCP (open for extension - can add new segment types without modifying existing code)
 */
public class SegmentFactory {
    
    /**
     * Factory method to create a segment based on type and criteria
     * @param segmentType Type of segmentation (CITY, AGE_GROUP, INTEREST)
     * @param criteria The specific criteria value
     * @return A Segment object of the appropriate type
     * @throws InvalidSegmentCriteriaException if the criteria is invalid for the type
     */
    public static Segment createSegment(String segmentType, String criteria) throws InvalidSegmentCriteriaException {
        if (segmentType == null || criteria == null) {
            throw new InvalidSegmentCriteriaException("Segment type and criteria cannot be null");
        }
        
        switch (segmentType.toUpperCase()) {
            case "CITY":
                return createCitySegment(criteria);
            case "AGE_GROUP":
                return createAgeGroupSegment(criteria);
            case "INTEREST":
                return createInterestSegment(criteria);
            default:
                throw new InvalidSegmentCriteriaException("Invalid segment type: " + segmentType);
        }
    }
    
    /**
     * Creates a city-based segment
     * @param city The city name
     * @return A city-based Segment
     * @throws InvalidSegmentCriteriaException if city is invalid
     */
    private static Segment createCitySegment(String city) throws InvalidSegmentCriteriaException {
        if (city == null || city.trim().isEmpty()) {
            throw new InvalidSegmentCriteriaException("City name cannot be empty");
        }
        
        Segment segment = new Segment();
        segment.setSegmentName("City: " + city);
        segment.setSegmentType("CITY");
        segment.setCriteria(city);
        segment.setDescription("Segment for customers in " + city);
        
        return segment;
    }
    
    /**
     * Creates an age-group-based segment
     * @param ageGroup The age group range (e.g., "18-25", "25-35", "35-50", "50+")
     * @return An age-group-based Segment
     * @throws InvalidSegmentCriteriaException if age group format is invalid
     */
    private static Segment createAgeGroupSegment(String ageGroup) throws InvalidSegmentCriteriaException {
        if (ageGroup == null || !isValidAgeGroup(ageGroup)) {
            throw new InvalidSegmentCriteriaException("Invalid age group format: " + ageGroup + 
                    ". Use format like '18-25', '25-35', '35-50', or '50+'");
        }
        
        Segment segment = new Segment();
        segment.setSegmentName("Age Group: " + ageGroup);
        segment.setSegmentType("AGE_GROUP");
        segment.setCriteria(ageGroup);
        segment.setDescription("Segment for customers aged " + ageGroup);
        
        return segment;
    }
    
    /**
     * Creates an interest-based segment
     * @param interest The interest category (e.g., "Sports", "Technology", "Fashion", "Travel")
     * @return An interest-based Segment
     * @throws InvalidSegmentCriteriaException if interest is invalid
     */
    private static Segment createInterestSegment(String interest) throws InvalidSegmentCriteriaException {
        if (interest == null || interest.trim().isEmpty()) {
            throw new InvalidSegmentCriteriaException("Interest category cannot be empty");
        }
        
        // Validate against known interests
        if (!isValidInterest(interest)) {
            System.out.println("Warning: Unknown interest category '" + interest + "'. Proceeding anyway.");
        }
        
        Segment segment = new Segment();
        segment.setSegmentName("Interest: " + interest);
        segment.setSegmentType("INTEREST");
        segment.setCriteria(interest);
        segment.setDescription("Segment for customers interested in " + interest);
        
        return segment;
    }
    
    /**
     * Validates if an age group format is correct
     * @param ageGroup The age group to validate
     * @return true if valid, false otherwise
     */
    private static boolean isValidAgeGroup(String ageGroup) {
        return ageGroup.matches("(18-25|25-35|35-50|50\\+|\\d+-\\d+)");
    }
    
    /**
     * Validates if an interest category is known
     * @param interest The interest to validate
     * @return true if known, false otherwise
     */
    private static boolean isValidInterest(String interest) {
        String[] knownInterests = {"Sports", "Technology", "Fashion", "Travel", "Food", 
                                  "Entertainment", "Health", "Finance", "Education", "Automotive"};
        for (String known : knownInterests) {
            if (known.equalsIgnoreCase(interest)) {
                return true;
            }
        }
        return false;
    }
}

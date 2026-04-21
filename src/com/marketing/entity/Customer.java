package com.marketing.entity;

import java.time.LocalDate;

/**
 * Customer Entity Class
 * Represents a customer in the CRM system.
 * Maps directly to the customers table in the database.
 * GRASP: Information Expert (knows its own data)
 */
public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String segment;
    private String region;
    private String interestedCarModel;
    private String purchasedVin;
    private String vehicleModelYear;
    private double lifetimeValue;
    private String status; // ACTIVE, INACTIVE
    private LocalDate lastContactDate;
    
    // Legacy fields for backward compatibility
    private String firstName;
    private String lastName;
    private String city;
    private int age;
    private String interest;
    
    /**
     * Default constructor
     */
    public Customer() {
    }
    
    /**
     * Constructor with essential fields (new schema)
     */
    public Customer(String name, String email, String phone, String region) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.region = region;
        this.status = "ACTIVE";
    }
    
    /**
     * Full constructor (new schema)
     */
    public Customer(int customerId, String name, String email, String phone, String segment, String region,
                   String interestedCarModel, String purchasedVin, String vehicleModelYear, 
                   double lifetimeValue, String status, LocalDate lastContactDate) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.segment = segment;
        this.region = region;
        this.interestedCarModel = interestedCarModel;
        this.purchasedVin = purchasedVin;
        this.vehicleModelYear = vehicleModelYear;
        this.lifetimeValue = lifetimeValue;
        this.status = status;
        this.lastContactDate = lastContactDate;
    }
    
    /**
     * Legacy constructor (backward compatibility)
     */
    public Customer(int customerId, String firstName, String lastName, String email, 
                    String phone, String city, int age, String interest, String status) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.age = age;
        this.interest = interest;
        this.status = status;
        this.name = firstName + " " + lastName;
        this.region = city;
    }
    
    // New schema getters/setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getInterestedCarModel() { return interestedCarModel; }
    public void setInterestedCarModel(String interestedCarModel) { this.interestedCarModel = interestedCarModel; }
    
    public String getPurchasedVin() { return purchasedVin; }
    public void setPurchasedVin(String purchasedVin) { this.purchasedVin = purchasedVin; }
    
    public String getVehicleModelYear() { return vehicleModelYear; }
    public void setVehicleModelYear(String vehicleModelYear) { this.vehicleModelYear = vehicleModelYear; }
    
    public double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDate getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDate lastContactDate) { this.lastContactDate = lastContactDate; }
    
    // Legacy backward compatibility getters/setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        updateNameFromParts();
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        updateNameFromParts();
    }
    
    public String getCity() { return city; }
    public void setCity(String city) { 
        this.city = city;
        if (this.region == null) this.region = city;
    }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
    
    private void updateNameFromParts() {
        if (firstName != null && lastName != null) {
            this.name = firstName + " " + lastName;
        } else if (firstName != null) {
            this.name = firstName;
        } else if (lastName != null) {
            this.name = lastName;
        }
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", region='" + region + '\'' +
                ", status='" + status + '\'' +
                ", lifetimeValue=" + lifetimeValue +
                '}';
    }
}

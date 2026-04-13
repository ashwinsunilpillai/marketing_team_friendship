package com.marketing.entity;

/**
 * Customer Entity Class
 * Represents a customer in the CRM system.
 * Maps directly to the customers table in the database.
 * GRASP: Information Expert (knows its own data)
 */
public class Customer {
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String city;
    private int age;
    private String interest;
    private String status; // ACTIVE, INACTIVE, UNSUBSCRIBED
    
    /**
     * Default constructor
     */
    public Customer() {
    }
    
    /**
     * Constructor with essential fields
     */
    public Customer(String firstName, String lastName, String email, String phone, 
                    String city, int age, String interest) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.age = age;
        this.interest = interest;
        this.status = "ACTIVE";
    }
    
    /**
     * Full constructor
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
    }
    
    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", age=" + age +
                ", interest='" + interest + '\'' +
                '}';
    }
}

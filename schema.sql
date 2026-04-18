-- Marketing ERP Database Schema
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS marketing_erp;
USE marketing_erp;

-- Campaigns Table
CREATE TABLE IF NOT EXISTS campaigns (
    campaign_id INT AUTO_INCREMENT PRIMARY KEY,
    campaign_name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    segment_id INT,
    description TEXT,
    impressions INT DEFAULT 0,
    clicks INT DEFAULT 0,
    conversions INT DEFAULT 0,
    lead_target INT DEFAULT 0,
    leads_generated INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(100),
    age INT,
    interest VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Segments Table
CREATE TABLE IF NOT EXISTS segments (
    segment_id INT AUTO_INCREMENT PRIMARY KEY,
    segment_name VARCHAR(255) NOT NULL,
    segment_type VARCHAR(50) NOT NULL,
    criteria VARCHAR(255) NOT NULL,
    customer_count INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_campaign_status ON campaigns(status);
CREATE INDEX idx_campaign_segment ON campaigns(segment_id);
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_city ON customers(city);
CREATE INDEX idx_customer_age ON customers(age);
CREATE INDEX idx_segment_type ON segments(segment_type);

-- Sample data for testing
INSERT INTO segments (segment_name, segment_type, criteria, customer_count, description) VALUES
('Bangalore Tech', 'CITY', 'Bangalore', 0, 'Tech-savvy customers in Bangalore'),
('Young Professionals', 'AGE_GROUP', '25-35', 0, 'Professionals aged 25-35'),
('Sports Enthusiasts', 'INTEREST', 'Sports', 0, 'Customers interested in sports');

INSERT INTO customers (first_name, last_name, email, phone, city, age, interest) VALUES
('John', 'Doe', 'john.doe@example.com', '9876543210', 'Bangalore', 28, 'Sports'),
('Jane', 'Smith', 'jane.smith@example.com', '9876543211', 'Bangalore', 32, 'Technology'),
('Bob', 'Johnson', 'bob.johnson@example.com', '9876543212', 'Delhi', 25, 'Fashion');

INSERT INTO campaigns (campaign_name, start_date, end_date, budget, status, segment_id, description) VALUES
('Summer Sale 2026', '2026-04-15', '2026-05-15', 50000, 'ACTIVE', 1, 'Summer promotional campaign for Bangalore tech segment'),
('Spring Clearance', '2026-04-01', '2026-04-30', 30000, 'ACTIVE', 2, 'Spring clearance for young professionals');

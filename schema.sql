-- Marketing ERP Database Schema
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS marketing_erp;
USE marketing_erp;

-- Campaigns Table
CREATE TABLE IF NOT EXISTS campaigns (
    campaign_id INT AUTO_INCREMENT PRIMARY KEY,
    campaign_title VARCHAR(120) NOT NULL,
    campaign_type VARCHAR(50),
    target_vehicle_segment VARCHAR(100),
    campaign_budget DECIMAL(15, 2),
    target_leads JSON,
    lead_target INT DEFAULT 100,
    leads_generated INT DEFAULT 0,
    start_date DATE,
    end_date DATE,
    campaign_roi DECIMAL(10, 2),
    campaign_results JSON,
    status VARCHAR(30) DEFAULT 'PLANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(30),
    segment VARCHAR(50),
    region VARCHAR(80),
    interested_car_model VARCHAR(100),
    purchased_vin VARCHAR(50),
    vehicle_model_year VARCHAR(30),
    lifetime_value DECIMAL(15, 2),
    status VARCHAR(30),
    last_contact_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Customer Segments Table (base table for segments view)
CREATE TABLE IF NOT EXISTS customer_segments (
    segment_id INT AUTO_INCREMENT PRIMARY KEY,
    segment_name VARCHAR(100) NOT NULL UNIQUE,
    segment_description TEXT,
    criteria_definition TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Segments View (compatibility view over customer_segments)
DROP VIEW IF EXISTS segments;
CREATE VIEW segments AS
SELECT 
    segment_id,
    segment_name AS name,
    criteria_definition AS criteria
FROM customer_segments;

-- Segment Members Table (junction table for customers in segments)
CREATE TABLE IF NOT EXISTS segment_members (
    segment_member_id INT AUTO_INCREMENT PRIMARY KEY,
    segment_id INT NOT NULL,
    customer_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_segment_members_segment FOREIGN KEY (segment_id) REFERENCES customer_segments(segment_id),
    CONSTRAINT fk_segment_members_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT uq_segment_customer UNIQUE (segment_id, customer_id)
);

-- Message Templates Table
CREATE TABLE IF NOT EXISTS message_templates (
    template_id INT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL UNIQUE,
    channel VARCHAR(30) NOT NULL,
    subject_template VARCHAR(255),
    body_template TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Campaign Messages Table (message delivery tracking)
CREATE TABLE IF NOT EXISTS campaign_messages (
    campaign_message_id INT AUTO_INCREMENT PRIMARY KEY,
    campaign_id INT NOT NULL,
    customer_id INT NOT NULL,
    template_id INT,
    channel VARCHAR(30) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaign_messages_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id),
    CONSTRAINT fk_campaign_messages_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_campaign_messages_template FOREIGN KEY (template_id) REFERENCES message_templates(template_id)
);

-- Campaign Metrics Table (performance metrics)
CREATE TABLE IF NOT EXISTS campaign_metrics (
    metric_id INT AUTO_INCREMENT PRIMARY KEY,
    campaign_id INT NOT NULL,
    metric_date DATE NOT NULL,
    impressions INT DEFAULT 0,
    clicks INT DEFAULT 0,
    conversions INT DEFAULT 0,
    revenue_generated DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaign_metrics_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
);

-- Consent Preferences Table
CREATE TABLE IF NOT EXISTS consent_preferences (
    consent_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    channel VARCHAR(30) NOT NULL,
    is_opted_in BOOLEAN DEFAULT FALSE,
    consent_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(80),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_consent_preferences_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_campaign_roi ON campaigns(campaign_roi);
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_segment ON customers(segment);
CREATE INDEX idx_customer_region ON customers(region);
CREATE INDEX idx_segment_members_segment_id ON segment_members(segment_id);
CREATE INDEX idx_segment_members_customer_id ON segment_members(customer_id);
CREATE INDEX idx_campaign_messages_campaign_id ON campaign_messages(campaign_id);
CREATE INDEX idx_campaign_messages_customer_id ON campaign_messages(customer_id);
CREATE INDEX idx_campaign_messages_delivery_status ON campaign_messages(delivery_status);
CREATE INDEX idx_campaign_metrics_campaign_id ON campaign_metrics(campaign_id);
CREATE INDEX idx_campaign_metrics_metric_date ON campaign_metrics(metric_date);
CREATE INDEX idx_consent_preferences_customer_id ON consent_preferences(customer_id);
CREATE INDEX idx_consent_preferences_channel ON consent_preferences(channel);

-- Sample data for testing
INSERT INTO customer_segments (segment_name, segment_description, criteria_definition) VALUES
('Bangalore Tech', 'Tech-savvy customers in Bangalore', 'region=Bangalore AND interested_car_model LIKE "%electric%"'),
('Young Professionals', 'Professionals aged relevant segment', 'lifetime_value > 50000'),
('Premium Customers', 'High-value customer segment', 'lifetime_value > 100000');

INSERT INTO customers (name, email, phone, segment, region, interested_car_model, purchased_vin, vehicle_model_year, lifetime_value, status, last_contact_date) VALUES
('John Doe', 'john.doe@example.com', '9876543210', 'Bangalore Tech', 'Bangalore', 'Tesla Model 3', 'VIN123456789', '2024', 75000, 'ACTIVE', '2026-04-10'),
('Jane Smith', 'jane.smith@example.com', '9876543211', 'Young Professionals', 'Bangalore', 'BMW X5', 'VIN987654321', '2023', 120000, 'ACTIVE', '2026-04-15'),
('Bob Johnson', 'bob.johnson@example.com', '9876543212', 'Premium Customers', 'Delhi', 'Mercedes S-Class', 'VIN456123789', '2025', 150000, 'ACTIVE', '2026-04-12');

INSERT INTO segment_members (segment_id, customer_id, assigned_at) VALUES
(1, 1, CURRENT_TIMESTAMP),
(2, 2, CURRENT_TIMESTAMP),
(3, 3, CURRENT_TIMESTAMP);

INSERT INTO message_templates (template_name, channel, subject_template, body_template) VALUES
('Welcome Email', 'EMAIL', 'Welcome to our platform', 'Dear {{name}}, welcome to our marketing platform!'),
('Campaign Announcement', 'EMAIL', 'New Campaign: {{campaign_title}}', 'Check out our latest campaign: {{campaign_title}}'),
('Promotional SMS', 'SMS', '', 'Hi {{name}}, enjoy {{discount}}% off on your next purchase!');

INSERT INTO campaigns (campaign_title, campaign_type, target_vehicle_segment, campaign_budget, target_leads, start_date, end_date, campaign_roi, campaign_results, lead_target, leads_generated) VALUES
('Summer Electric Vehicle Campaign', 'EMAIL', 'electric', 50000, JSON_OBJECT('target', 500, 'region', 'Bangalore'), '2026-04-15', '2026-05-15', 2.5, JSON_OBJECT('leads_generated', 450, 'conversions', 85), 500, 450),
('Spring Luxury Segment', 'MULTI_CHANNEL', 'luxury', 75000, JSON_OBJECT('target', 300, 'region', 'Delhi'), '2026-04-01', '2026-04-30', 3.2, JSON_OBJECT('leads_generated', 275, 'conversions', 60), 300, 275);

INSERT INTO campaign_messages (campaign_id, customer_id, template_id, channel, delivery_status, sent_at, delivered_at, opened_at, clicked_at) VALUES
(1, 1, 2, 'EMAIL', 'DELIVERED', '2026-04-15 10:00:00', '2026-04-15 10:05:00', '2026-04-15 10:30:00', '2026-04-15 11:00:00'),
(1, 2, 2, 'EMAIL', 'DELIVERED', '2026-04-15 10:00:00', '2026-04-15 10:05:00', '2026-04-15 10:45:00', '2026-04-15 11:15:00'),
(2, 3, 3, 'SMS', 'DELIVERED', '2026-04-01 09:00:00', '2026-04-01 09:02:00', NULL, NULL);

INSERT INTO campaign_metrics (campaign_id, metric_date, impressions, clicks, conversions, revenue_generated) VALUES
(1, '2026-04-15', 5000, 450, 85, 42500),
(1, '2026-04-16', 5200, 480, 92, 46000),
(2, '2026-04-01', 8000, 640, 120, 60000);

INSERT INTO consent_preferences (customer_id, channel, is_opted_in, source) VALUES
(1, 'EMAIL', TRUE, 'signup_form'),
(1, 'SMS', TRUE, 'signup_form'),
(2, 'EMAIL', TRUE, 'signup_form'),
(2, 'SMS', FALSE, 'signup_form'),
(3, 'EMAIL', TRUE, 'manual_entry'),
(3, 'SMS', TRUE, 'manual_entry');
